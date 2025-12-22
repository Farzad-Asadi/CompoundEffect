package com.example.compoundeffectV1_01.data.room.appSystemInfo

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query


@Dao
interface SystemDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAppSystemInfo(appSystemInfo: AppSystemInfo)

    @Delete
    suspend fun deleteAppSystemInfo(appSystemInfo: AppSystemInfo)

    @Query("Update app_system_info Set dashboardHourHeight =:dashboardHourHeight,dashboardZoomState =:dashboardZoomState Where id = 1")
    suspend fun updateAppSystemInfo(dashboardHourHeight: Float,dashboardZoomState: Float)

    @Query("SELECT * FROM app_system_info ")
    suspend fun getAllAppSystemInfo(): List<AppSystemInfo>

}