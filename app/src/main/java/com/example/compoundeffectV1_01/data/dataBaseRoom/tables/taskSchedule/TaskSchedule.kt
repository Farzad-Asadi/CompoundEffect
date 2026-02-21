package com.example.compoundeffectV1_01.data.dataBaseRoom.tables.taskSchedule

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.example.compoundeffectV1_01.data.dataBaseRoom.tables.task.Task
import java.time.LocalDate
import java.time.LocalTime



@Entity(
    tableName = "task_schedule",
    indices = [Index(value = ["taskId"])],
    foreignKeys = [
        ForeignKey(
            entity = Task::class,
            parentColumns = ["id"],
            childColumns = ["taskId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class TaskSchedule(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,

    val taskId: Int,

    val title: String? = null,

    val mode: ScheduleMode = ScheduleMode.TIME_RANGE,

    // برای TIME_RANGE
    val dateEpochDay: Long? = null,       // LocalDate.toEpochDay()
    val startMinuteOfDay: Int? = null,    // LocalTime.toSecondOfDay()/60
    val endMinuteOfDay: Int? = null,

    // برای AMOUNT_OF_TIME
    val durationMinutes: Int? = null,

    val reminderMinutesBefore: Int? = null,

    val inPallet: Boolean = false,

    val repeating: Boolean = false,
    val repeatInterval: Int? = null,
    val repeatUnit: RepeatUnit? = null,
    val weekdaysMask: Int? = null,  // ✅ NEW: فقط وقتی repeatUnit=WEEK معنی دارد (0..127)

)







enum class ScheduleMode { TIME_RANGE, AMOUNT_OF_TIME }
enum class RepeatUnit { DAY, WEEK, MONTH, YEAR }
