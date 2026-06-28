package com.example.compoundeffectV1_01.data.dataBaseRoom.tables.task

data class TaskChildRequirementUi(
    val requirementId: Int,
    val ruleId: Int,
    val parentTaskId: Int,
    val childTaskId: Int,

    val childTitle: String,
    val childColor: String,

    val contextType: String,

    val scheduleId: Int?,
    val parentRuleScheduleId: Int?,
    val occurrenceDateEpochDay: Long?,
    val listSessionId: Int?,

    val slotIndex: Int,
    val learningIndex: Int,
    val learningTargetCount: Int?,

    val status: String,

    val dueAtEpochMillis: Long?,
    val expiresAtEpochMillis: Long?,
    val completedAtEpochMillis: Long?,

    val sourceCatalogItemId: Int?
)