package com.example.compoundeffectV1_01.ui.categoryScreen

import android.annotation.SuppressLint
import android.os.SystemClock
import android.util.Log
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
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
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RemoveDone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Task
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TimePicker
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.compoundeffectV1_01.data.dataBaseRoom.tables.category.CategoryEntity
import com.example.compoundeffectV1_01.data.dataBaseRoom.tables.task.Task
import com.example.compoundeffectV1_01.data.dataBaseRoom.tables.taskSchedule.RepeatUnit
import com.example.compoundeffectV1_01.data.dataBaseRoom.tables.taskSchedule.ScheduleMode
import com.example.compoundeffectV1_01.data.dataBaseRoom.tables.taskSchedule.TaskSchedule
import com.example.compoundeffectV1_01.utils.DimmedDialog
import com.example.compoundeffectV1_01.utils.IconOption
import com.example.compoundeffectV1_01.utils.buildColorOptions
import com.example.compoundeffectV1_01.utils.buildIconSections
import com.example.compoundeffectV1_01.utils.colorFromHex
import com.example.compoundeffectV1_01.utils.durationMinutesSameDay
import com.example.compoundeffectV1_01.utils.ensureAfter
import com.example.compoundeffectV1_01.utils.iconFromKey
import com.example.compoundeffectV1_01.utils.plusMinutesClamped
import com.example.compoundeffectV1_01.utils.toFaText
import com.example.compoundeffectV1_01.utils.toJalali
import com.example.compoundeffectV1_01.utils.toLocalDate
import com.gmail.hamedvakhide.compose_jalali_datepicker.JalaliDatePickerDialog
import ir.huri.jcal.JalaliCalendar
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable
import java.time.LocalDate
import java.time.LocalTime

@SuppressLint("SuspiciousIndentation")
@Composable
fun CategoryScreen(
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
    val childLevelUi by viewModel.childLevelUi.collectAsState()
    val schedules by viewModel.schedulesUiForTaskDialog.collectAsState()


    var showPickParent by rememberSaveable { mutableStateOf(false) }
    var showAddCategory by rememberSaveable { mutableStateOf(false) }

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

    var tasksExpanded by rememberSaveable(menuCategoryId) { mutableStateOf(false) }

    var showScheduleDialog by rememberSaveable { mutableStateOf(false) }

    var sheetMode by rememberSaveable(menuCategoryId) { mutableStateOf(CategorySheetMode.OVERVIEW) }

    var showTaskDialog by rememberSaveable { mutableStateOf(false) }

    var showPickTaskCategory by rememberSaveable { mutableStateOf(false) }




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

    LaunchedEffect(showTaskDialog, childLevelUi.allowed) {
        if (showTaskDialog) viewModel.clampTaskChildLevel(childLevelUi.allowed)
    }


    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddCategory = true }
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Category")
            }


        },
        floatingActionButtonPosition = FabPosition.Start
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

            val draggingKey = remember { mutableStateOf<Int?>(null) }


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

                val now = SystemClock.uptimeMillis()
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




            LazyColumn(
                state = reorderState.listState,
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

                            CategoryRow(
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
        AddCategoryDialog(
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
            onDescriptionChange = {
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
                    parentTaskId = tws.task.parentTaskId,
                    siblingIndex = tws.task.siblingIndex,
                    priority = tws.task.priority,

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
                viewModel.startAddTask(menuCategory.categoryId!!)
                showTaskDialog = true
            },
            onClickTask = { taskId ->
                viewModel.startEditTask(taskId)
                showTaskDialog = true
            },
            scheduledCount = scheduledCount,
            sheetMode = sheetMode,
            onChangeMode = { sheetMode = it },
            toggleTaskCompleted = { taskId, _ ->
                viewModel.toggleTaskCompletedCascade(taskId)
            },
            deleteTask = { taskId: Int ->
                viewModel.deleteTask(taskId)
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
            toggleExpandForTask = { viewModel.toggleExpandForTask(it) },
            onCompleteAll = { viewModel.completeAllTasks(it) },
            onUncompletedAll = { viewModel.uncompletedAllTasks(it) },
            onDeleteCompleted = { viewModel.deleteCompletedTasks(it) },
            onDeleteAll = { viewModel.deleteAllTasks(it) },

            )
    }

    if (showDeleteConfirm && menuCategoryId != null) {
        AlertDialog(
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
        AlertDialog(
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

        val selectedCategoryId = taskDraft.categoryId ?: menuCategory.categoryId
        val selectedCategory = state.categories.firstOrNull { it.categoryId == selectedCategoryId }
            ?: menuCategory

        val isEdit = (editingTaskId != null)

        AddTaskDialog(
            addTaskMod = !isEdit,
            categoryName = selectedCategory.name,
            categoryIconName = selectedCategory.iconName,
            categoryColorHex = selectedCategory.color,
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
            onPickCategory = { showPickTaskCategory = true },
            onConfirm = { action ->
                val editing = (editingTaskId != null)
                val color = selectedCategory.color

                if (editing) {
                    viewModel.saveEditedTask(color)
                    showTaskDialog = false
                } else {
                    viewModel.createTaskForCategory(color)

                    if (action == ConfirmAction.SAVE_AND_CLOSE) {
                        showTaskDialog = false
                    } else {
                        viewModel.resetTaskDraftKeepSomeDefaults()
                    }
                }
            },
            onOpenSchedule = {
                viewModel.startAddSchedule()
                showScheduleDialog = true
            },
            allowedChildLevels = childLevelUi.allowed,
            schedules = schedules,
            onClickSchedule = { key ->
                viewModel.startEditScheduleByKey(key)
                showScheduleDialog = true
            },
            onDeleteSchedule = { key ->
                viewModel.deleteScheduleByKey(key)
            },
        )


    }

    if (showScheduleDialog && menuCategory != null) {
        TaskScheduleDialog(
            taskName = taskDraft.name.ifBlank { "Task" },
            draft = scheduleDraft,
            onDismiss = {
                showScheduleDialog = false
                viewModel.finishEditSchedule()
            },
            onTitleChange = viewModel::setScheduleTitle,
            onModeChange = viewModel::setScheduleMode,
            onStartChange = viewModel::setScheduleStart,
            onEndChange = viewModel::setScheduleEnd,
            onDurationChange = viewModel::setScheduleDuration,
            onRepeatingChange = viewModel::setRepeatEnabled,
            onConfirm = {
                viewModel.confirmScheduleFromDialog()

                showScheduleDialog = false
            },
            onDateChange = viewModel::setScheduleDate,
            onRepeatIntervalChange = viewModel::setRepeatInterval,
            onRepeatUnitChange = viewModel::setRepeatUnit,
            onWeekdaysMaskChange=viewModel::setRepeatWeekdaysMask,
        )
    }

    if (showTaskDialog && showPickTaskCategory) {
        PickParentDialogSmall(
            items = pickerFlatten.items,
            levelById = pickerFlatten.levelById,
            onDismiss = { showPickTaskCategory = false },
            onPick = { categoryId ->
                viewModel.setTaskCategoryId(categoryId)
                showPickTaskCategory = false
            }
        )
    }


}

@Composable
fun AddCategoryDialog(
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
private fun JalaliDateRow(
    selectedDate: LocalDate,
    onChangeDate: (LocalDate) -> Unit
) {
    val openDialog = remember { mutableStateOf(false) }

    val jalali = remember(selectedDate) { selectedDate.toJalali() }
    val dateText = remember(selectedDate) { jalali.toFaText() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { openDialog.value = true }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Filled.CalendarMonth, contentDescription = null)
        Spacer(Modifier.width(12.dp))

        Text(
            text = dateText,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f)
        )

        IconButton(onClick = { onChangeDate(LocalDate.now()) }) {
            Icon(Icons.Filled.Refresh, contentDescription = "today")
        }
    }


    // دیالوگ تقویم شمسی
    JalaliDatePickerDialog(
        openDialog = openDialog,
        initialDate = jalali,

        onConfirm = { picked: JalaliCalendar ->
            onChangeDate(picked.toLocalDate())
        },

        // محدودیت تاریخ نداشته باشیم
        disableBeforeDate = null,
        disableAfterDate = null,

        onSelectDay = { /* لازم نیست کاری کنیم */ },

        // رنگ‌ها از تم فعلی
        backgroundColor = Color(0xFFF8F9FB),         // خیلی روشن، نرم و مدرن

        textColor = Color(0xFF1C1C1E),                // مشکی نرم (نه کاملاً سیاه)
        textDisabledColor = Color(0xFFB0B3B8),       // خاکستری ملایم

        selectedIconColor = Color(0xFF00B894),        // سبز فیروزه‌ای زنده
        textColorHighlight = Color(0xFF009E84),       // سبز کمی تیره‌تر برای انتخاب

        dropDownColor = Color(0xFF1C1C1E),
        dayOfWeekLabelColor = Color(0xFF6B7280),      // خاکستری متوسط برای شنبه/یکشنبه...

        confirmBtnColor = Color(0xFF00B894),          // هم‌رنگ انتخاب
        cancelBtnColor = Color(0xFF9CA3AF),           // خاکستری خنثی
        todayBtnColor = Color(0xFF1C1C1E),
        nextPreviousBtnColor = Color(0xFF00B894),    // فلش‌های ماه

        fontFamily = FontFamily.Default,   // پیش‌فرض سیستم
        fontSize = 18.sp     // پیش‌فرض کتابخانه
    )

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RepeatEveryRow(
    interval: Int,
    unit: RepeatUnit,
    onIntervalChange: (Int) -> Unit,
    onUnitChange: (RepeatUnit) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val unitLabel = when (unit) {
        RepeatUnit.DAY -> "days"
        RepeatUnit.WEEK -> "weeks"
        RepeatUnit.MONTH -> "months"
        RepeatUnit.YEAR -> "years"
    }

    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text("Repeat every")

        Spacer(Modifier.width(10.dp))

        // عدد 0..99 (ولی بهتره ذخیره رو 1..99 کنی)
        TextField(
            value = interval.toString(),
            onValueChange = { v ->
                val n = v.filter(Char::isDigit).take(2).toIntOrNull() ?: 0
                onIntervalChange(n.coerceIn(0, 99))
            },
            singleLine = true,
            modifier = Modifier
                .width(54.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                errorContainerColor = Color.Transparent
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,   // ✅ فقط عدد
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    keyboardController?.hide()   // کیبورد بسته شود
                    focusManager.clearFocus()    // فوکوس برداشته شود
                }
            )
        )

        Spacer(Modifier.width(10.dp))

        // واحد (مثل مود، کشویی)
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.weight(1f)
        ) {
            TextField(
                value = unitLabel,
                onValueChange = {},
                readOnly = true,
                singleLine = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                modifier = Modifier
                    .width(132.dp)
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    errorContainerColor = Color.Transparent
                ),
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                DropdownMenuItem(
                    text = { Text("days") },
                    onClick = { onUnitChange(RepeatUnit.DAY); expanded = false })
                DropdownMenuItem(
                    text = { Text("weeks") },
                    onClick = { onUnitChange(RepeatUnit.WEEK); expanded = false })
                DropdownMenuItem(
                    text = { Text("months") },
                    onClick = { onUnitChange(RepeatUnit.MONTH); expanded = false })
                DropdownMenuItem(
                    text = { Text("years") },
                    onClick = { onUnitChange(RepeatUnit.YEAR); expanded = false })
            }
        }
    }
}

@Composable
private fun WeekdayPickerRow(
    selectedMask: Int,
    onChangeMask: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    // Sa Su Mo Tu We Th Fr => بیت‌های 0..6
    val days = listOf(
        0 to "Sa", 1 to "Su", 2 to "Mo", 3 to "Tu",
        4 to "We", 5 to "Th", 6 to "Fr",
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 6.dp, bottom = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        days.forEach { (bit, label) ->
            val selected = (selectedMask and (1 shl bit)) != 0

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        color = if (selected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant,
                        shape = CircleShape
                    )
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                        CircleShape
                    )
                    .clickable {
                        val newMask =
                            if (selected) selectedMask and (1 shl bit).inv()
                            else selectedMask or (1 shl bit)
                        onChangeMask(newMask)
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (selected) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}



@Composable
private fun CategoryRow(
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

                IconButton(onClick = { onOpenMenu(id) }) {
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
                    interactionSource = remember { MutableInteractionSource() }
                ) { onToggleDone(id) }
        )


        // ✅ Priority marker
        val (pText, pColor) = when (item.task.priority) {
            1 -> "*" to Color(0xFF2E7D32) // سبز
            2 -> "!" to Color(0xFFF9A825) // زرد
            3 -> "!!" to Color(0xFFC62828) // قرمز
            else -> "" to Color.Unspecified
        }
        if (pText.isNotBlank()) {
            Spacer(Modifier.width(8.dp))
            Text(
                text = pText,
                color = pColor,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.width(8.dp))
        } else {
            Spacer(Modifier.width(12.dp))
        }

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
                tint = MaterialTheme.colorScheme.error
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
            .fillMaxHeight(0.65f)
            .background(MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.extraLarge),
        dimAlpha = 0.4f,
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
            sections.forEach { put(it.title, true) }
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
                        onToggle = { expanded[section.title] = expanded[section.title] != true }
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
    onClickTask: (Int) -> Unit,
    scheduledCount: Int,
    sheetMode: CategorySheetMode,
    onChangeMode: (CategorySheetMode) -> Unit,
    toggleTaskCompleted: (taskId: Int, done: Boolean) -> Unit,
    deleteTask: (taskId: Int) -> Unit,
    applyTaskDragResult: (
        draggedId: Int,
        oldParentId: Int?,
        newParentId: Int?,
        categoryId: Int,
        currentList: List<TaskRenderItem>
    ) -> Unit,
    onDragEndRestoreExpandForTask: () -> Unit,
    onDragStartMaybeCollapseForTask: (taskId: Int) -> Unit,
    toggleExpandForTask: (taskId: Int) -> Unit,
    onCompleteAll: (Int) -> Unit,
    onUncompletedAll: (Int) -> Unit,
    onDeleteCompleted: (Int) -> Unit,
    onDeleteAll: (Int) -> Unit,
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
                    interactionSource = remember { MutableInteractionSource() }
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
                                text = "$scheduledCount scheduled activities"
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
                    TasksModeContent(
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
                        toggleExpandForTask = { toggleExpandForTask(it) },
                        onCompleteAll = { onCompleteAll(category.categoryId!!) },
                        onUncompletedAll = { onUncompletedAll(category.categoryId!!) },
                        onDeleteCompleted = { onDeleteCompleted(category.categoryId!!) },
                        onDeleteAll = { onDeleteAll(category.categoryId!!) },
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
private fun TasksModeContent(
    category: CategoryEntity,
    tasks: List<Task>,
    taskMiniList: List<TaskMiniUi>,
    tasksRenderList: List<TaskRenderItem>,
    onBack: () -> Unit,
    onAddTask: () -> Unit,
    onClickTask: (Int) -> Unit,
    toggleTaskCompleted: (taskId: Int, done: Boolean) -> Unit,
    deleteTask: (taskId: Int) -> Unit,
    applyTaskDragResult: (
        draggedId: Int,
        oldParentId: Int?,
        newParentId: Int?,
        categoryId: Int,
        currentList: List<TaskRenderItem>
    ) -> Unit,
    onDragEndRestoreExpandForTask: () -> Unit,
    onDragStartMaybeCollapseForTask: (taskId: Int) -> Unit,
    toggleExpandForTask: (taskId: Int) -> Unit,
    onCompleteAll: () -> Unit,
    onUncompletedAll: () -> Unit,
    onDeleteCompleted: () -> Unit,
    onDeleteAll: () -> Unit,
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

    fun normalizeParent(p: Int?): Int? = if (p == ROOT) null else p

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

// کمک: دسترسی سریع به آیتم‌ها با id (از لیست واقعی uiState)
    val entityById = remember(tasks) {
        tasks.associateBy { it.id }
    }

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


    // parent موثر: اگر pending داریم از آن استفاده کن
    fun effectiveParentId(id: Int): Int? {
        val p = pendingParentById[id] ?: entityById[id]?.parentTaskId
        return normalizeParent(p)
    }

    // محاسبه level بر اساس parent موثر (حداکثر 4)
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
        if (currentParent == null) return

        val parentItem = allById[currentParent] ?: return
        val newParent = parentItem.task.parentTaskId

        // ✅ ریشه را با -1 نگه دار (نه null)
        pendingParentById[id] = newParent
    }


    val onHorizontalReparentHint: (Int, Float) -> Unit = reparent@{ id, deltaX ->
        val acc = (dragOffsetXById[id] ?: 0f) + deltaX
        dragOffsetXById[id] = acc

        val now = SystemClock.uptimeMillis()
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

    var expandedForAll by rememberSaveable { mutableStateOf(true) }

    var sortMode by rememberSaveable { mutableStateOf(TaskSortMode.NONE) }

    LaunchedEffect(sortMode, tasksRenderList) {
        if (!dragging) {
            listState.value = when (sortMode) {
                TaskSortMode.NONE -> tasksRenderList
                TaskSortMode.BY_NAME -> tasksRenderList.sortedBy { it.task.title.lowercase() }
                TaskSortMode.BY_PRIORITY -> tasksRenderList.sortedByDescending { it.task.priority }
                TaskSortMode.BY_COMPLETED -> tasksRenderList.sortedBy { it.task.isDone } // انجام نشده‌ها بالا
            }
        }
    }





    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {

        // ردیف اول: Add task + آیکون کتگوری + Search
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBack) { Text("Back") }

            Spacer(Modifier.width(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onAddTask() },
                verticalAlignment = Alignment.CenterVertically
            ) {

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

                )
            }

        }

        HorizontalDivider()

        // ردیف دوم: آیکون تسک، تعداد، Tasks، اکسپند، سه نقطه
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
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

            TasksOptionsMenu(
                sortMode = sortMode,
                onChangeSort = { sortMode = it },
                onCompleteAll = onCompleteAll,
                onUncompletedAll = onUncompletedAll,
                onDeleteCompleted = onDeleteCompleted,
                onDeleteAll = onDeleteAll
            )


        }
        HorizontalDivider(thickness = 0.5.dp)

        var pendingDeleteTaskId by rememberSaveable { mutableStateOf<Int?>(null) }
        var pendingDeleteTaskTitle by rememberSaveable { mutableStateOf("") }


        androidx.compose.animation.AnimatedVisibility(
            visible = expandedForAll,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {

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
                            val isDragActive = reorderState.draggingItemKey != null

                            val dismissState =
                                rememberSwipeToDismissBoxState(
                                    confirmValueChange = { value ->
                                        // فقط سوایپ به چپ (EndToStart) را به حذف تبدیل کن
                                        if (!isDragActive && value == SwipeToDismissBoxValue.EndToStart) {
                                            pendingDeleteTaskId = id
                                            pendingDeleteTaskTitle = item.task.title
                                        }
                                        // ❗ همیشه false تا آیتم واقعاً dismiss (حذف نمایشی) نشود
                                        false
                                    }
                                )

                            SwipeToDismissBox(
                                state = dismissState,
                                enableDismissFromStartToEnd = false,
                                enableDismissFromEndToStart = !isDragActive,
                                backgroundContent = {
                                    // پس‌زمینه‌ی حذف (ساده)
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(MaterialTheme.colorScheme.error.copy(alpha = 0.15f))
                                            .padding(horizontal = 16.dp),
                                        contentAlignment = Alignment.CenterEnd
                                    ) {
                                        Icon(Icons.Filled.Delete, contentDescription = "delete")
                                    }
                                }
                            ) {

                                TaskRow(
                                    item = item,
                                    computedLevel = effectiveLevel(id),
                                    onToggleExpand = { toggleExpandForTask(it) },
                                    onClickTask = { onClickTask(it) },
                                    onToggleDone = { taskId ->
                                        val cur =
                                            listState.value.firstOrNull { it.task.id == taskId }?.task
                                                ?: return@TaskRow
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

        if (pendingDeleteTaskId != null) {
            AlertDialog(
                onDismissRequest = { pendingDeleteTaskId = null },
                title = { Text("Delete task?") },
                text = { Text("آیا از حذف این تسک مطمئن هستی؟\n\n${pendingDeleteTaskTitle}") },
                confirmButton = {
                    TextButton(onClick = {
                        val tid = pendingDeleteTaskId!!
                        deleteTask(tid)          // همون callback که به VM وصل است
                        pendingDeleteTaskId = null
                    }) { Text("Delete") }
                },
                dismissButton = {
                    TextButton(onClick = { pendingDeleteTaskId = null }) { Text("Cancel") }
                }
            )
        }


    }
}

@Composable
fun TasksOptionsMenu(
    sortMode: TaskSortMode,
    onChangeSort: (TaskSortMode) -> Unit,
    onCompleteAll: () -> Unit,
    onUncompletedAll: () -> Unit,
    onDeleteCompleted: () -> Unit,
    onDeleteAll: () -> Unit
) {

    var menuOpen by remember { mutableStateOf(false) }
    var sortSubOpen by remember { mutableStateOf(false) }

    Box {

        IconButton(onClick = { menuOpen = true }) {
            Icon(Icons.Filled.MoreVert, contentDescription = "tasks menu")
        }

        // ====== منوی اصلی ======
        DropdownMenu(
            expanded = menuOpen,
            onDismissRequest = {
                menuOpen = false
                sortSubOpen = false
            }
        ) {

            // ===== Sort =====
            DropdownMenuItem(
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = null)
                        Spacer(Modifier.width(10.dp))
                        Text("Sort")
                    }
                },
                trailingIcon = {
                    Icon(Icons.AutoMirrored.Filled.ArrowRight, contentDescription = null)
                },
                onClick = { sortSubOpen = true }
            )

            HorizontalDivider()

            DropdownMenuItem(
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.DoneAll, contentDescription = null)
                        Spacer(Modifier.width(10.dp))
                        Text("Complete all")
                    }
                },
                onClick = {
                    menuOpen = false
                    onCompleteAll()
                }
            )

            DropdownMenuItem(
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.RemoveDone, contentDescription = null)
                        Spacer(Modifier.width(10.dp))
                        Text("Uncomplete all")
                    }
                },
                onClick = {
                    menuOpen = false
                    onUncompletedAll()
                }
            )

            HorizontalDivider()

            DropdownMenuItem(
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.DeleteSweep, contentDescription = null)
                        Spacer(Modifier.width(10.dp))
                        Text("Delete completed")
                    }
                },
                onClick = {
                    menuOpen = false
                    onDeleteCompleted()
                }
            )

            DropdownMenuItem(
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Delete, contentDescription = null)
                        Spacer(Modifier.width(10.dp))
                        Text("Delete all")
                    }
                },
                onClick = {
                    menuOpen = false
                    onDeleteAll()
                }
            )
        }

        // ====== ساب منوی Sort ======
        DropdownMenu(
            expanded = menuOpen && sortSubOpen,
            onDismissRequest = { sortSubOpen = false },
            offset = DpOffset(x = 180.dp, y = 0.dp)
        ) {

            SortMenuItem(
                title = "By name",
                selected = sortMode == TaskSortMode.BY_NAME
            ) {
                onChangeSort(TaskSortMode.BY_NAME)
                menuOpen = false
                sortSubOpen = false
            }

            SortMenuItem(
                title = "By priority",
                selected = sortMode == TaskSortMode.BY_PRIORITY
            ) {
                onChangeSort(TaskSortMode.BY_PRIORITY)
                menuOpen = false
                sortSubOpen = false
            }

            SortMenuItem(
                title = "By completed",
                selected = sortMode == TaskSortMode.BY_COMPLETED
            ) {
                onChangeSort(TaskSortMode.BY_COMPLETED)
                menuOpen = false
                sortSubOpen = false
            }

            SortMenuItem(
                title = "No sort",
                selected = sortMode == TaskSortMode.NONE
            ) {
                onChangeSort(TaskSortMode.NONE)
                menuOpen = false
                sortSubOpen = false
            }
        }
    }
}

@Composable
private fun SortMenuItem(
    title: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    DropdownMenuItem(
        text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (selected)
                        Icons.Filled.RadioButtonChecked
                    else
                        Icons.Filled.RadioButtonUnchecked,
                    contentDescription = null
                )
                Spacer(Modifier.width(10.dp))
                Text(title)
            }
        },
        onClick = onClick
    )
}


@Composable
private fun SheetStatRow(icon: ImageVector, text: String) {
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
    onOpenSchedule: () -> Unit,
    onPickCategory: () -> Unit,
    allowedChildLevels: Set<Int>,
    schedules: List<TaskScheduleUi>,
    onClickSchedule: (Int) -> Unit,
    onDeleteSchedule: (Int) -> Unit,
) {

    var pendingDeleteScheduleId by rememberSaveable { mutableStateOf<Int?>(null) }
    var pendingDeleteScheduleTitle by rememberSaveable { mutableStateOf("") }


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
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.ArrowBackIosNew, contentDescription = "close")
                }

                Spacer(Modifier.width(16.dp))

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
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
//                    Icon(Icons.Filled.Check, contentDescription = null)
//                    Spacer(Modifier.width(10.dp))
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
                        .clickable { onPickCategory() }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = iconFromKey(categoryIconName),
                        contentDescription = null,
                        tint = colorFromHex(categoryColorHex),
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(Modifier.width(14.dp))
                    Text(
                        categoryName,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(Modifier.weight(1f))

                    VerticalDivider(thickness = 1.dp, modifier = Modifier.height(24.dp))

                    Spacer(Modifier.width(8.dp))

                    IconButton(onClick = { onInsertAtTopChange(!draft.insertAtTop) }) {
                        Icon(
                            imageVector = if (draft.insertAtTop) Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward,
                            contentDescription = "insert position"
                        )
                    }

                }
                HorizontalDivider()


                // Priority row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
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
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Child", modifier = Modifier.width(90.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {

                        ChildLevelChip(
                            "-",
                            selected = draft.childLevel == 0,
                            enabled = 0 in allowedChildLevels
                        ) { onChildLevelChange(0) }
                        ChildLevelChip(
                            ">",
                            selected = draft.childLevel == 1,
                            enabled = 1 in allowedChildLevels
                        ) { onChildLevelChange(1) }
                        ChildLevelChip(
                            ">>",
                            selected = draft.childLevel == 2,
                            enabled = 2 in allowedChildLevels
                        ) { onChildLevelChange(2) }
                        ChildLevelChip(
                            ">>>",
                            selected = draft.childLevel == 3,
                            enabled = 3 in allowedChildLevels
                        ) { onChildLevelChange(3) }

                    }

                }
                HorizontalDivider()


                // Completed row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
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

                if (schedules.isNotEmpty()) {
                    Column(Modifier.fillMaxWidth()) {
                        schedules.forEach { ui ->
                            ScheduleRow(
                                schedule = ui.schedule,
                                onClick = { onClickSchedule(ui.key) },
                                onRequestDelete = {
                                    // ✅ فقط درخواست حذف => دیالوگ باز شود
                                    pendingDeleteScheduleId = ui.key
                                    pendingDeleteScheduleTitle =
                                        ui.schedule.title?.takeIf { it.isNotBlank() }
                                            ?: taskNameForScheduleFallback(draft.name) // پایین تعریف می‌کنیم
                                }
                            )

                            HorizontalDivider(thickness = 0.5.dp)
                        }
                    }
                }


                if (pendingDeleteScheduleId != null) {
                    AlertDialog(
                        onDismissRequest = { pendingDeleteScheduleId = null },
                        title = { Text("Delete schedule?") },
                        text = { Text("آیا از حذف این اسکچول مطمئن هستی؟\n\n$pendingDeleteScheduleTitle") },
                        confirmButton = {
                            TextButton(onClick = {
                                val key = pendingDeleteScheduleId!!
                                onDeleteSchedule(key) // وصل به viewModel.deleteScheduleByKey(key)
                                pendingDeleteScheduleId = null
                            }) { Text("Delete") }
                        },
                        dismissButton = {
                            TextButton(onClick = {
                                pendingDeleteScheduleId = null
                            }) { Text("Cancel") }
                        }
                    )
                }


            }
        }
    }
}

private fun taskNameForScheduleFallback(taskName: String): String =
    taskName.trim().ifBlank { "Task" }


@Composable
private fun ScheduleRow(
    schedule: TaskSchedule,
    onClick: () -> Unit,
    onRequestDelete: () -> Unit
) {
    fun minuteToTime(min: Int): String {
        val h = min / 60
        val m = min % 60
        return "%02d:%02d".format(h, m)
    }

    val icon = when (schedule.mode) {
        ScheduleMode.TIME_RANGE -> Icons.Filled.DateRange
        ScheduleMode.AMOUNT_OF_TIME -> Icons.Filled.Timer
    }

    val timeText = when (schedule.mode) {
        ScheduleMode.TIME_RANGE -> {
            val s = schedule.startMinuteOfDay ?: 0
            val e = schedule.endMinuteOfDay ?: 0
            "${minuteToTime(s)} - ${minuteToTime(e)}"
        }

        ScheduleMode.AMOUNT_OF_TIME -> {
            val d = schedule.durationMinutes ?: 0
            "$d min"
        }
    }

    val dateText =
        schedule.dateEpochDay?.let { epoch ->
            LocalDate.ofEpochDay(epoch).toString()
        } ?: "No date"

    val repeatText = if (schedule.repeating) "Repeats" else "No repeat"

    val reminderText = schedule.reminderMinutesBefore?.let { "Reminder: $it min before" } ?: ""

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null)

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(timeText, style = MaterialTheme.typography.titleMedium)
            Text(
                text = buildString {
                    append(dateText)
                    append(" • ")
                    append(repeatText)
                    if (reminderText.isNotBlank()) {
                        append(" • ")
                        append(reminderText)
                    }
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        ScheduleOptionsMenu(
            onDelete = onRequestDelete
        )
    }
}

@Composable
private fun ScheduleOptionsMenu(
    onDelete: () -> Unit
) {
    var open by remember { mutableStateOf(false) }

    Box {
        IconButton(onClick = { open = true }) {
            Icon(Icons.Filled.MoreVert, contentDescription = "schedule menu")
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            DropdownMenuItem(
                text = { Text("Delete") },
                leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = null) },
                onClick = { open = false; onDelete() }
            )
        }
    }
}


@Composable
private fun PriorityDots(selected: Int, onPick: (Int) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        (0..3).forEach { i ->
            val isSel = i == selected
            Box(
                modifier = Modifier
                    .size(36.dp)
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
                        0 -> "-"; 1 -> "*"; 2 -> "!"; else -> "!!"
                    },
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Composable
private fun ChildLevelChip(
    label: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val alpha = if (enabled) 1f else 0.35f

    Box(
        modifier = Modifier
            .size(44.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (selected) 1f else 0.5f),
                shape = CircleShape
            )
            .border(
                width = if (selected) 2.dp else 0.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha),
                shape = CircleShape
            )
            .then(
                if (enabled) Modifier.clickable { onClick() } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            style = MaterialTheme.typography.titleMedium,
            color = LocalContentColor.current.copy(alpha = alpha)
        )
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
    onConfirm: () -> Unit,
    onDateChange: (LocalDate) -> Unit,
    onRepeatIntervalChange: (Int) -> Unit,
    onRepeatUnitChange: (RepeatUnit) -> Unit,
    onWeekdaysMaskChange: (Int) -> Unit
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
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
            ,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

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
            TextField(
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
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    errorContainerColor = Color.Transparent
                ),
            )

            HorizontalDivider()

            // mode dropdown: Time range / Amount of time
            ModeDropdownRow(
                mode = draft.mode,
                onPick = onModeChange
            )

            HorizontalDivider(Modifier.padding(top = 8.dp))

            //  TimePicker
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


            if (draft.mode == ScheduleMode.TIME_RANGE || draft.mode == ScheduleMode.AMOUNT_OF_TIME) {


                JalaliDateRow(
                    selectedDate = draft.date,
                    onChangeDate = { onDatePicked ->
                        // چون draft از VM میاد، باید setter داشته باشی:
                        // viewModel.setScheduleDate(onDatePicked)
                        // اینجا فقط callback می‌گیریم:
                        onDateChange(onDatePicked)
                    }
                )

                HorizontalDivider()
            }


            // Repeating
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Repeating", modifier = Modifier.weight(1f))
                Switch(checked = draft.repeat.enabled, onCheckedChange = onRepeatingChange)
            }

            HorizontalDivider()

            //RepeatOptions
            if (draft.repeat.enabled) {

                RepeatEveryRow(
                    interval = draft.repeat.interval,
                    unit = draft.repeat.unit,
                    onIntervalChange = { onRepeatIntervalChange(it) },
                    onUnitChange = { onRepeatUnitChange(it) }
                )

                if (draft.repeat.unit == RepeatUnit.WEEK) {
                    WeekdayPickerRow(
                        selectedMask = draft.repeat.weekdaysMask,
                        onChangeMask = onWeekdaysMaskChange
                    )
                }

                HorizontalDivider()
            }




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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModeDropdownRow(
    mode: ScheduleMode,
    onPick: (ScheduleMode) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val selectedLabel = when (mode) {
        ScheduleMode.TIME_RANGE -> "Time range"
        ScheduleMode.AMOUNT_OF_TIME -> "Amount of time"
    }

    val items = listOf(
        ScheduleMode.TIME_RANGE to "Time range",
        ScheduleMode.AMOUNT_OF_TIME to "Amount of time"
    )

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        // ✅ anchor = کل عرض دیالوگ
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryEditable), // یا Primary
            contentAlignment = Alignment.Center
        ) {
            TextField(
                value = selectedLabel,
                onValueChange = {},
                readOnly = true,
                singleLine = true,
                label = { Text("Type") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.width(220.dp), // ✅ کوچیک‌تر و وسط
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    textAlign = TextAlign.Center // ✅ متن وسط
                ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    errorContainerColor = Color.Transparent
                ),
            )
        }

        // ✅ منو هم‌عرض anchor (یعنی هم‌عرض دیالوگ) و زیر ردیف
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            items.forEach { (value, label) ->
                DropdownMenuItem(
                    text = { Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    onClick = {
                        onPick(value)
                        expanded = false
                    }
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimeRangeRow(
    start: LocalTime,
    end: LocalTime,
    onStartChange: (LocalTime) -> Unit,
    onEndChange: (LocalTime) -> Unit,
) {
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

    // وقتی Start تایید شد، این فلگ باعث میشه End بعدش باز بشه
    var openEndAfterStart by remember { mutableStateOf(false) }

    // یک مدت پیش‌فرض/آخرین مدت معتبر برای پیشنهاد End
    var lastDurationMinutes by remember { mutableIntStateOf(60) }

    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 18.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TimeChip(label = "Start", time = start) { showStartPicker = true }
        Text("-", style = MaterialTheme.typography.titleLarge)
        TimeChip(label = "End", time = end) { showEndPicker = true }
    }

    if (showStartPicker) {
        TimePickerDialog(
            title = "Select start time",
            initial = start,
            onDismiss = {
                showStartPicker = false
                openEndAfterStart = false
            },
            onConfirm = { newStart ->
                // مدت فعلی اگر معتبر بود نگه دار، وگرنه lastDuration همان 60 بماند
                val duration = durationMinutesSameDay(start, end)
                if (duration > 0) lastDurationMinutes = duration

                // Start را ست کن
                onStartChange(newStart)

                // End پیشنهادی (برای نمایش اولیه در دیالوگ End)
                val suggestedEnd =
                    newStart.plusMinutesClamped(lastDurationMinutes).ensureAfter(newStart)
                onEndChange(suggestedEnd)

                // Start بسته، End خودکار باز
                showStartPicker = false
                openEndAfterStart = true
            }
        )
    }

    // ✅ نکته مهم: باز کردن دیالوگ End را با LaunchedEffect انجام بده
    // تا در یک فریم، اول Start بسته شود بعد End باز شود (بدون تداخل)
    LaunchedEffect(openEndAfterStart) {
        if (openEndAfterStart) {
            showEndPicker = true
            openEndAfterStart = false
        }
    }

    if (showEndPicker) {
        TimePickerDialog(
            title = "Select end time",
            initial = end,
            onDismiss = { showEndPicker = false },
            onConfirm = { newEnd ->
                val fixed = newEnd.ensureAfter(start)
                // ✅ اینجا “برعکس نه” رعایت میشه: فقط End تغییر می‌کند
                onEndChange(fixed)

                // اگر end > start شد، lastDuration را آپدیت کن تا دفعه بعد پیشنهاد بهتر باشد
                val d = durationMinutesSameDay(start, fixed)
                if (d > 0) lastDurationMinutes = d

                showEndPicker = false
            }
        )
    }
}

@Composable
private fun TimeChip(
    label: String,
    time: LocalTime,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelMedium)
        Spacer(Modifier.height(6.dp))
        TextButton(onClick = onClick) {
            Text(
                "%02d:%02d".format(time.hour, time.minute),
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    title: String,
    initial: LocalTime,
    onDismiss: () -> Unit,
    onConfirm: (LocalTime) -> Unit
) {
    val state = rememberTimePickerState(
        initialHour = initial.hour,
        initialMinute = initial.minute,
        is24Hour = true
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { TimePicker(state = state) },
        confirmButton = {
            TextButton(onClick = {
                onConfirm(
                    LocalTime.of(
                        state.hour,
                        state.minute
                    )
                )
            }) { Text("OK") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
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
enum class TaskSortMode { NONE, BY_NAME, BY_PRIORITY, BY_COMPLETED }

const val ROOT = -1

