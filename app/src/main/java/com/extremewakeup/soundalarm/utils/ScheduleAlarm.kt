package com.extremewakeup.soundalarm.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.extremewakeup.soundalarm.model.Alarm
import com.extremewakeup.soundalarm.receiver.AlarmReceiver
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

fun scheduleAlarms(context: Context, alarmList: List<Alarm>) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
        requestExactAlarmPermission(context)
        return
    }

    for (alarm in alarmList) {
        if (alarm.isActive) {
            scheduleExactAlarm(context, alarm, alarmManager)
        }
    }
}

private fun requestExactAlarmPermission(context: Context) {
    val intent = Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
    context.startActivity(intent)
}

private fun scheduleExactAlarm(context: Context, alarm: Alarm, alarmManager: AlarmManager) {
    val now = ZonedDateTime.now(ZoneId.systemDefault())
    var triggerTime = alarm.time.atDate(LocalDate.now()).atZone(ZoneId.systemDefault())

    if (triggerTime.isBefore(now)) {
        triggerTime = triggerTime.plusDays(1)
    }

    val intent = Intent(context, AlarmReceiver::class.java).apply {
        action = "com.extremewakeup.ACTION_ALARM_TRIGGER"
        putExtra("ALARM_ID", alarm.id)
    }

    val pendingIntentFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    } else {
        PendingIntent.FLAG_UPDATE_CURRENT
    }

    val pendingIntent = PendingIntent.getBroadcast(
        context, alarm.id, intent, pendingIntentFlag
    )

    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime.toInstant().toEpochMilli(), pendingIntent)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime.toInstant().toEpochMilli(), pendingIntent)
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime.toInstant().toEpochMilli(), pendingIntent)
        }
        Log.d("scheduleAlarms", "Alarm scheduled for: $triggerTime")
    } catch (e: SecurityException) {
        Log.e("scheduleAlarms", "Failed to schedule alarm: ${e.message}")
        // Handle the exception, possibly by asking the user for permission again
    }
}
