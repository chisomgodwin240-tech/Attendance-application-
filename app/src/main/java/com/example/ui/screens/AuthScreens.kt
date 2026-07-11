package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AcademicGold
import com.example.ui.theme.DeepBlue
import com.example.ui.viewmodel.CampusViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: CampusViewModel,
    onRegisterClick: () -> Unit
) {
    var selectedRoleTab by remember { mutableStateOf(0) } // 0 = Student, 1 = Lecturer/Admin
    var idInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Dialog simulations
    var showForgotPassword by remember { mutableStateOf(false) }
    var forgotEmail by remember { mutableStateOf("") }
    var showOtpDialog by remember { mutableStateOf(false) }
    var otpInput by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Decorative header background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(DeepBlue, MaterialTheme.colorScheme.primary)
                    )
                )
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = "Logo",
                    tint = AcademicGold,
                    modifier = Modifier.size(56.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "CAMPUS ATTENDANCE TRACKER",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Secure Verification Portal",
                    color = AcademicGold,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Form Container
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 210.dp, start = 20.dp, end = 20.dp)
                .align(Alignment.TopCenter),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Portal Access",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Role Tab Row
                TabRow(
                    selectedTabIndex = selectedRoleTab,
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Tab(
                        selected = selectedRoleTab == 0,
                        onClick = {
                            selectedRoleTab = 0
                            idInput = ""
                        },
                        text = { Text("Student", fontWeight = FontWeight.SemiBold) }
                    )
                    Tab(
                        selected = selectedRoleTab == 1,
                        onClick = {
                            selectedRoleTab = 1
                            idInput = ""
                        },
                        text = { Text("Staff / Admin", fontWeight = FontWeight.SemiBold) }
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Username / ID Field
                OutlinedTextField(
                    value = idInput,
                    onValueChange = { idInput = it },
                    label = {
                        Text(if (selectedRoleTab == 0) "Matriculation Number" else "Staff ID / Username")
                    },
                    placeholder = {
                        Text(if (selectedRoleTab == 0) "e.g. CSC/2022/001" else "e.g. L_CSC01 or ADMIN01")
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = if (selectedRoleTab == 0) Icons.Default.Badge else Icons.Default.Person,
                            contentDescription = "ID Icon"
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("id_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Password Field
                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = { passwordInput = it },
                    label = { Text("Secret Password") },
                    placeholder = { Text("••••••••") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Lock Icon"
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle Password"
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("password_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Forgot Password Button
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Text(
                        text = "Forgot password?",
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        modifier = Modifier
                            .clickable { showForgotPassword = true }
                            .padding(4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Log In Button
                Button(
                    onClick = { viewModel.login(idInput, passwordInput) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("login_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(imageVector = Icons.Default.Login, contentDescription = "Login")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "AUTHENTICATE",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Register Link (For students only)
                if (selectedRoleTab == 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "New Student?",
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Register here",
                            color = AcademicGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier
                                .clickable { onRegisterClick() }
                                .padding(4.dp)
                        )
                    }
                }
            }
        }

        // Demo Credentials Hint Pane
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, bottom = 24.dp)
                .align(Alignment.BottomCenter),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Demo Credentials Quick Access",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Student: CSC/2022/001 / stu123", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                    Text(text = "Lecturer: L_CSC01 / lec123", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        // Forgot Password Modal
        if (showForgotPassword) {
            AlertDialog(
                onDismissRequest = { showForgotPassword = false },
                title = { Text("Account Recovery") },
                text = {
                    Column {
                        Text("Enter your email or Staff/Matric number to receive a secure recovery OTP verification.")
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = forgotEmail,
                            onValueChange = { forgotEmail = it },
                            label = { Text("Email / ID") },
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (forgotEmail.isNotBlank()) {
                                showForgotPassword = false
                                showOtpDialog = true
                            }
                        }
                    ) {
                        Text("REQUEST OTP")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showForgotPassword = false }) {
                        Text("CANCEL")
                    }
                }
            )
        }

        // OTP Verification Modal
        if (showOtpDialog) {
            AlertDialog(
                onDismissRequest = { showOtpDialog = false },
                title = { Text("Email OTP Verification") },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("A secure 6-digit OTP verification code has been simulated for email verification: 482931")
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = otpInput,
                            onValueChange = { otpInput = it },
                            label = { Text("Enter OTP Code") },
                            placeholder = { Text("e.g. 482931") },
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.width(180.dp)
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (otpInput == "482931") {
                                showOtpDialog = false
                                idInput = "CSC/2022/001"
                                passwordInput = "stu123"
                                showForgotPassword = false
                            }
                        }
                    ) {
                        Text("VERIFY & LOG IN")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showOtpDialog = false }) {
                        Text("CANCEL")
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    viewModel: CampusViewModel,
    onBackToLogin: () -> Unit
) {
    var id by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("Computer Science") }
    var faculty by remember { mutableStateOf("Physical Sciences") }
    var level by remember { mutableStateOf("400L") }
    var gender by remember { mutableStateOf("Female") }
    var password by remember { mutableStateOf("") }

    val levels = listOf("100L", "200L", "300L", "400L", "500L")
    val genders = listOf("Female", "Male")
    val departments = listOf("Computer Science", "Electrical Engineering", "Mathematics", "Mechanical Engineering")
    val faculties = listOf("Physical Sciences", "Engineering", "Biological Sciences")

    var deptExpanded by remember { mutableStateOf(false) }
    var facultyExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Spacer(modifier = Modifier.height(28.dp))
            
            IconButton(
                onClick = { onBackToLogin() },
                modifier = Modifier.background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
            ) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Student Registration",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Fill in details to set up your academic time-tracker portal.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Form Cards
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Matric Number
                    OutlinedTextField(
                        value = id,
                        onValueChange = { id = it },
                        label = { Text("Matriculation Number") },
                        placeholder = { Text("e.g. CSC/2022/005") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Full Name
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Full Name (Surname First)") },
                        placeholder = { Text("e.g. Chisom Godwin") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Email
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Institutional Email") },
                        placeholder = { Text("e.g. chisom@student.edu") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Phone Number
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Mobile Phone Number") },
                        placeholder = { Text("e.g. +2347012345678") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Faculty Dropdown Selector
                    ExposedDropdownMenuBox(
                        expanded = facultyExpanded,
                        onExpandedChange = { facultyExpanded = !facultyExpanded }
                    ) {
                        OutlinedTextField(
                            value = faculty,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Faculty") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = facultyExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = facultyExpanded,
                            onDismissRequest = { facultyExpanded = false }
                        ) {
                            faculties.forEach { f ->
                                DropdownMenuItem(
                                    text = { Text(f) },
                                    onClick = {
                                        faculty = f
                                        facultyExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Department Dropdown Selector
                    ExposedDropdownMenuBox(
                        expanded = deptExpanded,
                        onExpandedChange = { deptExpanded = !deptExpanded }
                    ) {
                        OutlinedTextField(
                            value = department,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Department") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = deptExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = deptExpanded,
                            onDismissRequest = { deptExpanded = false }
                        ) {
                            departments.forEach { d ->
                                DropdownMenuItem(
                                    text = { Text(d) },
                                    onClick = {
                                        department = d
                                        deptExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Level Radio Buttons
                    Text("Current Academic Level:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        levels.forEach { l ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { level = l }
                            ) {
                                RadioButton(selected = level == l, onClick = { level = l })
                                Text(l, fontSize = 13.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Gender Radio Buttons
                    Text("Gender:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Row {
                        genders.forEach { g ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clickable { gender = g }
                                    .padding(end = 24.dp)
                            ) {
                                RadioButton(selected = gender == g, onClick = { gender = g })
                                Text(g, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Password Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password Selection") },
                    placeholder = { Text("At least 6 characters") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(10.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Submit Button
            Button(
                onClick = {
                    viewModel.registerStudent(
                        id, name, email, phone, department, faculty, level, gender, password
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("CREATE ACCREDITED PROFILE", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}
