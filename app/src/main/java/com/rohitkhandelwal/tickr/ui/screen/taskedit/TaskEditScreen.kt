package com.rohitkhandelwal.tickr.ui.screen.taskedit

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rohitkhandelwal.tickr.core.time.TaskDateFormatter
import java.util.Calendar
import java.util.TimeZone

@Composable
fun TaskEditScreen(
    viewModel: TaskEditViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                TaskEditEvent.Saved -> onNavigateBack()
            }
        }
    }

    TaskEditScreen(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onTitleChanged = viewModel::onTitleChanged,
        onDescriptionChanged = viewModel::onDescriptionChanged,
        onDueDateChanged = viewModel::onDueDateChanged,
        onCompletedChanged = viewModel::onCompletedChanged,
        onSaveClicked = viewModel::onSaveClicked
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaskEditScreen(
    uiState: TaskEditUiState,
    onNavigateBack: () -> Unit,
    onTitleChanged: (String) -> Unit,
    onDescriptionChanged: (String) -> Unit,
    onDueDateChanged: (Long?) -> Unit,
    onCompletedChanged: (Boolean) -> Unit,
    onSaveClicked: () -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (uiState.isEditMode) "Edit Task" else "Create Task")
                },
                navigationIcon = {
                    TextButton(onClick = onNavigateBack) {
                        Text("Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (showDatePicker) {
            TickrDatePickerDialog(
                initialDate = uiState.dueDate,
                onDismiss = { showDatePicker = false },
                onDateSelected = { selectedDate ->
                    onDueDateChanged(selectedDate)
                    showDatePicker = false
                }
            )
        }

        if (uiState.isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = uiState.title,
                    onValueChange = onTitleChanged,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Title") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    isError = uiState.titleError != null,
                    supportingText = {
                        uiState.titleError?.let { Text(it) }
                    }
                )

                OutlinedTextField(
                    value = uiState.description,
                    onValueChange = onDescriptionChanged,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Description") },
                    minLines = 3
                )

                OutlinedTextField(
                    value = TaskDateFormatter.format(uiState.dueDate),
                    onValueChange = {},
                    label = { Text("Due date") },
                    singleLine = true,
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            focusManager.clearFocus()
                            showDatePicker = true
                        },
                    supportingText = {
                        Text("Optional")
                    }
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = {
                        focusManager.clearFocus()
                        showDatePicker = true
                    }) {
                        Text(if (uiState.dueDate == null) "Pick Date" else "Change Date")
                    }

                    if (uiState.dueDate != null) {
                        TextButton(onClick = { onDueDateChanged(null) }) {
                            Text("Clear")
                        }
                    }
                }

                if (uiState.isEditMode) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Checkbox(
                            checked = uiState.isCompleted,
                            onCheckedChange = onCompletedChanged
                        )
                        Text(
                            text = "Mark as completed",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                Button(
                    onClick = onSaveClicked,
                    enabled = !uiState.isSaving,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (uiState.isSaving) "Saving..." else "Save Task")
                }

                Text(
                    text = "Tip: include [fail-sync] in title or description to simulate sync failure and test retries.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TickrDatePickerDialog(
    initialDate: Long?,
    onDismiss: () -> Unit,
    onDateSelected: (Long?) -> Unit
) {
    val datePickerState = androidx.compose.material3.rememberDatePickerState(
        initialSelectedDateMillis = initialDate?.toUtcStartOfDay()
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onDateSelected(datePickerState.selectedDateMillis?.fromUtcStartOfDay())
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

private fun Long.toUtcStartOfDay(): Long {
    val localCalendar = Calendar.getInstance().apply {
        timeInMillis = this@toUtcStartOfDay
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    val utcCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        set(Calendar.YEAR, localCalendar.get(Calendar.YEAR))
        set(Calendar.MONTH, localCalendar.get(Calendar.MONTH))
        set(Calendar.DAY_OF_MONTH, localCalendar.get(Calendar.DAY_OF_MONTH))
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    return utcCalendar.timeInMillis
}

private fun Long.fromUtcStartOfDay(): Long {
    val utcCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        timeInMillis = this@fromUtcStartOfDay
    }

    val localCalendar = Calendar.getInstance().apply {
        set(Calendar.YEAR, utcCalendar.get(Calendar.YEAR))
        set(Calendar.MONTH, utcCalendar.get(Calendar.MONTH))
        set(Calendar.DAY_OF_MONTH, utcCalendar.get(Calendar.DAY_OF_MONTH))
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    return localCalendar.timeInMillis
}
