package com.example.compoundeffectV1_01.ui.categoryScreen

import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.compoundeffectV1_01.utils.DimmedDialog
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


    var showPickParent by rememberSaveable { mutableStateOf(false) }
    var showAddCategory by rememberSaveable { mutableStateOf(false) }
    var isDragging by remember { mutableStateOf(false) }




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

                    // محدودیت عمق: parent level باید <4 باشد تا child level <=4 شود
                    val parentLevel = state.levelById[prevId] ?: prev.level
                    if (parentLevel >= 4) return

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

                val onHorizontalReparentHint: (Int, Float) -> Unit = { id, deltaX ->
                    val acc = (dragOffsetXById[id] ?: 0f) + deltaX
                    dragOffsetXById[id] = acc

                    if (acc >= threshold) {
                        dragOffsetXById[id] = 0f
                        tryIndent(id)   // ➜ level +1
                    } else if (acc <= -threshold) {
                        dragOffsetXById[id] = 0f
                        tryOutdent(id)  // ➜ level -1
                    }
                }


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
                                CategoryRow2(
                                    item = item,
                                    computedLevel = effectiveLevel(id),
                                    onToggleExpand = viewModel::toggleExpand,
                                    modifier = Modifier.detectReorderAfterLongPress(reorderState),
                                    isDraggingEnabled =(draggingKey.value == id) ,
                                    onHorizontalReparentHint =onHorizontalReparentHint
                                )
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
            onDismiss = {
                showAddCategory = false
                viewModel.resetDraft()
            },
            onPickParent = { showPickParent = true },
            onNameChange = viewModel::setDraftName,
            onConfirm = {
                val ok = viewModel.createCategoryFromDraft()
                if (ok) showAddCategory = false
            }
        )
    }

    if (showAddCategory && showPickParent) {
        PickParentDialogSmall(
            items = state.renderItems,
            levelById = state.levelById,
            onDismiss = { showPickParent = false },
            onPick = { parentId ->
                val ok = viewModel.trySetDraftParent(parentId)
                if (ok) showPickParent = false
            }
        )
    }

}

@Composable
fun AddCategoryDialog2(
    draft: CategoryDraft2,
    parentName: String,
    onDismiss: () -> Unit,
    onPickParent: () -> Unit,
    onNameChange: (String) -> Unit,
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            // Top row (مثل TopBar)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = onDismiss) { Text("Back") }
                Text(text = "گروه جدید", style = MaterialTheme.typography.titleLarge)
                TextButton(onClick = onConfirm) { Text("Confirm") }
            }

            HorizontalDivider(modifier = Modifier.padding(top = 8.dp, bottom = 12.dp))

            OutlinedTextField(
                value = draft.name,
                onValueChange = onNameChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("نام گروه") },
                singleLine = true
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onPickParent() }
                    .padding(vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("والد:", modifier = Modifier.padding(end = 8.dp))
                Text(parentName, style = MaterialTheme.typography.titleMedium)
            }

            HorizontalDivider()

            Text(
                text = "ParentId: ${draft.parentId}",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(top = 10.dp)
            )
        }
    }
}


@Composable
private fun CategoryRow2(
    item: CategoryRenderItem,
    computedLevel: Int,
    onToggleExpand: (Int) -> Unit,
    isDraggingEnabled: Boolean,
    onHorizontalReparentHint: (id: Int, deltaX: Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val indent = (computedLevel - 1).coerceAtLeast(0) * 16


    ListItem(
        modifier = modifier
            .padding(start = indent.dp)
            .pointerInput(isDraggingEnabled) {
                if (!isDraggingEnabled) return@pointerInput
                detectHorizontalDragGestures(
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        val id = item.category.categoryId ?: return@detectHorizontalDragGestures
                        onHorizontalReparentHint(id, dragAmount)
                    }
                )
            },
        headlineContent = { Text(item.category.name) },
        supportingContent = { Text("id=${item.category.categoryId}, level=${item.level}") },
        trailingContent = {
            val id = item.category.categoryId
            val canHaveChildren = computedLevel < 4

            if (id != null && item.hasChildren && canHaveChildren ) {
                IconButton(onClick = { onToggleExpand(id) }) {
                    Icon(
                        imageVector = if (item.isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = "expand"
                    )
                }
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
                    val level = levelById[id] ?: item.level
                    val enabled = level < 4
                    val indent = ((item.level - 1).coerceAtLeast(0) * 14).dp

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = indent, top = 6.dp, bottom = 6.dp)
                            .then(if (enabled) Modifier.clickable { onPick(id) } else Modifier),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = item.category.name,
                            modifier = Modifier.weight(1f),
                            color = if (enabled) LocalContentColor.current
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                        if (!enabled) {
                            Text(
                                text = "حداکثر عمق",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    HorizontalDivider(thickness = 0.5.dp)
                }
            }
        }
    }
}


