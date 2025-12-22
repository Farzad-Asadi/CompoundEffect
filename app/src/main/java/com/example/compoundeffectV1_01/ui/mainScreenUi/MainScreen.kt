package com.example.compoundeffectV1_01.ui.mainScreenUi

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.compoundeffectV1_01.ui.categoryScreen.CategoryScreen
import com.example.compoundeffectV1_01.ui.scheduleScreen.ScheduleScreen


@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    viewModel: MainScreenViewModel = hiltViewModel()
) {

    // region متغییرها

    val mainUiState by viewModel.mainUiState.collectAsState()

    var bottomBarIconSelected by rememberSaveable { mutableStateOf("Schedule") }
    var fabClicked by rememberSaveable { mutableStateOf<String?>(null) }

    var showOtherIcons by rememberSaveable { mutableStateOf(false) }

    val pagerStateForBottomBar = rememberPagerState(pageCount = { 2 }, initialPage = 0)
    LaunchedEffect(bottomBarIconSelected) {
        showOtherIcons = false
        when (bottomBarIconSelected) {
            "Schedule" -> {
                pagerStateForBottomBar.animateScrollToPage(0)

            }

            "Category" -> {
                pagerStateForBottomBar.animateScrollToPage(1)

            }
        }
    }


    // endregion

    Scaffold(
        modifier = modifier
            .fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(innerPadding)

            ) {
                TopAppBarContent(
                    modifier = Modifier
                )
                HorizontalPager(
                    state = pagerStateForBottomBar,
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    verticalAlignment = Alignment.Top,
                    userScrollEnabled = false,
                ) { page ->
                    when (page) {
                        0 -> ScheduleScreen(modifier = Modifier)
                        1 -> CategoryScreen(
                            fabClicked = fabClicked,
                            onDismissRequest = { fabClicked = null } ,
                            modifier = Modifier)
                    }
                }
                BottomAppBarContent(
                    bottomBarIconSelected = bottomBarIconSelected,
                    onClickIcons = { bottomBarIconSelected = it ;fabClicked=null },
                    modifier = Modifier
                )
            }
            FloatingActionButtons(
                showOtherIcons = showOtherIcons,
                onClickShowOtherIcon = { showOtherIcons = !showOtherIcons },
                onClickFAB = {fabClicked=it ;showOtherIcons=false},
                itemsForFloatingActionButtonList =
                when (bottomBarIconSelected) {
                    "Schedule" -> {
                        mainUiState.itemsForFloatingActionButtonInSchedule
                    }

                    "Category" -> {
                        mainUiState.itemsForFloatingActionButtonInCategory

                    }

                    else -> {
                        mainUiState.itemsForFloatingActionButtonInSchedule
                    }
                },
                modifier = modifier
                    .align(Alignment.BottomEnd)
                    .offset(y = (-30).dp, x = (-20).dp)
            )

        }

    }
}


@Composable
fun TopAppBarContent(
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceContainer,
                shape = RectangleShape
            )

    ) {

    }

}

@Composable
fun BottomAppBarContent(
    bottomBarIconSelected: String,
    onClickIcons: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceContainer,
                shape = RectangleShape
            ),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically

    ) {

        Icon(
            Icons.Filled.Schedule,
            tint = if (bottomBarIconSelected == "Schedule") {
                MaterialTheme.colorScheme.onSurfaceVariant
            } else {
                MaterialTheme.colorScheme.outlineVariant
            },
            contentDescription = null,
            modifier = Modifier
                .size(66.dp)
                .clickable { onClickIcons("Schedule") }
        )
        Icon(
            Icons.Filled.Category,
            tint = if (bottomBarIconSelected == "Category") {
                MaterialTheme.colorScheme.onSurfaceVariant
            } else {
                MaterialTheme.colorScheme.outlineVariant
            },
            contentDescription = null,
            modifier = Modifier
                .size(66.dp)
                .clickable { onClickIcons("Category") }
        )

    }

}


@Composable
fun FloatingActionButtons(
    showOtherIcons: Boolean,
    onClickShowOtherIcon: () -> Unit,
    onClickFAB: (name:String?) -> Unit,
    itemsForFloatingActionButtonList: List<ItemsForFloatingActionButton>,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // تنظیم مدت زمان انیمیشن‌ها (مثلاً 1000 میلی‌ثانیه یا 1 ثانیه)
    val animationSpecForFade: FiniteAnimationSpec<Float> = tween(durationMillis = 500)
    val animationSpecForExpand: FiniteAnimationSpec<IntSize> = tween(durationMillis = 500)


    LazyColumn(
        modifier = modifier,
        reverseLayout = true,
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.End
    ) {
        item {
            Row(
                modifier = Modifier,
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {

                FloatingActionButton(
                    onClick = { onClickShowOtherIcon() },
                    content = {
                        Icon(
                            imageVector = if (!isPressed) {
                                Icons.Filled.Add
                            } else {
                                Icons.Filled.QuestionMark
                            },
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            contentDescription = null,
                            modifier = Modifier
                                .size(66.dp)
                        )
                    },
                    shape = CircleShape,
                    interactionSource = interactionSource,
                    modifier = Modifier
                        .padding(14.dp),
                )
            }
        }

        items(itemsForFloatingActionButtonList) { itemsForFloatingActionButton ->
            AnimatedVisibility(
                visible = showOtherIcons,
                enter = fadeIn(animationSpec = animationSpecForFade) + expandVertically(
                    animationSpec = animationSpecForExpand
                ),
                exit = fadeOut(animationSpec = animationSpecForFade) + shrinkVertically(
                    animationSpec = animationSpecForExpand
                )
            ) {
                Row(
                    modifier = Modifier,
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    if (itemsForFloatingActionButton.name != null) {
                        Text(
                            text = itemsForFloatingActionButton.name
                        )
                    }
                    FloatingActionButton(
                        onClick = {onClickFAB(itemsForFloatingActionButton.name)},
                        content = {
                            Icon(
                                imageVector = if (itemsForFloatingActionButton.imageVector2 != null) {
                                    if (!isPressed) {
                                        itemsForFloatingActionButton.imageVector1
                                    } else itemsForFloatingActionButton.imageVector2
                                } else itemsForFloatingActionButton.imageVector1,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(66.dp)
                            )
                        },
                        shape = CircleShape,
                        interactionSource = interactionSource,
                        modifier = Modifier
                            .padding(14.dp),
                    )
                }

            }
        }
    }
}

