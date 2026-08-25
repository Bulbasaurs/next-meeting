package calendar

import model.MeetingInfo
import net.fortuna.ical4j.data.CalendarBuilder
import net.fortuna.ical4j.model.Component
import net.fortuna.ical4j.model.DateTime
import net.fortuna.ical4j.model.Period
import net.fortuna.ical4j.model.Property
import net.fortuna.ical4j.model.component.VEvent
import java.io.File
import java.io.FileInputStream
import java.util.Date

class CalendarService(private val icsFile: File) {

    fun fetchNextMeeting(): MeetingInfo? {
        val calendar = CalendarBuilder().build(FileInputStream(icsFile))
        val now = Date()
        val windowEnd = Date(now.time + 30L * 24 * 60 * 60 * 1000) // 30 days ahead

        data class Occurrence(val start: Date, val end: Date, val event: VEvent)

        val upcoming = mutableListOf<Occurrence>()

        @Suppress("UNCHECKED_CAST")
        val events = calendar.getComponents<VEvent>(Component.VEVENT)

        for (event in events) {
            val startDate = event.startDate?.date ?: continue
            // Skip all-day events — they don't have a time component
            if (startDate !is DateTime) continue
            val endDate = event.endDate?.date ?: startDate

            if (event.getProperty<Property>("RRULE") != null) {
                // Recurring event — expand occurrences over the next 30 days
                runCatching {
                    val period = Period(DateTime(now), DateTime(windowEnd))
                    for (p in event.calculateRecurrenceSet(period)) {
                        if (!p.start.before(now)) {
                            upcoming.add(Occurrence(p.start, p.end, event))
                        }
                    }
                }
            } else {
                if (!startDate.before(now)) {
                    upcoming.add(Occurrence(startDate, endDate, event))
                }
            }
        }

        val next = upcoming.minByOrNull { it.start.time } ?: return null

        return MeetingInfo(
            title = next.event.summary?.value ?: "(No title)",
            startTime = next.start.toInstant(),
            endTime = next.end.toInstant(),
            videoLink = extractVideoLink(next.event)
        )
    }

    private fun extractVideoLink(event: VEvent): String? {
        // Check the URL property (some clients export meeting links here)
        event.getProperty<Property>("URL")?.value
            ?.takeIf { it.startsWith("http") }
            ?.let { return it }

        // Regex scan of location + description for Zoom / Meet / Teams links
        val text = listOfNotNull(event.location?.value, event.description?.value).joinToString(" ")
        val pattern = Regex(
            """https?://\S*(?:zoom\.us/[jm]/|teams\.microsoft\.com/l/meetup-join|meet\.google\.com/)\S*"""
        )
        return pattern.find(text)?.value?.trimEnd(')')
    }
}
