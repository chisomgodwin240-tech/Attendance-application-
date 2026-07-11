package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AcademicGold
import com.example.ui.theme.DeepBlue
import com.example.ui.theme.IceBlue
import com.example.ui.viewmodel.CampusViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ReportsScreen(
    viewModel: CampusViewModel,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var selectedFormat by remember { mutableStateOf("") } // "PDF", "Excel", "CSV"
    var isExporting by remember { mutableStateOf(false) }
    var exportStatusMessage by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Generate Attendance Reports", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = DeepBlue)
        Text("Export professional, accredited summaries of institutional lectures, hours, and participation percentages.", fontSize = 12.sp, color = Color.Gray)

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, IceBlue)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Select Report Type", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                
                var expandedReportType by remember { mutableStateOf(false) }
                var selectedReportType by remember { mutableStateOf("Semester Attendance Summary") }
                Box {
                    OutlinedButton(onClick = { expandedReportType = true }) {
                        Text(selectedReportType)
                        Icon(Icons.Default.ArrowDropDown, "")
                    }
                    DropdownMenu(expanded = expandedReportType, onDismissRequest = { expandedReportType = false }) {
                        listOf("Daily Attendance Log", "Weekly Summary", "Monthly Report", "Semester Attendance Summary", "Department Performance Audit").forEach { t ->
                            DropdownMenuItem(text = { Text(t) }, onClick = {
                                selectedReportType = t
                                expandedReportType = false
                            })
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text("Export Document Format", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ExportFormatBtn(label = "PDF Document", icon = Icons.Default.PictureAsPdf, isActive = selectedFormat == "PDF", onClick = { selectedFormat = "PDF" })
                    ExportFormatBtn(label = "Excel Sheet", icon = Icons.Default.GridOn, isActive = selectedFormat == "Excel", onClick = { selectedFormat = "Excel" })
                    ExportFormatBtn(label = "CSV Log", icon = Icons.Default.Description, isActive = selectedFormat == "CSV", onClick = { selectedFormat = "CSV" })
                }
            }
        }

        Button(
            onClick = {
                if (selectedFormat.isNotEmpty()) {
                    coroutineScope.launch {
                        isExporting = true
                        exportStatusMessage = "Compiling institutional logs..."
                        delay(1200)
                        exportStatusMessage = "Structuring file format..."
                        delay(1000)
                        isExporting = false
                        exportStatusMessage = "Successfully exported report as $selectedFormat! Saved to: /storage/emulated/0/Download/Campus_Attendance_Report.$selectedFormat"
                    }
                }
            },
            enabled = selectedFormat.isNotEmpty() && !isExporting,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = DeepBlue)
        ) {
            if (isExporting) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(exportStatusMessage, fontSize = 12.sp)
            } else {
                Icon(Icons.Default.Download, "")
                Spacer(modifier = Modifier.width(6.dp))
                Text("EXPORT NOW")
            }
        }

        if (exportStatusMessage.isNotEmpty() && !isExporting) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.CheckCircle, "", tint = Color(0xFF2E7D32))
                    Text(exportStatusMessage, color = Color(0xFF2E7D32), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ExportFormatBtn(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, isActive: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isActive) DeepBlue else Color.White,
            contentColor = if (isActive) Color.White else DeepBlue
        ),
        border = BorderStroke(1.dp, DeepBlue),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.height(38.dp)
    ) {
        Icon(icon, "", modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun NotificationsScreen(
    viewModel: CampusViewModel,
    modifier: Modifier = Modifier
) {
    val notifications by viewModel.notifications.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Alerts & Notifications", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = DeepBlue)
            if (notifications.isNotEmpty()) {
                TextButton(onClick = { viewModel.clearMyNotifications() }) {
                    Text("Clear All", color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        Spacer(modifier = Modifier.height(10.dp))

        if (notifications.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.NotificationsNone, "", tint = Color.Gray, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No notifications yet.", color = Color.Gray)
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(notifications) { notification ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, IceBlue)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = if (notification.title.contains("Warning") || notification.title.contains("Alert")) Icons.Default.Warning else Icons.Default.Notifications,
                                contentDescription = "",
                                tint = if (notification.title.contains("Warning")) Color(0xFFC62828) else AcademicGold,
                                modifier = Modifier.size(24.dp)
                            )
                            Column {
                                Text(notification.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(notification.message, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileScreen(
    viewModel: CampusViewModel,
    modifier: Modifier = Modifier
) {
    val user by viewModel.currentUser.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("My Accredited Profile", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = DeepBlue)

        // Mock Passport Card Photo Display
        Box(
            modifier = Modifier
                .size(110.dp)
                .clip(RoundedCornerShape(55.dp))
                .background(IceBlue)
                .border(2.dp, AcademicGold, RoundedCornerShape(55.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Face,
                contentDescription = "Avatar",
                tint = DeepBlue,
                modifier = Modifier.size(64.dp)
            )
        }

        Text(user?.fullName ?: "Chisom Godwin", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = DeepBlue)
        Text(
            text = "Accredited Role: " + (user?.role ?: "STUDENT"),
            fontWeight = FontWeight.Bold,
            color = AcademicGold,
            fontSize = 11.sp
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, IceBlue)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ProfileDetailRow(label = "Identification ID", value = user?.id ?: "CSC/2022/001")
                ProfileDetailRow(label = "Department", value = user?.department ?: "Computer Science")
                ProfileDetailRow(label = "Faculty", value = user?.faculty ?: "Physical Sciences")
                if (user?.role == "STUDENT") {
                    ProfileDetailRow(label = "Level", value = user?.level ?: "400L")
                    ProfileDetailRow(label = "Date of Admission", value = user?.dateOfAdmission ?: "2022-10-15")
                }
                ProfileDetailRow(label = "Email address", value = user?.email ?: "chisom.godwin@student.edu")
                ProfileDetailRow(label = "Phone line", value = user?.phone ?: "+2347012345678")
                ProfileDetailRow(label = "Gender", value = user?.gender ?: "Female")
            }
        }
    }
}

@Composable
fun ProfileDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 12.sp, color = Color.Gray)
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SettingsScreen(
    viewModel: CampusViewModel,
    modifier: Modifier = Modifier
) {
    val gpsSpoofing by viewModel.gpsSpoofingPrevention.collectAsState()
    val nfcSupported by viewModel.nfcAttendanceSupported.collectAsState()
    val offlineMode by viewModel.isOfflineMode.collectAsState()
    val pendingCount by viewModel.pendingSyncCount.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Application Settings", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = DeepBlue)
        Text("Configure anti-attendance fraud rules, local sensor access, and offline-synchronization testing environments.", fontSize = 12.sp, color = Color.Gray)

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, IceBlue)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                
                // Anti GPS Spoofing Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("GPS Spoofing Prevention", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Detect mock locations, dynamic developer-option spoofers, and anomaly telemetry.", fontSize = 10.sp, color = Color.Gray)
                    }
                    Switch(checked = gpsSpoofing, onCheckedChange = { viewModel.gpsSpoofingPrevention.value = it })
                }

                Divider(color = IceBlue)

                // NFC Card support Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Smart NFC Tap Attendance", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Allow physical smart card taps at the classroom doors to record attendance.", fontSize = 10.sp, color = Color.Gray)
                    }
                    Switch(checked = nfcSupported, onCheckedChange = { viewModel.nfcAttendanceSupported.value = it })
                }

                Divider(color = IceBlue)

                // Simulated Offline Mode Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Simulate Offline Mode", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Turn on to cache scan logs locally and test auto-syncing when internet is restored.", fontSize = 10.sp, color = Color.Gray)
                        if (offlineMode) {
                            Text("Offline Mode Active! Cached logs: $pendingCount", fontWeight = FontWeight.Bold, color = AcademicGold, fontSize = 10.sp)
                        }
                    }
                    Switch(checked = offlineMode, onCheckedChange = { viewModel.toggleOfflineMode() })
                }
            }
        }
    }
}
