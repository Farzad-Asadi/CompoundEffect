package com.example.compoundeffectV1_01.data.dataBaseRoom.tables.task

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class TaskChildRepositoryImpl @Inject constructor(
    private val taskChildDao: TaskChildDao
) : TaskChildRepository {

    override fun observeRulesByParentTaskId(
        parentTaskId: Int
    ): Flow<List<TaskChildRuleEntity>> =
        taskChildDao.observeRulesByParentTaskId(parentTaskId)

    override suspend fun getRule(
        parentTaskId: Int,
        childTaskId: Int
    ): TaskChildRuleEntity? =
        taskChildDao.getRule(
            parentTaskId = parentTaskId,
            childTaskId = childTaskId
        )

    override suspend fun upsertRule(
        entity: TaskChildRuleEntity
    ): Long =
        taskChildDao.upsertRule(entity)

    override suspend fun disableRule(
        ruleId: Int
    ) =
        taskChildDao.disableRule(ruleId)

    override fun observeRequirementsByScheduleOccurrence(
        scheduleId: Int,
        occurrenceDateEpochDay: Long
    ): Flow<List<TaskChildRequirementEntity>> =
        taskChildDao.observeRequirementsByScheduleOccurrence(
            scheduleId = scheduleId,
            occurrenceDateEpochDay = occurrenceDateEpochDay
        )

    override fun observeRequirementsByRuleOccurrence(
        parentRuleScheduleId: Int,
        occurrenceDateEpochDay: Long
    ): Flow<List<TaskChildRequirementEntity>> =
        taskChildDao.observeRequirementsByRuleOccurrence(
            parentRuleScheduleId = parentRuleScheduleId,
            occurrenceDateEpochDay = occurrenceDateEpochDay
        )

    override fun observeRequirementsByListSession(
        listSessionId: Int
    ): Flow<List<TaskChildRequirementEntity>> =
        taskChildDao.observeRequirementsByListSession(listSessionId)

    override suspend fun upsertRequirement(
        entity: TaskChildRequirementEntity
    ): Long =
        taskChildDao.upsertRequirement(entity)

    override suspend fun upsertRequirements(
        entities: List<TaskChildRequirementEntity>
    ) =
        taskChildDao.upsertRequirements(entities)

    override suspend fun toggleRequirementCompleted(
        requirement: TaskChildRequirementEntity,
        completed: Boolean
    ) =
        taskChildDao.toggleRequirementCompleted(
            requirement = requirement,
            completed = completed
        )

    override suspend fun getActiveListSession(
        parentTaskId: Int
    ): TaskListSessionEntity? =
        taskChildDao.getActiveListSession(parentTaskId)

    override suspend fun upsertListSession(
        entity: TaskListSessionEntity
    ): Long =
        taskChildDao.upsertListSession(entity)

    override suspend fun closeListSession(
        sessionId: Int
    ) =
        taskChildDao.closeListSession(sessionId)

    override suspend fun searchReusableItemsByPrefix(
        itemType: String,
        query: String,
        limit: Int
    ): List<ReusableListItemEntity> =
        taskChildDao.searchReusableItemsByPrefix(
            itemType = itemType,
            query = query,
            limit = limit
        )

    override suspend fun getReusableItemByNormalizedTitle(
        itemType: String,
        normalizedTitle: String
    ): ReusableListItemEntity? =
        taskChildDao.getReusableItemByNormalizedTitle(
            itemType = itemType,
            normalizedTitle = normalizedTitle
        )

    override suspend fun upsertReusableItem(
        entity: ReusableListItemEntity
    ): Long =
        taskChildDao.upsertReusableItem(entity)

    override suspend fun increaseReusableItemUsage(
        itemId: Int
    ) =
        taskChildDao.increaseReusableItemUsage(itemId)

    override fun observeDirectChildTasks(
        parentTaskId: Int
    ): Flow<List<TaskEntity>> =
        taskChildDao.observeDirectChildTasks(parentTaskId)

    override suspend fun getDirectChildTasks(
        parentTaskId: Int
    ): List<TaskEntity> =
        taskChildDao.getDirectChildTasks(parentTaskId)

    override suspend fun ensureDefaultOccurrenceRulesForDirectChildren(
        parentTaskId: Int
    ) =
        taskChildDao.ensureDefaultOccurrenceRulesForDirectChildren(parentTaskId)

    override suspend fun ensureRequirementsForParentOccurrence(
        parentTaskId: Int,
        scheduleId: Int?,
        parentRuleScheduleId: Int?,
        occurrenceDateEpochDay: Long
    ) =
        taskChildDao.ensureRequirementsForParentOccurrence(
            parentTaskId = parentTaskId,
            scheduleId = scheduleId,
            parentRuleScheduleId = parentRuleScheduleId,
            occurrenceDateEpochDay = occurrenceDateEpochDay
        )

    override fun observeRequirementUiByScheduleOccurrence(
        scheduleId: Int,
        occurrenceDateEpochDay: Long
    ): Flow<List<TaskChildRequirementUi>> =
        taskChildDao.observeRequirementUiByScheduleOccurrence(
            scheduleId = scheduleId,
            occurrenceDateEpochDay = occurrenceDateEpochDay
        )

    override fun observeRequirementUiByRuleOccurrence(
        parentRuleScheduleId: Int,
        occurrenceDateEpochDay: Long
    ): Flow<List<TaskChildRequirementUi>> =
        taskChildDao.observeRequirementUiByRuleOccurrence(
            parentRuleScheduleId = parentRuleScheduleId,
            occurrenceDateEpochDay = occurrenceDateEpochDay
        )

    override suspend fun toggleRequirementCompletedById(
        requirementId: Int,
        completed: Boolean
    ) =
        taskChildDao.toggleRequirementCompletedById(
            requirementId = requirementId,
            completed = completed
        )

    override fun observeRequirementSummariesByDateRange(
        startEpochDay: Long,
        endEpochDay: Long
    ): Flow<List<TaskChildRequirementSummaryUi>> =
        taskChildDao.observeRequirementSummariesByDateRange(
            startEpochDay = startEpochDay,
            endEpochDay = endEpochDay
        )

    override fun observeRequirementUiByDateRange(
        startEpochDay: Long,
        endEpochDay: Long
    ): Flow<List<TaskChildRequirementUi>> =
        taskChildDao.observeRequirementUiByDateRange(
            startEpochDay = startEpochDay,
            endEpochDay = endEpochDay
        )

    override fun observeRulesByChildTaskId(
        childTaskId: Int
    ): Flow<List<TaskChildRuleEntity>> =
        taskChildDao.observeRulesByChildTaskId(childTaskId)

    override suspend fun cancelOpenRequirementsByRuleId(
        ruleId: Int
    ) =
        taskChildDao.cancelOpenRequirementsByRuleId(ruleId)

    override fun observeRequirementUiForParentOccurrence(
        parentTaskId: Int,
        scheduleId: Int?,
        parentRuleScheduleId: Int?,
        occurrenceDateEpochDay: Long
    ): Flow<List<TaskChildRequirementUi>> =
        taskChildDao.observeRequirementUiForParentOccurrence(
            parentTaskId = parentTaskId,
            scheduleId = scheduleId,
            parentRuleScheduleId = parentRuleScheduleId,
            occurrenceDateEpochDay = occurrenceDateEpochDay
        )



}