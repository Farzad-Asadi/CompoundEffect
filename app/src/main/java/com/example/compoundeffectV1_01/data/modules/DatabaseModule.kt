package com.example.compoundeffectV1_01.data.modules

import android.content.Context
import androidx.room.Room
import com.example.compoundeffectV1_01.data.dataStore.AppPreferences
import com.example.compoundeffectV1_01.data.dataStore.AppPreferencesImpl
import com.example.compoundeffectV1_01.data.room.AppDatabase
import com.example.compoundeffectV1_01.data.room.appSystemInfo.SystemDao
import com.example.compoundeffectV1_01.data.room.category.CategoryDao
import com.example.compoundeffectV1_01.data.room.event.EventDao
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "compound_effect_db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideCategoryDao(db: AppDatabase): CategoryDao = db.categoryDao()
    @Provides fun provideEventDao(db: AppDatabase): EventDao = db.eventDao()
    @Provides fun provideSystemDao(db: AppDatabase): SystemDao = db.systemDao()



}
