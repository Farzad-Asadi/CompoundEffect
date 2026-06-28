package com.example.compoundeffectV1_01.ui.scheduleScreen


import android.util.Log
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.compoundeffectV1_01.data.alarm.PomodoroAlarmReceiver
import com.example.compoundeffectV1_01.data.alarm.PomodoroAlarmScheduler
import com.example.compoundeffectV1_01.data.dataBaseRoom.tables.reminder.TaskReminderRepository
import com.example.compoundeffectV1_01.data.dataBaseRoom.tables.task.TaskChildRepository
import com.example.compoundeffectV1_01.data.dataBaseRoom.tables.task.TaskChildRequirementSummaryUi
import com.example.compoundeffectV1_01.data.dataBaseRoom.tables.task.TaskChildRequirementUi
import com.example.compoundeffectV1_01.data.dataBaseRoom.tables.task.TaskMode
import com.example.compoundeffectV1_01.data.dataBaseRoom.tables.task.TaskRepository
import com.example.compoundeffectV1_01.data.dataBaseRoom.tables.taskSchedule.RepeatUnit
import com.example.compoundeffectV1_01.data.dataBaseRoom.tables.taskSchedule.ScheduleMode
import com.example.compoundeffectV1_01.data.dataBaseRoom.tables.taskSchedule.TaskSchedule
import com.example.compoundeffectV1_01.data.dataBaseRoom.tables.taskSchedule.TaskScheduleRepository
import com.example.compoundeffectV1_01.data.dataStore.AppPreferences
import com.example.compoundeffectV1_01.data.workManager.ReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject



@HiltViewModel
class ScheduleScreenViewModel @Inject constructor(
    private val taskRepo: TaskRepository,
    private val taskChildRepo: TaskChildRepository,
    private val taskScheduleRepo: TaskScheduleRepository,
    private val reminderScheduler: ReminderScheduler,
    private val reminderRepo: TaskReminderRepository,
    private val pomodoroAlarmScheduler: PomodoroAlarmScheduler,
    private val appPreferences: AppPreferences
) : ViewModel() {

    private var runningPomodoroJob: Job? = null
    val hasAskedSchedulePermissions =
        appPreferences.hasAskedSchedulePermissions
            .map<Boolean, Boolean?> { it }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                null
            )

    val scheduleVerticalZoom =
        appPreferences.scheduleVerticalZoom
            .map<Float, Float?> { it }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                null
            )

    private var autoPomodoroWatcherJob: Job? = null
    private val autoSuppressedPomodoroIds = mutableSetOf<Int>()


    private val _runningPomodoro = MutableStateFlow<RunningPomodoroUiState?>(null)
    val runningPomodoro = _runningPomodoro.asStateFlow()

    private var lastEnsuredTaskChildOccurrenceKeys: Set<String> = emptySet()

    val allItems: StateFlow<List<ScheduleScreenItem>> =
        taskScheduleRepo.observeAllSchedulesWithTask()
            .map { rows ->
                rows.mapNotNull { r ->
                    val date = r.s_dateEpochDay?.let(LocalDate::ofEpochDay) ?: return@mapNotNull null
                    val s = r.s_startMinuteOfDay ?: return@mapNotNull null
                    val e = r.s_endMinuteOfDay ?: return@mapNotNull null
                    if (e <= s) return@mapNotNull null

                    val start = date.atStartOfDay().plusMinutes(s.toLong())
                    val end   = date.atStartOfDay().plusMinutes(e.toLong())

                    ScheduleScreenItem(
                        scheduleId = r.s_id,
                        taskId = r.t_id,
                        title = r.t_name,
                        mode = r.s_mode,

                        dateEpochDay = r.s_dateEpochDay,
                        start = start,
                        end = end,

                        durationMinutes = r.s_durationMinutes,

                        inPallet = r.s_inPallet,
                        repeating = r.s_repeating,

                        categoryName = r.c_name,
                        categoryIconName = r.c_iconName,
                        categoryColor = r.c_color,
                        taskMode = r.t_taskMode,
                        pomodoroTargetUnits = r.t_pomodoroTargetUnits      ,
                        pomodoroDoneUnits =r.t_pomodoroDoneUnits,
                        repeatInterval = r.s_repeatInterval,
                        repeatUnit = r.s_repeatUnit,
                        weekdaysMask = r.s_weekdaysMask,
                        focusMinutes = r.s_focusMinutes,
                        shortBreakMinutes = r.s_shortBreakMinutes,
                        longBreakMinutes = r.s_longBreakMinutes,
                        longBreakEvery = r.s_longBreakEvery,
                        pomodoroUnitsPerDay = r.s_pomodoroUnitsPerDay,
                        parentRuleScheduleId = r.s_parentRuleScheduleId,
                        occurrenceDateEpochDay = r.s_occurrenceDateEpochDay,
                        pomodoroFocusDoneApplied = r.s_pomodoroFocusDoneApplied,
                    )
                }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val pomodoroDailyAdjustments =
        taskRepo.observePomodoroDailyAdjustments()
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                emptyList()
            )

    private val _taskChildSheetState = MutableStateFlow<TaskChildSheetState?>(null)
    val taskChildSheetState = _taskChildSheetState.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val taskChildSheetRequirements: StateFlow<List<TaskChildRequirementUi>> =
        _taskChildSheetState
            .flatMapLatest { state ->
                if (state == null) {
                    flowOf(emptyList())
                } else {
                    taskChildRepo.observeRequirementUiForParentOccurrence(
                        parentTaskId = state.parentTaskId,
                        scheduleId = state.scheduleId,
                        parentRuleScheduleId = state.parentRuleScheduleId,
                        occurrenceDateEpochDay = state.occurrenceDateEpochDay
                    )
                }
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                emptyList()
            )

    private val _taskChildVisibleRange =
        MutableStateFlow<TaskChildVisibleRange?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val taskChildRequirementSummaries: StateFlow<List<TaskChildRequirementSummaryUi>> =
        _taskChildVisibleRange
            .flatMapLatest { range ->
                if (range == null) {
                    flowOf(emptyList())
                } else {
                    taskChildRepo.observeRequirementSummariesByDateRange(
                        startEpochDay = range.startEpochDay,
                        endEpochDay = range.endEpochDay
                    )
                }
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                emptyList()
            )

    @OptIn(ExperimentalCoroutinesApi::class)
    val taskChildRequirementPreviews: StateFlow<List<TaskChildRequirementUi>> =
        _taskChildVisibleRange
            .flatMapLatest { range ->
                if (range == null) {
                    flowOf(emptyList())
                } else {
                    taskChildRepo.observeRequirementUiByDateRange(
                        startEpochDay = range.startEpochDay,
                        endEpochDay = range.endEpochDay
                    )
                }
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                emptyList()
            )

    init {
        startPomodoroAutoWatcher()
        startTaskChildRequirementAutoGenerator()
    }

    private fun startTaskChildRequirementAutoGenerator() {
        viewModelScope.launch {
            allItems.collect { items ->

                val occurrenceInputs = items
                    .asSequence()
                    .filter { !it.inPallet }
                    .mapNotNull { item ->
                        val parentTaskId = item.taskId

                        val isVirtualOccurrence =
                            item.parentRuleScheduleId != null &&
                                    item.occurrenceDateEpochDay != null &&
                                    (item.scheduleId < 0 || item.scheduleId == item.parentRuleScheduleId)

                        val realScheduleId =
                            if (!isVirtualOccurrence && item.scheduleId > 0) {
                                item.scheduleId
                            } else {
                                null
                            }

                        val virtualParentRuleScheduleId =
                            if (realScheduleId == null) {
                                item.parentRuleScheduleId
                            } else {
                                null
                            }

                        if (realScheduleId == null && virtualParentRuleScheduleId == null) {
                            return@mapNotNull null
                        }

                        val occurrenceDateEpochDay =
                            item.occurrenceDateEpochDay
                                ?: item.dateEpochDay
                                ?: item.start.toLocalDate().toEpochDay()

                        TaskChildOccurrenceEnsureInput(
                            parentTaskId = parentTaskId,
                            scheduleId = realScheduleId,
                            parentRuleScheduleId = virtualParentRuleScheduleId,
                            occurrenceDateEpochDay = occurrenceDateEpochDay
                        )
                    }
                    .distinctBy {
                        "${it.parentTaskId}|${it.scheduleId}|${it.parentRuleScheduleId}|${it.occurrenceDateEpochDay}"
                    }
                    .toList()

                val currentKeys = occurrenceInputs
                    .map {
                        "${it.parentTaskId}|${it.scheduleId}|${it.parentRuleScheduleId}|${it.occurrenceDateEpochDay}"
                    }
                    .toSet()

                if (currentKeys == lastEnsuredTaskChildOccurrenceKeys) {
                    return@collect
                }

                lastEnsuredTaskChildOccurrenceKeys = currentKeys

                withContext(Dispatchers.IO) {
                    occurrenceInputs.forEach { input ->
                        taskChildRepo.ensureRequirementsForParentOccurrence(
                            parentTaskId = input.parentTaskId,
                            scheduleId = input.scheduleId,
                            parentRuleScheduleId = input.parentRuleScheduleId,
                            occurrenceDateEpochDay = input.occurrenceDateEpochDay
                        )
                    }
                }
            }
        }
    }

    fun openTaskChildSheet(item: ScheduleScreenItem) {
        val state = buildTaskChildSheetState(item) ?: return

        viewModelScope.launch(Dispatchers.IO) {
            taskChildRepo.ensureRequirementsForParentOccurrence(
                parentTaskId = state.parentTaskId,
                scheduleId = state.scheduleId,
                parentRuleScheduleId = state.parentRuleScheduleId,
                occurrenceDateEpochDay = state.occurrenceDateEpochDay
            )

            _taskChildSheetState.value = state
        }
    }

    fun closeTaskChildSheet() {
        _taskChildSheetState.value = null
    }

    fun toggleTaskChildRequirementCompleted(
        requirementId: Int,
        completed: Boolean
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            taskChildRepo.toggleRequirementCompletedById(
                requirementId = requirementId,
                completed = completed
            )
        }
    }

    fun setTaskChildVisibleRange(
        startEpochDay: Long,
        endEpochDay: Long
    ) {
        val newRange = TaskChildVisibleRange(
            startEpochDay = startEpochDay,
            endEpochDay = endEpochDay
        )

        if (_taskChildVisibleRange.value == newRange) return

        _taskChildVisibleRange.value = newRange
    }

    private fun buildTaskChildSheetState(
        item: ScheduleScreenItem
    ): TaskChildSheetState? {
        val occurrenceDateEpochDay =
            item.occurrenceDateEpochDay
                ?: item.dateEpochDay
                ?: item.start.toLocalDate().toEpochDay()

        val isVirtualOccurrence =
            item.parentRuleScheduleId != null &&
                    item.occurrenceDateEpochDay != null &&
                    (item.scheduleId < 0 || item.scheduleId == item.parentRuleScheduleId)

        val realScheduleId =
            if (!isVirtualOccurrence && item.scheduleId > 0) {
                item.scheduleId
            } else {
                null
            }

        val ruleScheduleId =
            if (realScheduleId == null) {
                item.parentRuleScheduleId
            } else {
                null
            }

        if (realScheduleId == null && ruleScheduleId == null) {
            return null
        }

        return TaskChildSheetState(
            parentTaskId = item.taskId,
            title = item.title,
            scheduleId = realScheduleId,
            parentRuleScheduleId = ruleScheduleId,
            occurrenceDateEpochDay = occurrenceDateEpochDay
        )
    }


    //common


    fun deleteScheduleById(scheduleId: Int) {
        viewModelScope.launch(Dispatchers.IO) {

            pomodoroAlarmScheduler.cancelPomodoroEvents(scheduleId)

            taskScheduleRepo.deleteById(scheduleId)
            val reminders = reminderRepo.getByScheduleId(scheduleId)
            reminders.forEach { rUi ->
                try {
                    reminderScheduler.cancel(rUi.id)   // ✅ این خط حیاتی است
                } catch (_: Throwable) {}
            }
        }
    }



    fun materializeVirtual(
        virtual: ScheduleScreenItem,
        newDate: LocalDate,
        newStartMin: Int,
        newEndMin: Int,
        inPallet: Boolean = false
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val ruleId = virtual.parentRuleScheduleId ?: return@launch
            val occDay = virtual.occurrenceDateEpochDay ?: return@launch

            // اینجا باید بسته به ساختار Entity شما پر بشه:
            val entity = TaskSchedule(
                // id اگر auto است null بزن
                id = null, // یا null (طبق Entity خودت)
                taskId = virtual.taskId,
                mode = virtual.mode,

                dateEpochDay = newDate.toEpochDay(),
                startMinuteOfDay = newStartMin,
                endMinuteOfDay = newEndMin,

                inPallet = inPallet,
                repeating = false,

                parentRuleScheduleId = ruleId,
                occurrenceDateEpochDay = occDay,
            )

            taskScheduleRepo.insert(entity)
        }
    }

    fun startPomodoroNow(scheduleId: Int) {
        autoSuppressedPomodoroIds.remove(scheduleId)

        viewModelScope.launch(Dispatchers.IO) {
            val schedule = taskScheduleRepo.getById(scheduleId) ?: return@launch
            if (schedule.mode != ScheduleMode.POMODORO) return@launch

            val focus = schedule.focusMinutes ?: 25
            val shortBreak = schedule.shortBreakMinutes ?: 0

            val rawNow = LocalDateTime.now()

            // مثل Restart، شروع را روی دقیقه‌ی بعدی تمیز می‌کنیم
            val now = if (rawNow.second > 0 || rawNow.nano > 0) {
                rawNow.plusMinutes(1).withSecond(0).withNano(0)
            } else {
                rawNow.withSecond(0).withNano(0)
            }

            val date = now.toLocalDate()
            val startMin = now.hour * 60 + now.minute
            val endMin = (startMin + focus + shortBreak).coerceAtMost(24 * 60)

            // ✅ مهم:
            // به جای اینکه فقط همین schedule جابه‌جا شود،
            // کل زنجیره‌ی پومودوروهای چسبیده‌ی بعد از آن هم همراهش جابه‌جا می‌شود.
            movePomodoroChainForward(
                scheduleId = scheduleId,
                newDate = date,
                newStartMin = startMin,
                newEndMin = endMin
            )

            val updatedSchedule = taskScheduleRepo.getById(scheduleId) ?: return@launch

            startRunningPomodoroTimer(
                schedule = updatedSchedule,
                clickedAt = rawNow,
                realStartAt = now,
                focusMinutes = focus,
                shortBreakMinutes = shortBreak
            )
        }
    }

    private fun startRunningPomodoroTimer(
        schedule: TaskSchedule,
        clickedAt: LocalDateTime,
        realStartAt: LocalDateTime,
        focusMinutes: Int,
        shortBreakMinutes: Int
    ) {
        val focusEndAt = realStartAt.plusMinutes(focusMinutes.toLong())
        val breakEndAt = focusEndAt.plusMinutes(shortBreakMinutes.toLong())

        val initialState = RunningPomodoroUiState(
            scheduleId = schedule.id ?: return,
            taskId = schedule.taskId,
            title = schedule.title ?: "Pomodoro",
            clickedAt = clickedAt,
            realStartAt = realStartAt,
            focusEndAt = focusEndAt,
            breakEndAt = breakEndAt,
            phase = PomodoroRunPhase.WAITING_TO_START,
            waitingSeconds = Duration.between(LocalDateTime.now(), realStartAt).seconds.coerceAtLeast(0),
            focusElapsedSeconds = 0,
            breakElapsedSeconds = 0,
            isPaused = false,
            pauseAt = null
        )

        _runningPomodoro.value = initialState
        cancelStartSoonForForwardChainChildren(initialState.scheduleId)
        scheduleRunningPomodoroAlarms(initialState)
        startRunningPomodoroTicker(initialState)
    }

    private fun scheduleRunningPomodoroAlarms(state: RunningPomodoroUiState) {
        // فقط آلارم‌های همین پومودوروی در حال اجرا را عوض کن
        // نه کل آلارم‌های پومودوروهای بعدی
        pomodoroAlarmScheduler.cancelPomodoroEvents(state.scheduleId)

        val nowMillis = System.currentTimeMillis()

        fun scheduleIfFuture(type: String, at: LocalDateTime) {
            val millis = at.atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()

            if (millis > nowMillis) {
                pomodoroAlarmScheduler.schedulePomodoroEvent(
                    scheduleId = state.scheduleId,
                    title = state.title,
                    type = type,
                    triggerAtMillis = millis
                )
            }
        }

        when (state.phase) {
            PomodoroRunPhase.WAITING_TO_START -> {
                scheduleIfFuture(PomodoroAlarmReceiver.TYPE_FOCUS_START, state.realStartAt)
                scheduleIfFuture(PomodoroAlarmReceiver.TYPE_FOCUS_END, state.focusEndAt)
                scheduleIfFuture(PomodoroAlarmReceiver.TYPE_BREAK_END, state.breakEndAt)
            }

            PomodoroRunPhase.FOCUS -> {
                scheduleIfFuture(PomodoroAlarmReceiver.TYPE_FOCUS_END, state.focusEndAt)
                scheduleIfFuture(PomodoroAlarmReceiver.TYPE_BREAK_END, state.breakEndAt)
            }

            PomodoroRunPhase.BREAK -> {
                scheduleIfFuture(PomodoroAlarmReceiver.TYPE_BREAK_END, state.breakEndAt)
            }

            PomodoroRunPhase.FINISHED -> Unit
        }
    }

    private fun startRunningPomodoroTicker(initialState: RunningPomodoroUiState) {
        runningPomodoroJob?.cancel()

        runningPomodoroJob = viewModelScope.launch {
            while (true) {
                val now = LocalDateTime.now()

                val phase = when {
                    now.isBefore(initialState.realStartAt) -> PomodoroRunPhase.WAITING_TO_START
                    now.isBefore(initialState.focusEndAt) -> PomodoroRunPhase.FOCUS
                    now.isBefore(initialState.breakEndAt) -> PomodoroRunPhase.BREAK
                    else -> PomodoroRunPhase.FINISHED
                }

                val currentStateBeforeUpdate = _runningPomodoro.value ?: initialState

                val shouldApplyDone =
                    !currentStateBeforeUpdate.focusDoneApplied &&
                            (phase == PomodoroRunPhase.BREAK || phase == PomodoroRunPhase.FINISHED) &&
                            !now.isBefore(currentStateBeforeUpdate.focusEndAt)

                if (shouldApplyDone) {
                    withContext(Dispatchers.IO) {
                        val marked = taskScheduleRepo.markPomodoroFocusDoneIfNeeded(
                            currentStateBeforeUpdate.scheduleId
                        )

                        if (marked) {
                            taskRepo.incrementPomodoroDoneUnits(
                                currentStateBeforeUpdate.taskId
                            )
                        }
                    }
                }

                val waitingSeconds = if (now.isBefore(initialState.realStartAt)) {
                    Duration.between(now, initialState.realStartAt).seconds.coerceAtLeast(0)
                } else {
                    0
                }

                val focusElapsed = Duration.between(initialState.realStartAt, now)
                    .seconds
                    .coerceIn(0, Duration.between(initialState.realStartAt, initialState.focusEndAt).seconds)

                val breakElapsed = Duration.between(initialState.focusEndAt, now)
                    .seconds
                    .coerceIn(0, Duration.between(initialState.focusEndAt, initialState.breakEndAt).seconds)

                _runningPomodoro.value = initialState.copy(
                    phase = phase,
                    waitingSeconds = waitingSeconds,
                    focusElapsedSeconds = focusElapsed,
                    breakElapsedSeconds = breakElapsed,
                    isPaused = false,
                    pauseAt = null,
                    focusDoneApplied = currentStateBeforeUpdate.focusDoneApplied || shouldApplyDone,
                )

                if (phase == PomodoroRunPhase.FINISHED) {
                    delay(3_000)
                    _runningPomodoro.value = null
                    break
                }

                delay(1_000)
            }
        }
    }

    fun pauseRunningPomodoro() {
        val current = _runningPomodoro.value ?: return

        if (current.isPaused || current.phase == PomodoroRunPhase.FINISHED) return

        runningPomodoroJob?.cancel()

        _runningPomodoro.value = current.copy(
            isPaused = true,
            pauseAt = LocalDateTime.now()
        )

        viewModelScope.launch(Dispatchers.IO) {
            cancelForwardPomodoroChainAlarms(current.scheduleId)
        }
    }

    fun resumeRunningPomodoro() {
        val current = _runningPomodoro.value ?: return

        if (!current.isPaused || current.phase == PomodoroRunPhase.FINISHED) return

        val pauseAt = current.pauseAt ?: return
        val now = LocalDateTime.now()

        val pauseDurationRaw = Duration.between(pauseAt, now)

        val pauseDuration =
            if (pauseDurationRaw.isNegative || pauseDurationRaw.isZero) {
                Duration.ZERO
            } else {
                pauseDurationRaw
            }

        // برای تایمر، تأخیر دقیق ثانیه‌ای لازم داریم
        // تا elapsed بعد از Resume نپرد.
        val timerDelay = pauseDuration

        // برای کارت تایم‌لاین، چون واحد زمان‌بندی دقیقه است،
        // تأخیر را به دقیقه‌ی کامل رو به بالا تبدیل می‌کنیم.
        val scheduleDelayMinutes = durationToScheduleDelayMinutes(pauseDuration)

        val resumedState = when (current.phase) {
            PomodoroRunPhase.WAITING_TO_START,
            PomodoroRunPhase.FOCUS,
            PomodoroRunPhase.BREAK -> {
                current.copy(
                    // مهم:
                    // این سه مقدار برای محاسبه‌ی درست elapsed باید با pauseDuration دقیق شیفت شوند.
                    realStartAt = current.realStartAt.plus(timerDelay),
                    focusEndAt = current.focusEndAt.plus(timerDelay),
                    breakEndAt = current.breakEndAt.plus(timerDelay),

                    isPaused = false,
                    pauseAt = null
                )
            }

            PomodoroRunPhase.FINISHED -> return
        }

        _runningPomodoro.value = resumedState

        viewModelScope.launch {
            if (scheduleDelayMinutes > 0) {
                withContext(Dispatchers.IO) {
                    val schedule = taskScheduleRepo.getById(current.scheduleId)
                        ?: return@withContext

                    val date = schedule.dateEpochDay
                        ?.let(LocalDate::ofEpochDay)
                        ?: return@withContext

                    val oldStart = schedule.startMinuteOfDay ?: return@withContext
                    val oldEnd = schedule.endMinuteOfDay ?: return@withContext

                    val newStart: Int
                    val newEnd: Int

                    when (current.phase) {
                        PomodoroRunPhase.WAITING_TO_START -> {
                            // هنوز شروع نشده:
                            // کل کارت فعلی و زنجیره‌ی بعدی جابه‌جا شوند.
                            newStart = (oldStart + scheduleDelayMinutes.toInt())
                                .coerceIn(DAY_MIN, DAY_MAX)

                            newEnd = (oldEnd + scheduleDelayMinutes.toInt())
                                .coerceIn(DAY_MIN, DAY_MAX)
                        }

                        PomodoroRunPhase.FOCUS,
                        PomodoroRunPhase.BREAK -> {
                            // شروع شده:
                            // start ثابت می‌ماند، end کش می‌آید.
                            // movePomodoroChainForward بچه‌های چسبیده‌ی زیرش را از end جدید ادامه می‌دهد.
                            newStart = oldStart

                            newEnd = (oldEnd + scheduleDelayMinutes.toInt())
                                .coerceIn(DAY_MIN, DAY_MAX)
                        }

                        PomodoroRunPhase.FINISHED -> return@withContext
                    }

                    movePomodoroChainForward(
                        scheduleId = current.scheduleId,
                        newDate = date,
                        newStartMin = newStart,
                        newEndMin = newEnd
                    )
                }
            }

            scheduleRunningPomodoroAlarms(resumedState)
            startRunningPomodoroTicker(resumedState)
        }
    }

    fun skipRunningPomodoro() {
        val current = _runningPomodoro.value ?: return

        if (current.phase == PomodoroRunPhase.FINISHED) return

        autoSuppressedPomodoroIds += current.scheduleId

        runningPomodoroJob?.cancel()
        pomodoroAlarmScheduler.cancelAll()

        val now = LocalDateTime.now()

        val focusDuration = Duration
            .between(current.realStartAt, current.focusEndAt)
            .takeIf { it.seconds > 0 }
            ?: Duration.ofMinutes(25)

        val breakDuration = Duration
            .between(current.focusEndAt, current.breakEndAt)
            .takeIf { it.seconds > 0 }
            ?: Duration.ZERO

        when (current.phase) {

            PomodoroRunPhase.WAITING_TO_START -> {
                val newFocusEndAt = now.plus(focusDuration)
                val newBreakEndAt = newFocusEndAt.plus(breakDuration)

                val newState = current.copy(
                    realStartAt = now,
                    focusEndAt = newFocusEndAt,
                    breakEndAt = newBreakEndAt,
                    phase = PomodoroRunPhase.FOCUS,
                    waitingSeconds = 0,
                    focusElapsedSeconds = 0,
                    breakElapsedSeconds = 0,
                    isPaused = false,
                    pauseAt = null
                )

                _runningPomodoro.value = newState
                scheduleRunningPomodoroAlarms(newState)
                startRunningPomodoroTicker(newState)
            }

            PomodoroRunPhase.FOCUS -> {
                if (breakDuration.seconds <= 0) {
                    _runningPomodoro.value = current.copy(
                        phase = PomodoroRunPhase.FINISHED,
                        waitingSeconds = 0,
                        focusElapsedSeconds = focusDuration.seconds,
                        breakElapsedSeconds = 0,
                        isPaused = false,
                        pauseAt = null,

                        // چون کاربر وسط Focus اسکیپ زده، نباید Done ثبت شود.
                        // این true فقط جلوی ثبت Done توسط ticker را می‌گیرد.
                        focusDoneApplied = true
                    )

                    viewModelScope.launch {
                        delay(1_500)
                        _runningPomodoro.value = null
                    }

                    return
                }

                val newBreakEndAt = now.plus(breakDuration)

                val newState = current.copy(
                    focusEndAt = now,
                    breakEndAt = newBreakEndAt,
                    phase = PomodoroRunPhase.BREAK,
                    waitingSeconds = 0,
                    focusElapsedSeconds = focusDuration.seconds,
                    breakElapsedSeconds = 0,
                    isPaused = false,
                    pauseAt = null,

                    // چون کاربر Focus را کامل نکرده و اسکیپ زده،
                    // ticker نباید این ورود به Break را به عنوان پایان طبیعی Focus حساب کند.
                    focusDoneApplied = true
                )

                _runningPomodoro.value = newState
                scheduleRunningPomodoroAlarms(newState)
                startRunningPomodoroTicker(newState)
            }

            PomodoroRunPhase.BREAK -> {
                _runningPomodoro.value = current.copy(
                    phase = PomodoroRunPhase.FINISHED,
                    waitingSeconds = 0,
                    focusElapsedSeconds = focusDuration.seconds,
                    breakElapsedSeconds = breakDuration.seconds,
                    isPaused = false,
                    pauseAt = null
                )

                viewModelScope.launch {
                    delay(1_500)
                    _runningPomodoro.value = null
                }
            }

            PomodoroRunPhase.FINISHED -> Unit
        }
    }

    fun restartRunningPomodoro() {
        val current = _runningPomodoro.value ?: return

        autoSuppressedPomodoroIds.remove(current.scheduleId)

        if (current.phase == PomodoroRunPhase.FINISHED) return

        viewModelScope.launch(Dispatchers.IO) {
            runningPomodoroJob?.cancel()
            pomodoroAlarmScheduler.cancelAll()

            val schedule = taskScheduleRepo.getById(current.scheduleId) ?: return@launch
            if (schedule.mode != ScheduleMode.POMODORO) return@launch

            val focus = schedule.focusMinutes ?: 25
            val shortBreak = schedule.shortBreakMinutes ?: 0

            val rawNow = LocalDateTime.now()

            val now = if (rawNow.second > 0 || rawNow.nano > 0) {
                rawNow.plusMinutes(1).withSecond(0).withNano(0)
            } else {
                rawNow.withSecond(0).withNano(0)
            }

            val date = now.toLocalDate()
            val startMin = now.hour * 60 + now.minute
            val endMin = (startMin + focus + shortBreak).coerceAtMost(24 * 60)

            taskScheduleRepo.updatePomodoroTimeRange(
                scheduleId = current.scheduleId,
                date = date,
                startMin = startMin,
                endMin = endMin
            )

            val focusEndAt = now.plusMinutes(focus.toLong())
            val breakEndAt = focusEndAt.plusMinutes(shortBreak.toLong())

            val restartedState = RunningPomodoroUiState(
                scheduleId = current.scheduleId,
                taskId = current.taskId,
                title = current.title,
                clickedAt = rawNow,
                realStartAt = now,
                focusEndAt = focusEndAt,
                breakEndAt = breakEndAt,
                phase = PomodoroRunPhase.WAITING_TO_START,
                waitingSeconds = Duration.between(LocalDateTime.now(), now).seconds.coerceAtLeast(0),
                focusElapsedSeconds = 0,
                breakElapsedSeconds = 0,
                isPaused = false,
                pauseAt = null
            )

            _runningPomodoro.value = restartedState

            scheduleRunningPomodoroAlarms(restartedState)
            startRunningPomodoroTicker(restartedState)
        }
    }

    fun markSchedulePermissionsAsked() {
        viewModelScope.launch {
            appPreferences.setHasAskedSchedulePermissions(true)
        }
    }

    private suspend fun applyPomodoroDoneIfNeeded(state: RunningPomodoroUiState) {
        if (state.focusDoneApplied) return

        taskRepo.incrementPomodoroDoneUnits(state.taskId)

        _runningPomodoro.value = state.copy(
            focusDoneApplied = true
        )
    }

    private suspend fun schedulePomodoroTimelineAlarms(schedule: TaskSchedule) {
        val scheduleId = schedule.id ?: return
        if (schedule.mode != ScheduleMode.POMODORO) return
        if (schedule.inPallet) return

        val dateEpochDay = schedule.dateEpochDay ?: return
        val startMin = schedule.startMinuteOfDay ?: return
        val focus = schedule.focusMinutes ?: return
        val shortBreak = schedule.shortBreakMinutes ?: 0

        val taskTitle =
            taskRepo.getTaskById(schedule.taskId)?.name
                ?: schedule.title
                ?: "Pomodoro"

        val startAt = LocalDate.ofEpochDay(dateEpochDay)
            .atStartOfDay()
            .plusMinutes(startMin.toLong())

        val focusEndAt = startAt.plusMinutes(focus.toLong())
        val breakEndAt = focusEndAt.plusMinutes(shortBreak.toLong())

        val nowMillis = System.currentTimeMillis()

        fun LocalDateTime.toMillis(): Long {
            return this.atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        }

        fun scheduleIfFuture(type: String, at: LocalDateTime) {
            val millis = at.toMillis()
            if (millis > nowMillis) {
                pomodoroAlarmScheduler.schedulePomodoroEvent(
                    scheduleId = scheduleId,
                    title = taskTitle,
                    type = type,
                    triggerAtMillis = millis
                )
            }
        }

        // ✅ اگر کمتر از یک دقیقه مانده، هشدار را تقریباً فوراً نشان بده.
        if (startAt.toMillis() > nowMillis) {
            val warningAtMillis = maxOf(
                startAt.minusMinutes(1).toMillis(),
                nowMillis + 1_000L
            )

            pomodoroAlarmScheduler.schedulePomodoroEvent(
                scheduleId = scheduleId,
                title = taskTitle,
                type = PomodoroAlarmReceiver.TYPE_START_SOON,
                triggerAtMillis = warningAtMillis
            )
        }

        scheduleIfFuture(PomodoroAlarmReceiver.TYPE_FOCUS_START, startAt)
        scheduleIfFuture(PomodoroAlarmReceiver.TYPE_FOCUS_END, focusEndAt)

        if (shortBreak > 0) {
            scheduleIfFuture(PomodoroAlarmReceiver.TYPE_BREAK_END, breakEndAt)
        }
    }

    private fun startPomodoroAutoWatcher() {
        if (autoPomodoroWatcherJob != null) return

        autoPomodoroWatcherJob = viewModelScope.launch {
            while (true) {
                val current = _runningPomodoro.value

                // اگر تایمر دستی/اتوماتیک در حال اجراست، دخالت نکن
                if (current == null) {
                    val now = LocalDateTime.now()
                    val candidate = findAutoRunnablePomodoro(
                        items = allItems.value,
                        now = now
                    )

                    if (candidate != null) {
                        startRunningPomodoroFromTimelineItem(
                            item = candidate,
                            now = now
                        )
                    }
                }

                delay(1_000)
            }
        }
    }

    private fun findAutoRunnablePomodoro(
        items: List<ScheduleScreenItem>,
        now: LocalDateTime
    ): ScheduleScreenItem? {
        return items
            .asSequence()
            .filter { it.mode == ScheduleMode.POMODORO }
            .filter { !it.inPallet }
            .filter { it.scheduleId !in autoSuppressedPomodoroIds }
            .filter { item ->
                val focus = item.focusMinutes ?: 25
                val shortBreak = item.shortBreakMinutes ?: 0

                val startAt = item.start
                val focusEndAt = startAt.plusMinutes(focus.toLong())
                val breakEndAt = focusEndAt.plusMinutes(shortBreak.toLong())

                val autoActivationAt = startAt.minusSeconds(60)

                // از یک دقیقه قبل شروع تا پایان Break، تایمر قابل فعال‌سازی است
                !now.isBefore(autoActivationAt) && now.isBefore(breakEndAt)
            }
            .sortedBy { it.start }
            .firstOrNull()
    }

    private fun startRunningPomodoroFromTimelineItem(
        item: ScheduleScreenItem,
        now: LocalDateTime
    ) {
        val focus = item.focusMinutes ?: 25
        val shortBreak = item.shortBreakMinutes ?: 0

        val realStartAt = item.start
        val focusEndAt = realStartAt.plusMinutes(focus.toLong())
        val breakEndAt = focusEndAt.plusMinutes(shortBreak.toLong())

        val phase = when {
            now.isBefore(realStartAt) -> PomodoroRunPhase.WAITING_TO_START
            now.isBefore(focusEndAt) -> PomodoroRunPhase.FOCUS
            now.isBefore(breakEndAt) -> PomodoroRunPhase.BREAK
            else -> PomodoroRunPhase.FINISHED
        }

        if (phase == PomodoroRunPhase.FINISHED) return

        val waitingSeconds =
            if (now.isBefore(realStartAt)) {
                Duration.between(now, realStartAt).seconds.coerceAtLeast(0)
            } else {
                0
            }

        val focusTotalSeconds = Duration
            .between(realStartAt, focusEndAt)
            .seconds
            .coerceAtLeast(0)

        val breakTotalSeconds = Duration
            .between(focusEndAt, breakEndAt)
            .seconds
            .coerceAtLeast(0)

        val focusElapsed = Duration
            .between(realStartAt, now)
            .seconds
            .coerceIn(0, focusTotalSeconds)

        val breakElapsed = Duration
            .between(focusEndAt, now)
            .seconds
            .coerceIn(0, breakTotalSeconds)

        val initialState = RunningPomodoroUiState(
            scheduleId = item.scheduleId,
            taskId = item.taskId,
            title = item.title,
            clickedAt = now,
            realStartAt = realStartAt,
            focusEndAt = focusEndAt,
            breakEndAt = breakEndAt,
            phase = phase,
            waitingSeconds = waitingSeconds,
            focusElapsedSeconds = focusElapsed,
            breakElapsedSeconds = breakElapsed,
            focusDoneApplied = false,
            isPaused = false,
            pauseAt = null
        )

        _runningPomodoro.value = initialState
        cancelStartSoonForForwardChainChildren(initialState.scheduleId)
        scheduleRunningPomodoroAlarms(initialState)
        startRunningPomodoroTicker(initialState)
    }

    private data class PomodoroChainNode(
        val scheduleId: Int,
        val date: LocalDate,
        val startMin: Int,
        val endMin: Int
    ) {
        val durationMin: Int
            get() = (endMin - startMin).coerceAtLeast(1)
    }

    private fun minuteOfDay(dateTime: LocalDateTime): Int {
        val t = dateTime.toLocalTime()
        return t.hour * 60 + t.minute
    }

    private fun findForwardPomodoroChain(scheduleId: Int): List<PomodoroChainNode> {
        val items = allItems.value

        val current = items.firstOrNull {
            it.scheduleId == scheduleId &&
                    it.scheduleId > 0 &&
                    it.mode == ScheduleMode.POMODORO &&
                    !it.inPallet
        } ?: return emptyList()

        val date = current.start.toLocalDate()

        val sameDayPomodoros = items
            .filter {
                it.scheduleId > 0 &&
                        it.mode == ScheduleMode.POMODORO &&
                        !it.inPallet &&
                        it.start.toLocalDate() == date
            }
            .sortedWith(
                compareBy<ScheduleScreenItem> { minuteOfDay(it.start) }
                    .thenBy { minuteOfDay(it.end) }
                    .thenBy { it.scheduleId }
            )

        fun toNode(item: ScheduleScreenItem): PomodoroChainNode {
            return PomodoroChainNode(
                scheduleId = item.scheduleId,
                date = item.start.toLocalDate(),
                startMin = minuteOfDay(item.start),
                endMin = minuteOfDay(item.end)
            )
        }

        val result = mutableListOf<PomodoroChainNode>()
        val usedIds = mutableSetOf<Int>()

        var cursor = current
        while (true) {
            val node = toNode(cursor)
            result += node
            usedIds += cursor.scheduleId

            val cursorEnd = minuteOfDay(cursor.end)

            val next = sameDayPomodoros.firstOrNull {
                it.scheduleId !in usedIds &&
                        minuteOfDay(it.start) == cursorEnd
            } ?: break

            cursor = next
        }

        return result
    }

    private suspend fun movePomodoroChainForward(
        scheduleId: Int,
        newDate: LocalDate,
        newStartMin: Int,
        newEndMin: Int
    ) {
        val chain = findForwardPomodoroChain(scheduleId)

        if (chain.isEmpty()) {
            taskScheduleRepo.updatePomodoroTimeRange(
                scheduleId = scheduleId,
                date = newDate,
                startMin = newStartMin,
                endMin = newEndMin
            )
            return
        }

        var cursorStart = newStartMin
        var cursorEnd = newEndMin

        chain.forEachIndexed { index, node ->
            if (index == 0) {
                cursorStart = newStartMin
                cursorEnd = newEndMin
            } else {
                cursorStart = cursorEnd
                cursorEnd = cursorStart + node.durationMin
            }

            if (cursorEnd > 24 * 60) {
                // فعلاً اگر زنجیره از انتهای روز بیرون زد، ادامه را جابه‌جا نمی‌کنیم.
                return@forEachIndexed
            }

            taskScheduleRepo.updatePomodoroTimeRange(
                scheduleId = node.scheduleId,
                date = newDate,
                startMin = cursorStart,
                endMin = cursorEnd
            )

            pomodoroAlarmScheduler.cancelPomodoroEvents(node.scheduleId)

            val updated = taskScheduleRepo.getById(node.scheduleId)
            if (updated != null) {
                schedulePomodoroTimelineAlarms(updated)
            }
        }
    }

    private fun durationToScheduleDelayMinutes(duration: Duration): Long {
        val seconds = duration.seconds.coerceAtLeast(0)

        if (seconds <= 0) return 0L

        // ceil(seconds / 60)
        return ((seconds + 59) / 60)
    }

    private fun cancelForwardPomodoroChainAlarms(scheduleId: Int) {
        val chain = findForwardPomodoroChain(scheduleId)

        if (chain.isEmpty()) {
            pomodoroAlarmScheduler.cancelPomodoroEvents(scheduleId)
            return
        }

        chain.forEach { node ->
            pomodoroAlarmScheduler.cancelPomodoroEvents(node.scheduleId)
        }
    }

    private fun cancelStartSoonForForwardChainChildren(scheduleId: Int) {
        val chain = findForwardPomodoroChain(scheduleId)

        // خود پومودوروی در حال اجرا را نگه می‌داریم؛ فقط بچه‌های بعدی
        chain.drop(1).forEach { node ->
            pomodoroAlarmScheduler.cancelPomodoroEvent(
                scheduleId = node.scheduleId,
                type = PomodoroAlarmReceiver.TYPE_START_SOON
            )
        }
    }

    fun setScheduleVerticalZoom(value: Float) {
        viewModelScope.launch {
            appPreferences.setScheduleVerticalZoom(
                value.coerceIn(0.6f, 10.0f)
            )
        }
    }

    fun adjustPomodoroDoneToday(
        taskId: Int,
        delta: Int
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            taskRepo.adjustManualPomodoroDone(
                taskId = taskId,
                dateEpochDay = LocalDate.now().toEpochDay(),
                delta = delta
            )
        }
    }


    //only Schedule TIME_RANGE

    fun resizeScheduleEnd(scheduleId: Int, newEnd: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            taskScheduleRepo.updateEndMinute(scheduleId, newEnd)

            val reminders = reminderRepo.getByScheduleId(scheduleId)
            reminders.forEach { rUi ->
                try {
                    reminderScheduler.reschedule(rUi.id)   // ✅ این خط حیاتی است
                } catch (_: Throwable) {}
            }
        }
    }
    fun resizeScheduleStart(scheduleId: Int, newStart: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            taskScheduleRepo.updateStartMinute(scheduleId, newStart)

            val reminders = reminderRepo.getByScheduleId(scheduleId)
            reminders.forEach { rUi ->
                try {
                    reminderScheduler.reschedule(rUi.id)   // ✅ این خط حیاتی است
                } catch (_: Throwable) {}
            }
        }
    }
    fun moveSchedule(scheduleId: Int, newDate: LocalDate, newStart: Int, newEnd: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            taskScheduleRepo.updateTimeRange(scheduleId, newDate, newStart, newEnd)


            val reminders = reminderRepo.getByScheduleId(scheduleId)
            reminders.forEach { rUi ->
                try {
                    reminderScheduler.reschedule(rUi.id)   // ✅ این خط حیاتی است
                } catch (_: Throwable) {}
            }

        }

    }
    fun moveScheduleFromTimeLineToPallet(scheduleId: Int) {
        viewModelScope.launch(Dispatchers.IO) {

            pomodoroAlarmScheduler.cancelPomodoroEvents(scheduleId)

            val reminders = reminderRepo.getByScheduleId(scheduleId)
            reminders.forEach { rUi ->
                try {
                    reminderScheduler.cancel(rUi.id)   // ✅ این خط حیاتی است
                } catch (_: Throwable) {}
            }
            val schedule=taskScheduleRepo.getById(scheduleId)
            taskScheduleRepo.setSchedulePalletState(scheduleId, true)
        }
    }
    fun dropScheduleFromPalletToTimeLine(
        scheduleId: Int,
        date: LocalDate,
        startMin: Int,
        endMin: Int
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val reminders = reminderRepo.getByScheduleId(scheduleId)
            reminders.forEach { rUi ->
                try {
                    reminderScheduler.reschedule(rUi.id)   // ✅ این خط حیاتی است
                } catch (_: Throwable) {}
            }
            taskScheduleRepo.dropFromPalletToTimeline(scheduleId, date, startMin, endMin)
        }
    }




    //only Schedule  POMODORO

    suspend fun insertOnePomodoroTimelineItem(
        taskId: Int,
        scheduleId: Int,
        date: LocalDate,
        startMin: Int,
        focus: Int,
        shortBreak: Int,
    ): Int? {
        val end = (startMin + focus + shortBreak).coerceAtMost(24 * 60)

        val parentSchedule = taskScheduleRepo.getById(scheduleId)

        val childSchedule = parentSchedule?.copy(
            id = null,
            inPallet = false,
            repeating = false,
            dateEpochDay = date.toEpochDay(),
            startMinuteOfDay = startMin,
            endMinuteOfDay = end,
            focusMinutes = focus,
            shortBreakMinutes = shortBreak,
            pomodoroFocusDoneApplied = false,
            pomodoroParentId = scheduleId
        )

        val newScheduleChildId = childSchedule?.let { taskScheduleRepo.insert(it) }

        if (newScheduleChildId != null && childSchedule != null) {
            schedulePomodoroTimelineAlarms(
                childSchedule.copy(id = newScheduleChildId)
            )
        }

        val reminders = reminderRepo.getByScheduleId(scheduleId)
        reminders.forEach { rUi ->
            try {
                val newReminder = newScheduleChildId?.let { rUi.copy(id = null ,scheduleId = it) }
                if (newReminder != null) {
                    val newReminderId = reminderRepo.upsert(newReminder)
                    Log.i("TEST1","newReminderId=$newReminderId")
                    reminderScheduler.reschedule(newReminderId)
                }

            } catch (_: Throwable) {}
        }

        return newScheduleChildId
    }

    fun movePomodoroSchedule(
        scheduleId: Int,
        newDate: LocalDate,
        newStart: Int,
        newEnd: Int
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            movePomodoroChainForward(
                scheduleId = scheduleId,
                newDate = newDate,
                newStartMin = newStart,
                newEndMin = newEnd
            )
        }
    }




}





data class ScheduleScreenItem(
    val scheduleId: Int,
    val taskId: Int,
    val title: String,
    val mode: ScheduleMode ,

    // برای TIME_RANGE
    val dateEpochDay: Long? = null,       // LocalDate.toEpochDay()
    val start: LocalDateTime,
    val end: LocalDateTime,

    // برای AMOUNT_OF_TIME
    val durationMinutes: Int? = null,

    val inPallet: Boolean ,
    val repeating: Boolean ,

    val categoryName: String?,
    val categoryIconName: String?,
    val categoryColor: String?,
    val taskMode: TaskMode,
    val pomodoroTargetUnits: Int?,
    val pomodoroDoneUnits: Int,

    val repeatInterval: Int?,
    val repeatUnit: RepeatUnit?,
    val weekdaysMask: Int?,

    val focusMinutes: Int?,
    val shortBreakMinutes: Int?,
    val longBreakMinutes: Int?,
    val longBreakEvery: Int?,
    val pomodoroUnitsPerDay: Int?,
    val pomodoroFocusDoneApplied: Boolean,

    val parentRuleScheduleId: Int?,
    val occurrenceDateEpochDay: Long?,
    )

data class PendingMove(
    val date: LocalDate,
    val startMin: Int,
    val endMin: Int
)

data class ScheduleItemsRow(
    val s_id: Int,
    val s_taskId: Int,
    val s_inPallet: Boolean,
    val s_title: String?,
    val s_mode: ScheduleMode,
    val s_dateEpochDay: Long?,
    val s_startMinuteOfDay: Int?,
    val s_endMinuteOfDay: Int?,
    val s_durationMinutes: Int?,
    val s_repeating: Boolean,
    val s_repeatInterval: Int?,
    val s_repeatUnit: RepeatUnit?,
    val s_weekdaysMask: Int?,

    val s_focusMinutes: Int?,
    val s_shortBreakMinutes: Int?,
    val s_longBreakMinutes: Int?,
    val s_longBreakEvery: Int?,
    val s_pomodoroUnitsPerDay: Int?,

    val s_parentRuleScheduleId: Int?,
    val s_occurrenceDateEpochDay: Long?,
    val s_pomodoroFocusDoneApplied: Boolean,

    val t_taskMode: TaskMode,
    val t_pomodoroTargetUnits: Int?,
    val t_pomodoroDoneUnits: Int,

    val t_id: Int,
    val t_name: String,
    val t_color: String,
    val t_description: String,
    val t_categoryId: Int?,
    val t_isCompleted: Boolean,
    val t_priority: Int,
    val t_selected: Boolean,
    val t_changed: Boolean,

    // ✅ from CategoryEntity (LEFT JOIN)
    val c_name: String?,
    val c_iconName: String?,
    val c_color: String?
)

data class OverlapLayout(
    val level: Int,
    val widthFrac: Float,   // 1.0, 0.75, 0.5, 0.25
    val offsetFrac: Float,  // 0.0, 0.25, 0.5, 0.75
    val z: Float
)

data class PomodoroPalletCardItem(
    val taskId: Int,
    val scheduleId: Int,
    val taskName: String,

    val totalTarget: Int,
    val totalDone: Int,

    val expectedToday: Int,   // E
    val scheduledToday: Int,  // D (در پالت یعنی Scheduled)
    val doneToday: Int,

    val focus: Int,
    val shortBreak: Int,
    val longBreak: Int,
    val longBreakEvery: Int,
    val categoryColor: String?,
    val categoryIconName: String?,

    val remainingToday: Int
)

data class PomodoroAdjustState(
    val taskId: Int,
    val scheduleId: Int,
    val date: LocalDate,
    val startMin: Int,
    val focus: Int,
    val shortBreak: Int,
    val ids: List<Int?>,
    val anchorInRoot: Offset, // محل نمایش stepper
    val maxAllowed: Int // remainingToday از پالت
)

data class RunningPomodoroUiState(
    val scheduleId: Int,
    val taskId: Int,
    val title: String,
    val clickedAt: LocalDateTime,
    val realStartAt: LocalDateTime,
    val focusEndAt: LocalDateTime,
    val breakEndAt: LocalDateTime,
    val phase: PomodoroRunPhase = PomodoroRunPhase.WAITING_TO_START,
    val waitingSeconds: Long = 0,
    val focusElapsedSeconds: Long = 0,
    val breakElapsedSeconds: Long = 0,
    val focusDoneApplied: Boolean = false,
    val isPaused: Boolean = false,
    val pauseAt: LocalDateTime? = null
)

private data class TaskChildOccurrenceEnsureInput(
    val parentTaskId: Int,
    val scheduleId: Int?,
    val parentRuleScheduleId: Int?,
    val occurrenceDateEpochDay: Long
)

data class TaskChildSheetState(
    val parentTaskId: Int,
    val title: String,
    val scheduleId: Int?,
    val parentRuleScheduleId: Int?,
    val occurrenceDateEpochDay: Long
)

private data class TaskChildVisibleRange(
    val startEpochDay: Long,
    val endEpochDay: Long
)


enum class PomodoroRunPhase {
    WAITING_TO_START,
    FOCUS,
    BREAK,
    FINISHED
}