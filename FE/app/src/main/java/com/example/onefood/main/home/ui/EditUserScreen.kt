package com.example.onefood.main.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.onefood.R
import com.example.onefood.ui.theme.OneFoodTheme
import com.example.onefood.ui.theme.RedPrimary
import com.example.onefood.core.components.BottomTabBar

@Composable
fun EditUserScreenTopBar(navController: NavController, onSave: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 50.dp, start = 15.dp, end = 15.dp, bottom = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
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
        Text(
            text = "Edit Profile",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        IconButton(onClick = onSave) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Lưu",
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun EditUserScreen(navController: NavController) {
    var firstName by remember { mutableStateOf("Lê") }
    var lastName by remember { mutableStateOf("Thị Tuyết Băng") }
    var phone by remember { mutableStateOf("+84 01234567890") }
    var gender by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            EditUserScreenTopBar(
                navController = navController,
                onSave = {
                    // Save user info
                    navController.popBackStack()
                }
            )
        },
        bottomBar = { BottomTabBar(navController) },
        modifier = Modifier.fillMaxSize().background(Color.White)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            // Profile Picture
            Box(
                modifier = Modifier.size(120.dp)
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
                        .size(36.dp)
                        .background(Color.White, shape = CircleShape)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_logo_cart),
                        contentDescription = "Camera",
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Your Information Section
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Your Information",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                
                // First name
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "First name",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                    OutlinedTextField(
                        value = firstName,
                        onValueChange = { firstName = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RedPrimary,
                            unfocusedBorderColor = Color.Gray,
                            focusedContainerColor = Color(0xFFF0F0F0),
                            unfocusedContainerColor = Color(0xFFF0F0F0)
                        )
                    )
                }
                
                // Last name
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Last name",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                    OutlinedTextField(
                        value = lastName,
                        onValueChange = { lastName = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RedPrimary,
                            unfocusedBorderColor = Color.Gray,
                            focusedContainerColor = Color(0xFFF0F0F0),
                            unfocusedContainerColor = Color(0xFFF0F0F0)
                        )
                    )
                }
                
                // Phone
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Phone",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RedPrimary,
                            unfocusedBorderColor = Color.Gray,
                            focusedContainerColor = Color(0xFFF0F0F0),
                            unfocusedContainerColor = Color(0xFFF0F0F0)
                        )
                    )
                }
                
                // Gender
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Gender",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                    OutlinedTextField(
                        value = gender,
                        onValueChange = { gender = it },
                        placeholder = { Text("Chọn giới tính") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RedPrimary,
                            unfocusedBorderColor = Color.Gray,
                            focusedContainerColor = Color(0xFFF0F0F0),
                            unfocusedContainerColor = Color(0xFFF0F0F0)
                        )
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EditUserScreenPreview() {
    OneFoodTheme {
        EditUserScreen(navController = rememberNavController())
    }
}
