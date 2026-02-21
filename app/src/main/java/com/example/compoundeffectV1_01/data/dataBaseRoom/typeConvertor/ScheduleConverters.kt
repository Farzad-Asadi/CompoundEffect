package com.example.compoundeffectV1_01.data.dataBaseRoom.typeConvertor

import androidx.room.TypeConverter
import com.example.compoundeffectV1_01.data.dataBaseRoom.tables.taskSchedule.RepeatUnit
import com.example.compoundeffectV1_01.data.dataBaseRoom.tables.taskSchedule.ScheduleMode
import java.time.LocalDate
import java.time.LocalTime

class ScheduleConverters {

    @TypeConverter fun dateToString(v: LocalDate?): String? = v?.toString()
    @TypeConverter fun stringToDate(v: String?): LocalDate? = v?.let(LocalDate::parse)

    @TypeConverter fun timeToString(v: LocalTime?): String? = v?.toString()
    @TypeConverter fun stringToTime(v: String?): LocalTime? = v?.let(LocalTime::parse)

    @TypeConverter fun modeToString(v: ScheduleMode?): String? = v?.name
    @TypeConverter fun stringToMode(v: String?): ScheduleMode? = v?.let(ScheduleMode::valueOf)

    @TypeConverter fun toDb(v: RepeatUnit?): String? = v?.name
    @TypeConverter fun fromDb(v: String?): RepeatUnit? = v?.let(RepeatUnit::valueOf)
}