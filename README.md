# MinePresence

MinePresence is a client-only Fabric mod for Minecraft Java Edition `26.2`. It forces Minecraft into a borderless, decorated=false GLFW window and adds a Discord desktop-share guard intended to avoid stream freezes when sharing the entire display.

## What the Discord guard does

- Avoids native/exclusive fullscreen by reapplying windowed borderless mode.
- Uses a configurable safe inset, default `1 px`, so Windows is less likely to route Minecraft through fullscreen-like capture paths. Set it to `0 px` for true edge-to-edge borderless.
- Adds render-loop frame pacing while focused so Discord desktop capture and DWM get regular scheduling time.
- Releases the mouse immediately after focus loss for cleaner alt-tab and click-out behavior.

No Minecraft-side mod can guarantee perfect Discord capture on every GPU, driver, Windows build, and Discord release, but this is the strongest game-side workaround: keep Minecraft composited, avoid exclusive fullscreen behavior, and avoid starving desktop capture while the game is focused.

## Compatibility

- Loader: Fabric only.
- Side: client only.
- Required dependencies: Fabric Loader `0.19.3`, Minecraft `26.2`, and Java `25`.
- Optional integrations: Mod Menu `20.0.0-alpha.1` or newer for the config screen, Fabric API `0.154.0+26.2` or newer if your modpack already uses it.
- Build note: Fabric API is not placed on the compile classpath because this mod does not call Fabric API classes.
- Intended to coexist with Sodium, Iris, and shader packs because it does not patch renderer internals or shader code.

## Settings

Open `Options...` and click `MinePresence` in the top-right corner, or open it through Mod Menu when Mod Menu is installed.

Recommended starting settings:

- `Enabled: On`
- `Borderless: On`
- `Discord Guard: On`
- `Safe Inset: 1 px`
- `FPS Limit: Off`
- `FPS Cap: Unlimited`
- `Auto Reapply: On`
- `Verify Every: 5 s`
- `Coexist Mode: On`
- `Restore Borders: On`
- `Diagnostics: Off`
- `Release Mouse: On`

`Coexist Mode` controls how MinePresence behaves when another borderless mod (such as Cubes Without Borders) is installed:

- `On` (default): the other mod keeps doing the fullscreen-to-borderless conversion, and MinePresence enforces only the capture-safe inset on top of its window. This is the recommended setting because the two mods share the work instead of fighting over the window.
- `Off`: MinePresence takes the window over itself, converting fullscreen to its own borderless window with the inset. Use this if you want MinePresence to be the sole window manager.

If no other borderless mod is installed, this setting has no effect — MinePresence always manages the window itself.

If you need literal edge-to-edge borderless, set `Safe Inset` to `0 px`. If Discord freezes again, return it to `1 px`.

`Diagnostics` writes a lightweight status line to the log every 10 seconds. Leave it off for normal play; turn it on when comparing behavior across versions or collecting bug reports.

## Build

Install a Java 25 JDK, then run the checked-in Gradle wrapper:

```powershell
$env:JAVA_HOME='C:\Program Files\Zulu\zulu-25'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
.\gradlew.bat build
```

The mod jar will be created under:

```text
build/libs/
```

The wrapper is pinned to Gradle `9.6.1`, which can run on Java 25.

## Version coordinates

The target Minecraft version is centralized in `gradle.properties`:

```properties
minecraft_version=26.2
intermediary_version=0.0.0
loader_version=0.19.3
fabric_version=0.154.0+26.2
```

This project includes a tiny local Maven shim for `net.fabricmc:intermediary:26.2`, because Fabric Maven currently reports the real placeholder intermediary as `0.0.0`, which Gradle rejects when Loom asks for `26.2`.
