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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ClassSessionEntity
import com.example.data.CourseEntity
import com.example.ui.theme.AcademicGold
import com.example.ui.theme.DeepBlue
import com.example.ui.theme.IceBlue
import com.example.ui.viewmodel.CampusViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LecturerDashboardScreen(
    viewModel: CampusViewModel,
    modifier: Modifier = Modifier
) {
    val courses by viewModel.allCourses.collectAsState()
    val sessions by viewModel.allSessions.collectAsState()
    val records by viewModel.allRecords.collectAsState()
    val students by viewModel.students.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    // Filter sessions and courses taught by this lecturer
    val lecturerCourses = remember(courses, currentUser) {
        courses.filter { it.lecturerId == (currentUser?.id ?: "") }
    }
    val lecturerSessions = remember(sessions, lecturerCourses) {
        val courseCodes = lecturerCourses.map { it.code }.toSet()
        sessions.filter { courseCodes.contains(it.courseId) }
    }

    var selectedSessionForDetails by remember { mutableStateOf<ClassSessionEntity?>(null) }
    var showCreateSessionDialog by remember { mutableStateOf(false) }

    // Session form values
    var sessionCourse by remember { mutableStateOf("") }
    var sessionLocation by remember { mutableStateOf("ETF Hall 1") }
    var sessionDate by remember { mutableStateOf("") }
    var sessionStart by remember { mutableStateOf("09:00 AM") }
    var sessionEnd by remember { mutableStateOf("11:00 AM") }
    var sessionWindowOpen by remember { mutableStateOf("08:45 AM") }
    var sessionWindowClose by remember { mutableStateOf("09:20 AM") }

    if (sessionDate.isEmpty()) {
        sessionDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Banner
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
                        text = currentUser?.fullName ?: "Lecturer",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Department of ${currentUser?.department ?: "Engineering"}",
                        color = AcademicGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Icon(
                    imageVector = Icons.Default.CoPresent,
                    contentDescription = "Lecturer",
                    tint = AcademicGold,
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        if (selectedSessionForDetails == null) {
            // Main Overview Scroll
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Headline Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("My Class Sessions", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DeepBlue)
                    Button(
                        onClick = {
                            if (lecturerCourses.isNotEmpty()) {
                                sessionCourse = lecturerCourses.first().code
                            }
                            showCreateSessionDialog = true
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DeepBlue)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Schedule Session", fontSize = 12.sp)
                    }
                }

                if (lecturerSessions.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.EventBusy, "", tint = Color.Gray, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("No sessions scheduled yet.", color = Color.Gray, fontSize = 14.sp)
                        }
                    }
                } else {
                    lecturerSessions.forEach { session ->
                        LecturerSessionCard(
                            session = session,
                            records = records,
                            onManageClick = { selectedSessionForDetails = session }
                        )
                    }
                }

                // Stats Section
                Text("Course Attendance Performance", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DeepBlue)
                lecturerCourses.forEach { course ->
                    val courseSess = lecturerSessions.filter { it.courseId == course.code }
                    val totalEligible = students.count { it.department == course.department }
                    val expectedLogs = courseSess.size * totalEligible
                    val actualLogs = records.filter { r -> courseSess.any { s -> s.id == r.classSessionId } && r.status != "Absent" }
                    val attendancePercent = if (expectedLogs > 0) (actualLogs.size.toFloat() / expectedLogs) * 100 else 100f

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, IceBlue)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(course.code, fontWeight = FontWeight.Bold, color = DeepBlue)
                                Text(course.title, fontSize = 12.sp, color = Color.Gray)
                                Text("Required minimum: ${course.requiredAttendancePercent}%", fontSize = 11.sp, color = AcademicGold)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("${attendancePercent.toInt()}%", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = DeepBlue)
                                Text("Average Attendance", fontSize = 10.sp, color = Color.Gray)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        } else {
            // Manage Selected Session (Details and logs)
            val activeSession = selectedSessionForDetails!!
            val sessionRecords = records.filter { it.classSessionId == activeSession.id }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { selectedSessionForDetails = null }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                    Text(
                        text = "Manage ${activeSession.courseId}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = DeepBlue
                    )
                }

                // Dynamic QR Generator display mockup
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = IceBlue)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "ACTIVE SECURE QR CODE",
                            fontWeight = FontWeight.Bold,
                            color = DeepBlue,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Students scan this QR to track real-time attendance",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        // Beautiful mock QR canvas drawing
                        Canvas(
                            modifier = Modifier
                                .size(140.dp)
                                .background(Color.White, RoundedCornerShape(8.dp))
                        ) {
                            // Simple abstract blocks representing a QR code
                            val sizePx = size.width
                            val step = sizePx / 7f
                            for (row in 0..6) {
                                for (col in 0..6) {
                                    // Make corners solid anchor marks
                                    val isAnchor = (row < 2 && col < 2) || (row > 4 && col < 2) || (row < 2 && col > 4)
                                    val randFill = (row + col) % 2 == 0 && (row * col) % 3 != 0
                                    if (isAnchor || randFill) {
                                        drawRect(
                                            color = DeepBlue,
                                            topLeft = androidx.compose.ui.geometry.Offset(col * step, row * step),
                                            size = androidx.compose.ui.geometry.Size(step, step)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "TOKEN: ${activeSession.qrCodeToken}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = DeepBlue
                        )
                    }
                }

                // GPS Settings info for this classroom
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PinDrop, "", tint = AcademicGold, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Classroom GPS Target: ${activeSession.locationName}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Text("Lat: ${activeSession.latitude} | Lng: ${activeSession.longitude} | Radius limit: ${activeSession.allowedRadiusMeters.toInt()} meters", fontSize = 11.sp, color = Color.Gray)
                        Text("Attendance window: ${activeSession.attendanceWindowOpen} to ${activeSession.attendanceWindowClose}", fontSize = 11.sp, color = Color.Gray)
                    }
                }

                // List of student submissions
                Text("Scanned Attendance Records (${sessionRecords.size})", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = DeepBlue)

                if (sessionRecords.isEmpty()) {
                    Text("No scans recorded yet.", color = Color.Gray, fontSize = 13.sp)
                } else {
                    sessionRecords.forEach { record ->
                        val studName = students.find { it.id == record.studentId }?.fullName ?: record.studentId
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, IceBlue)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(studName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("Matric: ${record.studentId} | Scanned: ${record.arrivalTime}", fontSize = 11.sp, color = Color.Gray)
                                    if (record.minutesLate > 0) {
                                        Text("${record.minutesLate} minutes late", color = Color.Red, fontSize = 10.sp)
                                    }
                                }
                                
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                    // Status Badge clickable to change manually (Approve late attendance!)
                                    var showStatusMenu by remember { mutableStateOf(false) }
                                    Box {
                                        AssistChip(
                                            onClick = { showStatusMenu = true },
                                            label = { Text(record.status, fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                                            colors = AssistChipDefaults.assistChipColors(
                                                labelColor = when (record.status) {
                                                    "Present" -> Color(0xFF2E7D32)
                                                    "Late" -> Color(0xFFEF6C00)
                                                    "Absent" -> Color(0xFFC62828)
                                                    else -> Color(0xFF1565C0)
                                                }
                                            )
                                        )
                                        DropdownMenu(expanded = showStatusMenu, onDismissRequest = { showStatusMenu = false }) {
                                            listOf("Present", "Late", "Absent", "Excused").forEach { st ->
                                                DropdownMenuItem(
                                                    text = { Text(st) },
                                                    onClick = {
                                                        viewModel.approveLateOrManualAttendance(record.studentId, record.classSessionId, st)
                                                        showStatusMenu = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // CREATE SESSION SCHEDULE DIALOG
    if (showCreateSessionDialog) {
        AlertDialog(
            onDismissRequest = { showCreateSessionDialog = false },
            title = { Text("Schedule Class Session") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Course Select
                    Text("Select Course Code:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    var expandedCourseSelect by remember { mutableStateOf(false) }
                    Box {
                        OutlinedButton(onClick = { expandedCourseSelect = true }) {
                            Text(sessionCourse.ifEmpty { "Select Course" })
                            Icon(Icons.Default.ArrowDropDown, "")
                        }
                        DropdownMenu(expanded = expandedCourseSelect, onDismissRequest = { expandedCourseSelect = false }) {
                            lecturerCourses.forEach { c ->
                                DropdownMenuItem(
                                    text = { Text(c.code) },
                                    onClick = {
                                        sessionCourse = c.code
                                        expandedCourseSelect = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = sessionLocation,
                        onValueChange = { sessionLocation = it },
                        label = { Text("Classroom Location (e.g. Hall A)") },
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = sessionDate,
                        onValueChange = { sessionDate = it },
                        label = { Text("Session Date (YYYY-MM-DD)") },
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = sessionStart,
                        onValueChange = { sessionStart = it },
                        label = { Text("Start Time (e.g. 09:00 AM)") },
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = sessionEnd,
                        onValueChange = { sessionEnd = it },
                        label = { Text("End Time (e.g. 11:00 AM)") },
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = sessionWindowOpen,
                        onValueChange = { sessionWindowOpen = it },
                        label = { Text("Attendance Window Open Time") },
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = sessionWindowClose,
                        onValueChange = { sessionWindowClose = it },
                        label = { Text("Attendance Window Close Time") },
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (sessionCourse.isNotBlank() && sessionLocation.isNotBlank() && sessionStart.isNotBlank()) {
                            viewModel.createClassSession(
                                courseId = sessionCourse,
                                date = sessionDate,
                                startTime = sessionStart,
                                endTime = sessionEnd,
                                location = sessionLocation,
                                lat = 6.2543, // Target lat matches student simulator
                                lng = 5.6123, // Target lng
                                radius = 50.0, // allowed radius 50 meters
                                windowOpen = sessionWindowOpen,
                                windowClose = sessionWindowClose
                            )
                            showCreateSessionDialog = false
                        }
                    }
                ) {
                    Text("OPEN CLASS SESSION")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateSessionDialog = false }) {
                    Text("CANCEL")
                }
            }
        )
    }
}

@Composable
fun LecturerSessionCard(
    session: ClassSessionEntity,
    records: List<com.example.data.AttendanceRecordEntity>,
    onManageClick: () -> Unit
) {
    val attendeeCount = records.count { it.classSessionId == session.id }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, IceBlue)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(session.courseId, fontWeight = FontWeight.Bold, color = DeepBlue, fontSize = 16.sp)
                    Text(session.locationName, fontSize = 12.sp, color = Color.Gray)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(AcademicGold.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text("$attendeeCount Checked-in", color = DeepBlue, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = IceBlue)
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Date: ${session.date}", fontSize = 11.sp, color = Color.Gray)
                    Text("Time: ${session.startTime} - ${session.endTime}", fontSize = 11.sp, color = Color.Gray)
                }
                Button(
                    onClick = onManageClick,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DeepBlue),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text("Manage Session", fontSize = 11.sp)
                }
            }
        }
    }
}
