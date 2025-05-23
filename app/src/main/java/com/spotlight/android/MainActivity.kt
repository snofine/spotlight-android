package com.spotlight.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.ui.unit.dp
import com.spotlight.android.ui.SearchScreen
import android.content.Intent
import android.content.pm.PackageManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val spotlightApp = application as SpotlightApp
        
        setContent {
            MaterialTheme {
                Surface {
                    var isLoading by remember { mutableStateOf(true) }
                    val coroutineScope = rememberCoroutineScope()
                    
                    LaunchedEffect(Unit) {
                        coroutineScope.launch {
                            // Ждем загрузки приложений
                            while (spotlightApp.getInstalledApps().isEmpty()) {
                                delay(100)
                            }
                            isLoading = false
                        }
                    }
                    
                    if (isLoading) {
                        LoadingScreen()
                    } else {
                        SearchScreen(
                            apps = spotlightApp.getInstalledApps(),
                            settingsItems = spotlightApp.getSettingsItems(),
                            onAppSelected = { appInfo ->
                                launchApp(appInfo.packageName)
                            },
                            onSettingsItemSelected = { item ->
                                item.action(this)
                            },
                            onWebSearch = { query ->
                                spotlightApp.searchWeb(query)
                            }
                        )
                    }
                }
            }
        }
    }
    
    private fun launchApp(packageName: String) {
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }
}

@Composable
fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.he