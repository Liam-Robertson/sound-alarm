package com.extremewakeup.soundalarm.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.LocalTime

@Entity
@Serializable
data class AlarmTiming(
    @Serializable(with = LocalTimeSerializer::class) val time: LocalTime,
    val volume: Int,
)