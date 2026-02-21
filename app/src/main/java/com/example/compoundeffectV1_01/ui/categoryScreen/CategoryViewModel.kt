package com.example.compoundeffectV1_01.ui.categoryScreen


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.compoundeffectV1_01.data.dataBaseRoom.tables.category.CategoryEntity
import com.example.compoundeffectV1_01.data.dataBaseRoom.tables.category.CategoryRepository
import com.example.compoundeffectV1_01.data.dataBaseRoom.tables.task.Task
import com.example.compoundeffectV1_01.data.dataBaseRoom.tables.task.TaskRepository
import com.example.compoundeffectV1_01.data.dataBaseRoom.tables.task.TaskWithSchedule
import com.example.compoundeffectV1_01.data.dataBaseRoom.tables.taskSchedule.RepeatUnit
import com.example.compoundeffectV1_01.data.dataBaseRoom.tables.taskSchedule.ScheduleMode
import com.example.compoundeffectV1_01.data.dataBaseRoom.tables.taskSchedule.TaskSchedule
import com.example.compoundeffectV1_01.data.dataBaseRoom.tables.taskSchedule.TaskScheduleRepository
import com.example.compoundeffectV1_01.utils.ceilToNextQuarter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject



@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository,
    private val taskRepo: TaskRepository,
    private val scheduleRepo: TaskScheduleRepository,
) : ViewModel() {

    private var nextTempScheduleId = -1
    private fun newTempId(): Int = nextTempScheduleId--

    // این دو تا برای سناریوی “درگ شروع شد/تمام شد”
    private val _dragCollapsedRestoreCategory = MutableStateFlow<Int?>(null)
    private val _dragCollapsedRestoreTask = MutableStateFlow<Int?>(null)
    private val _collapsedIdsCategory = MutableStateFlow<Set<Int>>(emptySet())
    private val _taskCollapsedIds = MutableStateFlow<Set<Int>>(emptySet())

    private val _menuCategoryId = MutableStateFlow<Int?>(null)
    val menuCategoryId = _menuCategoryId.asStateFlow()

    // ✅ لیست تسک‌ها + schedule
    @OptIn(ExperimentalCoroutinesApi::class)
    val tasksWithScheduleForMenu: StateFlow<List<TaskWithSchedule>> =
        menuCategoryId
            .flatMapLatest { id ->
                if (id == null) flowOf(emptyList())
                else taskRepo.observeTasksWithScheduleByCategory(id)
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val taskMiniUiForMenu: StateFlow<List<TaskMiniUi>> =
        tasksWithScheduleForMenu
            .map { list ->
                list.mapNotNull { tws ->
                    val t = tws.task
                    val id = t.id ?: return@mapNotNull null

                    TaskMiniUi(
                        id = id,
                        title = t.name,
                        isDone = t.isCompleted,
                        hasSchedule = (tws.schedule != null),
                        parentTaskId = t.parentTaskId,
                        siblingIndex = t.siblingIndex,
                        priority = t.priority
                    )

                }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val tasksForMenuCategory: StateFlow<List<Task>> =
        _menuCategoryId
            .flatMapLatest { id ->
                if (id == null) flowOf(emptyList())
                else taskRepo.observeTasksByCategory(id)
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )


    val uiState: StateFlow<CategoryUiState2> =
        combine(
            categoryRepository.observeAll(),
            _collapsedIdsCategory,
            tasksForMenuCategory,     // ✅ لیست خام Task ها
            taskMiniUiForMenu,        // ✅ برای ساخت renderItems تسک‌ها
            _taskCollapsedIds
        ) { categories, catCollapsed, tasks, tasksMini, taskCollapsed ->

            val catFlatten = flattenCategoryTreeWithLevelsAndVisibility(categories, catCollapsed)

            val taskRender = flattenTaskTreeWithLevelsAndVisibility(
                all = tasksMini,
                collapsedIds = taskCollapsed,
                rootParentId = ROOT,
                maxDepth = 4
            )


            CategoryUiState2(
                isLoading = false,
                categories = categories,
                renderItems = catFlatten.items,

                tasks = tasks,                 // ✅ اینجا کامل شد
                taskRenderItems = taskRender,  // ✅ اینم مثل قبل

                levelById = catFlatten.levelById
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = CategoryUiState2(
                isLoading = true,
                categories = emptyList(),
                tasks = emptyList()
            )
        )



    private val _draft = MutableStateFlow(CategoryDraft2())
    val draft = _draft.asStateFlow()


    private val _createResult = MutableStateFlow<CreateResult?>(null)
    val createResult = _createResult.asStateFlow()

    val parentPickerItems: StateFlow<FlattenResult> =
        categoryRepository.observeAll()
            .map { categories ->
                flattenForPickerAllVisible(categories)
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = FlattenResult(emptyList(), emptyMap())
            )

    private val _taskDraft = MutableStateFlow(TaskDraft())
    val taskDraft = _taskDraft.asStateFlow()





    private val _editingTaskId = MutableStateFlow<Int?>(null)
    val editingTaskId = _editingTaskId.asStateFlow()

    private val _scheduleDraft = MutableStateFlow(ScheduleDraft())
    val scheduleDraft = _scheduleDraft.asStateFlow()

    private val _scheduleConfirmedForNewTask = MutableStateFlow(false)



    // ✅ شمارش scheduled (دو روش)
    @OptIn(ExperimentalCoroutinesApi::class)
    val scheduledCountForMenu: StateFlow<Int> =
        menuCategoryId
            .flatMapLatest { id ->
                if (id == null) flowOf(0)
                else taskRepo.observeScheduledCountByCategory(id)
                // یا اگر اون Query رو نذاشتی:
                // else taskRepository.observeTasksWithScheduleByCategory(id).map { list -> list.count { it.schedule != null } }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val childLevelUi: StateFlow<ChildLevelUi> =
        combine(
            tasksForMenuCategory,
            _taskDraft,
            _editingTaskId
        ) { tasks, draft, editingId ->

            val selectedCatId = draft.categoryId
            val sameCategory = selectedCatId != null && selectedCatId == _menuCategoryId.value
            if (!sameCategory) return@combine ChildLevelUi(setOf(0), 0)
            if (draft.insertAtTop) return@combine ChildLevelUi(setOf(0), 0)

            val parentById = buildParentById(tasks)
            val miniAll = buildTaskMiniAll(tasks)
            val flat = flattenAllTasks(miniAll)

            fun depthOfId(id: Int): Int = depthOf(id, parentById)

            val insertionIndex =
                if (editingId == null) flat.size
                else flat.indexOfFirst { it.id == editingId }.let { if (it == -1) flat.size else it }

            val prevId = flat.getOrNull(insertionIndex - 1)?.id
            val prevDepth = if (prevId == null) -1 else depthOfId(prevId)

            val cap = (prevDepth + 1).coerceIn(0, 3)

            val allowed = (0..cap).toMutableSet()

            // در edit سطح فعلی همیشه مجاز
            if (editingId != null) allowed += depthOfId(editingId)

            ChildLevelUi(
                allowed = allowed,
                maxAllowed = allowed.maxOrNull() ?: 0
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ChildLevelUi())



    private val _editingScheduleKey = MutableStateFlow<Int?>(null)
    val editingScheduleKey = _editingScheduleKey.asStateFlow()


    private var pendingKeyCounter = -1

    private val _pendingSchedulesForNewTask = MutableStateFlow<List<TaskScheduleUi>>(emptyList())
    private val pendingSchedulesForNewTask = _pendingSchedulesForNewTask.asStateFlow()


    @OptIn(ExperimentalCoroutinesApi::class)
    val schedulesUiForTaskDialog: StateFlow<List<TaskScheduleUi>> =
        editingTaskId.flatMapLatest { tid ->
            if (tid == null) {
                pendingSchedulesForNewTask
            } else {
                scheduleRepo.observeByTaskId(tid).map { list ->
                    list.map { sch ->
                        TaskScheduleUi(
                            key = sch.id!!,       // چون DB هست id دارد
                            schedule = sch,
                            isPending = false
                        )
                    }
                }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())







    //کتگوری ها
    fun setDraftName(value: String) {
        _draft.update { it.copy(name = value) }
    }
    fun setDraftIconName(value: String) {
        _draft.update { it.copy(iconName = value) }
    }
    fun setDraftColor(value: String) {
        _draft.update { it.copy(color = value) }
    }
    fun createCategoryFromDraft() {
        val d = _draft.value
        if (d.name.isBlank()) {
            _createResult.value = CreateResult.Error("نام گروه را وارد کن")
            return
        }

        viewModelScope.launch {
            val current = uiState.value.categories
            val nextSiblingIndex = current.count { it.parentCategoryId == d.parentId }

            categoryRepository.insertCategory(
                CategoryEntity(
                    name = d.name.trim(),
                    parentCategoryId = d.parentId,
                    iconName = d.iconName,
                    color = d.color,
                    description = d.description,
                    siblingIndex = nextSiblingIndex
                )
            )

            resetDraft()
            _createResult.value = CreateResult.Success
        }
    }
    fun resetDraft() {
        _draft.value = CategoryDraft2()
        _createResult.value = null
    }
    fun renameCategory(categoryId: Int, newName: String) {
        val name = newName.trim()
        if (name.isBlank()) return

        viewModelScope.launch {
            val current = uiState.value.categories.firstOrNull { it.categoryId == categoryId } ?: return@launch
            categoryRepository.updateCategory(current.copy(name = name))
        }
    }
    fun updateCategoryIcon(categoryId: Int, iconName: String) {
        viewModelScope.launch {
            val current = uiState.value.categories.firstOrNull { it.categoryId == categoryId } ?: return@launch
            categoryRepository.updateCategory(current.copy(iconName = iconName))
        }
    }
    fun updateCategoryColor(categoryId: Int, colorHex: String) {
        viewModelScope.launch {
            val current = uiState.value.categories.firstOrNull { it.categoryId == categoryId } ?: return@launch
            categoryRepository.updateCategory(current.copy(color = colorHex))
        }
    }
    fun applyDragResult(draggedId: Int, oldParentId: Int?, newParentId: Int?, currentList: List<CategoryRenderItem>) {
        viewModelScope.launch {
            val all = uiState.value.categories
            val dragged = all.firstOrNull { it.categoryId == draggedId } ?: return@launch

            val finalNewParent = newParentId ?: dragged.parentCategoryId ?: ROOT

            // ✅ 1) self-parent ممنوع
            if (finalNewParent == draggedId) return@launch

            // ✅ 2) cycle ممنوع
            // ✅ 2) cycle ممنوع (Category) — Root = -1
            val parentById: Map<Int, Int> =
                all.mapNotNull { c ->
                    val id = c.categoryId ?: return@mapNotNull null
                    id to (c.parentCategoryId ?: -1)
                }.toMap()

            if (wouldCreateCycleCategory(draggedId, finalNewParent, parentById)) {
                return@launch
            }


            // parent جدید باید سطحش < 4 باشد
            val parentLevel =
                if (finalNewParent == -1) 1
                else (uiState.value.levelById[finalNewParent] ?: 1)

            if (parentLevel >= 4) return@launch

            // آپدیت parent (اگر تغییر کرده)
            if (finalNewParent != dragged.parentCategoryId) {
                categoryRepository.updateCategory(dragged.copy(parentCategoryId = finalNewParent))
            }

            suspend fun reorderFor(parentId: Int?) {
                val orderedIds = currentList
                    .asSequence()
                    .filter { it.isVisible }
                    .filter { item ->
                        val id = item.category.categoryId ?: return@filter false
                        val p = if (id == draggedId) finalNewParent else item.category.parentCategoryId
                        p == parentId
                    }
                    .mapNotNull { it.category.categoryId }
                    .toList()

                val currentEntities = uiState.value.categories.filter { it.parentCategoryId == parentId }
                val byId = currentEntities.associateBy { it.categoryId }

                orderedIds.forEachIndexed { index, id ->
                    val entity =
                        if (id == draggedId) {
                            // dragged ممکنه هنوز تو uiState با parent قبلی باشه
                            dragged.copy(parentCategoryId = finalNewParent)
                        } else {
                            byId[id] ?: return@forEachIndexed
                        }

                    categoryRepository.updateCategory(entity.copy(siblingIndex = index))
                }
            }



            reorderFor(oldParentId)
            reorderFor(finalNewParent)
        }
    }
    fun toggleTaskCompletedCascade(taskId: Int) {
        viewModelScope.launch {
            val task = withContext(Dispatchers.IO) { taskRepo.getTaskById(taskId) } ?: return@launch
            val newDone = !task.isCompleted

            if (!newDone) {
                // ✅ برعکسش زنجیره‌ای نیست
                withContext(Dispatchers.IO) {
                    taskRepo.updateTask(task.copy(isCompleted = false))
                }
                return@launch
            }

            // ✅ done شدن: خودت + همه‌ی descendants
            val categoryId = task.categoryId ?: return@launch
            val allInCategory = withContext(Dispatchers.IO) { taskRepo.getTasksByCategoryOrdered(categoryId) }

            val childrenByParent: Map<Int?, List<Int>> =
                allInCategory
                    .filter { it.id != null }
                    .groupBy { it.parentTaskId }
                    .mapValues { it.value.mapNotNull { t -> t.id } }

            val idsToMarkDone = buildList {
                add(taskId)

                val stack = ArrayDeque<Int>()
                stack.add(taskId)

                while (stack.isNotEmpty()) {
                    val cur = stack.removeLast()
                    val children = childrenByParent[cur].orEmpty()
                    for (childId in children) {
                        add(childId)
                        stack.add(childId)
                    }
                }
            }.distinct()

            withContext(Dispatchers.IO) {
                taskRepo.setCompletedForIds(idsToMarkDone, true)
            }
        }
    }
    fun setTaskCategoryId(categoryId: Int) {
        _taskDraft.update { it.copy(categoryId = categoryId) }
    }
    private fun wouldCreateCycleCategory(
        draggedId: Int,
        newParentId: Int,
        parentById: Map<Int, Int> // id -> parentId (Root = -1)
    ): Boolean {
        var cur = newParentId
        var guard = 0
        while (cur != -1 && guard < 100) {
            if (cur == draggedId) return true
            cur = parentById[cur] ?: -1
            guard++
        }
        return false
    }




    //تسک ها
    fun startAddTask(categoryId: Int) {
        _editingTaskId.value = null   // ✅ اضافه کن
        _taskDraft.value = TaskDraft(categoryId = categoryId)
        _scheduleDraft.value = ScheduleDraft()
        _scheduleConfirmedForNewTask.value = false
        _pendingSchedulesForNewTask.value = emptyList()
    }

    fun startEditTask(taskId: Int) {
        viewModelScope.launch {
            val t = taskRepo.getTaskById(taskId) ?: return@launch

            val categoryId = t.categoryId ?: return@launch
            val all = withContext(Dispatchers.IO) { taskRepo.getTasksByCategory(categoryId) }
            val parentById = buildParentById(all)
            val depth = t.id?.let { depthOf(it, parentById) } ?: 0

            _editingTaskId.value = taskId
            _taskDraft.value = TaskDraft(
                name = t.name,
                categoryId = t.categoryId,
                priority = t.priority,
                isCompleted = t.isCompleted,
                note = t.description,
                insertAtTop = false,
                childLevel = depth
            )

            val sch = scheduleRepo.getByTaskId(taskId)
            _scheduleDraft.value = sch?.toDraft() ?: ScheduleDraft()


        }
    }
    fun setTaskName(v: String) = _taskDraft.update { it.copy(name = v) }
    fun setTaskPriority(p: Int) = _taskDraft.update { it.copy(priority = p) }
    fun setTaskCompleted(v: Boolean) = _taskDraft.update { it.copy(isCompleted = v) }
    fun setTaskNote(v: String) = _taskDraft.update { it.copy(note = v) }
    fun setTaskInsertAtTop(v: Boolean) = _taskDraft.update { it.copy(insertAtTop = v) }
    fun setTaskChildLevel(v: Int) = _taskDraft.update { it.copy(childLevel = v.coerceIn(0, 3)) }
    fun resetTaskDraftKeepSomeDefaults() {
        val cur = _taskDraft.value
        _taskDraft.value = TaskDraft(
            name = "",
            categoryId = cur.categoryId,
            priority = cur.priority,
            isCompleted = false,
            note = "",
            insertAtTop = cur.insertAtTop,
            childLevel = cur.childLevel
        )
        _scheduleDraft.value = ScheduleDraft()
        _scheduleConfirmedForNewTask.value = false
    }
    fun createTaskForCategory(categoryColor: String) {
        val d = _taskDraft.value
        if (d.name.isBlank()) return
        val categoryId = d.categoryId ?: return

        viewModelScope.launch {
            val all = withContext(Dispatchers.IO) { taskRepo.getTasksByCategory(categoryId) }
            val parentById = buildParentById(all)
            val miniAll = buildTaskMiniAll(all)
            val flat = flattenAllTasks(miniAll)

            val insertionIndex = if (d.insertAtTop) 0 else flat.size

            val desiredDepth = d.childLevel.coerceIn(0, 3)

            val parentId = pickParentIdForDesiredDepth(
                flat = flat,
                parentById = parentById,
                desiredDepth = desiredDepth,
                insertionIndex = insertionIndex
            ) ?: ROOT





            // ✅ siblingIndex مثل category
            val siblings = all
                .filter { it.parentTaskId == parentId }

            val newSiblingIndex =
                if (d.insertAtTop) {
                    // شیفت دادن بقیه
                    withContext(Dispatchers.IO) {
                        siblings.forEach { s ->
                            val sid = s.id ?: return@forEach
                            taskRepo.updateSiblingIndex(sid, s.siblingIndex + 1)
                        }
                    }
                    0
                } else {
                    siblings.size
                }

            val newTask = Task(
                id = null,
                name = d.name.trim(),
                color = categoryColor,
                description = d.note,
                durationOverlap = 0,
                selected = false,
                changed = false,
                categoryId = categoryId,
                isCompleted = d.isCompleted,
                priority = d.priority,
                parentTaskId = parentId,
                siblingIndex = newSiblingIndex
            )

            val newId = withContext(Dispatchers.IO) {
                taskRepo.insertTaskAndReturnId(newTask) // Long
            }
            val pending = _pendingSchedulesForNewTask.value
            if (pending.isNotEmpty()) {
                withContext(Dispatchers.IO) {
                    pending.forEach { ui ->
                        scheduleRepo.insert(ui.schedule.copy(taskId = newId))
                    }
                }
                _pendingSchedulesForNewTask.value = emptyList()
            }



            if (_scheduleConfirmedForNewTask.value) {
                val sd = _scheduleDraft.value
                fun LocalTime.toMinuteOfDay(): Int = hour * 60 + minute
                val safeInterval = sd.repeat.interval.coerceIn(1, 99)

                val schedule = TaskSchedule(
                    id = null,
                    taskId = newId,
                    title = sd.title.trim().ifBlank { null },
                    mode = sd.mode,
                    dateEpochDay = if (sd.mode == ScheduleMode.TIME_RANGE) sd.date.toEpochDay() else null,
                    startMinuteOfDay = if (sd.mode == ScheduleMode.TIME_RANGE) sd.start.toMinuteOfDay() else null,
                    endMinuteOfDay = if (sd.mode == ScheduleMode.TIME_RANGE) sd.end.toMinuteOfDay() else null,
                    durationMinutes = if (sd.mode == ScheduleMode.AMOUNT_OF_TIME) sd.durationMinutes else null,
                    inPallet = false,
                    repeating = sd.repeat.enabled,
                    repeatInterval = if (sd.repeat.enabled) safeInterval else null,
                    repeatUnit = if (sd.repeat.enabled) sd.repeat.unit else null,
                    weekdaysMask = if (sd.repeat.enabled && sd.repeat.unit == RepeatUnit.WEEK)
                        sd.repeat.weekdaysMask.coerceIn(0, 127)
                    else null

                )

                withContext(Dispatchers.IO) { scheduleRepo.upsert(schedule) }
            }
        }
    }
    fun deleteTask(taskId: Int) {
        viewModelScope.launch {
            val t = taskRepo.getTaskById(taskId) ?: return@launch
            withContext(Dispatchers.IO) {
                taskRepo.deleteTask(t)
            }
        }
    }
    fun saveEditedTask(categoryColor: String) {
        val taskId = _editingTaskId.value ?: return
        val d = _taskDraft.value
        if (d.name.isBlank()) return

        viewModelScope.launch {
            val current = taskRepo.getTaskById(taskId) ?: return@launch

            val newCategoryId = d.categoryId ?: current.categoryId ?: return@launch
            val categoryChanged = (newCategoryId != current.categoryId)

            val childrenCount = withContext(Dispatchers.IO) { taskRepo.countChildren(taskId) }

            val (finalParentId, finalSiblingIndex) =
                if (categoryChanged) {
                    val allNew = withContext(Dispatchers.IO) { taskRepo.getTasksByCategory(newCategoryId) }
                    val rootSiblings = allNew.filter { it.parentTaskId == ROOT }
                    ROOT to rootSiblings.size

                } else {
                    val categoryId = current.categoryId ?: return@launch
                    val all = withContext(Dispatchers.IO) { taskRepo.getTasksByCategory(categoryId) }
                    val parentById = buildParentById(all)
                    val curDepth = current.id?.let { depthOf(it, parentById) } ?: 0

                    // ✅ اگر بچه دارد، اجازه‌ی کم‌عمق‌تر شدن نده (مثل قبل)
                    if (childrenCount > 0 && d.childLevel < curDepth) return@launch

                    val miniAll = buildTaskMiniAll(all)
                    val flat = flattenAllTasks(miniAll)

                    // محل درج برای edit: قبل از خود آیتم
                    val insertionIndex = flat.indexOfFirst { it.id == taskId }.let { if (it == -1) flat.size else it }

                    val desiredDepth = d.childLevel.coerceIn(0, 3)



                    val newParent = pickParentIdForDesiredDepth(
                        flat = flat,
                        parentById = parentById,
                        desiredDepth = desiredDepth,
                        insertionIndex = insertionIndex
                    )


                    if (newParent == current.parentTaskId) {
                        // parent ثابت => siblingIndex رو دست نزن
                        newParent to current.siblingIndex
                    } else {
                        val newSiblings = all.filter { it.parentTaskId == newParent }
                        newParent to newSiblings.size
                    }
                }
            val safeFinalParentId = finalParentId ?: ROOT
            val updated = current.copy(
                name = d.name.trim(),
                description = d.note,
                isCompleted = d.isCompleted,
                priority = d.priority,
                color = categoryColor,

                categoryId = newCategoryId,
                parentTaskId = safeFinalParentId,
                siblingIndex = finalSiblingIndex
            )

            withContext(Dispatchers.IO) { taskRepo.updateTask(updated) }

            finishEditTask()
        }
    }
    private fun flattenTaskTreeWithLevelsAndVisibility(
        all: List<TaskMiniUi>,
        collapsedIds: Set<Int>,
        rootParentId: Int = ROOT,
        maxDepth: Int = 4
    ): List<TaskRenderItem> {

        val byParent = all.groupBy { it.parentTaskId }
        val items = mutableListOf<TaskRenderItem>()

        fun dfs(parentId: Int, realDepth: Int, ancestorCollapsed: Boolean) {
            if (realDepth > 50) return

            val renderLevel = realDepth.coerceAtMost(maxDepth)
            val children = byParent[parentId].orEmpty()
                .sortedWith(compareBy<TaskMiniUi> { it.siblingIndex }.thenBy { it.id })

            for (child in children) {
                val id = child.id
                val hasChildrenRaw = byParent[id].orEmpty().isNotEmpty()
                val hasChildren = (renderLevel < maxDepth) && hasChildrenRaw

                val selfCollapsed = collapsedIds.contains(id)
                val visible = !ancestorCollapsed

                items += TaskRenderItem(
                    task = child,
                    level = renderLevel,
                    hasChildren = hasChildren,
                    isExpanded = !selfCollapsed,
                    isVisible = visible
                )

                dfs(id, realDepth + 1, ancestorCollapsed || selfCollapsed)
            }
        }

        dfs(rootParentId, realDepth = 1, ancestorCollapsed = false)
        return items
    }
    fun applyTaskDragResult(
        draggedId: Int,
        oldParentId: Int?,
        newParentId: Int?,
        categoryId: Int,
        currentList: List<TaskRenderItem>
    ) {
        viewModelScope.launch {

            val all = withContext(Dispatchers.IO) { taskRepo.getTasksByCategory(categoryId) }
            val dragged = all.firstOrNull { it.id == draggedId } ?: return@launch

            val finalNewParent: Int = when (newParentId) {
                null -> dragged.parentTaskId ?: ROOT
                else -> newParentId
            }


            if (finalNewParent == draggedId) return@launch

            // cycle check
            val parentById = all.filter { it.id != null }.associate { it.id!! to (it.parentTaskId ?: ROOT) }
            if (wouldCreateTaskCycle(draggedId, finalNewParent, parentById)) return@launch


            // 1) parent update (اگر تغییر کرده)
            if (finalNewParent != dragged.parentTaskId) {
                withContext(Dispatchers.IO) {
                    taskRepo.updateTask(dragged.copy(parentTaskId = finalNewParent))
                }
            }

            suspend fun reorderFor(parentId: Int) {
                val orderedIds = currentList
                    .asSequence()
                    .filter { it.isVisible }
                    .filter { item ->
                        val id = item.task.id
                        val p = if (id == draggedId) finalNewParent else (item.task.parentTaskId ?: ROOT)
                        p == parentId

                    }
                    .map { it.task.id }
                    .toList()

                if (orderedIds.isEmpty()) return

                val freshAll = withContext(Dispatchers.IO) { taskRepo.getTasksByCategory(categoryId) }
                val byId = freshAll.associateBy { it.id }

                withContext(Dispatchers.IO) {
                    orderedIds.forEachIndexed { index, id ->
                        val e = byId[id] ?: return@forEachIndexed
                        if (e.siblingIndex != index) {
                            taskRepo.updateTask(e.copy(siblingIndex = index))
                        }
                    }
                }
            }

            reorderFor(oldParentId ?: ROOT)
            if (finalNewParent != (oldParentId ?: ROOT)) reorderFor(finalNewParent)

        }
    }
    fun onDragEndRestoreExpandForTask() {
        val id = _dragCollapsedRestoreTask.value ?: return
        _dragCollapsedRestoreTask.value = null

        _taskCollapsedIds.update { it - id }

        // Persist اختیاری:
        viewModelScope.launch {
            val current = uiState.value.tasks.firstOrNull { it.id == id } ?: return@launch
            taskRepo.updateTask(current.copy(isExtended = true))
        }
    }
    fun onDragStartMaybeCollapseForTask(taskId: Int) {
        val item = uiState.value.taskRenderItems.firstOrNull { it.task.id == taskId } ?: return
        if (!item.hasChildren) return

        val isExpandedNow = item.isExpanded
        if (isExpandedNow) {
            _dragCollapsedRestoreTask.value = taskId
            _taskCollapsedIds.update { it + taskId }
            // Persist اختیاری:
            viewModelScope.launch {
                val current = uiState.value.tasks.firstOrNull { it.id == taskId } ?: return@launch
                taskRepo.updateTask(current.copy(isExtended = false))
            }
        }
    }
    fun toggleExpandForTask(taskId: Int) {
        viewModelScope.launch {
            val current = uiState.value.tasks.firstOrNull { it.id == taskId } ?: return@launch
            val willCollapse = !_taskCollapsedIds.value.contains(taskId)

            _taskCollapsedIds.update { set ->
                if (willCollapse) set + taskId else set - taskId
            }

            // Persist
            taskRepo.updateTask(current.copy(isExtended = !willCollapse))
        }
    }
    fun clampTaskChildLevel(allowed: Set<Int>) {
        val cur = _taskDraft.value.childLevel
        if (cur !in allowed) {
            _taskDraft.update { it.copy(childLevel = allowed.maxOrNull() ?: 0) }
        }
    }
    private fun wouldCreateTaskCycle(
        draggedId: Int,
        newParentId: Int,
        parentById: Map<Int, Int>
    ): Boolean {
        var cur = newParentId
        var guard = 0
        while (cur != ROOT && guard < 200) {
            if (cur == draggedId) return true
            cur = parentById[cur] ?: ROOT
            guard++
        }
        return false
    }
    fun completeAllTasks(categoryId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            taskRepo.completeAllInCategory(categoryId)
        }
    }
    fun uncompletedAllTasks(categoryId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            taskRepo.uncompleteAllInCategory(categoryId)
        }
    }
    fun deleteCompletedTasks(categoryId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            taskRepo.deleteCompletedInCategory(categoryId)
        }
    }
    fun deleteAllTasks(categoryId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            taskRepo.deleteAllInCategory(categoryId)
        }
    }







    //اسچدول ها
    fun setScheduleTitle(v: String) = _scheduleDraft.update { it.copy(title = v) }
    fun setScheduleMode(m: ScheduleMode) = _scheduleDraft.update { it.copy(mode = m) }
    fun setScheduleStart(t: LocalTime) = _scheduleDraft.update { it.copy(start = t) }
    fun setScheduleEnd(t: LocalTime) = _scheduleDraft.update { it.copy(end = t) }
    fun setScheduleDuration(min: Int) = _scheduleDraft.update { it.copy(durationMinutes = min.coerceAtLeast(1)) }
    fun confirmScheduleFromDialog() {
        val sd = _scheduleDraft.value
        fun LocalTime.toMinuteOfDay() = hour * 60 + minute

        val tid = _editingTaskId.value
        val key = _editingScheduleKey.value

        val safeInterval = sd.repeat.interval.coerceIn(1, 99)
        val safeMask = sd.repeat.weekdaysMask.coerceIn(0, 127)

        val newSchedule = TaskSchedule(
            id = if (tid != null) key else null,
            taskId = tid ?: 0,
            title = sd.title.trim().ifBlank { null },
            mode = sd.mode,
            dateEpochDay = if (sd.mode == ScheduleMode.TIME_RANGE) sd.date.toEpochDay() else null,
            startMinuteOfDay = if (sd.mode == ScheduleMode.TIME_RANGE) sd.start.toMinuteOfDay() else null,
            endMinuteOfDay = if (sd.mode == ScheduleMode.TIME_RANGE) sd.end.toMinuteOfDay() else null,
            durationMinutes = if (sd.mode == ScheduleMode.AMOUNT_OF_TIME) sd.durationMinutes else null,

            inPallet = false,

            repeating = sd.repeat.enabled,
            repeatInterval = if (sd.repeat.enabled) safeInterval else null,
            repeatUnit = if (sd.repeat.enabled) sd.repeat.unit else null,

            // ✅ NEW
            weekdaysMask = if (sd.repeat.enabled && sd.repeat.unit == RepeatUnit.WEEK) safeMask else null
        )

        if (tid == null) {
            // ===== pending mode =====
            if (key == null) {
                // add pending
                val newKey = pendingKeyCounter--
                _pendingSchedulesForNewTask.update {
                    it + TaskScheduleUi(newKey, newSchedule, isPending = true)
                }
            } else {
                // edit pending
                _pendingSchedulesForNewTask.update { list ->
                    list.map { ui ->
                        if (ui.key == key) ui.copy(schedule = newSchedule) else ui
                    }
                }
            }
            finishEditSchedule()
        } else {
            // ===== DB mode =====
            viewModelScope.launch(Dispatchers.IO) {
                scheduleRepo.upsert(newSchedule.copy(taskId = tid))
            }
            finishEditSchedule()
        }
    }
    private fun TaskSchedule.toDraft(): ScheduleDraft {
        fun minuteToLocalTime(min: Int) = LocalTime.of(min / 60, min % 60)

        val enabled = repeating
        val unit = repeatUnit ?: RepeatUnit.DAY

        return ScheduleDraft(
            title = title.orEmpty(),
            mode = mode,
            date = dateEpochDay?.let(LocalDate::ofEpochDay) ?: LocalDate.now(),
            start = startMinuteOfDay?.let(::minuteToLocalTime) ?: LocalTime.of(20, 0),
            end = endMinuteOfDay?.let(::minuteToLocalTime) ?: LocalTime.of(21, 0),
            durationMinutes = durationMinutes ?: 60,

            repeat = RepeatDraft(
                enabled = enabled,
                interval = (repeatInterval ?: 1).coerceIn(1, 99),
                unit = unit,
                weekdaysMask = (weekdaysMask ?: 0).coerceIn(0, 127)
            )
        )
    }


    fun startEditScheduleByKey(key: Int) {
        viewModelScope.launch {
            _editingScheduleKey.value = key

            val tid = _editingTaskId.value
            val schedule: TaskSchedule? =
                if (tid == null) {
                    // pending
                    _pendingSchedulesForNewTask.value.firstOrNull { it.key == key }?.schedule
                } else {
                    // DB
                    schedulesUiForTaskDialog.value.firstOrNull { it.key == key }?.schedule
                    // یا اگر می‌خوای مطمئن‌تر: scheduleRepo.getById(key)
                }

            schedule ?: return@launch
            _scheduleDraft.value = schedule.toDraft()
        }
    }
    fun finishEditSchedule() {
        _editingScheduleKey.value = null
        _scheduleDraft.value = ScheduleDraft()
    }
    fun deleteScheduleByKey(key: Int) {
        val tid = _editingTaskId.value
        if (tid == null) {
            // pending delete
            _pendingSchedulesForNewTask.update { list -> list.filterNot { it.key == key } }
        } else {
            // DB delete
            viewModelScope.launch(Dispatchers.IO) {
                val target = schedulesUiForTaskDialog.value.firstOrNull { it.key == key }?.schedule
                if (target != null) scheduleRepo.delete(target)
            }
        }
    }
    fun startAddSchedule() {
        _editingScheduleKey.value = null      // یعنی Add schedule (نه edit)
        _scheduleDraft.value = defaultScheduleDraftNow()
    }
    fun setScheduleDate(d: LocalDate) = _scheduleDraft.update { it.copy(date = d) }
    fun setRepeatEnabled(v: Boolean) {
        _scheduleDraft.update { it.copy(repeat = it.repeat.copy(enabled = v)) }
    }

    fun setRepeatInterval(n: Int) {
        _scheduleDraft.update {
            it.copy(repeat = it.repeat.copy(interval = n.coerceIn(1, 99)))
        }
    }

    fun setRepeatUnit(u: RepeatUnit) {
        _scheduleDraft.update { d ->
            val r = d.repeat
            val newMask =
                if (u == RepeatUnit.WEEK && r.weekdaysMask == 0) {
                    // پیش‌فرض: امروز تیک بخورد
                    1 shl todayBitIndex()
                } else r.weekdaysMask

            d.copy(repeat = r.copy(unit = u, weekdaysMask = newMask))
        }
    }

    fun setRepeatWeekdaysMask(mask: Int) {
        _scheduleDraft.update { it.copy(repeat = it.repeat.copy(weekdaysMask = mask.coerceIn(0, 127))) }
    }

    // Sa..Fr => 0..6
    private fun todayBitIndex(): Int {
        // اگر خواستی دقیق با تقویم خودت تنظیم می‌کنیم؛
        // فعلاً یک نگاشت ساده:
        val dow = java.time.LocalDate.now().dayOfWeek // MON..SUN
        return when (dow) {
            java.time.DayOfWeek.SATURDAY -> 0
            java.time.DayOfWeek.SUNDAY -> 1
            java.time.DayOfWeek.MONDAY -> 2
            java.time.DayOfWeek.TUESDAY -> 3
            java.time.DayOfWeek.WEDNESDAY -> 4
            java.time.DayOfWeek.THURSDAY -> 5
            java.time.DayOfWeek.FRIDAY -> 6
        }
    }








    private fun buildTaskMiniAll(tasks: List<Task>, schedules: Map<Int, Boolean> = emptyMap()): List<TaskMiniUi> =
        tasks.mapNotNull { t ->
            val id = t.id ?: return@mapNotNull null
            TaskMiniUi(
                id = id,
                title = t.name,
                isDone = t.isCompleted,
                hasSchedule = schedules[id] == true,
                parentTaskId = t.parentTaskId,
                siblingIndex = t.siblingIndex,
                priority = t.priority
            )
        }

    private fun flattenAllTasks(allMini: List<TaskMiniUi>): List<TaskMiniUi> {
        val byParent = allMini.groupBy { it.parentTaskId }
        val out = mutableListOf<TaskMiniUi>()

        fun dfs(parentId: Int) {
            val children = byParent[parentId].orEmpty()
                .sortedWith(compareBy<TaskMiniUi> { it.siblingIndex }.thenBy { it.id })
            for (c in children) {
                out += c
                dfs(c.id)
            }
        }
        dfs(ROOT)
        return out
    }

    private fun pickParentIdForDesiredDepth(
        flat: List<TaskMiniUi>,
        parentById: Map<Int, Int?>,
        desiredDepth: Int,
        insertionIndex: Int
    ): Int? {
        if (desiredDepth <= 0) return null

        // parent depth = desiredDepth - 1
        val parentDepth = desiredDepth - 1

        fun depthOfLocal(id: Int): Int {
            var d = 0
            var cur = parentById[id] ?: ROOT
            var guard = 0
            while (cur != ROOT && guard < 200) {
                d++
                cur = parentById[id] ?: ROOT
                guard++
            }
            return d
        }

        // فقط آیتم‌های قبل از محل درج
        val before = flat.take(insertionIndex.coerceIn(0, flat.size))

        // آخرین آیتمی که depthش == parentDepth
        val parent = before.lastOrNull { depthOfLocal(it.id) == parentDepth } ?: return null
        return parent.id
    }



    private fun buildParentById(tasks: List<Task>): Map<Int, Int> =
        tasks.mapNotNull { t ->
            val id = t.id ?: return@mapNotNull null
            id to (t.parentTaskId ?: ROOT)
        }.toMap()

    private fun depthOf(id: Int, parentById: Map<Int, Int>): Int {
        var depth = 0
        var cur = parentById[id] ?: ROOT
        var guard = 0
        while (cur != ROOT && guard < 100) {
            depth++
            cur = parentById[cur] ?: ROOT
            guard++
        }
        return depth.coerceIn(0, 3)
    }

    fun trySetDraftParent(parentId: Int): Boolean {
        val parentLevel = uiState.value.levelById[parentId] ?: 1
        if (parentLevel >= 4) return false
        _draft.update { it.copy(parentId = parentId) }
        return true
    }

    fun setMenuCategoryId(id: Int?) {
        _menuCategoryId.value = id
    }

    fun onDragStartMaybeCollapse(categoryId: Int) {
        val item = uiState.value.renderItems.firstOrNull { it.category.categoryId == categoryId } ?: return
        if (!item.hasChildren) return

        val isExpandedNow = item.isExpanded
        if (isExpandedNow) {
            _dragCollapsedRestoreCategory.value = categoryId
            _collapsedIdsCategory.update { it + categoryId }
            // Persist اختیاری:
            viewModelScope.launch {
                val current = uiState.value.categories.firstOrNull { it.categoryId == categoryId } ?: return@launch
                categoryRepository.updateCategory(current.copy(isExtended = false))
            }
        }
    }

    fun onDragEndRestoreExpand() {
        val id = _dragCollapsedRestoreCategory.value ?: return
        _dragCollapsedRestoreCategory.value = null

        _collapsedIdsCategory.update { it - id }

        // Persist اختیاری:
        viewModelScope.launch {
            val current = uiState.value.categories.firstOrNull { it.categoryId == id } ?: return@launch
            categoryRepository.updateCategory(current.copy(isExtended = true))
        }
    }

    fun toggleExpand(categoryId: Int) {
        viewModelScope.launch {
            val current = uiState.value.categories.firstOrNull { it.categoryId == categoryId } ?: return@launch
            val willCollapse = !_collapsedIdsCategory.value.contains(categoryId)

            _collapsedIdsCategory.update { set ->
                if (willCollapse) set + categoryId else set - categoryId
            }

            // Persist
            categoryRepository.updateCategory(current.copy(isExtended = !willCollapse))
        }
    }

    private fun flattenCategoryTreeWithLevelsAndVisibility(
        all: List<CategoryEntity>,
        collapsedIds: Set<Int>,
        rootParentId: Int = -1,
        maxDepth: Int = 4
    ): FlattenResult {

        val byParent = all.groupBy { it.parentCategoryId }
        val items = mutableListOf<CategoryRenderItem>()
        val levelById = mutableMapOf<Int, Int>()

        fun dfs(parentId: Int?, realDepth: Int, ancestorCollapsed: Boolean) {
            // ✅ توقف فقط برای امنیت (در صورت داده خراب/سیکل)
            if (realDepth > 50) return

            val renderLevel = realDepth.coerceAtMost(maxDepth)

            val children = (byParent[parentId] ?: emptyList())
                .sortedBy { it.siblingIndex }

            for (child in children) {
                val id = child.categoryId
                val hasChildrenRaw = (byParent[id] ?: emptyList()).isNotEmpty()

                // ✅ دکمه expand فقط تا سطح ۴ (مثل قبل)
                val hasChildren = (renderLevel < maxDepth) && hasChildrenRaw

                val selfCollapsed = id != null && collapsedIds.contains(id)
                val isExpanded = id != null && !selfCollapsed

                if (id != null) levelById[id] = renderLevel

                val visible = !ancestorCollapsed

                items.add(
                    CategoryRenderItem(
                        category = child,
                        level = renderLevel,         // ✅ level نمایشی
                        hasChildren = hasChildren,
                        isExpanded = isExpanded,
                        isVisible = visible
                    )
                )

                // ✅ ادامه بده، حتی اگر depth واقعی از ۴ رد شد
                dfs(
                    parentId = id,
                    realDepth = realDepth + 1,
                    ancestorCollapsed = ancestorCollapsed || selfCollapsed
                )
            }
        }

        dfs(rootParentId, realDepth = 1, ancestorCollapsed = false)
        return FlattenResult(items, levelById)
    }

    private fun flattenForPickerAllVisible(
        all: List<CategoryEntity>,
        rootParentId: Int = -1,
        maxDepth: Int = 4
    ): FlattenResult {

        val byParent = all.groupBy { it.parentCategoryId }
        val items = mutableListOf<CategoryRenderItem>()
        val levelById = mutableMapOf<Int, Int>()

        fun dfs(parentId: Int?, realDepth: Int) {
            if (realDepth > 50) return // گارد امنیتی

            val renderLevel = realDepth.coerceAtMost(maxDepth)

            val children = (byParent[parentId] ?: emptyList())
                .sortedBy { it.siblingIndex }

            for (child in children) {
                val id = child.categoryId
                val hasChildrenRaw = (byParent[id] ?: emptyList()).isNotEmpty()
                val hasChildren = (renderLevel < maxDepth) && hasChildrenRaw

                if (id != null) levelById[id] = renderLevel

                items.add(
                    CategoryRenderItem(
                        category = child,
                        level = renderLevel,
                        hasChildren = hasChildren,
                        isExpanded = true,  // در picker مهم نیست
                        isVisible = true    // ✅ همیشه visible
                    )
                )

                dfs(id, realDepth + 1)
            }
        }

        dfs(rootParentId, realDepth = 1)
        return FlattenResult(items, levelById)
    }

    fun deleteCategoryPromoteChildren(categoryId: Int) {
        viewModelScope.launch {
            val all = uiState.value.categories
            val target = all.firstOrNull { it.categoryId == categoryId } ?: return@launch

            val parentOfTarget = target.parentCategoryId
            val children = all.filter { it.parentCategoryId == categoryId }

            // 1) بچه‌ها رو به parentِ کتگوری حذف‌شده وصل کن
            // siblingIndex جدید: بعد از آخرین بچه‌های parentOfTarget
            val baseIndex = all.count { it.parentCategoryId == parentOfTarget }

            children.forEachIndexed { i, child ->
                categoryRepository.updateCategory(
                    child.copy(
                        parentCategoryId = parentOfTarget,
                        siblingIndex = baseIndex + i
                    )
                )
            }

            // 2) خود کتگوری حذف شود
            categoryRepository.deleteCategory(target)

            // 3) مرتب‌سازی siblingIndex برای parent قبلی (جای خالی پر شود)
            reorderSiblings(parentOfTarget)

            // 4) مرتب‌سازی siblingIndex برای parent جدید (بعد از promote)
            reorderSiblings(parentOfTarget)
        }
    }

    private suspend fun reorderSiblings(parentId: Int?) {
        val siblings = uiState.value.categories
            .filter { it.parentCategoryId == parentId }
            .sortedBy { it.siblingIndex }

        siblings.forEachIndexed { idx, e ->
            if (e.siblingIndex != idx) {
                categoryRepository.updateCategory(e.copy(siblingIndex = idx))
            }
        }
    }

    fun finishEditTask() {
        _editingTaskId.value = null
        _taskDraft.value = TaskDraft()
    }

    //ساخت پیش‌فرض Start/End با زمان فعلی
    private fun defaultScheduleDraftNow(): ScheduleDraft {
        val now = LocalTime.now()
        val start = now.ceilToNextQuarter()
        val end = start.plusMinutes(60)

        return ScheduleDraft(
            title = "",
            mode = ScheduleMode.TIME_RANGE,
            date = LocalDate.now(),
            start = start,
            end = end,
            durationMinutes = 60,

            // ✅ تکرار پیش‌فرض خاموش
            repeat = RepeatDraft(
                enabled = false,
                interval = 1,
                unit = RepeatUnit.DAY,
                weekdaysMask = 0
            )
        )
    }










    sealed class CreateResult {
        data object Success : CreateResult()
        data class Error(val message: String) : CreateResult()
    }
}










data class CategoryUiState2(
    val isLoading: Boolean = true,
    val categories: List<CategoryEntity> = emptyList(),
    val renderItems: List<CategoryRenderItem> = emptyList(),

    val tasks: List<Task> = emptyList(),
    val taskRenderItems: List<TaskRenderItem> = emptyList(),
    val levelById: Map<Int, Int> = emptyMap(),

    )

data class CategoryRenderItem(
    val category: CategoryEntity,
    val level: Int,
    val hasChildren: Boolean,
    val isExpanded: Boolean,
    val isVisible: Boolean
)

data class FlattenResult(
    val items: List<CategoryRenderItem>,
    val levelById: Map<Int, Int>
)

data class CategoryDraft2(
    val name: String = "",
    val parentId: Int = -1,
    val iconName: String = "QuestionMark",
    val color: String =  "#2196F3",  // آبی متریال
    val description: String = ""
)

data class TaskMiniUi(
    val id: Int,
    val title: String,
    val isDone: Boolean = false,
    val hasSchedule: Boolean = false,

    val parentTaskId: Int? = null,
    val siblingIndex: Int = 0,
    val priority: Int = 0
)

data class TaskDraft(
    val name: String = "",
    val categoryId: Int? = null,
    val priority: Int = 0,
    val isCompleted: Boolean = false,
    val note: String = "",

    // ✅ جدیدها برای دیالوگ
    val insertAtTop: Boolean = false, // false=آخر لیست، true=اول لیست
    val childLevel: Int = 0           // 0..3 (0 یعنی هیچ)
)

data class ScheduleDraft(
    val title: String = "",
    val mode: ScheduleMode = ScheduleMode.TIME_RANGE,

    val date: LocalDate = LocalDate.now(),
    val start: LocalTime = LocalTime.of(20, 0),
    val end: LocalTime = LocalTime.of(21, 0),
    val durationMinutes: Int = 60,

    val repeat: RepeatDraft = RepeatDraft()
)

data class TaskRenderItem(
    val task: TaskMiniUi,
    val level: Int,        // 1..4 (نمایشی)
    val hasChildren: Boolean,
    val isExpanded: Boolean,
    val isVisible: Boolean
)

data class ChildLevelUi(
    val allowed: Set<Int> = setOf(0),
    val maxAllowed: Int = 0
)

data class TaskScheduleUi(
    val key: Int,              // برای pending منفی، برای DB همون id
    val schedule: TaskSchedule,
    val isPending: Boolean
)

data class RepeatDraft(
    val enabled: Boolean = false,
    val interval: Int = 1,
    val unit: RepeatUnit = RepeatUnit.DAY,
    val weekdaysMask: Int = 0
)