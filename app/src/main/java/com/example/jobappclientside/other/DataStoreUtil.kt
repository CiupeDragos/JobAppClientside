package com.example.jobappclientside.other

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

class DataStoreUtil(private val context: Context) {
    private val Context.myDataStore: DataStore<Preferences> by preferencesDataStore(name = "credentials")

    companion object {
        val USERNAME = stringPreferencesKey("username")
        val PASSWORD = stringPreferencesKey("password")
    }

    suspend fun storeCredentials(username: String, password: String) {
        context.myDataStore.edit { preferences ->
            preferences[USERNAME] = username
            preferences[PASSWORD] = password
        }
    }

    suspend fun isUserLoggedIn(): Boolean {
        val preferences = context.myDataStore.data.first()
        val username = preferences[USERNAME]
        val password = preferences[PASSWORD]

        return username != null && password != null
    }

    suspend fun getUsername(): String {
        val preferences = context.myDataStore.data.first()
        return preferences[USERNAME] ?: ""
    }
}