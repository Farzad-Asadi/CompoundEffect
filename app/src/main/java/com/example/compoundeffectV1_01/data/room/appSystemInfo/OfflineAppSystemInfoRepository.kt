package com.example.compoundeffectV1_01.data.room.appSystemInfo

class OfflineAppSystemInfoRepository (
    private val systemDao: SystemDao
): AppSystemInfoRepository {
    override suspend fun insertAppSystemInfo(appSystemInfo: AppSystemInfo) =
        systemDao.insertAppSystemInfo(appSystemInfo)

    override suspend fun getAllAppSystemInfo(): List<AppSystemInfo> =
        systemDao.getAllAppSystemInfo()


    override suspend fun updateAppSystemInfo(dashboardHourHeight: Float,dashboardZoomState: Float) =
        systemDao.updateAppSystemInfo(dashboardHourHeight,dashboardZoomState)

//    override fun getAllAppSystemInfoStream(): Flow<List<AppSystemInfo>> =
//        appSystemInfoDao.getAllAppSystemInfoStream()


}