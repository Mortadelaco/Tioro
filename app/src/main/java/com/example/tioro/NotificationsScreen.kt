package com.example.tioro

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    navController: NavController,
    viewModel: InquiViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    // We assume notifications are enabled if permission is granted AND user preference is true.
    // In a real app we'd fetch the exact permission state or assume it from preference.
    var notificationsEnabled by remember { mutableStateOf(uiState.notificationsEnabled) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            notificationsEnabled = true
            viewModel.saveNotificationsEnabled(true)
            TioroNotificationManager.scheduleAllDailyReminders(context)
        } else {
            notificationsEnabled = false
            viewModel.saveNotificationsEnabled(false)
        }
    }

    Scaffold(
        containerColor = InquiDarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Notificaciones",
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
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Recordatorios Inteligentes",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Recibe avisos para mantener tu racha si tu lista está vacía.",
                            color = InquiTextMuted,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = { isChecked ->
                            if (isChecked) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    notificationsEnabled = true
                                    viewModel.saveNotificationsEnabled(true)
                                    TioroNotificationManager.scheduleAllDailyReminders(context)
                                }
                            } else {
                                notificationsEnabled = false
                                viewModel.saveNotificationsEnabled(false)
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = InquiDarkBackground,
                            checkedTrackColor = InquiGold,
                            uncheckedThumbColor = InquiTextMuted,
                            uncheckedTrackColor = InquiDarkBackground,
                            uncheckedBorderColor = InquiTextMuted
                        )
                    )
                }
            }

            if (notificationsEnabled) {
                Button(
                    onClick = {
                        TioroNotificationManager.showNotification(
                            context,
                            "¡Hola desde Tioro!",
                            "Las notificaciones funcionan correctamente."
                        )
                    },
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    colors = ButtonDefaults.buttonColors(containerColor = InquiButtonInner)
                ) {
                    Text("Probar notificación ahora", color = InquiGold)
                }
            }
        }
    }
}
