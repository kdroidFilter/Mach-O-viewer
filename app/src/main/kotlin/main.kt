import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.github.terrakok.App
import com.github.terrakok.FileInbox
import io.github.kdroidfilter.nucleus.core.runtime.DeepLinkHandler
import io.github.kdroidfilter.nucleus.darkmodedetector.isSystemInDarkMode
import io.github.kdroidfilter.nucleus.graalvm.GraalVmInitializer
import io.github.kdroidfilter.nucleus.window.jewel.JewelDecoratedWindow
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.intui.standalone.theme.IntUiTheme
import org.jetbrains.jewel.intui.standalone.theme.darkThemeDefinition
import org.jetbrains.jewel.intui.standalone.theme.default
import org.jetbrains.jewel.intui.standalone.theme.lightThemeDefinition
import org.jetbrains.jewel.ui.ComponentStyling
import java.awt.Dimension
fun main(args: Array<String>) {
    GraalVmInitializer.initialize()

    DeepLinkHandler.register(args) { uri ->
        val path = uri.path ?: uri.toString()
        FileInbox.send(path)
    }

    // Handle file passed as initial argument
    val initialUri = DeepLinkHandler.uri
    if (initialUri != null) {
        val path = initialUri.path ?: initialUri.toString()
        FileInbox.send(path)
    } else {
        args.firstOrNull()?.let { FileInbox.send(it) }
    }

    application {
        val systemIsDark = isSystemInDarkMode()
        val theme = if (systemIsDark) JewelTheme.darkThemeDefinition()
        else JewelTheme.lightThemeDefinition()
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
