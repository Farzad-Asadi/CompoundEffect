package com.example.compoundeffectV1_01.ui.categoryScreen

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Task
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.compoundeffectV1_01.data.dataBaseRoom.tables.category.CategoryEntity
import com.example.compoundeffectV1_01.data.dataBaseRoom.tables.task.Task
import com.example.compoundeffectV1_01.data.dataBaseRoom.tables.taskSchedule.ScheduleMode
import com.example.compoundeffectV1_01.utils.DimmedDialog
import com.example.compoundeffectV1_01.utils.IconOption
import com.example.compoundeffectV1_01.utils.buildColorOptions
import com.example.compoundeffectV1_01.utils.buildIconSections
import com.example.compoundeffectV1_01.utils.colorFromHex
import com.example.compoundeffectV1_01.utils.iconFromKey
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable
import java.time.LocalTime

@SuppressLint("SuspiciousIndentation")
@Composable
fun CategoryScreen(
    onNavigateToSchedule: () -> Unit, // فعلاً استفاده نمی‌کنیم، بعداً به bottom bar وصل می‌کنیم
    viewModel: CategoryViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val draft by viewModel.draft.collectAsState()
    val createResult by viewModel.createResult.collectAsState()
    val pickerFlatten by viewModel.parentPickerItems.collectAsState()
    val editingTaskId by viewModel.editingTaskId.collectAsState()
    val taskDraft by viewModel.taskDraft.collectAsState()
    val scheduleDraft by viewModel.scheduleDraft.collectAsState()
    val menuCategoryId by viewModel.menuCategoryId.collectAsState()
    val tasksForMenu by viewModel.tasksForMenuCategory.collectAsState()
    val tasksWithSchedule by viewModel.tasksWithScheduleForMenu.collectAsState()
    val scheduledCount by viewModel.scheduledCountForMenu.collectAsState()


    var showPickParent by rememberSaveable { mutableStateOf(false) }
    var showAddCategory by rememberSaveable { mutableStateOf(false) }
    var isDragging by remember { mutableStateOf(false) }

    var showIconPicker by rememberSaveable { mutableStateOf(false) }
    var showColorPicker by rememberSaveable { mutableStateOf(false) }

    var nameError by rememberSaveable { mutableStateOf<String?>(null) }

    val parentEntity = state.categories.firstOrNull { it.categoryId == draft.parentId }


    var showDeleteConfirm by rememberSaveable { mutableStateOf(false) }
    val menuCategory = state.categories.firstOrNull { it.categoryId == menuCategoryId }


    var showRenameDialog by rememberSaveable { mutableStateOf(false) }
    var renameText by rememberSaveable { mutableStateOf("") }
    var renameError by rememberSaveable { mutableStateOf<String?>(null) }
    var showEditIconPicker by rememberSaveable { mutableStateOf(false) }
    var showEditColorPicker by rememberSaveable { mutableStateOf(false) }

    var showAddTaskDialog by rememberSaveable { mutableStateOf(false) }
    var tasksExpanded by rememberSaveable(menuCategoryId) { mutableStateOf(false) }

    var showScheduleDialog by rememberSaveable { mutableStateOf(false) }

    var sheetMode by rememberSaveable(menuCategoryId) { mutableStateOf(CategorySheetMode.OVERVIEW) }

    var showTaskDialog by rememberSaveable { mutableStateOf(false) }


    LaunchedEffect(createResult) {
        nameError = (createResult as? CategoryViewModel.CreateResult.Error)?.message
    }

    LaunchedEffect(createResult) {
        when (createResult) {
            is CategoryViewModel.CreateResult.Success -> {
                showAddCategory = false
                showPickParent = false
                showIconPicker = false
                showColorPicker = false
                viewModel.resetDraft()
            }

            else -> Unit
        }
    }

    LaunchedEffect(menuCategoryId, tasksForMenu.size) {
        if (menuCategoryId != null && tasksForMenu.isNotEmpty()) {
            tasksExpanded = true // ✅ وقتی تسک آمد، اتومات باز شود
        }
    }



    Scaffold(
//        topBar = { TopAppBar(title = { Text("CategoryScreen2") }) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddCategory = true }
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Category")
            }


        },
        floatingActionButtonPosition = androidx.compose.material3.FabPosition.Start

        // bottomBar فعلاً از Navigation foundation میاد، بعداً یکدستش می‌کنیم
    ) { padding ->

        Column(modifier = Modifier.padding(padding)) {

            var dragging by remember { mutableStateOf(false) }

            val listState = remember { mutableStateOf(state.renderItems) }

            // parent قبلی و parent موقت جدید
            val fromParentId = remember { mutableStateOf<Int?>(null) }
            val pendingParentId = remember { mutableStateOf<Int?>(null) }

            val pendingParentById =
                remember { mutableStateMapOf<Int, Int?>() } // draggedId -> newParentId
            val dragOffsetXById =
                remember { mutableStateMapOf<Int, Float>() }  // draggedId -> accumulatedX
            val threshold = 70f

            val lastReparentAtById = remember { mutableStateMapOf<Int, Long>() }
            val reparentDelayMs = 200L

            val childrenByParent = remember(state.categories) {
                state.categories
                    .filter { it.categoryId != null }
                    .groupBy { it.parentCategoryId }
                    .mapValues { entry -> entry.value.mapNotNull { it.categoryId } }
            }

            fun isDescendantOfDragged(candidateParentId: Int, draggedId: Int): Boolean {
                // آیا candidateParentId داخل زیر درخت draggedId است؟
                val stack = ArrayDeque<Int>()
                stack.add(draggedId)

                while (stack.isNotEmpty()) {
                    val cur = stack.removeLast()
                    val children = childrenByParent[cur] ?: emptyList()
                    if (candidateParentId in children) return true
                    children.forEach { stack.add(it) }
                }
                return false
            }


            fun effectiveParentId(item: CategoryRenderItem): Int? {
                val id = item.category.categoryId ?: return item.category.parentCategoryId
                return pendingParentById[id] ?: item.category.parentCategoryId
            }


            // وقتی درگ نداریم، با state.sync شو
            LaunchedEffect(state.renderItems) {
                if (!dragging) listState.value = state.renderItems
            }

            val draggingKey = remember { mutableStateOf<Int?>(null) }

            val reorderState = rememberReorderableLazyListState(
                onMove = { from, to ->
                    dragging = true

                    val draggedId = from.key as? Int ?: return@rememberReorderableLazyListState


                    val fromIndex = from.index
                    val list = listState.value.toMutableList()
                    if (list.isEmpty()) return@rememberReorderableLazyListState

                    val max = list.lastIndex
                    val toIndex = to.index.coerceIn(0, max)
                    if (fromIndex !in list.indices || toIndex !in list.indices) return@rememberReorderableLazyListState
                    if (fromIndex == toIndex) return@rememberReorderableLazyListState

                    val toItem = list[toIndex]

                    val moved = list.removeAt(fromIndex)
                    list.add(toIndex, moved)
                    listState.value = list

                    // ✅ parent موقت = parentِ مقصد (عمودی)
                    val targetParent = effectiveParentId(toItem) // همون fun(item)
                    pendingParentById[draggedId] = targetParent
                },
                onDragEnd = { _, _ ->
                    dragging = false

                    val draggedId = draggingKey.value ?: return@rememberReorderableLazyListState

                    val oldParent = fromParentId.value
                    val newParent = pendingParentById[draggedId]

                    viewModel.applyDragResult(
                        draggedId = draggedId,
                        oldParentId = oldParent,
                        newParentId = newParent,
                        currentList = listState.value
                    )

                    viewModel.onDragEndRestoreExpand()

                    draggingKey.value = null
                    fromParentId.value = null

                    pendingParentById.clear()
                    dragOffsetXById.clear()
                    lastReparentAtById.clear()

                }


            )

            // ✅ collapse/restore هنگام شروع/پایان درگ (مثل قبل)
            LaunchedEffect(reorderState.draggingItemKey) {
                val key = reorderState.draggingItemKey as? Int
                if (key != null) {
                    draggingKey.value = key
                    fromParentId.value =
                        listState.value.firstOrNull { it.category.categoryId == key }?.category?.parentCategoryId
                    pendingParentId.value = null
                    viewModel.onDragStartMaybeCollapse(key)
                }
            }

            val allById = remember(state.renderItems) {
                state.renderItems.associateBy { it.category.categoryId }
            }

            // کمک: دسترسی سریع به آیتم‌ها با id (از لیست واقعی uiState)
            val entityById = remember(state.categories) {
                state.categories.associateBy { it.categoryId }
            }

            // parent موثر: اگر pending داریم از آن استفاده کن
            fun effectiveParentId(id: Int): Int? {
                return pendingParentById[id] ?: entityById[id]?.parentCategoryId
            }

            // محاسبه level بر اساس parent موثر (حداکثر 4)
            fun effectiveLevel(id: Int): Int {
                var level = 1
                var curParent = effectiveParentId(id)
                var guard = 0
                while (curParent != null && curParent != -1 && guard < 10) {
                    level++
                    val next = effectiveParentId(curParent)
                    curParent = next
                    guard++
                }
                return level.coerceAtMost(4)
            }


            fun tryIndent(id: Int) {
                // child شدن: parent = آیتم قبلی بالای خودش در همون لیست که level < 4
                val list = listState.value
                val idx = list.indexOfFirst { it.category.categoryId == id }
                if (idx <= 0) return
                val prev = list[idx - 1]
                val prevId = prev.category.categoryId ?: return

                val parentLevel = effectiveLevel(prevId)
                if (parentLevel >= 4) return


                // ✅ جلوگیری از cycle: نباید زیر بچه‌ی خودش بره
                if (isDescendantOfDragged(candidateParentId = prevId, draggedId = id)) return


                if (effectiveLevel(prevId) >= 4) return
                pendingParentById[id] = prevId
            }

            fun tryOutdent(id: Int) {
                val current = allById[id] ?: return
                val currentParent = pendingParentById[id] ?: current.category.parentCategoryId
                if (currentParent == null) return

                val parentItem = allById[currentParent] ?: return
                val newParent = parentItem.category.parentCategoryId
                pendingParentById[id] = newParent
            }

            val onHorizontalReparentHint: (Int, Float) -> Unit = reparent@{ id, deltaX ->
                val acc = (dragOffsetXById[id] ?: 0f) + deltaX
                dragOffsetXById[id] = acc

                val now = android.os.SystemClock.uptimeMillis()
                val last = lastReparentAtById[id] ?: 0L
                val canStep = (now - last) >= reparentDelayMs
                if (!canStep) return@reparent

                if (acc >= threshold) {
                    dragOffsetXById[id] = 0f
                    lastReparentAtById[id] = now
                    tryIndent(id)   // +1 level
                } else if (acc <= -threshold) {
                    dragOffsetXById[id] = 0f
                    lastReparentAtById[id] = now
                    tryOutdent(id)  // -1 level
                }
            }

            val currentDraggingId = reorderState.draggingItemKey as? Int
            val isDragActive = currentDraggingId != null


            LazyColumn(
                state = reorderState.listState, // ✅ مهم
                modifier = Modifier
                    .fillMaxSize()
                    .reorderable(reorderState)
            ) {
                items(
                    items = listState.value,
                    key = { it.category.categoryId ?: it.hashCode() }
                ) { item ->
                    val id = item.category.categoryId ?: return@items

                    ReorderableItem(reorderState, key = id) { _ ->
                        androidx.compose.animation.AnimatedVisibility(
                            visible = item.isVisible,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {

                            val rowDragModifier = Modifier
                                .pointerInput(id, draggingKey.value) {
                                    awaitPointerEventScope {
                                        while (true) {
                                            val event = awaitPointerEvent()
                                            val change = event.changes.firstOrNull() ?: continue

                                            // فقط وقتی خود این آیتم در حال drag واقعی است
                                            if (draggingKey.value != id) continue
                                            if (!change.pressed) continue

                                            val dx = change.positionChange().x
                                            if (dx != 0f) onHorizontalReparentHint(id, dx)
                                        }
                                    }
                                }

                                .detectReorderAfterLongPress(reorderState) // ✅ کل ردیف draggable


                            CategoryRow2(
                                item = item,
                                computedLevel = effectiveLevel(id),
                                onToggleExpand = viewModel::toggleExpand,
                                modifier = rowDragModifier,
                                onOpenMenu = { id -> viewModel.setMenuCategoryId(id) }
                            )

                            HorizontalDivider(thickness = 0.5.dp) // ✅ خط باریک
                        }
                    }
                }

            }

        }

    }


    if (showAddCategory) {
        AddCategoryDialog2(
            draft = draft,
            parentName = state.categories.firstOrNull { it.categoryId == draft.parentId }?.name
                ?: "ریشه اصلی",
            parentIconName = parentEntity?.iconName ?: "Category",
            parentColorHex = parentEntity?.color ?: "#9E9E9E",
            onDismiss = {
                showAddCategory = false
                viewModel.resetDraft()
            },
            onPickParent = { showPickParent = true },
            onPickIcon = { showIconPicker = true },   // ✅
            onPickColor = { showColorPicker = true }, // ✅
            onNameChange = viewModel::setDraftName,
            onDescriptionChange = { desc ->
                // اگر draft.description را در VM نداری، فعلاً همینجا نگه دار یا یک setter اضافه کن
                // پیشنهاد: viewModel.setDraftDescription(desc)
            },
            onConfirm = {
                viewModel.createCategoryFromDraft()
            }

        )
    }

    if (showAddCategory && showPickParent) {
        PickParentDialogSmall(
            items = pickerFlatten.items,      // ✅ به جای state.renderItems
            levelById = pickerFlatten.levelById,
            onDismiss = { showPickParent = false },
            onPick = { parentId ->
                val ok = viewModel.trySetDraftParent(parentId)
                if (ok) showPickParent = false
            }
        )
    }


    if (showAddCategory && showIconPicker) {
        ChooseIconDialog(
            selectedKey = draft.iconName,
            onDismiss = { showIconPicker = false },
            onPick = { opt ->
                viewModel.setDraftIconName(opt.key)
                showIconPicker = false
            }
        )
    }

    if (showAddCategory && showColorPicker) {
        ChooseColorDialog(
            initialHex = draft.color,
            onDismiss = { showColorPicker = false },
            onConfirm = { hex ->
                viewModel.setDraftColor(hex)
                showColorPicker = false
            }
        )
    }

    if (menuCategory != null) {

        CategoryOptionsSideSheet(
            category = menuCategory,
            tasks = state.tasks,
            taskMiniUis = tasksWithSchedule.map { tws ->
                TaskMiniUi(
                    id = tws.task.id ?: -1,
                    title = tws.task.name,
                    isDone = tws.task.isCompleted,
                    hasSchedule = (tws.schedule != null),
                    indentLevel = tws.task.indentLevel,
                    parentTaskId = tws.task.parentTaskId
                )
            },
            onDismiss = {
                sheetMode = CategorySheetMode.OVERVIEW
                viewModel.setMenuCategoryId(null)
            },
            onClickPickIcon = { showEditIconPicker = true },
            onClickPickColor = { showEditColorPicker = true },
            onClickRename = {
                showRenameDialog = true
                renameText = menuCategory.name
                renameError = null
            },
            onClickEditDescription = {
                // یک دیالوگ برای description
            },
            onClickDelete = {
                showDeleteConfirm = true
            },
            tasksRenderList = state.taskRenderItems,
            onAddTask = {
                val cat = menuCategory ?: return@CategoryOptionsSideSheet
                viewModel.startAddTask(cat.categoryId!!, cat.color)
                showTaskDialog = true
            },
            tasksExpanded = tasksExpanded,
            onToggleTasksExpand = { tasksExpanded = !tasksExpanded },
            onClickTask = { taskId ->
                viewModel.startEditTask(taskId)
                showTaskDialog = true
            },
            scheduledCount = scheduledCount,
            sheetMode = sheetMode,
            onChangeMode = { sheetMode = it },
            toggleTaskCompleted = { taskId ,_->
                viewModel.toggleTaskCompletedCascade(taskId)
            },
            deleteTask = { taskId: Int ->
                viewModel.deleteTask(taskId)
            },
            flattenTaskTreeWithLevelsAndVisibility = { all, collapsedIds, rootParentId, maxDepth ->
                viewModel.flattenTaskTreeWithLevelsAndVisibility(
                    all,
                    collapsedIds,
                    rootParentId,
                    maxDepth
                )
            },
            findBlockRange = { list, startIndex ->
                viewModel.findBlockRange(list, startIndex)
            },
            applyTaskDragResult = { draggedId, oldParentId, newParentId, categoryId, currentList ->
                viewModel.applyTaskDragResult(
                    draggedId,
                    oldParentId,
                    newParentId,
                    categoryId,
                    currentList
                )
            },
            onDragEndRestoreExpandForTask = { viewModel.onDragEndRestoreExpandForTask() },
            onDragStartMaybeCollapseForTask = { viewModel.onDragStartMaybeCollapseForTask(it) },
            toggleExpandForTask={viewModel.toggleExpandForTask(it)}

        )
    }

    if (showDeleteConfirm && menuCategoryId != null) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete category?") },
            text = { Text("Are you sure you want to delete this category?") },
            confirmButton = {
                TextButton(onClick = {
                    val id = menuCategoryId ?: return@TextButton
                    viewModel.deleteCategoryPromoteChildren(id)
                    showDeleteConfirm = false
                    viewModel.setMenuCategoryId(null)
                }) { Text("Delete") }

            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            }
        )
    }

    if (showRenameDialog && menuCategoryId != null) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Rename") },
            text = {
                Column {
                    OutlinedTextField(
                        value = renameText,
                        onValueChange = {
                            renameText = it
                            if (renameError != null) renameError = null
                        },
                        singleLine = true,
                        label = { Text("Name") },
                        isError = renameError != null
                    )
                    if (renameError != null) {
                        Text(
                            text = renameError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val trimmed = renameText.trim()
                    if (trimmed.isBlank()) {
                        renameError = "نام نمی‌تواند خالی باشد"
                        return@TextButton
                    }
                    viewModel.renameCategory(menuCategoryId!!, trimmed)
                    showRenameDialog = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (menuCategoryId != null && showEditIconPicker) {
        val current = state.categories.firstOrNull { it.categoryId == menuCategoryId } ?: return

        ChooseIconDialog(
            selectedKey = current.iconName,
            onDismiss = { showEditIconPicker = false },
            onPick = { opt ->
                viewModel.updateCategoryIcon(menuCategoryId!!, opt.key)
                showEditIconPicker = false
            }
        )
    }

    if (menuCategoryId != null && showEditColorPicker) {
        val current = state.categories.firstOrNull { it.categoryId == menuCategoryId } ?: return

        ChooseColorDialog(
            initialHex = current.color,
            onDismiss = { showEditColorPicker = false },
            onConfirm = { hex ->
                viewModel.updateCategoryColor(menuCategoryId!!, hex)
                showEditColorPicker = false
            }
        )
    }

    if (showTaskDialog && menuCategory != null) {
        val isEdit = (editingTaskId != null)

        AddTaskDialog(
            addTaskMod = !isEdit,
            categoryName = menuCategory.name,
            categoryIconName = menuCategory.iconName,
            categoryColorHex = menuCategory.color,
            draft = taskDraft,
            onDismiss = {
                showTaskDialog = false
                viewModel.finishEditTask()
            },
            onNameChange = viewModel::setTaskName,
            onPriorityChange = viewModel::setTaskPriority,
            onCompletedToggle = viewModel::setTaskCompleted,
            onNoteChange = viewModel::setTaskNote,

            onInsertAtTopChange = viewModel::setTaskInsertAtTop,
            onChildLevelChange = viewModel::setTaskChildLevel,

            onConfirm = { action ->
                val editing = (editingTaskId != null)

                if (editing) {
                    viewModel.saveEditedTask(menuCategory.color)
                    showTaskDialog = false
                } else {
                    viewModel.createTaskForCategory(menuCategory.color)

                    if (action == ConfirmAction.SAVE_AND_CLOSE) {
                        showTaskDialog = false
                    } else {
                        viewModel.resetTaskDraftKeepSomeDefaults()
                    }
                }
            },
            onOpenSchedule = { showScheduleDialog = true }
        )


    }

    if (showScheduleDialog && menuCategory != null) {
        TaskScheduleDialog(
            taskName = taskDraft.name.ifBlank { "Task" },
            draft = scheduleDraft,
            onDismiss = { showScheduleDialog = false },
            onTitleChange = viewModel::setScheduleTitle,
            onModeChange = viewModel::setScheduleMode,
            onStartChange = viewModel::setScheduleStart,
            onEndChange = viewModel::setScheduleEnd,
            onDurationChange = viewModel::setScheduleDuration,
            onRepeatingChange = viewModel::setScheduleRepeating,
            onConfirm = {
                viewModel.markScheduleConfirmedForNewTask()
                viewModel.saveScheduleForCurrentTask()
                showScheduleDialog = false
            }
        )
    }


}

@Composable
fun AddCategoryDialog2(
    draft: CategoryDraft2,
    parentName: String,
    parentIconName: String,   // ✅ جدید
    parentColorHex: String,
    onDismiss: () -> Unit,
    onPickParent: () -> Unit,
    onPickIcon: () -> Unit,   // ✅ جدید
    onPickColor: () -> Unit,  // ✅ جدید
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit, // ✅ جدید (فعلاً اگر نداری، می‌تونی خالی پاس بدی)
    onConfirm: () -> Unit,
) {
    DimmedDialog(
        onDismiss = onDismiss,
        modifier = Modifier
            .fillMaxWidth(0.92f)
            .fillMaxHeight(0.85f)
            .background(MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.extraLarge),
        dimAlpha = 0.6f,
        dismissOnBackdropClick = true
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // --- Top bar داخل دیالوگ (مثل عکس) ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = onDismiss) { Text("Back") }
                Text(text = "New category", style = MaterialTheme.typography.titleLarge)
                TextButton(onClick = onConfirm) { Text("✓") }
            }

            HorizontalDivider()

            // --- محتوا ---
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {

                // Name*
                OutlinedTextField(
                    value = draft.name,
                    onValueChange = onNameChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Name *") },
                    singleLine = true
                )

                Spacer(Modifier.size(10.dp))

                // Parent row (مثل عکس یک ردیف ساده)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onPickParent() }
                        .padding(vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Parent", modifier = Modifier.weight(1f))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = iconFromKey(parentIconName),
                            contentDescription = parentIconName,
                            tint = colorFromHex(parentColorHex),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(parentName, style = MaterialTheme.typography.titleMedium)
                    }
                }


                HorizontalDivider()

                // Icon row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onPickIcon() }
                        .padding(vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Icon", modifier = Modifier.weight(1f))

                    Icon(
                        imageVector = iconFromKey(draft.iconName),
                        contentDescription = draft.iconName,
                        tint = colorFromHex(draft.color),   // ✅ مهم
                        modifier = Modifier.size(24.dp)
                    )

                }


                HorizontalDivider()

                // Color row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onPickColor() }
                        .padding(vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Color", modifier = Modifier.weight(1f))

                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .border(1.dp, Color.Black.copy(alpha = 0.15f), CircleShape)
                            .background(colorFromHex(draft.color), CircleShape)
                    )
                }

                HorizontalDivider()

                Spacer(Modifier.size(10.dp))

                // Description (اختیاری)
                OutlinedTextField(
                    value = draft.description,
                    onValueChange = onDescriptionChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Add description") },
                    minLines = 2
                )

                Spacer(Modifier.weight(1f))

                // (اختیاری) نمایش parentId برای دیباگ
                Text(
                    text = "ParentId: ${draft.parentId}",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}


@Composable
private fun CategoryRow2(
    item: CategoryRenderItem,
    computedLevel: Int,
    onToggleExpand: (Int) -> Unit,
    modifier: Modifier = Modifier,
    onOpenMenu: (Int) -> Unit, // ✅ جدید
) {
    val id = item.category.categoryId ?: return
    val indent = (computedLevel - 1).coerceAtLeast(0) * 16
    val bg = containerColorForLevel(computedLevel)

    ListItem(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = indent.dp),
        colors = ListItemDefaults.colors(containerColor = bg),
        leadingContent = {
            Icon(
                imageVector = iconFromKey(item.category.iconName),
                contentDescription = null,
                tint = colorFromHex(item.category.color),
                modifier = Modifier.size(30.dp)
            )
        },
        headlineContent = { Text(item.category.name) },
        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val canHaveChildren = computedLevel < 4
                if (item.hasChildren && canHaveChildren) {
                    IconButton(onClick = { onToggleExpand(id) }) {
                        Icon(
                            imageVector = if (item.isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                            contentDescription = "expand"
                        )
                    }
                }

                IconButton(onClick = { onOpenMenu(id) }) { // ✅ مستقیم SideSheet
                    Icon(Icons.Filled.MoreVert, contentDescription = "menu")
                }
            }
        }
    )
}


@Composable
private fun TaskRow(
    item: TaskRenderItem,
    computedLevel: Int,
    onToggleExpand: (Int) -> Unit,
    onClickTask: (Int) -> Unit,
    onToggleDone: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val id = item.task.id
    val indent = ((computedLevel - 1).coerceAtLeast(0) * 16).dp
    val bg = containerColorForLevel(computedLevel)

    val canHaveChildren = computedLevel < 4
    val showExpand = item.hasChildren && canHaveChildren

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp) // ✅ ارتفاع ثابت
            .background(bg)
            .clickable { onClickTask(id) }
            .padding(start = indent, end = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Spacer(Modifier.width(6.dp))

        // ✅ دایره done
        Box(
            modifier = Modifier
                .size(22.dp)
                .border(
                    2.dp,
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                    CircleShape
                )
                .background(
                    color = if (item.task.isDone)
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
                    else Color.Transparent,
                    shape = CircleShape
                )
                .clickable(
                    // ✅ مهم: کلیک دایره به کلیک ردیف نره
                    indication = null,
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                ) { onToggleDone(id) }
        )

        Spacer(Modifier.width(12.dp))

        // ✅ عنوان (یک خط، ellipsis)
        Text(
            text = item.task.title,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        // ✅ schedule icon (اگر نبود، فضا نمی‌گیرد)
        if (item.task.hasSchedule) {
            Spacer(Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Filled.Event,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.width(8.dp))

        // ✅ جایگاه expand همیشه رزرو می‌شود تا چیدمان ثابت بماند
        // اندازه‌ی IconButton معمولاً 48.dp است، پس اینجا دقیقاً همان را رزرو می‌کنیم.
        Box(
            modifier = Modifier.size(48.dp),
            contentAlignment = Alignment.Center
        ) {
            if (showExpand) {
                IconButton(
                    onClick = { onToggleExpand(id) },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = if (item.isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = "expand"
                    )
                }
            } else {
                // هیچ چیز نمایش نده، ولی فضا محفوظ است ✅
            }
        }
    }
}



@Composable
fun PickParentDialogSmall(
    items: List<CategoryRenderItem>,
    levelById: Map<Int, Int>,
    onDismiss: () -> Unit,
    onPick: (parentId: Int) -> Unit,
) {
    DimmedDialog(
        onDismiss = onDismiss,
        modifier = Modifier
            .fillMaxWidth(0.86f)
            .fillMaxHeight(0.65f) // کوچکتر از قبلی
            .background(MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.extraLarge),
        dimAlpha = 0.4f, // کمی کمتر چون روی دیالوگ دیگر میاد
        dismissOnBackdropClick = true
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = onDismiss) { Text("Close") }
                Text("انتخاب والد", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.size(48.dp)) // برای بالانس
            }

            HorizontalDivider(modifier = Modifier.padding(top = 8.dp, bottom = 8.dp))

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(items, key = { it.category.categoryId ?: it.hashCode() }) { item ->
                    val id = item.category.categoryId ?: return@items

                    // level واقعی/نمایشی (از map بهتره)
                    val level = levelById[id] ?: item.level

                    // همون قانون قبلی: والد نباید سطح 4 باشه
                    val enabled = level < 4

                    ParentPickerRow(
                        item = item,
                        computedLevel = level,
                        enabled = enabled,
                        onPick = { onPick(id) }
                    )

                    HorizontalDivider(thickness = 0.5.dp) // مثل صفحه اصلی
                }
            }

        }
    }
}


@Composable
fun ChooseIconDialog(
    selectedKey: String,
    onDismiss: () -> Unit,
    onPick: (IconOption) -> Unit,
) {
    val sections = remember { buildIconSections() }
    val expanded = remember {
        mutableStateMapOf<String, Boolean>().apply {
            sections.forEach { put(it.title, true) } // همه باز باشند مثل عکس
        }
    }

    DimmedDialog(
        onDismiss = onDismiss,
        modifier = Modifier
            .fillMaxWidth(0.92f)
            .fillMaxHeight(0.80f)
            .background(MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.extraLarge),
        dimAlpha = 0.6f,
        dismissOnBackdropClick = true
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            Text(
                text = "Choose icon",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(16.dp)
            )

            HorizontalDivider()

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                items(sections) { section ->
                    SectionHeaderRow(
                        title = section.title,
                        isExpanded = expanded[section.title] == true,
                        onToggle = { expanded[section.title] = !(expanded[section.title] == true) }
                    )

                    if (expanded[section.title] == true) {
                        IconGrid4(
                            options = section.options,
                            selectedKey = selectedKey,
                            onPick = onPick
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) { Text("CANCEL") }
            }
        }
    }
}

@Composable
private fun SectionHeaderRow(
    title: String,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onToggle) {
            Icon(
                imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                contentDescription = "toggle"
            )
        }
    }
}

@Composable
private fun IconGrid4(
    options: List<IconOption>,
    selectedKey: String,
    onPick: (IconOption) -> Unit,
) {
    // ساده و سبک: ۴تایی در هر ردیف، بدون LazyVerticalGrid
    val rows = (options.size + 3) / 4
    Column(modifier = Modifier.fillMaxWidth()) {
        repeat(rows) { r ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (c in 0 until 4) {
                    val idx = r * 4 + c
                    if (idx < options.size) {
                        val opt = options[idx]
                        val selected = opt.key == selectedKey

                        IconButton(
                            onClick = { onPick(opt) },
                            modifier = Modifier
                                .size(56.dp)
                                .then(
                                    if (selected) Modifier.border(
                                        width = 2.dp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        shape = MaterialTheme.shapes.small
                                    ) else Modifier
                                )
                        ) {
                            Icon(
                                imageVector = opt.icon,
                                contentDescription = opt.key,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.size(56.dp))
                    }
                }
            }
        }
    }
}


@Composable
fun ChooseColorDialog(
    initialHex: String,
    onDismiss: () -> Unit,
    onConfirm: (hex: String) -> Unit
) {
    val options = remember { buildColorOptions() }
    var selectedHex by remember { mutableStateOf(initialHex.ifBlank { options.first().hex }) }

    DimmedDialog(
        onDismiss = onDismiss,
        modifier = Modifier
            .fillMaxWidth(0.92f)
            .fillMaxHeight(0.62f)
            .background(MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.extraLarge),
        dimAlpha = 0.6f,
        dismissOnBackdropClick = true
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            Text(
                text = "Choose color",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(16.dp)
            )

            HorizontalDivider()

            Spacer(Modifier.height(18.dp))

            // Grid ساده: 6 ستون * چند ردیف
            val cols = 6
            val rows = (options.size + cols - 1) / cols

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                repeat(rows) { r ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (c in 0 until cols) {
                            val idx = r * cols + c
                            if (idx < options.size) {
                                val opt = options[idx]
                                val selected = opt.hex.equals(selectedHex, ignoreCase = true)

                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .border(
                                            width = if (selected) 4.dp else 0.dp,
                                            color = if (selected) Color.Black else Color.Transparent,
                                            shape = CircleShape
                                        )
                                        .padding(if (selected) 4.dp else 0.dp)
                                        .border(
                                            width = 1.dp,
                                            color = Color.Black.copy(alpha = 0.08f),
                                            shape = CircleShape
                                        )
                                        .clickable { selectedHex = opt.hex },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .border(0.dp, Color.Transparent, CircleShape)
                                            .background(opt.color, CircleShape)
                                    )
                                }
                            } else {
                                Spacer(Modifier.size(42.dp))
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) { Text("CANCEL") }
                Spacer(Modifier.width(8.dp))
                TextButton(onClick = { onConfirm(selectedHex) }) { Text("OK") }
            }
        }
    }
}


@Composable
private fun ParentPickerRow(
    item: CategoryRenderItem,
    computedLevel: Int,
    enabled: Boolean,
    onPick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val indent = (computedLevel - 1).coerceAtLeast(0) * 16
    val bg = containerColorForLevel(computedLevel)

    ListItem(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = indent.dp)
            .then(if (enabled) Modifier.clickable { onPick() } else Modifier),
        colors = ListItemDefaults.colors(
            containerColor = bg
        ),
        leadingContent = {
            Icon(
                imageVector = iconFromKey(item.category.iconName),
                contentDescription = item.category.iconName,
                tint = if (enabled) colorFromHex(item.category.color)
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f),
                modifier = Modifier.size(28.dp)
            )
        },
        headlineContent = {
            Text(
                text = item.category.name,
                color = if (enabled) LocalContentColor.current
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
            )
        },
        supportingContent = {
            if (!enabled) {
                Text(
                    text = "حداکثر عمق",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    )
}


@Composable
private fun containerColorForLevel(level: Int): Color {
    val e = when (level.coerceIn(1, 4)) {
        1 -> 0.dp
        2 -> 2.dp
        3 -> 6.dp
        else -> 12.dp
    }
    return MaterialTheme.colorScheme.surfaceColorAtElevation(e)
}


@Composable
private fun CategoryOptionsSideSheet(
    category: CategoryEntity,
    tasks: List<Task>,
    taskMiniUis: List<TaskMiniUi>,
    tasksRenderList: List<TaskRenderItem>,
    onDismiss: () -> Unit,
    onClickPickIcon: () -> Unit,
    onClickPickColor: () -> Unit,
    onClickRename: () -> Unit,
    onClickEditDescription: () -> Unit,
    onClickDelete: () -> Unit,
    onAddTask: () -> Unit,
    tasksExpanded: Boolean,
    onToggleTasksExpand: () -> Unit,
    onClickTask: (Int) -> Unit,
    scheduledCount: Int,
    sheetMode: CategorySheetMode,
    onChangeMode: (CategorySheetMode) -> Unit,
    toggleTaskCompleted: (taskId: Int, done: Boolean) -> Unit,
    deleteTask: (taskId: Int) -> Unit,
    flattenTaskTreeWithLevelsAndVisibility: (
        all: List<TaskMiniUi>,
        collapsedIds: Set<Int>,
        rootParentId: Int?,
        maxDepth: Int
    ) -> List<TaskRenderItem>,
    findBlockRange: (list: List<TaskMiniUi>, startIndex: Int) -> IntRange,
    applyTaskDragResult: (
        draggedId: Int,
        oldParentId: Int?,
        newParentId: Int?,
        categoryId: Int,
        currentList: List<TaskRenderItem>
    ) -> Unit,
    onDragEndRestoreExpandForTask: () -> Unit,
    onDragStartMaybeCollapseForTask: (taskId: Int) -> Unit,
    toggleExpandForTask : (taskId: Int) -> Unit,
) {


    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(100f)
    ) {
        // ✅ Backdrop کلیکی برای dismiss
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.45f))
                .clickable(
                    indication = null,
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                ) { onDismiss() }
        )
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
                .fillMaxWidth(0.78f)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            when (sheetMode) {
                CategorySheetMode.OVERVIEW -> {
                    // ✅ هدر فعلی کتگوری
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(colorFromHex(category.color).copy(alpha = 0.15f))
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = iconFromKey(category.iconName),
                            contentDescription = null,
                            tint = colorFromHex(category.color),
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = category.name,
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    HorizontalDivider()

                    // ✅ بدنه اسکرول‌دار (Overview)
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        item {
                            // ✅ ردیف Tasks ساده (فقط یک ردیف، بدون expand/منو/لیست)
                            SheetTasksCompactRow(
                                count = tasks.size,
                                onClick = { onChangeMode(CategorySheetMode.TASKS) }
                            )
                            HorizontalDivider(thickness = 0.5.dp)

                            // آمارها مثل قبل (اگر می‌خوای نگه داریم)
                            SheetStatRow(
                                icon = Icons.Filled.Event,
                                text = "${scheduledCount} scheduled activities"
                            )
                            SheetStatRow(icon = Icons.Filled.History, text = "0 logged activities")
                            SheetStatRow(icon = Icons.Filled.Description, text = "0 notes")
                            SheetStatRow(icon = Icons.Filled.AttachFile, text = "0 attachments")

                            Spacer(Modifier.height(10.dp))
                            HorizontalDivider()
                        }

                        item {
                            // ✅ Parameters همون قبلی (بدون تغییر)
                            var paramsExpanded by rememberSaveable { mutableStateOf(true) }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { paramsExpanded = !paramsExpanded }
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.Settings, contentDescription = null)
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    "Parameters",
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    imageVector = if (paramsExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                    contentDescription = null
                                )
                            }

                            if (paramsExpanded) {
                                HorizontalDivider()

                                SheetActionRow(
                                    leading = {
                                        Icon(
                                            iconFromKey(category.iconName),
                                            contentDescription = null,
                                            tint = colorFromHex(category.color)
                                        )
                                    },
                                    title = "Icon",
                                    trailing = {
                                        Icon(
                                            Icons.Filled.GridView,
                                            contentDescription = null
                                        )
                                    },
                                    onClick = onClickPickIcon
                                )

                                SheetActionRow(
                                    leading = {
                                        Box(
                                            Modifier
                                                .size(22.dp)
                                                .border(
                                                    1.dp,
                                                    Color.Black.copy(alpha = 0.12f),
                                                    CircleShape
                                                )
                                                .background(
                                                    colorFromHex(category.color),
                                                    CircleShape
                                                )
                                        )
                                    },
                                    title = "Color",
                                    trailing = {
                                        Icon(
                                            Icons.Filled.Palette,
                                            contentDescription = null
                                        )
                                    },
                                    onClick = onClickPickColor
                                )

                                SheetActionRow(
                                    leading = {
                                        Icon(
                                            Icons.Filled.Edit,
                                            contentDescription = null
                                        )
                                    },
                                    title = "Rename",
                                    onClick = onClickRename
                                )

                                SheetActionRow(
                                    leading = {
                                        Icon(
                                            Icons.Filled.Description,
                                            contentDescription = null
                                        )
                                    },
                                    title = "Add description",
                                    onClick = onClickEditDescription
                                )

                                SheetActionRow(
                                    leading = {
                                        Icon(
                                            Icons.Filled.Delete,
                                            contentDescription = null
                                        )
                                    },
                                    title = "Delete",
                                    titleColor = MaterialTheme.colorScheme.error,
                                    onClick = onClickDelete
                                )
                            }

                            Spacer(Modifier.height(24.dp))
                        }
                    }
                }

                CategorySheetMode.TASKS -> {
                    // ✅ حالت Tasks: کل سایدشیت عوض میشه
                    TasksModeContent2(
                        category = category,
                        tasks = tasks,
                        taskMiniList = taskMiniUis,
                        tasksRenderList = tasksRenderList,
                        onBack = { onChangeMode(CategorySheetMode.OVERVIEW) },
                        onAddTask = onAddTask,
                        onClickTask = onClickTask,
                        toggleTaskCompleted = { taskId: Int, done: Boolean ->
                            toggleTaskCompleted(taskId, done)
                        },
                        deleteTask = { taskId: Int ->
                            deleteTask(taskId)
                        },
                        flattenTaskTreeWithLevelsAndVisibility = { all, collapsedIds, rootParentId, maxDepth ->
                            flattenTaskTreeWithLevelsAndVisibility(
                                all,
                                collapsedIds,
                                rootParentId,
                                maxDepth
                            )
                        },
                        findBlockRange = { list, startIndex ->
                            findBlockRange(list, startIndex)
                        },
                        applyTaskDragResult = { draggedId, oldParentId, newParentId, categoryId, currentList ->
                            applyTaskDragResult(
                                draggedId,
                                oldParentId,
                                newParentId,
                                categoryId,
                                currentList
                            )
                        },
                        onDragEndRestoreExpandForTask = { onDragEndRestoreExpandForTask() },
                        onDragStartMaybeCollapseForTask = { onDragStartMaybeCollapseForTask(it) },
                        toggleExpandForTask={toggleExpandForTask(it)}
                    )
                }
            }
        }
    }
}


@Composable
private fun SheetTasksCompactRow(
    count: Int,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Filled.Task,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.width(12.dp))

        // "Tasks" چسبیده به تعداد
        Text("Tasks", style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.width(8.dp))
        Text("$count", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    HorizontalDivider(thickness = 0.5.dp)
}

@Composable
private fun TasksModeContent2(
    category: CategoryEntity,
    tasks: List<Task>,
    taskMiniList: List<TaskMiniUi>,
    tasksRenderList: List<TaskRenderItem>,
    onBack: () -> Unit,
    onAddTask: () -> Unit,
    onClickTask: (Int) -> Unit,
    toggleTaskCompleted: (taskId: Int, done: Boolean) -> Unit,
    deleteTask: (taskId: Int) -> Unit,
    flattenTaskTreeWithLevelsAndVisibility: (
        all: List<TaskMiniUi>,
        collapsedIds: Set<Int>,
        rootParentId: Int?,
        maxDepth: Int
    ) -> List<TaskRenderItem>,
    findBlockRange: (list: List<TaskMiniUi>, startIndex: Int) -> IntRange,
    applyTaskDragResult: (
        draggedId: Int,
        oldParentId: Int?,
        newParentId: Int?,
        categoryId: Int,
        currentList: List<TaskRenderItem>
    ) -> Unit,
    onDragEndRestoreExpandForTask: () -> Unit,
    onDragStartMaybeCollapseForTask: (taskId: Int) -> Unit,
    toggleExpandForTask : (taskId: Int) -> Unit,
) {
    var dragging by remember { mutableStateOf(false) }
    val listState = remember { mutableStateOf(tasksRenderList) }
    Log.i("TEST", "listState=$listState")

    // parent قبلی و parent موقت جدید
    val fromParentId = remember { mutableStateOf<Int?>(null) }
    val pendingParentId = remember { mutableStateOf<Int?>(null) }

    val pendingParentById =
        remember { mutableStateMapOf<Int, Int?>() } // draggedId -> newParentId
    val dragOffsetXById =
        remember { mutableStateMapOf<Int, Float>() }  // draggedId -> accumulatedX
    val threshold = 70f

    val lastReparentAtById = remember { mutableStateMapOf<Int, Long>() }
    val reparentDelayMs = 200L

    val childrenByParent = remember(taskMiniList) {
        taskMiniList
            .filter { true }
            .groupBy { it.parentTaskId }
            .mapValues { entry -> entry.value.map { it.id } }
    }
    Log.i("TEST", "childrenByParent=$childrenByParent")

    fun isDescendantOfDragged(candidateParentId: Int, draggedId: Int): Boolean {
        // آیا candidateParentId داخل زیر درخت draggedId است؟
        val stack = ArrayDeque<Int>()
        stack.add(draggedId)

        while (stack.isNotEmpty()) {
            val cur = stack.removeLast()
            val children = childrenByParent[cur] ?: emptyList()
            if (candidateParentId in children) return true
            children.forEach { stack.add(it) }
        }
        return false
    }

    fun effectiveParentId(item: TaskRenderItem): Int? {
        val id = item.task.id
        return pendingParentById[id] ?: item.task.parentTaskId
    }

    // وقتی درگ نداریم، با state.sync شو
    LaunchedEffect(tasksRenderList) {
        if (!dragging) listState.value = tasksRenderList
    }

    val draggingKey = remember { mutableStateOf<Int?>(null) }


    val reorderState = rememberReorderableLazyListState(
        onMove = { from, to ->
            dragging = true

            val draggedId = from.key as? Int ?: return@rememberReorderableLazyListState


            val fromIndex = from.index
            val list = listState.value.toMutableList()
            if (list.isEmpty()) return@rememberReorderableLazyListState

            val max = list.lastIndex
            val toIndex = to.index.coerceIn(0, max)
            if (fromIndex !in list.indices || toIndex !in list.indices) return@rememberReorderableLazyListState
            if (fromIndex == toIndex) return@rememberReorderableLazyListState

            val toItem = list[toIndex]

            val moved = list.removeAt(fromIndex)
            list.add(toIndex, moved)
            listState.value = list

            // ✅ parent موقت = parentِ مقصد (عمودی)
            val targetParent = effectiveParentId(toItem) // همون fun(item)
            pendingParentById[draggedId] = targetParent
        },
        onDragEnd = { _, _ ->
            dragging = false

            val draggedId = draggingKey.value ?: return@rememberReorderableLazyListState

            val oldParent = fromParentId.value
            val newParent = pendingParentById[draggedId]
            val categoryId = category.categoryId ?: 1

            applyTaskDragResult(draggedId, oldParent, newParent, categoryId, listState.value)

            onDragEndRestoreExpandForTask()

            draggingKey.value = null
            fromParentId.value = null

            pendingParentById.clear()
            dragOffsetXById.clear()
            lastReparentAtById.clear()

        }
    )

    // ✅ collapse/restore هنگام شروع/پایان درگ (مثل قبل)
    LaunchedEffect(reorderState.draggingItemKey) {
        val key = reorderState.draggingItemKey as? Int
        if (key != null) {
            draggingKey.value = key
            fromParentId.value =
                listState.value.firstOrNull { it.task.id == key }?.task?.parentTaskId
            pendingParentId.value = null
            onDragStartMaybeCollapseForTask(key)
        }
    }

    val allById = remember(tasksRenderList) {
        tasksRenderList.associateBy { it.task.id }
    }

    // کمک: دسترسی سریع به آیتم‌ها با id (از لیست واقعی uiState)
    val entityById = remember(tasks) {
        tasks.associateBy { it.id }
    }

    // parent موثر: اگر pending داریم از آن استفاده کن
    fun effectiveParentId(id: Int): Int? {
        return pendingParentById[id] ?: entityById[id]?.parentTaskId
    }

    // محاسبه level بر اساس parent موثر (حداکثر 4)
    fun effectiveLevel(id: Int): Int {
        var level = 1
        var curParent = effectiveParentId(id)
        var guard = 0
        while (curParent != null && curParent != -1 && guard < 10) {
            level++
            val next = effectiveParentId(curParent)
            curParent = next
            guard++
        }
        return level.coerceAtMost(4)
    }

    fun tryIndent(id: Int) {
        // child شدن: parent = آیتم قبلی بالای خودش در همون لیست که level < 4
        val list = listState.value
        val idx = list.indexOfFirst { it.task.id == id }
        if (idx <= 0) return
        val prev = list[idx - 1]
        val prevId = prev.task.id

        val parentLevel = effectiveLevel(prevId)
        if (parentLevel >= 4) return


        // ✅ جلوگیری از cycle: نباید زیر بچه‌ی خودش بره
        if (isDescendantOfDragged(candidateParentId = prevId, draggedId = id)) return


        if (effectiveLevel(prevId) >= 4) return
        pendingParentById[id] = prevId
    }

    fun tryOutdent(id: Int) {
        val current = allById[id] ?: return

        val currentParent = pendingParentById[id] ?: current.task.parentTaskId
        // اگر همین الان ریشه است، دیگه چیزی برای outdent نداریم
        if (currentParent == null || currentParent == -1) return

        val parentItem = allById[currentParent] ?: return
        val newParent = parentItem.task.parentTaskId

        // ✅ ریشه را با -1 نگه دار (نه null)
        pendingParentById[id] = newParent ?: -1
    }


    val onHorizontalReparentHint: (Int, Float) -> Unit = reparent@{ id, deltaX ->
        val acc = (dragOffsetXById[id] ?: 0f) + deltaX
        dragOffsetXById[id] = acc

        val now = android.os.SystemClock.uptimeMillis()
        val last = lastReparentAtById[id] ?: 0L
        val canStep = (now - last) >= reparentDelayMs
        if (!canStep) return@reparent

        if (acc >= threshold) {
            dragOffsetXById[id] = 0f
            lastReparentAtById[id] = now
            tryIndent(id)   // +1 level
        } else if (acc <= -threshold) {
            dragOffsetXById[id] = 0f
            lastReparentAtById[id] = now
            tryOutdent(id)  // -1 level
        }
    }

    val currentDraggingId = reorderState.draggingItemKey as? Int
    val isDragActive = currentDraggingId != null
    var expandedForAll by rememberSaveable { mutableStateOf(true) }





    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {

        // ردیف اول: Add task + آیکون کتگوری + Search
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBack) { Text("Back") }

            Spacer(Modifier.width(8.dp))

            Icon(
                imageVector = iconFromKey(category.iconName),
                contentDescription = null,
                tint = colorFromHex(category.color),
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(10.dp))

            Text(
                text = "Add task",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .weight(1f)
                    .clickable { onAddTask() }
            )

            IconButton(onClick = { /* بعداً سرچ */ }) {
                Icon(Icons.Filled.Search, contentDescription = "search")
            }
        }

        HorizontalDivider()

        // ردیف دوم: آیکون تسک، تعداد، Tasks، اکسپند، سه نقطه
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.Task,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.width(10.dp))

            Text("${tasks.size}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.width(10.dp))

            Text("Tasks", modifier = Modifier.weight(1f))

            IconButton(onClick = { expandedForAll = !expandedForAll }) {
                Icon(
                    imageVector = if (expandedForAll) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = "expand"
                )
            }

            IconButton(onClick = { /* منوی سه نقطه برای tasks: بعداً */ }) {
                Icon(Icons.Filled.MoreVert, contentDescription = "tasks menu")
            }
        }
        HorizontalDivider(thickness = 0.5.dp)

        LazyColumn(
            state = reorderState.listState, // ✅ مهم
            modifier = Modifier
                .fillMaxSize()
                .reorderable(reorderState)
        ) {
            items(
                items = listState.value,
                key = { it.task.id }
            ) { item ->
                val id = item.task.id

                ReorderableItem(reorderState, key = id) { _ ->
                    androidx.compose.animation.AnimatedVisibility(
                        visible = item.isVisible,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {

                        val rowDragModifier = Modifier
                            .pointerInput(id, draggingKey.value) {
                                awaitPointerEventScope {
                                    while (true) {
                                        val event = awaitPointerEvent()
                                        val change = event.changes.firstOrNull() ?: continue

                                        // فقط وقتی خود این آیتم در حال drag واقعی است
                                        if (draggingKey.value != id) continue
                                        if (!change.pressed) continue

                                        val dx = change.positionChange().x
                                        if (dx != 0f) onHorizontalReparentHint(id, dx)
                                    }
                                }
                            }

                            .detectReorderAfterLongPress(reorderState) // ✅ کل ردیف draggable


                        TaskRow(
                            item = item,
                            computedLevel = effectiveLevel(id),
                            onToggleExpand = { toggleExpandForTask(it) },
                            onClickTask={onClickTask(it)},
                            onToggleDone = { taskId ->
                                val cur = listState.value.firstOrNull { it.task.id == taskId }?.task ?: return@TaskRow
                                toggleTaskCompleted(taskId, !cur.isDone)  // ✅ معکوس
                            },
                            modifier = rowDragModifier,
                        )

                        HorizontalDivider(thickness = 0.5.dp) // ✅ خط باریک



                    }
                }
            }
        }
    }
}


@Composable
private fun TasksModeContent(
    category: CategoryEntity,
    tasks: List<TaskMiniUi>,
    onBack: () -> Unit,
    onAddTask: () -> Unit,
    onClickTask: (Int) -> Unit,
    toggleTaskCompleted: (taskId: Int, done: Boolean) -> Unit,
    deleteTask: (taskId: Int) -> Unit,
    flattenTaskTreeWithLevelsAndVisibility: (
        all: List<TaskMiniUi>,
        collapsedIds: Set<Int>,
        rootParentId: Int?,
        maxDepth: Int
    ) -> List<TaskRenderItem>,
    findBlockRange: (list: List<TaskMiniUi>, startIndex: Int) -> IntRange,
    applyTaskDragResult: (categoryId: Int, finalFlatList: List<TaskMiniUi>) -> Unit,
) {


    var expanded by rememberSaveable { mutableStateOf(true) }

    var collapsedIds by rememberSaveable { mutableStateOf(setOf<Int>()) }
    // برای collapse موقت هنگام drag
    val dragCollapsedRestore = remember { mutableStateOf<Int?>(null) }
    val pendingIndentById = remember { mutableStateMapOf<Int, Int>() } // id -> indentLevel(0..3)

    var taskMenuForId by rememberSaveable { mutableStateOf<Int?>(null) }
    var expandedIds by rememberSaveable { mutableStateOf(setOf<Int>()) } // برای expand تسک‌های دارای child

    // ✅ لیست لوکال برای UI (مثل کتگوری‌ها)
    var dragging by remember { mutableStateOf(false) }
    val listState = remember(tasks) { mutableStateOf(tasks) }

    // برای parent موقت و تغییر level
    val pendingParentById = remember { mutableStateMapOf<Int, Int?>() }
    val dragOffsetXById = remember { mutableStateMapOf<Int, Float>() }
    val lastReparentAtById = remember { mutableStateMapOf<Int, Long>() }

    val threshold = 70f
    val reparentDelayMs = 200L

    // برای جلوگیری از cycle
    val childrenByParent = remember(tasks) {
        tasks.groupBy { it.parentTaskId }
            .mapValues { it.value.map { it.id } }
    }

    fun isDescendant(candidateParentId: Int, draggedId: Int): Boolean {
        val stack = ArrayDeque<Int>()
        stack.add(draggedId)
        while (stack.isNotEmpty()) {
            val cur = stack.removeLast()
            val children = childrenByParent[cur] ?: emptyList()
            if (candidateParentId in children) return true
            children.forEach { stack.add(it) }
        }
        return false
    }

    // parent موثر (اگر pending داریم از آن استفاده کن)
    fun effectiveParentId(id: Int): Int? =
        pendingParentById[id] ?: listState.value.firstOrNull { it.id == id }?.parentTaskId

    // level موثر: root=1 ... max=4
    fun effectiveLevel(id: Int): Int {
        var level = 1
        var curParent = effectiveParentId(id)
        var guard = 0
        while (curParent != null && guard < 10) {
            level++
            curParent = effectiveParentId(curParent)
            guard++
        }
        return level.coerceAtMost(4)
    }


    fun updatePendingIndentFor(id: Int) {
        val lvl = effectiveLevel(id)           // 1..4
        pendingIndentById[id] = (lvl - 1).coerceIn(0, 3)
    }

    fun tryIndent(id: Int) {
        val list = listState.value
        val idx = list.indexOfFirst { it.id == id }
        if (idx <= 0) return
        val prevId = list[idx - 1].id

        val parentLevel = effectiveLevel(prevId)
        if (parentLevel >= 4) return
        if (isDescendant(candidateParentId = prevId, draggedId = id)) return

        pendingParentById[id] = prevId
        updatePendingIndentFor(id)
        pendingIndentById[id] = (effectiveLevel(id) - 1).coerceIn(0, 3)

    }

    fun tryOutdent(id: Int) {
        val currentParent = effectiveParentId(id)

        // ✅ اگر همین الان ریشه است، مطمئن شو indent=0 شود
        if (currentParent == null) {
            pendingParentById[id] = null
            pendingIndentById[id] = 0
            return
        }

        val newParent = effectiveParentId(currentParent) // ممکنه null بشه => سطح 0
        pendingParentById[id] = newParent

        // ✅ indent را دقیق و قطعی کم کن
        val currentIndent = pendingIndentById[id] ?: (effectiveLevel(id) - 1).coerceIn(0, 3)
        val newIndent = (currentIndent - 1).coerceAtLeast(0)
        pendingIndentById[id] = newIndent
    }


    fun findBlockRangeUi(list: List<TaskMiniUi>, startIndex: Int): IntRange {
        val baseId = list[startIndex].id
        val baseIndent = pendingIndentById[baseId] ?: list[startIndex].indentLevel

        var end = startIndex
        for (i in startIndex + 1..list.lastIndex) {
            val curId = list[i].id
            val curIndent = pendingIndentById[curId] ?: list[i].indentLevel
            if (curIndent <= baseIndent) break
            end = i
        }
        return startIndex..end
    }


    val onHorizontalReparentHint: (Int, Float) -> Unit = let@{ id, deltaX ->
        val acc = (dragOffsetXById[id] ?: 0f) + deltaX
        dragOffsetXById[id] = acc

        val now = android.os.SystemClock.uptimeMillis()
        val last = lastReparentAtById[id] ?: 0L
        if ((now - last) < reparentDelayMs) return@let

        if (acc >= threshold) {
            dragOffsetXById[id] = 0f
            lastReparentAtById[id] = now
            tryIndent(id)
        } else if (acc <= -threshold) {
            dragOffsetXById[id] = 0f
            lastReparentAtById[id] = now
            tryOutdent(id)
        }
    }

    val draggingKey = remember { mutableStateOf<Int?>(null) }
    val fromParentId = remember { mutableStateOf<Int?>(null) }

    val draggedBlockIds = remember { mutableStateListOf<Int>() }
    val draggedIdState = remember { mutableStateOf<Int?>(null) }


    val reorderState = rememberReorderableLazyListState(
        onMove = { from, to ->
            dragging = true
            val list = listState.value.toMutableList()
            if (list.isEmpty()) return@rememberReorderableLazyListState

            val fromIndex = from.index
            val toIndex = to.index.coerceIn(0, list.lastIndex)
            if (fromIndex !in list.indices || toIndex !in list.indices) return@rememberReorderableLazyListState
            if (fromIndex == toIndex) return@rememberReorderableLazyListState

            val moved = list.removeAt(fromIndex)
            list.add(toIndex, moved)
            listState.value = list
        },
        onDragEnd = { _, _ ->
            dragging = false
            val draggedId = draggedIdState.value
            val block = draggedBlockIds.toList()
            draggedIdState.value = null
            draggedBlockIds.clear()

            if (draggedId != null && block.isNotEmpty()) {
                val list = listState.value.toMutableList()

                // مقصد: جایی که خود dragged الان در لیست قرار گرفته (بعد از onMove های حین drag)
                val targetIndex = list.indexOfFirst { it.id == draggedId }.coerceAtLeast(0)

                // کل بلاک را از لیست حذف کن (هر جا که باشند)
                val remaining = list.filterNot { it.id in block }.toMutableList()

                // اندیس درج: اگر dragged قبل از حذف داخل بلاک بوده، targetIndex مربوط به لیست قبل است
                // ساده و پایدار: درج در همان targetIndex با clamp
                val insertAt = targetIndex.coerceIn(0, remaining.size)

                // بلاک را با ترتیب اولیه‌اش درج کن
                val blockItemsInOrder = block.mapNotNull { id -> list.firstOrNull { it.id == id } }
                remaining.addAll(insertAt, blockItemsInOrder)

                listState.value = remaining
            }

            val finalForDb = listState.value.map { t ->
                val p = pendingParentById[t.id] ?: t.parentTaskId
                val ind = pendingIndentById[t.id] ?: t.indentLevel
                t.copy(parentTaskId = p, indentLevel = ind)
            }

            applyTaskDragResult(category.categoryId!!, finalForDb)

            // پاکسازی pending ها
            pendingParentById.clear()
            pendingIndentById.clear()
            dragOffsetXById.clear()
            lastReparentAtById.clear()

            dragCollapsedRestore.value?.let { id -> collapsedIds = collapsedIds - id }
            dragCollapsedRestore.value = null
        }

    )

    val isDragActive = reorderState.draggingItemKey != null
    val collapsedForUi = if (isDragActive) emptySet<Int>() else collapsedIds


    fun hasChildren(id: Int): Boolean {
        // بر اساس ساختار فعلی UI (با pending هم سازگار)
        return listState.value.any { effectiveParentId(it.id) == id }
    }

    fun isHiddenByCollapsedAncestors(id: Int): Boolean {
        var p = effectiveParentId(id)
        var guard = 0
        while (p != null && guard < 50) {
            if (collapsedForUi.contains(p)) return true
            p = effectiveParentId(p)
            guard++
        }
        return false
    }


    // وقتی شروع شد: parent قبلی را نگه دار
    LaunchedEffect(reorderState.draggingItemKey) {
        val key = reorderState.draggingItemKey as? Int

        if (key != null) {
            draggedIdState.value = key

            // snapshot block ids (بر اساس indent فعلی لیست)
            val list = listState.value
            val fromIndex = list.indexOfFirst { it.id == key }
            if (fromIndex != -1) {
                val range = findBlockRangeUi(list, fromIndex) // همون تابعی که داری
                draggedBlockIds.clear()
                draggedBlockIds.addAll(list.subList(range.first, range.last + 1).map { it.id })
            }



            draggingKey.value = key
            fromParentId.value = listState.value.firstOrNull { it.id == key }?.parentTaskId

            // ✅ مثل کتگوری: اگر بچه دارد و الان باز است => موقع drag جمعش کن
            if (hasChildren(key) && !collapsedIds.contains(key)) {
                dragCollapsedRestore.value = key
                collapsedIds = collapsedIds + key
            }
        }
    }


    // sync با دیتای جدید وقتی درگ نداریم
    LaunchedEffect(tasks) {
        if (!dragging) listState.value = tasks
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {

        // ردیف اول: Add task + آیکون کتگوری + Search
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBack) { Text("Back") }

            Spacer(Modifier.width(8.dp))

            Icon(
                imageVector = iconFromKey(category.iconName),
                contentDescription = null,
                tint = colorFromHex(category.color),
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(10.dp))

            Text(
                text = "Add task",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .weight(1f)
                    .clickable { onAddTask() }
            )

            IconButton(onClick = { /* بعداً سرچ */ }) {
                Icon(Icons.Filled.Search, contentDescription = "search")
            }
        }

        HorizontalDivider()

        // ردیف دوم: آیکون تسک، تعداد، Tasks، اکسپند، سه نقطه
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.Task,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.width(10.dp))

            Text("${tasks.size}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.width(10.dp))

            Text("Tasks", modifier = Modifier.weight(1f))

            IconButton(onClick = { expanded = !expanded }) {
                Icon(
                    imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = "expand"
                )
            }

            IconButton(onClick = { /* منوی سه نقطه برای tasks: بعداً */ }) {
                Icon(Icons.Filled.MoreVert, contentDescription = "tasks menu")
            }
        }

        HorizontalDivider(thickness = 0.5.dp)


        val renderItems = remember(tasks, collapsedIds) {
            flattenTaskTreeWithLevelsAndVisibility(tasks, collapsedIds, null, 4)
        }

        LazyColumn(
            state = reorderState.listState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .reorderable(reorderState)
        ) {
            if (expanded) {
                items(
                    items = listState.value,
                    key = { it.id }
                ) { t ->
                    val id = t.id

                    ReorderableItem(reorderState, key = id) { _ ->
                        val rowDragModifier = Modifier
                            .pointerInput(id, draggingKey.value) {
                                awaitPointerEventScope {
                                    var active = true
                                    while (active) {
                                        val event = awaitPointerEvent()
                                        val change = event.changes.firstOrNull() ?: continue

                                        // فقط وقتی همین آیتم واقعاً drag شده
                                        if (draggingKey.value != id) continue

                                        if (!change.pressed) {
                                            active = false
                                            continue
                                        }

                                        val dx = change.positionChange().x
                                        if (dx != 0f) onHorizontalReparentHint(id, dx)
                                    }
                                }
                            }

                            .detectReorderAfterLongPress(reorderState)

                        // ✅ level نمایشی بر اساس parent موثر
                        val level = effectiveLevel(id)
                        val indent = (level - 1).coerceAtLeast(0) * 16
                        val bg = containerColorForLevel(level)

                        val canShowExpand = hasChildren(id) && effectiveLevel(id) < 4
                        val isExpanded = !collapsedForUi.contains(id)


                        val isDragActive = reorderState.draggingItemKey != null
                        val hidden = if (isDragActive) false else isHiddenByCollapsedAncestors(id)

                        androidx.compose.animation.AnimatedVisibility(
                            visible = !hidden,
                            enter = EnterTransition.None,
                            exit = ExitTransition.None
                        ) {
                            Row(
                                modifier = rowDragModifier
                                    .fillMaxWidth()
                                    .background(bg)
                                    .padding(start = indent.dp)
                                    .clickable { onClickTask(id) }
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // دایره done
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .border(
                                            2.dp,
                                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                                            CircleShape
                                        )
                                        .background(
                                            color = if (t.isDone) MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                                alpha = 0.25f
                                            )
                                            else Color.Transparent,
                                            shape = CircleShape
                                        )
                                )
                                Spacer(Modifier.width(12.dp))

                                Text(t.title, modifier = Modifier.weight(1f))

                                if (t.hasSchedule) {
                                    Spacer(Modifier.width(8.dp))
                                    Icon(
                                        Icons.Filled.Event,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                if (canShowExpand) {
                                    IconButton(
                                        onClick = {
                                            collapsedIds =
                                                if (isExpanded) collapsedIds + id
                                                else collapsedIds - id
                                        }
                                    ) {
                                        Icon(
                                            imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                            contentDescription = "expand"
                                        )
                                    }
                                }
                            }

                            HorizontalDivider(thickness = 0.5.dp)
                        }
                    }
                }
            }

        }
    }
}


@Composable
private fun TaskRowInSheet(
    task: TaskMiniUi,
    expanded: Boolean,
    onToggleExpand: () -> Unit,
    onClickRow: () -> Unit,
    onDismissMenu: () -> Unit,
    showMenu: Boolean,
    onEdit: () -> Unit,
    onToggleDone: () -> Unit,
    onDelete: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClickRow() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // ✅ دایره وضعیت
        Box(
            modifier = Modifier
                .size(22.dp)
                .border(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                    shape = CircleShape
                )
                .background(
                    color = if (task.isDone) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f)
                    else Color.Transparent,
                    shape = CircleShape
                )
        )
        Spacer(Modifier.width(12.dp))

        // ✅ عنوان
        Text(
            task.title,
            modifier = Modifier.weight(1f)
        )

        // ✅ تقویم اگر schedule داشت
        if (task.hasSchedule) {
            Spacer(Modifier.width(8.dp))
            Icon(
                Icons.Filled.Event,
                contentDescription = "scheduled",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // ✅ expand اگر child داشت
        if (task.indentLevel > 0) {
            IconButton(onClick = onToggleExpand) {
                Icon(
                    imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = "expand"
                )
            }
        }

    }
}


@Composable
private fun SheetStatRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(12.dp))
        Text(text)
    }
    HorizontalDivider(thickness = 0.5.dp)
}

@Composable
private fun SheetActionRow(
    leading: @Composable () -> Unit,
    title: String,
    onClick: () -> Unit,
    trailing: (@Composable () -> Unit)? = null,
    titleColor: Color = LocalContentColor.current,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(26.dp), contentAlignment = Alignment.Center) { leading() }
        Spacer(Modifier.width(12.dp))
        Text(title, color = titleColor, modifier = Modifier.weight(1f))
        if (trailing != null) {
            Box(Modifier.size(24.dp), contentAlignment = Alignment.Center) { trailing() }
        }
    }
    HorizontalDivider(thickness = 0.5.dp)
}


@Composable
private fun TaskRowMini(
    task: TaskMiniUi,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // شبیه عکس: دایره کنار تسک
        Box(
            modifier = Modifier
                .size(22.dp)
                .border(
                    2.dp,
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                    CircleShape
                )
        )
        Spacer(Modifier.width(14.dp))
        Text(task.title)
    }
}

@Composable
fun AddTaskDialog(
    addTaskMod: Boolean,
    categoryName: String,
    categoryIconName: String,
    categoryColorHex: String,
    draft: TaskDraft,
    onDismiss: () -> Unit,
    onNameChange: (String) -> Unit,
    onPriorityChange: (Int) -> Unit,
    onCompletedToggle: (Boolean) -> Unit,
    onNoteChange: (String) -> Unit,
    onInsertAtTopChange: (Boolean) -> Unit,   // ✅ جدید
    onChildLevelChange: (Int) -> Unit,        // ✅ جدید
    onConfirm: (ConfirmAction) -> Unit,       // ✅ جدید
    onOpenSchedule: () -> Unit
) {


    DimmedDialog(
        onDismiss = onDismiss,
        modifier = Modifier
            .fillMaxWidth(0.92f)
            .fillMaxHeight(0.88f)
            .background(MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.extraLarge),
        dimAlpha = 0.6f,
        dismissOnBackdropClick = true
    ) {
        Column(modifier = Modifier.fillMaxSize())

        {

            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.ArrowBackIosNew, contentDescription = "close")
                }

                Text(
                    if (addTaskMod) "New task" else "Edit Task",
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(Modifier.weight(1f))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // ✅✅ فقط در حالت Add
                    IconButton(
                        onClick = { onConfirm(ConfirmAction.SAVE_AND_CONTINUE) },
                        enabled = addTaskMod && draft.name.isNotBlank()
                    ) {
                        Icon(Icons.Filled.DoneAll, contentDescription = "save_and_continue")
                    }

                    IconButton(
                        onClick = { onConfirm(ConfirmAction.SAVE_AND_CLOSE) },
                        enabled = draft.name.isNotBlank()
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = "save")
                    }
                }
            }
            HorizontalDivider()


            // ناحیه اسکرول دار
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f) // مهم
                    .verticalScroll(rememberScrollState())
            ) {

                // Title row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Check, contentDescription = null)
                    Spacer(Modifier.width(10.dp))
                    OutlinedTextField(
                        value = draft.name,
                        onValueChange = onNameChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Title") },
                        singleLine = true
                    )
                }
                HorizontalDivider()


                // Category row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = iconFromKey(categoryIconName),
                        contentDescription = null,
                        tint = colorFromHex(categoryColorHex),
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        categoryName,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(Modifier.weight(1f))

                    IconButton(onClick = { onInsertAtTopChange(!draft.insertAtTop) }) {
                        Icon(
                            imageVector = if (draft.insertAtTop) Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward,
                            contentDescription = "insert position"
                        )
                    }

                }
                HorizontalDivider()


                // Priority row (سه دایره مثل عکس)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Priority", modifier = Modifier.width(90.dp))
                    PriorityDots(
                        selected = draft.priority,
                        onPick = onPriorityChange
                    )
                }
                HorizontalDivider()


                //ردیف فرزند کردن
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Child", modifier = Modifier.width(90.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        ChildLevelChip(
                            label = "-",
                            selected = draft.childLevel == 0
                        ) { onChildLevelChange(0) }
                        ChildLevelChip(
                            label = ">",
                            selected = draft.childLevel == 1
                        ) { onChildLevelChange(1) }
                        ChildLevelChip(
                            label = ">>",
                            selected = draft.childLevel == 2
                        ) { onChildLevelChange(2) }
                        ChildLevelChip(
                            label = ">>>",
                            selected = draft.childLevel == 3
                        ) { onChildLevelChange(3) }
                    }

                }
                HorizontalDivider()


                // Completed row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (draft.isCompleted) "Completed" else "Uncompleted",
                        modifier = Modifier.weight(1f)
                    )
                    Switch(checked = draft.isCompleted, onCheckedChange = onCompletedToggle)
                }
                HorizontalDivider()


                // Note
                OutlinedTextField(
                    value = draft.note,
                    onValueChange = onNoteChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    label = { Text("Add note") },
                    minLines = 2
                )





                SheetActionRow(
                    leading = { Icon(Icons.Filled.Event, contentDescription = null) },
                    title = "Schedule this task",
                    onClick = { onOpenSchedule() }
                )

            }
        }
    }
}

@Composable
private fun PriorityDots(selected: Int, onPick: (Int) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        (0..2).forEach { i ->
            val isSel = i == selected
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isSel) 1f else 0.5f),
                        shape = CircleShape
                    )
                    .border(
                        width = if (isSel) 2.dp else 0.dp,
                        color = MaterialTheme.colorScheme.onSurface,
                        shape = CircleShape
                    )
                    .clickable { onPick(i) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (i) {
                        0 -> "*"; 1 -> "!"; else -> "!!"
                    },
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Composable
private fun ChildLevelChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (selected) 1f else 0.5f),
                shape = CircleShape
            )
            .border(
                width = if (selected) 2.dp else 0.dp,
                color = MaterialTheme.colorScheme.onSurface,
                shape = CircleShape
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(label, style = MaterialTheme.typography.titleMedium)
    }
}


@Composable
fun TaskScheduleDialog(
    taskName: String,
    draft: ScheduleDraft,
    onDismiss: () -> Unit,
    onTitleChange: (String) -> Unit,
    onModeChange: (ScheduleMode) -> Unit,
    onStartChange: (LocalTime) -> Unit,
    onEndChange: (LocalTime) -> Unit,
    onDurationChange: (Int) -> Unit,
    onRepeatingChange: (Boolean) -> Unit,
    onConfirm: () -> Unit
) {
    DimmedDialog(
        onDismiss = onDismiss,
        modifier = Modifier
            .fillMaxWidth(0.92f)
            .fillMaxHeight(0.88f)
            .background(MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.extraLarge),
        dimAlpha = 0.6f,
        dismissOnBackdropClick = true
    ) {
        Column(Modifier.fillMaxSize()) {

            // top bar
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = onDismiss) { Text("Back") }
                Text("New scheduled activity", style = MaterialTheme.typography.titleLarge)
                TextButton(onClick = onConfirm) { Text("✓") }
            }

            HorizontalDivider()

            // title (کم‌رنگ پیش‌فرض)
            OutlinedTextField(
                value = draft.title,
                onValueChange = onTitleChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                label = { Text("Title (optional)") },
                placeholder = {
                    Text(
                        taskName,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
                    )
                },
                singleLine = true
            )

            HorizontalDivider()

            // mode dropdown: Time range / Amount of time
            ModeDropdownRow(
                mode = draft.mode,
                onPick = onModeChange
            )

            HorizontalDivider()

            if (draft.mode == ScheduleMode.TIME_RANGE) {
                // فعلاً ساده: نمایش متن + بعداً TimePicker
                TimeRangeRow(
                    start = draft.start,
                    end = draft.end,
                    onStartChange = onStartChange,
                    onEndChange = onEndChange
                )
            } else {
                AmountOfTimeRow(
                    minutes = draft.durationMinutes,
                    onMinutesChange = onDurationChange
                )
            }

            HorizontalDivider()

            // Repeating
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Repeating", modifier = Modifier.weight(1f))
                Switch(checked = draft.repeating, onCheckedChange = onRepeatingChange)
            }

            HorizontalDivider()

            // Note / Reminder placeholder (فعلاً)
            SheetActionRow(
                leading = { Icon(Icons.Filled.Description, null) },
                title = "Add note",
                onClick = { /* بعداً */ }
            )
            SheetActionRow(
                leading = { Icon(Icons.Filled.History, null) },
                title = "Add reminder",
                onClick = { /* بعداً */ }
            )
        }
    }
}

@Composable
private fun ModeDropdownRow(
    mode: ScheduleMode,
    onPick: (ScheduleMode) -> Unit
) {
    var open by remember { mutableStateOf(false) }
    Row(
        Modifier
            .fillMaxWidth()
            .clickable { open = true }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Time range", modifier = Modifier.weight(1f))
        Text(
            text = if (mode == ScheduleMode.TIME_RANGE) "Time range" else "Amount of time",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.width(8.dp))
        Icon(Icons.Filled.ExpandMore, contentDescription = null)
    }
    DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
        DropdownMenuItem(
            text = { Text("Time range") },
            onClick = { open = false; onPick(ScheduleMode.TIME_RANGE) }
        )
        DropdownMenuItem(
            text = { Text("Amount of time") },
            onClick = { open = false; onPick(ScheduleMode.AMOUNT_OF_TIME) }
        )
    }
}

@Composable
private fun TimeRangeRow(
    start: LocalTime,
    end: LocalTime,
    onStartChange: (LocalTime) -> Unit,
    onEndChange: (LocalTime) -> Unit
) {
    // فعلاً فقط نمایش، بعداً TimePicker می‌ذاریم
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 18.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("${start}", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.width(18.dp))
        Text("-", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.width(18.dp))
        Text("${end}", style = MaterialTheme.typography.titleLarge)
    }
}

@Composable
private fun AmountOfTimeRow(
    minutes: Int,
    onMinutesChange: (Int) -> Unit
) {
    OutlinedTextField(
        value = minutes.toString(),
        onValueChange = { v -> onMinutesChange(v.filter(Char::isDigit).toIntOrNull() ?: minutes) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        label = { Text("Minutes") },
        singleLine = true
    )
}


//>>>>>>>>>>>>>>>> Utils <<<<<<<<<<<<<<<<<<

private enum class CategorySheetMode { OVERVIEW, TASKS }
enum class ConfirmAction { SAVE_AND_CLOSE, SAVE_AND_CONTINUE }

const val TASK_MAX_INDENT = 3

private fun normalizeUiList(flat: List<TaskMiniUi>): List<TaskMiniUi> {
    val lastIdAtIndent = arrayOfNulls<Int>(TASK_MAX_INDENT + 1) // 0..3
    var prevIndent = 0

    return flat.map { t ->
        var indent = t.indentLevel.coerceIn(0, TASK_MAX_INDENT)

        // ✅ قانون 1: پرش نداشته باش (نسبت به قبلی)
        indent = indent.coerceAtMost(prevIndent + 1)

        // ✅ قانون 2: اگر indent=2 ولی indent=1 نداریم => کمش کن
        while (indent > 0 && lastIdAtIndent[indent - 1] == null) {
            indent -= 1
        }

        val parentId = if (indent == 0) null else lastIdAtIndent[indent - 1]

        // ثبت
        lastIdAtIndent[indent] = t.id
        for (i in indent + 1..TASK_MAX_INDENT) lastIdAtIndent[i] = null
        prevIndent = indent

        t.copy(indentLevel = indent, parentTaskId = parentId)
    }
}
