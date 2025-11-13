package com.example.onefood.main.home.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.widget.Toast
import java.net.HttpURLConnection
import java.net.URL
import android.content.Context
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.onefood.R
import com.example.onefood.ui.theme.OneFoodTheme
import com.example.onefood.core.components.BottomTabBar

@Composable
fun UserScreenTopBar(navController: NavController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 50.dp, start = 15.dp, end = 15.dp, bottom = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { navController.popBackStack() }) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Quay lại",
                tint = Color.Black,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = "My Profile",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
    }
}

@Composable
fun UserScreen(navController: NavController) {
    Scaffold(
        topBar = { UserScreenTopBar(navController) },
        bottomBar = { BottomTabBar(navController) },
        modifier = Modifier.fillMaxSize().background(Color.White)
    ) { paddingValues ->
    val coroutineScope = rememberCoroutineScope()

    Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            val context = LocalContext.current
            val prefs = context.getSharedPreferences("onefood_prefs", Context.MODE_PRIVATE)
            val userName = prefs.getString("user_name", "Người dùng") ?: "Người dùng"
            val userPhone = prefs.getString("user_phone", "") ?: ""

            // Profile Information Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar with camera icon
                Box(
                    modifier = Modifier.size(80.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(Color.Gray.copy(alpha = 0.3f))
                    )
                    // Camera icon overlay
                    IconButton(
                        onClick = { /* Change profile picture */ },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(28.dp)
                            .background(Color.White, shape = CircleShape)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_logo_cart),
                            contentDescription = "Camera",
                            tint = Color.Black,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = userName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Button(
                        onClick = { navController.navigate("edit_user") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.width(120.dp)
                    ) {
                        Text(
                            text = "Xem thông tin",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Account Actions Section
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                AccountActionItem(
                    icon = Icons.Default.Delete,
                    text = "Clear Cache",
                    onClick = { /* Clear cache */ }
                )
                AccountActionItem(
                    icon = Icons.Default.ExitToApp,
                    text = "Log Out",
                    onClick = {
                        coroutineScope.launch {
                            val prefs = context.getSharedPreferences("onefood_prefs", Context.MODE_PRIVATE)
                            val token = prefs.getString("jwt_token", "") ?: ""
                            val baseUrl = "http://10.0.2.2/BeMobile/BE/"
                            var serverMessage: String? = null
                            // Attempt to call backend logout endpoint (best-effort)
                            try {
                                withContext(Dispatchers.IO) {
                                    val url = URL(baseUrl + "logout.php")
                                    val conn = (url.openConnection() as HttpURLConnection)
                                    conn.requestMethod = "POST"
                                    conn.connectTimeout = 5000
                                    conn.readTimeout = 5000
                                    if (!token.isNullOrEmpty()) {
                                        conn.setRequestProperty("Authorization", "Bearer $token")
                                    }
                                    try {
                                        val code = conn.responseCode
                                        val body = conn.inputStream.bufferedReader().use { it.readText() }
                                        serverMessage = body
                                    } finally {
                                        conn.disconnect()
                                    }
                                }
                            } catch (e: Exception) {
                                // ignore network failure for logout; we'll still clear local session
                                serverMessage = null
                            }

                            // Clear stored session info
                            prefs.edit()
                                .remove("jwt_token")
                                .remove("user_id")
                                .remove("user_name")
                                .remove("user_phone")
                                .remove("user_role")
                                .apply()

                            // Navigate back to login and clear backstack
                            navController.navigate("login") {
                                popUpTo(navController.graph.startDestinationId) {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }

                            // Show feedback
                            withContext(Dispatchers.Main) {
                                if (serverMessage != null) {
                                    Toast.makeText(context, "Đăng xuất thành công", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Đã đăng xuất (offline)", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun AccountActionItem(icon: ImageVector, text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = Color.Black,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = text,
            fontSize = 16.sp,
            color = Color.Black
        )
    }
}

@Preview(showBackground = true)
@Composable
fun UserScreenPreview() {
    OneFoodTheme {
        UserScreen(navController = rememberNavController())
    }
}
