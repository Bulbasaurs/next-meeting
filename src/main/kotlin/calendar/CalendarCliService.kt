package calendar

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import model.CalendarData
import model.MeetingInfo
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class CalendarCliService {

    fun runAuthLogin(onUrlFound: (String) -> Unit = {}) {
        val gwsPath = findGws()
        val proc = ProcessBuilder(gwsPath, "auth", "login", "--scopes", "https://www.googleapis.com/auth/calendar.readonly")
            .redirectErrorStream(true)
            .start()

        val urlRegex = Regex("""https://\S+""")
        var urlReported = false
        proc.inputStream.bufferedReader().forEachLine { line ->
            if (!urlReported) {
                val url = urlRegex.find(line)?.value?.trimEnd('.')
                if (url != null) {
                    urlReported = true
                    onUrlFound(url)
                    runCatching { java.awt.Desktop.getDesktop().browse(java.net.URI(url)) }
                }
            }
        }

        proc.waitFor()
    }

    fun fetchCalendarData(): CalendarData {
        val json = runGws()
        return parseCalendarData(json)
    }

    private fun runGws(): String {
        val gwsPath = findGws()
        val zone = ZoneId.systemDefault()
        val timeMin = DateTimeFormatter.ISO_INSTANT.format(Instant.now())
        val timeMax = DateTimeFormatter.ISO_INSTANT.format(
            LocalDate.now(zone).plusDays(2).atStartOfDay(zone).toInstant()
        )
        val params = """{"calendarId":"primary","timeMin":"$timeMin","timeMax":"$timeMax","maxResults":50,"orderBy":"startTime","singleEvents":true}"""
        val proc = ProcessBuilder(gwsPath, "calendar", "events", "list", "--params", params).start()
        val stdout = proc.inputStream.bufferedReader().readText()
        val stderr = proc.errorStream.bufferedReader().readText()
        val exit = proc.waitFor()
        if (exit != 0) throw RuntimeException(stderr.ifBlank { "gws exited with code $exit" })
        return stdout
    }

    private fun findGws(): String {
        val isWindows = System.getProperty("os.name").lowercase().contains("win")

        if (isWindows) {
            val appData = System.getenv("APPDATA")
            listOfNotNull(appData?.let { "$it\\npm\\gws.cmd" })
                .firstOrNull { File(it).exists() }?.let { return it }
        } else {
            listOf("/opt/homebrew/bin/gws", "/usr/local/bin/gws", "/usr/bin/gws")
                .firstOrNull { File(it).canExecute() }?.let { return it }
        }

        val lookupCmd = if (isWindows) listOf("where", "gws") else listOf("which", "gws")
        runCatching {
            val proc = ProcessBuilder(lookupCmd).start()
            val path = proc.inputStream.bufferedReader().readText().trim().lineSequence().firstOrNull().orEmpty()
            proc.waitFor()
            if (path.isNotEmpty() && File(path).exists()) return path
        }

        throw RuntimeException("gws not found. Install with:\n  npm install -g @google/gws\nThen run:\n  gws auth setup")
    }

    private fun parseCalendarData(json: String): CalendarData {
        val root = JsonParser.parseString(json).asJsonObject
        val items = root.getAsJsonArray("items") ?: return CalendarData(null, null, null, 0)
        val now = Instant.now()
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        val tomorrow = today.plusDays(1)

        data class ParsedEvent(val info: MeetingInfo, val raw: JsonObject)

        val allEvents = items.mapNotNull { element ->
            val event = element.asJsonObject
            val startStr = event.getAsJsonObject("start")?.get("dateTime")?.asString ?: return@mapNotNull null
            val endStr = event.getAsJsonObject("end")?.get("dateTime")?.asString ?: startStr
            val startTime = OffsetDateTime.parse(startStr).toInstant()
            val endTime = OffsetDateTime.parse(endStr).toInstant()
            ParsedEvent(
                MeetingInfo(
                    title = event.get("summary")?.asString ?: "(No title)",
                    startTime = startTime,
                    endTime = endTime,
                    videoLink = extractVideoLink(event),
                    streamLink = extractStreamLink(event),
                ),
                event,
            )
        }

        // Tomorrow: all events on the next calendar day that the user accepted
        val tomorrowAccepted = allEvents
            .filter { LocalDate.ofInstant(it.info.startTime, zone) == tomorrow && isAccepted(it.raw) }

        // Next: first event today that hasn't ended yet
        val nextIdx = allEvents.indexOfFirst {
            it.info.endTime.isAfter(now) && LocalDate.ofInstant(it.info.startTime, zone) == today
        }
        if (nextIdx == -1) return CalendarData(
            nextMeeting = null,
            followingMeeting = null,
            tomorrowFirst = tomorrowAccepted.firstOrNull()?.info,
            tomorrowCount = tomorrowAccepted.size,
        )
        val next = allEvents[nextIdx].info

        // Following: next event also today
        val following = allEvents.drop(nextIdx + 1)
            .firstOrNull { LocalDate.ofInstant(it.info.startTime, zone) == today }
            ?.info

        return CalendarData(
            nextMeeting = next,
            followingMeeting = following,
            tomorrowFirst = tomorrowAccepted.firstOrNull()?.info,
            tomorrowCount = tomorrowAccepted.size,
        )
    }

    private fun isAccepted(event: JsonObject): Boolean {
        val attendees = event.getAsJsonArray("attendees") ?: return true // organizer
        val self = attendees.map { it.asJsonObject }.firstOrNull { it.get("self")?.asBoolean == true }
            ?: return true // not listed as attendee, treat as organizer
        return self.get("responseStatus")?.asString == "accepted"
    }

    private fun extractStreamLink(event: JsonObject): String? {
        val streamRegex = Regex("""https://stream\.meet\.google\.com/stream/\S*""")

        // Check conferenceData entryPoints — stream events have no entryPointType, just a uri
        event.getAsJsonObject("conferenceData")
            ?.getAsJsonArray("entryPoints")
            ?.map { it.asJsonObject }
            ?.mapNotNull { it.get("uri")?.asString }
            ?.firstOrNull { streamRegex.containsMatchIn(it) }
            ?.let { return it }

        // Fall back to searching description/location for stream URLs
        val text = listOfNotNull(
            event.get("location")?.asString,
            event.get("description")?.asString,
        ).joinToString(" ")

        return streamRegex.find(text)?.value?.trimEnd(')')
    }

    private fun extractVideoLink(event: JsonObject): String? {
        event.get("hangoutLink")?.asString?.let { return it }
        event.getAsJsonObject("conferenceData")
            ?.getAsJsonArray("entryPoints")
            ?.map { it.asJsonObject }
            ?.firstOrNull { it.get("entryPointType")?.asString == "video" }
            ?.get("uri")?.asString
            ?.let { return it }
        val text = listOfNotNull(
            event.get("location")?.asString,
            event.get("description")?.asString,
        ).joinToString(" ")
        return Regex("""https?://\S*(?:zoom\.us/[jm]/|teams\.microsoft\.com/l/meetup-join|meet\.google\.com/)\S*""")
            .find(text)?.value?.trimEnd(')')
    }
}
