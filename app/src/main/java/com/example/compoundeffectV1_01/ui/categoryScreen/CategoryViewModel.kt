package com.example.compoundeffectV1_01.ui.categoryScreen


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.compoundeffectV1_01.data.dataBaseRoom.tables.category.CategoryEntity
import com.example.compoundeffectV1_01.data.dataBaseRoom.tables.category.CategoryRepository
import com.example.compoundeffectV1_01.data.dataBaseRoom.tables.task.Task
import com.example.compoundeffectV1_01.data.dataBaseRoom.tables.task.TaskRepository
import com.example.compoundeffectV1_01.data.dataBaseRoom.tables.task.TaskWithSchedule
import com.example.compoundeffectV1_01.data.dataBaseRoom.tables.taskSchedule.ScheduleMode
import com.example.compoundeffectV1_01.data.dataBaseRoom.tables.taskSchedule.TaskSchedule
import com.example.compoundeffectV1_01.data.dataBaseRoom.tables.taskSchedule.TaskScheduleRepository
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

    @OptIn(ExperimentalCoroutinesApi::class)
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
                        indentLevel = t.indentLevel,
                        parentTaskId = t.parentTaskId
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
                rootParentId = null,
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
    fun applyDragResult(
        draggedId: Int,
        oldParentId: Int?,
        newParentId: Int?,
        currentList: List<CategoryRenderItem>
    ) {
        viewModelScope.launch {
            val dragged = uiState.value.categories.firstOrNull { it.categoryId == draggedId } ?: return@launch

            val finalNewParent = newParentId ?: dragged.parentCategoryId

            // parent جدید باید سطحش < 4 باشد
            val parentLevel = uiState.value.levelById[finalNewParent ?: return@launch] ?: 1
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



    //تسک ها
    fun startAddTask(categoryId: Int, categoryColor: String) {
        _taskDraft.value = TaskDraft(categoryId = categoryId)
        _scheduleDraft.value = ScheduleDraft()          // ✅ ریست
        _scheduleConfirmedForNewTask.value = false
    }
    fun startEditTask(taskId: Int) {
        viewModelScope.launch {
            val t = taskRepo.getTaskById(taskId) ?: return@launch

            _editingTaskId.value = taskId
            _taskDraft.value = TaskDraft(
                name = t.name,
                categoryId = t.categoryId,
                priority = t.priority,
                isCompleted = t.isCompleted,
                note = t.description,
                insertAtTop = false,
                childLevel = t.indentLevel
            )



            val sch = scheduleRepo.getByTaskId(taskId)

            fun minuteOfDayToLocalTime(min: Int): java.time.LocalTime =
                java.time.LocalTime.of(min / 60, min % 60)

            _scheduleDraft.value =
                if (sch == null) {
                    ScheduleDraft()
                } else {
                    ScheduleDraft(
                        title = sch.title.orEmpty(),
                        mode = sch.mode,

                        date = sch.dateEpochDay?.let(java.time.LocalDate::ofEpochDay) ?: java.time.LocalDate.now(),
                        start = sch.startMinuteOfDay?.let(::minuteOfDayToLocalTime) ?: java.time.LocalTime.of(20, 0),
                        end = sch.endMinuteOfDay?.let(::minuteOfDayToLocalTime) ?: java.time.LocalTime.of(21, 0),

                        durationMinutes = sch.durationMinutes ?: 60,
                        repeating = sch.repeating
                    )
                }
        }
    }
    fun setTaskName(v: String) = _taskDraft.update { it.copy(name = v) }
    fun setTaskPriority(p: Int) = _taskDraft.update { it.copy(priority = p) }
    fun setTaskCompleted(v: Boolean) = _taskDraft.update { it.copy(isCompleted = v) }
    fun setTaskNote(v: String) = _taskDraft.update { it.copy(note = v) }
    fun setTaskInsertAtTop(v: Boolean) = _taskDraft.update { it.copy(insertAtTop = v) }
    fun setTaskChildLevel(v: Int) = _taskDraft.update { it.copy(childLevel = v.coerceIn(0, 3)) }
    fun markScheduleConfirmedForNewTask() {
        _scheduleConfirmedForNewTask.value = true
    }
    fun resetScheduleConfirmedForNewTask() {
        _scheduleConfirmedForNewTask.value = false
    }
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
            val ordered = withContext(Dispatchers.IO) {
                taskRepo.getTasksByCategoryOrdered(categoryId)
            }

            val minIdx = ordered.minOfOrNull { it.orderIndex }
            val maxIdx = ordered.maxOfOrNull { it.orderIndex }

            val newOrderIndex =
                if (d.insertAtTop) (minIdx?.minus(1) ?: 0)
                else (maxIdx?.plus(1) ?: 0)

            val newIndent = d.childLevel.coerceIn(0, 3)

            // ✅ parent ساده و پایدار:
            // اگر insertAtTop => parent=null
            // اگر indent>0 و insertAtTop=false => آخرین آیتم با indent-1
            val parentId: Int? =
                if (newIndent <= 0) null
                else if (d.insertAtTop) null
                else {
                    val targetIndent = newIndent - 1
                    ordered.asReversed().firstOrNull { it.indentLevel == targetIndent }?.id
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
                orderIndex = newOrderIndex,
                indentLevel = newIndent,
                parentTaskId = parentId
            )

            val newId = withContext(Dispatchers.IO) {
                taskRepo.insertTaskAndReturnId(newTask) // ✅ اینجا Int برمی‌گرده
            }

            // ✅ اگر schedule تایید شده بود، همینجا ذخیره کن
            if (_scheduleConfirmedForNewTask.value) {
                val sd = _scheduleDraft.value

                fun LocalTime.toMinuteOfDay(): Int = hour * 60 + minute

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
                    repeating = sd.repeating
                )

                withContext(Dispatchers.IO) {
                    scheduleRepo.upsert(schedule)
                }
            }

            // ✅ اینجا چیزی ریست نکن؛ کنترل close/continue بیرون دیالوگه
        }
    }
    private fun resetTaskDraft() {
        _taskDraft.value = TaskDraft()
    }
    fun toggleTaskCompleted(taskId: Int, done: Boolean) {
        viewModelScope.launch {
            val t = taskRepo.getTaskById(taskId) ?: return@launch
            withContext(Dispatchers.IO) {
                taskRepo.updateTask(t.copy(isCompleted = done))
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
            val categoryId = current.categoryId ?: return@launch

            val childrenCount = withContext(Dispatchers.IO) { taskRepo.countChildren(taskId) }
            if (childrenCount > 0 && d.childLevel < current.indentLevel) {
                // فعلاً اجازه نده پایین بیاد (بعداً می‌تونیم کل subtree رو شیفت بدیم)
                return@launch
            }

            val ordered = withContext(Dispatchers.IO) {
                taskRepo.getTasksByCategoryOrdered(categoryId)
            }

            val selfIndex = ordered.indexOfFirst { it.id == taskId }
            if (selfIndex == -1) return@launch

            val (finalIndent, parentId) = computeParentForIndent(
                ordered = ordered,
                selfIndex = selfIndex,
                desiredIndent = d.childLevel
            )

            val updated = current.copy(
                name = d.name.trim(),
                description = d.note,
                isCompleted = d.isCompleted,
                priority = d.priority,
                color = categoryColor,
                indentLevel = finalIndent,
                parentTaskId = parentId
            )

            withContext(Dispatchers.IO) {
                taskRepo.updateTask(updated)
            }

            finishEditTask()
        }
    }
    private fun computeParentForIndent(
        ordered: List<Task>,
        selfIndex: Int,
        desiredIndent: Int
    ): Pair<Int, Int?> {
        // خروجی: (finalIndent, parentId)
        if (desiredIndent <= 0) return 0 to null

        val prev = ordered.getOrNull(selfIndex - 1)
        // قانون ساده: indent بیشتر از (indent قبلی + 1) نشه
        val maxAllowedIndent = ((prev?.indentLevel ?: 0) + 1).coerceAtMost(3)
        val finalIndent = desiredIndent.coerceIn(0, maxAllowedIndent)

        if (finalIndent <= 0) return 0 to null

        val targetIndent = finalIndent - 1
        val parent = ordered
            .subList(0, selfIndex)
            .asReversed()
            .firstOrNull { it.indentLevel == targetIndent }

        return if (parent == null) (0 to null) else (finalIndent to parent.id)
    }
    fun flattenTaskTreeWithLevelsAndVisibility(
        all: List<TaskMiniUi>,
        collapsedIds: Set<Int>,
        rootParentId: Int? = null,
        maxDepth: Int = 4
    ): List<TaskRenderItem> {

        // مرتب‌سازی: چون orderIndex داخل TaskMiniUi نداریم،
        // همین لیستی که از DB میاد را به ترتیب دریافت می‌کنیم (در DAO باید ORDER BY داشته باشد)
        // پس اینجا فرض می‌کنیم all همین ترتیب درست است.

        val byParent = all.groupBy { it.parentTaskId }
        val items = mutableListOf<TaskRenderItem>()

        fun dfs(parentId: Int?, realDepth: Int, ancestorCollapsed: Boolean) {
            if (realDepth > 50) return

            val renderLevel = realDepth.coerceAtMost(maxDepth)
            val children = byParent[parentId].orEmpty()

            for (child in children) {
                val id = child.id
                if (id < 0) continue

                val hasChildrenRaw = byParent[id].orEmpty().isNotEmpty()
                val hasChildren = (renderLevel < maxDepth) && hasChildrenRaw

                val selfCollapsed = collapsedIds.contains(id)
                val isExpanded = !selfCollapsed
                val visible = !ancestorCollapsed

                items += TaskRenderItem(
                    task = child,
                    level = renderLevel,
                    hasChildren = hasChildren,
                    isExpanded = isExpanded,
                    isVisible = visible
                )

                dfs(
                    parentId = id,
                    realDepth = realDepth + 1,
                    ancestorCollapsed = ancestorCollapsed || selfCollapsed
                )
            }
        }

        dfs(rootParentId, realDepth = 1, ancestorCollapsed = false)
        return items
    }
    fun findBlockRange(list: List<TaskMiniUi>, startIndex: Int): IntRange {
        val base = list[startIndex]
        val baseIndent = base.indentLevel // یا childLevel/indentLevel نمایشی‌ات
        var end = startIndex
        for (i in startIndex + 1 .. list.lastIndex) {
            val cur = list[i]
            if (cur.indentLevel <= baseIndent) break
            end = i
        }
        return startIndex..end
    }
    private fun normalizeTasksAndComputeParents(
        flat: List<TaskMiniUi>
    ): List<TaskReorderUpdate> {

        // lastIdAtIndent[i] = آخرین تسک دیده‌شده در indent i
        val lastIdAtIndent = arrayOfNulls<Int>(TASK_MAX_INDENT + 1)

        val updates = ArrayList<TaskReorderUpdate>(flat.size)

        flat.forEachIndexed { index, t ->
            var indent = t.indentLevel.coerceIn(0, TASK_MAX_INDENT)

            // ✅ جلوگیری از "پرش": اگر indent=2 ولی indent=1 نداریم => خواهر/برادرش کن => indent را کم کن
            while (indent > 0 && lastIdAtIndent[indent - 1] == null) {
                indent -= 1
            }

            val parentId = if (indent == 0) null else lastIdAtIndent[indent - 1]

            // ثبت در lastIdAtIndent
            lastIdAtIndent[indent] = t.id
            // سطوح عمیق‌تر را پاک کن تا ساختار درست بماند
            for (i in indent + 1..TASK_MAX_INDENT) lastIdAtIndent[i] = null

            updates += TaskReorderUpdate(
                id = t.id,
                orderIndex = index,     // ✅ ساده‌ترین و پایدارترین
                indentLevel = indent,
                parentTaskId = parentId
            )
        }

        return updates
    }
    fun applyTaskDragResult(categoryId: Int, finalFlatList: List<TaskMiniUi>) {
        viewModelScope.launch {
            val updates = normalizeTasksAndComputeParents(finalFlatList)

            withContext(Dispatchers.IO) {
                taskRepo.applyTaskReorderAndHierarchy(updates)
            }
        }
    }
    fun applyTaskDragResult(
        draggedId: Int,
        oldParentId: Int?,
        newParentId: Int?,
        categoryId: Int,
        currentList: List<TaskRenderItem>
    ) {
        viewModelScope.launch {

            // 1) گرفتن تسک درگ‌شده از DB (قابل اعتمادتر از uiState)
            val dragged = withContext(Dispatchers.IO) {
                taskRepo.getTaskById(draggedId)
            } ?: return@launch

            val finalNewParent: Int? = when (newParentId) {
                -1 -> null                 // ✅ یعنی “ریشه”
                null -> dragged.parentTaskId // یعنی “اصلاً تغییر parent ندادی”
                else -> newParentId
            }


            // 2) محاسبه indent مناسب بر اساس parent جدید
            val finalIndent: Int = if (finalNewParent == null) {
                0
            } else {
                val parentIndent = withContext(Dispatchers.IO) {
                    taskRepo.getTaskById(finalNewParent)?.indentLevel
                } ?: return@launch

                val desired = parentIndent + 1
                if (desired > 3) return@launch   // سقف عمق
                desired
            }

            // 3) اگر parent/indent تغییر کرده، خود تسک را آپدیت کن
            if (finalNewParent != dragged.parentTaskId || finalIndent != dragged.indentLevel) {
                withContext(Dispatchers.IO) {
                    taskRepo.updateTask(
                        dragged.copy(
                            parentTaskId = finalNewParent,
                            indentLevel = finalIndent
                        )
                    )
                }
            }

            suspend fun reorderFor(parentId: Int?) {
                // ترتیب جدید siblings زیر این parent بر اساس currentList (فقط visible)
                val orderedIds = currentList
                    .asSequence()
                    .filter { it.isVisible }
                    .filter { item ->
                        val id = item.task.id
                        val p = if (id == draggedId) finalNewParent else item.task.parentTaskId
                        p == parentId
                    }
                    .map { it.task.id }
                    .toList()

                if (orderedIds.isEmpty()) return

                // همه تسک‌های دسته برای mapping (orderIndex/parentTaskId اصلی از DB)
                val all = withContext(Dispatchers.IO) {
                    taskRepo.getTasksByCategoryOrdered(categoryId)
                }
                val byId = all.associateBy { it.id }

                withContext(Dispatchers.IO) {
                    orderedIds.forEachIndexed { index, id ->
                        val entity =
                            if (id == draggedId) {
                                // ممکنه هنوز در DB با وضعیت قبلی باشد؛ نسخه نهایی را اعمال کن
                                dragged.copy(
                                    parentTaskId = finalNewParent,
                                    indentLevel = finalIndent
                                )
                            } else {
                                byId[id] ?: return@forEachIndexed
                            }

                        // اینجا فقط orderIndex را sync می‌کنیم
                        taskRepo.updateTask(entity.copy(orderIndex = index))
                    }
                }
            }

            // مثل کتگوری: هم oldParent و هم newParent را reorder کن
            reorderFor(oldParentId)
            if (finalNewParent != oldParentId) {
                reorderFor(finalNewParent)
            }
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




    //اسچدول ها
    fun setScheduleTitle(v: String) = _scheduleDraft.update { it.copy(title = v) }
    fun setScheduleMode(m: ScheduleMode) = _scheduleDraft.update { it.copy(mode = m) }
    fun setScheduleDate(d: LocalDate) = _scheduleDraft.update { it.copy(date = d) }
    fun setScheduleStart(t: LocalTime) = _scheduleDraft.update { it.copy(start = t) }
    fun setScheduleEnd(t: LocalTime) = _scheduleDraft.update { it.copy(end = t) }
    fun setScheduleDuration(min: Int) = _scheduleDraft.update { it.copy(durationMinutes = min.coerceAtLeast(1)) }
    fun setScheduleRepeating(v: Boolean) = _scheduleDraft.update { it.copy(repeating = v) }
    fun saveScheduleForCurrentTask() {
        val taskId = _editingTaskId.value ?: return

        viewModelScope.launch {
            val d = _scheduleDraft.value

            fun java.time.LocalTime.toMinuteOfDay(): Int = this.hour * 60 + this.minute

            // (اختیاری) اعتبارسنجی برای TIME_RANGE
            if (d.mode == ScheduleMode.TIME_RANGE) {
                val s = d.start.toMinuteOfDay()
                val e = d.end.toMinuteOfDay()
                if (e <= s) return@launch // یا اینجا خطا/اسنک‌بار بده
            }

            val schedule = TaskSchedule(
                id = null, // اگر upsert بر اساس taskId unique باشه، null هم اوکیه
                taskId = taskId,
                title = d.title.trim().ifBlank { null },
                mode = d.mode,

                dateEpochDay = if (d.mode == ScheduleMode.TIME_RANGE) d.date.toEpochDay() else null,
                startMinuteOfDay = if (d.mode == ScheduleMode.TIME_RANGE) d.start.toMinuteOfDay() else null,
                endMinuteOfDay = if (d.mode == ScheduleMode.TIME_RANGE) d.end.toMinuteOfDay() else null,

                durationMinutes = if (d.mode == ScheduleMode.AMOUNT_OF_TIME) d.durationMinutes else null,
                inPallet = false,
                repeating = d.repeating
            )

            withContext(Dispatchers.IO) {
                scheduleRepo.upsert(schedule)
            }
        }
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


    fun reorderWithinSameParent(parentId: Int?, orderedIds: List<Int>) {
        viewModelScope.launch {
            val current = uiState.value.categories.filter { it.parentCategoryId == parentId }
            val byId = current.associateBy { it.categoryId }

            orderedIds.forEachIndexed { index, id ->
                val entity = byId[id] ?: return@forEachIndexed
                categoryRepository.updateCategory(entity.copy(siblingIndex = index))
            }
        }
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

    // ✅ برای tree
    val indentLevel: Int = 0,      // 0..3
    val parentTaskId: Int? = null  // null یعنی ریشه
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
    val repeating: Boolean = false
)

data class TaskRenderItem(
    val task: TaskMiniUi,
    val level: Int,        // 1..4 (نمایشی)
    val hasChildren: Boolean,
    val isExpanded: Boolean,
    val isVisible: Boolean
)

data class TaskReorderUpdate(
    val id: Int,
    val orderIndex: Int,
    val indentLevel: Int,
    val parentTaskId: Int?
)
