package com.example.compoundeffectV1_01

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Apps
import androidx.compose.ui.graphics.Color
import com.example.compoundeffectV1_01.data.AppContainer
import com.example.compoundeffectV1_01.data.AppDataContainer
import com.example.compoundeffectV1_01.data.room.AppDatabase
import com.example.compoundeffectV1_01.data.room.appSystemInfo.AppSystemInfo
import com.example.compoundeffectV1_01.data.room.category.CategoryEntity
import com.example.compoundeffectV1_01.data.room.event.Event
import com.example.compoundeffectV1_01.utils.colorToString
import com.example.compoundeffectV1_01.utils.createTimeForSampleEvents
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.Calendar


@HiltAndroidApp
class CompoundEffectApplication : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)


    private val database by lazy { AppDatabase.getDatabase(this) }
    lateinit var container: AppContainer
    private val sharedPreferences: SharedPreferences by lazy {
        getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
    }

    override fun onCreate() {
        super.onCreate()
        container = AppDataContainer(this)
        checkFirstRun()


    }

    private fun checkFirstRun() {
        val isFirstRun = sharedPreferences.getBoolean("is_first_run", true)
        if (isFirstRun) {

            val currentTime: Calendar = Calendar.getInstance()
            val sampleEvents = arrayOf(
                Event(
                    id = 1,
                    name = "Google",
                    color = Color(0xFFAFBBF2).colorToString(),
                    start = createTimeForSampleEvents(currentTime, 7, 0, 0),
                    end = createTimeForSampleEvents(currentTime, 9, 0, 0),
                    description = "Tune in to find out about how we're furthering our mission to organize the world’s information and make it universally accessible and useful.",
                    inPallet = true,
                    selected = false
                ),
                Event(
                    id = 2,
                    name = "Developer Keynote",
                    color = Color(0xFFAFBBF2).colorToString(),
                    start = createTimeForSampleEvents(currentTime, 11, 0, 0),
                    end = createTimeForSampleEvents(currentTime, 12, 0, 0),
                    description = "Learn about the latest updates to our developer products and platforms from Google Developers.",
                    inPallet = true,
                    selected = false
                ),
                Event(
                    id = 3,
                    name = "What's new in Android",
                    color = Color(0xFF1B998B).colorToString(),
                    start = createTimeForSampleEvents(currentTime, 13, 0, 0),
                    end = createTimeForSampleEvents(currentTime, 15, 0, 0),
                    description = "In this Keynote, Chet Haase, Dan Sandler, and Romain Guy discuss the latest Android features and enhancements for developers.",
                    inPallet = true,
                    selected = false
                ),
                Event(
                    id = 4,
                    name = "What's new in Machine Learning",
                    color = Color(0xFFF4BFDB).colorToString(),
                    start = createTimeForSampleEvents(currentTime, 18, 30, 0),
                    end = createTimeForSampleEvents(currentTime, 19, 45, 0),
                    description = "Learn about the latest and greatest in ML from Google. We’ll cover what’s available to developers when it comes to creating, understanding, and deploying models for a variety of different applications.",
                    inPallet = true,
                    selected = false
                ),
                Event(
                    id = 5,
                    name = "What's new in Material Design",
                    color = Color(0xFF6DD3CE).colorToString(),
                    start = createTimeForSampleEvents(currentTime, 21, 30, 0),
                    end = createTimeForSampleEvents(currentTime, 23, 0, 0),
                    description = "Learn about the latest design improvements to help you build personal dynamic experiences with Material Design.",
                    inPallet = true,
                    selected = false
                ),
                Event(
                    id = 6,
                    name = "Jetpack Compose Basics",
                    color = Color(0xFF1B998B).colorToString(),
                    start = createTimeForSampleEvents(currentTime, 13, 30, 0).plusDays(1),
                    end = createTimeForSampleEvents(currentTime, 15, 45, 0).plusDays(1),
                    description = "This Workshop will take you through the basics of building your first app with Jetpack Compose, Android's new modern UI toolkit that simplifies and accelerates UI development on Android.",
                    inPallet = true,
                    selected = false
                ),
                Event(
                    id = 7,
                    name = "test to other day",
                    color = Color(0xFF1B998B).colorToString(),
                    start = createTimeForSampleEvents(currentTime, 18, 30, 0).plusDays(1),
                    end = createTimeForSampleEvents(currentTime, 20, 0, 0).plusDays(1),
                    description = "This Workshop will take you through the basics of building your first app with Jetpack Compose, Android's new modern UI toolkit that simplifies and accelerates UI development on Android.",
                    inPallet = true,
                    selected = false
                ),
            )

            val appSystemInfo = AppSystemInfo()


            val sampleCategoryEntities = arrayOf(
                CategoryEntity(
                    categoryId = null, // اجازه دهید Room شناسه را ایجاد کند
                    name = "ریشه اصلی",
                    parentCategoryId = -1,
                    icon = Icons.Filled.AccountTree,
                    color = Color(0xFF000000).colorToString(),
                    description = "Root category"
                ),
                CategoryEntity(
                    categoryId = null,
                    name = "روتینها",
                    parentCategoryId = 1,
                    icon = Icons.Filled.Apps,
                    color = Color(0xFF9C27B0).colorToString(),
                    description = "روتینها",
                    siblingIndex = 1
                ),
//                Category(
//                    categoryId = null,
//                    name = "clean",
//                    parentCategoryId = 2,
//                    icon = Icons.Filled.Shower,
//                    color = Color(0xFFCDDC39).colorToString(),
//                    description = "Root category",
//                    siblingIndex = 0
//                ),
//                Category(
//                    categoryId = null,
//                    name = "kitchen",
//                    parentCategoryId = 3,
//                    icon = Icons.Filled.Kitchen,
//                    color = Color(0xFFE91E63).colorToString(),
//                    description = "Root category",
//                    siblingIndex = 0
//                ),
//                Category(
//                    categoryId = null,
//                    name = "me",
//                    parentCategoryId = 1,
//                    icon = Icons.Filled.AccountCircle,
//                    color = Color(0xFF2196F3).colorToString(),
//                    description = "Root category",
//                    siblingIndex = 0
//                ),
//                Category(
//                    categoryId = null,
//                    name = "sport",
//                    parentCategoryId = 5,
//                    icon = Icons.Filled.Sports,
//                    color = Color(0xFF4CAF50).colorToString(),
//                    description = "Root category",
//                    siblingIndex = 0
//                ),
            )

            applicationScope.launch {
                database.categoryDao().insertCategory(*sampleCategoryEntities)
                database.eventDao().insertEvent(*sampleEvents)
                database.systemDao().insertAppSystemInfo(appSystemInfo)
            }

            // ذخیره وضعیت اولین اجرا
            sharedPreferences.edit().putBoolean("is_first_run", false).apply()
        }
    }
}