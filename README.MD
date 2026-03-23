# Mach-O Viewer (Nucleus fork)

Fork of [terrakok/Mach-O-viewer](https://github.com/terrakok/Mach-O-viewer) powered by [Nucleus](https://github.com/kdroidFilter/Nucleus).

Additional features over the original:
- GraalVM Native Image support (faster startup, lower memory)
- Reactive dark mode detection (follows system theme changes)
- System accent color integration (UI adapts to macOS accent color)
- File associations for `.dylib`, `.o`, `.a`, and `.bundle` files

![App Screenshot](media/app.png)

## Installation

Run the install script:

```bash
curl -fsSL https://raw.githubusercontent.com/kdroidFilter/Mach-O-viewer/main/install.sh | bash
```

Or download manually from the [Releases](https://github.com/kdroidFilter/Mach-O-viewer/releases) page.

## Development

Run the app:
```bash
./gradlew :app:run
```

Build the GraalVM native image:
```bash
./gradlew :app:runWithNativeAgent   # run and exercise all features first
./gradlew :app:packageGraalvmDmg    # then build the native DMG
```
