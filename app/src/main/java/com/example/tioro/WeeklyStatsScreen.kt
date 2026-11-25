package com.example.tioro

import io.github.mortadelaco.tioro.R
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun WeeklyStatsScreen(
    navController: NavController,
    viewModel: InquiViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    WeeklyStatsContent(
        navController = navController,
        uiState = uiState,
        onNavigateBack = { navController.popBackStack() },
        onNavigateToMonthly = { navController.navigate("monthly_stats") }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeklyStatsContent(
    navController: NavController,
    uiState: InquiUiState,
    onNavigateBack: () -> Unit,
    onNavigateToMonthly: () -> Unit
) {
    var animationPlayed by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        animationPlayed = true
    }
    val animFactor by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (animationPlayed) 1f else 0f,
        animationSpec = androidx.compose.animation.core.tween(durationMillis = 650, easing = androidx.compose.animation.core.FastOutSlowInEasing)
    )

    val percentageInt = Math.round(uiState.weeklyAverage * animFactor).coerceIn(0, 100)

    val activitiesPlusPercent = uiState.weeklyEffortPct * animFactor
    val activitiesPercent = uiState.weeklyLeisurePct * animFactor
    val projectsPercent = uiState.weeklyProjectsPct * animFactor

    val activityPoints = uiState.weeklyActivityChartData.map { it * animFactor }

    Scaffold(
        containerColor = InquiDarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.stats_weekly_title),
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
                            contentDescription = stringResource(R.string.back),
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
            MonthlyDataButton(onClick = onNavigateToMonthly)
            ChartSection(
                activityPoints = activityPoints
            )
            RadialProgressSection(percentageInt)
            LinearProgressBarsSection(
                effortPct = activitiesPlusPercent,
                leisurePct = activitiesPercent,
                projectsPct = projectsPercent
            )
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun ChartSection(activityPoints: List<Float>) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = InquiSurface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = stringResource(R.string.chart_header_weekly),
                color = InquiTextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Leyenda
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                LegendItem(stringResource(R.string.legend_activities), InquiGold)
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

                    // Función auxiliar para calcular coordenadas
                    fun calculatePath(points: List<Float>): Path {
                        val path = Path()
                        val stepX = width / (points.size - 1)
                        points.forEachIndexed { index, value ->
                            // Invertir valor porque la Y=0 es arriba
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
                val daysOfWeek = listOf("L", "M", "X", "J", "V", "S", "D")
                daysOfWeek.forEach { day ->
                    Text(
                        text = day,
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
fun LegendItem(text: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(color, CircleShape)
        )
        Text(text = text, color = InquiTextMuted, fontSize = 11.sp)
    }
}

@Composable
fun RadialProgressSection(percentage: Int) {
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
                        text = stringResource(R.string.label_total_average),
                        color = InquiTextMuted,
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(24.dp))

            // Texto descriptivo a la derecha
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.weekly_radial_title),
                    color = InquiGold,
                    fontSize = 22.sp,
                    fontFamily = FontFamily.Serif,
                    fontStyle = FontStyle.Italic
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.weekly_radial_sub),
                    color = InquiTextMuted,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
fun LinearProgressBarsSection(effortPct: Float, leisurePct: Float, projectsPct: Float) {
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        LinearProgressItem(stringResource(R.string.label_activities_plus), effortPct)
        LinearProgressItem(stringResource(R.string.label_activities), leisurePct)
        LinearProgressItem(stringResource(R.string.label_work_block), projectsPct)
    }
}

@Composable
fun LinearProgressItem(title: String, progress: Float) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${(progress * 100).toInt()}%",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
        // Barra de progreso gruesa
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .background(InquiButtonInner, RoundedCornerShape(50))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = progress.coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .background(InquiGold, RoundedCornerShape(50))
            )
        }
    }
}

@Composable
fun MonthlyDataButton(onClick: () -> Unit) {
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
                text = stringResource(R.string.btn_consult_monthly),
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = stringResource(R.string.continue_text),
                tint = InquiGold,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D0D0D)
@Composable
fun WeeklyStatsScreenPreview() {
    MaterialTheme {
        WeeklyStatsContent(
            navController = androidx.navigation.compose.rememberNavController(),
            uiState = InquiUiState(
                weeklyAverage = 72.0f,
                weeklyActivityChartData = listOf(0.2f, 0.4f, 0.1f, 0.6f, 0.9f, 0.5f, 0.6f),
                weeklyEffortPct = 0.85f,
                weeklyLeisurePct = 0.68f,
                weeklyProjectsPct = 0.63f
            ),
            onNavigateBack = {},
            onNavigateToMonthly = {}
        )
    }
}
