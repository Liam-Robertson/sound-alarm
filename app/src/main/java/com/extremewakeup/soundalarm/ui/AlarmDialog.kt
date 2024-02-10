package com.extremewakeup.soundalarm.ui

import android.app.TimePickerDialog
import android.content.Context
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.extremewakeup.soundalarm.model.Alarm
import com.extremewakeup.soundalarm.viewmodel.MainViewModel
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAlarmDialog(viewModel: MainViewModel, context: Context, userId: Int, onDismiss: () -> Unit) {
    var timeInput by remember { mutableStateOf("") }
    val context = LocalContext.current

    fun showTimePicker() {
        val calendar = Calendar.getInstance()
        TimePickerDialog(
            context,
            { _, hour, minute ->
                val formattedHour = hour.toString().padStart(2, '0')
                val formattedMinute = minute.toString().padStart(2, '0')
                timeInput = "$formattedHour:$formattedMinute"
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        ).show()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New Alarm") },
        text = {
            Column {
                OutlinedTextField(
                    value = timeInput,
                    onValueChange = {},
                    label = { Text("Select Time") },
                    readOnly = true,
                    trailingIcon = {
                        Icon(Icons.Default.AccessTime, "Select Time", Modifier.clickable { showTimePicker() })
                    }
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                Log.d("CreateAlarm", "Create button clicked")
                val selectedTime = timeInput
                val timeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())
                val time: LocalTime = LocalTime.parse(selectedTime, timeFormatter)
                val daysActive = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
                val alarm = Alarm(time = time, daysActive = daysActive, volume = 5, isActive = true, userId = userId)
                viewModel.addAlarm(alarm)
                onDismiss()
            }) {
                Text("Create")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
