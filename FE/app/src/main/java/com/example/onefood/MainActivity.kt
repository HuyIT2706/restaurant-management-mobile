package com.example.onefood

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.onefood.main.auth.ui.LoginScreen
import com.example.onefood.ui.theme.OneFoodTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OneFoodTheme {
                com.example.onefood.navigation.RoutingApp()
            }
        }
    }
}

@Composable
fun OneFoodApp() {
    com.example.onefood.navigation.RoutingApp()
}

@Preview(showBackground = true)
@Composable
fun OneFoodAppPreview() {
    OneFoodTheme {
        OneFoodApp()
    }
}

