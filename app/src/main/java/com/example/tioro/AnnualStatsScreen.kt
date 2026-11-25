package com.example.tioro

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun AnnualStatsScreen(
    navController: NavController,
    viewModel: InquiViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    AnnualStatsContent(
        navController = navController,
        uiState = uiState,
        onNavigateBack = { navController.popBackStack() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnnualStatsContent(
    navController: NavController,
    uiState: InquiUiState,
    onNavigateBack: () -> Unit
) {
    var animationPlayed by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        animationPlayed = true
    }
    val animFactor by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (animationPlayed) 1f else 0f,
        animationSpec = androidx.compose.animation.core.tween(durationMillis = 650, easing = androidx.compose.animation.core.FastOutSlowInEasing)
    )

    val percentageInt = Math.round(uiState.annualAverage * animFactor).coerceIn(0, 100)
    val annualActivityChartData = uiState.annualActivityChartData.map { it * animFactor }
    val effortPct = uiState.annualEffortPct * animFactor
    val leisurePct = uiState.annualLeisurePct * animFactor
    val projectsPct = uiState.annualProjectsPct * animFactor

    Scaffold(
        containerColor = InquiDarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Estadísticas Anuales",
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
                .padding(horizontal = 24.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            AnnualChartSection(
                activityPoints = annualActivityChartData
            )
            
            AnnualSummarySection(
                percentage = percentageInt,
                effortPct = effortPct,
                leisurePct = leisurePct,
                projectsPct = projectsPct
            )
            
            AnnualMedalsSection(medals = uiState.annualMedals)
        }
    }
}

@Composable
fun AnnualChartSection(activityPoints: List<Float>) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = InquiSurface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "RENDIMIENTO ANUAL HISTÓRICO",
                color = InquiTextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Área del gráfico (Canvas)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height

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
                        val safePoints = if (points.size >= 12) points.take(12) else points + List(12 - points.size) { 0f }
                        val stepX = width / (safePoints.size - 1)
                        safePoints.forEachIndexed { index, value ->
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
                val months = listOf("E", "F", "M", "A", "M", "J", "J", "A", "S", "O", "N", "D")
                months.forEach { month ->
                    Text(
                        text = month,
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
fun AnnualSummarySection(percentage: Int, effortPct: Float, leisurePct: Float, projectsPct: Float) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = InquiSurface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Indicador Circular a la izquierda
            Box(
                modifier = Modifier.size(90.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.fillMaxSize(),
                    color = InquiDarkBackground,
                    strokeWidth = 6.dp,
                )
                CircularProgressIndicator(
                    progress = { percentage / 100f },
                    modifier = Modifier.fillMaxSize(),
                    color = InquiGold,
                    strokeWidth = 6.dp,
                    strokeCap = StrokeCap.Round
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$percentage%",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Media",
                        color = InquiTextMuted,
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(20.dp))

            // Barras horizontales a la derecha
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AnnualLinearProgressItem("Actividades+", effortPct)
                AnnualLinearProgressItem("Actividades", leisurePct)
                AnnualLinearProgressItem("Proyectos", projectsPct)
            }
        }
    }
}

@Composable
fun AnnualLinearProgressItem(title: String, progress: Float) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${(progress * 100).toInt()}%",
                color = InquiGold,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .background(InquiDarkBackground, RoundedCornerShape(50))
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
fun AnnualMedalsSection(medals: List<Boolean>) {
    val months = listOf("Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic")
    val safeMedals = if (medals.size >= 12) medals else medals + List(12 - medals.size) { false }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        SectionTitle(title = "MEDALLAS DEL AÑO")
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            itemsIndexed(months) { index, monthName ->
                val earned = safeMedals[index]
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = InquiSurface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = monthName,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        if (earned) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(InquiGold, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EmojiEvents,
                                    contentDescription = "Medalla dorada",
                                    tint = InquiDarkBackground,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        } else {
                            Text(
                                text = "-",
                                color = InquiTextMuted,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D0D0D)
@Composable
fun AnnualStatsScreenPreview() {
    MaterialTheme {
        AnnualStatsContent(
            navController = androidx.navigation.compose.rememberNavController(),
            uiState = InquiUiState(
                annualAverage = 65.0f,
                annualEffortPct = 0.80f,
                annualLeisurePct = 0.65f,
                annualProjectsPct = 0.58f,
                annualActivityChartData = listOf(0.2f, 0.4f, 0.3f, 0.6f, 0.8f, 0.9f, 0.5f, 0.4f, 0.6f, 0.7f, 0.8f, 0.9f),
                annualMedals = listOf(true, true, true, false, false, true, false, false, false, false, false, false)
            ),
            onNavigateBack = {}
        )
    }
}
