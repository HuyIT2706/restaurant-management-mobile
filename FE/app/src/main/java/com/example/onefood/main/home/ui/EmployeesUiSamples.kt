package com.example.onefood.main.home.ui


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.onefood.R
import com.example.onefood.ui.theme.RedPrimary

// File UI tĩnh mẫu cho màn Quản lý nhân viên.
// Không gọi API, chỉ để preview / copy-paste.

data class SampleEmployee(
    val id: Int,
    val fullname: String,
    val phone: String,
    val position: String,
    val salary: String,
    val isActive: Boolean,
    val imageUrl: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeesStaticListScreen(
    onBackPress: () -> Unit = {}
) {
    // Make list mutable so sample UI can delete items
    val samples = remember {
        mutableStateListOf(
            SampleEmployee(1, "Lê Thị Tuyết Băng", "0123456789", "Nhân viên", "5.000.000", true, null),
            SampleEmployee(2, "Kiều Trần Thu Uyên", "0123456789", "Nhân viên", "5.000.000", false, null),
            SampleEmployee(3, "Bùi Văn Huy", "0123456789", "Quản lí", "5.000.000", true, null)
        )
    }

    var selectedEmployee by remember { mutableStateOf<SampleEmployee?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            // Center the title
            CenterAlignedTopAppBar(
                title = { Text("Nhân viên", fontWeight = FontWeight.Medium) },
                navigationIcon = {
                    IconButton(onClick = onBackPress) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = Color.Black
                        )
                    }
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .size(44.dp)
                            .background(Color(0xFFFF4444), shape = CircleShape)
                            .clickable { showAddDialog = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Thêm",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Tìm kiếm") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Tìm") },
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    unfocusedContainerColor = Color(0xFFF0F0F0),
                    focusedContainerColor = Color(0xFFF0F0F0),
                    cursorColor = RedPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                singleLine = true
            )

            Spacer(Modifier.height(16.dp))

            val filteredSamples = if (searchQuery.isEmpty()) {
                samples
            } else {
                samples.filter { 
                    it.fullname.contains(searchQuery, ignoreCase = true) ||
                    it.phone.contains(searchQuery, ignoreCase = true) ||
                    it.position.contains(searchQuery, ignoreCase = true)
                }
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(filteredSamples, key = { it.id }) { s ->
                    EmployeeListItemSample(
                        employee = s,
                        onClick = {
                            selectedEmployee = s
                            showEditDialog = true
                        },
                        onEdit = {
                            selectedEmployee = s
                            showEditDialog = true
                        },
                        onDelete = {
                            selectedEmployee = s
                            showDeleteDialog = true
                        }
                    )
                }
            }
        }

        // Show add dialog
        if (showAddDialog) {
            AddEmployeeStaticDialog(
                onDismiss = {
                    showAddDialog = false
                },
                onRegister = { item ->
                    val newId = (samples.maxOfOrNull { it.id } ?: 0) + 1
                    samples.add(item.copy(id = newId))
                    showAddDialog = false
                }
            )
        }

        // Edit dialog (show details + save)
        if (showEditDialog && selectedEmployee != null) {
            EditEmployeeStaticDialog(
                employee = selectedEmployee!!,
                onDismiss = { showEditDialog = false; selectedEmployee = null },
                onSave = { updated ->
                    val index = samples.indexOfFirst { it.id == updated.id }
                    if (index != -1) samples[index] = updated
                    showEditDialog = false
                    selectedEmployee = null
                }
            )
        }

        // Delete confirmation
        if (showDeleteDialog && selectedEmployee != null) {
            DeleteEmployeeStaticDialog(onDismiss = { showDeleteDialog = false; selectedEmployee = null }, onConfirm = {
                samples.removeAll { it.id == selectedEmployee?.id }
                showDeleteDialog = false
                selectedEmployee = null
            })
        }
    }
}

@Composable
private fun EmployeeListItemSample(
    employee: SampleEmployee,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(12.dp))
            .border(1.dp, Color.Black, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Image(
                painter = painterResource(id = R.drawable.avatarsignin),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
            )

            // Employee Info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = employee.fullname,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                
                // Position Tag
                Box(
                    modifier = Modifier
                        .background(
                            Color(0xFFE0E0E0),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = employee.position,
                        fontSize = 12.sp,
                        color = Color.Black
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = employee.phone,
                    fontSize = 14.sp,
                    color = Color.Black
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_money),
                        contentDescription = null,
                        tint = Color(0xFF757575),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "${employee.salary}đ",
                        fontSize = 14.sp,
                        color = Color(0xFF757575)
                    )
                }
            }
            
            // Action Buttons
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Edit Button
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            Color.White,
                            shape = RoundedCornerShape(6.dp)
                        )
                        .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(6.dp))
                        .clickable(onClick = onEdit),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_update),
                        contentDescription = "Chỉnh sửa",
                        tint = Color.Black,
                        modifier = Modifier.size(18.dp)
                    )
                }
                
                // Delete Button
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            RedPrimary,
                            shape = RoundedCornerShape(6.dp)
                        )
                        .clickable(onClick = onDelete),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_trash),
                        contentDescription = "Xóa",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// --- Dialog Thêm (tĩnh) ---
@Composable
fun AddEmployeeStaticDialog(onDismiss: () -> Unit, onRegister: (SampleEmployee) -> Unit) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("") }
    var salary by remember { mutableStateOf("") }
    var active by remember { mutableStateOf(true) }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("THÊM NHÂN VIÊN MỚI", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = "Close") }
                }

                Spacer(Modifier.height(12.dp))

                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.avatarsignin),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Tên đầy đủ") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Số điện thoại") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = role, onValueChange = { role = it }, label = { Text("Chức vụ") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = salary, onValueChange = { salary = it }, label = { Text("Lương") }, modifier = Modifier.fillMaxWidth(), singleLine = true)

                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Trạng thái:")
                    Switch(
                        checked = active,
                        onCheckedChange = { active = it },
                        colors = SwitchDefaults.colors(
                            checkedTrackColor = Color(0xFF00E676),
                            uncheckedTrackColor = Color(0xFFFF1744),
                            checkedThumbColor = Color.White,
                            uncheckedThumbColor = Color.White
                        )
                    )
                }

                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = {
                        onRegister(SampleEmployee(0, name, phone, role, salary, active, null))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828))
                ) {
                    Text("Đăng kí", color = Color.White)
                }
            }
        }
    }
}

// --- Dialog Xoá (tĩnh) ---
@Composable
fun DeleteEmployeeStaticDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(12.dp), modifier = Modifier.padding(16.dp)) {
            Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("XOÁ NHÂN VIÊN", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text("Bạn có chắc chắn muốn xoá nhân viên này ?")
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828))) { Text("OK", color = Color.White) }
                    Button(onClick = onDismiss) { Text("HUỶ") }
                }
            }
        }
    }
}

// --- Dialog Sửa (tĩnh) ---
@Composable
fun EditEmployeeStaticDialog(
    employee: SampleEmployee,
    onDismiss: () -> Unit,
    onSave: (SampleEmployee) -> Unit
) {
    var name by remember { mutableStateOf(employee.fullname) }
    var phone by remember { mutableStateOf(employee.phone) }
    var role by remember { mutableStateOf(employee.position) }
    var salary by remember { mutableStateOf(employee.salary) }
    var active by remember { mutableStateOf(employee.isActive) }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("CHỈNH SỬA THÔNG TIN", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = "Close") }
                }

                Spacer(Modifier.height(12.dp))

                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.avatarsignin),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Tên đầy đủ") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Số điện thoại") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = role, onValueChange = { role = it }, label = { Text("Chức vụ") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = salary, onValueChange = { salary = it }, label = { Text("Lương") }, modifier = Modifier.fillMaxWidth(), singleLine = true)

                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Trạng thái:")
                    Switch(
                        checked = active,
                        onCheckedChange = { active = it },
                        colors = SwitchDefaults.colors(
                            checkedTrackColor = Color(0xFF00E676),
                            uncheckedTrackColor = Color(0xFFFF1744),
                            checkedThumbColor = Color.White,
                            uncheckedThumbColor = Color.White
                        )
                    )
                }

                Spacer(Modifier.height(16.dp))
                Button(onClick = {
                    onSave(employee.copy(fullname = name, phone = phone, position = role, salary = salary, isActive = active, imageUrl = null))
                }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828))) {
                    Text("Lưu thông tin", color = Color.White)
                }
            }
        }
    }
}

// --- Previews ---
@Preview(showBackground = true)
@Composable
fun PreviewEmployeesList() {
    EmployeesStaticListScreen()
}

@Preview(showBackground = true)
@Composable
fun PreviewAddDialog() {
    AddEmployeeStaticDialog(onDismiss = {}, onRegister = { _ -> })
}

@Preview(showBackground = true)
@Composable
fun PreviewDeleteDialog() {
    DeleteEmployeeStaticDialog(onDismiss = {}, onConfirm = {})
}

@Preview(showBackground = true)
@Composable
fun PreviewEditDialog() {
    EditEmployeeStaticDialog(employee = SampleEmployee(1, "Nguyễn Văn A", "0987654321", "Nhân viên", "5.000.000", true, null), onDismiss = {}, onSave = { _ -> })
}
