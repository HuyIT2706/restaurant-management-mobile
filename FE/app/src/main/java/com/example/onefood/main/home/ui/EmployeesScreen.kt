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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.onefood.R
import com.example.onefood.data.model.User
import com.example.onefood.main.home.viewmodel.UserViewModel
import com.example.onefood.ui.theme.RedPrimary
import com.example.onefood.ui.theme.TableGreen
import com.example.onefood.ui.theme.colorCategory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeesScreen(
    viewModel: UserViewModel = androidx.hilt.navigation.compose.hiltViewModel(),
    token: String = "", // Nhận token từ parameter hoặc SharedPreference
    onBackPress: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    var selectedEmployee by remember { mutableStateOf<User?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf<Int?>(null) } // null = tất cả, 1 = hoạt động, 0 = bị khóa

    // Load users khi screen khởi tạo
    LaunchedEffect(Unit) {
        if (token.isNotEmpty()) {
            viewModel.loadUsers(token)
        }
    }

    val context = LocalContext.current
    // Show toast for success and clear messages after 3s
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            kotlinx.coroutines.delay(3000)
            viewModel.clearMessages()
        }
    }
    // Clear error messages after 3s (toast removed, only show banner)
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            kotlinx.coroutines.delay(3000)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
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
            // Thông báo lỗi/thành công
            if (uiState.error != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFF5252))
                ) {
                    Text(
                        text = uiState.error ?: "",
                        modifier = Modifier.padding(12.dp),
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }
            }

            // success messages shown as Toast (handled above)

            // Search bar and Status filter - Always visible at top
            var expanded by remember { mutableStateOf(false) }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Search bar
                    var _searchQuery by remember { mutableStateOf(searchQuery) }
                    OutlinedTextField(
                        value = _searchQuery,
                        onValueChange = {
                            _searchQuery = it
                            searchQuery = it
                            viewModel.updateSearchQuery(it)
                        },
                        placeholder = { Text("Tìm kiếm nhân viên...") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = "Tìm kiếm",
                                tint = Color.Gray,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        shape = RoundedCornerShape(20.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            unfocusedContainerColor = Color(0xFFF5F5F5),
                            focusedContainerColor = Color(0xFFF5F5F5),
                            cursorColor = RedPrimary
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 5.dp, top = 10.dp, bottom = 10.dp),
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp, color = Color.Black)
                    )

                    // Filter button
                    Box(modifier = Modifier.wrapContentSize()) {
                        IconButton(
                            onClick = { expanded = true },
                            modifier = Modifier
                                .size(50.dp)
                                .background(
                                    color = if (selectedStatus != null) RedPrimary else Color(0xFFF5F5F5),
                                    shape = RoundedCornerShape(20.dp)
                                )
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_filter),
                                contentDescription = "Lọc theo trạng thái",
                                tint = if (selectedStatus != null) Color.White else Color.Black,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.widthIn(max = 200.dp)
                        ) {
                            // Option: Tất cả
                            DropdownMenuItem(
                                text = { 
                                    Text(
                                        "Tất cả",
                                        fontSize = 14.sp,
                                        fontWeight = if (selectedStatus == null) FontWeight.Medium else FontWeight.Normal
                                    ) 
                                },
                                onClick = {
                                    selectedStatus = null
                                    expanded = false
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Filled.Clear,
                                        contentDescription = null,
                                        tint = if (selectedStatus == null) RedPrimary else Color.Gray,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            )
                            
                            // Option: Hoạt động
                            DropdownMenuItem(
                                text = { 
                                    Text(
                                        "Hoạt động",
                                        fontSize = 14.sp,
                                        fontWeight = if (selectedStatus == 1) FontWeight.Medium else FontWeight.Normal
                                    ) 
                                },
                                onClick = {
                                    selectedStatus = 1
                                    expanded = false
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Filled.Check,
                                        contentDescription = null,
                                        tint = if (selectedStatus == 1) RedPrimary else Color.Transparent,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            )
                            
                            // Option: Bị khóa
                            DropdownMenuItem(
                                text = { 
                                    Text(
                                        "Bị khóa",
                                        fontSize = 14.sp,
                                        fontWeight = if (selectedStatus == 0) FontWeight.Medium else FontWeight.Normal
                                    ) 
                                },
                                onClick = {
                                    selectedStatus = 0
                                    expanded = false
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Filled.Check,
                                        contentDescription = null,
                                        tint = if (selectedStatus == 0) RedPrimary else Color.Transparent,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            )
                        }
                    }
                }
                
                // Show selected status badge
                if (selectedStatus != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = RedPrimary.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .clickable {
                                    selectedStatus = null
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (selectedStatus == 1) "Hoạt động" else "Bị khóa",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = RedPrimary
                                )
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Xóa lọc",
                                    modifier = Modifier.size(14.dp),
                                    tint = RedPrimary
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Loading state
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = RedPrimary)
                }
            } else {
                // Filter users using remember (like ProductScreen)
                val filteredUsers = remember(uiState.users, searchQuery, selectedStatus) {
                    var filtered = uiState.users
                    
                    // Filter by status
                    if (selectedStatus != null) {
                        filtered = filtered.filter { user ->
                            (user.status ?: 0) == selectedStatus
                        }
                    }
                    
                    // Filter by search query
                    if (searchQuery.isNotEmpty()) {
                        val q = searchQuery.lowercase()
                        filtered = filtered.filter { user ->
                            (user.fullname ?: "").lowercase().contains(q) ||
                                    (user.phone ?: "").lowercase().contains(q) ||
                                    (user.role ?: "").lowercase().contains(q) ||
                                    (user.gender ?: "").lowercase().contains(q)
                        }
                    }
                    
                    filtered
                }

                if (filteredUsers.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Không có nhân viên nào", color = Color.Gray)
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(filteredUsers, key = { it.userId ?: 0 }) { user ->
                            EmployeeListItem(
                                employee = user,
                                onClick = {
                                    selectedEmployee = user
                                    showEditDialog = true
                                },
                                onEdit = {
                                    selectedEmployee = user
                                    showEditDialog = true
                                },
                                onDelete = {
                                    selectedEmployee = user
                                    showDeleteDialog = true
                                }
                            )
                        }
                    }
                }
            }
        }

        // Add dialog
        if (showAddDialog) {
            AddEmployeeDialog(
                token = token,
                viewModel = viewModel,
                onDismiss = {
                    showAddDialog = false
                }
            )
        }

        // Edit dialog
        if (showEditDialog && selectedEmployee != null) {
            EditEmployeeDialog(
                employee = selectedEmployee!!,
                token = token,
                viewModel = viewModel,
                onDismiss = {
                    showEditDialog = false
                    selectedEmployee = null
                }
            )
        }

        // Delete confirmation
        if (showDeleteDialog && selectedEmployee != null) {
            DeleteEmployeeDialog(
                onDismiss = {
                    showDeleteDialog = false
                    selectedEmployee = null
                },
                onConfirm = {
                    selectedEmployee?.userId?.let { userId ->
                        viewModel.deleteUser(userId, token)
                    }
                    showDeleteDialog = false
                    selectedEmployee = null
                }
            )
        }
    }
}

@Composable
private fun EmployeeListItem(
    employee: User,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(12.dp))
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
                painter = painterResource(id = R.drawable.img_avt),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(50.dp))
            )

            // Employee Info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = employee.fullname ?: "",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )


                Text(
                    text = employee.role ?: "",
                    fontSize = 16.sp,
                    color = TableGreen,
                    fontWeight = FontWeight.Bold

                )

                // Show gender (if available)
                if (!employee.gender.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = employee.gender ?: "",
                        fontSize = 16.sp,
                        color = RedPrimary
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = employee.phone ?: "",
                    fontSize = 16.sp,
                    color = Color.Black
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_money),
                        contentDescription = null,
                        tint = colorCategory,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "${employee.wage ?: 0.0}đ",
                        fontSize = 16.sp,
                        color = colorCategory,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Action buttons: Edit, Delete, Status
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color.White, shape = RoundedCornerShape(6.dp))
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

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(RedPrimary, shape = RoundedCornerShape(6.dp))
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

                val statusColor = if ((employee.status ?: 0) == 1) Color(0xFF00C853) else Color(0xFFD32F2F)
                val statusText = if ((employee.status ?: 0) == 1) "Hoạt động" else "Bị khóa"
                Box(
                    modifier = Modifier
                        .width(92.dp)
                        .height(32.dp)
                        .background(statusColor, shape = RoundedCornerShape(8.dp))
                        .clickable(enabled = false) { /* no-op for now */ },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = statusText,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun AddEmployeeDialog(
    token: String,
    viewModel: UserViewModel,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var wage by remember { mutableStateOf("") }
    var active by remember { mutableStateOf(true) }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth(0.9f)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("THÊM NHÂN VIÊN MỚI", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Tên đầy đủ") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Số điện thoại") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = role,
                    onValueChange = { role = it },
                    label = { Text("Chức vụ (Order, QuanLy, ThuNgan)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = gender,
                    onValueChange = { gender = it },
                    label = { Text("Giới tính (Nam, Nữ)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = wage,
                    onValueChange = { wage = it },
                    label = { Text("Lương") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

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
                            uncheckedTrackColor = Color(0xFFFF1744)
                        )
                    )
                }

                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = {
                        if (name.isNotEmpty() && phone.isNotEmpty() && role.isNotEmpty()) {
                            viewModel.addUser(
                                fullname = name,
                                phone = phone,
                                gender = if (gender.isBlank()) null else gender,
                                role = role,
                                wage = wage.toDoubleOrNull() ?: 0.0,
                                status = if (active) 1 else 0,
                                image = null,
                                token = token
                            )
                            onDismiss()
                        }
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

@Composable
private fun EditEmployeeDialog(
    employee: User,
    token: String,
    viewModel: UserViewModel,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(employee.fullname ?: "") }
    var phone by remember { mutableStateOf(employee.phone ?: "") }
    var role by remember { mutableStateOf(employee.role ?: "") }
    var gender by remember { mutableStateOf(employee.gender ?: "") }
    var wage by remember { mutableStateOf(employee.wage?.toString() ?: "") }
    var active by remember { mutableStateOf((employee.status ?: 0) == 1) }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth(0.9f)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("CHỈNH SỬA THÔNG TIN", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Tên đầy đủ") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Số điện thoại") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = role,
                    onValueChange = { role = it },
                    label = { Text("Chức vụ (Order, QuanLy, ThuNgan)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = gender,
                    onValueChange = { gender = it },
                    label = { Text("Giới tính (Nam, Nữ)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = wage,
                    onValueChange = { wage = it },
                    label = { Text("Lương") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

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
                            uncheckedTrackColor = Color(0xFFFF1744)
                        )
                    )
                }

                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = {
                        if (name.isNotEmpty() && phone.isNotEmpty() && role.isNotEmpty()) {
                            employee.userId?.let { userId ->
                                viewModel.updateUser(
                                    userId = userId,
                                    fullname = name,
                                    phone = phone,
                                    gender = if (gender.isBlank()) null else gender,
                                    role = role,
                                    wage = wage.toDoubleOrNull() ?: 0.0,
                                    status = if (active) 1 else 0,
                                    image = null,
                                    token = token
                                )
                                onDismiss()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828))
                ) {
                    Text("Lưu thông tin", color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun DeleteEmployeeDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(12.dp), modifier = Modifier.padding(16.dp)) {
            Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("XOÁ NHÂN VIÊN", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text("Bạn có chắc chắn muốn xoá nhân viên này ?")
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Button(
                        onClick = onConfirm,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828))
                    ) {
                        Text("OK", color = Color.White)
                    }
                    Button(onClick = onDismiss) {
                        Text("HUỶ")
                    }
                }
            }
        }
    }
}