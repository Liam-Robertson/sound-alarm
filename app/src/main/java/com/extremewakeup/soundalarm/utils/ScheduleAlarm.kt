package com.extremewakeup.soundalarm.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.extremewakeup.soundalarm.model.Alarm
import com.extremewakeup.soundalarm.receiver.AlarmReceiver
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

fun scheduleAlarms(context: Context, alarmList: List<Alarm>) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    for (alarm in alarmList) {
        if (alarm.isActive) {
            // Ensure the trigger time is in the future
            val now = ZonedDateTime.now(ZoneId.systemDefault())
            var triggerTime = alarm.time.atDate(LocalDate.now())
                .atZone(ZoneId.systemDefault())

            // If the trigger time has already passed today, schedule it for the next day
            if (triggerTime.isBefore(now)) {
                triggerTime = triggerTime.plusDays(1)
            }

            val intent = Intent(context, AlarmReceiver::class.java).apply {
                // Consider adding unique action or extras to differentiate alarms
                action = "com.yourapp.ACTION_ALARM_TRIGGER"
                putExtra("ALARM_ID", alarm.id) // Example of passing extra data
            }

            val pendingIntentFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context, alarm.id, intent, pendingIntentFlag
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime.toInstant().toEpochMilli(), pendingIntent)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime.toInstant().toEpochMilli(), pendingIntent)
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime.toInstant().toEpochMilli(), pendingIntent)
            }
        }
    }
}
