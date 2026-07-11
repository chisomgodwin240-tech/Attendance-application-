package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String, // Matriculation Number (Student), Staff ID (Lecturer), or ADMIN ID
    val role: String, // "STUDENT", "LECTURER", "ADMIN"
    val fullName: String,
    val email: String,
    val phone: String,
    val department: String,
    val faculty: String,
    val level: String = "", // e.g. "100L", "200L", "300L", "400L", "500L" (Empty for lecturers)
    val gender: String = "Male",
    val dateOfAdmission: String = "", // e.g. "2023-09-12" (Empty for lecturers)
    val passwordHash: String = "password", // Simple simulated hashed/plain password
    val avatarIndex: Int = 0 // Used to display consistent local avatars
)

@Entity(tableName = "courses")
data class CourseEntity(
    @PrimaryKey val code: String, // e.g. "CSC401"
    val title: String, // e.g. "Mobile Application Development"
    val units: Int, // e.g. 3
    val requiredAttendancePercent: Int = 75, // e.g. 75%
    val department: String,
    val faculty: String,
    val level: String, // e.g. "400L"
    val lecturerId: String // UserEntity.id (Lecturer)
)

@Entity(tableName = "class_sessions")
data class ClassSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val courseId: String, // CourseEntity.code
    val date: String, // "YYYY-MM-DD"
    val startTime: String, // "09:00 AM" (or HH:MM)
    val endTime: String, // "11:00 AM"
    val locationName: String, // e.g. "Hall A", "ETF Lecture Theatre"
    val latitude: Double = 6.2543, // Target Latitude
    val longitude: Double = 5.6123, // Target Longitude
    val allowedRadiusMeters: Double = 50.0,
    val attendanceWindowOpen: String = "08:45 AM",
    val attendanceWindowClose: String = "09:20 AM",
    val qrCodeToken: String = "" // Random token generated for QR attendance
)

@Entity(tableName = "attendance_records")
data class AttendanceRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val studentId: String, // UserEntity.id (Student)
    val classSessionId: Int, // ClassSessionEntity.id
    val arrivalTime: String, // "09:03 AM"
    val submissionTime: Long, // Epoch timestamp in ms
    val status: String, // "Present", "Late", "Absent", "Excused"
    val minutesLate: Int = 0,
    val latitude: Double = 0.0, // Student scanned latitude
    val longitude: Double = 0.0, // Student scanned longitude
    val scannedViaQR: Boolean = true,
    val verifiedByGPS: Boolean = true,
    val isSynced: Boolean = true
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String, // Target user
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)
