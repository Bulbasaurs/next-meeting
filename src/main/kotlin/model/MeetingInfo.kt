package model

import java.time.Instant

data class MeetingInfo(
    val title: String,
    val startTime: Instant,
    val endTime: Instant,
    val videoLink: String?,
    val streamLink: String? = null,
)
