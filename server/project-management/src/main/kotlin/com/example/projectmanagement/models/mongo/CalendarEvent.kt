package com.example.projectmanagement.models.mongo

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.util.*

@Document(collection = "calendar_events")
data class CalendarEvent(
    @Id
    var id: String? = null,
    var superObjectId: String? = null,
    var title: String? = null,
    var description: String? = null,
    var startTime: Date? = null,
    var endTime: Date? = null,
    var participants: List<Int>? = null
)
