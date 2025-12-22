package com.example.compoundeffectV1_01.data.dataStore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private val Context.dataStore by preferencesDataStore(name = "app_prefs")

class AppPreferencesImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : AppPreferences {

    private object Keys {
        val SEED_DONE = booleanPreferencesKey("seed_done")
    }

    override val isSeedDone: Flow<Boolean> =
        context.dataStore.data.map { it[Keys.SEED_DONE] ?: false }

    override suspend fun setSeedDone(value: Boolean) {
        context.dataStore.edit { it[Keys.SEED_DONE] = value }
    }
}
