package com.example.tioro

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.clickable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import java.util.Locale

// Colores personalizados basados en el diseño de Figma
val InquiDarkBackground = Color(0xFF0D0D0D)
val InquiSurface = Color(0xFF1C1C1E) // Un poco más claro/frío para hacer mejor contraste
val InquiGold = Color(0xFFE5C07B)
val InquiTextMuted = Color(0xFFA0A0A0)
val InquiButtonInner = Color(0xFF2A2A2A)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InquiHomeScreen(
    navController: NavController,
    viewModel: InquiViewModel,
    onNavigateToRegister: (String) -> Unit,
    onNavigateToDailyList: () -> Unit,
    onNavigateToNewProject: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    InquiHomeScreenContent(
        navController = navController,
        uiState = uiState,
        onAddLeisure = { onNavigateToRegister("Actividad") },
        onAddEffort = { onNavigateToRegister("Actividad+") },
        onToggleComplete = { log -> viewModel.toggleActivityCompletion(log) },
        onDeleteLog = { log -> viewModel.deleteLog(log) },
        onEditLog = { logId, newName -> viewModel.updateLogName(logId, newName) },
        onExpandList = onNavigateToDailyList,
        onNavigateToWorkBlock = onNavigateToNewProject, // Using this route for the block screen
        onToggleObjectiveComplete = { obj -> viewModel.toggleWorkBlockObjectiveCompletion(obj) },
        onSaveUserName = { newName -> viewModel.saveUserName(newName) },
        onGetRecordsInRange = { start, end -> viewModel.getRecordsInRange(start, end) },
        onDismissMondayRecap = { viewModel.dismissMondayRecap() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InquiHomeScreenContent(
    navController: NavController? = null,
    uiState: InquiUiState,
    onAddLeisure: () -> Unit,
    onAddEffort: () -> Unit,
    onToggleComplete: (ActivityLogItemData) -> Unit,
    onDeleteLog: (ActivityLogItemData) -> Unit,
    onEditLog: (Int, String) -> Unit,
    onExpandList: () -> Unit,
    onNavigateToWorkBlock: () -> Unit,
    onToggleObjectiveComplete: (com.example.tioro.data.local.WorkBlockObjective) -> Unit,
    onSaveUserName: (String) -> Unit,
    onGetRecordsInRange: suspend (String, String) -> List<com.example.tioro.data.local.DailyRecord> = { _, _ -> emptyList() },
    onDismissMondayRecap: () -> Unit = {}
) {
    var logToEdit by remember { mutableStateOf<ActivityLogItemData?>(null) }
    var showStreakSheet by remember { mutableStateOf(false) }

    var isEditingName by remember { mutableStateOf(false) }
    var hasAnimatedEntrance by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(false) }

    val animAlpha1 by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (hasAnimatedEntrance) 1f else 0f, 
        animationSpec = androidx.compose.animation.core.tween(350)
    )
    val animY1 by androidx.compose.animation.core.animateDpAsState(
        targetValue = if (hasAnimatedEntrance) 0.dp else 24.dp, 
        animationSpec = androidx.compose.animation.core.tween(350, easing = androidx.compose.animation.core.FastOutSlowInEasing)
    )

    val animAlpha2 by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (hasAnimatedEntrance) 1f else 0f, 
        animationSpec = androidx.compose.animation.core.tween(350, delayMillis = 100)
    )
    val animY2 by androidx.compose.animation.core.animateDpAsState(
        targetValue = if (hasAnimatedEntrance) 0.dp else 24.dp, 
        animationSpec = androidx.compose.animation.core.tween(350, delayMillis = 100, easing = androidx.compose.animation.core.FastOutSlowInEasing)
    )

    val animAlpha3 by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (hasAnimatedEntrance) 1f else 0f, 
        animationSpec = androidx.compose.animation.core.tween(350, delayMillis = 200)
    )
    val animY3 by androidx.compose.animation.core.animateDpAsState(
        targetValue = if (hasAnimatedEntrance) 0.dp else 24.dp, 
        animationSpec = androidx.compose.animation.core.tween(350, delayMillis = 200, easing = androidx.compose.animation.core.FastOutSlowInEasing)
    )

    androidx.compose.runtime.LaunchedEffect(Unit) {
        hasAnimatedEntrance = true
    }

    Scaffold(
        containerColor = InquiDarkBackground,
        bottomBar = { InquiBottomNavigationBar(navController) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = animY1)
                    .alpha(animAlpha1),
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                TopSection(
                    score = uiState.dailyScore,
                    streak = uiState.currentStreak,
                    onStreakClick = { showStreakSheet = true }
                )
                GreetingSection(
                    userName = uiState.userName ?: "Tioro User",
                    onEditNameClick = { isEditingName = true }
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = animY2)
                    .alpha(animAlpha2)
            ) {
                MiddleSection(
                    leisureCount = uiState.leisureCount,
                    effortCount = uiState.effortCount,
                    logs = uiState.logs,
                    onAddLeisure = onAddLeisure,
                    onAddEffort = onAddEffort,
                    onToggleComplete = onToggleComplete,
                    onDeleteLog = onDeleteLog,
                    onClickLog = { logToEdit = it },
                    onExpandList = onExpandList
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = animY3)
                    .alpha(animAlpha3)
            ) {
                WorkBlockSection(
                    uiState = uiState,
                    onNavigateToWorkBlock = onNavigateToWorkBlock,
                    onToggleObjectiveComplete = onToggleObjectiveComplete
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (showStreakSheet) {
        StreakBottomSheet(
            streak = uiState.currentStreak,
            isStreakSavedToday = uiState.isStreakSavedToday,
            onGetRecordsInRange = onGetRecordsInRange,
            onDismiss = { showStreakSheet = false }
        )
    }

    // Modal de edición de log
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
                    if (newName.isNotBlank()) onEditLog(log.id, newName)
                    logToEdit = null 
                }) { Text("Guardar", color = InquiGold) }
            },
            dismissButton = {
                TextButton(onClick = { logToEdit = null }) { Text("Cancelar", color = InquiTextMuted) }
            }
        )
    }

    if (isEditingName) {
        var newUserName by remember { mutableStateOf(uiState.userName ?: "") }
        AlertDialog(
            onDismissRequest = { isEditingName = false },
            containerColor = InquiSurface,
            title = { Text("Cambiar nombre", color = Color.White) },
            text = {
                OutlinedTextField(
                    value = newUserName,
                    onValueChange = { newUserName = it },
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
                    if (newUserName.isNotBlank()) {
                        onSaveUserName(newUserName)
                        isEditingName = false 
                    }
                }) { Text("Guardar", color = InquiGold) }
            },
            dismissButton = {
                TextButton(onClick = { isEditingName = false }) { Text("Cancelar", color = InquiTextMuted) }
            }
        )
    }

    uiState.mondayRecapData?.let { recap ->
        AlertDialog(
            onDismissRequest = { onDismissMondayRecap() },
            containerColor = InquiSurface,
            title = {
                Text(
                    text = "Resumen de tu semana",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "${String.format(Locale.getDefault(), "%.1f", recap.lastWeekFinalScore)}%",
                        color = InquiGold,
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Surface(
                        color = InquiDarkBackground,
                        shape = RoundedCornerShape(50),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp, 
                            if (recap.lastWeekEarnedMedal) InquiGold else InquiTextMuted
                        )
                    ) {
                        Text(
                            text = if (recap.lastWeekEarnedMedal) "🏆 ¡Medalla semanal conseguida!" else "Semana cerrada sin medalla",
                            color = if (recap.lastWeekEarnedMedal) InquiGold else InquiTextMuted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }

                    Text(
                        text = if (recap.carriedOverObjectivesCount > 0) {
                            "Se han arrastrado ${recap.carriedOverObjectivesCount} objetivos pendientes del Bloque de Trabajo a esta nueva semana."
                        } else {
                            "¡Completaste al 100% los objetivos de tu Bloque de Trabajo de la semana anterior!"
                        },
                        color = InquiTextMuted,
                        fontSize = 13.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        lineHeight = 18.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { onDismissMondayRecap() },
                    colors = ButtonDefaults.buttonColors(containerColor = InquiGold),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Empezar nueva semana", color = InquiDarkBackground, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
fun TopSection(score: Float, streak: Int, onStreakClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Badge de estadísticas recientes
        Surface(
            color = InquiSurface,
            shape = RoundedCornerShape(50),
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Punto en lugar de estrella para imitar el Figma
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(InquiGold)
                )
                Text(
                    text = "Nota hoy: ${String.format(Locale.getDefault(), "%.1f", score)}",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Contador de Racha a la derecha
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .clickable { onStreakClick() }
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            FlameIcon(modifier = Modifier.size(24.dp))
            Text(
                text = "$streak",
                color = InquiGold,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun GreetingSection(userName: String, onEditNameClick: () -> Unit) {
    Column {
        Text(
            text = "Buenos días,",
            color = InquiTextMuted,
            fontSize = 32.sp,
            fontFamily = FontFamily.Serif,
            fontStyle = FontStyle.Italic
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { onEditNameClick() }.padding(end = 16.dp)
        ) {
            Text(
                text = userName,
                color = Color.White,
                fontSize = 40.sp,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Normal,
                lineHeight = 44.sp
            )
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Editar nombre",
                tint = InquiTextMuted,
                modifier = Modifier.padding(start = 12.dp).size(20.dp)
            )
        }
    }
}

@Composable
fun MiddleSection(
    leisureCount: Int,
    effortCount: Int,
    logs: List<ActivityLogItemData>,
    onAddLeisure: () -> Unit,
    onAddEffort: () -> Unit,
    onToggleComplete: (ActivityLogItemData) -> Unit,
    onDeleteLog: (ActivityLogItemData) -> Unit,
    onClickLog: (ActivityLogItemData) -> Unit,
    onExpandList: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Columna Izquierda: Botones de Actividad
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SectionTitle(title = "REGISTRAR")
            ActivityButton(
                title = "Actividad ($leisureCount)", 
                icon = Icons.Default.PlayArrow,
                onClick = onAddLeisure
            )
            ActivityButton(
                title = "Actividad+ ($effortCount)", 
                icon = Icons.Default.Add,
                onClick = onAddEffort
            )
        }

        // Columna Derecha: Lista Diaria
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onExpandList() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ACTUALIZAR\nLISTA DIARIA",
                    color = InquiTextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 14.sp
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar",
                        tint = InquiTextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.CallMade, // Flecha diagonal
                        contentDescription = "Ir",
                        tint = InquiTextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            
            // Lista envuelta en un Card para imitar el figma
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = InquiSurface),
                modifier = Modifier.fillMaxWidth()
            ) {
                val pendingLogs = logs.filter { !it.isCompleted }
                Column {
                    if (pendingLogs.isEmpty()) {
                        Text(
                            text = "Aún no hay actividades",
                            color = InquiTextMuted,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(16.dp)
                        )
                    } else {
                        // Actividades pendientes
                        pendingLogs.take(4).forEachIndexed { index, log ->
                            ActivityLogItem(
                                log = log,
                                onToggleComplete = onToggleComplete,
                                onDelete = onDeleteLog, 
                                onClick = onClickLog
                            )
                            if (index < pendingLogs.take(4).size - 1) {
                                HorizontalDivider(color = Color(0xFF2A2A2A), thickness = 1.dp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ActivityButton(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit = {}) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = InquiSurface),
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp) // Altura explícita para que sea un botón grande y casi cuadrado
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Icono dentro de una cajita interior
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(InquiButtonInner, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = InquiGold,
                    modifier = Modifier.size(24.dp)
                )
            }
            Text(
                text = title,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = InquiTextMuted,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityLogItem(
    log: ActivityLogItemData,
    onToggleComplete: (ActivityLogItemData) -> Unit,
    onDelete: (ActivityLogItemData) -> Unit,
    onClick: (ActivityLogItemData) -> Unit
) {
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    val checkboxScale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (log.isCompleted) 1.2f else 1f,
        animationSpec = androidx.compose.animation.core.spring(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
            stiffness = androidx.compose.animation.core.Spring.StiffnessLow
        )
    )

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            if (it == SwipeToDismissBoxValue.EndToStart) {
                onDelete(log)
                true
            } else false
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val color = if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) {
                Color.Red.copy(alpha = 0.8f)
            } else Color.Transparent
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color)
                    .padding(end = 16.dp), 
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.White)
            }
        },
        enableDismissFromStartToEnd = false
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(InquiSurface) // Mismo color que la card base para que se funda bien
                .clickable { onClick(log) }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = log.isCompleted,
                    onCheckedChange = { 
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                        onToggleComplete(log) 
                    },
                    colors = CheckboxDefaults.colors(
                        checkedColor = InquiGold,
                        uncheckedColor = InquiTextMuted,
                        checkmarkColor = InquiDarkBackground
                    ),
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .size(20.dp)
                        .scale(checkboxScale)
                )
                Text(
                    text = log.name, 
                    color = Color.White, 
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    textDecoration = if (log.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                )
            }
            Text(text = log.date, color = InquiTextMuted, fontSize = 12.sp)
        }
    }
}

@Composable
fun WorkBlockSection(
    uiState: InquiUiState,
    onNavigateToWorkBlock: () -> Unit,
    onToggleObjectiveComplete: (com.example.tioro.data.local.WorkBlockObjective) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionTitle(title = "BLOQUE DE TRABAJO")
        
        if (uiState.currentWeekObjectives.isEmpty()) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = InquiDarkBackground),
                border = androidx.compose.foundation.BorderStroke(1.dp, InquiGold),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToWorkBlock() }
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Registrar Bloque de Trabajo",
                            color = InquiGold,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Define tus objetivos mayoritarios de la semana (representa el 30% de tu media semanal)",
                            color = InquiTextMuted,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(InquiGold.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Añadir Bloque",
                            tint = InquiGold,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        } else if (uiState.isBlockLockedForWeek) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = InquiSurface),
                border = androidx.compose.foundation.BorderStroke(2.dp, InquiGold),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = "Completado",
                        tint = InquiGold,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "¡Bloque Semanal Completado! (100%)",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Has asegurado el 30% semanal. El próximo bloque se habilitará el lunes.",
                        color = InquiTextMuted,
                        fontSize = 12.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        } else {
            val total = uiState.currentWeekObjectives.size
            val completed = uiState.currentWeekObjectives.count { it.isCompleted }
            val pct = if (total > 0) (completed.toFloat() / total) * 100 else 0f
            
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = InquiSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToWorkBlock() }
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = uiState.currentWeekObjectives.firstOrNull()?.blockTitle ?: "Bloque Actual",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$completed de $total objetivos (${pct.toInt()}%)",
                            color = InquiGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .background(InquiDarkBackground, RoundedCornerShape(50))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fraction = pct / 100f)
                                .fillMaxHeight()
                                .background(InquiGold, RoundedCornerShape(50))
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        uiState.currentWeekObjectives.forEach { obj ->
                            WorkBlockObjectiveItem(
                                obj = obj,
                                onToggleObjectiveComplete = onToggleObjectiveComplete
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WorkBlockObjectiveItem(
    obj: com.example.tioro.data.local.WorkBlockObjective,
    onToggleObjectiveComplete: (com.example.tioro.data.local.WorkBlockObjective) -> Unit
) {
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    val scale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (obj.isCompleted) 1.2f else 1f,
        animationSpec = androidx.compose.animation.core.spring(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
            stiffness = androidx.compose.animation.core.Spring.StiffnessLow
        )
    )
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Checkbox(
            checked = obj.isCompleted,
            onCheckedChange = { 
                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                onToggleObjectiveComplete(obj) 
            },
            colors = CheckboxDefaults.colors(
                checkedColor = InquiGold,
                uncheckedColor = InquiTextMuted,
                checkmarkColor = InquiDarkBackground
            ),
            modifier = Modifier.size(24.dp).scale(scale)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = obj.objectiveName,
            color = if (obj.isCompleted) InquiTextMuted else Color.White,
            fontSize = 13.sp,
            textDecoration = if (obj.isCompleted) TextDecoration.LineThrough else TextDecoration.None
        )
    }
}

@Composable
fun ProjectCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = InquiSurface),
        modifier = modifier.aspectRatio(0.9f) // Un poco más alto que ancho
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(InquiButtonInner, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.CallMade, // Flecha diagonal
                    contentDescription = null,
                    tint = InquiTextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    color = InquiTextMuted,
                    fontSize = 11.sp,
                    lineHeight = 14.sp,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        color = InquiTextMuted,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.2.sp
    )
}

@Composable
fun InquiBottomNavigationBar(navController: NavController? = null) {
    val navBackStackEntry = navController?.currentBackStackEntryAsState()?.value
    val currentRoute = navBackStackEntry?.destination?.route

    val isHome = currentRoute == "home"
    val isStats = currentRoute in listOf("weekly_stats", "monthly_stats", "yearly_stats")
    val isSettings = currentRoute == "settings"

    // Para lograr el diseño de píldora flotante (como en el mockup),
    // creamos un Surface flotante en lugar del NavigationBar que ocupa todo el borde.
    Surface(
        color = InquiSurface,
        shape = RoundedCornerShape(50),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 72.dp, vertical = 24.dp)
            .height(64.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController?.navigate("home") { popUpTo(0) } }) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Inicio",
                    tint = if (isHome) InquiGold else InquiTextMuted
                )
            }
            IconButton(
                onClick = {
                    navController?.navigate("weekly_stats") {
                        navController.graph.findStartDestination().id.let { startId ->
                            popUpTo(startId) {
                                saveState = true
                            }
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                    contentDescription = "Estadísticas",
                    tint = if (isStats) InquiGold else InquiTextMuted
                )
            }
            IconButton(onClick = { navController?.navigate("settings") }) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Ajustes",
                    tint = if (isSettings) InquiGold else InquiTextMuted
                )
            }
        }
    }
}

@Composable
fun FlameIcon(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFFFFA500),
    innerColor: Color = Color(0xFFFFD700)
) {
    androidx.compose.foundation.Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        val outerPath = androidx.compose.ui.graphics.Path().apply {
            moveTo(width * 0.5f, 0f)
            cubicTo(
                width * 0.65f, height * 0.25f,
                width * 0.95f, height * 0.45f,
                width * 0.85f, height * 0.75f
            )
            cubicTo(
                width * 0.75f, height * 0.98f,
                width * 0.25f, height * 0.98f,
                width * 0.15f, height * 0.75f
            )
            cubicTo(
                width * 0.05f, height * 0.5f,
                width * 0.35f, height * 0.3f,
                width * 0.3f, height * 0.15f
            )
            cubicTo(
                width * 0.45f, height * 0.25f,
                width * 0.4f, height * 0.1f,
                width * 0.5f, 0f
            )
            close()
        }

        drawPath(
            path = outerPath,
            color = color
        )

        val innerPath = androidx.compose.ui.graphics.Path().apply {
            moveTo(width * 0.5f, height * 0.45f)
            cubicTo(
                width * 0.65f, height * 0.58f,
                width * 0.75f, height * 0.72f,
                width * 0.65f, height * 0.88f
            )
            cubicTo(
                width * 0.55f, height * 0.96f,
                width * 0.35f, height * 0.96f,
                width * 0.35f, height * 0.88f
            )
            cubicTo(
                width * 0.25f, height * 0.72f,
                width * 0.4f, height * 0.58f,
                width * 0.5f, height * 0.45f
            )
            close()
        }

        drawPath(
            path = innerPath,
            color = innerColor
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StreakBottomSheet(
    streak: Int,
    isStreakSavedToday: Boolean,
    onGetRecordsInRange: suspend (String, String) -> List<com.example.tioro.data.local.DailyRecord>,
    onDismiss: () -> Unit
) {
    var selectedYearMonth by remember { mutableStateOf(java.time.YearMonth.now()) }
    var monthRecords by remember { mutableStateOf<List<com.example.tioro.data.local.DailyRecord>>(emptyList()) }

    androidx.compose.runtime.LaunchedEffect(selectedYearMonth) {
        val start = selectedYearMonth.atDay(1).toString()
        val end = selectedYearMonth.atEndOfMonth().toString()
        monthRecords = onGetRecordsInRange(start, end)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = InquiSurface,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 1. Cabecera de Racha
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar",
                        tint = InquiTextMuted
                    )
                }
                Text(
                    text = "Días de racha",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Box(modifier = Modifier.size(32.dp))
            }

            // 2. Cabecera con Número Gigante y Llama
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "$streak",
                        color = Color.White,
                        fontSize = 56.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 56.sp
                    )
                    Text(
                        text = if (streak == 1) "día de racha" else "días de racha",
                        color = InquiTextMuted,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                FlameIcon(modifier = Modifier.size(72.dp))
            }

            // 3. Mensaje dinámico de motivación
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = InquiDarkBackground),
                border = androidx.compose.foundation.BorderStroke(1.dp, InquiGold.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = if (isStreakSavedToday) "¡Sigue con este ritmo! Tu constancia se nota." else "¡Escribe tus actividades hoy para mantener tu racha viva!",
                        color = Color.White,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            }

            // 4. Calendario de Racha Interactivo
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val monthNameFormatter = java.time.format.DateTimeFormatter.ofPattern("MMMM 'de' yyyy", Locale("es", "ES"))
                val formattedMonthName = selectedYearMonth.format(monthNameFormatter)
                    .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("es", "ES")) else it.toString() }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { selectedYearMonth = selectedYearMonth.minusMonths(1) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Mes anterior",
                            tint = InquiGold
                        )
                    }
                    Text(
                        text = formattedMonthName,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { selectedYearMonth = selectedYearMonth.plusMonths(1) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Mes siguiente",
                            tint = InquiGold
                        )
                    }
                }

                // Grid de iniciales de días de la semana
                val dayHeaders = listOf("L", "Ma", "Mi", "J", "V", "S", "D")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    dayHeaders.forEach { header ->
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = header,
                                color = InquiTextMuted,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Render del mes en filas de 7 días
                val firstDayOfWeekVal = selectedYearMonth.atDay(1).dayOfWeek.value // 1 (Mon) .. 7 (Sun)
                val emptyLeadingSlots = firstDayOfWeekVal - 1
                val daysInMonth = selectedYearMonth.lengthOfMonth()
                val totalCells = emptyLeadingSlots + daysInMonth
                val totalRows = (totalCells + 6) / 7

                val todayStr = java.time.LocalDate.now().toString()

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (rowIndex in 0 until totalRows) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            for (colIndex in 0..6) {
                                val cellIndex = rowIndex * 7 + colIndex
                                val dayNum = cellIndex - emptyLeadingSlots + 1

                                Box(
                                    modifier = Modifier.weight(1f),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (dayNum in 1..daysInMonth) {
                                        val dateStr = selectedYearMonth.atDay(dayNum).toString()
                                        val record = monthRecords.find { it.date == dateStr }
                                        val isApproved = record != null && record.dailyScore >= 50f
                                        val isToday = dateStr == todayStr

                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .then(
                                                    if (isApproved) {
                                                        Modifier.background(InquiGold)
                                                    } else if (isToday) {
                                                        Modifier.border(2.dp, InquiGold, CircleShape)
                                                    } else {
                                                        Modifier
                                                    }
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (isApproved) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.Center
                                                ) {
                                                    Text(
                                                        text = "$dayNum",
                                                        color = InquiDarkBackground,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = null,
                                                        tint = InquiDarkBackground,
                                                        modifier = Modifier.size(10.dp)
                                                    )
                                                }
                                            } else {
                                                Text(
                                                    text = "$dayNum",
                                                    color = if (isToday) Color.White else Color(0xFF666666),
                                                    fontSize = 12.sp,
                                                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                                                )
                                            }
                                        }
                                    } else {
                                        Spacer(modifier = Modifier.size(36.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 5. Meta de racha (Sección inferior)
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionTitle(title = "META DE RACHA")

                val milestones = listOf(7, 14, 30, 50, 100)
                val targetMilestone = milestones.firstOrNull { it > streak } ?: (streak + 10)
                val prevMilestone = milestones.lastOrNull { it <= streak } ?: 0
                val progressRatio = if (targetMilestone > prevMilestone) {
                    ((streak - prevMilestone).toFloat() / (targetMilestone - prevMilestone)).coerceIn(0f, 1f)
                } else 1f

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Objetivo: $targetMilestone días",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$streak / $targetMilestone",
                        color = InquiGold,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Barra gruesa
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .background(InquiDarkBackground, RoundedCornerShape(50))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = progressRatio)
                            .fillMaxHeight()
                            .background(InquiGold, RoundedCornerShape(50))
                    )
                }

                // Hitos en casillas cuadradas
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    milestones.take(4).forEach { milestone ->
                        val reached = streak >= milestone
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (reached) InquiGold else InquiDarkBackground
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "${milestone}d",
                                    color = if (reached) InquiDarkBackground else InquiTextMuted,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // 6. Botón de continuar
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = InquiGold)
            ) {
                Text(
                    text = "Continuar",
                    color = InquiDarkBackground,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D0D0D)
@Composable
fun InquiHomeScreenPreview() {
    MaterialTheme {
        InquiHomeScreenContent(
            uiState = InquiUiState(
                leisureCount = 3,
                effortCount = 1,
                dailyScore = 67.5f,
                currentStreak = 12,
                logs = listOf(
                    ActivityLogItemData(1, "Actividad", "14:30", "Actividad", false),
                    ActivityLogItemData(2, "Actividad+", "12:15", "Actividad+", true)
                )
            ),
            onAddLeisure = {},
            onAddEffort = {},
            onToggleComplete = {},
            onDeleteLog = {},
            onEditLog = { _, _ -> },
            onExpandList = {},
            onNavigateToWorkBlock = {},
            onToggleObjectiveComplete = {},
            onSaveUserName = {}
        )
    }
}
