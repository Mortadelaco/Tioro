package com.example.tioro

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreen(
    navController: NavController,
    viewModel: InquiViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val backupLogs = uiState.backupLogs
    val backupDate = uiState.backupSelectedDate
    
    var showDatePicker by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var newActivityName by remember { mutableStateOf("") }
    var newActivityIsEffort by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        if (uiState.backupSelectedDate == LocalDate.now().toString()) {
            viewModel.selectBackupDate(LocalDate.now().minusDays(1).toString())
        } else {
            viewModel.selectBackupDate(uiState.backupSelectedDate)
        }
    }

    Scaffold(
        containerColor = InquiDarkBackground,
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Máquina del Tiempo",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Atrás",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = InquiDarkBackground
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = InquiGold,
                contentColor = InquiDarkBackground
            ) {
                Icon(Icons.Default.Add, contentDescription = "Añadir")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = InquiSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, InquiGold.copy(alpha = 0.5f))
            ) {
                Text(
                    text = "Usa el Backup como un salvavidas excepcional para días olvidados, no como una costumbre. La honestidad es la base de Tioro.",
                    color = InquiTextMuted,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(16.dp),
                    lineHeight = 18.sp
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "FECHA SELECCIONADA",
                        color = InquiTextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .clickable { showDatePicker = true }
                            .background(InquiSurface, RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.DateRange, contentDescription = null, tint = InquiGold, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = backupDate,
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "NOTA DEL DÍA",
                        color = InquiTextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${uiState.backupDayScore.toInt()}%",
                        color = if (uiState.backupDayScore >= 50f) InquiGold else Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Text(
                        text = if (uiState.backupDayScore >= 50f) "Racha a salvo" else "Racha perdida",
                        color = if (uiState.backupDayScore >= 50f) InquiGold else InquiTextMuted,
                        fontSize = 10.sp
                    )
                }
            }

            Text(
                text = "ACTIVIDADES",
                color = InquiTextMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            if (backupLogs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay registros en esta fecha.",
                        color = InquiTextMuted,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(backupLogs) { log ->
                        ActivityLogItem(
                            log = log,
                            onToggleComplete = { viewModel.toggleBackupActivityCompletion(it) },
                            onDelete = { viewModel.deleteBackupActivity(it) },
                            onClick = { } // No edit name in backup to keep it simple, or add it later
                        )
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = Instant.now().toEpochMilli(),
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    val todayEnd = LocalDate.now().atStartOfDay(ZoneId.of("UTC")).plusDays(1).toInstant().toEpochMilli()
                    return utcTimeMillis < todayEnd
                }
            }
        )
        
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val localDate = Instant.ofEpochMilli(millis).atZone(ZoneId.of("UTC")).toLocalDate()
                        viewModel.selectBackupDate(localDate.toString())
                    }
                    showDatePicker = false
                }) {
                    Text("Aceptar", color = InquiGold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar", color = InquiTextMuted)
                }
            },
            colors = DatePickerDefaults.colors(
                containerColor = InquiSurface,
                titleContentColor = InquiGold,
                headlineContentColor = Color.White,
                weekdayContentColor = InquiTextMuted,
                subheadContentColor = InquiTextMuted,
                yearContentColor = Color.White,
                currentYearContentColor = InquiGold,
                selectedYearContentColor = InquiDarkBackground,
                selectedYearContainerColor = InquiGold,
                dayContentColor = Color.White,
                selectedDayContentColor = InquiDarkBackground,
                selectedDayContainerColor = InquiGold,
                todayContentColor = InquiGold,
                todayDateBorderColor = InquiGold
            )
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    titleContentColor = InquiGold,
                    headlineContentColor = Color.White,
                    weekdayContentColor = InquiTextMuted,
                    subheadContentColor = InquiTextMuted,
                    yearContentColor = Color.White,
                    currentYearContentColor = InquiGold,
                    selectedYearContentColor = InquiDarkBackground,
                    selectedYearContainerColor = InquiGold,
                    dayContentColor = Color.White,
                    selectedDayContentColor = InquiDarkBackground,
                    selectedDayContainerColor = InquiGold,
                    todayContentColor = InquiGold,
                    todayDateBorderColor = InquiGold
                )
            )
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            containerColor = InquiSurface,
            title = { Text("Añadir Actividad Pasada", color = Color.White) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(
                        value = newActivityName,
                        onValueChange = { newActivityName = it },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = InquiGold
                        ),
                        placeholder = { Text("Nombre de la actividad", color = InquiTextMuted) }
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = newActivityIsEffort,
                            onCheckedChange = { newActivityIsEffort = it },
                            colors = CheckboxDefaults.colors(checkedColor = InquiGold)
                        )
                        Text("Es Actividad+ (Mayor esfuerzo)", color = Color.White, fontSize = 14.sp)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newActivityName.isNotBlank()) {
                        viewModel.addBackupActivity(backupDate, newActivityName, newActivityIsEffort)
                        newActivityName = ""
                        newActivityIsEffort = false
                        showAddDialog = false
                    }
                }) { Text("Guardar", color = InquiGold) }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancelar", color = InquiTextMuted) }
            }
        )
    }
}
