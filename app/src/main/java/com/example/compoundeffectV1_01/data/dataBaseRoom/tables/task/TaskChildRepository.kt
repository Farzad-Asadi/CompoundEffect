package com.example.compoundeffectV1_01.data.dataBaseRoom.tables.task

import kotlinx.coroutines.flow.Flow

interface TaskChildRepository {

    fun observeRulesByParentTaskId(
        parentTaskId: Int
    ): Flow<List<TaskChildRuleEntity>>

    suspend fun getRule(
        parentTaskId: Int,
        childTaskId: Int
    ): TaskChildRuleEntity?

    suspend fun upsertRule(
        entity: TaskChildRuleEntity
    ): Long

    suspend fun disableRule(
        ruleId: Int
    )

    fun observeRequirementsByScheduleOccurrence(
        scheduleId: Int,
        occurrenceDateEpochDay: Long
    ): Flow<List<TaskChildRequirementEntity>>

    fun observeRequirementsByRuleOccurrence(
        parentRuleScheduleId: Int,
        occurrenceDateEpochDay: Long
    ): Flow<List<TaskChildRequirementEntity>>

    fun observeRequirementsByListSession(
        listSessionId: Int
    ): Flow<List<TaskChildRequirementEntity>>

    suspend fun upsertRequirement(
        entity: TaskChildRequirementEntity
    ): Long

    suspend fun upsertRequirements(
        entities: List<TaskChildRequirementEntity>
    )

    suspend fun toggleRequirementCompleted(
        requirement: TaskChildRequirementEntity,
        completed: Boolean
    )

    suspend fun getActiveListSession(
        parentTaskId: Int
    ): TaskListSessionEntity?

    suspend fun upsertListSession(
        entity: TaskListSessionEntity
    ): Long

    suspend fun closeListSession(
        sessionId: Int
    )

    suspend fun searchReusableItemsByPrefix(
        itemType: String,
        query: String,
        limit: Int = 20
    ): List<ReusableListItemEntity>

    suspend fun getReusableItemByNormalizedTitle(
        itemType: String,
        normalizedTitle: String
    ): ReusableListItemEntity?

    suspend fun upsertReusableItem(
        entity: ReusableListItemEntity
    ): Long

    suspend fun increaseReusableItemUsage(
        itemId: Int
    )

    fun observeDirectChildTasks(
        parentTaskId: Int
    ): Flow<List<TaskEntity>>

    suspend fun getDirectChildTasks(
        parentTaskId: Int
    ): List<TaskEntity>

    suspend fun ensureDefaultOccurrenceRulesForDirectChildren(
        parentTaskId: Int
    )

    suspend fun ensureRequirementsForParentOccurrence(
        parentTaskId: Int,
        scheduleId: Int?,
        parentRuleScheduleId: Int?,
        occurrenceDateEpochDay: Long
    )

    fun observeRequirementUiByScheduleOccurrence(
        scheduleId: Int,
        occurrenceDateEpochDay: Long
    ): Flow<List<TaskChildRequirementUi>>

    fun observeRequirementUiByRuleOccurrence(
        parentRuleScheduleId: Int,
        occurrenceDateEpochDay: Long
    ): Flow<List<TaskChildRequirementUi>>

    suspend fun toggleRequirementCompletedById(
        requirementId: Int,
        completed: Boolean
    )

    fun observeRequirementSummariesByDateRange(
        startEpochDay: Long,
        endEpochDay: Long
    ): Flow<List<TaskChildRequirementSummaryUi>>

    fun observeRequirementUiByDateRange(
        startEpochDay: Long,
        endEpochDay: Long
    ): Flow<List<TaskChildRequirementUi>>
}