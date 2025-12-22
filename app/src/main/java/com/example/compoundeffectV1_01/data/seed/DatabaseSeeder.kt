package com.example.compoundeffectV1_01.data.seed

import com.example.compoundeffectV1_01.data.dataBaseRoom.tables.category.CategoryDao
import com.example.compoundeffectV1_01.data.dataBaseRoom.tables.category.CategoryEntity
import com.example.compoundeffectV1_01.data.dataStore.AppPreferences
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseSeeder @Inject constructor(
    private val categoryDao: CategoryDao,
    private val prefs: AppPreferences
) {

    suspend fun seedIfNeeded() {
        // اگر قبلاً seed شده، هیچ کاری نکن
        if (prefs.isSeedDone.first()) return

        // اگر دیتابیس خالیه، فقط Root رو بساز
        val hasAnyCategory = categoryDao.count() > 0
        if (!hasAnyCategory) {
            categoryDao.insertCategory(
                CategoryEntity(
                    categoryId = null,
                    name = "ریشه اصلی",
                    parentCategoryId = -1,
                    iconName = "AccountTree",
                    color = "#000000",
                    description = "Root category",
                    siblingIndex = 0,
                    // این‌ها تو Entity پیش‌فرض دارند، لازم نیست ست شوند
                    // expandable = false,
                    // isExtended = true,
                    // visible = true
                )
            )
        }

        // مهم: حتی اگر خالی نبود، از این به بعد seed رو done کن
        prefs.setSeedDone(true)
    }



}

