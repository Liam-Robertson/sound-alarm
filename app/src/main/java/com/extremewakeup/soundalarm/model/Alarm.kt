package com.extremewakeup.soundalarm.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.LocalTime

@Entity
@Serializable
data class Alarm(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @Serializable(with = LocalTimeSerializer::class) val time: LocalTime,
    val daysActive: List<String>,
    val volume: Int,
    var isActive: Boolean,
    val userId: Int
)