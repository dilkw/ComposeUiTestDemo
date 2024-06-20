package com.christelle.mrppda.helper

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
object DataStoreHelper {

    private lateinit var dataStore: DataStore<Preferences>

    fun init(context: Context) {
        dataStore = context.dataStore
    }

    private val KEY_IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
    private val KEY_USERNAME = stringPreferencesKey("username")

    // 保存登录状态
    suspend fun saveLoginState(isLoggedIn: Boolean, username: String = "") {
        dataStore.edit { preferences ->
            preferences[KEY_IS_LOGGED_IN] = isLoggedIn
            if (username.isNotEmpty()) {
                preferences[KEY_USERNAME] = username
            }
        }
    }

    fun isLoggedIn(): Boolean {
        var value = false
        runBlocking {
            dataStore.data.first {
                value = it[KEY_IS_LOGGED_IN] ?: false
                true
            }
        }
        return value
    }

     fun getUsername(): String {
        var value = ""
        runBlocking {
            dataStore.data.first {
                value = it[KEY_USERNAME] ?: ""
                true
            }
        }
        return value
    }
     fun clearUserInfo() {
         runBlocking {
             dataStore.edit {
                 it[KEY_USERNAME] = ""
                 it[KEY_IS_LOGGED_IN] = false
             }
         }
    }
}