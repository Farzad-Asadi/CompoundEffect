package com.example.compoundeffectV1_01.ui.categoryScreen

import android.util.Log
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen2(
    onNavigateToSchedule: () -> Unit, // فعلاً استفاده نمی‌کنیم، بعداً به bottom bar وصل می‌کنیم
    viewModel: CategoryViewModel2 = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val draft by viewModel.draft.collectAsState()
    val createResult by viewModel.createResult.collectAsState()
    val pickerFlatten by viewModel.parentPickerItems.collectAsState()



    var showPickParent by rememberSaveable { mutableStateOf(false) }
    var showAddCategory by rememberSaveable { mutableStateOf(false) }
    var isDragging by remember { mutableStateOf(false) }

    var showIconPicker by rememberSaveable { mutableStateOf(false) }
    var showColorPicker by rememberSaveable { mutableStateOf(false) }

    var nameError by rememberSaveable { mutableStateOf<String?>(null) }

    val parentEntity = state.categories.firstOrNull { it.categoryId == draft.parentId }


    LaunchedEffect(createResult) {
        nameError = (createResult as? CategoryViewModel2.CreateResult.Error)?.message
    }

    LaunchedEffect(createResult) {
        when (createResult) {
            is CategoryViewModel2.CreateResult.Success -> {
                showAddCategory = false
                showPickParent = false
                showIconPicker = false
                showColorPicker = false
                viewModel.resetDraft()
            }
            else -> Unit
        }
    }


    Scaffold(
        topBar = { TopAppBar(title = { Text("CategoryScreen2") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddCategory = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Add Category")
            }

        },

        // bottomBar فعلاً از Navigation foundation میاد، بعداً یکدستش می‌کنیم
    ) { padding ->

            Column(modifier = Modifier.padding(padding)) {
                val visibleItems = state.renderItems.filter { it.isVisible }

                // ✅ لیست لوکال برای UI (مثل بامبو)
                var dragging by remember { mutableStateOf(false) }

                val listState = remember { mutableStateOf(state.renderItems) }

                // parent قبلی و parent موقت جدید
                val fromParentId = remember { mutableStateOf<Int?>(null) }
                val pendingParentId = remember { mutableStateOf<Int?>(null) }

                val pendingParentById = remember { mutableStateMapOf<Int, Int?>() } // draggedId -> newParentId
                val dragOffsetXById = remember { mutableStateMapOf<Int, Float>() }  // draggedId -> accumulatedX
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

                        val list = listState.value.toMutableList()
                        val fromIndex = from.index
                        val toIndex = to.index.coerceIn(0, list.lastIndex)
                        if (fromIndex !in list.indices || toIndex !in list.indices) return@rememberReorderableLazyListState
                        if (fromIndex == toIndex) return@rememberReorderableLazyListState

                        val toItem = list[toIndex]

                        val moved = list.removeAt(fromIndex)
                        list.add(toIndex, moved)
                        listState.value = list

                        // ✅ parent موقت = parentِ مقصد (عمودی)
                        val targetParent = effectiveParentId(toItem) // همون fun(item)
                        pendingParentById[draggedId] = targetParent
                    }


                    ,
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
                        fromParentId.value = listState.value.firstOrNull { it.category.categoryId == key }?.category?.parentCategoryId
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

                        // ✅ هندل درگ: هم reorder، هم گرفتن dx افقی (بدون consume)
                        val dragHandleModifier = Modifier
                            .pointerInput(id, draggingKey.value) {
                                awaitPointerEventScope {
                                    while (true) {
                                        val event = awaitPointerEvent()
                                        val change = event.changes.firstOrNull() ?: continue

                                        // فقط وقتی همین آیتم واقعاً درگ میشه
                                        if (draggingKey.value == id && change.pressed) {
                                            val dx = change.positionChange().x
                                            if (dx != 0f) {
                                                onHorizontalReparentHint(id, dx)
                                            }
                                        }
                                        // ❌ consume نکن، چون reorder باید کار کنه
                                    }
                                }
                            }
                            .detectReorderAfterLongPress(reorderState)

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
                                                if (draggingKey.value == id && change.pressed) {
                                                    val dx = change.positionChange().x
                                                    if (dx != 0f) onHorizontalReparentHint(id, dx)
                                                }
                                            }
                                        }
                                    }
                                    .detectReorderAfterLongPress(reorderState) // ✅ کل ردیف draggable


                                CategoryRow2(
                                    item = item,
                                    computedLevel = effectiveLevel(id),
                                    onToggleExpand = viewModel::toggleExpand,
                                    modifier = rowDragModifier
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
            parentName = state.categories.firstOrNull { it.categoryId == draft.parentId }?.name ?: "ریشه اصلی",
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
    onEdit: (Int) -> Unit = {},
    onDelete: (Int) -> Unit = {},
) {
    val id = item.category.categoryId ?: return
    val indent = (computedLevel - 1).coerceAtLeast(0) * 16

    val bg = containerColorForLevel(computedLevel)

    ListItem(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = indent.dp),
        colors = ListItemDefaults.colors(
            containerColor = bg
        ),
        leadingContent = {
            Icon(
                imageVector = iconFromKey(item.category.iconName),
                contentDescription = item.category.iconName,
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
                CategoryOverflowMenu(
                    onEdit = { onEdit(id) },
                    onDelete = { onDelete(id) }
                )
            }
        }
    )
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
private fun CategoryOverflowMenu(
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    var open by remember { mutableStateOf(false) }

    Box {
        IconButton(onClick = { open = true }) {
            Icon(Icons.Filled.MoreVert, contentDescription = "menu")
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            DropdownMenuItem(
                text = { Text("Edit") },
                onClick = { open = false; onEdit() }
            )
            DropdownMenuItem(
                text = { Text("Delete") },
                onClick = { open = false; onDelete() }
            )
        }
    }
}
