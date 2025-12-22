package com.example.compoundeffectV1_01.data.modules

import android.content.Context
import androidx.room.Room
import com.example.compoundeffectV1_01.data.dataBaseRoom.appDataBase.AppDatabase
import com.example.compoundeffectV1_01.data.dataBaseRoom.tables.appSystemInfo.SystemDao
import com.example.compoundeffectV1_01.data.dataBaseRoom.tables.category.CategoryDao
import com.example.compoundeffectV1_01.data.dataBaseRoom.tables.event.EventDao
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
        AppDatabase.getDatabase(context)






    @Provides fun provideCategoryDao(db: AppDatabase): CategoryDao = db.categoryDao()
    @Provides fun provideEventDao(db: AppDatabase): EventDao = db.eventDao()
    @Provides fun provideSystemDao(db: AppDatabase): SystemDao = db.systemDao()



}
