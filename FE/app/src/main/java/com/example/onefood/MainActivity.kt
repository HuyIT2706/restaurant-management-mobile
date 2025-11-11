package com.example.onefood

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import android.net.Uri
import com.example.onefood.ui.theme.OneFoodTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Parse deep link from VNPay return: onefood://payment_result?payment=success&order_id=...
        val data: Uri? = intent?.data
        val deepStatus: String? = data?.getQueryParameter("payment")
        val deepOrderId: String? = data?.getQueryParameter("order_id")

        setContent {
            OneFoodTheme {
                com.example.onefood.navigation.RoutingApp(
                    deepLinkPaymentStatus = deepStatus,
                    deepLinkOrderId = deepOrderId
                )
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

