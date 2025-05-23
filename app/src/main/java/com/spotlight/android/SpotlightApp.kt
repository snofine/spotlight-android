package com.spotlight.android

import android.app.Application
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.content.Intent
import android.provider.Settings
import android.net.Uri
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import android.graphics.drawable.BitmapDrawable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

data class AppInfo(
    val name: String,
    val icon: ImageBitmap?,
    val packageName: String
)

class SpotlightApp : Application() {
    private var installedApps: List<AppInfo> = emptyList()
    private var settingsItems: List<SearchItem> = emptyList()
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val appsMutex = Mutex()
    private var isAppsLoaded = false
    
    override fun onCreate() {
        super.onCreate()
        loadSettingsItems()
        loadInstalledAppsAsync()
    }
    
    private fun loadInstalledAppsAsync() {
        applicationScope.launch {
            withContext(Dispatchers.IO) {
                val pm = packageManager
                val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
                val loadedApps = apps.mapNotNull { app ->
                    try {
                        val name = pm.getApplicationLabel(app).toString()
                        val drawable = pm.getApplicationIcon(app)
                        val icon = if (drawable is BitmapDrawable) drawable.bitmap.asImageBitmap() else null
                        AppInfo(name, icon, app.packageName)
                    } catch (e: Exception) {
                        null
                    }
                }.sortedBy { it.name }
                
                appsMutex.withLock {
                    installedApps = loadedApps
                    isAppsLoaded = true
                }
            }
        }
    }
    
    private fun loadSettingsItems() {
        settingsItems = listOf(
            SearchItem(
                title = "Настройки Wi-Fi",
                action = { context ->
                    context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
                }
            ),
            SearchItem(
                title = "Настройки Bluetooth",
                action = { context ->
                    context.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS))
                }
            ),
            SearchItem(
                title = "Настройки звука",
                action = { context ->
                    context.startActivity(Intent(Settings.ACTION_SOUND_SETTINGS))
                }
            ),
            SearchItem(
                title = "Настройки дисплея",
                action = { context ->
                    context.startActivity(Intent(Settings.ACTION_DISPLAY_SETTINGS))
                }
            )
