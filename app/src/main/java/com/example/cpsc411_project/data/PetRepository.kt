package com.example.cpsc411_project.data

import android.content.Context
import androidx.core.content.edit

enum class Stat(val prefix: String, val decayMs: Long) {
    HAPPINESS("happiness", 6 * 60 * 1000L), // pet and play decay every 6 mins
    HUNGER("hunger", 40 * 60 * 1000L), // clean decays every 40 mins
    CLEAN("clean", 30 * 60 * 1000L) // hunger decays every 30 mins
}

class PetRepository(context: Context) {
    private val prefs = context.applicationContext
        .getSharedPreferences("pet", Context.MODE_PRIVATE)
    val petType: String = prefs.getString("pet_type", "dog") ?: "dog"

    private fun valueKey(stat: Stat) = "${stat.prefix}_$petType"
    private fun timeKey(stat: Stat) = "${stat.prefix}Update_$petType"

    fun read(stat: Stat): Int = prefs.getInt(valueKey(stat), 50)

    fun applyDecay(stat: Stat): Int {
        val now = System.currentTimeMillis()
        val saved = prefs.getInt(valueKey(stat), 50)
        val last = prefs.getLong(timeKey(stat), now)
        val intervals = ((now - last) / stat.decayMs).toInt().coerceAtLeast(0)
        val current = (saved - intervals * POINTS_PER_INTERVAL).coerceIn(0, 100)
        prefs.edit {
            putInt(valueKey(stat), current)
            putLong(timeKey(stat), last + intervals * stat.decayMs)
        }
        return current
    }
    fun change(stat: Stat, amount: Int): Int {
        val newValue = (applyDecay(stat) + amount).coerceIn(0, 100)
        prefs.edit(commit = true) { putInt(valueKey(stat), newValue) }
        return newValue
    }

    private companion object {
        const val POINTS_PER_INTERVAL = 5 // meters go up by 5
    }
}