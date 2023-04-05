package com.ritarime.polkkaruokalistat

import android.content.Context
import android.content.SharedPreferences

fun saveSetting(context: Context, key: String, value: String) {
    val sharedPref: SharedPreferences = context.getSharedPreferences("my_settings", Context.MODE_PRIVATE)
    with (sharedPref.edit()) {
        putString(key, value)
        apply()
    }
}

fun getSetting(context: Context, key: String): String? {
    val sharedPref: SharedPreferences = context.getSharedPreferences("my_settings", Context.MODE_PRIVATE)
    return sharedPref.getString(key, null)
}
