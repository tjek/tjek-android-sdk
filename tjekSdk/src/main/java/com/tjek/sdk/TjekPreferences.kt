package com.tjek.sdk
/*
 * Copyright (C) 2022 Tjek
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import android.content.Context
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.tjek.sdk.legacy.LegacyLocation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

object TjekPreferences {

    private val INSTALLATION_ID = stringPreferencesKey("installation_id")

    // these two info won't be stored in the sdk anymore.
    // It's possible to retrieve them one last time when migrating to the new sdk
    private val LEGACY_LOCATION_JSON = stringPreferencesKey("location_json")
    private val LEGACY_LOCATION_ENABLED_FLAG = booleanPreferencesKey("location_enabled")

    var initialized = AtomicBoolean(false)

    var installationId: String = ""
    private set

    private val Context.dataStore by preferencesDataStore(
        name = "tjek_sdk_preferences",
        produceMigrations = { context ->
            listOf(SharedPreferencesMigration(
                context = context,
                sharedPreferencesName = "com.shopgun.android.sdk_preferences",
                keysToMigrate = setOf("installation_id", "location_json", "location_enabled")
            ))
        }
    )

    fun initialize(context: Context) {
        CoroutineScope(Dispatchers.Default).launch {
            installationId = context.dataStore.data.firstOrNull()?.get(INSTALLATION_ID)
                ?: createUUID()
                    .also { id ->
                        try {
                            context.dataStore.edit { it[INSTALLATION_ID] = id }
                        } catch (e: Exception) {
                            TjekLogCat.forceE("Error while writing new UUID: ${e.message}")
                            TjekLogCat.printStackTrace(e)
                        }
                    }

            initialized.set(true)
        }
    }

    suspend fun getLegacyLocation(context: Context): LegacyLocation? {
        val appContext = context.applicationContext
        val locationJson = appContext.dataStore.data.firstOrNull()?.get(LEGACY_LOCATION_JSON)
        val locationEnabledFlag = appContext.dataStore.data.firstOrNull()?.get(LEGACY_LOCATION_ENABLED_FLAG)
        return when {
            locationJson != null && locationEnabledFlag != null ->
                LegacyLocation.fromLegacySettings(locationJson, locationEnabledFlag)
            locationJson != null ->
                LegacyLocation.fromLegacySettings(locationJson, isLocationEnabled = false)
            else -> null
        }
    }
}