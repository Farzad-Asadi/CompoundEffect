package com.example.compoundeffectV1_01.data.dataBaseRoom.tables.task

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskChildDao {

    // -------------------------
    // Rules
    // -------------------------

    @Query("""
        SELECT * FROM task_child_rule
        WHERE parentTaskId = :parentTaskId
          AND isEnabled = 1
        ORDER BY sortOrder ASC, id ASC
    """)
    fun observeRulesByParentTaskId(
        parentTaskId: Int
    ): Flow<List<TaskChildRuleEntity>>

    @Query("""
        SELECT * FROM task_child_rule
        WHERE childTaskId = :childTaskId
          AND isEnabled = 1
        ORDER BY id ASC
    """)
    fun observeRulesByChildTaskId(
        childTaskId: Int
    ): Flow<List<TaskChildRuleEntity>>

    @Query("""
        SELECT * FROM task_child_rule
        WHERE parentTaskId = :parentTaskId
          AND childTaskId = :childTaskId
        LIMIT 1
    """)
    suspend fun getRule(
        parentTaskId: Int,
        childTaskId: Int
    ): TaskChildRuleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRule(
        entity: TaskChildRuleEntity
    ): Long

    @Query("""
        UPDATE task_child_rule
        SET isEnabled = 0,
            updatedAtEpochMillis = :updatedAt
        WHERE id = :ruleId
    """)
    suspend fun disableRule(
        ruleId: Int,
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query("""
        DELETE FROM task_child_rule
        WHERE id = :ruleId
    """)
    suspend fun deleteRuleById(
        ruleId: Int
    )


    // -------------------------
    // Requirements
    // -------------------------

    @Query("""
        SELECT * FROM task_child_requirement
        WHERE parentTaskId = :parentTaskId
          AND occurrenceDateEpochDay = :occurrenceDateEpochDay
        ORDER BY childTaskId ASC, slotIndex ASC, learningIndex ASC, id ASC
    """)
    fun observeRequirementsByParentAndDate(
        parentTaskId: Int,
        occurrenceDateEpochDay: Long
    ): Flow<List<TaskChildRequirementEntity>>

    @Query("""
        SELECT * FROM task_child_requirement
        WHERE scheduleId = :scheduleId
          AND occurrenceDateEpochDay = :occurrenceDateEpochDay
        ORDER BY childTaskId ASC, slotIndex ASC, learningIndex ASC, id ASC
    """)
    fun observeRequirementsByScheduleOccurrence(
        scheduleId: Int,
        occurrenceDateEpochDay: Long
    ): Flow<List<TaskChildRequirementEntity>>

    @Query("""
        SELECT * FROM task_child_requirement
        WHERE parentRuleScheduleId = :parentRuleScheduleId
          AND occurrenceDateEpochDay = :occurrenceDateEpochDay
        ORDER BY childTaskId ASC, slotIndex ASC, learningIndex ASC, id ASC
    """)
    fun observeRequirementsByRuleOccurrence(
        parentRuleScheduleId: Int,
        occurrenceDateEpochDay: Long
    ): Flow<List<TaskChildRequirementEntity>>

    @Query("""
        SELECT * FROM task_child_requirement
        WHERE listSessionId = :listSessionId
        ORDER BY status ASC, childTaskId ASC, slotIndex ASC, id ASC
    """)
    fun observeRequirementsByListSession(
        listSessionId: Int
    ): Flow<List<TaskChildRequirementEntity>>

    @Query("""
        SELECT * FROM task_child_requirement
        WHERE uniqueKey = :uniqueKey
        LIMIT 1
    """)
    suspend fun getRequirementByUniqueKey(
        uniqueKey: String
    ): TaskChildRequirementEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRequirement(
        entity: TaskChildRequirementEntity
    ): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRequirements(
        entities: List<TaskChildRequirementEntity>
    )

    @Query("""
        UPDATE task_child_requirement
        SET status = :status,
            completedAtEpochMillis = :completedAt,
            updatedAtEpochMillis = :updatedAt
        WHERE id = :requirementId
    """)
    suspend fun updateRequirementCompleteState(
        requirementId: Int,
        status: String,
        completedAt: Long?,
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query("""
        UPDATE task_child_requirement
        SET status = :status,
            skippedAtEpochMillis = :skippedAt,
            updatedAtEpochMillis = :updatedAt
        WHERE id = :requirementId
    """)
    suspend fun updateRequirementSkippedState(
        requirementId: Int,
        status: String,
        skippedAt: Long?,
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query("""
        DELETE FROM task_child_requirement
        WHERE id = :requirementId
    """)
    suspend fun deleteRequirementById(
        requirementId: Int
    )


    // -------------------------
    // List Sessions
    // -------------------------

    @Query("""
        SELECT * FROM task_list_session
        WHERE parentTaskId = :parentTaskId
          AND status = :status
        ORDER BY startedAtEpochMillis DESC
        LIMIT 1
    """)
    suspend fun getActiveListSession(
        parentTaskId: Int,
        status: String = TaskListSessionStatus.ACTIVE
    ): TaskListSessionEntity?

    @Query("""
        SELECT * FROM task_list_session
        WHERE parentTaskId = :parentTaskId
        ORDER BY startedAtEpochMillis DESC
    """)
    fun observeListSessionsByParent(
        parentTaskId: Int
    ): Flow<List<TaskListSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertListSession(
        entity: TaskListSessionEntity
    ): Long

    @Query("""
        UPDATE task_list_session
        SET status = :status,
            closedAtEpochMillis = :closedAt,
            updatedAtEpochMillis = :updatedAt
        WHERE id = :sessionId
    """)
    suspend fun closeListSession(
        sessionId: Int,
        status: String = TaskListSessionStatus.CLOSED,
        closedAt: Long = System.currentTimeMillis(),
        updatedAt: Long = System.currentTimeMillis()
    )


    // -------------------------
    // Reusable List Items
    // -------------------------

    @Query("""
        SELECT * FROM reusable_list_item
        WHERE itemType = :itemType
          AND isArchived = 0
          AND normalizedTitle LIKE :query || '%'
        ORDER BY usageCount DESC, lastUsedAtEpochMillis DESC, title ASC
        LIMIT :limit
    """)
    suspend fun searchReusableItemsByPrefix(
        itemType: String,
        query: String,
        limit: Int = 20
    ): List<ReusableListItemEntity>

    @Query("""
        SELECT * FROM reusable_list_item
        WHERE itemType = :itemType
          AND normalizedTitle = :normalizedTitle
        LIMIT 1
    """)
    suspend fun getReusableItemByNormalizedTitle(
        itemType: String,
        normalizedTitle: String
    ): ReusableListItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertReusableItem(
        entity: ReusableListItemEntity
    ): Long

    @Query("""
        UPDATE reusable_list_item
        SET usageCount = usageCount + 1,
            lastUsedAtEpochMillis = :usedAt,
            updatedAtEpochMillis = :updatedAt
        WHERE id = :itemId
    """)
    suspend fun increaseReusableItemUsage(
        itemId: Int,
        usedAt: Long = System.currentTimeMillis(),
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query("""
        UPDATE reusable_list_item
        SET completedCount = completedCount + 1,
            lastCompletedAtEpochMillis = :completedAt,
            updatedAtEpochMillis = :updatedAt
        WHERE id = :itemId
    """)
    suspend fun increaseReusableItemCompleted(
        itemId: Int,
        completedAt: Long = System.currentTimeMillis(),
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query("""
        UPDATE reusable_list_item
        SET isArchived = 1,
            updatedAtEpochMillis = :updatedAt
        WHERE id = :itemId
    """)
    suspend fun archiveReusableItem(
        itemId: Int,
        updatedAt: Long = System.currentTimeMillis()
    )


    // -------------------------
    // Simple transaction helpers
    // -------------------------

    @Transaction
    suspend fun toggleRequirementCompleted(
        requirement: TaskChildRequirementEntity,
        completed: Boolean
    ) {
        val now = System.currentTimeMillis()

        updateRequirementCompleteState(
            requirementId = requirement.id,
            status = if (completed) {
                TaskChildRequirementStatus.COMPLETE
            } else {
                TaskChildRequirementStatus.INCOMPLETE
            },
            completedAt = if (completed) now else null,
            updatedAt = now
        )

        if (completed && requirement.sourceCatalogItemId != null) {
            increaseReusableItemCompleted(
                itemId = requirement.sourceCatalogItemId,
                completedAt = now,
                updatedAt = now
            )
        }

        if (completed) {
            createNextLearningRequirementIfNeeded(
                completedRequirement = requirement,
                completedAt = now
            )
        }
    }

    @Query("""
    SELECT * FROM task
    WHERE parentTaskId = :parentTaskId
    ORDER BY siblingIndex ASC, id ASC
""")
    fun observeDirectChildTasks(
        parentTaskId: Int
    ): Flow<List<TaskEntity>>

    @Query("""
    SELECT * FROM task
    WHERE parentTaskId = :parentTaskId
    ORDER BY siblingIndex ASC, id ASC
""")
    suspend fun getDirectChildTasks(
        parentTaskId: Int
    ): List<TaskEntity>

    @Query("""
    SELECT * FROM task_child_rule
    WHERE parentTaskId = :parentTaskId
      AND isEnabled = 1
    ORDER BY sortOrder ASC, id ASC
""")
    suspend fun getEnabledRulesByParentTaskId(
        parentTaskId: Int
    ): List<TaskChildRuleEntity>



    @Transaction
    suspend fun ensureDefaultOccurrenceRulesForDirectChildren(
        parentTaskId: Int
    ) {
        val children = getDirectChildTasks(parentTaskId)

        for ((index, child) in children.withIndex()) {
            val childId = child.id ?: continue

            val existingRule = getRule(
                parentTaskId = parentTaskId,
                childTaskId = childId
            )

            if (existingRule == null) {
                upsertRule(
                    TaskChildRuleEntity(
                        parentTaskId = parentTaskId,
                        childTaskId = childId,
                        ruleType = TaskChildRuleType.ONCE_PER_PARENT_OCCURRENCE,
                        resetScope = TaskChildResetScope.PARENT_OCCURRENCE,
                        isRequired = true,
                        isEnabled = true,
                        sortOrder = index,
                        targetCount = 1,
                        timesPerOccurrence = 1,
                        showInTimelineCard = true,
                        showInBottomSheet = true
                    )
                )
            }
        }
    }



    @Transaction
    suspend fun ensureRequirementsForParentOccurrence(
        parentTaskId: Int,
        scheduleId: Int?,
        parentRuleScheduleId: Int?,
        occurrenceDateEpochDay: Long
    ) {
        if (scheduleId == null && parentRuleScheduleId == null) return

        ensureDefaultOccurrenceRulesForDirectChildren(parentTaskId)

        val rules = getEnabledRulesByParentTaskId(parentTaskId)

        val occurrenceContextType = if (scheduleId != null) {
            TaskChildRequirementContextType.SCHEDULE_OCCURRENCE
        } else {
            TaskChildRequirementContextType.RULE_OCCURRENCE
        }

        for (rule in rules) {
            var contextType = occurrenceContextType
            var effectiveScheduleId = scheduleId
            var effectiveParentRuleScheduleId = parentRuleScheduleId
            var effectiveOccurrenceDateEpochDay: Long? = occurrenceDateEpochDay
            var slotCount = 0
            var learningTargetCount: Int? = null

            when (rule.ruleType) {
                TaskChildRuleType.ONCE_PER_PARENT_OCCURRENCE -> {
                    contextType = occurrenceContextType
                    effectiveScheduleId = scheduleId
                    effectiveParentRuleScheduleId = parentRuleScheduleId
                    effectiveOccurrenceDateEpochDay = occurrenceDateEpochDay
                    slotCount = 1
                }

                TaskChildRuleType.N_TIMES_PER_PARENT_OCCURRENCE -> {
                    contextType = occurrenceContextType
                    effectiveScheduleId = scheduleId
                    effectiveParentRuleScheduleId = parentRuleScheduleId
                    effectiveOccurrenceDateEpochDay = occurrenceDateEpochDay
                    slotCount = rule.timesPerOccurrence.coerceAtLeast(1)
                }

                TaskChildRuleType.ONCE_PER_DAY -> {
                    contextType = TaskChildRequirementContextType.DAY
                    effectiveScheduleId = null
                    effectiveParentRuleScheduleId = null
                    effectiveOccurrenceDateEpochDay = occurrenceDateEpochDay
                    slotCount = 1
                }

                TaskChildRuleType.N_TIMES_PER_DAY -> {
                    contextType = TaskChildRequirementContextType.DAY
                    effectiveScheduleId = null
                    effectiveParentRuleScheduleId = null
                    effectiveOccurrenceDateEpochDay = occurrenceDateEpochDay
                    slotCount = rule.timesPerDay.coerceAtLeast(1)
                }

                TaskChildRuleType.ONCE_PER_PARENT_LIFETIME -> {
                    contextType = TaskChildRequirementContextType.PARENT_LIFETIME
                    effectiveScheduleId = null
                    effectiveParentRuleScheduleId = null
                    effectiveOccurrenceDateEpochDay = null
                    slotCount = 1
                }

                TaskChildRuleType.G5_LEARNING -> {
                    contextType = TaskChildRequirementContextType.LEARNING_CYCLE
                    effectiveScheduleId = null
                    effectiveParentRuleScheduleId = null
                    effectiveOccurrenceDateEpochDay = null
                    slotCount = 1
                    learningTargetCount = rule.g5TargetCount.coerceAtLeast(1)
                }

                TaskChildRuleType.MANUAL_RESET -> {
                    contextType = TaskChildRequirementContextType.MANUAL
                    effectiveScheduleId = null
                    effectiveParentRuleScheduleId = null
                    effectiveOccurrenceDateEpochDay = null
                    slotCount = 1
                }

                TaskChildRuleType.MANUAL_LIST_ITEM -> {
                    slotCount = 0
                }
            }

            if (slotCount <= 0) continue

            for (slotIndex in 0 until slotCount) {
                val uniqueKey = buildString {
                    append("rule:")
                    append(rule.id)
                    append("|ctx:")
                    append(contextType)
                    append("|schedule:")
                    append(effectiveScheduleId ?: "null")
                    append("|parentRule:")
                    append(effectiveParentRuleScheduleId ?: "null")
                    append("|date:")
                    append(effectiveOccurrenceDateEpochDay ?: "null")
                    append("|slot:")
                    append(slotIndex)
                    append("|learn:")
                    append(0)
                }

                val existing = getRequirementByUniqueKey(uniqueKey)
                if (existing != null) continue

                upsertRequirement(
                    TaskChildRequirementEntity(
                        uniqueKey = uniqueKey,
                        ruleId = rule.id,
                        parentTaskId = parentTaskId,
                        childTaskId = rule.childTaskId,
                        contextType = contextType,
                        scheduleId = effectiveScheduleId,
                        parentRuleScheduleId = effectiveParentRuleScheduleId,
                        occurrenceDateEpochDay = effectiveOccurrenceDateEpochDay,
                        listSessionId = null,
                        slotIndex = slotIndex,
                        learningIndex = 0,
                        learningTargetCount = learningTargetCount,
                        status = TaskChildRequirementStatus.INCOMPLETE,
                        sourceCatalogItemId = rule.sourceCatalogItemId
                    )
                )
            }
        }
    }



    @Query("""
    SELECT 
        r.id AS requirementId,
        r.ruleId AS ruleId,
        r.parentTaskId AS parentTaskId,
        r.childTaskId AS childTaskId,

        t.name AS childTitle,
        t.color AS childColor,

        r.contextType AS contextType,

        r.scheduleId AS scheduleId,
        r.parentRuleScheduleId AS parentRuleScheduleId,
        r.occurrenceDateEpochDay AS occurrenceDateEpochDay,
        r.listSessionId AS listSessionId,

        r.slotIndex AS slotIndex,
        r.learningIndex AS learningIndex,
        r.learningTargetCount AS learningTargetCount,

        r.status AS status,

        r.dueAtEpochMillis AS dueAtEpochMillis,
        r.expiresAtEpochMillis AS expiresAtEpochMillis,
        r.completedAtEpochMillis AS completedAtEpochMillis,

        r.sourceCatalogItemId AS sourceCatalogItemId
    FROM task_child_requirement r
    INNER JOIN task t ON t.id = r.childTaskId
    WHERE r.scheduleId = :scheduleId
      AND r.occurrenceDateEpochDay = :occurrenceDateEpochDay
    ORDER BY 
    t.siblingIndex ASC,
    t.id ASC,
    r.slotIndex ASC,
    r.learningIndex ASC,
    r.id ASC
""")
    fun observeRequirementUiByScheduleOccurrence(
        scheduleId: Int,
        occurrenceDateEpochDay: Long
    ): Flow<List<TaskChildRequirementUi>>

    @Query("""
    SELECT 
        r.id AS requirementId,
        r.ruleId AS ruleId,
        r.parentTaskId AS parentTaskId,
        r.childTaskId AS childTaskId,

        t.name AS childTitle,
        t.color AS childColor,

        r.contextType AS contextType,

        r.scheduleId AS scheduleId,
        r.parentRuleScheduleId AS parentRuleScheduleId,
        r.occurrenceDateEpochDay AS occurrenceDateEpochDay,
        r.listSessionId AS listSessionId,

        r.slotIndex AS slotIndex,
        r.learningIndex AS learningIndex,
        r.learningTargetCount AS learningTargetCount,

        r.status AS status,

        r.dueAtEpochMillis AS dueAtEpochMillis,
        r.expiresAtEpochMillis AS expiresAtEpochMillis,
        r.completedAtEpochMillis AS completedAtEpochMillis,

        r.sourceCatalogItemId AS sourceCatalogItemId
    FROM task_child_requirement r
    INNER JOIN task t ON t.id = r.childTaskId
    WHERE r.parentRuleScheduleId = :parentRuleScheduleId
      AND r.occurrenceDateEpochDay = :occurrenceDateEpochDay
    ORDER BY 
    t.siblingIndex ASC,
    t.id ASC,
    r.slotIndex ASC,
    r.learningIndex ASC,
    r.id ASC
""")
    fun observeRequirementUiByRuleOccurrence(
        parentRuleScheduleId: Int,
        occurrenceDateEpochDay: Long
    ): Flow<List<TaskChildRequirementUi>>

    @Query("""
    SELECT * FROM task_child_requirement
    WHERE id = :requirementId
    LIMIT 1
""")
    suspend fun getRequirementById(
        requirementId: Int
    ): TaskChildRequirementEntity?

    @Transaction
    suspend fun toggleRequirementCompletedById(
        requirementId: Int,
        completed: Boolean
    ) {
        val requirement = getRequirementById(requirementId) ?: return

        toggleRequirementCompleted(
            requirement = requirement,
            completed = completed
        )
    }


    @Query("""
    SELECT
        parentTaskId AS parentTaskId,
        scheduleId AS scheduleId,
        parentRuleScheduleId AS parentRuleScheduleId,
        occurrenceDateEpochDay AS occurrenceDateEpochDay,

        COUNT(*) AS totalCount,

        SUM(
            CASE 
                WHEN status = 'COMPLETE' THEN 1 
                ELSE 0 
            END
        ) AS completedCount

    FROM task_child_requirement
    WHERE occurrenceDateEpochDay BETWEEN :startEpochDay AND :endEpochDay
      AND status != 'CANCELLED'
    GROUP BY 
        parentTaskId,
        scheduleId,
        parentRuleScheduleId,
        occurrenceDateEpochDay
""")
    fun observeRequirementSummariesByDateRange(
        startEpochDay: Long,
        endEpochDay: Long
    ): Flow<List<TaskChildRequirementSummaryUi>>

    @Query("""
    SELECT 
        r.id AS requirementId,
        r.ruleId AS ruleId,
        r.parentTaskId AS parentTaskId,
        r.childTaskId AS childTaskId,

        t.name AS childTitle,
        t.color AS childColor,

        r.contextType AS contextType,

        r.scheduleId AS scheduleId,
        r.parentRuleScheduleId AS parentRuleScheduleId,
        r.occurrenceDateEpochDay AS occurrenceDateEpochDay,
        r.listSessionId AS listSessionId,

        r.slotIndex AS slotIndex,
        r.learningIndex AS learningIndex,
        r.learningTargetCount AS learningTargetCount,

        r.status AS status,

        r.dueAtEpochMillis AS dueAtEpochMillis,
        r.expiresAtEpochMillis AS expiresAtEpochMillis,
        r.completedAtEpochMillis AS completedAtEpochMillis,

        r.sourceCatalogItemId AS sourceCatalogItemId
    FROM task_child_requirement r
    INNER JOIN task t ON t.id = r.childTaskId
    WHERE r.status != 'CANCELLED'
      AND (
            r.occurrenceDateEpochDay BETWEEN :startEpochDay AND :endEpochDay
            OR r.contextType IN ('PARENT_LIFETIME', 'LEARNING_CYCLE', 'MANUAL')
      )
    ORDER BY 
        r.parentTaskId ASC,
        t.siblingIndex ASC,
        t.id ASC,
        r.contextType ASC,
        r.slotIndex ASC,
        r.learningIndex ASC,
        r.id ASC
""")
    fun observeRequirementUiByDateRange(
        startEpochDay: Long,
        endEpochDay: Long
    ): Flow<List<TaskChildRequirementUi>>

    @Query("""
    UPDATE task_child_requirement
    SET status = 'CANCELLED',
        updatedAtEpochMillis = :updatedAt
    WHERE ruleId = :ruleId
      AND status IN ('WAITING', 'INCOMPLETE')
""")
    suspend fun cancelOpenRequirementsByRuleId(
        ruleId: Int,
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query("""
    SELECT 
        r.id AS requirementId,
        r.ruleId AS ruleId,
        r.parentTaskId AS parentTaskId,
        r.childTaskId AS childTaskId,

        t.name AS childTitle,
        t.color AS childColor,

        r.contextType AS contextType,

        r.scheduleId AS scheduleId,
        r.parentRuleScheduleId AS parentRuleScheduleId,
        r.occurrenceDateEpochDay AS occurrenceDateEpochDay,
        r.listSessionId AS listSessionId,

        r.slotIndex AS slotIndex,
        r.learningIndex AS learningIndex,
        r.learningTargetCount AS learningTargetCount,

        r.status AS status,

        r.dueAtEpochMillis AS dueAtEpochMillis,
        r.expiresAtEpochMillis AS expiresAtEpochMillis,
        r.completedAtEpochMillis AS completedAtEpochMillis,

        r.sourceCatalogItemId AS sourceCatalogItemId
    FROM task_child_requirement r
    INNER JOIN task t ON t.id = r.childTaskId
    WHERE r.parentTaskId = :parentTaskId
      AND r.status != 'CANCELLED'
      AND (
            (
                r.contextType = 'SCHEDULE_OCCURRENCE'
                AND r.scheduleId = :scheduleId
                AND r.occurrenceDateEpochDay = :occurrenceDateEpochDay
            )
            OR
            (
                r.contextType = 'RULE_OCCURRENCE'
                AND r.parentRuleScheduleId = :parentRuleScheduleId
                AND r.occurrenceDateEpochDay = :occurrenceDateEpochDay
            )
            OR
            (
                r.contextType = 'DAY'
                AND r.occurrenceDateEpochDay = :occurrenceDateEpochDay
            )
            OR
            (
                r.contextType = 'PARENT_LIFETIME'
            )
            OR
            (
                r.contextType = 'LEARNING_CYCLE'
            )
            OR
            (
                r.contextType = 'MANUAL'
            )
      )
    ORDER BY 
        t.siblingIndex ASC,
        t.id ASC,
        r.contextType ASC,
        r.slotIndex ASC,
        r.learningIndex ASC,
        r.id ASC
""")
    fun observeRequirementUiForParentOccurrence(
        parentTaskId: Int,
        scheduleId: Int?,
        parentRuleScheduleId: Int?,
        occurrenceDateEpochDay: Long
    ): Flow<List<TaskChildRequirementUi>>


    @Query("""
    SELECT * FROM task_child_rule
    WHERE id = :ruleId
    LIMIT 1
""")
    suspend fun getRuleById(
        ruleId: Int
    ): TaskChildRuleEntity?

    fun buildLearningRequirementUniqueKey(
        ruleId: Int,
        learningIndex: Int
    ): String {
        return buildString {
            append("rule:")
            append(ruleId)
            append("|ctx:")
            append(TaskChildRequirementContextType.LEARNING_CYCLE)
            append("|schedule:null")
            append("|parentRule:null")
            append("|date:null")
            append("|slot:0")
            append("|learn:")
            append(learningIndex)
        }
    }

    fun g5DelayDaysForIndex(
        rule: TaskChildRuleEntity,
        learningIndex: Int
    ): Int {
        val custom = rule.g5IntervalDaysCsv
            ?.split(",")
            ?.mapNotNull { it.trim().toIntOrNull() }
            ?.filter { it >= 0 }
            .orEmpty()

        val default = listOf(0, 1, 2, 4, 7, 14, 30)

        val source = if (custom.isNotEmpty()) custom else default

        return source.getOrNull(learningIndex)
            ?: source.lastOrNull()
            ?: 0
    }

    @Transaction
    suspend fun createNextLearningRequirementIfNeeded(
        completedRequirement: TaskChildRequirementEntity,
        completedAt: Long
    ) {
        if (completedRequirement.contextType != TaskChildRequirementContextType.LEARNING_CYCLE) {
            return
        }

        val rule = getRuleById(completedRequirement.ruleId) ?: return

        if (rule.ruleType != TaskChildRuleType.G5_LEARNING) {
            return
        }

        val targetCount =
            completedRequirement.learningTargetCount
                ?: rule.g5TargetCount.coerceAtLeast(1)

        val currentIndex = completedRequirement.learningIndex
        val nextIndex = currentIndex + 1

        if (nextIndex >= targetCount) {
            return
        }

        val nextUniqueKey = buildLearningRequirementUniqueKey(
            ruleId = rule.id,
            learningIndex = nextIndex
        )

        val existingNext = getRequirementByUniqueKey(nextUniqueKey)
        if (existingNext != null) {
            return
        }

        val delayDays = g5DelayDaysForIndex(
            rule = rule,
            learningIndex = nextIndex
        )

        val dueAt = completedAt + delayDays * 24L * 60L * 60L * 1000L

        val nextStatus =
            if (dueAt <= completedAt) {
                TaskChildRequirementStatus.INCOMPLETE
            } else {
                TaskChildRequirementStatus.WAITING
            }

        upsertRequirement(
            TaskChildRequirementEntity(
                uniqueKey = nextUniqueKey,
                ruleId = rule.id,
                parentTaskId = completedRequirement.parentTaskId,
                childTaskId = completedRequirement.childTaskId,
                contextType = TaskChildRequirementContextType.LEARNING_CYCLE,
                scheduleId = null,
                parentRuleScheduleId = null,
                occurrenceDateEpochDay = null,
                listSessionId = null,
                slotIndex = 0,
                learningIndex = nextIndex,
                learningTargetCount = targetCount,
                status = nextStatus,
                dueAtEpochMillis = dueAt,
                sourceCatalogItemId = rule.sourceCatalogItemId
            )
        )
    }
}