package com.github.lostfound.util

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import kotlin.reflect.KProperty

typealias GetterType<T> = (SharedPreferences, String, T) -> T
typealias SetterType<T> = (SharedPreferences.Editor, String, T) -> SharedPreferences.Editor

interface PreferenceProvider {
    val preferenceName: String
    val context: Context
}

abstract class BasePreference<T>(private val default: T, private val getter: GetterType<T>, private val setter: SetterType<T>) {
    operator fun getValue(thisRef: PreferenceProvider, property: KProperty<*>): T {
        val preference = thisRef.context.getSharedPreferences(thisRef.preferenceName, MODE_PRIVATE)
        val name = property.name
        return getter(preference, name, default)
    }
    operator fun setValue(thisRef: PreferenceProvider, property: KProperty<*>, value: T) {
        val preference = thisRef.context.getSharedPreferences(thisRef.preferenceName, MODE_PRIVATE)
        val name = property.name
        val editor = preference.edit()
        setter(editor, name, value).apply()
    }
}

class StringPreferenceNullable(default: String? = null):
    BasePreference<String?>(default, SharedPreferences::getString, SharedPreferences.Editor::putString)

class StringPreferenceNotNull(default: String):
    BasePreference<String>(default, { p, k, d -> p.getString(k, null) ?: d }, { e, k, v -> e.putString(k, v) })

class BooleanPreferenceImpl(default: Boolean):
    BasePreference<Boolean>(default, SharedPreferences::getBoolean, SharedPreferences.Editor::putBoolean)

class IntegerPreference(default: Int):
    BasePreference<Int>(default, SharedPreferences::getInt, SharedPreferences.Editor::putInt)

@Suppress("FunctionName")
@JvmName("StringPreferenceNotNull")
fun StringPreference(default: String) = StringPreferenceNotNull(default)

@Suppress("FunctionName")
@JvmName("StringPreferenceNullable")
fun StringPreference(default: String? = null) = StringPreferenceNullable(default)

@Suppress("FunctionName")
fun BooleanPreference(default: Boolean) = BooleanPreferenceImpl(default)
