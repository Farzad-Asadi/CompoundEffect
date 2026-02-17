package com.example.compoundeffectV1_01.ui.scheduleScreen


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.compoundeffectV1_01.data.dataBaseRoom.tables.task.TaskRepository
import com.example.compoundeffectV1_01.data.dataBaseRoom.tables.taskSchedule.ScheduleMode
import com.example.compoundeffectV1_01.data.dataBaseRoom.tables.taskSchedule.TaskScheduleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class ScheduleScreenViewModel @Inject constructor(
    private val taskRepo: TaskRepository,
    private val taskScheduleRepo: TaskScheduleRepository,
) : ViewModel() {





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
                        title =r.t_name,
                        mode =r.s_mode,

                        dateEpochDay = r.s_dateEpochDay,
                        start = start,
                        end = end,

                        durationMinutes = r.s_durationMinutes,

                        inPallet = r.s_inPallet,
                        repeating = r.s_repeating,

                        categoryName = r.c_name,
                        categoryIconName = r.c_iconName,
                        categoryColor = r.c_color
                    )
                }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())












    fun moveSchedule(scheduleId: Int, newDate: LocalDate, newStart: Int, newEnd: Int) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            taskScheduleRepo.updateTimeRange(scheduleId, newDate, newStart, newEnd)
        }

    }

    fun resizeScheduleEnd(scheduleId: Int, newEnd: Int) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            taskScheduleRepo.updateEndMinute(scheduleId, newEnd)
        }
    }

    fun resizeScheduleStart(scheduleId: Int, newStart: Int) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            taskScheduleRepo.updateStartMinute(scheduleId, newStart)
        }
    }

    fun moveScheduleFromTimeLineToPallet(scheduleId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
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
            taskScheduleRepo.dropFromPalletToTimeline(scheduleId, date, startMin, endMin)
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
    val categoryColor: String?
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
