package com.example.data

import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*

class CampusRepository(private val db: AppDatabase) {

    private val userDao = db.userDao()
    private val courseDao = db.courseDao()
    private val classSessionDao = db.classSessionDao()
    private val attendanceRecordDao = db.attendanceRecordDao()
    private val notificationDao = db.notificationDao()

    // Flows
    val allUsers: Flow<List<UserEntity>> = userDao.getAllUsers()
    val lecturers: Flow<List<UserEntity>> = userDao.getLecturers()
    val students: Flow<List<UserEntity>> = userDao.getStudents()
    val allCourses: Flow<List<CourseEntity>> = courseDao.getAllCourses()
    val allSessions: Flow<List<ClassSessionEntity>> = classSessionDao.getAllSessions()
    val allRecords: Flow<List<AttendanceRecordEntity>> = attendanceRecordDao.getAllRecords()

    fun getNotifications(userId: String): Flow<List<NotificationEntity>> =
        notificationDao.getNotificationsForUser(userId)

    fun getCourseByCode(code: String): Flow<CourseEntity?> =
        courseDao.getCourseByCode(code)

    fun getSessionsForCourse(courseId: String): Flow<List<ClassSessionEntity>> =
        classSessionDao.getSessionsForCourse(courseId)

    fun getSessionsForLecturer(lecturerId: String): Flow<List<ClassSessionEntity>> =
        classSessionDao.getSessionsForLecturer(lecturerId)

    fun getRecordsForSession(sessionId: Int): Flow<List<AttendanceRecordEntity>> =
        attendanceRecordDao.getRecordsForSession(sessionId)

    fun getRecordsForStudent(studentId: String): Flow<List<AttendanceRecordEntity>> =
        attendanceRecordDao.getRecordsForStudent(studentId)

    fun getRecordForStudentAndSession(studentId: String, sessionId: Int): Flow<AttendanceRecordEntity?> =
        attendanceRecordDao.getRecordForStudentAndSession(studentId, sessionId)

    // Suspends
    suspend fun getUserSync(id: String): UserEntity? = userDao.getUserByIdSync(id)
    suspend fun insertUser(user: UserEntity) = userDao.insertUser(user)
    suspend fun deleteUser(id: String) = userDao.deleteUser(id)

    suspend fun insertCourse(course: CourseEntity) = courseDao.insertCourse(course)
    suspend fun deleteCourse(code: String) = courseDao.deleteCourseByCode(code)

    suspend fun insertSession(session: ClassSessionEntity): Long = classSessionDao.insertSession(session)
    suspend fun getSessionSync(id: Int): ClassSessionEntity? = classSessionDao.getSessionByIdSync(id)
    suspend fun deleteSession(id: Int) = classSessionDao.deleteSessionById(id)

    suspend fun insertRecord(record: AttendanceRecordEntity) = attendanceRecordDao.insertRecord(record)
    suspend fun updateRecord(record: AttendanceRecordEntity) = attendanceRecordDao.updateRecord(record)
    suspend fun getRecordForStudentAndSessionSync(studentId: String, sessionId: Int): AttendanceRecordEntity? =
        attendanceRecordDao.getRecordForStudentAndSessionSync(studentId, sessionId)

    suspend fun insertNotification(notification: NotificationEntity) =
        notificationDao.insertNotification(notification)
    suspend fun markNotificationAsRead(id: Int) = notificationDao.markAsRead(id)
    suspend fun clearNotifications(userId: String) = notificationDao.clearNotificationsForUser(userId)

    // Seed Data Helper
    suspend fun seedMockDataIfEmpty() {
        val adminUser = userDao.getUserByIdSync("ADMIN01")
        if (adminUser != null) return // Already seeded

        // 1. Seed Admins
        userDao.insertUser(
            UserEntity(
                id = "ADMIN01",
                role = "ADMIN",
                fullName = "Dr. Arthur Pendragon",
                email = "admin@campus.edu",
                phone = "+2348011223344",
                department = "Registry",
                faculty = "Central Administration",
                passwordHash = "admin123",
                avatarIndex = 0
            )
        )

        // 2. Seed Lecturers
        val lecturersToSeed = listOf(
            UserEntity(
                id = "L_CSC01",
                role = "LECTURER",
                fullName = "Prof. Charles Babbage",
                email = "charles.babbage@campus.edu",
                phone = "+2348022334455",
                department = "Computer Science",
                faculty = "Physical Sciences",
                passwordHash = "lec123",
                avatarIndex = 1
            ),
            UserEntity(
                id = "L_EEE01",
                role = "LECTURER",
                fullName = "Dr. Nikola Tesla",
                email = "nikola.tesla@campus.edu",
                phone = "+2348033445566",
                department = "Electrical Engineering",
                faculty = "Engineering",
                passwordHash = "lec123",
                avatarIndex = 2
            )
        )
        lecturersToSeed.forEach { userDao.insertUser(it) }

        // 3. Seed Students
        val studentsToSeed = listOf(
            UserEntity(
                id = "CSC/2022/001",
                role = "STUDENT",
                fullName = "Chisom Godwin",
                email = "chisom.godwin@student.edu",
                phone = "+2347012345678",
                department = "Computer Science",
                faculty = "Physical Sciences",
                level = "400L",
                gender = "Male",
                dateOfAdmission = "2022-10-15",
                passwordHash = "stu123",
                avatarIndex = 3
            ),
            UserEntity(
                id = "CSC/2022/002",
                role = "STUDENT",
                fullName = "Ada Lovelace",
                email = "ada.lovelace@student.edu",
                phone = "+2347087654321",
                department = "Computer Science",
                faculty = "Physical Sciences",
                level = "400L",
                gender = "Female",
                dateOfAdmission = "2022-10-15",
                passwordHash = "stu123",
                avatarIndex = 4
            ),
            UserEntity(
                id = "CSC/2022/003",
                role = "STUDENT",
                fullName = "Grace Hopper",
                email = "grace.hopper@student.edu",
                phone = "+2348099887766",
                department = "Computer Science",
                faculty = "Physical Sciences",
                level = "400L",
                gender = "Female",
                dateOfAdmission = "2022-10-15",
                passwordHash = "stu123",
                avatarIndex = 5
            ),
            UserEntity(
                id = "EEE/2022/015",
                role = "STUDENT",
                fullName = "Alan Turing",
                email = "alan.turing@student.edu",
                phone = "+2348122334455",
                department = "Electrical Engineering",
                faculty = "Engineering",
                level = "400L",
                gender = "Male",
                dateOfAdmission = "2022-10-15",
                passwordHash = "stu123",
                avatarIndex = 6
            )
        )
        studentsToSeed.forEach { userDao.insertUser(it) }

        // 4. Seed Courses
        val coursesToSeed = listOf(
            CourseEntity(
                code = "CSC401",
                title = "Mobile Application Development",
                units = 3,
                requiredAttendancePercent = 75,
                department = "Computer Science",
                faculty = "Physical Sciences",
                level = "400L",
                lecturerId = "L_CSC01"
            ),
            CourseEntity(
                code = "CSC403",
                title = "Artificial Intelligence",
                units = 4,
                requiredAttendancePercent = 75,
                department = "Computer Science",
                faculty = "Physical Sciences",
                level = "400L",
                lecturerId = "L_CSC01"
            ),
            CourseEntity(
                code = "EEE412",
                title = "Control Systems Engineering",
                units = 3,
                requiredAttendancePercent = 70,
                department = "Electrical Engineering",
                faculty = "Engineering",
                level = "400L",
                lecturerId = "L_EEE01"
            )
        )
        coursesToSeed.forEach { courseDao.insertCourse(it) }

        // 5. Seed Class Sessions (Past and Upcoming)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()

        // Session 1: CSC401 (3 days ago - Completed)
        calendar.add(Calendar.DAY_OF_YEAR, -3)
        val dateSession1 = dateFormat.format(calendar.time)
        val s1Id = classSessionDao.insertSession(
            ClassSessionEntity(
                courseId = "CSC401",
                date = dateSession1,
                startTime = "09:00 AM",
                endTime = "11:00 AM",
                locationName = "ETF Hall 1",
                latitude = 6.2543,
                longitude = 5.6123,
                allowedRadiusMeters = 50.0,
                attendanceWindowOpen = "08:45 AM",
                attendanceWindowClose = "09:20 AM",
                qrCodeToken = "TOKEN_CSC401_S1"
            )
        ).toInt()

        // Session 2: CSC401 (Yesterday - Completed)
        calendar.add(Calendar.DAY_OF_YEAR, 2) // yesterday
        val dateSession2 = dateFormat.format(calendar.time)
        val s2Id = classSessionDao.insertSession(
            ClassSessionEntity(
                courseId = "CSC401",
                date = dateSession2,
                startTime = "09:00 AM",
                endTime = "11:00 AM",
                locationName = "ETF Hall 1",
                latitude = 6.2543,
                longitude = 5.6123,
                allowedRadiusMeters = 50.0,
                attendanceWindowOpen = "08:45 AM",
                attendanceWindowClose = "09:20 AM",
                qrCodeToken = "TOKEN_CSC401_S2"
            )
        ).toInt()

        // Session 3: EEE412 (2 days ago - Completed)
        calendar.set(Calendar.DAY_OF_YEAR, Calendar.getInstance().get(Calendar.DAY_OF_YEAR) - 2)
        val dateSession3 = dateFormat.format(calendar.time)
        val s3Id = classSessionDao.insertSession(
            ClassSessionEntity(
                courseId = "EEE412",
                date = dateSession3,
                startTime = "11:00 AM",
                endTime = "01:00 PM",
                locationName = "Engineering Theatre A",
                latitude = 6.2599,
                longitude = 5.6155,
                allowedRadiusMeters = 50.0,
                attendanceWindowOpen = "10:45 AM",
                attendanceWindowClose = "11:20 AM",
                qrCodeToken = "TOKEN_EEE412_S3"
            )
        ).toInt()

        // Session 4: CSC401 (Upcoming - Today in a few hours)
        calendar.set(Calendar.DAY_OF_YEAR, Calendar.getInstance().get(Calendar.DAY_OF_YEAR))
        val dateSession4 = dateFormat.format(calendar.time)
        val s4Id = classSessionDao.insertSession(
            ClassSessionEntity(
                courseId = "CSC401",
                date = dateSession4,
                startTime = "09:00 AM",
                endTime = "11:00 AM",
                locationName = "ETF Hall 1",
                latitude = 6.2543,
                longitude = 5.6123,
                allowedRadiusMeters = 50.0,
                attendanceWindowOpen = "08:45 AM",
                attendanceWindowClose = "09:20 AM",
                qrCodeToken = "TOKEN_CSC401_ACTIVE"
            )
        ).toInt()

        // 6. Seed Attendance Records for Sessions
        // For s1Id (CSC401)
        // Chisom was Present (arrived 9:03 AM)
        attendanceRecordDao.insertRecord(
            AttendanceRecordEntity(
                studentId = "CSC/2022/001",
                classSessionId = s1Id,
                arrivalTime = "09:03 AM",
                submissionTime = System.currentTimeMillis() - (3 * 24 * 3600 * 1000L),
                status = "Present",
                minutesLate = 3,
                latitude = 6.25431,
                longitude = 5.61229
            )
        )
        // Ada Lovelace was Late (arrived 9:15 AM)
        attendanceRecordDao.insertRecord(
            AttendanceRecordEntity(
                studentId = "CSC/2022/002",
                classSessionId = s1Id,
                arrivalTime = "09:15 AM",
                submissionTime = System.currentTimeMillis() - (3 * 24 * 3600 * 1000L),
                status = "Late",
                minutesLate = 15,
                latitude = 6.25432,
                longitude = 5.61228
            )
        )
        // Grace Hopper was Absent (never scanned)
        attendanceRecordDao.insertRecord(
            AttendanceRecordEntity(
                studentId = "CSC/2022/003",
                classSessionId = s1Id,
                arrivalTime = "--",
                submissionTime = 0,
                status = "Absent",
                minutesLate = 120,
                latitude = 0.0,
                longitude = 0.0,
                scannedViaQR = false,
                verifiedByGPS = false
            )
        )

        // For s2Id (CSC401)
        // Chisom was Present (arrived 9:02 AM)
        attendanceRecordDao.insertRecord(
            AttendanceRecordEntity(
                studentId = "CSC/2022/001",
                classSessionId = s2Id,
                arrivalTime = "09:02 AM",
                submissionTime = System.currentTimeMillis() - (24 * 3600 * 1000L),
                status = "Present",
                minutesLate = 2,
                latitude = 6.25432,
                longitude = 5.61231
            )
        )
        // Ada Lovelace was Present (arrived 9:08 AM)
        attendanceRecordDao.insertRecord(
            AttendanceRecordEntity(
                studentId = "CSC/2022/002",
                classSessionId = s2Id,
                arrivalTime = "09:08 AM",
                submissionTime = System.currentTimeMillis() - (24 * 3600 * 1000L),
                status = "Present",
                minutesLate = 8,
                latitude = 6.25431,
                longitude = 5.61229
            )
        )
        // Grace Hopper was Late (arrived 9:22 AM)
        attendanceRecordDao.insertRecord(
            AttendanceRecordEntity(
                studentId = "CSC/2022/003",
                classSessionId = s2Id,
                arrivalTime = "09:22 AM",
                submissionTime = System.currentTimeMillis() - (24 * 3600 * 1000L),
                status = "Late",
                minutesLate = 22,
                latitude = 6.25433,
                longitude = 5.61230
            )
        )

        // For s3Id (EEE412)
        // Alan Turing was Present (arrived 11:05 AM)
        attendanceRecordDao.insertRecord(
            AttendanceRecordEntity(
                studentId = "EEE/2022/015",
                classSessionId = s3Id,
                arrivalTime = "11:05 AM",
                submissionTime = System.currentTimeMillis() - (2 * 24 * 3600 * 1000L),
                status = "Present",
                minutesLate = 5,
                latitude = 6.2598,
                longitude = 5.6154
            )
        )

        // 7. Seed initial notifications
        notificationDao.insertNotification(
            NotificationEntity(
                userId = "CSC/2022/001",
                title = "Welcome to Campus Tracker",
                message = "Your attendance profile has been set up successfully for Computer Science 400 Level.",
                timestamp = System.currentTimeMillis() - (4 * 24 * 3600 * 1000L),
                isRead = true
            )
        )
        notificationDao.insertNotification(
            NotificationEntity(
                userId = "CSC/2022/001",
                title = "Upcoming Class Session",
                message = "CSC401 - Mobile Application Development scheduled today at 9:00 AM in ETF Hall 1.",
                timestamp = System.currentTimeMillis() - (3600 * 1000L),
                isRead = false
            )
        )
        notificationDao.insertNotification(
            NotificationEntity(
                userId = "L_CSC01",
                title = "Welcome, Professor",
                message = "Your staff profile and assigned courses (CSC401, CSC403) are ready for management.",
                timestamp = System.currentTimeMillis() - (4 * 24 * 3600 * 1000L),
                isRead = true
            )
        )
    }
}
