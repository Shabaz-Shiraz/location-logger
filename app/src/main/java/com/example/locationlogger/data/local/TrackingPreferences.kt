package com.example.locationlogger.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrackingPreferences @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    val isTracking: Flow<Boolean> = dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { it[IS_TRACKING] ?: false }
        .distinctUntilChanged()

    suspend fun setTracking(tracking: Boolean) {
        dataStore.edit { it[IS_TRACKING] = tracking }
    }

    private companion object {
        val IS_TRACKING = booleanPreferencesKey("is_tracking")
    }
}
