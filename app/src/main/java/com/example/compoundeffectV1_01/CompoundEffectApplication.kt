package com.example.compoundeffectV1_01

import android.app.Application
import com.example.compoundeffectV1_01.data.modules.SeederEntryPoint
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch


@HiltAndroidApp
class CompoundEffectApplication : Application() {

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        val entryPoint = EntryPointAccessors.fromApplication(this, SeederEntryPoint::class.java)
        val seeder = entryPoint.seeder()

        appScope.launch {
            seeder.seedIfNeeded()   // این رو می‌سازیم
        }
    }
}