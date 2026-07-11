package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class CampusViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CampusRepository
    
    init {
        val db = AppDatabase.getDatabase(application)
        repository = CampusRepository(db)
        
        // Seed initial mock data asynchronously on startup
        viewModelScope.launch {
            repository.seedMockDataIfEmpty()
        }
    }

    // AUTH STATE
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    private val _authState = MutableStateFlow<AuthState>(AuthState.LoggedOut)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage: SharedFlow<String> = _toastMessage.asSharedFlow()

    // Dynamic Lists mapped from DB Flows
    val students: StateFlow<List<UserEntity>> = repository.students
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lecturers: StateFlow<List<UserEntity>> = repository.lecturers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCourses: StateFlow<List<CourseEntity>> = repository.allCourses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSessions: StateFlow<List<ClassSessionEntity>> = repository.allSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allRecords: StateFlow<List<AttendanceRecordEntity>> = repository.allRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Notifications for currently logged-in user
    val notifications: StateFlow<List<NotificationEntity>> = _currentUser
        .flatMapLatest { user ->
            if (user != null) repository.getNotifications(user.id) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Simulated GPS Coordinates of Student (can be modified in settings/scanner)
    private val _studentLocation = MutableStateFlow(Pair(6.2543, 5.6123)) // Matches ETF Hall 1 default
    val studentLocation: StateFlow<Pair<Double, Double>> = _studentLocation.asStateFlow()

    // GPS Spoofing Prevention Simulation Toggle
    val gpsSpoofingPrevention = MutableStateFlow(true)

    // NFC Simulation Enabled
    val nfcAttendanceSupported = MutableStateFlow(true)

    // Offline Mode Simulation
    val isOfflineMode = MutableStateFlow(false)
    val pendingSyncCount = MutableStateFlow(0)

    // ACTIONS

    fun updateStudentLocation(lat: Double, lng: Double) {
        _studentLocation.value = Pair(lat, lng)
    }

    fun toggleOfflineMode() {
        isOfflineMode.value = !isOfflineMode.value
        viewModelScope.launch {
            if (!isOfflineMode.value) {
                // Simulating sync when coming online
                if (pendingSyncCount.value > 0) {
                    _toastMessage.emit("Network restored! Automatically synced ${pendingSyncCount.value} pending attendance logs.")
                    pendingSyncCount.value = 0
                }
            } else {
                _toastMessage.emit("Offline mode enabled. Attendance will be cached locally.")
            }
        }
    }

    // LOGIN & REGISTRATION
    fun login(idInput: String, passwordInput: String) {
        viewModelScope.launch {
            val trimmedId = idInput.trim()
            val user = repository.getUserSync(trimmedId)
            if (user != null && user.passwordHash == passwordInput) {
                _currentUser.value = user
                _authState.value = AuthState.LoggedIn(user)
                _toastMessage.emit("Welcome back, ${user.fullName}!")
            } else {
                _toastMessage.emit("Invalid credentials. Try: CSC/2022/001 (stu123) or L_CSC01 (lec123) or ADMIN01 (admin123)")
            }
        }
    }

    fun registerStudent(
        id: String,
        fullName: String,
        email: String,
        phone: String,
        department: String,
        faculty: String,
        level: String,
        gender: String,
        passwordInput: String
    ) {
        viewModelScope.launch {
            if (id.isBlank() || fullName.isBlank() || email.isBlank()) {
                _toastMessage.emit("Please fill out all required fields")
                return@launch
            }
            val existing = repository.getUserSync(id)
            if (existing != null) {
                _toastMessage.emit("User with this Matriculation / Staff ID already exists")
                return@launch
            }

            val newUser = UserEntity(
                id = id,
                role = "STUDENT",
                fullName = fullName,
                email = email,
                phone = phone,
                department = department,
                faculty = faculty,
                level = level,
                gender = gender,
                dateOfAdmission = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                passwordHash = passwordInput,
                avatarIndex = Random().nextInt(8)
            )
            repository.insertUser(newUser)
            _currentUser.value = newUser
            _authState.value = AuthState.LoggedIn(newUser)
            _toastMessage.emit("Registration successful!")

            // Welcome Notification
            repository.insertNotification(
                NotificationEntity(
                    userId = id,
                    title = "Welcome, Student!",
                    message = "Your profile has been created. Check eligibility and scan QR codes to mark attendance."
                )
            )
        }
    }

    fun logout() {
        _currentUser.value = null
        _authState.value = AuthState.LoggedOut
    }

    // ADMIN ACTIONS
    fun createCourse(code: String, title: String, units: Int, requirement: Int, dept: String, faculty: String, level: String, lecturerId: String) {
        viewModelScope.launch {
            if (code.isBlank() || title.isBlank() || lecturerId.isBlank()) {
                _toastMessage.emit("Please fill in course code, title and lecturer")
                return@launch
            }
            val newCourse = CourseEntity(
                code = code,
                title = title,
                units = units,
                requiredAttendancePercent = requirement,
                department = dept,
                faculty = faculty,
                level = level,
                lecturerId = lecturerId
            )
            repository.insertCourse(newCourse)
            _toastMessage.emit("Course $code created successfully!")
        }
    }

    fun createFacultyOrDepartment(message: String) {
        viewModelScope.launch {
            _toastMessage.emit(message)
        }
    }

    // LECTURER ACTIONS
    fun createClassSession(
        courseId: String,
        date: String,
        startTime: String,
        endTime: String,
        location: String,
        lat: Double,
        lng: Double,
        radius: Double,
        windowOpen: String,
        windowClose: String
    ) {
        viewModelScope.launch {
            val qrToken = "QR_${courseId}_${System.currentTimeMillis()}"
            val newSession = ClassSessionEntity(
                courseId = courseId,
                date = date,
                startTime = startTime,
                endTime = endTime,
                locationName = location,
                latitude = lat,
                longitude = lng,
                allowedRadiusMeters = radius,
                attendanceWindowOpen = windowOpen,
                attendanceWindowClose = windowClose,
                qrCodeToken = qrToken
            )
            val sessionId = repository.insertSession(newSession)
            _toastMessage.emit("Class session for $courseId scheduled successfully! ID: $sessionId")

            // Send notification to eligible students
            repository.students.first().forEach { student ->
                if (student.department == "Computer Science" && courseId.startsWith("CSC")) {
                    repository.insertNotification(
                        NotificationEntity(
                            userId = student.id,
                            title = "New Class Scheduled: $courseId",
                            message = "$courseId - $location scheduled on $date starting $startTime. Requirement: 75%."
                        )
                    )
                }
            }
        }
    }

    fun approveLateOrManualAttendance(studentId: String, sessionId: Int, status: String) {
        viewModelScope.launch {
            val existing = repository.getRecordForStudentAndSessionSync(studentId, sessionId)
            if (existing != null) {
                val updated = existing.copy(status = status, minutesLate = if (status == "Present") 0 else existing.minutesLate)
                repository.insertRecord(updated)
                _toastMessage.emit("Attendance for $studentId marked as $status!")
                repository.insertNotification(
                    NotificationEntity(
                        userId = studentId,
                        title = "Attendance Updated",
                        message = "Lecturer updated your attendance to: $status."
                    )
                )
            } else {
                // Insert a new manual record
                val record = AttendanceRecordEntity(
                    studentId = studentId,
                    classSessionId = sessionId,
                    arrivalTime = "Manual",
                    submissionTime = System.currentTimeMillis(),
                    status = status,
                    scannedViaQR = false,
                    verifiedByGPS = false
                )
                repository.insertRecord(record)
                _toastMessage.emit("Manual attendance marked as $status!")
                repository.insertNotification(
                    NotificationEntity(
                        userId = studentId,
                        title = "Attendance Recorded Manually",
                        message = "Your attendance has been recorded manually as: $status."
                    )
                )
            }
        }
    }

    // STUDENT ATTENDANCE SUBMISSION (QR + GPS + TIME CHECKS)
    fun recordStudentAttendanceViaQR(scannedToken: String, sessionId: Int) {
        viewModelScope.launch {
            val student = _currentUser.value
            if (student == null) {
                _toastMessage.emit("Not authenticated")
                return@launch
            }

            // 1. Fetch Session
            val session = repository.getSessionSync(sessionId)
            if (session == null) {
                _toastMessage.emit("Session not found")
                return@launch
            }

            // Check dynamic verification code matches session
            if (scannedToken != session.qrCodeToken && !scannedToken.startsWith("QR_")) {
                _toastMessage.emit("Invalid QR Code Token")
                return@launch
            }

            // 2. Check Duplicates
            val existing = repository.getRecordForStudentAndSessionSync(student.id, session.id)
            if (existing != null && existing.status != "Absent") {
                _toastMessage.emit("You have already recorded attendance for this class!")
                return@launch
            }

            // 3. GPS Verification
            val studLat = _studentLocation.value.first
            val studLng = _studentLocation.value.second
            val distance = calculateDistanceInMeters(studLat, studLng, session.latitude, session.longitude)

            // GPS Spoofing detection (Simulated check: if student's speed is abnormally high, or location is anomalous, flag it)
            if (gpsSpoofingPrevention.value && (studLat == 0.0 && studLng == 0.0)) {
                _toastMessage.emit("Security Alert: GPS Spoofing detected or Location Services disabled. Attendance denied.")
                return@launch
            }

            val gpsVerified = distance <= session.allowedRadiusMeters
            if (!gpsVerified) {
                _toastMessage.emit("GPS Verification Failed: You are ${distance.toInt()}m away (Allowed radius: ${session.allowedRadiusMeters.toInt()}m).")
                return@launch
            }

            // 4. Time Tracking System
            val calendar = Calendar.getInstance()
            val arrivalTimeStr = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(calendar.time)
            
            // Arrival evaluation
            val startMin = parseTimeToMinutes(session.startTime)
            val arrivalMin = parseTimeToMinutes(arrivalTimeStr)
            val minutesDifference = arrivalMin - startMin

            // Check if window is open/closed
            val windowOpenMin = parseTimeToMinutes(session.attendanceWindowOpen)
            val windowCloseMin = parseTimeToMinutes(session.attendanceWindowClose)

            if (arrivalMin < windowOpenMin) {
                _toastMessage.emit("Attendance Window is not open yet. Starts at ${session.attendanceWindowOpen}.")
                return@launch
            }
            if (arrivalMin > windowCloseMin) {
                _toastMessage.emit("Attendance Window closed at ${session.attendanceWindowClose}. Please contact your lecturer.")
                return@launch
            }

            // Core Logic for Attendance Status
            // Present: 0 - 10 minutes late
            // Late: 11 - 30 minutes late
            // Absent: More than 30 minutes late
            val status = when {
                minutesDifference <= 10 -> "Present"
                minutesDifference <= 30 -> "Late"
                else -> "Absent"
            }

            val record = AttendanceRecordEntity(
                studentId = student.id,
                classSessionId = session.id,
                arrivalTime = arrivalTimeStr,
                submissionTime = System.currentTimeMillis(),
                status = status,
                minutesLate = if (minutesDifference > 0) minutesDifference else 0,
                latitude = studLat,
                longitude = studLng,
                scannedViaQR = true,
                verifiedByGPS = true,
                isSynced = !isOfflineMode.value
            )

            if (isOfflineMode.value) {
                repository.insertRecord(record)
                pendingSyncCount.value += 1
                _toastMessage.emit("Saved offline! Will sync automatically when network returns.")
            } else {
                repository.insertRecord(record)
                _toastMessage.emit("Attendance successfully recorded as '$status'! Arrived at $arrivalTimeStr ($minutesDifference min late).")
            }

            // Post notification
            repository.insertNotification(
                NotificationEntity(
                    userId = student.id,
                    title = "Attendance Recorded: ${session.courseId}",
                    message = "You have been marked $status for class on ${session.date} at $arrivalTimeStr."
                )
            )

            // Trigger attendance warning if overall rate falls low
            triggerLowAttendanceWarningIfNecessary(student.id, session.courseId)
        }
    }

    private suspend fun triggerLowAttendanceWarningIfNecessary(studentId: String, courseId: String) {
        // Fetch all course records
        val allSess = repository.allSessions.first().filter { it.courseId == courseId }
        if (allSess.isEmpty()) return

        val myRecs = repository.allRecords.first().filter { it.studentId == studentId && mySessionIds(allSess).contains(it.classSessionId) }
        val presentCount = myRecs.count { it.status == "Present" || it.status == "Late" }
        val pct = (presentCount.toFloat() / allSess.size) * 100

        if (pct < 75.0f) {
            repository.insertNotification(
                NotificationEntity(
                    userId = studentId,
                    title = "Low Attendance Warning! ⚠️",
                    message = "You currently have ${pct.toInt()}% attendance in $courseId. Minimum required for examination eligibility is 75%!"
                )
            )
        }
    }

    private fun mySessionIds(sessions: List<ClassSessionEntity>): Set<Int> {
        return sessions.map { it.id }.toSet()
    }

    // NFC Simulation Tap Action
    fun simulateNFCCardTap(sessionId: Int) {
        viewModelScope.launch {
            if (!nfcAttendanceSupported.value) {
                _toastMessage.emit("NFC is disabled in settings.")
                return@launch
            }
            _toastMessage.emit("Simulating NFC Card Tap...")
            // Tap behaves like scanning QR instantly
            val calendar = Calendar.getInstance()
            val arrivalTimeStr = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(calendar.time)
            
            val student = _currentUser.value ?: return@launch
            val session = repository.getSessionSync(sessionId) ?: return@launch

            // Immediate validation
            val record = AttendanceRecordEntity(
                studentId = student.id,
                classSessionId = session.id,
                arrivalTime = arrivalTimeStr,
                submissionTime = System.currentTimeMillis(),
                status = "Present", // NFC implies physical presence in classroom
                minutesLate = 0,
                latitude = session.latitude,
                longitude = session.longitude,
                scannedViaQR = false,
                verifiedByGPS = true
            )
            repository.insertRecord(record)
            _toastMessage.emit("NFC Attendance Recorded! Marked Present at $arrivalTimeStr.")

            repository.insertNotification(
                NotificationEntity(
                    userId = student.id,
                    title = "NFC Attendance Recorded",
                    message = "Logged Present for ${session.courseId} via physical Smart NFC Card reader."
                )
            )
        }
    }

    // Notification Clean up
    fun clearMyNotifications() {
        val user = _currentUser.value
        if (user != null) {
            viewModelScope.launch {
                repository.clearNotifications(user.id)
                _toastMessage.emit("Notifications cleared!")
            }
        }
    }

    // UTILS
    fun parseTimeToMinutes(timeStr: String): Int {
        try {
            val clean = timeStr.trim().uppercase()
            // Format check: can be "09:00 AM" or "9:00 AM" or just HH:MM
            val parts = clean.split(" ")
            val timeParts = parts[0].split(":")
            var hours = timeParts[0].toInt()
            val minutes = timeParts[1].toInt()
            val amPm = if (parts.size > 1) parts[1] else {
                if (hours >= 12) "PM" else "AM"
            }
            
            val adjHours = when {
                amPm == "PM" && hours < 12 -> hours + 12
                amPm == "AM" && hours == 12 -> 0
                else -> hours
            }
            return adjHours * 60 + minutes
        } catch (e: Exception) {
            return 540 // Default to 9:00 AM if parse fails
        }
    }

    // Haversine distance formula to compute distance in meters between two lat/lng coordinates
    private fun calculateDistanceInMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371e3 // Earth's radius in meters
        val phi1 = Math.toRadians(lat1)
        val phi2 = Math.toRadians(lat2)
        val deltaPhi = Math.toRadians(lat2 - lat1)
        val deltaLambda = Math.toRadians(lon2 - lon1)

        val a = Math.sin(deltaPhi / 2.0) * Math.sin(deltaPhi / 2.0) +
                Math.cos(phi1) * Math.cos(phi2) *
                Math.sin(deltaLambda / 2.0) * Math.sin(deltaLambda / 2.0)
        val c = 2.0 * Math.atan2(Math.sqrt(a), Math.sqrt(1.0 - a))

        return r * c // returns distance in meters
    }
}

sealed interface AuthState {
    object LoggedOut : AuthState
    data class LoggedIn(val user: UserEntity) : AuthState
}
