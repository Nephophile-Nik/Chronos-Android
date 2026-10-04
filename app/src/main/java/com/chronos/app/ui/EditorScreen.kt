package com.chronos.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import com.chronos.app.data.RepeatType
import com.chronos.app.data.ScheduledMessage
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    existing: ScheduledMessage?,
    onSave: (ScheduledMessage) -> Unit,
    onBack: () -> Unit,
) {
    var title by remember { mutableStateOf(existing?.title ?: "") }
    var body by remember { mutableStateOf(existing?.body ?: "") }
    var name by remember { mutableStateOf(existing?.recipientName ?: "") }
    var phone by remember { mutableStateOf(existing?.phone ?: "") }
    var isGroup by remember { mutableStateOf(existing?.isGroup ?: false) }
    var repeat by remember { mutableStateOf(existing?.repeatType ?: RepeatType.DAILY) }
    var mask by remember { mutableIntStateOf(existing?.daysMask ?: 0) }
    var hour by remember { mutableIntStateOf(existing?.hour ?: 7) }
    var minute by remember { mutableIntStateOf(existing?.minute ?: 0) }
    var date by remember {
        mutableStateOf(existing?.oneTimeDate?.let { LocalDate.ofEpochDay(it) } ?: LocalDate.now())
    }
    var error by remember { mutableStateOf<String?>(null) }
    var showTime by remember { mutableStateOf(false) }
    var showDate by remember { mutableStateOf(false) }

    fun validate(): String? {
        if (title.isBlank()) return "Enter a title."
        if (body.isBlank()) return "Enter the message."
        if (name.isBlank()) return "Enter the recipient's name."
        if (!isGroup && phone.count { it.isDigit() } < 8) return "Enter the phone number with country code."
        if (repeat == RepeatType.DAYS && mask == 0) return "Pick at least one day."
        if (repeat == RepeatType.ONCE &&
            !ZonedDateTime.of(date, LocalTime.of(hour, minute), ZoneId.systemDefault()).isAfter(ZonedDateTime.now())
        ) return "Pick a date and time in the future."
        return null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (existing == null) "New message" else "Edit message") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
                },
            )
        },
    ) { pad ->
        Column(
            Modifier.fillMaxSize().padding(pad).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(title, { title = it }, label = { Text("Title") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(body, { body = it }, label = { Text("Message") }, minLines = 3, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(name, { name = it }, label = { Text("Recipient name") }, singleLine = true, modifier = Modifier.fillMaxWidth())

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("This is a group", Modifier.weight(1f))
                Switch(isGroup, { isGroup = it })
            }
            if (isGroup) {
                Text(
                    "Groups can't be opened reliably by Chronos. At send time it opens WhatsApp's chat picker with your text prefilled and you pick the group and press Send.",
                    style = MaterialTheme.typography.bodySmall,
                )
            } else {
                OutlinedTextField(
                    phone, { phone = it },
                    label = { Text("WhatsApp number (with country code)") },
                    placeholder = { Text("919876543210") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Text("Repeat", style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(repeat == RepeatType.ONCE, { repeat = RepeatType.ONCE }, label = { Text("Once") })
                FilterChip(repeat == RepeatType.DAILY, { repeat = RepeatType.DAILY }, label = { Text("Daily") })
                FilterChip(repeat == RepeatType.DAYS, { repeat = RepeatType.DAYS }, label = { Text("Days") })
            }
            if (repeat == RepeatType.DAYS) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    DAY_NAMES.forEachIndexed { i, d ->
                        FilterChip(
                            selected = (mask shr i) and 1 == 1,
                            onClick = { mask = mask xor (1 shl i) },
                            label = { Text(d.take(1)) },
                        )
                    }
                }
            }
            if (repeat == RepeatType.ONCE) {
                OutlinedButton(onClick = { showDate = true }) { Text("Date: $date") }
            }
            OutlinedButton(onClick = { showTime = true }) { Text("Time: ${timeText(hour, minute)}") }

            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    val e = validate()
                    if (e != null) { error = e; return@Button }
                    onSave(
                        (existing ?: ScheduledMessage(title = "", body = "", recipientName = "", hour = 0, minute = 0)).copy(
                            title = title.trim(),
                            body = body.trim(),
                            recipientName = name.trim(),
                            phone = if (isGroup) "" else phone.filter { it.isDigit() },
                            isGroup = isGroup,
                            hour = hour,
                            minute = minute,
                            repeatType = repeat,
                            daysMask = if (repeat == RepeatType.DAYS) mask else 0,
                            oneTimeDate = if (repeat == RepeatType.ONCE) date.toEpochDay() else null,
                            enabled = existing?.enabled ?: true,
                        )
                    )
                },
            ) { Text("Save") }
        }
    }

    if (showTime) {
        val st = rememberTimePickerState(hour, minute, false)
        AlertDialog(
            onDismissRequest = { showTime = false },
            confirmButton = { TextButton(onClick = { hour = st.hour; minute = st.minute; showTime = false }) { Text("OK") } },
            dismissButton = { TextButton(onClick = { showTime = false }) { Text("Cancel") } },
            text = { TimePicker(state = st) },
        )
    }
    if (showDate) {
        val ds = rememberDatePickerState(
            initialSelectedDateMillis = date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDate = false },
            confirmButton = {
                TextButton(onClick = {
                    ds.selectedDateMillis?.let {
                        date = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()
                    }
                    showDate = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDate = false }) { Text("Cancel") } },
        ) { DatePicker(state = ds) }
    }
}
