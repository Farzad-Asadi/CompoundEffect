package com.example.compoundeffectV1_01

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import com.example.compoundeffectV1_01.data.modules.SeederEntryPoint
import com.example.compoundeffectV1_01.data.notification.ReminderNotifications
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import androidx.work.Configuration
import javax.inject.Inject


@HiltAndroidApp
class CompoundEffectApplication : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        // کانال نوتیفیکیشن
        ReminderNotifications.ensureChannel(this)

        val entryPoint = EntryPointAccessors.fromApplication(this, SeederEntryPoint::class.java)
        val seeder = entryPoint.seeder()

        appScope.launch {
            seeder.seedIfNeeded()
        }
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            // (اختیاری) اگر خواستی لاگ WorkManager بیشتر بشه:
            // .setMinimumLoggingLevel(Log.DEBUG)
            .build()
}