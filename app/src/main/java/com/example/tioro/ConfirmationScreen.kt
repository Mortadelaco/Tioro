package com.example.tioro

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay

@Composable
fun ConfirmationScreen(
    navController: NavController,
    title: String = "¡Excelente!",
    subtitle: String,
    accentColor: Color = InquiGold
) {
    var startAnim by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (startAnim) 1f else 0.7f,
        animationSpec = tween(durationMillis = 400, easing = androidx.compose.animation.core.FastOutSlowInEasing)
    )
    val alpha by animateFloatAsState(
        targetValue = if (startAnim) 1f else 0f,
        animationSpec = tween(durationMillis = 400)
    )

    LaunchedEffect(Unit) {
        startAnim = true
        delay(1200)
        navController.navigate("home") {
            popUpTo("home") { inclusive = false }
        }
    }

    Scaffold(
        containerColor = InquiDarkBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .scale(scale)
                .alpha(alpha),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Círculo con check
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(Color.Transparent, CircleShape)
                    .border(2.dp, accentColor.copy(alpha = 0.3f), CircleShape)
                    .padding(8.dp)
                    .border(1.dp, accentColor.copy(alpha = 0.6f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Confirmado",
                    tint = accentColor,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Textos
            Text(
                text = title,
                color = Color.White,
                fontSize = 40.sp,
                fontFamily = FontFamily.Serif,
                fontStyle = FontStyle.Italic
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = subtitle,
                color = InquiTextMuted,
                fontSize = 16.sp
            )
        }
    }
}
