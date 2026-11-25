package com.example.tioro

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController? = null,
    viewModel: InquiViewModel? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var showRestoreDialog by remember { mutableStateOf(false) }

    // Launcher para Exportar (.json)
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null && viewModel != null) {
            coroutineScope.launch {
                try {
                    val jsonString = viewModel.exportBackupJson(context)
                    context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                        outputStream.write(jsonString.toByteArray())
                    }
                    Toast.makeText(context, "Copia de seguridad exportada con éxito", Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(context, "Error al exportar la copia", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Launcher para Importar (.json)
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null && viewModel != null) {
            coroutineScope.launch {
                try {
                    val jsonString = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        inputStream.bufferedReader().use { it.readText() }
                    }
                    if (jsonString != null) {
                        val success = viewModel.importBackupJson(context, jsonString)
                        if (success) {
                            Toast.makeText(context, "Copia restaurada correctamente", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, "Archivo de copia no válido", Toast.LENGTH_LONG).show()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(context, "Error al restaurar la copia", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Scaffold(
        containerColor = InquiDarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Configuración",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
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
            
            SettingsGroup(
                title = "PREFERENCIAS",
                items = listOf(
                    SettingsItemData(Icons.Default.Notifications, "Notificaciones", "notifications_screen")
                ),
                onNavigate = { route ->
                    if (route.isNotEmpty()) {
                        navController?.navigate(route)
                    }
                }
            )

            SettingsGroup(
                title = "MANTENIMIENTO",
                items = listOf(
                    SettingsItemData(Icons.Default.Storage, "Backup (Máquina del Tiempo)", "backup_screen")
                ),
                onNavigate = { route ->
                    if (route.isNotEmpty()) {
                        navController?.navigate(route)
                    }
                }
            )

            // FASE 1: Datos y Copia de Seguridad
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "DATOS Y COPIA DE SEGURIDAD",
                    color = InquiTextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = InquiSurface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        SettingsItemRow(
                            item = SettingsItemData(Icons.Default.FileUpload, "Exportar copia local (.json)", ""),
                            onClick = {
                                val defaultFileName = "tioro_backup_${LocalDate.now()}.json"
                                exportLauncher.launch(defaultFileName)
                            }
                        )
                        HorizontalDivider(color = Color(0xFF2A2A2A), thickness = 1.dp)
                        SettingsItemRow(
                            item = SettingsItemData(Icons.Default.FileDownload, "Restaurar copia (.json)", ""),
                            onClick = {
                                showRestoreDialog = true
                            }
                        )
                    }
                }
            }

            SettingsGroup(
                title = "SISTEMA",
                items = listOf(
                    SettingsItemData(Icons.Default.Info, "Acerca de Inqui", "")
                ),
                onNavigate = {}
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (showRestoreDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreDialog = false },
            containerColor = InquiSurface,
            title = { Text("Restaurar Copia de Seguridad", color = Color.White) },
            text = {
                Text(
                    "Esto sustituirá tus datos actuales por los de la copia importada. ¿Deseas continuar?",
                    color = InquiTextMuted,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showRestoreDialog = false
                    importLauncher.launch(arrayOf("application/json"))
                }) {
                    Text("Restaurar", color = InquiGold, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreDialog = false }) {
                    Text("Cancelar", color = InquiTextMuted)
                }
            }
        )
    }
}

data class SettingsItemData(
    val icon: ImageVector,
    val title: String,
    val route: String
)

@Composable
fun SettingsGroup(title: String, items: List<SettingsItemData>, onNavigate: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = title,
            color = InquiTextMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp
        )
        
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = InquiSurface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                items.forEachIndexed { index, item ->
                    SettingsItemRow(item, onClick = { onNavigate(item.route) })
                    if (index < items.size - 1) {
                        HorizontalDivider(color = Color(0xFF2A2A2A), thickness = 1.dp)
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsItemRow(item: SettingsItemData, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(InquiButtonInner),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.title,
                    tint = InquiGold,
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                text = item.title,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = "Ir",
            tint = InquiTextMuted,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D0D0D)
@Composable
fun SettingsScreenPreview() {
    SettingsScreen()
}
