package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.*
import com.example.ui.theme.AcademicGold
import com.example.ui.theme.DeepBlue
import com.example.ui.theme.IceBlue
import com.example.ui.viewmodel.AuthState
import com.example.ui.viewmodel.CampusViewModel
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampusApp(viewModel: CampusViewModel) {
    val context = LocalContext.current
    val authState by viewModel.authState.collectAsState()
    val user by viewModel.currentUser.collectAsState()
    val isOffline by viewModel.isOfflineMode.collectAsState()
    val pendingSyncs by viewModel.pendingSyncCount.collectAsState()

    var showSplash by remember { mutableStateOf(true) }
    var showRegisterScreen by remember { mutableStateOf(false) }
    var activeTab by remember { mutableStateOf("home") } // "home", "reports", "alerts", "profile", "settings"

    // Toast listener
    LaunchedEffect(key1 = true) {
        viewModel.toastMessage.collectLatest { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
        }
    }

    if (showSplash) {
        SplashScreen(onTimeout = { showSplash = false })
        return
    }

    when (val state = authState) {
        is AuthState.LoggedOut -> {
            if (showRegisterScreen) {
                RegisterScreen(
                    viewModel = viewModel,
                    onBackToLogin = { showRegisterScreen = false }
                )
            } else {
                LoginScreen(
                    viewModel = viewModel,
                    onRegisterClick = { showRegisterScreen = true }
                )
            }
        }
        is AuthState.LoggedIn -> {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Campus Tracker",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = Color.White
                                )
                                // Online/Offline connection status badge
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isOffline) AcademicGold else Color(0xFF2E7D32))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                        .clickable { viewModel.toggleOfflineMode() }
                                ) {
                                    Icon(
                                        imageVector = if (isOffline) Icons.Default.WifiOff else Icons.Default.Wifi,
                                        contentDescription = "Status",
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = if (isOffline) "OFFLINE ($pendingSyncs)" else "ONLINE",
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = DeepBlue,
                            titleContentColor = Color.White
                        ),
                        actions = {
                            IconButton(onClick = { viewModel.logout() }) {
                                Icon(
                                    imageVector = Icons.Default.Logout,
                                    contentDescription = "Logout",
                                    tint = Color.White
                                )
                            }
                        }
                    )
                },
                bottomBar = {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 8.dp
                    ) {
                        // Dynamic bottom bar depending on role
                        val role = state.user.role

                        // Home Tab
                        NavigationBarItem(
                            selected = activeTab == "home",
                            onClick = { activeTab = "home" },
                            icon = {
                                Icon(
                                    imageVector = when (role) {
                                        "ADMIN" -> Icons.Default.AdminPanelSettings
                                        "LECTURER" -> Icons.Default.CoPresent
                                        else -> Icons.Default.Dashboard
                                    },
                                    contentDescription = "Home"
                                )
                            },
                            label = { Text(if (role == "ADMIN") "Registry" else if (role == "LECTURER") "Sessions" else "Dashboard") }
                        )

                        // Reports Tab
                        NavigationBarItem(
                            selected = activeTab == "reports",
                            onClick = { activeTab = "reports" },
                            icon = { Icon(imageVector = Icons.Default.Assessment, contentDescription = "Reports") },
                            label = { Text("Reports") }
                        )

                        // Alerts Tab
                        NavigationBarItem(
                            selected = activeTab == "alerts",
                            onClick = { activeTab = "alerts" },
                            icon = { Icon(imageVector = Icons.Default.Notifications, contentDescription = "Alerts") },
                            label = { Text("Alerts") }
                        )

                        // Profile Tab
                        NavigationBarItem(
                            selected = activeTab == "profile",
                            onClick = { activeTab = "profile" },
                            icon = { Icon(imageVector = Icons.Default.Person, contentDescription = "Profile") },
                            label = { Text("Profile") }
                        )

                        // Settings Tab
                        NavigationBarItem(
                            selected = activeTab == "settings",
                            onClick = { activeTab = "settings" },
                            icon = { Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings") },
                            label = { Text("Settings") }
                        )
                    }
                }
            ) { innerPadding ->
                Box(modifier = Modifier.padding(innerPadding)) {
                    when (activeTab) {
                        "home" -> {
                            when (state.user.role) {
                                "ADMIN" -> AdminDashboardScreen(viewModel = viewModel)
                                "LECTURER" -> LecturerDashboardScreen(viewModel = viewModel)
                                "STUDENT" -> StudentDashboardScreen(viewModel = viewModel)
                            }
                        }
                        "reports" -> ReportsScreen(viewModel = viewModel)
                        "alerts" -> NotificationsScreen(viewModel = viewModel)
                        "profile" -> ProfileScreen(viewModel = viewModel)
                        "settings" -> SettingsScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }
}
