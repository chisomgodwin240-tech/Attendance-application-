package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ClassSessionEntity
import com.example.data.CourseEntity
import com.example.ui.theme.AcademicGold
import com.example.ui.theme.DeepBlue
import com.example.ui.theme.IceBlue
import com.example.ui.viewmodel.CampusViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentDashboardScreen(
    viewModel: CampusViewModel,
    modifier: Modifier = Modifier
) {
    val student by viewModel.currentUser.collectAsState()
    val courses by viewModel.allCourses.collectAsState()
    val sessions by viewModel.allSessions.collectAsState()
    val records by viewModel.allRecords.collectAsState()
    val studentLoc by viewModel.studentLocation.collectAsState()

    var showScannerDialog by remember { mutableStateOf(false) }
    var selectedSessionForScan by remember { mutableStateOf<ClassSessionEntity?>(null) }

    // Filter relevant courses for student (based on student department/level)
    val myCourses = remember(courses, student) {
        courses.filter { it.department == (student?.department ?: "") && it.level == (student?.level ?: "") }
    }

    // Filter relevant sessions for student's courses
    val myCourseCodes = myCourses.map { it.code }.toSet()
    val mySessions = remember(sessions, myCourseCodes) {
        sessions.filter { myCourseCodes.contains(it.courseId) }
    }

    // Filter student records
    val myRecords = remember(records, student) {
        records.filter { it.studentId == (student?.id ?: "") }
    }

    // Compute student aggregates
    val totalLectures = mySessions.size
    val totalAttended = myRecords.count { it.status == "Present" || it.status == "Late" }
    val totalMissed = mySessions.count { s ->
        val record = myRecords.find { r -> r.classSessionId == s.id }
        record == null || record.status == "Absent"
    }
    val averageAttendancePercent = if (totalLectures > 0) (totalAttended.toFloat() / totalLectures) * 100 else 100f

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Banner Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(DeepBlue)
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Student: ${student?.fullName ?: "Chisom Godwin"}",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Matric: ${student?.id ?: "CSC/2022/001"} | ${student?.level ?: "400L"}",
                        color = AcademicGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = "Student Icon",
                    tint = AcademicGold,
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        // Stats Highlights
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StudentStatHighlightCard(label = "Attended", value = "$totalAttended", color = Color(0xFF2E7D32), modifier = Modifier.weight(1f))
            StudentStatHighlightCard(label = "Missed", value = "$totalMissed", color = Color(0xFFC62828), modifier = Modifier.weight(1f))
            StudentStatHighlightCard(label = "Avg Attendance", value = "${averageAttendancePercent.toInt()}%", color = DeepBlue, modifier = Modifier.weight(1.2f))
        }

        // List & Detail Layout
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Exam Eligibility Panel
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, IceBlue)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Examination Eligibility Tracker",
                        fontWeight = FontWeight.Bold,
                        color = DeepBlue,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Attendance threshold is 75%. Below 75% triggers alert and bars examination eligibility.",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    myCourses.forEach { course ->
                        val courseSessionsCount = mySessions.count { it.courseId == course.code }
                        val courseAttendedCount = myRecords.count { r ->
                            val s = mySessions.find { it.id == r.classSessionId }
                            s?.courseId == course.code && (r.status == "Present" || r.status == "Late")
                        }
                        val rate = if (courseSessionsCount > 0) (courseAttendedCount.toFloat() / courseSessionsCount) * 100 else 100f
                        val eligible = rate >= course.requiredAttendancePercent

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(course.code, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("${courseAttendedCount}/${courseSessionsCount} lectures logged", fontSize = 11.sp, color = Color.Gray)
                            }
                            
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("${rate.toInt()}%", fontWeight = FontWeight.Bold, color = if (eligible) Color(0xFF2E7D32) else Color(0xFFC62828), fontSize = 14.sp)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (eligible) Color(0xFFE8F5E9) else Color(0xFFFFEBEE))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = if (eligible) "ELIGIBLE" else "NOT ELIGIBLE",
                                        color = if (eligible) Color(0xFF2E7D32) else Color(0xFFC62828),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // QR Attendance Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Scheduled Class Sessions", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = DeepBlue)
                
                // NFC Tap Emulator button
                IconButton(
                    onClick = {
                        val activeSession = mySessions.firstOrNull()
                        if (activeSession != null) {
                            viewModel.simulateNFCCardTap(activeSession.id)
                        } else {
                            viewModel.updateStudentLocation(6.2543, 5.6123) // Ensure default GPS is set
                        }
                    },
                    modifier = Modifier.background(IceBlue, RoundedCornerShape(8.dp))
                ) {
                    Icon(Icons.Default.Nfc, contentDescription = "Simulate NFC Card Tap", tint = DeepBlue)
                }
            }

            if (mySessions.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("No scheduled sessions found for your classes.", color = Color.Gray, fontSize = 13.sp)
                    }
                }
            } else {
                mySessions.forEach { session ->
                    val record = myRecords.find { it.classSessionId == session.id }
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, IceBlue)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(session.courseId, fontWeight = FontWeight.Bold, color = DeepBlue)
                                    Text(session.locationName, fontSize = 12.sp, color = Color.Gray)
                                }
                                if (record != null) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(
                                                when (record.status) {
                                                    "Present" -> Color(0xFFE8F5E9)
                                                    "Late" -> Color(0xFFFFF3E0)
                                                    "Absent" -> Color(0xFFFFEBEE)
                                                    else -> Color(0xFFE3F2FD)
                                                }
                                            )
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = record.status.uppercase(),
                                            color = when (record.status) {
                                                "Present" -> Color(0xFF2E7D32)
                                                "Late" -> Color(0xFFEF6C00)
                                                "Absent" -> Color(0xFFC62828)
                                                else -> Color(0xFF1565C0)
                                            },
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                } else {
                                    Button(
                                        onClick = {
                                            selectedSessionForScan = session
                                            showScannerDialog = true
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = DeepBlue),
                                        modifier = Modifier.height(34.dp)
                                    ) {
                                        Icon(Icons.Default.QrCodeScanner, "", modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Scan QR", fontSize = 11.sp)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Window: ${session.attendanceWindowOpen} - ${session.attendanceWindowClose}", fontSize = 11.sp, color = Color.Gray)
                                if (record != null && record.arrivalTime != "--") {
                                    Text("Logged at: ${record.arrivalTime}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DeepBlue)
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // QR & GPS SCANNER SIMULATION DIALOG
    if (showScannerDialog && selectedSessionForScan != null) {
        val activeSession = selectedSessionForScan!!
        var isGpsInsideClassroom by remember { mutableStateOf(true) }

        // Adjust coordinates based on simulator toggle
        val simLat = if (isGpsInsideClassroom) activeSession.latitude else 6.3000 // Inside class vs too far away (approx 5km)
        val simLng = if (isGpsInsideClassroom) activeSession.longitude else 5.7000

        AlertDialog(
            onDismissRequest = { showScannerDialog = false },
            title = {
                Text(
                    text = "Accredited Scanner Simulator",
                    fontWeight = FontWeight.Bold,
                    color = DeepBlue
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "This screen simulates using the hardware camera to decode the lecturer's dynamic QR code and matches it with your active GPS coordinates.",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )

                    // Large camera/scanner simulation box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .background(Color.Black, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        // Dynamic crosshairs
                        Canvas(modifier = Modifier.size(100.dp)) {
                            val w = size.width
                            val h = size.height
                            // Draw corner crosshairs
                            drawLine(color = AcademicGold, start = androidx.compose.ui.geometry.Offset(0f, 0f), end = androidx.compose.ui.geometry.Offset(20f, 0f), strokeWidth = 4f)
                            drawLine(color = AcademicGold, start = androidx.compose.ui.geometry.Offset(0f, 0f), end = androidx.compose.ui.geometry.Offset(0f, 20f), strokeWidth = 4f)
                            
                            drawLine(color = AcademicGold, start = androidx.compose.ui.geometry.Offset(w, 0f), end = androidx.compose.ui.geometry.Offset(w - 20f, 0f), strokeWidth = 4f)
                            drawLine(color = AcademicGold, start = androidx.compose.ui.geometry.Offset(w, 0f), end = androidx.compose.ui.geometry.Offset(w, 20f), strokeWidth = 4f)

                            drawLine(color = AcademicGold, start = androidx.compose.ui.geometry.Offset(0f, h), end = androidx.compose.ui.geometry.Offset(20f, h), strokeWidth = 4f)
                            drawLine(color = AcademicGold, start = androidx.compose.ui.geometry.Offset(0f, h), end = androidx.compose.ui.geometry.Offset(0f, h - 20f), strokeWidth = 4f)

                            drawLine(color = AcademicGold, start = androidx.compose.ui.geometry.Offset(w, h), end = androidx.compose.ui.geometry.Offset(w - 20f, h), strokeWidth = 4f)
                            drawLine(color = AcademicGold, start = androidx.compose.ui.geometry.Offset(w, h), end = androidx.compose.ui.geometry.Offset(w, h - 20f), strokeWidth = 4f)
                        }
                        
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.QrCodeScanner, "", tint = Color.White, modifier = Modifier.size(40.dp))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("QR Code Frame Aligned", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // STUDENT GPS CONFIGURATION INTERACTIVE SIMULATOR (CRITICAL FOR REVIEWING THE ATTENDANCE RULE)
                    Card(
                        colors = CardDefaults.cardColors(containerColor = IceBlue),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Interactive Location Simulator:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = DeepBlue)
                            Text("Use this to test GPS Verification (50m Radius limit).", fontSize = 10.sp, color = Color.Gray)
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { isGpsInsideClassroom = true },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isGpsInsideClassroom) DeepBlue else Color.White,
                                        contentColor = if (isGpsInsideClassroom) Color.White else DeepBlue
                                    ),
                                    border = BorderStroke(1.dp, DeepBlue),
                                    modifier = Modifier.weight(1f).height(36.dp),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text("Inside Class", fontSize = 10.sp)
                                }
                                Button(
                                    onClick = { isGpsInsideClassroom = false },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (!isGpsInsideClassroom) DeepBlue else Color.White,
                                        contentColor = if (!isGpsInsideClassroom) Color.White else DeepBlue
                                    ),
                                    border = BorderStroke(1.dp, DeepBlue),
                                    modifier = Modifier.weight(1f).height(36.dp),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text("Outside Class (Fail)", fontSize = 10.sp)
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (isGpsInsideClassroom) "Simulated distance: ~2 meters (GPS Lock Valid)" else "Simulated distance: ~4500 meters (GPS Lock Blocked)",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isGpsInsideClassroom) Color(0xFF2E7D32) else Color(0xFFC62828),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        // Apply simulated GPS coordinates to viewmodel, then perform scan
                        viewModel.updateStudentLocation(simLat, simLng)
                        viewModel.recordStudentAttendanceViaQR(activeSession.qrCodeToken, activeSession.id)
                        showScannerDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DeepBlue)
                ) {
                    Text("SIMULATE SCAN")
                }
            },
            dismissButton = {
                TextButton(onClick = { showScannerDialog = false }) {
                    Text("CANCEL")
                }
            }
        )
    }
}

@Composable
fun StudentStatHighlightCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(76.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = IceBlue)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, fontSize = 11.sp, color = color, fontWeight = FontWeight.Bold)
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}
