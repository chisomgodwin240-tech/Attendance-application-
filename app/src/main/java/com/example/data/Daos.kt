package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :id")
    fun getUserById(id: String): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUserByIdSync(id: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE role = 'LECTURER'")
    fun getLecturers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE role = 'STUDENT'")
    fun getStudents(): Flow<List<UserEntity>>

    @Query("DELETE FROM users WHERE id = :id")
    suspend fun deleteUser(id: String)
}

@Dao
interface CourseDao {
    @Query("SELECT * FROM courses WHERE code = :code")
    fun getCourseByCode(code: String): Flow<CourseEntity?>

    @Query("SELECT * FROM courses")
    fun getAllCourses(): Flow<List<CourseEntity>>

    @Query("SELECT * FROM courses WHERE lecturerId = :lecturerId")
    fun getCoursesByLecturer(lecturerId: String): Flow<List<CourseEntity>>

    @Query("SELECT * FROM courses WHERE department = :dept AND level = :level")
    fun getCoursesByDeptAndLevel(dept: String, level: String): Flow<List<CourseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourse(course: CourseEntity)

    @Query("DELETE FROM courses WHERE code = :code")
    suspend fun deleteCourseByCode(code: String)
}

@Dao
interface ClassSessionDao {
    @Query("SELECT * FROM class_sessions WHERE id = :id")
    fun getSessionById(id: Int): Flow<ClassSessionEntity?>

    @Query("SELECT * FROM class_sessions WHERE id = :id")
    suspend fun getSessionByIdSync(id: Int): ClassSessionEntity?

    @Query("SELECT * FROM class_sessions WHERE courseId = :courseId ORDER BY date DESC")
    fun getSessionsForCourse(courseId: String): Flow<List<ClassSessionEntity>>

    @Query("SELECT cs.* FROM class_sessions cs INNER JOIN courses c ON cs.courseId = c.code WHERE c.lecturerId = :lecturerId ORDER BY cs.date DESC")
    fun getSessionsForLecturer(lecturerId: String): Flow<List<ClassSessionEntity>>

    @Query("SELECT * FROM class_sessions ORDER BY date DESC")
    fun getAllSessions(): Flow<List<ClassSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: ClassSessionEntity): Long

    @Query("DELETE FROM class_sessions WHERE id = :id")
    suspend fun deleteSessionById(id: Int)
}

@Dao
interface AttendanceRecordDao {
    @Query("SELECT * FROM attendance_records WHERE classSessionId = :sessionId")
    fun getRecordsForSession(sessionId: Int): Flow<List<AttendanceRecordEntity>>

    @Query("SELECT * FROM attendance_records WHERE studentId = :studentId")
    fun getRecordsForStudent(studentId: String): Flow<List<AttendanceRecordEntity>>

    @Query("SELECT * FROM attendance_records WHERE studentId = :studentId AND classSessionId = :sessionId")
    fun getRecordForStudentAndSession(studentId: String, sessionId: Int): Flow<AttendanceRecordEntity?>

    @Query("SELECT * FROM attendance_records WHERE studentId = :studentId AND classSessionId = :sessionId")
    suspend fun getRecordForStudentAndSessionSync(studentId: String, sessionId: Int): AttendanceRecordEntity?

    @Query("SELECT * FROM attendance_records")
    fun getAllRecords(): Flow<List<AttendanceRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: AttendanceRecordEntity)

    @Update
    suspend fun updateRecord(record: AttendanceRecordEntity)

    @Query("DELETE FROM attendance_records WHERE id = :id")
    suspend fun deleteRecordById(id: Int)
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY timestamp DESC")
    fun getNotificationsForUser(userId: String): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: Int)

    @Query("DELETE FROM notifications WHERE userId = :userId")
    suspend fun clearNotificationsForUser(userId: String)
}
