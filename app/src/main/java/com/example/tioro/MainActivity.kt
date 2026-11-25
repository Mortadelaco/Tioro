package com.example.tioro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.tioro.data.local.AppDatabase
import com.example.tioro.data.local.UserPreferencesRepository
import com.example.tioro.data.local.dataStore
import com.example.tioro.ui.theme.TioroTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializamos la base de datos y el DAO
        val db = AppDatabase.getDatabase(applicationContext)
        val dao = db.dailyRecordDao()
        val prefsRepository = UserPreferencesRepository(applicationContext.dataStore)

        // Creamos una fábrica para inyectar las dependencias en el InquiViewModel
        val factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return InquiViewModel(dao, prefsRepository, applicationContext) as T
            }
        }
        val viewModel = ViewModelProvider(this, factory)[InquiViewModel::class.java]

        enableEdgeToEdge()
        setContent {
            TioroTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF0D0D0D)
                ) {
                    val context = androidx.compose.ui.platform.LocalContext.current
                    
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                        val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                            androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
                        ) { isGranted ->
                            if (isGranted) {
                                viewModel.saveNotificationsEnabled(true)
                                TioroNotificationManager.scheduleAllDailyReminders(context)
                            }
                        }
                        androidx.compose.runtime.LaunchedEffect(Unit) {
                            if (androidx.core.content.ContextCompat.checkSelfPermission(
                                    context,
                                    android.Manifest.permission.POST_NOTIFICATIONS
                                ) != android.content.pm.PackageManager.PERMISSION_GRANTED
                            ) {
                                permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                            }
                        }
                    }

                    androidx.compose.runtime.LaunchedEffect(Unit) {
                        TioroNotificationManager.createNotificationChannel(context)
                        TioroNotificationManager.scheduleAllDailyReminders(context)
                    }
                    
                    // Arrancamos el host de navegación principal
                    InquiAppNavigation(viewModel = viewModel)
                }
            }
        }
    }
}
