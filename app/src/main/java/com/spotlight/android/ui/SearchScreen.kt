package com.spotlight.android.ui

import android.content.pm.ResolveInfo
import android.graphics.drawable.BitmapDrawable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.Image
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.spotlight.android.SearchItem
import com.spotlight.android.AppInfo

sealed class SearchableItem {
    data class App(val appInfo: AppInfo) : SearchableItem()
    data class Setting(val searchItem: SearchItem) : SearchableItem()
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SearchScreen(
    apps: List<AppInfo>,
    settingsItems: List<SearchItem>,
    onAppSelected: (AppInfo) -> Unit,
    onSettingsItemSelected: (SearchItem) -> Unit,
    onWebSearch: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val context = LocalContext.current

    val allItems = remember(apps, settingsItems) {
        apps.map { SearchableItem.App(it) } + settingsItems.map { SearchableItem.Setting(it) }
    }

    val filteredItems = remember(searchQuery, allItems) {
        if (searchQuery.isEmpty()) {
            allItems
        } else {
            allItems.filter { item ->
                when (item) {
                    is SearchableItem.App -> item.appInfo.name.contains(searchQuery, ignoreCase = true)
                    is SearchableItem.Setting -> item.searchItem.title.contains(searchQuery, ignoreCase = true)
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            placeholder = { Text("Поиск приложений, настроек или в интернете...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Поиск") },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Очистить")
                    }
                }
            }
        )

        LazyColumn {
            if (searchQuery.isNotEmpty()) {
                // Если введен URL, показываем кнопку для перехода на сайт
                if (searchQuery.startsWith("http://") || searchQuery.startsWith("https://")) {
                    item {
                        Button(
                            onClick = { onWebSearch(searchQuery) },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Перейти на сайт: ${searchQuery.take(30)}...")
                        }
                    }
                } else {
                    // Показываем кнопку поиска в Google всегда первой
                    item {
                        Button(
                            onClick = { onWebSearch(searchQuery) },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.Search, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Поиск в Google: $searchQuery")
                        }
                    }
                }
            }

            items(filteredItems) { item ->
                when (item) {
                    is SearchableItem.App -> AppItem(
                        app = item.appInfo,
                        onClick = { onAppSelected(item.appInfo) }
                    )
                    is SearchableItem.Setting -> SettingsItem(
                        item = item.searchItem,
                        onClick = { onSettingsItemSelected(item.searchItem) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SettingsItem(item: SearchItem, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), onClick = onClick) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = null,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = item.title, style = MaterialTheme.typography.h6)
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AppItem(app: AppInfo, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), onClick = onClick) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (app.icon != null) {
                Image(
                    bitmap = app.icon,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = app.name,
                style = MaterialTheme.typography.h6
            )
        }
    }
}
