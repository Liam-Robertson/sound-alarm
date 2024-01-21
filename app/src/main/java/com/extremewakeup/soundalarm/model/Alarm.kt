package com.extremewakeup.soundalarm.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalTime

@Entity
data class Alarm(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val time: LocalTime,
    val daysSelected: List<String>,
    val volume: Int,
    var isActive: Boolean,
    val userId: Int
)
