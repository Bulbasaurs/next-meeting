package model

data class CalendarData(
    val nextMeeting: MeetingInfo?,
    val followingMeeting: MeetingInfo?,  // next meeting same day, after nextMeeting
    val tomorrowFirst: MeetingInfo?,     // first accepted meeting tomorrow
    val tomorrowCount: Int,              // total accepted meetings tomorrow
)
