package com.example.compoundeffectV1_01.ui.scheduleScreen

import android.Manifest
import android.app.Activity
import android.app.AlarmManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.compoundeffectV1_01.data.dataBaseRoom.tables.taskSchedule.RepeatUnit
import com.example.compoundeffectV1_01.data.dataBaseRoom.tables.taskSchedule.ScheduleMode
import com.example.compoundeffectV1_01.data.dataStore.AppPreferences
import com.example.compoundeffectV1_01.ui.navigation.AppRoutes
import com.example.compoundeffectV1_01.utils.convertToPersianDatePretty
import com.example.compoundeffectV1_01.utils.iconFromKey
import com.example.compoundeffectV1_01.utils.requestExactAlarmPermission
import com.example.compoundeffectV1_01.utils.scheduleModeColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import kotlin.math.round
import kotlin.math.roundToInt

@Composable
fun ScheduleScreen(
    navController: NavHostController,
    viewModel: ScheduleScreenViewModel = hiltViewModel()
) {
    val allItems by viewModel.allItems.collectAsState()

    val runningPomodoro by viewModel.runningPomodoro.collectAsState()


    val timelineItemsReal = remember(allItems) { allItems.filter { !it.inPallet } }

    val overrideKeys = remember(timelineItemsReal) {
        // key = "ruleId|occurrenceDay"
        timelineItemsReal
            .asSequence()
            .filter { it.parentRuleScheduleId != null && it.occurrenceDateEpochDay != null }
            .map { "${it.parentRuleScheduleId}|${it.occurrenceDateEpochDay}" }
            .toHashSet()
    }

    val numDays = 5
    val startDate = remember { LocalDate.now() } // یا هر startDate که تایم‌لاین‌ات دارد
    val endDate = remember(startDate) { startDate.plusDays((numDays - 1).toLong()) }

    val today = remember { LocalDate.now() }

    val timelineItems = remember(timelineItemsReal, startDate, endDate, overrideKeys) {
        val existingKeys = HashSet<String>(timelineItemsReal.size * 2)

        fun keyOf(taskId: Int, dateEpochDay: Long, startMin: Int, endMin: Int, mode: ScheduleMode): String =
            "$taskId|$dateEpochDay|$startMin|$endMin|$mode"

        // کلیدهای آیتم‌های واقعی (برای جلوگیری از دوبل)
        timelineItemsReal.forEach { it ->
            val d = it.dateEpochDay ?: return@forEach
            val sMin = it.start.toLocalTime().hour * 60 + it.start.toLocalTime().minute
            val eMin = it.end.toLocalTime().hour * 60 + it.end.toLocalTime().minute
            existingKeys += keyOf(it.taskId, d, sMin, eMin, it.mode)
        }

        val repeatingRules = timelineItemsReal
            .asSequence()
            .filter { it.repeating && it.repeatUnit != null }
            .toList()

        val virtuals = ArrayList<ScheduleScreenItem>(64)

        var d = startDate
        while (!d.isAfter(endDate)) {
            val dEpoch = d.toEpochDay()

            repeatingRules.forEach { rule ->
                val baseDate = rule.dateEpochDay?.let(LocalDate::ofEpochDay) ?: return@forEach
                if (d == baseDate) return@forEach

                // ✅ اگر برای این روز override ثبت شده، occurrence مجازی نساز
                val ovKey = "${rule.scheduleId}|$dEpoch"
                if (overrideKeys.contains(ovKey)) return@forEach

                // ✅ فعال بودن rule در این روز
                if (!ruleIsActiveOnDate(rule, d)) return@forEach

                val sMin = rule.start.toLocalTime().hour * 60 + rule.start.toLocalTime().minute
                val eMin = rule.end.toLocalTime().hour * 60 + rule.end.toLocalTime().minute
                if (eMin <= sMin) return@forEach

                // ✅ اگر آیتم واقعی همون بازه وجود داره، occurrence مجازی نساز
                val k = keyOf(rule.taskId, dEpoch, sMin, eMin, rule.mode)
                if (existingKeys.contains(k)) return@forEach

                // ✅ یک ID منفی برای آیتم مجازی
                val virtualId =
                    if (d == today) rule.scheduleId
                    else -kotlin.math.abs((rule.scheduleId * 31) xor (dEpoch.toInt() * 17)).coerceAtLeast(1)

                virtuals += rule.copy(
                    scheduleId = virtualId,     // منفی یعنی مجازی
                    dateEpochDay = dEpoch,
                    start = d.atStartOfDay().plusMinutes(sMin.toLong()),
                    end = d.atStartOfDay().plusMinutes(eMin.toLong()),
                    repeating = false,          // این خود rule نیست، occurrence است
                    inPallet = false,

                    // ✅ مهم: لینک دادن به rule
                    parentRuleScheduleId = rule.scheduleId,
                    occurrenceDateEpochDay = dEpoch
                )

                existingKeys += k
            }

            d = d.plusDays(1)
        }

        timelineItemsReal + virtuals
    }

    val palletItems = remember(allItems) {
        allItems.filter { it.inPallet && it.mode != ScheduleMode.POMODORO }
    }




    // ✅ rule های پومودورو (برای expectedToday و config)
    val pomodoroPARENTRulesToday = remember(allItems) {
        allItems.filter { it.mode == ScheduleMode.POMODORO && it.repeating }
            .filter { ruleIsActiveToday(it, today) } // تابع پایین رو اضافه می‌کنیم
    }

    // ✅ پومودوهای تایم‌لاینِ امروز (برای scheduledToday)
    val todayTimelinePomodoros = remember(allItems) {
        allItems.filter { !it.inPallet && it.mode == ScheduleMode.POMODORO }
            .filter { it.start.toLocalDate() == today }
    }

    // ✅ کارت‌های تجمیعی پالت
    val pomodoroPalletCards = remember(pomodoroPARENTRulesToday, todayTimelinePomodoros) {
        pomodoroPARENTRulesToday
            .groupBy { it.taskId }
            .mapNotNull { (taskId, rules) ->
                val any = rules.first()

                val expected = rules.sumOf { (it.pomodoroUnitsPerDay ?: 1).coerceAtLeast(1) }
                val scheduled = todayTimelinePomodoros.count { it.taskId == taskId }

                val remaining = (expected - scheduled).coerceAtLeast(0)
                if (remaining == 0) return@mapNotNull null

                PomodoroPalletCardItem(
                    taskId = taskId,
                    scheduleId =any.scheduleId ,
                    taskName = any.title,

                    totalTarget = any.pomodoroTargetUnits ?: 0,
                    totalDone = any.pomodoroDoneUnits,

                    expectedToday = expected,
                    scheduledToday = scheduled,

                    focus = any.focusMinutes ?: 25,
                    shortBreak = any.shortBreakMinutes ?: 5,
                    longBreak = any.longBreakMinutes ?: 15,
                    longBreakEvery = any.longBreakEvery ?: 4,

                    remainingToday = remaining
                )
            }
            .sortedBy { it.taskName }
    }

    // مقدار ذخیره‌شونده برای اینکه بعد rotate/process-death هم برگرده
    var verticalZoomSaved by rememberSaveable { mutableFloatStateOf(1f) }

    // زوم واقعیِ در حال pinch (پیوسته)
    var verticalZoomRaw by remember { mutableFloatStateOf(verticalZoomSaved) }

    // زوم نمایشی (پله‌ای ولی نرم)
    val verticalZoomAnim = remember { androidx.compose.animation.core.Animatable(verticalZoomSaved) }

    // مقدار نهایی که بقیه‌ی UI باید ببینه
    val verticalZoom = verticalZoomAnim.value

    val horizontalZoom by rememberSaveable { mutableFloatStateOf(1f) }

    val hourHeightDp = (72.dp * verticalZoom).coerceIn(48.dp, 360.dp)
    val dayWidthDp = (220.dp * horizontalZoom).coerceIn(160.dp, 360.dp)

    val vScroll = rememberScrollState()
    val hScroll = rememberScrollState()

    val didAutoScroll = rememberSaveable { mutableStateOf(false) }

    var pendingScrollTo by remember { mutableStateOf<Int?>(null) }

    var pendingScrollByPx by remember { mutableFloatStateOf(0f) }

    var gridViewportHeightPx by remember { mutableFloatStateOf(0f) }
    val density = LocalDensity.current

    var autoScrollDir by remember { mutableIntStateOf(0) } // -1 بالا، +1 پایین، 0 هیچ
    var autoScrollIntensity by remember { mutableFloatStateOf(0f) } // 0..1 سرعت نسبی

    val edgeThresholdPx = with(density) { 72.dp.toPx() }  // ناحیه حساس
    val baseScrollStepPx = with(density) { 22.dp.toPx() } // سرعت پایه

    val scope = rememberCoroutineScope()

    var dragX by remember { mutableFloatStateOf(0f) }
    var dragY by remember { mutableFloatStateOf(0f) }

    // برای اینکه بفهمیم Grid کجای صفحه‌ست
    var gridOriginInWindow by remember { mutableStateOf(Offset.Zero) }
    var palletExpanded by rememberSaveable { mutableStateOf(false) }

    var overlayLayerOriginInRoot by remember { mutableStateOf(Offset.Zero) }

    val palletContentWidth = 160.dp
    val palletHandleWidth = 48.dp
    val palletOverlayWidth = palletHandleWidth + if (palletExpanded) palletContentWidth else 0.dp

    val pendingMove = remember { mutableStateMapOf<Int, PendingMove>() } // key = scheduleId

    var selectedScheduleId by rememberSaveable { mutableStateOf<Int?>(null) }

    val dayWidthPx = with(density) { dayWidthDp.toPx() }
    val hourHeightPx = with(density) { hourHeightDp.toPx() }

    var grabOffsetX by remember { mutableFloatStateOf(0f) }
    var grabOffsetY by remember { mutableFloatStateOf(0f) }

    var draggingFromPallet by remember { mutableStateOf<ScheduleScreenItem?>(null) }

    val overlayItem = draggingFromPallet
    val overlayWidth = dayWidthDp - 12.dp

    val overlayHeight = remember(overlayItem, hourHeightDp) {
        if (overlayItem == null) 48.dp
        else {
            val s =
                overlayItem.start.toLocalTime().hour * 60 + overlayItem.start.toLocalTime().minute
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

                val topI = if (topHit) ((edgeThresholdPx - distTop) / edgeThresholdPx).coerceIn(
                    0f,
                    1f
                ) else 0f
                val botI = if (botHit) ((edgeThresholdPx - distBottom) / edgeThresholdPx).coerceIn(
                    0f,
                    1f
                ) else 0f

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


    var pendingBringDayIntoView by remember { mutableStateOf<Int?>(null) }


    var draggingPomodoro by remember { mutableStateOf<PomodoroPalletCardItem?>(null) }


    val isDraggingAnything = (draggingFromPallet != null || draggingPomodoro != null)

    val overlayScheduleItem = draggingFromPallet
    val overlayPomodoroItem = draggingPomodoro


    var pomodoroAdjust by remember { mutableStateOf<PomodoroAdjustState?>(null) }

    // 1) اول آیتم‌های نمایشی (با pendingMove) را بساز
    val displayTimelineItems = remember(timelineItems, pendingMove) {
        timelineItems.map { it ->
            val pm = pendingMove[it.scheduleId]
            if (pm == null) it
            else it.copy(
                start = pm.date.atStartOfDay().plusMinutes(pm.startMin.toLong()),
                end = pm.date.atStartOfDay().plusMinutes(pm.endMin.toLong())
            )
        }
    }

// 2) بعد layout های همپوشانی را حساب کن (فقط داخل روز)
    val overlapLayouts = remember(displayTimelineItems, startDate, numDays) {
        computeOverlapLayouts(displayTimelineItems, startDate, numDays)
    }
    val context = LocalContext.current

    val activity = context as Activity

    val hasAskedSchedulePermissions by viewModel.hasAskedSchedulePermissions.collectAsState()

    var showPermissionDialog by rememberSaveable { mutableStateOf(false) }

    val alarmManager = remember(context) {
        context.getSystemService(AlarmManager::class.java)
    }

    fun hasNotificationPermission(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
    }

    fun hasExactAlarmPermission(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
                alarmManager.canScheduleExactAlarms()
    }

    val openExactAlarmSettings = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            activity.requestExactAlarmPermission()
        }
    }

    val notificationPermissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) {
            openExactAlarmSettings()
        }

    LaunchedEffect(hasAskedSchedulePermissions) {
        val asked = hasAskedSchedulePermissions ?: return@LaunchedEffect

        val needsSomething =
            !hasNotificationPermission() || !hasExactAlarmPermission()

        if (!asked && needsSomething) {
            showPermissionDialog = true
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

    LaunchedEffect(pendingBringDayIntoView, dayWidthDp, numDays) {
        val day = pendingBringDayIntoView ?: return@LaunchedEffect
        // یکم حاشیه بده که روز کامل دیده شه
        val targetX = (day * dayWidthPx - dayWidthPx * 0.2f)
            .coerceIn(0f, hScroll.maxValue.toFloat())
        hScroll.animateScrollTo(targetX.toInt())
        pendingBringDayIntoView = null
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

    LaunchedEffect(pendingScrollTo) {
        val t = pendingScrollTo ?: return@LaunchedEffect
        vScroll.scrollTo(t)
        pendingScrollTo = null
    }

    LaunchedEffect(pendingScrollByPx) {
        val d = pendingScrollByPx
        if (d != 0f) {
            vScroll.scrollBy(d)
            pendingScrollByPx = 0f
        }
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
                Box(Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                awaitEachGesture {
                                    awaitFirstDown(pass = PointerEventPass.Initial)

                                    var lastDistance = 0f
                                    var lastStepTarget = verticalZoomAnim.value
                                    var animJob: kotlinx.coroutines.Job? = null

                                    while (true) {
                                        val event =
                                            awaitPointerEvent(pass = PointerEventPass.Initial)
                                        val pressed = event.changes.filter { it.pressed }
                                        if (pressed.size < 2) break

                                        val p1 = pressed[0].position
                                        val p2 = pressed[1].position
                                        val focus = (p1 + p2) / 2f
                                        val dist = (p1 - p2).getDistance()
                                        if (dist == 0f) continue

                                        if (lastDistance != 0f) {
                                            val zoomChange = dist / lastDistance

                                            // 1) rawZoom پیوسته
                                            val newRaw = (verticalZoomRaw * zoomChange).coerceIn(
                                                ZOOM_MIN,
                                                ZOOM_MAX
                                            )
                                            verticalZoomRaw = newRaw

                                            // 2) پله‌ی هدف
                                            val targetStep = snapZoom(newRaw)

                                            // 3) فقط وقتی پله عوض شد، نرم animate کن
                                            if (targetStep != lastStepTarget) {
                                                lastStepTarget = targetStep
                                                verticalZoomSaved = targetStep

                                                animJob?.cancel()
                                                animJob = scope.launch {
                                                    // --- 1) anchor را در لحظه‌ی شروع پله‌ جدید قفل کن ---
                                                    // نقطه‌ی انگشت داخل viewport گرید
                                                    val focusYInWindow =
                                                        focus.y + overlayLayerOriginInRoot.y
                                                    val focusYInGridViewport =
                                                        (focusYInWindow - gridOriginInWindow.y).coerceIn(
                                                            0f,
                                                            gridViewportHeightPx
                                                        )

                                                    // زوم فعلیِ نمایشی (شروع انیمیشن)
                                                    val z0 = verticalZoomAnim.value
                                                    val hourH0 = with(density) {
                                                        (72.dp * z0).coerceIn(
                                                            48.dp,
                                                            360.dp
                                                        ).toPx()
                                                    }

                                                    // دقیقه‌ای که دقیقاً زیر انگشت است (در مختصات محتوا)
                                                    val contentY0 =
                                                        vScroll.value + focusYInGridViewport
                                                    val minuteUnderFinger =
                                                        (contentY0 / hourH0) * 60f

                                                    // --- 2) انیمیشن دستی: هم‌زمان zoom و scroll ---
                                                    val z1 = targetStep
                                                    val durationMs = 120L
                                                    val startNs = withFrameNanos { it }
                                                    val endNs = startNs + durationMs * 1_000_000L

                                                    while (true) {
                                                        val now = withFrameNanos { it }
                                                        val tRaw =
                                                            ((now - startNs).toDouble() / (endNs - startNs).toDouble())
                                                                .coerceIn(0.0, 1.0)
                                                                .toFloat()

                                                        // easing مشابه FastOutSlowInEasing (تقریب خوب و سبک)
                                                        val t = FastOutSlowInEasing.transform(tRaw)

                                                        // lerp زوم
                                                        val z = z0 + (z1 - z0) * t

                                                        // 2-1) اعمال زوم (بدون animateTo)
                                                        verticalZoomAnim.snapTo(z)

                                                        // 2-2) محاسبه‌ی اسکرول لازم برای ثابت ماندن دقیقه زیر انگشت
                                                        val hourH = with(density) {
                                                            (72.dp * z).coerceIn(
                                                                48.dp,
                                                                360.dp
                                                            ).toPx()
                                                        }
                                                        val contentY =
                                                            (minuteUnderFinger / 60f) * hourH
                                                        val targetScroll =
                                                            (contentY - focusYInGridViewport)
                                                                .roundToInt()
                                                                .coerceIn(0, vScroll.maxValue)

                                                        vScroll.scrollTo(targetScroll)

                                                        if (tRaw >= 1f) break
                                                    }

                                                    // ذخیره برای بعد
                                                    verticalZoomSaved = z1
                                                }
                                            }
                                        }

                                        lastDistance = dist
                                        pressed.forEach { it.consume() }
                                    }
                                }
                            }

                    ) {

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

                                // ✅ آیتم‌ها روی Grid
                                displayTimelineItems.forEach { displayItem ->

                                    val layout = overlapLayouts[displayItem.scheduleId]

                                    val pm = pendingMove[displayItem.scheduleId]
                                    val item = if (pm == null) displayItem else displayItem.copy(
                                        start = LocalDateTime.of(pm.date, LocalTime.MIN)
                                            .plusMinutes(pm.startMin.toLong()),
                                        end = LocalDateTime.of(pm.date, LocalTime.MIN)
                                            .plusMinutes(pm.endMin.toLong())
                                    )

                                    TimelineItemBox(
                                        item = item,
                                        startDate = startDate,
                                        overlapLayout = layout,
                                        dayWidthDp = dayWidthDp,
                                        hourHeightDp = hourHeightDp,
                                        verticalZoom = verticalZoom,
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
                                            val isVirtual = scheduleId < 0

                                            if (isVirtual) {
                                                // 🔥 مجازی → واقعی بساز (Override)
                                                // پیشنهاد: توی ViewModel یه تابع unified داشته باشی که هم TIME_RANGE هم POMODORO رو ساپورت کنه
                                                viewModel.materializeVirtual(
                                                    virtual = displayItem,
                                                    newDate = date,
                                                    newStartMin = s,
                                                    newEndMin = e,
                                                    inPallet = false
                                                )

                                                selectedScheduleId = null
                                                return@TimelineItemBox
                                            }

                                            // واقعی → همان رفتار فعلی
                                            pendingMove[scheduleId] = PendingMove(date, s, e)

                                            if (item.mode == ScheduleMode.POMODORO) {
                                                viewModel.movePomodoroSchedule(scheduleId, date, s, e)
                                            } else {
                                                viewModel.moveSchedule(scheduleId, date, s, e)
                                            }
                                        },
                                        onResizeEndCommit = { scheduleId, newEnd ->
                                            val isVirtual = scheduleId < 0

                                            if (isVirtual) {
                                                val d = displayItem.start.toLocalDate()
                                                val startMin =
                                                    displayItem.start.toLocalTime().hour * 60 + displayItem.start.toLocalTime().minute

                                                viewModel.materializeVirtual(
                                                    virtual = displayItem,
                                                    newDate = d,
                                                    newStartMin = startMin,
                                                    newEndMin = newEnd,
                                                    inPallet = false
                                                )

                                                selectedScheduleId = null
                                                return@TimelineItemBox
                                            }

                                            viewModel.resizeScheduleEnd(scheduleId, newEnd)
                                        },
                                        onResizeStartCommit = { scheduleId, newStart ->
                                            val isVirtual = scheduleId < 0

                                            if (isVirtual) {
                                                val d = displayItem.start.toLocalDate()
                                                val endMin =
                                                    displayItem.end.toLocalTime().hour * 60 + displayItem.end.toLocalTime().minute

                                                viewModel.materializeVirtual(
                                                    virtual = displayItem,
                                                    newDate = d,
                                                    newStartMin = newStart,
                                                    newEndMin = endMin,
                                                    inPallet = false
                                                )

                                                selectedScheduleId = null
                                                return@TimelineItemBox
                                            }

                                            viewModel.resizeScheduleStart(scheduleId, newStart)
                                        },
                                        onSendToPallet = { scheduleId, _ ->
                                            val isVirtual = scheduleId < 0

                                            if (isVirtual) {
                                                val d = displayItem.start.toLocalDate()
                                                val s =
                                                    displayItem.start.toLocalTime().hour * 60 + displayItem.start.toLocalTime().minute
                                                val e =
                                                    displayItem.end.toLocalTime().hour * 60 + displayItem.end.toLocalTime().minute

                                                viewModel.materializeVirtual(
                                                    virtual = displayItem,
                                                    newDate = d,
                                                    newStartMin = s,
                                                    newEndMin = e,
                                                    inPallet = true
                                                )

                                                selectedScheduleId = null
                                                return@TimelineItemBox
                                            }

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
                                        onBringDayIntoView = { dayIndex ->
                                            pendingBringDayIntoView = dayIndex
                                        },
                                        onEdit = { taskId ->
                                            // ✅ همان مسیر CategoryScreen
                                            navController.navigate(AppRoutes.taskEdit(taskId))
                                        },
                                        onDelete = { scheduleId ->
                                            viewModel.deleteScheduleById(scheduleId)
                                            selectedScheduleId = null
                                        },
                                        onMakeIndependent = {
                                            // این callback فقط از منوی آیتم مجازی صدا زده می‌شود
                                            val d = displayItem.start.toLocalDate()
                                            val s = displayItem.start.toLocalTime().hour * 60 + displayItem.start.toLocalTime().minute
                                            val e = displayItem.end.toLocalTime().hour * 60 + displayItem.end.toLocalTime().minute

                                            viewModel.materializeVirtual(
                                                virtual = displayItem,
                                                newDate = d,
                                                newStartMin = s,
                                                newEndMin = e,
                                                inPallet = false
                                            )
                                            selectedScheduleId = null
                                        },
                                        onStartPomodoroNow = { scheduleId ->
                                            viewModel.startPomodoroNow(scheduleId)
                                            selectedScheduleId = null
                                        },

                                        )
                                }
                            }
                        }
                    }

                    // ✅ خط زمان روی کل ناحیه (ستون ساعت + گرید)
                    CurrentTimeOverlay(
                        startDate = startDate,
                        numDays = numDays,
                        dayWidthDp=dayWidthDp,
                        hourHeightDp = hourHeightDp,
                        vScrollValue = vScroll.value,
                        palletOverlayWidth = palletOverlayWidth,
                        sidebarWidth = 56.dp
                    )
                }
            }




            // ✅ Drag overlay
            if (draggingFromPallet != null || draggingPomodoro != null) {
                Box(Modifier.fillMaxSize()) {

                    val title = draggingFromPallet?.title ?: draggingPomodoro?.taskName.orEmpty()
                    val iconName = draggingFromPallet?.categoryIconName // پومودورو فعلاً icon نداره

                    Box(
                        modifier = Modifier
                            .offset {
                                IntOffset(
                                    (dragX - grabOffsetX - overlayLayerOriginInRoot.x).toInt(),
                                    (dragY - grabOffsetY - overlayLayerOriginInRoot.y).toInt()
                                )
                            }
                            .width(overlayWidth)
                            .height(
                                if (draggingPomodoro != null) 64.dp
                                else overlayHeight.coerceAtLeast(44.dp)
                            )
                            .background(
                                scheduleModeColor(
                                    draggingFromPallet?.mode ?: ScheduleMode.POMODORO
                                ).copy(alpha = 0.6f)
                            )
                    ) {
                        Row(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(start = 8.dp, top = 8.dp, end = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (iconName != null) {
                                CategoryIconWithPlate(iconName = iconName)
                            } else {
                                Icon(Icons.Filled.Timeline, contentDescription = null)
                            }
                            Spacer(Modifier.width(8.dp))
                            Text(
                                title,
                                maxLines = 1,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }

                        if (draggingPomodoro != null) {
                            Text(
                                "Pomodoro",
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(start = 16.dp, bottom = 10.dp),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }

            }

            //Stepper
            pomodoroAdjust?.let { st ->
                PomodoroCountStepperOverlay(
                    count = st.ids.size,
                    max = st.maxAllowed,
                    anchorInRoot = st.anchorInRoot,
                    onInc = {
                        if (st.ids.size >= st.maxAllowed) return@PomodoroCountStepperOverlay
                        val nextStart = st.startMin + (st.ids.size * (st.focus + st.shortBreak))
                        scope.launch {
                            val newId = withContext(Dispatchers.IO) {
                                viewModel.insertOnePomodoroTimelineItem(
                                    taskId = st.taskId,
                                    scheduleId=st.scheduleId,
                                    date = st.date,
                                    startMin = nextStart,
                                    focus = st.focus,
                                    shortBreak = st.shortBreak
                                )
                            }
                            pomodoroAdjust = st.copy(ids = st.ids + newId)
                        }
                    },
                    onDec = {
                        if (st.ids.size <= 1) return@PomodoroCountStepperOverlay
                        val lastId = st.ids.last()
                        if (lastId != null) {
                            viewModel.deleteScheduleById(lastId)
                        }
                        pomodoroAdjust = st.copy(ids = st.ids.dropLast(1))
                    },
                    onDismiss = { pomodoroAdjust = null }
                )
            }


            //تایمر پومودورو
            runningPomodoro?.let { running ->
                RunningPomodoroPanel(
                    state = running,
                    onPause = { viewModel.pauseRunningPomodoro() },
                    onResume = { viewModel.resumeRunningPomodoro() },
                    onSkip = { viewModel.skipRunningPomodoro() },
                    onRestart = { viewModel.restartRunningPomodoro() }
                )
            }

            RightPallet(
                palletItems = palletItems,
                pomodoroCards = pomodoroPalletCards,
                expanded = palletExpanded,
                isDraggingFromPallet = isDraggingAnything,
                onToggle = { palletExpanded = !palletExpanded },
                onDragStart = { item, sx, sy, downX, downY ->
                    palletExpanded = false
                    draggingPomodoro = null
                    draggingFromPallet = item

                    dragX = sx
                    dragY = sy
                    grabOffsetX = downX
                    grabOffsetY = downY
                },
                onPomodoroDragStart = { card, sx, sy, downX, downY ->
                    palletExpanded = false
                    draggingFromPallet = null
                    draggingPomodoro = card

                    dragX = sx
                    dragY = sy
                    grabOffsetX = downX
                    grabOffsetY = downY
                },
                onDrag = { dx, dy ->
                    dragX += dx
                    dragY += dy
                },
                onDragEnd = { scheduleId ->
                    val localX = dragX - gridOriginInWindow.x
                    val localY = dragY - gridOriginInWindow.y

                    val contentX = localX + hScroll.value
                    val contentY = localY + vScroll.value

                    val dayIndex = (contentX / dayWidthPx).toInt()
                    val minuteOfDay = ((contentY / hourHeightPx) * 60f).toInt()

                    if (dayIndex !in 0 until numDays || minuteOfDay !in 0 until 24 * 60) {
                        draggingFromPallet = null
                        draggingPomodoro = null
                        return@RightPallet
                    }

                    val date = startDate.plusDays(dayIndex.toLong())
                    val minDur = 5
                    val startMin = snap(minuteOfDay.coerceIn(0, 24 * 60 - minDur), 5)

                    // ✅ اگر پومودورو کارت درگ می‌شد
                    val pomo = draggingPomodoro
                    if (pomo != null) {
                        val dur = (pomo.focus + pomo.shortBreak).coerceAtLeast(5)
                        val endMin = (startMin + dur).coerceAtMost(24 * 60)

                        // anchor برای stepper: نزدیک نقطه drop
                        val anchor = Offset(
                            x = (dragX - overlayLayerOriginInRoot.x).coerceAtLeast(0f),
                            y = (dragY - overlayLayerOriginInRoot.y).coerceAtLeast(0f)
                        )

                        // remainingToday: این مقدار را از خود pomo داریم
                        val maxAllowed = pomo.remainingToday.coerceAtLeast(1)

                        // ✅ یک عدد بساز و id بگیر
                        scope.launch {
                            val id = withContext(Dispatchers.IO) {
                                viewModel.insertOnePomodoroTimelineItem(
                                    taskId = pomo.taskId,
                                    scheduleId = scheduleId,
                                    date = date,
                                    startMin = startMin,
                                    focus = pomo.focus,
                                    shortBreak = pomo.shortBreak
                                )
                            }

                            pomodoroAdjust = PomodoroAdjustState(
                                taskId = pomo.taskId,
                                scheduleId = scheduleId,
                                date = date,
                                startMin = startMin,
                                focus = pomo.focus,
                                shortBreak = pomo.shortBreak,
                                ids = listOf(id),
                                anchorInRoot = Offset(
                                    x = dragX - overlayLayerOriginInRoot.x,
                                    y = dragY - overlayLayerOriginInRoot.y
                                ),
                                maxAllowed = maxAllowed
                            )
                        }

                        draggingFromPallet = null
                        draggingPomodoro = null
                        return@RightPallet
                    }


                    // ✅ در غیر این صورت schedule معمولی
                    val t = draggingFromPallet ?: run {
                        draggingFromPallet = null
                        draggingPomodoro = null
                        return@RightPallet
                    }

                    val oldStartMin = t.start.toLocalTime().hour * 60 + t.start.toLocalTime().minute
                    val oldEndMin = t.end.toLocalTime().hour * 60 + t.end.toLocalTime().minute
                    val oldDurMin = (oldEndMin - oldStartMin).coerceAtLeast(15)

                    val endMin = (startMin + oldDurMin).coerceAtMost(24 * 60)

                    viewModel.dropScheduleFromPalletToTimeLine(
                        scheduleId = t.scheduleId,
                        date = date,
                        startMin = startMin,
                        endMin = endMin
                    )

                    draggingFromPallet = null
                    draggingPomodoro = null
                    return@RightPallet
                },
                onDragCancel = {
                    draggingFromPallet = null
                    draggingPomodoro = null
                },
                modifier = Modifier.align(Alignment.CenterEnd),
            )




        }
        if (showPermissionDialog) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                AlertDialog(
                    onDismissRequest = {
                        viewModel.markSchedulePermissionsAsked()
                        showPermissionDialog = false
                    },
                    title = {
                        Text(
                            text = "فعال‌سازی یادآورها",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Right
                        )
                    },
                    text = {
                        Text(
                            text = "برای اینکه تایمر پومودورو و یادآورها دقیق‌تر اجرا شوند، لازم است اجازه نوتیفیکیشن و در صورت نیاز اجازه Alarms & reminders را فعال کنید.\n\nاین پیام فقط یک بار نمایش داده می‌شود.",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Right,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                viewModel.markSchedulePermissionsAsked()
                                showPermissionDialog = false

                                if (
                                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                    ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.POST_NOTIFICATIONS
                                    ) != PackageManager.PERMISSION_GRANTED
                                ) {
                                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    openExactAlarmSettings()
                                }
                            }
                        ) {
                            Text("ادامه")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                viewModel.markSchedulePermissionsAsked()
                                showPermissionDialog = false
                            }
                        ) {
                            Text("بعداً")
                        }
                    }
                )
            }
        }

    }
}



@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TimelineItemBox(
    item: ScheduleScreenItem,
    startDate: LocalDate,
    overlapLayout: OverlapLayout?,
    dayWidthDp: Dp,
    hourHeightDp: Dp,
    verticalZoom: Float,
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
    onBringDayIntoView: (dayIndex: Int) -> Unit,
    onEdit: (taskId: Int) -> Unit,
    onDelete: (scheduleId: Int) -> Unit,
    onMakeIndependent: () -> Unit,
    onStartPomodoroNow: (scheduleId: Int) -> Unit,


    ) {


    val density = LocalDensity.current

    //ثابت ها
    val minDur = 15


    //متغییر های کلی
    val dayWpx = with(density) { dayWidthDp.toPx() }
    val hourHpx = with(density) { hourHeightDp.toPx() }
    val snapStep = snapStepForZoom(verticalZoom)


    //مشخصات اسکچول
    //فاصله روز اسکچول با روز جاری
    val dayIndex0 = ChronoUnit.DAYS.between(startDate, item.start.toLocalDate()).toInt()
    if (dayIndex0 !in 0 until numDays) return
    val startMin0 = item.start.toLocalTime().hour * 60 + item.start.toLocalTime().minute
    val endMin0 = item.end.toLocalTime().hour * 60 + item.end.toLocalTime().minute
    val dur0 = (endMin0 - startMin0).coerceAtLeast(minDur)


    // Move state
    var dragDx by remember(item.scheduleId) { mutableFloatStateOf(0f) }
    var dragDy by remember(item.scheduleId) { mutableFloatStateOf(0f) }


    //ترشولد برای جایجایی افقی
    val daySnapThresholdPx = dayWpx / 4f

    //دلتا برای حرکت افقی
    val dayDelta = when {
        dragDx > 0f -> ((dragDx + daySnapThresholdPx) / dayWpx).toInt()
        dragDx < 0f -> ((dragDx - daySnapThresholdPx) / dayWpx).toInt()
        else -> 0
    }

    val effectiveDragDyForPreview = dragDy
    val minDelta = ((effectiveDragDyForPreview / hourHpx) * 60f).roundToInt()


    val moveDayIndex = (dayIndex0 + dayDelta).coerceIn(0, numDays - 1)
    val moveStartRaw = (startMin0 + minDelta).coerceIn(0, 24 * 60 - minDur)
    val desiredMoveStart = snap(moveStartRaw, snapStep)
    val desiredMoveEnd = desiredMoveStart + dur0

    var lastScrollPx by remember(item.scheduleId) { mutableIntStateOf(0) }

    val moveRange = clampRange(desiredMoveStart, desiredMoveEnd)
    val moveStart = moveRange.start
    val moveEnd = moveRange.end

    // ---------------- Resize state ----------------
    var topDy by remember(item.scheduleId) { mutableFloatStateOf(0f) }
    var bottomDy by remember(item.scheduleId) { mutableFloatStateOf(0f) }

    val topDeltaMin = ((topDy / hourHpx) * 60f).roundToInt()
    val bottomDeltaMin = ((bottomDy / hourHpx) * 60f).roundToInt()

    val resizeStartRaw = (startMin0 + topDeltaMin)
        .coerceIn(0, (endMin0 - minDur).coerceAtLeast(0))
    val desiredResizeStart = snap(resizeStartRaw, snapStep)
    val rStart = clampRange(desiredResizeStart, endMin0)
    val resizeStart = rStart.start


    val resizeEndRaw = (endMin0 + bottomDeltaMin)
        .coerceIn((startMin0 + minDur).coerceAtMost(24 * 60), 24 * 60)
    val desiredResizeEnd = snap(resizeEndRaw, snapStep)
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


    val baseW = (dayWidthDp - 12.dp)

    val widthFrac = overlapLayout?.widthFrac ?: 1f
    val offsetFrac = overlapLayout?.offsetFrac ?: 0f
    val z = overlapLayout?.z ?: item.scheduleId.toFloat()

    val x = dayWidthDp * displayDayIndex + (baseW * offsetFrac)
    val y = hourHeightDp * (displayStart / 60f)
    val h = hourHeightDp * (((displayEnd - displayStart).coerceAtLeast(minDur)) / 60f)

    val taskH = h.coerceAtLeast(24.dp) // ✅ حداقل خیلی کوچک فقط برای قابل لمس بودن/رندر
    val selectionH =
        if (taskH >=76.dp) taskH else 76.dp


    val containerY = (y )



    var lastBroughtDay by remember(item.scheduleId) { mutableIntStateOf(dayIndex0) }

    // مقدار dragDx که باعث تغییر روز شده رو جدا می‌کنیم، فقط "باقی‌مانده" رو برای نمایش حرکت می‌ذاریم
    val residualDx = if (isMoving) (dragDx - (dayDelta * dayWpx)) else 0f

    // محدودش کن که کارت خیلی از ستون خارج نشه (اختیاری ولی خوش‌دست‌تر)
    val previewDx = residualDx.coerceIn(-dayWpx * 0.45f, dayWpx * 0.45f)

    var menuExpanded by remember(item.scheduleId) { mutableStateOf(false) }
    var showDeleteConfirm by remember(item.scheduleId) { mutableStateOf(false) }

    val level = overlapLayout?.level ?: 0

    val borderWidth = when (level) {
        1 -> 1.dp
        2 -> 1.5.dp
        3 -> 2.dp
        else -> 0.dp
    }

    val borderColor = when (level) {
        1 -> MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
        2 -> MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
        3 -> MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
        else -> Color.Transparent
    }

    LaunchedEffect(isMoving, moveDayIndex) {
        if (isMoving && moveDayIndex != lastBroughtDay) {
            lastBroughtDay = moveDayIndex
            onBringDayIntoView(moveDayIndex)
        }
    }


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

    val isPomodoroPARENT = item.mode == ScheduleMode.POMODORO

    val focusMin = (item.focusMinutes ?: 25).coerceAtLeast(1)
    val breakMin = (item.shortBreakMinutes ?: 5).coerceAtLeast(0)

    // اگر schedule طولش با focus+break یکی نبود، باز هم نسبت رو از همین‌ها می‌گیریم
    val totalPomo = (focusMin + breakMin).coerceAtLeast(1)
    val focusFrac = (focusMin.toFloat() / totalPomo).coerceIn(0f, 1f)

    val pomoFocusColor = scheduleModeColor(ScheduleMode.POMODORO).copy(alpha = 0.45f)
    val pomoBreakColor = scheduleModeColor(ScheduleMode.POMODORO).copy(alpha = 0.22f)


    val handleH = 16.dp
    val colorForBorder = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
    val allowResize = selected && item.mode != ScheduleMode.POMODORO
    val showTimeHandles = selected || isMoving   // ✅ در حالت move هم نمایش بده
    val showHandles = (selected || isMoving)


    val resizeStartModifier = if (allowResize) {
        Modifier.pointerInput(item.scheduleId, item.start, item.end) {
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
                    val topDeltaNow =
                        ((effectiveTopDy / hourHpx) * 60f).toInt()
                    val raw = (startMin0 + topDeltaNow)
                        .coerceIn(
                            DAY_MIN,
                            (endMin0 - MIN_GAP_MIN).coerceAtLeast(DAY_MIN)
                        )

                    val desired = snap(raw, snapStep)
                    val clamped = clampRange(desired, endMin0)

                    onAutoScroll(endMin0,clamped.start,  AutoScrollMode.RESIZE_START)
                },
                onDragEnd = {
                    val topDeltaMin2 = ((topDy / hourHpx) * 60f).roundToInt()

                    val raw = (startMin0 + topDeltaMin2)
                        .coerceIn(
                            DAY_MIN,
                            (endMin0 - MIN_GAP_MIN).coerceAtLeast(DAY_MIN)
                        )

                    val desired = snap(raw, snapStep)
                    val clamped = clampRange(desired, endMin0)

                    onResizeStartCommit(item.scheduleId, clamped.start)

                    isResizingStart = false
                    topDy = 0f
                    onAutoScrollStop()
                },
                onDragCancel = {
                    isResizingStart = false
                    topDy = 0f
                    onAutoScrollStop()
                }
            )
        }
    } else {
        Modifier
    }


    val resizeEndModifier = if (allowResize) {
        Modifier.pointerInput(item.scheduleId, item.start, item.end) {
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
                    val bottomDeltaNow =
                        ((effectiveBottomDy / hourHpx) * 60f).toInt()
                    val raw = (endMin0 + bottomDeltaNow)
                        .coerceIn(
                            (startMin0 + MIN_GAP_MIN).coerceAtMost(DAY_MAX),
                            DAY_MAX
                        )
                    val desired = snap(raw, snapStep)

                    val clamped = clampRange(startMin0, desired)

                    onAutoScroll(startMin0, clamped.end, AutoScrollMode.RESIZE_END)

//                    Log.i("TEST" , "bottomDy=$bottomDy ")
//                    Log.i("TEST" , "bottomDeltaNow=$bottomDeltaNow ")
//                    Log.i("TEST" , "raw=$raw ")
//                    Log.i("TEST" , "desired=$desired ")
//                    Log.i("TEST" , "clamped=$clamped ")
//                    Log.i("TEST" , "------------------- ")
                },
                onDragEnd = {
                    val effectiveBottomDy2 = bottomDy
                    val bottomDeltaMin2 =
                        ((effectiveBottomDy2 / hourHpx) * 60f).roundToInt()


                    val raw = (endMin0 + bottomDeltaMin2)
                        .coerceIn(
                            (startMin0 + MIN_GAP_MIN).coerceAtMost(DAY_MAX),
                            DAY_MAX
                        )

                    val desired = snap(raw, snapStep)

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
        }
    } else {
        Modifier
    }



    val showHeader = taskH >= 56.dp   // ✅ این عدد را به سلیقه‌ات تنظیم کن

    val isVirtual = item.scheduleId < 0

    val scheduleColor =if (isVirtual) {
        scheduleModeColor(item.mode).copy(alpha = 0.15f)
    }else{
        scheduleModeColor(item.mode).copy(alpha = 0.35f)
    }

    val dashed = taskH <= 76.dp && selected




    Box(
        modifier = Modifier
            .offset(x = x + 6.dp, y = containerY)
            .width(baseW * widthFrac)
            .height(selectionH)
            .zIndex(z)
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

        //آیکون وضعیت بالا راست
        if (isVirtual) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 6.dp, end = 6.dp)
                    .size(22.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Link,
                    contentDescription = "Virtual occurrence",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        else if (item.repeating){
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 6.dp, end = 6.dp)
                    .size(22.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Repeat,
                    contentDescription = "Virtual occurrence",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }


        //  آیکون گروه + نام اسکچول یا تسک
        if (showHeader) {
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
        }


        //  باکس اصلی
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .width(dayWidthDp - 12.dp)
                .height(taskH)
                .border(
                    width = borderWidth,
                    color = borderColor,
                    shape = MaterialTheme.shapes.medium
                )
                .clip(MaterialTheme.shapes.medium) // ✅ مهم: دو رنگ داخل شکل کلیپ شوند
        ) {

            if (isPomodoroPARENT) {
                // ✅ دو رنگ: بالا فوکوس، پایین استراحت
                Box(Modifier.fillMaxSize()) {

                    // بخش فوکوس (بالا)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(focusFrac)
                            .background(pomoFocusColor)
                            .align(Alignment.TopStart)
                    )

                    // بخش استراحت (پایین)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(1f - focusFrac)
                            .background(pomoBreakColor)
                            .align(Alignment.BottomStart)
                    )
                }
            }
            //(is time range)
            else {
                // ✅ حالت عادی همان رنگ قبلی
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            color = scheduleColor,
                            shape = RectangleShape // چون clip بالا انجام شده
                        )
                )
            }
            val innerTopPad = if (showHeader) 34.dp else 8.dp

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
                                val moveStartRawNow =
                                    (startMin0 + minDeltaNow).coerceIn(0, 24 * 60 - minDur)

                                val desiredStart = snap(moveStartRawNow, snapStep)
                                val desiredEnd = desiredStart + dur0
                                val r = clampRange(desiredStart, desiredEnd)

                                onAutoScroll(r.start, r.end, AutoScrollMode.MOVE)

                            },
                            onDragEnd = {
                                // محاسبه نهایی با dragDx/dragDy فعلی
                                val daySnapThresholdPx2 = dayWpx / 3f
                                val dayDelta2 = when {
                                    dragDx > 0f -> ((dragDx + daySnapThresholdPx2) / dayWpx).toInt()
                                    dragDx < 0f -> ((dragDx - daySnapThresholdPx2) / dayWpx).toInt()
                                    else -> 0
                                }


                                val effectiveDragDy2 = dragDy
                                val minDelta2 = ((effectiveDragDy2 / hourHpx) * 60f).roundToInt()

                                val moveDayIndex2 = (dayIndex0 + dayDelta2).coerceIn(0, numDays - 1)
                                val moveStartRaw2 =
                                    (startMin0 + minDelta2).coerceIn(0, 24 * 60 - minDur)

                                val finalDate = startDate.plusDays(moveDayIndex2.toLong())

                                val desiredStart = snap(moveStartRaw2, snapStep)
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
                    .padding(start = 8.dp, top = innerTopPad, end = 8.dp, bottom = 8.dp)
            ) {


            }


        }


        // هندل‌ها وقتی selected یا در حال move
        if (showHandles) {

            // --- Top handle (resize start) ---

            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .height(handleH)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f))
                    .then(resizeStartModifier),
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
                if (allowResize) {
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




            // --- More menu (move/edit/delete) ---
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 6.dp)
            ) {
                IconButton(
                    onClick = { menuExpanded = true },
                    modifier = Modifier
                        .size(34.dp)
//                        .background(
//                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
//                            shape = CircleShape
//                        )
                ) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = "More",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    if (isVirtual) {
                        // ✅ فقط یک گزینه برای مجازی‌ها
                        DropdownMenuItem(
                            text = { Text("تبدیل به زمان‌بندی مستقل") },
                            leadingIcon = {
                                Icon(Icons.Default.LinkOff, contentDescription = null)
                                // اگر آیکون بهتری داشتی: LinkOff / CallSplit
                            },
                            onClick = {
                                menuExpanded = false
                                onMakeIndependent()
                            }
                        )
                    } else {


                        if (item.mode == ScheduleMode.POMODORO) {
                            DropdownMenuItem(
                                text = { Text("همین حالا شروع کن") },
                                leadingIcon = {
                                    Icon(Icons.Filled.PlayArrow, contentDescription = null)
                                },
                                onClick = {
                                    menuExpanded = false
                                    onStartPomodoroNow(item.scheduleId)
                                }
                            )
                        }


                        // ✅ آیتم‌های واقعی: Move / Edit / Delete
                        DropdownMenuItem(
                            text = { Text("Move to pallet") },
                            leadingIcon = { Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null) },
                            onClick = {
                                menuExpanded = false
                                onSendToPallet(item.scheduleId, item.taskId)
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Edit") },
                            leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = null) },
                            onClick = {
                                menuExpanded = false
                                onEdit(item.taskId)
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Delete") },
                            leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = null) },
                            onClick = {
                                menuExpanded = false
                                showDeleteConfirm = true
                            }
                        )
                    }
                }
            }




            // --- Bottom handle (resize end) ---

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(handleH)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f))
                    .then(resizeEndModifier),
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
                if (allowResize) {
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


        if (showDeleteConfirm) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirm = false },
                title = { Text("Delete schedule?") },
                text = { Text("This action cannot be undone.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDeleteConfirm = false
                            onDelete(item.scheduleId)
                        }
                    ) { Text("Delete") }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
                }
            )
        }


    }
}


@Composable
private fun RightPallet(
    palletItems: List<ScheduleScreenItem>,
    pomodoroCards: List<PomodoroPalletCardItem>,
    expanded: Boolean,
    isDraggingFromPallet: Boolean,
    onToggle: () -> Unit,
    onDragStart: (ScheduleScreenItem, Float, Float, Float, Float) -> Unit,
    onPomodoroDragStart: (PomodoroPalletCardItem, Float, Float, Float, Float) -> Unit,
    onDrag: (dx: Float, dy: Float) -> Unit,
    onDragEnd: (scheduleId: Int) -> Unit,
    onDragCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val keepContent = expanded || isDraggingFromPallet

    Row(modifier = modifier.fillMaxHeight()) {

        // دکمه باز/بسته
        // دکمه باز/بسته
        Box(
            modifier = Modifier
                .width(48.dp)
                .fillMaxHeight()
                .padding(vertical = 8.dp)
                .graphicsLayer { alpha = if (isDraggingFromPallet) 0f else 1f }
                .clickable(enabled = !isDraggingFromPallet) { onToggle() },
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
                        items(pomodoroCards, key = { "pomo_${it.taskId}" }) { c ->
                            PomodoroPalletCard(
                                item = c,
                                onDragStart = onPomodoroDragStart,
                                onDrag = onDrag,
                                onDragEnd = { onDragEnd(c.scheduleId) },
                                onDragCancel = onDragCancel
                            )
                            HorizontalDivider(thickness = 0.5.dp)
                        }
                        items(palletItems, key = { it.scheduleId }) { t ->
                            PalletTaskItem(
                                item = t,
                                onDragStart = onDragStart,
                                onDrag = onDrag,
                                onDragEnd = { onDragEnd(t.scheduleId) },
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
private fun PomodoroPalletCard(
    item: PomodoroPalletCardItem,
    onDragStart: (PomodoroPalletCardItem, Float, Float, Float, Float) -> Unit,
    onDrag: (dx: Float, dy: Float) -> Unit,
    onDragEnd: () -> Unit,
    onDragCancel: () -> Unit
) {
    var coords by remember { mutableStateOf<LayoutCoordinates?>(null) }

    val progress = remember(item.expectedToday, item.scheduledToday) {
        if (item.expectedToday <= 0) 0f
        else (item.scheduledToday.toFloat() / item.expectedToday.toFloat())
            .coerceIn(0f, 1f)
    }

    val pomoColor = scheduleModeColor(ScheduleMode.POMODORO)
    val shape = RoundedCornerShape(18.dp)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(102.dp)
            .padding(vertical = 6.dp)
            .onGloballyPositioned { coords = it }
            .pointerInput(item.taskId) {
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
            },
        shape = shape,
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(shape)
        ) {
            // رنگ پایه کارت
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(pomoColor.copy(alpha = 0.18f))
            )

            // مقدار انجام‌شده امروز بر اساس E/D
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress)
                    .background(pomoColor.copy(alpha = 0.55f))
                    .align(Alignment.CenterStart)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp)
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            item.taskName,
                            maxLines = 1,
                            style = MaterialTheme.typography.labelLarge
                        )
                        Text(
                            "Pomodoro",
                            maxLines = 1,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }

                    Text(
                        "T/D : ${item.totalTarget}/${item.totalDone.toString().padStart(2, '0')}",
                        style = MaterialTheme.typography.labelMedium
                    )
                }

                Spacer(Modifier.height(6.dp))

                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "E/D : ${item.expectedToday}/${item.scheduledToday.toString().padStart(2, '0')}",
                        style = MaterialTheme.typography.labelMedium
                    )

                    Spacer(Modifier.weight(1f))

                    Text(
                        "${(progress * 100).roundToInt()}%",
                        style = MaterialTheme.typography.labelSmall
                    )
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
    var coords by remember { mutableStateOf<LayoutCoordinates?>(null) }

    val shape = RoundedCornerShape(16.dp)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .onGloballyPositioned { coords = it }
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
            },
        shape = shape,
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
        colors = CardDefaults.cardColors(
            containerColor = scheduleModeColor(item.mode).copy(alpha = 0.45f)
        )
    ) {
        Text(
            text = item.title,
            maxLines = 1,
            modifier = Modifier.padding(10.dp)
        )
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

@Composable
private fun PomodoroCountStepperOverlay(
    count: Int,
    max: Int,
    anchorInRoot: Offset,
    onInc: () -> Unit,
    onDec: () -> Unit,
    onDismiss: () -> Unit
) {
    // کلیک بیرون = بستن
    Box(
        Modifier
            .fillMaxSize()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onDismiss() }
    )

    Box(
        modifier = Modifier
            .offset { IntOffset(anchorInRoot.x.toInt() + 12, anchorInRoot.y.toInt() - 16) }
            .background(
                MaterialTheme.colorScheme.surfaceContainerHigh,
                shape = MaterialTheme.shapes.large
            )
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            IconButton(
                onClick = onInc,
                enabled = count < max,
                modifier = Modifier.size(52.dp)
            ) { Icon(Icons.Filled.KeyboardArrowUp, contentDescription = null) }

            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleLarge
            )

            IconButton(
                onClick = onDec,
                enabled = count > 1,
                modifier = Modifier.size(52.dp)
            ) { Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null) }

            Spacer(Modifier.height(4.dp))
            Text(
                text = "1..$max",
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
private fun CurrentTimeOverlay(
    startDate: LocalDate,
    numDays: Int,
    dayWidthDp :Dp,
    hourHeightDp: Dp,
    vScrollValue: Int,
    palletOverlayWidth: Dp,
    sidebarWidth: Dp = 56.dp,
) {
    val now = remember { mutableStateOf(LocalDateTime.now()) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000)
            now.value = LocalDateTime.now()
        }
    }

    val today = now.value.toLocalDate()
    val dayIndex = ChronoUnit.DAYS.between(startDate, today).toInt()
    if (dayIndex !in 0 until numDays) return

    val minutesNow = now.value.toLocalTime().hour * 60 + now.value.toLocalTime().minute
    val yContent = hourHeightDp * (minutesNow / 60f)
    val yViewport = yContent - with(LocalDensity.current) { vScrollValue.toDp() }

    val dash = remember { PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f) }
    val red = Color(0xFFE53935)
    val density = LocalDensity.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(end = palletOverlayWidth) // چون سمت راست پالت داری
            .offset(y = yViewport)
            .height(24.dp) // فضای کافی برای متن + خط
            .drawBehind {
                val y = (size.height / 2f) - 20f   //تیون کردن دستی
                val xStart = with(density) { sidebarWidth.toPx() }
                drawLine(
                    color = red,
                    start = Offset(xStart, y),
                    end = Offset(size.width, y),
                    strokeWidth = 2.dp.toPx(),
                    pathEffect = dash
                )
            }
    ) {
        // متن سمت چپ: "12 : 22"
        Text(
            text = "%02d : %02d".format(minutesNow / 60, minutesNow % 60),
            color = red,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 8.dp)
        )
    }
}


//>>>>>>>>>>>>>>>> Utils <<<<<<<<<<<<<<<<<<

private const val MIN_GAP_MIN = 5
private const val DAY_MIN = 0
private const val DAY_MAX = 24 * 60

private const val ZOOM_MIN = 0.6f
private const val ZOOM_MAX = 6.0f


private const val OVERLAP_MAX_LEVEL = 3            // 0..3  => 4 لایه
private const val OVERLAP_STEP_FRAC = 0.25f        // هر لایه 1/4 عرض جابه‌جا + کوچک می‌شود


private fun zoomStep(z: Float) = when {
    z < 1.2f -> 0.1f
    z < 2.5f -> 0.2f
    else -> 0.5f
}
private fun snapZoom(z: Float): Float {
    val step = zoomStep(z)
    return (round(z / step) * step).coerceIn(ZOOM_MIN, ZOOM_MAX)
}

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



private fun snapStepForZoom(zoom: Float): Int {
    return if (zoom >= 2.0f) 5 else 15
}


private data class DayKey(val date: LocalDate, val dayIndex: Int)

private fun minuteOfDay(dt: LocalDateTime): Int =
    dt.toLocalTime().hour * 60 + dt.toLocalTime().minute


/**
 * فقط داخل یک روز همپوشانی را محاسبه می‌کند.
 * اگر بیشتر از ۴ همپوشانی شد، همه اضافه‌ها روی level=3 تلنبار می‌شوند.
 */
private fun computeOverlapLayouts(
    items: List<ScheduleScreenItem>,
    startDate: LocalDate,
    numDays: Int
): Map<Int, OverlapLayout> {

    // فقط آیتم‌های داخل بازه 5 روزه
    val inRange = items.filter {
        it.start.toLocalDate() >= startDate && it.start.toLocalDate() < startDate.plusDays(numDays.toLong())
    }

    // group per-day
    val groups: Map<DayKey, List<ScheduleScreenItem>> = inRange.groupBy { it ->
        val dayIndex = ChronoUnit.DAYS.between(startDate, it.start.toLocalDate()).toInt()
        DayKey(it.start.toLocalDate(), dayIndex)
    }

    val result = mutableMapOf<Int, OverlapLayout>()

    for ((_, dayItems) in groups) {
        // مرتب‌سازی برای sweep-line
        val sorted = dayItems.sortedWith(
            compareBy<ScheduleScreenItem> { minuteOfDay(it.start) }
                .thenBy { minuteOfDay(it.end) }
                .thenBy { it.scheduleId }
        )

        // endMin هر level (اگر null یعنی آزاد)
        val endByLevel = Array<Int?>(OVERLAP_MAX_LEVEL + 1) { null }

        for (it in sorted) {
            val s = minuteOfDay(it.start).coerceIn(0, 1440)
            val e = minuteOfDay(it.end).coerceIn(0, 1440)
            if (e <= s) continue

            // levelهای 0..2 را اگر آزاد بود انتخاب کن
            var chosen = -1
            for (lvl in 0 until OVERLAP_MAX_LEVEL) { // 0..2
                val activeEnd = endByLevel[lvl]
                val free = (activeEnd == null) || (s >= activeEnd)
                if (free) {
                    chosen = lvl
                    endByLevel[lvl] = e
                    break
                }
            }

            // اگر جا نبود => تلنبار روی level=3
            if (chosen == -1) {
                chosen = OVERLAP_MAX_LEVEL
                val prev = endByLevel[OVERLAP_MAX_LEVEL]
                endByLevel[OVERLAP_MAX_LEVEL] = if (prev == null) e else maxOf(prev, e)
            }

            val widthFrac = (1f - chosen * OVERLAP_STEP_FRAC).coerceAtLeast(OVERLAP_STEP_FRAC)
            val offsetFrac = (chosen * OVERLAP_STEP_FRAC).coerceIn(0f, 1f - OVERLAP_STEP_FRAC)

            val idForZ = kotlin.math.abs(it.scheduleId)
            val z = (10_000_000 + chosen * 1_000_000 + idForZ).toFloat()

            result[it.scheduleId] = OverlapLayout(
                level = chosen,
                widthFrac = widthFrac,
                offsetFrac = offsetFrac,
                z = z
            )
        }
    }

    return result
}

private fun ruleIsActiveToday(rule: ScheduleScreenItem, today: LocalDate): Boolean {
    val base = rule.dateEpochDay?.let(LocalDate::ofEpochDay) ?: return false
    if (today.isBefore(base)) return false

    val unit = rule.repeatUnit ?: return false
    val interval = (rule.repeatInterval ?: 1).coerceAtLeast(1)

    return when (unit) {
        RepeatUnit.WEEK -> {
            val mask = (rule.weekdaysMask ?: 0)
            if (mask == 0) return false

            fun bitIndex(dow: DayOfWeek): Int = when (dow) {
                DayOfWeek.SATURDAY -> 0
                DayOfWeek.SUNDAY -> 1
                DayOfWeek.MONDAY -> 2
                DayOfWeek.TUESDAY -> 3
                DayOfWeek.WEDNESDAY -> 4
                DayOfWeek.THURSDAY -> 5
                DayOfWeek.FRIDAY -> 6
            }

            val allowed = (mask and (1 shl bitIndex(today.dayOfWeek))) != 0
            if (!allowed) return false

            val weeks = ChronoUnit.WEEKS.between(base, today)
            weeks >= 0 && (weeks % interval == 0L)
        }

        RepeatUnit.DAY -> {
            val days = ChronoUnit.DAYS.between(base, today)
            days >= 0 && (days % interval == 0L)
        }

        else -> true
    }
}


private fun ruleIsActiveOnDate(rule: ScheduleScreenItem, date: LocalDate): Boolean {
    val base = rule.dateEpochDay?.let(LocalDate::ofEpochDay) ?: return false
    if (date.isBefore(base)) return false

    val unit = rule.repeatUnit ?: return false
    val interval = (rule.repeatInterval ?: 1).coerceAtLeast(1)

    return when (unit) {
        RepeatUnit.WEEK -> {
            val mask = (rule.weekdaysMask ?: 0)
            if (mask == 0) return false

            fun bitIndex(dow: DayOfWeek): Int = when (dow) {
                DayOfWeek.SATURDAY -> 0
                DayOfWeek.SUNDAY -> 1
                DayOfWeek.MONDAY -> 2
                DayOfWeek.TUESDAY -> 3
                DayOfWeek.WEDNESDAY -> 4
                DayOfWeek.THURSDAY -> 5
                DayOfWeek.FRIDAY -> 6
            }

            val allowed = (mask and (1 shl bitIndex(date.dayOfWeek))) != 0
            if (!allowed) return false

            val weeks = ChronoUnit.WEEKS.between(base, date)
            weeks >= 0 && (weeks % interval == 0L)
        }

        RepeatUnit.DAY -> {
            val days = ChronoUnit.DAYS.between(base, date)
            days >= 0 && (days % interval == 0L)
        }

        else -> {
            // فعلاً مثل DAY
            val days = ChronoUnit.DAYS.between(base, date)
            days >= 0 && (days % interval == 0L)
        }
    }
}

@Composable
private fun RunningPomodoroPanel(
    state: RunningPomodoroUiState,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onSkip: () -> Unit,
    onRestart: () -> Unit
) {
    val resumePulse by rememberInfiniteTransition(label = "resume_pulse")
        .animateFloat(
            initialValue = 1f,
            targetValue = if (state.isPaused) 1.25f else 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 550),
                repeatMode = RepeatMode.Reverse
            ),
            label = "resume_pulse_scale"
        )

    val resumeAlpha by rememberInfiniteTransition(label = "resume_alpha")
        .animateFloat(
            initialValue = 0.15f,
            targetValue = if (state.isPaused) 0.65f else 0.15f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 550),
                repeatMode = RepeatMode.Reverse
            ),
            label = "resume_pulse_alpha"
        )
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        elevation = CardDefaults.cardElevation(12.dp),
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
                .background(MaterialTheme.colorScheme.primaryContainer),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,

        ) {
            Text(state.title, style = MaterialTheme.typography.titleMedium)



            Text(
                text = "انتظار تا شروع: ${formatSeconds(state.waitingSeconds)}",
                color = Color(0xFF4CAF50)
            )

            Text(
                text = "تمرکز: ${formatSeconds(state.focusElapsedSeconds)}",
                color = Color.Red
            )

            if (state.phase == PomodoroRunPhase.BREAK || state.phase == PomodoroRunPhase.FINISHED) {
                Text(
                    text = "استراحت: ${formatSeconds(state.breakElapsedSeconds)}",
                    color = MaterialTheme.colorScheme.tertiary
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(
                    onClick = onPause,
                    enabled = !state.isPaused && state.phase != PomodoroRunPhase.FINISHED
                ) {
                    Icon(Icons.Filled.Pause, contentDescription = "Pause")
                }

                IconButton(
                    onClick = onResume,
                    enabled = state.isPaused,
                    modifier = Modifier
                        .graphicsLayer {
                            scaleX = if (state.isPaused) resumePulse else 1f
                            scaleY = if (state.isPaused) resumePulse else 1f
                        }
                        .background(
                            color = if (state.isPaused)
                                MaterialTheme.colorScheme.primary.copy(alpha = resumeAlpha)
                            else
                                Color.Transparent,
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = "Resume",
                        tint = if (state.isPaused)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    )
                }

                IconButton(
                    onClick = onSkip,
                    enabled = state.phase != PomodoroRunPhase.FINISHED
                ) {
                    Icon(
                        imageVector = Icons.Filled.SkipNext,
                        contentDescription = "Skip"
                    )
                }

                IconButton(
                    onClick = onRestart,
                    enabled = state.phase != PomodoroRunPhase.FINISHED
                ) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = "Restart"
                    )
                }
            }
        }
    }
}

private fun formatSeconds(totalSeconds: Long): String {
    val m = totalSeconds / 60
    val s = totalSeconds % 60
    return "%02d:%02d".format(m, s)
}