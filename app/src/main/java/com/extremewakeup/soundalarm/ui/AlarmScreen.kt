import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults.cardElevation
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.extremewakeup.soundalarm.model.Alarm
import com.extremewakeup.soundalarm.ui.CreateAlarmDialog
import com.extremewakeup.soundalarm.ui.LighterNavyBlue
import com.extremewakeup.soundalarm.ui.MainViewModel
import com.extremewakeup.soundalarm.ui.MintGreen
import com.extremewakeup.soundalarm.ui.NavyBlue
import java.time.format.DateTimeFormatter

@Composable
fun AlarmScreen(navController: NavController, viewModel: MainViewModel, context: Context) {
    val userId = 1
    val alarmList by viewModel.alarmList.observeAsState(initial = emptyList())
    var showCreateAlarmDialog by remember { mutableStateOf(false) }
    val isConnected = true
    var expandFirstAlarm by remember { mutableStateOf(false) }

    if (!isConnected) {
        LaunchedEffect(Unit) {
            navController.navigate("bluetoothPairingRoute") {
                popUpTo("alarmScreenRoute") { inclusive = true }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 100.dp)
        ) {
            AlarmList(alarmList, viewModel, expandFirstAlarm)
        }

        CreateAlarmButton(
            onClick = {
                showCreateAlarmDialog = true
                expandFirstAlarm = false
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )
    }

    if (showCreateAlarmDialog) {
        CreateAlarmDialog(viewModel, context, userId, onDismiss = {
            showCreateAlarmDialog = false
            expandFirstAlarm = true
        })
    }
}

@Composable
fun AlarmList(alarmList: List<Alarm>, viewModel: MainViewModel, expandFirstAlarm: Boolean) {
    LazyColumn {
        val reversedAlarmList = alarmList.reversed()
        items(reversedAlarmList.size) { index ->
            val alarm = reversedAlarmList[index]
            val isExpandedInitially = expandFirstAlarm && alarmList.last() == alarm
            AlarmCard(
                alarm = alarm,
                isActive = true,
                isExpandedInitially = isExpandedInitially,
                onDayToggle = { dayIndex ->
                    viewModel.selectDaysActive(alarm, dayIndex)
                }
            )
        }
    }
}

@Composable
fun AlarmCard(alarm: Alarm, isActive: Boolean, isExpandedInitially: Boolean, onDayToggle: (Int) -> Unit) {
    var isExpanded by remember { mutableStateOf(isExpandedInitially) }
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    val formattedTime = alarm.time.format(timeFormatter)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clip(RoundedCornerShape(10.dp)),
        elevation = cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(formattedTime, style = MaterialTheme.typography.headlineLarge)
                    Text(
                        alarm.daysSelected.joinToString(", "),
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                    )
                }

                Switch(
                    checked = isActive,
                    onCheckedChange = { newIsActive ->
                        alarm.isActive = newIsActive
                    },
                )

                Icon(
                    imageVector = if (isExpanded) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown,
                    contentDescription = "Expand",
                    modifier = Modifier.clickable { isExpanded = !isExpanded }
                )
            }

            if (isExpanded) {
                DayPicker(alarm.daysSelected, onDayToggle)
            }
        }
    }
}

@Composable
fun CreateAlarmButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(70.dp) // Set the size of the circle
            .background(color = MintGreen, shape = androidx.compose.foundation.shape.CircleShape) // Set the background color and shape
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.Default.Add,
            contentDescription = "Add alarm",
            tint = NavyBlue, // Set the icon color for visibility
            modifier = Modifier.size(25.dp) // Adjust the size of the icon
        )
    }
}

@Composable
fun DayPicker(selectedDays: List<String>, onDayToggle: (Int) -> Unit) {
    val selectedDaysIndexMap = mapOf("Sunday" to 0, "Monday" to 1, "Tuesday" to 2, "Wednesday" to 3, "Thursday" to 4, "Friday" to 5, "Saturday" to 6)
    val selectedDaysIndexes = selectedDays.map { fruit -> selectedDaysIndexMap[fruit] ?: "unknown" }
    val daysOfWeekInitials = listOf("S", "M", "T", "W", "T", "F", "S")
    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(8.dp)
    ) {
        daysOfWeekInitials.forEachIndexed { index, initial ->
            val isSelected = selectedDaysIndexes.contains(index)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(30.dp)
                    .background(if (isSelected) LighterNavyBlue else Color.Transparent, CircleShape)
                    .clickable { onDayToggle(index) }
            ) {
                Text(initial, color = if (isSelected) Color.White else Color.Black)
            }
        }
    }
}
