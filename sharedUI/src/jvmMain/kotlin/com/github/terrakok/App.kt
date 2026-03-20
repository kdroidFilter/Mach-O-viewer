package com.github.terrakok

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.github.terrakok.theme.AppTheme
import kotlinx.coroutines.launch

@Composable
fun App() = AppTheme {
    var fileName: String? by remember { mutableStateOf(null) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val fileService = remember { FileService() }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            if (fileName == null) {
                FileDropScreen(
                    onFileDropped = { droppedPath ->
                        scope.launch {
                            if (fileService.isMachO(droppedPath)) {
                                fileName = droppedPath
                            } else {
                                fileName = null
                                snackbarHostState.showSnackbar("Wrong file type.")
                            }
                        }
                    }
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Dropped file:",
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = fileName!!,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(24.dp))
                    Button(
                        onClick = { fileName = null }
                    ) {
                        Text("Clear")
                    }
                }
            }
        }
    }
}

