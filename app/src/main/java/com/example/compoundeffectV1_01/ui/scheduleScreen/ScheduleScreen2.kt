package com.example.compoundeffectV1_01.ui.scheduleScreen

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.compoundeffectV1_01.data.dataBaseRoom.tables.task.Task
import com.example.compoundeffectV1_01.utils.convertToPersianDatePretty
import com.example.compoundeffectV1_01.utils.iconFromKey
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt

@Composable
fun ScheduleScreen2(
    viewModel: ScheduleScreenViewModel = hiltViewModel()
) {
    val allItems by viewModel.allItems.collectAsState()

    val timelineItems = remember(allItems) { allItems.filter { !it.inPallet } }
    val palletItems   = remember(allItems) { allItems.filter { it.inPallet } }

    // ✅ تنظیمات تایم‌لاین
    val numDays = 5
    val startDate by rememberSaveable { mutableStateOf(LocalDate.now()) }

    val verticalZoom by rememberSaveable { mutableFloatStateOf(1f) }
    val horizontalZoom by rememberSaveable { mutableStateOf(1f) }

    val hourHeightDp = (72.dp * verticalZoom).coerceIn(48.dp, 140.dp)
    val dayWidthDp = (220.dp * horizontalZoom).coerceIn(160.dp, 360.dp)

    val vScroll = rememberScrollState()
    val hScroll = rememberScrollState()

    val didAutoScroll = rememberSaveable { mutableStateOf(false) }

    var gridViewportHeightPx by remember { mutableFloatStateOf(0f) }
    val density = LocalDensity.current

    var autoScrollDir by remember { mutableIntStateOf(0) } // -1 بالا، +1 پایین، 0 هیچ
    var autoScrollIntensity by remember { mutableFloatStateOf(0f) } // 0..1 سرعت نسبی

    val edgeThresholdPx = with(density) { 72.dp.toPx() }  // ناحیه حساس
    val baseScrollStepPx = with(density) { 22.dp.toPx() } // سرعت پایه

    var draggingFromPallet by remember { mutableStateOf<ScheduleScreenItem?>(null) }

    var dragX by remember { mutableFloatStateOf(0f) }
    var dragY by remember { mutableFloatStateOf(0f) }

    // برای اینکه بفهمیم Grid کجای صفحه‌ست
    var gridOriginInWindow by remember { mutableStateOf(Offset.Zero) }
    var palletExpanded by rememberSaveable { mutableStateOf(false) }

    var overlayLayerOriginInRoot by remember { mutableStateOf(Offset.Zero) }

    val palletContentWidth = 160.dp
    val palletOverlayWidth = 18.dp + if (palletExpanded) palletContentWidth else 0.dp

    val pendingMove = remember { mutableStateMapOf<Int, PendingMove>() } // key = scheduleId

    var selectedScheduleId by rememberSaveable { mutableStateOf<Int?>(null) }

    val dayWidthPx = with(density) { dayWidthDp.toPx() }
    val hourHeightPx = with(density) { hourHeightDp.toPx() }

    var grabOffsetX by remember { mutableFloatStateOf(0f) }
    var grabOffsetY by remember { mutableFloatStateOf(0f) }

    val overlayItem = draggingFromPallet
    val overlayWidth = dayWidthDp - 12.dp

    val overlayHeight = remember(overlayItem, hourHeightDp) {
        if (overlayItem == null) 48.dp
        else {
            val s = overlayItem.start.toLocalTime().hour * 60 + overlayItem.start.toLocalTime().minute
            val e = overlayItem.end.toLocalTime().hour * 60 + overlayItem.end.toLocalTime().minute
            val durMin = (e - s).coerceAtLeast(15) // حداقل مثل تایم‌لاین
            hourHeightDp * (durMin / 60f)
        }
    }

    var isAutoScrollingActive by remember { mutableStateOf(false) }

    val updateAutoScroll2: (startMin: Int, endMin: Int, mode: AutoScrollMode) -> Unit =
        let@{ startMin, endMin, mode ->

            if (gridViewportHeightPx <= 0f) {
                autoScrollDir = 0
                autoScrollIntensity = 0f
                return@let
            }

            val viewTopPx = vScroll.value.toFloat()
            val viewBottomPx = viewTopPx + gridViewportHeightPx

            fun edgeIntensity(edgeMin: Int): Pair<Int, Float> {
                val yPx = hourHeightPx * (edgeMin / 60f)
                val distTop = yPx - viewTopPx
                val distBottom = viewBottomPx - yPx

                val topHit = distTop < edgeThresholdPx && vScroll.value > 0
                val botHit = distBottom < edgeThresholdPx && vScroll.value < vScroll.maxValue

                val topI = if (topHit) ((edgeThresholdPx - distTop) / edgeThresholdPx).coerceIn(0f, 1f) else 0f
                val botI = if (botHit) ((edgeThresholdPx - distBottom) / edgeThresholdPx).coerceIn(0f, 1f) else 0f

                return if (botI > topI) +1 to botI else -1 to topI
            }

            when (mode) {
                AutoScrollMode.RESIZE_START -> {
                    val (dir, inten) = edgeIntensity(startMin) // ✅ فقط لبه بالا
                    autoScrollDir = if (inten == 0f) 0 else dir
                    autoScrollIntensity = inten
                }

                AutoScrollMode.RESIZE_END -> {
                    val (dir, inten) = edgeIntensity(endMin)   // ✅ فقط لبه پایین
                    autoScrollDir = if (inten == 0f) 0 else dir
                    autoScrollIntensity = inten
                }

                AutoScrollMode.MOVE -> {
                    // ✅ هر دو لبه را می‌سنجیم و هرکدام شدت بیشتری دارد انتخاب می‌شود
                    val (dir1, i1) = edgeIntensity(startMin)
                    val (dir2, i2) = edgeIntensity(endMin)

                    val (dir, inten) = if (i2 > i1) dir2 to i2 else dir1 to i1
                    autoScrollDir = if (inten == 0f) 0 else dir
                    autoScrollIntensity = inten
                }
            }
        }




    LaunchedEffect(isAutoScrollingActive) {
        while (isAutoScrollingActive) {
            val dir = autoScrollDir
            val intensity = autoScrollIntensity

            if (dir != 0) {
                val step = baseScrollStepPx * (0.25f + 0.75f * intensity)
                val prev = vScroll.value
                val next = (prev + dir * step).toInt().coerceIn(0, vScroll.maxValue)

                if (next != prev) {
                    vScroll.scrollTo(next)
                } else {
                    // ✅ hit edge -> pause, don't stop
                    autoScrollDir = 0
                    autoScrollIntensity = 0f
                }
            }

            delay(16)
        }
    }

    LaunchedEffect(timelineItems) {
        timelineItems.forEach { ti ->
            val pm = pendingMove[ti.scheduleId] ?: return@forEach

            val curDate = ti.start.toLocalDate()
            val curStart = ti.start.toLocalTime().hour * 60 + ti.start.toLocalTime().minute
            val curEnd = ti.end.toLocalTime().hour * 60 + ti.end.toLocalTime().minute

            if (curDate == pm.date && curStart == pm.startMin && curEnd == pm.endMin) {
                pendingMove.remove(ti.scheduleId)
            }
        }
    }

    LaunchedEffect(vScroll.maxValue, hourHeightDp, dayWidthDp, startDate) {
        // صبر کن تا ScrollState اندازه‌ها رو بگیره
        if (didAutoScroll.value) return@LaunchedEffect
        if (vScroll.maxValue <= 0) return@LaunchedEffect

        val now = LocalDateTime.now()
        val minutesNow = now.hour * 60 + now.minute

        // y موقعیت خط زمان (px)
        val yPx = (hourHeightPx * (minutesNow / 60f))

        // می‌خوای خط زمان نزدیک بالا باشه ولی نچسبه:
        // مثلاً 1.5 ساعت فاصله از بالا
        val topMarginPx = hourHeightPx * 1.5f

        val targetY = (yPx - topMarginPx).coerceIn(0f, vScroll.maxValue.toFloat())
        vScroll.scrollTo(targetY.toInt())

        // (اختیاری) اگر امروز داخل بازه‌ی 5 روزه هست، افقی هم بیار تو دید
        val dayIndex = ChronoUnit.DAYS.between(startDate, now.toLocalDate()).toInt()
        if (dayIndex in 0 until numDays) {
            val targetX = (dayIndex * dayWidthPx - dayWidthPx * 0.2f)
                .coerceIn(0f, hScroll.maxValue.toFloat())
            hScroll.scrollTo(targetX.toInt())
        }

        didAutoScroll.value = true
    }



    Scaffold(
        modifier = Modifier,
        topBar = {
            CompactTopBar(
                title = "       Schedule TopBar",
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.surface)
                .onGloballyPositioned { coords ->
                    overlayLayerOriginInRoot = coords.positionInRoot()
                }
        ) {

            Column(Modifier.fillMaxSize()) {

                // --- Header (روزها) ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(hScroll)
                        .padding(start = 56.dp, end = palletOverlayWidth) // جای ستون ساعت
                ) {
                    repeat(numDays) { i ->
                        val day = startDate.plusDays(i.toLong())
                        Box(
                            modifier = Modifier
                                .width(dayWidthDp)
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = convertToPersianDatePretty(day),
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
                    }
                }

                HorizontalDivider(thickness = 0.5.dp)

                // --- بدنه: Sidebar ساعت + Grid ---
                Row(Modifier.fillMaxSize()) {

                    // Sidebar ساعت
                    Column(
                        modifier = Modifier
                            .width(56.dp)
                            .verticalScroll(vScroll)
                    ) {
                        repeat(24) { h ->
                            Box(
                                modifier = Modifier
                                    .height(hourHeightDp)
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.TopCenter
                            ) {
                                Text(
                                    text = "%02d:00".format(h),
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }

                    // Grid
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = palletOverlayWidth)
                            .onGloballyPositioned { coords ->
                                gridOriginInWindow = coords.positionInRoot()
                            }
                            .onSizeChanged { size ->
                                gridViewportHeightPx = size.height.toFloat()
                            }
                            .verticalScroll(vScroll)
                            .horizontalScroll(hScroll)
                    ) {
                        val totalHeight = hourHeightDp * 24
                        val totalWidth = dayWidthDp * numDays
                        val colorFor = MaterialTheme.colorScheme.outlineVariant

                        // پس‌زمینه Grid + خطوط
                        Box(
                            modifier = Modifier
                                .width(totalWidth)
                                .height(totalHeight)
                                .drawBehind {
                                    val hourH = hourHeightDp.toPx()
                                    val dayW = dayWidthDp.toPx()

                                    // خطوط افقی ساعت
                                    repeat(24) { i ->
                                        val y = i * hourH
                                        drawLine(
                                            color = colorFor,
                                            start = Offset(0f, y),
                                            end = Offset(size.width, y),
                                            strokeWidth = 1.dp.toPx()
                                        )
                                    }

                                    // خطوط عمودی روزها
                                    repeat(numDays + 1) { i ->
                                        val x = i * dayW
                                        drawLine(
                                            color = colorFor,
                                            start = Offset(x, 0f),
                                            end = Offset(x, size.height),
                                            strokeWidth = 1.dp.toPx()
                                        )
                                    }
                                }
                        ) {

                            // ✅ لایه‌ی خالیِ قابل کلیک (زیر آیتم‌ها)
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clickable(
                                        indication = null,
                                        interactionSource = remember { MutableInteractionSource() }
                                    ) {
                                        selectedScheduleId = null
                                    }
                            )

                            // ✅ خط زمان (اگر امروز داخل بازه بود)
                            CurrentTimeLine(
                                startDate = startDate,
                                numDays = numDays,
                                dayWidthDp = dayWidthDp,
                                hourHeightDp = hourHeightDp
                            )

                            // ✅ آیتم‌ها روی Grid
                            timelineItems.forEach {

                                val pm = pendingMove[it.scheduleId]
                                val displayItem = if (pm == null) it else it.copy(
                                    start = LocalDateTime.of(pm.date, java.time.LocalTime.MIN)
                                        .plusMinutes(pm.startMin.toLong()),
                                    end = LocalDateTime.of(pm.date, java.time.LocalTime.MIN)
                                        .plusMinutes(pm.endMin.toLong())
                                )

                                TimelineItemBox(
                                    item = displayItem,
                                    startDate = startDate,
                                    dayWidthDp = dayWidthDp,
                                    hourHeightDp = hourHeightDp,
                                    numDays = numDays,
                                    selected = (selectedScheduleId == displayItem.scheduleId),
                                    onToggleSelected = {
                                        val newSelection =
                                            if (selectedScheduleId == displayItem.scheduleId) null
                                            else displayItem.scheduleId

                                        selectedScheduleId = newSelection

                                        // ✅ اگر آیتم انتخاب شد، پالت بسته شود
                                        if (newSelection != null) {
                                            palletExpanded = false
                                        }
                                    },
                                    onMoveCommit = { scheduleId, date, s, e ->
                                        pendingMove[scheduleId] = PendingMove(date, s, e)
                                        viewModel.moveSchedule(scheduleId, date, s, e)
                                    },
                                    onResizeEndCommit = { scheduleId, newEnd ->
                                        viewModel.resizeScheduleEnd(scheduleId, newEnd)
                                    },
                                    onResizeStartCommit = { scheduleId, newStart ->
                                        viewModel.resizeScheduleStart(scheduleId, newStart)
                                    },
                                    onSendToPallet = { scheduleId, _ ->
                                        viewModel.moveScheduleFromTimeLineToPallet(scheduleId)
                                        selectedScheduleId = null
                                    },
                                    onAutoScroll = updateAutoScroll2,
                                    onAutoScrollStart = { isAutoScrollingActive = true },
                                    onAutoScrollStop = {
                                        isAutoScrollingActive = false
                                        autoScrollDir = 0
                                        autoScrollIntensity = 0f
                                    },
                                    vScrollValueProvider = { vScroll.value },
                                )
                            }
                        }
                    }
                }
            }

            RightPallet(
                palletItems = palletItems,
                expanded = palletExpanded,
                isDraggingFromPallet = (draggingFromPallet != null),
                onToggle = { palletExpanded = !palletExpanded },

                onDragStart = { item, sx, sy, downX, downY ->
                    palletExpanded = false
                    draggingFromPallet = item

                    // مختصات انگشت در window
                    dragX = sx
                    dragY = sy

                    // فاصله‌ی انگشت تا گوشه‌ی باکس (می‌خوای باکس زیر انگشت بیاد)
                    grabOffsetX = downX
                    grabOffsetY = downY
                },
                onDrag = { dx, dy ->
                    dragX += dx
                    dragY += dy
                },
                onDragEnd = {
                    // همون منطق drop که داشتی
                    val t = draggingFromPallet ?: return@RightPallet

                    // مدت واقعی آیتم (اگر TIME_RANGE دارد)
                    val oldStartMin = t.start.toLocalTime().hour * 60 + t.start.toLocalTime().minute
                    val oldEndMin   = t.end.toLocalTime().hour * 60 + t.end.toLocalTime().minute
                    val oldDurMin   = (oldEndMin - oldStartMin).coerceAtLeast(15) // حداقل مثل تایم‌لاین

                    val localX = dragX - gridOriginInWindow.x
                    val localY = dragY - gridOriginInWindow.y

                    // ✅ تبدیل به مختصات "محتوا" با در نظر گرفتن اسکرول
                    val contentX = localX + hScroll.value
                    val contentY = localY + vScroll.value

                    val dayIndex = (contentX / dayWidthPx).toInt()
                    val minuteOfDay = ((contentY / hourHeightPx) * 60f).toInt()


                    if (dayIndex in 0 until numDays && minuteOfDay in 0 until 24 * 60) {
                        val date = startDate.plusDays(dayIndex.toLong())
                        val minDur = 5
                        val startMin = snap(minuteOfDay.coerceIn(0, 24 * 60 - minDur), 5)
                        val endMin = (startMin + oldDurMin).coerceAtMost(24 * 60)


                        viewModel.dropScheduleFromPalletToTimeLine(
                            scheduleId = t.scheduleId,
                            date = date,
                            startMin = startMin,
                            endMin = endMin
                        )
                    }


                    draggingFromPallet = null
                },
                onDragCancel = { draggingFromPallet = null },

                modifier = Modifier.align(Alignment.CenterEnd)
            )


            // ✅ Drag overlay
            if (draggingFromPallet != null) {
                Box(Modifier.fillMaxSize()) {

                    Box(
                        modifier = Modifier
                            .offset {
                                IntOffset(
                                    (dragX - grabOffsetX - overlayLayerOriginInRoot.x).toInt(),
                                    (dragY - grabOffsetY - overlayLayerOriginInRoot.y).toInt()
                                )
                            }
                            .width(overlayWidth)
                            .height(overlayHeight.coerceAtLeast(44.dp))
                            .background(
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                                shape = MaterialTheme.shapes.medium
                            )
                    ) {
                        // مشابه تایم‌لاین: عنوان و آیکون
                        Row(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(start = 8.dp, top = 8.dp, end = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CategoryIconWithPlate(iconName = draggingFromPallet!!.categoryIconName)
                            Spacer(Modifier.width(8.dp))
                            Text(draggingFromPallet!!.title, maxLines = 1, style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }




        }


    }
}


@Composable
private fun CurrentTimeLine(
    startDate: LocalDate,
    numDays: Int,
    dayWidthDp: Dp,
    hourHeightDp: Dp
) {
    val now = remember { mutableStateOf(LocalDateTime.now()) }

    // هر 60 ثانیه آپدیت
    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000)
            now.value = LocalDateTime.now()
        }
    }

    val today = now.value.toLocalDate()
    val dayIndex = ChronoUnit.DAYS.between(startDate, today).toInt()
    if (dayIndex !in 0 until numDays) return

    val minutes = now.value.toLocalTime().hour * 60 + now.value.toLocalTime().minute
    val y = hourHeightDp * (minutes / 60f)

    val xStart = dayWidthDp * dayIndex
    val xEnd = xStart + dayWidthDp

    Box(
        modifier = Modifier
            .offset(x = xStart, y = y)
            .width(dayWidthDp)
            .height(2.dp)
            .background(MaterialTheme.colorScheme.primary)
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TimelineItemBox(
    item: ScheduleScreenItem,
    startDate: LocalDate,
    dayWidthDp: Dp,
    hourHeightDp: Dp,
    numDays: Int,
    selected: Boolean,
    onToggleSelected: () -> Unit,
    onMoveCommit: (scheduleId: Int, newDate: LocalDate, newStartMin: Int, newEndMin: Int) -> Unit,
    onResizeEndCommit: (scheduleId: Int, newEndMin: Int) -> Unit,
    onResizeStartCommit: (scheduleId: Int, newStartMin: Int) -> Unit,
    onSendToPallet: (scheduleId: Int, taskId: Int) -> Unit,
    onAutoScroll: (startMin: Int, endMin: Int, mode: AutoScrollMode) -> Unit,
    onAutoScrollStart: () -> Unit,
    onAutoScrollStop: () -> Unit,
    vScrollValueProvider: () -> Int,

    ) {

    val density = LocalDensity.current
    val dayWpx = with(density) { dayWidthDp.toPx() }
    val hourHpx = with(density) { hourHeightDp.toPx() }

    val dayIndex0 = ChronoUnit.DAYS.between(startDate, item.start.toLocalDate()).toInt()
    if (dayIndex0 !in 0 until numDays) return

    val minDur = 15
    val startMin0 = item.start.toLocalTime().hour * 60 + item.start.toLocalTime().minute
    val endMin0 = item.end.toLocalTime().hour * 60 + item.end.toLocalTime().minute
    val dur0 = (endMin0 - startMin0).coerceAtLeast(minDur)

    // ---------------- Move state ----------------
    var dragDx by remember(item.scheduleId) { mutableFloatStateOf(0f) }
    var dragDy by remember(item.scheduleId) { mutableFloatStateOf(0f) }


    val dayDelta = (dragDx / dayWpx).toInt()
    val effectiveDragDyForPreview = dragDy
    val minDelta = ((effectiveDragDyForPreview / hourHpx) * 60f).roundToInt()


    val moveDayIndex = (dayIndex0 + dayDelta).coerceIn(0, numDays - 1)
    val moveStartRaw = (startMin0 + minDelta).coerceIn(0, 24 * 60 - minDur)
    val desiredMoveStart = snap(moveStartRaw, 15)
    val desiredMoveEnd = desiredMoveStart + dur0

    var lastScrollPx by remember(item.scheduleId) { mutableIntStateOf(0) }

    val moveRange = clampRange(desiredMoveStart, desiredMoveEnd)
    val moveStart = moveRange.start
    val moveEnd = moveRange.end

    // ---------------- Resize state ----------------
    var topDy by remember(item.scheduleId) { mutableStateOf(0f) }
    var bottomDy by remember(item.scheduleId) { mutableStateOf(0f) }

    val topDeltaMin = ((topDy / hourHpx) * 60f).roundToInt()
    val bottomDeltaMin = ((bottomDy / hourHpx) * 60f).roundToInt()

    val resizeStartRaw = (startMin0 + topDeltaMin)
        .coerceIn(0, (endMin0 - minDur).coerceAtLeast(0))
    val desiredResizeStart = snap(resizeStartRaw, 15)
    val rStart = clampRange(desiredResizeStart, endMin0)
    val resizeStart = rStart.start


    val resizeEndRaw = (endMin0 + bottomDeltaMin)
        .coerceIn((startMin0 + minDur).coerceAtMost(24 * 60), 24 * 60)
    val desiredResizeEnd = snap(resizeEndRaw, 15)
    val rEnd = clampRange(startMin0, desiredResizeEnd)
    val resizeEnd = rEnd.end

    var isMoving by remember(item.scheduleId) { mutableStateOf(false) }
    var isResizingStart by remember(item.scheduleId) { mutableStateOf(false) }
    var isResizingEnd by remember(item.scheduleId) { mutableStateOf(false) }


    val showMovePreview = isMoving // وقتی در حال move هستیم، همیشه preview=move
    val showResizePreview = selected && (isResizingStart || isResizingEnd) // وقتی resize می‌کنیم

    val displayDayIndex = if (showMovePreview) moveDayIndex else dayIndex0

    val displayStart = when {
        showMovePreview -> moveStart
        showResizePreview -> resizeStart
        else -> startMin0
    }

    val displayEnd = when {
        showMovePreview -> moveEnd
        showResizePreview -> resizeEnd
        else -> endMin0
    }


    val x = dayWidthDp * displayDayIndex
    val y = hourHeightDp * (displayStart / 60f)
    val h = hourHeightDp * (((displayEnd - displayStart).coerceAtLeast(minDur)) / 60f)

    val minSelectedHeight = 110.dp

    val taskH = h.coerceAtLeast(44.dp)
    val selectionH = if (selected) maxOf(taskH, minSelectedHeight) else taskH
    val extra = if (selected) (selectionH - taskH) / 2 else 0.dp
    val dashed = selected && selectionH > taskH

    val containerY = (y + 2.dp) - extra


    // ✅ بعد از هر آپدیت از DB، state های drag/resize ریست بشن تا پرش نداشته باشیم
    LaunchedEffect(item.scheduleId, item.start, item.end) {
        dragDx = 0f
        dragDy = 0f
        topDy = 0f
        bottomDy = 0f
    }


    val isAnyDragging = isMoving || isResizingStart || isResizingEnd
    LaunchedEffect(isAnyDragging) {
        if (!isAnyDragging) return@LaunchedEffect

        while (true) {
            // اگر درگ تمام شد، خارج شو
            if (!(isMoving || isResizingStart || isResizingEnd)) break

            val cur = vScrollValueProvider()
            val ds = cur - lastScrollPx
            if (ds != 0) {
                if (isMoving) dragDy += ds.toFloat()
                if (isResizingStart) topDy += ds.toFloat()
                if (isResizingEnd) bottomDy += ds.toFloat()
                lastScrollPx = cur
            }

            delay(16)
        }
    }



    val handleH = 16.dp
    val colorForBorder =MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)

    //  Box اصلی
    Box(
        modifier = Modifier
            .offset(x = x + 6.dp, y = containerY)
            .width(dayWidthDp - 12.dp)
            .height(selectionH)
            .drawBehind {
                if (dashed) {
                    val stroke = Stroke(
                        width = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    )
                    // radius رو با shape کارتت نزدیک می‌گیریم
                    val r = 12.dp.toPx()
                    drawRoundRect(
                        color = colorForBorder,
                        cornerRadius = CornerRadius(r, r),
                        style = stroke
                    )
                }
            }
    ) {


        //  آیکون گروه + نام اسکچول یا تسک
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 8.dp, top = 16.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CategoryIconWithPlate(iconName = item.categoryIconName)
            Spacer(Modifier.width(8.dp))
            Text(
                text = item.title,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1
            )
        }



        //  باکس اصلی
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .width(dayWidthDp - 12.dp)
                .height(taskH)
                .background(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                    shape = MaterialTheme.shapes.medium
                )
        ) {


            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = if (selected) handleH else 0.dp,
                        bottom = if (selected) handleH else 0.dp
                    )
                    .combinedClickable(
                        onClick = onToggleSelected,
                        onLongClick = { /* فقط UX */ }
                    )
                    .pointerInput(item.scheduleId, item.start, item.end, selected) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = {
                                isMoving = true
                                isResizingStart = false
                                isResizingEnd = false
                                lastScrollPx = vScrollValueProvider()
                                onAutoScrollStart()
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                dragDx += dragAmount.x
                                dragDy += dragAmount.y

                                // ✅ محاسبه‌ی لحظه‌ای start/end برای auto-scroll
                                val effectiveDragDy = dragDy
                                val minDeltaNow = ((effectiveDragDy / hourHpx) * 60f).roundToInt()
                                val moveStartRawNow = (startMin0 + minDeltaNow).coerceIn(0, 24 * 60 - minDur)

                                val desiredStart = snap(moveStartRawNow, 15)
                                val desiredEnd = desiredStart + dur0
                                val r = clampRange(desiredStart, desiredEnd)

                                onAutoScroll(r.start, r.end, AutoScrollMode.MOVE)

                            },
                            onDragEnd = {
                                // محاسبه نهایی با dragDx/dragDy فعلی
                                val dayDelta2 = (dragDx / dayWpx).toInt()
                                val effectiveDragDy2 = dragDy
                                val minDelta2 = ((effectiveDragDy2 / hourHpx) * 60f).roundToInt()

                                val moveDayIndex2 = (dayIndex0 + dayDelta2).coerceIn(0, numDays - 1)
                                val moveStartRaw2 =
                                    (startMin0 + minDelta2).coerceIn(0, 24 * 60 - minDur)
                                val moveStart2 = snap(moveStartRaw2, 15)
                                val finalDate = startDate.plusDays(moveDayIndex2.toLong())

                                val desiredStart = snap(moveStartRaw2, 15)
                                val desiredEnd = desiredStart + dur0
                                val r = clampRange(desiredStart, desiredEnd)

                                onMoveCommit(item.scheduleId, finalDate, r.start, r.end)
                                isMoving = false
                                dragDx = 0f
                                dragDy = 0f
                                onAutoScrollStop()

                            },
                            onDragCancel = {
                                isMoving = false
                                dragDx = 0f
                                dragDy = 0f
                                onAutoScrollStop()

                            }
                        )
                    }
                    .padding(start = 8.dp, top = 34.dp, end = 8.dp, bottom = 8.dp)
            ) {


            }



        }




        // ✅ هندل‌ها فقط وقتی selected
        if (selected) {

            // --- Top handle (resize start) ---
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .height(handleH)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f))
                    .pointerInput(item.scheduleId, item.start, item.end) {
                        detectVerticalDragGestures(
                            onDragStart = {
                                isResizingStart = true
                                isMoving = false
                                isResizingEnd = false
                                lastScrollPx = vScrollValueProvider()
                                onAutoScrollStart()
                            },
                            onVerticalDrag = { change, dragAmount ->
                                change.consume()
                                topDy += dragAmount

                                val effectiveTopDy = topDy
                                val topDeltaNow = ((effectiveTopDy / hourHpx) * 60f).roundToInt()
                                val raw = (startMin0 + topDeltaNow)
                                    .coerceIn(DAY_MIN, (endMin0 - MIN_GAP_MIN).coerceAtLeast(DAY_MIN))
                                val desired = snap(raw, 15)
                                val clamped = clampRange(desired, endMin0)

                                onAutoScroll(clamped.start, endMin0, AutoScrollMode.RESIZE_START)

                            },
                            onDragEnd = {
                                // محاسبه نهایی داخل onDragEnd تا stale نشه
                                val effectiveTopDy2 = topDy
                                val topDeltaMin2 = ((effectiveTopDy2 / hourHpx) * 60f).roundToInt()


                                val raw = (startMin0 + topDeltaMin2)
                                    .coerceIn(DAY_MIN, (endMin0 - MIN_GAP_MIN).coerceAtLeast(DAY_MIN))

                                val desired = snap(raw, 15) // یا 15 اگر می‌خوای
                                val clamped = clampRange(desired, endMin0)

                                onResizeStartCommit(item.scheduleId, clamped.start)

                                isResizingStart = false
                                topDy = 0f
                                onAutoScrollStop()
                            }
                            ,
                            onDragCancel = {
                                isResizingStart = false
                                topDy = 0f
                                onAutoScrollStop()
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                // ⬅️ زمان چسبیده به چپ (لحظه‌ای)
                Text(
                    text = minuteToHHmm(displayStart),
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 6.dp)
                )

                // grip وسط
                Box(
                    Modifier
                        .width(38.dp)
                        .height(4.dp)
                        .background(
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f),
                            CircleShape
                        )
                )
            }



            // to pallet
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 6.dp)
                    .size(34.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                        shape = CircleShape
                    )
                    .clickable {
                        onSendToPallet(item.scheduleId, item.taskId)
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward, // یا ArrowForwardIos
                    contentDescription = "Send to pallet",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }


            // --- Bottom handle (resize end) ---
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(handleH)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f))
                    .pointerInput(item.scheduleId, item.start, item.end) {
                        detectVerticalDragGestures(
                            onDragStart = {
                                isResizingEnd = true
                                isMoving = false
                                isResizingStart = false
                                lastScrollPx = vScrollValueProvider()
                                onAutoScrollStart()

                            },
                            onVerticalDrag = { change, dragAmount ->
                                change.consume()
                                bottomDy += dragAmount

                                val effectiveBottomDy = bottomDy
                                val bottomDeltaNow = ((effectiveBottomDy / hourHpx) * 60f).toInt()
                                val raw = (endMin0 + bottomDeltaNow)
                                    .coerceIn((startMin0 + MIN_GAP_MIN).coerceAtMost(DAY_MAX), DAY_MAX)
                                val desired = snap(raw, 15)
                                val clamped = clampRange(startMin0, desired)

                                onAutoScroll(startMin0, clamped.end, AutoScrollMode.RESIZE_END)

                            },
                            onDragEnd = {
                                val effectiveBottomDy2 = bottomDy
                                val bottomDeltaMin2 = ((effectiveBottomDy2 / hourHpx) * 60f).roundToInt()


                                val raw = (endMin0 + bottomDeltaMin2)
                                    .coerceIn((startMin0 + MIN_GAP_MIN).coerceAtMost(DAY_MAX), DAY_MAX)

                                val desired = snap(raw, 15) // یا 15
                                val clamped = clampRange(startMin0, desired)

                                onResizeEndCommit(item.scheduleId, clamped.end)

                                isResizingEnd = false
                                bottomDy = 0f
                                onAutoScrollStop()
                            },
                            onDragCancel = {
                                isResizingEnd = false
                                bottomDy = 0f
                                onAutoScrollStop()
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                // ⬅️ زمان چسبیده به چپ (لحظه‌ای)
                Text(
                    text = minuteToHHmm(displayEnd),
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 6.dp)
                )

                // grip وسط
                Box(
                    Modifier
                        .width(38.dp)
                        .height(4.dp)
                        .background(
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f),
                            CircleShape
                        )
                )
            }


        }
    }
}







@Composable
private fun RightPallet(
    palletItems: List<ScheduleScreenItem>,
    expanded: Boolean,
    isDraggingFromPallet: Boolean,
    onToggle: () -> Unit,
    onDragStart: (ScheduleScreenItem, Float, Float, Float, Float) -> Unit,
    onDrag: (dx: Float, dy: Float) -> Unit,
    onDragEnd: () -> Unit,
    onDragCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val keepContent = expanded || isDraggingFromPallet

    Row(modifier = modifier.fillMaxHeight()) {

        // دکمه باز/بسته
        // دکمه باز/بسته
        Box(
            modifier = Modifier
                .width(18.dp)
                .fillMaxHeight()
                .graphicsLayer { alpha = if (isDraggingFromPallet) 0f else 1f } // ✅ مخفی هنگام درگ
                .clickable(enabled = !isDraggingFromPallet) { onToggle() },     // ✅ کلیک غیرفعال
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (expanded)
                    Icons.AutoMirrored.Filled.ArrowForwardIos
                else
                    Icons.AutoMirrored.Filled.ArrowBackIos,
                contentDescription = null
            )
        }


        // ✅ محتوا را هنگام Drag هم نگه دار
        if (keepContent) {
            // ظاهر را اگر می‌خواهی “بسته” نشان بدهی، عرض را کم کن
            val contentWidth = 160.dp
            val visibleAlpha = if (expanded && !isDraggingFromPallet) 1f else 0f

            Box(
                modifier = Modifier
                    .width(contentWidth)
                    .fillMaxHeight()
                    .graphicsLayer { alpha = visibleAlpha } // ✅ به جای صفر کردن عرض
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    .padding(8.dp)
            ) {
                Column(Modifier.fillMaxSize()) {
                    Text("Pallet", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))

                    LazyColumn {
                        items(palletItems, key = { it.scheduleId }) { t ->
                            PalletTaskItem(
                                item = t,
                                onDragStart = onDragStart,
                                onDrag = onDrag,
                                onDragEnd = onDragEnd,
                                onDragCancel = onDragCancel
                            )
                            HorizontalDivider(thickness = 0.5.dp)
                        }
                    }
                }
            }
        }
    }
}



@Composable
private fun PalletTaskItem(
    item: ScheduleScreenItem,
    onDragStart: (item: ScheduleScreenItem, startWindowX: Float, startWindowY: Float, downX: Float, downY: Float) -> Unit,
    onDrag: (dx: Float, dy: Float) -> Unit,
    onDragEnd: () -> Unit,
    onDragCancel: () -> Unit
) {
    var coords by remember { mutableStateOf<androidx.compose.ui.layout.LayoutCoordinates?>(null) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .background(
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                MaterialTheme.shapes.medium
            )
            .onGloballyPositioned { coords = it } // ✅ همین نود (هم‌سطح pointerInput)
            .pointerInput(item.scheduleId) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { downPos ->
                        val c = coords ?: return@detectDragGesturesAfterLongPress
                        val r = c.localToRoot(downPos)
                        onDragStart(item, r.x, r.y, downPos.x, downPos.y)
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        onDrag(dragAmount.x, dragAmount.y)
                    },
                    onDragEnd = onDragEnd,
                    onDragCancel = onDragCancel
                )
            }
            .padding(10.dp)
    ) {
        Text(item.title, maxLines = 1)
    }
}





@Composable
private fun CategoryIconWithPlate(
    iconName: String?,
    modifier: Modifier = Modifier
) {
    val icon = remember(iconName) { iconFromKey(iconName.orEmpty()) }

    Box(
        modifier = modifier
            .size(26.dp)
            .background(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                shape = RoundedCornerShape(8.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun CompactTopBar(title: String) {
    Box(
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
//            .statusBarsPadding()
            .height(44.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(title, modifier = Modifier.padding(horizontal = 12.dp))
    }
}





//>>>>>>>>>>>>>>>> Utils <<<<<<<<<<<<<<<<<<

private const val MIN_GAP_MIN = 5
private const val DAY_MIN = 0
private const val DAY_MAX = 24 * 60


private enum class AutoScrollMode { MOVE, RESIZE_START, RESIZE_END }

private data class MinRange(val start: Int, val end: Int)

private fun clampRange(start: Int, end: Int): MinRange {
    // Clamp to [0..1440] and keep at least MIN_GAP_MIN
    var s = start.coerceIn(DAY_MIN, DAY_MAX)
    var e = end.coerceIn(DAY_MIN, DAY_MAX)

    if (e - s < MIN_GAP_MIN) {
        // try push end forward first
        val needed = MIN_GAP_MIN - (e - s)
        if (e + needed <= DAY_MAX) e += needed
        else s = (s - needed).coerceAtLeast(DAY_MIN)
    }

    // final safety
    if (e - s < MIN_GAP_MIN) {
        e = (s + MIN_GAP_MIN).coerceAtMost(DAY_MAX)
        s = (e - MIN_GAP_MIN).coerceAtLeast(DAY_MIN)
    }
    return MinRange(s, e)
}

private fun snap(minute: Int, step: Int = 5): Int {
    val m = minute.coerceIn(DAY_MIN, DAY_MAX)
    return ((m + step / 2) / step) * step
}

private fun minuteToHHmm(minute: Int): String {
    val m = minute.coerceIn(0, 24 * 60)
    val h = m / 60
    val mm = m % 60
    return "%02d:%02d".format(h, mm)
}

fun dateAtMinute(date: LocalDate, minute: Int): LocalDateTime {
    val m = minute.coerceIn(0, 24 * 60)
    return date.atStartOfDay().plusMinutes(m.toLong())
}
