package com.hera.newsapp.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.hera.newsapp.util.Constants.SETTINGS

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(SETTINGS)