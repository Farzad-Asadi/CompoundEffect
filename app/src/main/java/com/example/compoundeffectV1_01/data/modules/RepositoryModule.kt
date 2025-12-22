package com.example.compoundeffectV1_01.data.modules

import com.example.compoundeffectV1_01.data.dataBaseRoom.tables.appSystemInfo.AppSystemInfoRepository
import com.example.compoundeffectV1_01.data.dataBaseRoom.tables.appSystemInfo.AppSystemInfoRepositoryImpl
import com.example.compoundeffectV1_01.data.dataBaseRoom.tables.category.CategoryRepository
import com.example.compoundeffectV1_01.data.dataBaseRoom.tables.category.CategoryRepositoryImpl
import com.example.compoundeffectV1_01.data.dataBaseRoom.tables.event.EventRepository
import com.example.compoundeffectV1_01.data.dataBaseRoom.tables.event.EventRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindCategoryRepository(
        impl: CategoryRepositoryImpl
    ): CategoryRepository

    @Binds @Singleton
    abstract fun bindEventRepository(
        impl: EventRepositoryImpl
    ): EventRepository

    @Binds @Singleton
    abstract fun bindAppSystemInfoRepository(
        impl: AppSystemInfoRepositoryImpl
    ): AppSystemInfoRepository



}
