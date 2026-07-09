# MinePresence 0.3.0

**Coexistence with borderless mods + the launch-crash fix.**

### Fixed
- **Startup crash.** The client entrypoint had a private constructor, which Fabric Loader cannot instantiate — the game crashed on launch with `IllegalAccessException`. The constructor is now public.

### Added
- **Coexist Mode** (on by default). When another borderless mod such as **Cubes Without Borders** is installed, MinePresence lets that mod handle the fullscreen-to-borderless conversion and only enforces the capture-safe inset on top of its window — no fighting over the window, no flicker. Turn it off to make MinePresence manage the window itself.
- **Verify Every** setting (1–60 s) to tune how often the window is re-checked.
- **Restore Borders** toggle to expose the existing decoration-restore behavior.

### Changed
- Window verification is now edge-triggered on focus changes and compares against the last applied geometry, instead of re-enumerating monitors every few seconds — lower idle overhead.
- Mouse release on focus loss now fires once at the moment focus is lost, instead of every tick while unfocused.
- Frame pacer no longer recomputes its timing every frame.
- Safe Inset now cycles 0 / 1 / 2 / 4 / 8 px.

### Compatibility
- Minecraft 26.2, Fabric Loader 0.19.3+, Java 25.
- Client-side only. Works alongside Sodium, Iris, Lithium, ModernFix, and Cubes Without Borders.
