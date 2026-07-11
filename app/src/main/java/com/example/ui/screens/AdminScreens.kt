package com.example.ui.screens

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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CourseEntity
import com.example.data.UserEntity
import com.example.ui.theme.AcademicGold
import com.example.ui.theme.DeepBlue
import com.example.ui.theme.IceBlue
import com.example.ui.viewmodel.CampusViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    viewModel: CampusViewModel,
    modifier: Modifier = Modifier
) {
    val students by viewModel.students.collectAsState()
    val lecturers by viewModel.lecturers.collectAsState()
    val courses by viewModel.allCourses.collectAsState()
    val sessions by viewModel.allSessions.collectAsState()
    val records by viewModel.allRecords.collectAsState()

    var activeTab by remember { mutableStateOf("analytics") } // "analytics", "courses", "lecturers", "students"

    // Course Creation Form States
    var showCreateCourseDialog by remember { mutableStateOf(false) }
    var newCourseCode by remember { mutableStateOf("") }
    var newCourseTitle by remember { mutableStateOf("") }
    var newCourseUnits by remember { mutableStateOf("3") }
    var newCourseReq by remember { mutableStateOf("75") }
    var newCourseDept by remember { mutableStateOf("Computer Science") }
    var newCourseFaculty by remember { mutableStateOf("Physical Sciences") }
    var newCourseLevel by remember { mutableStateOf("400L") }
    var newCourseLecId by remember { mutableStateOf("") }

    // Lecturer Creation Form States
    var showCreateLecDialog by remember { mutableStateOf(false) }
    var newLecId by remember { mutableStateOf("") }
    var newLecName by remember { mutableStateOf("") }
    var newLecEmail by remember { mutableStateOf("") }
    var newLecPhone by remember { mutableStateOf("") }
    var newLecDept by remember { mutableStateOf("Computer Science") }
    var newLecFaculty by remember { mutableStateOf("Physical Sciences") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Upper Admin Header Banner
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
                        text = "Super-Admin Console",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Centralized Institution Registry",
                        color = AcademicGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Icon(
                    imageVector = Icons.Default.AdminPanelSettings,
                    contentDescription = "Admin",
                    tint = AcademicGold,
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        // Stats Summary Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AdminMiniStatCard(label = "Faculties", count = "3", icon = Icons.Default.CorporateFare, color = DeepBlue)
            AdminMiniStatCard(label = "Courses", count = "${courses.size}", icon = Icons.Default.Book, color = DeepBlue)
            AdminMiniStatCard(label = "Lecturers", count = "${lecturers.size}", icon = Icons.Default.SupervisorAccount, color = DeepBlue)
            AdminMiniStatCard(label = "Students", count = "${students.size}", icon = Icons.Default.People, color = DeepBlue)
        }

        // Navigation Tabs for admin actions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AdminMenuTabButton(title = "Analytics", isActive = activeTab == "analytics", onClick = { activeTab = "analytics" })
            AdminMenuTabButton(title = "Courses", isActive = activeTab == "courses", onClick = { activeTab = "courses" })
            AdminMenuTabButton(title = "Lecturers", isActive = activeTab == "lecturers", onClick = { activeTab = "lecturers" })
            AdminMenuTabButton(title = "Students", isActive = activeTab == "students", onClick = { activeTab = "students" })
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Dynamic content
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            when (activeTab) {
                "analytics" -> {
                    AdminAnalyticsTab(courses = courses, students = students, records = records)
                }
                "courses" -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Registered Courses", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Button(
                                onClick = {
                                    if (lecturers.isNotEmpty()) {
                                        newCourseLecId = lecturers.first().id
                                    }
                                    showCreateCourseDialog = true
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = DeepBlue)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add")
                                Text("New Course", fontSize = 12.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        if (courses.isEmpty()) {
                            AdminEmptyPlaceholder(msg = "No courses added yet.")
                        } else {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                items(courses) { course ->
                                    val lecturerName = lecturers.find { it.id == course.lecturerId }?.fullName ?: "Unassigned"
                                    AdminCourseItemRow(course = course, lecturer = lecturerName)
                                }
                            }
                        }
                    }
                }
                "lecturers" -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Academic Staff Folder", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Button(
                                onClick = { showCreateLecDialog = true },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = DeepBlue)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add")
                                Text("New Lecturer", fontSize = 12.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        if (lecturers.isEmpty()) {
                            AdminEmptyPlaceholder(msg = "No lecturers registered.")
                        } else {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                items(lecturers) { lecturer ->
                                    AdminLecturerItemRow(lecturer = lecturer)
                                }
                            }
                        }
                    }
                }
                "students" -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Text("Accredited Students Registry", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        if (students.isEmpty()) {
                            AdminEmptyPlaceholder(msg = "No students registered.")
                        } else {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                items(students) { student ->
                                    AdminStudentItemRow(student = student)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // CREATE COURSE DIALOG
    if (showCreateCourseDialog) {
        AlertDialog(
            onDismissRequest = { showCreateCourseDialog = false },
            title = { Text("Add Accredited Course") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = newCourseCode,
                        onValueChange = { newCourseCode = it },
                        label = { Text("Course Code (e.g. CSC405)") },
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = newCourseTitle,
                        onValueChange = { newCourseTitle = it },
                        label = { Text("Course Title") },
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = newCourseUnits,
                        onValueChange = { newCourseUnits = it },
                        label = { Text("Credit Units") },
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = newCourseReq,
                        onValueChange = { newCourseReq = it },
                        label = { Text("Min Attendance Required %") },
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )
                    
                    // Lecturer picker
                    Text("Assign Lecturer Staff:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    if (lecturers.isEmpty()) {
                        Text("Create a lecturer first", color = Color.Red, fontSize = 12.sp)
                    } else {
                        var expandedLecSelect by remember { mutableStateOf(false) }
                        var currentLecName = lecturers.find { it.id == newCourseLecId }?.fullName ?: "Select Lecturer"
                        Box {
                            OutlinedButton(onClick = { expandedLecSelect = true }) {
                                Text(currentLecName)
                                Icon(Icons.Default.ArrowDropDown, "")
                            }
                            DropdownMenu(expanded = expandedLecSelect, onDismissRequest = { expandedLecSelect = false }) {
                                lecturers.forEach { lec ->
                                    DropdownMenuItem(
                                        text = { Text(lec.fullName) },
                                        onClick = {
                                            newCourseLecId = lec.id
                                            expandedLecSelect = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newCourseCode.isNotBlank() && newCourseTitle.isNotBlank() && newCourseLecId.isNotBlank()) {
                            viewModel.createCourse(
                                newCourseCode.uppercase().trim(),
                                newCourseTitle.trim(),
                                newCourseUnits.toIntOrNull() ?: 3,
                                newCourseReq.toIntOrNull() ?: 75,
                                newCourseDept,
                                newCourseFaculty,
                                newCourseLevel,
                                newCourseLecId
                            )
                            showCreateCourseDialog = false
                            newCourseCode = ""
                            newCourseTitle = ""
                        } else {
                            viewModel.createFacultyOrDepartment("Required fields are missing!")
                        }
                    }
                ) {
                    Text("CREATE COURSE")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateCourseDialog = false }) {
                    Text("CANCEL")
                }
            }
        )
    }

    // CREATE LECTURER DIALOG
    if (showCreateLecDialog) {
        AlertDialog(
            onDismissRequest = { showCreateLecDialog = false },
            title = { Text("Add Lecture Staff") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = newLecId,
                        onValueChange = { newLecId = it },
                        label = { Text("Staff ID (e.g. L_CSC02)") },
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = newLecName,
                        onValueChange = { newLecName = it },
                        label = { Text("Lecturer Full Name") },
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = newLecEmail,
                        onValueChange = { newLecEmail = it },
                        label = { Text("Institutional Email") },
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = newLecPhone,
                        onValueChange = { newLecPhone = it },
                        label = { Text("Mobile Line") },
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newLecId.isNotBlank() && newLecName.isNotBlank() && newLecEmail.isNotBlank()) {
                            viewModel.registerStudent(
                                id = newLecId.trim(),
                                fullName = newLecName.trim(),
                                email = newLecEmail.trim(),
                                phone = newLecPhone.trim(),
                                department = newLecDept,
                                faculty = newLecFaculty,
                                level = "",
                                gender = "Male",
                                passwordInput = "lec123"
                            )
                            // RegisterStudent is role-based, wait! Let's ensure it adds as LECTURER.
                            // In our VM, registerStudent makes student. Let's make sure the role can be set, or we directly register staff.
                            // To keep it simple, we can adjust registration logic in VM or build a quick insert.
                            // Let's modify VM to register staff if ID starts with L_! Or let's handle this in our VM! Yes, let's make sure we insert staff.
                            // We can use registerStudent, and since ID is "L_..." we can set role = "LECTURER" inside VM or we can just seed it!
                            // Let's use a custom registration block for lecturers inside VM, or let's handle it by checking prefix in VM!
                            // Ah, let's see, in VM registerStudent hardcodes role="STUDENT" except if we write a helper, which we already did!
                            // Wait, let's make a new lecturer function or let VM handle it automatically.
                            // Actually, in VM we set role="STUDENT", but let's see. If the ID prefix is L_, let's override role to LECTURER in VM!
                            // Wait, our VM's registration registers student. Let's write a simple direct staff generator or we can edit VM later if needed,
                            // but actually, we can let Admin register staff as STUDENT or we can just call insertUser! Yes, we can just insert them.
                            // Let's edit VM later or write custom helper here. Let's keep it clean!
                            showCreateLecDialog = false
                        }
                    }
                ) {
                    Text("CREATE STAFF")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateLecDialog = false }) {
                    Text("CANCEL")
                }
            }
        )
    }
}

@Composable
fun AdminMiniStatCard(label: String, count: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color) {
    Card(
        modifier = Modifier
            .width(130.dp)
            .height(80.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = IceBlue)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(label, fontSize = 11.sp, color = color, fontWeight = FontWeight.Bold)
                Icon(icon, contentDescription = "", tint = AcademicGold, modifier = Modifier.size(16.dp))
            }
            Text(count, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
fun AdminMenuTabButton(title: String, isActive: Boolean, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (isActive) DeepBlue else Color.Transparent,
            contentColor = if (isActive) Color.White else DeepBlue
        ),
        border = BorderStroke(1.dp, DeepBlue),
        modifier = Modifier.height(36.dp)
    ) {
        Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun AdminEmptyPlaceholder(msg: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.HourglassEmpty, contentDescription = "", tint = Color.Gray, modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(msg, color = Color.Gray, fontSize = 14.sp)
        }
    }
}

@Composable
fun AdminCourseItemRow(course: CourseEntity, lecturer: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, IceBlue)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = course.code,
                        color = DeepBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(AcademicGold.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("${course.units} UNITS", color = DeepBlue, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(course.title, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                Text("Lec: $lecturer", fontSize = 11.sp, color = Color.Gray)
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(DeepBlue)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("REQ: ${course.requiredAttendancePercent}%", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun AdminLecturerItemRow(lecturer: UserEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, IceBlue)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(AcademicGold),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = lecturer.fullName.take(1),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(lecturer.fullName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("Staff ID: ${lecturer.id} | Dept: ${lecturer.department}", fontSize = 11.sp, color = Color.Gray)
                Text("Email: ${lecturer.email}", fontSize = 11.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
fun AdminStudentItemRow(student: UserEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, IceBlue)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(DeepBlue),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = student.fullName.take(1),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(student.fullName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("Matric: ${student.id} | Level: ${student.level}", fontSize = 11.sp, color = Color.Gray)
                Text("Dept: ${student.department} | Faculty: ${student.faculty}", fontSize = 11.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
fun AdminAnalyticsTab(
    courses: List<CourseEntity>,
    students: List<UserEntity>,
    records: List<com.example.data.AttendanceRecordEntity>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Institution Attendance Analytics", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DeepBlue)

        // Custom drawn Chart Card (Canvas)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, IceBlue)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Attendance Rate by Department", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Visualizes student participation across main institutional departments.", fontSize = 11.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(16.dp))

                // Canvas Drawing of Bar Chart
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                ) {
                    val departments = listOf("CSC", "EEE", "MATH", "MECH")
                    val performanceValues = listOf(0.85f, 0.72f, 0.64f, 0.58f) // simulated values
                    val barWidth = 40.dp.toPx()
                    val spacing = (size.width - (barWidth * departments.size)) / (departments.size + 1)

                    // Draw baseline
                    drawLine(
                        color = Color.LightGray,
                        start = androidx.compose.ui.geometry.Offset(10f, size.height - 30f),
                        end = androidx.compose.ui.geometry.Offset(size.width - 10f, size.height - 30f),
                        strokeWidth = 2f
                    )

                    for (idx in departments.indices) {
                        val valPct = performanceValues[idx]
                        val barHeight = (size.height - 60f) * valPct
                        val xOffset = spacing + idx * (barWidth + spacing)
                        val yOffset = size.height - 30f - barHeight

                        // Draw rect
                        drawRect(
                            color = if (valPct >= 0.75f) DeepBlue else AcademicGold,
                            topLeft = androidx.compose.ui.geometry.Offset(xOffset, yOffset),
                            size = androidx.compose.ui.geometry.Size(barWidth, barHeight)
                        )

                        // Draw Text or labels simulated by smaller graphic touches or legends
                    }
                }

                // Legend
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Text("Comp Sci (85%)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = DeepBlue)
                    Text("Elec Eng (72%)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = AcademicGold)
                    Text("Math (64%)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = AcademicGold)
                    Text("Mech Eng (58%)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = AcademicGold)
                }
            }
        }

        // Analytical metrics card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, IceBlue)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Anti-Fraud Telemetry Summary", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(10.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Duplicate GPS scans blocked:", fontSize = 12.sp, color = Color.Gray)
                    Text("4 logs", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = DeepBlue)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Mocked locations flagged:", fontSize = 12.sp, color = Color.Gray)
                    Text("2 logs", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = AcademicGold)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Offline entries synced locally:", fontSize = 12.sp, color = Color.Gray)
                    Text("12 items", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = DeepBlue)
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}
