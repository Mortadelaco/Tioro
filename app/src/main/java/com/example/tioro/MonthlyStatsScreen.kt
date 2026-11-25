package com.example.tioro

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun MonthlyStatsScreen(
    navController: NavController,
    viewModel: InquiViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    MonthlyStatsContent(
        navController = navController,
        uiState = uiState,
        onNavigateBack = { navController.popBackStack() },
        onNavigateToYearly = { navController.navigate("yearly_stats") }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthlyStatsContent(
    navController: NavController,
    uiState: InquiUiState,
    onNavigateBack: () -> Unit,
    onNavigateToYearly: () -> Unit
) {
    var animationPlayed by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        animationPlayed = true
    }
    val animFactor by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (animationPlayed) 1f else 0f,
        animationSpec = androidx.compose.animation.core.tween(durationMillis = 650, easing = androidx.compose.animation.core.FastOutSlowInEasing)
    )

    val percentageInt = Math.round(uiState.monthlyAverage * animFactor).coerceIn(0, 100)
    val monthlyActivityPoints = uiState.monthlyActivityChartData.map { it * animFactor }
    val effortPct = uiState.monthlyEffortPct * animFactor
    val leisurePct = uiState.monthlyLeisurePct * animFactor
    val projectsPct = uiState.monthlyProjectsPct * animFactor
    
    Scaffold(
        containerColor = InquiDarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Estadísticas Mensuales",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    Box(
                        modifier = Modifier
                            .padding(start = 16.dp, end = 16.dp)
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(InquiSurface)
                            .clickable { onNavigateBack() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Atrás",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = InquiDarkBackground
                )
            )
        },
        bottomBar = { InquiBottomNavigationBar(navController) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            YearlyDataButton(onClick = onNavigateToYearly)

            MonthlyChartSection(monthlyActivityPoints)
            
            MonthlyRadialProgressSection(percentage = percentageInt)
            
            LinearProgressBarsSection(
                effortPct = effortPct,
                leisurePct = leisurePct,
                projectsPct = projectsPct
            )
            
            MedalsSection(medals = uiState.monthlyMedals)
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun MonthlyChartSection(activityPoints: List<Float>) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = InquiSurface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "RENDIMIENTO MENSUAL",
                color = InquiTextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Leyenda
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                LegendItem("Actividades", InquiGold)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Área del gráfico (Canvas)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height

                    // Dibujar 4 líneas horizontales de guía (Grid)
                    val gridLines = 4
                    val gridColor = Color(0xFF2A2A2A)
                    for (i in 0 until gridLines) {
                        val y = height * (i.toFloat() / (gridLines - 1))
                        drawLine(
                            color = gridColor,
                            start = Offset(0f, y),
                            end = Offset(width, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    fun calculatePath(points: List<Float>): Path {
                        val path = Path()
                        val stepX = width / (points.size - 1)
                        points.forEachIndexed { index, value ->
                            val x = index * stepX
                            val y = height - (value * height)
                            if (index == 0) {
                                path.moveTo(x, y)
                            } else {
                                path.lineTo(x, y)
                            }
                        }
                        return path
                    }

                    // Línea Continua (Actividades)
                    val activityPath = calculatePath(activityPoints)
                    drawPath(
                        path = activityPath,
                        color = InquiGold,
                        style = Stroke(
                            width = 2.5.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Etiquetas eje X
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val weeks = listOf("Sem 1", "Sem 2", "Sem 3", "Sem 4")
                weeks.forEach { week ->
                    Text(
                        text = week,
                        color = InquiTextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun MonthlyRadialProgressSection(percentage: Int) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = InquiSurface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Indicador Circular
            Box(
                modifier = Modifier.size(100.dp),
                contentAlignment = Alignment.Center
            ) {
                // Anillo de fondo oscuro
                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.fillMaxSize(),
                    color = InquiDarkBackground,
                    strokeWidth = 8.dp,
                )
                // Anillo de progreso dorado
                CircularProgressIndicator(
                    progress = { percentage / 100f },
                    modifier = Modifier.fillMaxSize(),
                    color = InquiGold,
                    strokeWidth = 8.dp,
                    strokeCap = StrokeCap.Round
                )
                // Texto central
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$percentage%",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Media total",
                        color = InquiTextMuted,
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(24.dp))

            // Texto descriptivo a la derecha
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Objetivos del mes",
                    color = InquiGold,
                    fontSize = 22.sp,
                    fontFamily = FontFamily.Serif,
                    fontStyle = FontStyle.Italic
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Buen rendimiento general. Has superado la mayoría de tus métricas previstas durante las últimas semanas.",
                    color = InquiTextMuted,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
fun MedalsSection(medals: List<Boolean>) {
    val safeMedals = if (medals.size >= 5) medals else List(5) { false }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SectionTitle(title = "MEDALLAS SEMANALES")
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            MedalItem(week = "Sem 1", earned = safeMedals[0])
            MedalItem(week = "Sem 2", earned = safeMedals[1])
            MedalItem(week = "Sem 3", earned = safeMedals[2])
            MedalItem(week = "Sem 4", earned = safeMedals[3])
            MedalItem(week = "Sem 5", earned = safeMedals[4])
        }
    }
}

@Composable
fun MedalItem(week: String, earned: Boolean) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (earned) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .border(2.dp, InquiGold, CircleShape)
                    .background(Color.Transparent, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = "Medalla conseguida",
                    tint = InquiGold,
                    modifier = Modifier.size(24.dp)
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(InquiSurface, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Medalla bloqueada",
                    tint = InquiDarkBackground,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Text(
            text = week,
            color = if (earned) Color.White else InquiTextMuted,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun YearlyDataButton(onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = InquiSurface),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Consultar estadísticas anuales",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Ir",
                tint = InquiGold,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D0D0D)
@Composable
fun MonthlyStatsScreenPreview() {
    MaterialTheme {
        MonthlyStatsContent(
            navController = androidx.navigation.compose.rememberNavController(),
            uiState = InquiUiState(
                monthlyAverage = 68.0f,
                monthlyEffortPct = 0.80f,
                monthlyLeisurePct = 0.65f,
                monthlyProjectsPct = 0.58f,
                monthlyActivityChartData = listOf(0.3f, 0.7f, 0.4f, 0.9f),
                monthlyMedals = listOf(true, true, true, false, false)
            ),
            onNavigateBack = {},
            onNavigateToYearly = {}
        )
    }
}
