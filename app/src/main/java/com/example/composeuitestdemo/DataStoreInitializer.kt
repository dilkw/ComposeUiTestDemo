package com.christelle.mrppda.helper

import android.content.Context
import androidx.startup.Initializer

class DataStoreInitializer: Initializer<Boolean> {
    override fun create(context: Context): Boolean {
        DataStoreHelper.init(context)
        return true
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return emptyList()
    }
}