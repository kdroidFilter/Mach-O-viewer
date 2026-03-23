import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.github.terrakok.App
import com.github.terrakok.FileInbox
import io.github.kdroidfilter.nucleus.darkmodedetector.isSystemInDarkMode
import io.github.kdroidfilter.nucleus.graalvm.GraalVmInitializer
import io.github.kdroidfilter.nucleus.systemcolor.systemAccentColor
import io.github.kdroidfilter.nucleus.window.jewel.JewelDecoratedWindow
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
                onCloseRequest = ::exitApplication,
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
