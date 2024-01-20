package com.extremewakeup.soundalarm.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Alarm(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val time: String, // You can also use a Date type with a TypeConverter
    val daysSelected: String, // This could be a comma-separated string or a JSON string
    val volume: Int,
    val isActive: Boolean
)
