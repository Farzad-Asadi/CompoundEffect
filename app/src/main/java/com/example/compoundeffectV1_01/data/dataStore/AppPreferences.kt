package com.example.compoundeffectV1_01.data.dataStore

interface AppPreferences {
    val isSeedDone: kotlinx.coroutines.flow.Flow<Boolean>
    suspend fun setSeedDone(value: Boolean)
}
