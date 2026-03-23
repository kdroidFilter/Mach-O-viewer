import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.github.terrakok.App
import com.github.terrakok.FileInbox
import io.github.kdroidfilter.nucleus.darkmodedetector.isSystemInDarkMode
import io.github.kdroidfilter.nucleus.graalvm.GraalVmInitializer
import io.github.kdroidfilter.nucleus.nativehttp.NativeHttpClient
import io.github.kdroidfilter.nucleus.systemcolor.systemAccentColor
import io.github.kdroidfilter.nucleus.updater.NucleusUpdater
import io.github.kdroidfilter.nucleus.updater.UpdateResult
import io.github.kdroidfilter.nucleus.updater.provider.GitHubProvider
import io.github.kdroidfilter.nucleus.window.jewel.JewelDecoratedWindow
import kotlinx.coroutines.launch
import org.jetbrains.jewel.foundation.BorderColors
import org.jetbrains.jewel.foundation.GlobalColors
import org.jetbrains.jewel.foundation.OutlineColors
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.intui.standalone.theme.IntUiTheme
import org.jetbrains.jewel.intui.standalone.theme.dark
import org.jetbrains.jewel.intui.standalone.theme.darkThemeDefinition
import org.jetbrains.jewel.intui.standalone.theme.default
import org.jetbrains.jewel.intui.standalone.theme.light
import org.jetbrains.jewel.intui.standalone.theme.lightThemeDefinition
import org.jetbrains.jewel.ui.ComponentStyling
import java.awt.Desktop
import java.awt.Dimension
import java.io.File

private val updater = NucleusUpdater {
    provider = GitHubProvider(owner = "kdroidFilter", repo = "Mach-O-viewer")
    httpClient = NativeHttpClient.create()
}

fun main(args: Array<String>) {
    GraalVmInitializer.initialize()
    args.firstOrNull()?.let { FileInbox.send(it) }
    if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.APP_OPEN_FILE)) {
        Desktop.getDesktop().setOpenFileHandler { event ->
            event.files.firstOrNull()?.let { file ->
                FileInbox.send(file.absolutePath)
            }
        }
    }
    application {
        val scope = rememberCoroutineScope()
        var pendingInstaller by remember { mutableStateOf<File?>(null) }

        // Silent background update check
       LaunchedEffect(Unit) {
            if (!updater.isUpdateSupported()) return@LaunchedEffect
            val result = updater.checkForUpdates()
            if (result is UpdateResult.Available) {
                updater.downloadUpdate(result.info).collect { progress ->
                    if (progress.file != null) {
                        pendingInstaller = progress.file
                    }
                }
            }
        }

        val systemIsDark = isSystemInDarkMode()
        val accent = systemAccentColor()
        val theme = if (systemIsDark) {
            if (accent != null) {
                JewelTheme.darkThemeDefinition(colors = accentGlobalColors(accent, isDark = true))
            } else {
                JewelTheme.darkThemeDefinition()
            }
        } else {
            if (accent != null) {
                JewelTheme.lightThemeDefinition(colors = accentGlobalColors(accent, isDark = false))
            } else {
                JewelTheme.lightThemeDefinition()
            }
        }
        IntUiTheme(
            theme = theme,
            styling = ComponentStyling.default(),
        ) {
            val windowState = rememberWindowState(width = 1300.dp, height = 900.dp)
            JewelDecoratedWindow(
                title = "Mach-O viewer",
                state = windowState,
                onCloseRequest = {
                    val installer = pendingInstaller
                    if (installer != null) {
                        scope.launch { updater.installAndQuit(installer) }
                    } else {
                        exitApplication()
                    }
                },
            ) {
                window.minimumSize = Dimension(1300, 900)
                App()
            }
        }
    }
}

private fun accentGlobalColors(accent: Color, isDark: Boolean): GlobalColors =
    if (isDark) {
        GlobalColors.dark(
            borders = BorderColors.dark(focused = accent),
            outlines = OutlineColors.dark(focused = accent),
        )
    } else {
        GlobalColors.light(
            borders = BorderColors.light(focused = accent),
            outlines = OutlineColors.light(focused = accent),
        )
    }
