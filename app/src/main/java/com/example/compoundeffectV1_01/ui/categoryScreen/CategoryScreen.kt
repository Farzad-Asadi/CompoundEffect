package com.example.compoundeffectV1_01.ui.categoryScreen

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.compoundeffectV1_01.AppViewModelProvider
import com.example.compoundeffectV1_01.data.room.category.CategoryEntity
import com.example.compoundeffectV1_01.utils.LoadingScreen
import com.example.compoundeffectV1_01.utils.colorsOfCategory
import com.example.compoundeffectV1_01.utils.stringToColor
import com.example.compoundeffectV1_01.utils.topic_iconMap
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(
    modifier: Modifier = Modifier,
    fabClicked: String?,
    onDismissRequest: () -> Unit,
    viewModel: CategoryScreenViewModel = viewModel(factory = AppViewModelProvider.factory)
) {
    // region متغییرها
    val categoryUiState by viewModel.categoryUiState.collectAsState()

    val sheetStateForAddCategory = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var showAddCategory by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(fabClicked) {
        if (fabClicked == "گروه بندی") {
            showAddCategory = true
        }
    }
    var showAddParentInAddCategory by rememberSaveable { mutableStateOf(false) }
    var showAddIconInAddCategory by rememberSaveable { mutableStateOf(false) }
    var showAddColorInAddCategory by rememberSaveable { mutableStateOf(false) }
    var showAddDescriptionInAddCategory by rememberSaveable { mutableStateOf(false) }

    val animationSpecForFade: FiniteAnimationSpec<Float> = tween(durationMillis = 500)
    val animationSpecForExpand: FiniteAnimationSpec<IntSize> = tween(durationMillis = 500)
    // endregion




    Box(
        modifier = modifier.fillMaxSize()
    ) {
        if (categoryUiState.isDataLoaded) {

            AnimatedVisibility(
                visible = showAddCategory,
                enter = slideInVertically(
                    initialOffsetY = { it } // شروع از پایین
                ) + fadeIn(animationSpec = animationSpecForFade),
                exit = slideOutVertically(
                    targetOffsetY = { it } // خروج به پایین
                ) + fadeOut(animationSpec = animationSpecForFade)
            ) {
                AddCategory(
                    newCategoryEntity = categoryUiState.newCategoryEntity,
                    categoryEntityList = categoryUiState.categoryEntityList,
                    onClickBack = {
                        showAddCategory =
                            false;onDismissRequest();viewModel.handleAddCategory(back = true)
                    },
                    onClickConfirm = {
                        showAddCategory =
                            false;onDismissRequest();viewModel.handleAddCategory(
                        confirm = true
                    )
                    },
                    onValueChangeName = { viewModel.handleAddCategory(name = it) },
                    onClickChoseParent = { showAddParentInAddCategory = true },
                    onClickChoseSiblingPosition = {viewModel.handleAddCategory(siblingPositionTop = it)},
                    onClickChoseIcon = { showAddIconInAddCategory = true },
                    onClickChoseColor = { showAddColorInAddCategory = true },
                    onClickChoseDescription = { showAddDescriptionInAddCategory = true }
                )
            }

            if (showAddParentInAddCategory) {
                BasicAlertDialog(
                    onDismissRequest = { showAddParentInAddCategory = false },
                    content = {
                        AddParentInAddCategory(
                            sortedCategoryEntityWhitGeneration = categoryUiState.sortedCategoryEntityWhitGeneration,
                            onClickCategoryExpand={categoryId: Int,isExtended :Boolean->
                                viewModel.onClickExpandInCategoryListInCategoryScreen(categoryId,isExtended) },
                            onClickParentInAddParent = {
                                showAddParentInAddCategory = false
                                viewModel.handleAddCategory(parentId = it)
                            }
                        )
                    }
                )
            }
            if (showAddIconInAddCategory) {
                BasicAlertDialog(
                    onDismissRequest = { showAddIconInAddCategory = false },
                    content = {
                        AddIconInAddCategory(
                            onClickOnIcon = {
                                showAddIconInAddCategory = false
                                viewModel.handleAddCategory(icon = it)
                            }
                        )
                    }
                )
            }
            if (showAddColorInAddCategory) {
                BasicAlertDialog(
                    onDismissRequest = { showAddColorInAddCategory = false },
                    content = {
                        AddColorInAddCategory(
                            onClickColor = {
                                showAddColorInAddCategory =
                                    false; viewModel.handleAddCategory(color = it)
                            }
                        )
                    }
                )
            }
            if (showAddDescriptionInAddCategory) {
                BasicAlertDialog(
                    onDismissRequest = { showAddDescriptionInAddCategory = false },
                    content = {
                        AddDescriptionInAddCategory()
                    }
                )
            }

            if (
                !showAddCategory &&
                !showAddParentInAddCategory &&
                !showAddIconInAddCategory &&
                !showAddColorInAddCategory &&
                !showAddDescriptionInAddCategory
            ) {
                CategoryContent(
                    categoryEntityList = categoryUiState.categoryEntityList,
                    sortedCategoryEntityWhitGeneration = categoryUiState.sortedCategoryEntityWhitGeneration,
                    resetKey = categoryUiState.resetKey,
                    categoryOffsetMap = categoryUiState.categoryOffset,
                    onClickCategoryExpand = {categoryId: Int,isExtended :Boolean->
                        viewModel.onClickExpandInCategoryListInCategoryScreen(categoryId,isExtended) },
                    onDragCategoryInList = { categoryEntity: CategoryEntity, offsetX: Float, offsetY: Float, endDrag: Boolean ->
                        viewModel.drugCategoryInCategoryContent(categoryEntity, offsetX, offsetY, endDrag)
                    },
                    onClickDeleteCategory = {viewModel.onClickDeleteCategory(it)}
                )
            }

        } else {
            LoadingScreen(modifier = Modifier)
        }
    }
}


@Composable
fun CategoryContent(
    categoryEntityList: List<CategoryEntity>,
    sortedCategoryEntityWhitGeneration: Map<CategoryEntity, Int>,
    categoryOffsetMap: Map<Int?, Pair<Float, Float>>,
    modifier: Modifier = Modifier,
    onClickCategoryExpand: (categoryId: Int,isExtended :Boolean) -> Unit,
    onDragCategoryInList: (categoryEntity: CategoryEntity, offsetX: Float, offsetY: Float, endDrag: Boolean) -> Unit,
    onClickDeleteCategory : (categoryEntity: CategoryEntity) -> Unit,
    resetKey: Boolean = false,
) {


    val offsetValue = 0.06f

    val itemToShowInList=sortedCategoryEntityWhitGeneration.filterKeys { it.visible }




    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RectangleShape
            )
            .padding(8.dp)
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(itemToShowInList.entries.toList()) { (category, generation) ->

                var offsetX :Float = categoryOffsetMap[category.categoryId]?.first ?:0f
                var offsetY :Float = categoryOffsetMap[category.categoryId]?.second ?:0f

                var categoryMenuExpand by rememberSaveable { mutableStateOf(false) }


                Column(
                    modifier = Modifier
                        .animateItem(fadeInSpec = null, fadeOutSpec = null)


                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .pointerInput(resetKey) {
                                detectDragGesturesAfterLongPress(
                                    onDragStart = {
                                    },
                                    onDrag = { change, dragAmount ->

                                        change.consume()
                                        offsetX += dragAmount.x
                                        offsetY += dragAmount.y
                                        onDragCategoryInList(category, offsetX, offsetY, false)

                                    },
                                    onDragCancel = {
                                        onDragCategoryInList(category, offsetX, offsetY, true)

                                    },
                                    onDragEnd = {
                                        onDragCategoryInList(category, offsetX, offsetY, true)

                                    }
                                )
                            }
                            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Spacer(
                            modifier = Modifier
                                .height(30.dp)
                                .then(
                                    if (generation > 2) Modifier.weight(offsetValue * (generation - 2))
                                    else Modifier
                                )
                                .wrapContentWidth(Alignment.End)


                        )
                        Icon(
                            imageVector = category.icon,
                            tint = category.color.stringToColor(),
                            contentDescription = "icon",
                            modifier = Modifier
                                .size(55.dp)
                                .weight(0.2f)
                        )
                        Text(
                            text = category.name,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .weight(0.5f - (offsetValue * (generation - 1)))
                                .wrapContentHeight(Alignment.CenterVertically),
                        )
                        if (category.expandable){
                            IconButton(
                                modifier = Modifier
                                    .weight(0.2f)
                                    .zIndex(1f),

                                onClick = {
                                    category.categoryId?.let { onClickCategoryExpand(it, category.isExtended) }
                                }
                            ) {
                                Icon(
                                    imageVector = if (!category.isExtended) {
                                        Icons.Filled.ExpandMore
                                    } else {
                                        Icons.Filled.ExpandLess
                                    },
                                    contentDescription = "Expand",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(2.dp)
                                )
                            }
                        }else{
                            Spacer(
                                modifier = Modifier.weight(0.2f),
                            )
                        }



                        VerticalDivider(
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.height(30.dp)
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .wrapContentSize(Alignment.TopStart)
                                .weight(0.1f)
                        ) {
                            IconButton(
                                onClick = { categoryMenuExpand = true }
                            ) {
                                Icon(
                                    Icons.Default.MoreVert,
                                    contentDescription = "Localized description"
                                )
                            }
                            DropdownMenu(
                                expanded = categoryMenuExpand,
                                onDismissRequest = { categoryMenuExpand = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Edit") },
                                    onClick = { /* Handle edit! */ },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Outlined.Edit,
                                            contentDescription = null
                                        )
                                    })
                                DropdownMenuItem(
                                    text = { Text("Delete") },
                                    onClick = { onClickDeleteCategory(category) },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Outlined.Delete,
                                            contentDescription = null
                                        )
                                    })
                                HorizontalDivider()
                            }
                        }


                    }
                    HorizontalDivider(
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 4.dp, end = 12.dp)
                    )

                }


            }
        }
    }
}


@Composable
fun AddCategory(
    newCategoryEntity: CategoryEntity?,
    categoryEntityList: List<CategoryEntity>,
    onClickBack: () -> Unit,
    onClickConfirm: () -> Unit,
    onValueChangeName: (name: String) -> Unit,
    onClickChoseParent: (parentCategoryId: Int) -> Unit,
    onClickChoseSiblingPosition: (siblingPositionTop: Boolean) -> Unit,
    onClickChoseIcon: () -> Unit,
    onClickChoseColor: () -> Unit,
    onClickChoseDescription: () -> Unit,
    modifier: Modifier = Modifier,
) {

    val context = LocalContext.current
    var siblingPositionTop by rememberSaveable { mutableStateOf(false) }
    onClickChoseSiblingPosition(siblingPositionTop)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                shape = RectangleShape
            ),

        ) {
        Row(
            // back - confirm
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.1f)
                .shadow(
                    elevation = 8.dp, // ارتفاع سایه
                    shape = RectangleShape
                )
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RectangleShape
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
        ) {
            IconButton(
                onClick = { onClickBack() },
                modifier = Modifier.weight(0.15f)
            ) {
                Icon(
                    Icons.Filled.ArrowBackIosNew,
                    contentDescription = "Back",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(2.dp)
                )
            }
            Text(
                text = "گروه جدید",
                modifier = Modifier
                    .weight(0.7f),
                fontSize = 22.sp
            )
            IconButton(
                modifier = Modifier.weight(0.15f),
                onClick = {
                    if (newCategoryEntity?.name == "") {
                        Toast.makeText(context, "لطفا یک نام وارد کنید", Toast.LENGTH_SHORT).show()
                    } else {
                        onClickConfirm()
                    }
                }
            ) {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = "Confirm",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(2.dp)
                )
            }
        }
        Row(
            // chose name
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.1f)
                .shadow(
                    elevation = 8.dp,
                    shape = RectangleShape
                )
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RectangleShape
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
        ) {

            Icon(
                Icons.Filled.Category,
                contentDescription = "Category",
                modifier = Modifier
                    .weight(0.15f)
                    .size(30.dp)

            )

            TextField(
                value = newCategoryEntity?.name ?: "",
                onValueChange = { onValueChangeName(it) },
                label = { Text("نام گروه") },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent, // رنگ زمینه هنگام فوکوس
                    unfocusedContainerColor = Color.Transparent, // رنگ زمینه هنگام عدم فوکوس
                    disabledContainerColor = Color.Transparent, // رنگ زمینه هنگام غیرفعال بودن
                    focusedIndicatorColor = Color.Transparent, // خط زیرین هنگام فوکوس
                    unfocusedIndicatorColor = Color.Transparent, // خط زیرین هنگام عدم فوکوس
                    disabledIndicatorColor = Color.Transparent, // خط زیرین هنگام غیرفعال بودن
                    focusedLabelColor = MaterialTheme.colorScheme.primary, // رنگ label هنگام فوکوس
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) // رنگ label هنگام عدم فوکوس
                ),
                modifier = Modifier.weight(0.85f)
            )


        }


        Row(
            //parent-icon-color - description

            modifier = Modifier
                .fillMaxWidth()
                .weight(0.4f)
                .background(
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    shape = RectangleShape
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Column(                     //Icons
                modifier = Modifier
                    .weight(0.15f),
                verticalArrangement = Arrangement.SpaceAround,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(                   //parent Icon
                    imageVector = categoryEntityList.firstOrNull {
                        it.categoryId == newCategoryEntity?.parentCategoryId
                    }?.icon ?: Icons.Filled.AccountTree,
                    tint = categoryEntityList.firstOrNull {
                        it.categoryId == newCategoryEntity?.parentCategoryId
                    }?.color?.stringToColor() ?: Color(0xFF000000),
                    contentDescription = "AccountTree",
                    modifier = Modifier
                        .weight(0.25f)
                        .fillMaxWidth(0.6f)
                        .clickable {
                            val parentCategoryId = categoryEntityList.firstOrNull {
                                it.categoryId == newCategoryEntity?.parentCategoryId
                            }?.categoryId ?: 1
                            onClickChoseParent(parentCategoryId)

                        }
                )
                Icon(                   //icon icon
                    imageVector = newCategoryEntity?.icon ?: Icons.Filled.QuestionMark,
                    tint = newCategoryEntity?.color?.stringToColor() ?: Color(0xFF000000),
                    contentDescription = "Route",
                    modifier = Modifier
                        .weight(0.25f)
                        .fillMaxWidth(0.6f)
                        .clickable { onClickChoseIcon() }
                )
                Icon(                   //color icon
                    imageVector = Icons.Filled.Circle,
                    tint = newCategoryEntity?.color?.stringToColor() ?: Color(0xFF000000),
                    contentDescription = "Circle",
                    modifier = Modifier
                        .weight(0.25f)
                        .fillMaxWidth(0.6f)
                        .clickable { onClickChoseColor() }
                )
                Icon(                   //add description icon
                    Icons.Filled.Description,
                    contentDescription = "Description",
                    modifier = Modifier
                        .weight(0.25f)
                        .fillMaxWidth(0.6f)
                        .clickable { onClickChoseDescription() }
                )
            }
            Column(                       //name of Icons
                modifier = Modifier
                    .weight(0.85f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Row(
                    // chose parent
                    modifier = Modifier
                        .weight(0.25f)
                        .fillMaxWidth()
                        ,
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = categoryEntityList.firstOrNull {
                            it.categoryId == newCategoryEntity?.parentCategoryId
                        }?.name ?: "ریشه اصلی",
                        modifier = Modifier
                            .weight(0.6f)
                            .clickable {
                                val parentCategoryId = categoryEntityList.firstOrNull {
                                    it.categoryId == newCategoryEntity?.parentCategoryId
                                }?.categoryId ?: 1
                                onClickChoseParent(parentCategoryId)
                            },
                        fontSize = 22.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "والد",
                        modifier = Modifier
                            .padding(end = 18.dp)
                            .weight(0.2f),
                        maxLines = 1,
                        fontSize = 22.sp
                    )
                    VerticalDivider()
                    IconButton(
                        modifier = Modifier,
                        onClick = {siblingPositionTop=!siblingPositionTop ; onClickChoseSiblingPosition(siblingPositionTop)}
                    ) {
                        Icon(
                            imageVector = if (siblingPositionTop)Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward,
                            contentDescription = "siblingPosition",
                            modifier = Modifier
                                .weight(0.2f)
                                .fillMaxWidth(0.6f)
                        )
                    }

                }
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .padding(start = 4.dp, end = 12.dp)
                        .background(
                            color = MaterialTheme.colorScheme.outlineVariant,
                            shape = RectangleShape
                        ),
                )
                Text(
                    text = "آیکون",
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.25f)
                        .padding(end = 22.dp)
                        .clickable { onClickChoseIcon() }
                        .wrapContentHeight(Alignment.CenterVertically),
                    fontSize = 22.sp

                )
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .padding(start = 4.dp, end = 12.dp)
                        .background(
                            color = MaterialTheme.colorScheme.outlineVariant,
                            shape = RectangleShape
                        ),
                )
                Text(
                    text = "رنگ",
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.25f)
                        .padding(end = 22.dp)
                        .clickable { onClickChoseColor() }
                        .wrapContentHeight(Alignment.CenterVertically),
                    fontSize = 22.sp
                )
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .padding(start = 4.dp, end = 12.dp)
                        .background(
                            color = MaterialTheme.colorScheme.outlineVariant,
                            shape = RectangleShape
                        ),
                )
                Text(
                    text = "اضافه کردن توضیحات",
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.25f)
                        .padding(end = 22.dp)
                        .clickable { onClickChoseDescription() }
                        .wrapContentHeight(Alignment.CenterVertically),
                    fontSize = 22.sp
                )
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .padding(start = 4.dp, end = 12.dp)
                        .background(
                            color = MaterialTheme.colorScheme.outlineVariant,
                            shape = RectangleShape
                        ),
                )
            }
        }
        Spacer(
            modifier = Modifier
                .fillMaxSize()
                .weight(0.5f),
        )

    }

}

@Composable
fun AddParentInAddCategory(
    sortedCategoryEntityWhitGeneration: Map<CategoryEntity, Int>,
    onClickCategoryExpand: (categoryId: Int,isExtended :Boolean) -> Unit,
    onClickParentInAddParent: (categoryId: Int) -> Unit,
    modifier: Modifier = Modifier,
) {

    val offsetValue = 0.06f
    val itemToShowInList=sortedCategoryEntityWhitGeneration.filterKeys { it.visible }

    Column(
        modifier = modifier
            .fillMaxWidth(0.9f)
            .fillMaxHeight(0.8f)
            .background(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(12.dp)
            ),
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RectangleShape
                )
                .padding(8.dp),
        ) {

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(itemToShowInList.entries.toList()) { (category, generation) ->

                    Column(
                        modifier = Modifier
                            .animateItem(fadeInSpec = null, fadeOutSpec = null)


                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { category.categoryId?.let { onClickParentInAddParent(it) } },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Spacer(
                                modifier = Modifier
                                    .height(30.dp)
                                    .then(
                                        if (generation > 2) Modifier.weight(offsetValue * (generation - 2))
                                        else Modifier
                                    )
                                    .wrapContentWidth(Alignment.End)


                            )
                            Icon(
                                imageVector = category.icon,
                                tint = category.color.stringToColor(),
                                contentDescription = "icon",
                                modifier = Modifier
                                    .size(55.dp)
                                    .weight(0.2f)
                            )
                            Text(
                                text = category.name,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier
                                    .weight(0.5f - (offsetValue * (generation - 1)))
                                    .wrapContentHeight(Alignment.CenterVertically),
                            )
                            if (category.expandable){
                                IconButton(
                                    modifier = Modifier
                                        .weight(0.2f)
                                        .zIndex(1f),

                                    onClick = {
                                        category.categoryId?.let { onClickCategoryExpand(it, category.isExtended) }
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (!category.isExtended) {
                                            Icons.Filled.ExpandMore
                                        } else {
                                            Icons.Filled.ExpandLess
                                        },
                                        contentDescription = "Expand",
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(2.dp)
                                    )
                                }
                            }else{
                                Spacer(
                                    modifier = Modifier.weight(0.2f),
                                )
                            }


                        }
                        HorizontalDivider(
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 4.dp, end = 12.dp)
                        )

                    }


                }
            }



        }
    }
}

@Composable
fun LazyGirdItemsForCategoryContent(
    categoryEntity: CategoryEntity,
    subCategoryCanExpand: Boolean?,
    categoryExpand: Boolean,
    onClickCategoryExpand: () -> Unit,
    modifier: Modifier = Modifier,
    resetKey: Boolean = false,
    categoryOffsetPair: Pair<Float, Float>? = null,
    categoryTreePosition: Int = 1,
    onDragCategoryInList: ((categoryEntity: CategoryEntity, offsetX: Float, offsetY: Float, endOfChangBranch: Boolean, endDrag: Boolean) -> Unit)? = null,
    onClickParentInAddParent: ((categoryId: Int) -> Unit)? = null,

    ) {


    var categoryMenuExpand by rememberSaveable { mutableStateOf(false) }


    val offsetValue = 0.01f
    val weightOffset = when (categoryTreePosition) {
        1 -> 0f
        2 -> offsetValue
        3 -> offsetValue * 2
        4 -> offsetValue * 3
        5 -> offsetValue * 4
        else -> 0f
    }

    var offsetX = categoryOffsetPair?.first
    var offsetY = categoryOffsetPair?.second




    Column(

        modifier = modifier
            .fillMaxSize()

            .pointerInput(resetKey) {
                if (onDragCategoryInList != null) {
                    if (offsetX != null && offsetY != null) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = {},
                            onDrag = { change, dragAmount ->

                                change.consume()
                                offsetX += dragAmount.x
                                offsetY += dragAmount.y

                                onDragCategoryInList(
                                    categoryEntity,
                                    offsetX,
                                    offsetY,
                                    subCategoryCanExpand ?: true,
                                    false
                                )

                            },
                            onDragCancel = {

                                onDragCategoryInList(
                                    categoryEntity,
                                    offsetX,
                                    offsetY,
                                    subCategoryCanExpand ?: true,
                                    true
                                )

                            },
                            onDragEnd = {
                                onDragCategoryInList(
                                    categoryEntity,
                                    offsetX,
                                    offsetY,
                                    subCategoryCanExpand ?: true,
                                    true
                                )

                            }
                        )
                    }
                }
            }


    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    if (onClickParentInAddParent != null) {
                        categoryEntity.categoryId?.let { onClickParentInAddParent(it) }
                    }
                },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            if (categoryTreePosition >= 2) {
                VerticalDivider(
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier
                        .height(30.dp)
                        .weight(0.1f + weightOffset)
                        .wrapContentWidth(Alignment.End)
                )
            }
            if (categoryTreePosition >= 3) {
                VerticalDivider(
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier
                        .height(30.dp)
                        .weight(0.1f + weightOffset)
                        .wrapContentWidth(Alignment.End)
                )
            }
            if (categoryTreePosition >= 4) {
                VerticalDivider(
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier
                        .height(30.dp)
                        .weight(0.1f + weightOffset)
                        .wrapContentWidth(Alignment.End)
                )
            }
            if (categoryTreePosition >= 5) {
                VerticalDivider(
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier
                        .height(30.dp)
                        .weight(0.1f + weightOffset)
                        .wrapContentWidth(Alignment.End)
                )
            }
            Icon(
                imageVector = categoryEntity.icon,
                tint = categoryEntity.color.stringToColor(),
                contentDescription = "icon",
                modifier = Modifier
                    .size(55.dp)
                    .weight(0.2f)


            )
            Text(
                text = categoryEntity.name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(0.4f - weightOffset)
                    .wrapContentHeight(Alignment.CenterVertically),
            )
            IconButton(
                enabled = subCategoryCanExpand ?: false,
                modifier = Modifier.weight(0.2f),
                onClick = { onClickCategoryExpand() }
            ) {
                Icon(
                    imageVector = if (!categoryExpand) {
                        Icons.Filled.ExpandMore
                    } else {
                        Icons.Filled.ExpandLess
                    },
                    contentDescription = "Expand",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(2.dp)
                )
            }

            if (onClickParentInAddParent == null) {
                VerticalDivider(
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.height(30.dp)
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentSize(Alignment.TopStart)
                        .weight(0.1f)
                ) {
                    IconButton(
                        onClick = { categoryMenuExpand = true }
                    ) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "Localized description"
                        )
                    }
                    DropdownMenu(
                        expanded = categoryMenuExpand,
                        onDismissRequest = { categoryMenuExpand = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            onClick = { /* Handle edit! */ },
                            leadingIcon = {
                                Icon(
                                    Icons.Outlined.Edit,
                                    contentDescription = null
                                )
                            })
                        DropdownMenuItem(
                            text = { Text("Settings") },
                            onClick = { /* Handle settings! */ },
                            leadingIcon = {
                                Icon(
                                    Icons.Outlined.Settings,
                                    contentDescription = null
                                )
                            })
                        HorizontalDivider()
                    }
                }
            }


        }
        HorizontalDivider(
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, end = 12.dp)
        )

    }
}

@Composable
fun AddIconInAddCategory(
    onClickOnIcon: (icon: ImageVector) -> Unit,
    modifier: Modifier = Modifier,
) {
    val topicIconMap = topic_iconMap.minus("ForAppOnly")


    Column(
        modifier = modifier
            .fillMaxWidth(0.9f)
            .background(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(12.dp)
            ),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "انتخاب آیکون",
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, end = 14.dp),
            textAlign = TextAlign.End,
            fontSize = 20.sp
        )

        LazyColumn(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxHeight(),
        ) {
            items(topicIconMap.toList()) { (topic, iconMap) ->


                var isExpanded by rememberSaveable { mutableStateOf(false) }
                val imageVectorsListForThisTopic = iconMap.values.toList()



                Column(
                    modifier = Modifier.fillMaxHeight(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = topic,
                            modifier = Modifier.weight(0.7f),
                            fontSize = 16.sp
                        )
                        IconButton(
                            modifier = Modifier.weight(0.3f),
                            onClick = { isExpanded = !isExpanded }
                        ) {
                            Icon(
                                imageVector = if (!isExpanded) {
                                    Icons.Filled.ExpandMore
                                } else {
                                    Icons.Filled.ExpandLess
                                },
                                contentDescription = "Expand",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(2.dp)
                            )
                        }
                    }
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .padding(start = 4.dp, end = 12.dp)
                            .background(
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                shape = RectangleShape
                            ),
                    )
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 50.dp, max = 500.dp)

                    ) {
                        if (isExpanded) {
                            items(imageVectorsListForThisTopic) {
                                IconButton(
                                    modifier = Modifier,
                                    onClick = { onClickOnIcon(it) }
                                ) {
                                    Icon(
                                        imageVector = it,
                                        contentDescription = it.name,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(2.dp),
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        } else {
                            items(imageVectorsListForThisTopic.take(4)) {
                                IconButton(
                                    modifier = Modifier,
                                    onClick = { onClickOnIcon(it) }
                                ) {
                                    Icon(
                                        imageVector = it,
                                        contentDescription = it.name,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(2.dp),
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }

                    }
                }
            }

        }


    }
}

@Composable
fun AddColorInAddCategory(
    onClickColor: (color: Color) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth(0.9f)
            .fillMaxHeight(0.8f)
            .background(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(12.dp)
            ),
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 50.dp, max = 700.dp)

        ) {
            items(colorsOfCategory) { color ->

                IconButton(
                    modifier = Modifier.weight(0.3f),
                    onClick = { onClickColor(color) }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Circle,
                        tint = color,
                        contentDescription = "Expand",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(2.dp)
                    )
                }

            }

        }


    }
}

@Composable
fun AddDescriptionInAddCategory(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth(0.9f)
            .fillMaxHeight(0.8f)
            .background(
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                shape = RoundedCornerShape(12.dp)
            ),
    ) {

    }
}

