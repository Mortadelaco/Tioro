package com.example.tioro

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullDailyListScreen(
    navController: NavController,
    viewModel: InquiViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val logs = uiState.logs

    var logToEdit by remember { mutableStateOf<ActivityLogItemData?>(null) }

    Scaffold(
        containerColor = InquiDarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Lista Diaria Completa",
                        color = Color.White,
                        fontSize = 16.sp,
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
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (logs.isEmpty()) {
                Text(
                    text = "Aún no hay actividades registradas hoy.",
                    color = InquiTextMuted,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(24.dp)
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        SectionTitle(title = "ACTIVIDADES")
                    }
                    items(logs, key = { "log_${it.id}" }) { log ->
                        ActivityLogItem(
                            log = log,
                            onToggleComplete = { viewModel.toggleActivityCompletion(log) },
                            onDelete = { viewModel.deleteLog(log) },
                            onClick = { logToEdit = log }
                        )
                    }
                }
            }
        }

        // Modal de edición compartido
        logToEdit?.let { log ->
            var newName by remember { mutableStateOf(log.name) }
            AlertDialog(
                onDismissRequest = { logToEdit = null },
                containerColor = InquiSurface,
                title = { Text("Editar Actividad", color = Color.White) },
                text = {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = InquiGold
                        )
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (newName.isNotBlank()) viewModel.updateLogName(log.id, newName)
                        logToEdit = null
                    }) { Text("Guardar", color = InquiGold) }
                },
                dismissButton = {
                    TextButton(onClick = { logToEdit = null }) { Text("Cancelar", color = InquiTextMuted) }
                }
            )
        }
    }
}
