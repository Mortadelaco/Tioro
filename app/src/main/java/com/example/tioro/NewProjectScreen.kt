package com.example.tioro

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewProjectScreen(
    navController: NavController,
    viewModel: InquiViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val isLocked = uiState.isBlockLockedForWeek
    val objectives = uiState.currentWeekObjectives

    // Solo podemos editar el título si estamos creando el bloque (0 objetivos)
    var blockTitle by remember { 
        mutableStateOf(objectives.firstOrNull()?.blockTitle ?: "") 
    }
    var newObjectiveText by remember { mutableStateOf("") }

    Scaffold(
        containerColor = InquiDarkBackground,
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Bloque de Trabajo",
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(24.dp), modifier = Modifier.weight(1f)) {
                
                if (isLocked) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = InquiSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, InquiGold)
                    ) {
                        Text(
                            text = "Bloque Semanal Completado al 100%. No puedes realizar modificaciones hasta el próximo lunes.",
                            color = InquiGold,
                            modifier = Modifier.padding(16.dp),
                            fontSize = 14.sp
                        )
                    }
                }

                // Campo Nombre del Bloque
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "TÍTULO DEL BLOQUE",
                        color = InquiTextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    OutlinedTextField(
                        value = blockTitle,
                        onValueChange = { if (objectives.isEmpty()) blockTitle = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        enabled = objectives.isEmpty(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = InquiSurface,
                            unfocusedContainerColor = InquiSurface,
                            focusedBorderColor = InquiGold,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            disabledContainerColor = InquiSurface,
                            disabledTextColor = Color.White,
                            disabledBorderColor = Color.Transparent
                        ),
                        singleLine = true,
                        placeholder = { Text("Ej: Desarrollo MVP", color = InquiTextMuted) }
                    )
                }

                // Lista de objetivos
                Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                    Text(
                        text = "OBJETIVOS",
                        color = InquiTextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    
                    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(objectives) { obj ->
                            val scale by androidx.compose.animation.core.animateFloatAsState(
                                targetValue = if (obj.isCompleted) 1.2f else 1f,
                                animationSpec = androidx.compose.animation.core.spring(
                                    dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
                                    stiffness = androidx.compose.animation.core.Spring.StiffnessLow
                                )
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(InquiSurface, RoundedCornerShape(12.dp))
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Checkbox(
                                        checked = obj.isCompleted,
                                        onCheckedChange = { 
                                            if (!isLocked) {
                                                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                                viewModel.toggleWorkBlockObjectiveCompletion(obj)
                                            }
                                        },
                                        enabled = !isLocked,
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = InquiGold,
                                            uncheckedColor = InquiTextMuted,
                                            checkmarkColor = InquiDarkBackground
                                        ),
                                        modifier = Modifier.scale(scale)
                                    )
                                    Text(
                                        text = obj.objectiveName,
                                        color = Color.White,
                                        fontSize = 14.sp
                                    )
                                }
                                if (!isLocked && !obj.isCompleted) {
                                    IconButton(onClick = { viewModel.deleteWorkBlockObjective(obj) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = InquiTextMuted)
                                    }
                                }
                            }
                        }
                    }
                }

                if (!isLocked) {
                    // Campo Añadir Objetivo
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = newObjectiveText,
                            onValueChange = { newObjectiveText = it },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = InquiSurface,
                                unfocusedContainerColor = InquiSurface,
                                focusedBorderColor = InquiGold,
                                unfocusedBorderColor = Color.Transparent,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            singleLine = true,
                            placeholder = { Text("Nuevo objetivo...", color = InquiTextMuted) }
                        )
                        IconButton(
                            onClick = {
                                if (newObjectiveText.isNotBlank() && blockTitle.isNotBlank()) {
                                    viewModel.addWorkBlockObjective(title = blockTitle, objectiveName = newObjectiveText)
                                    newObjectiveText = ""
                                }
                            },
                            modifier = Modifier
                                .background(InquiGold, RoundedCornerShape(12.dp))
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Añadir", tint = InquiDarkBackground)
                        }
                    }
                }
            }
        }
    }
}
