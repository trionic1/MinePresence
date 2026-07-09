# MinePresence

**Stream your whole screen on Discord without Minecraft freezing.**

MinePresence is a lightweight, **client-side** Fabric mod that fixes a specific, maddening problem: you share your **entire screen** (not just the Minecraft window) on Discord, and the moment you click into the game the viewers' preview **freezes or stutters** — even though the game runs perfectly smoothly on your end. Sharing just the Minecraft window works, but full-screen / whole-display share breaks.

MinePresence keeps Minecraft in a **composited borderless window** so Windows never hands the game an exclusive, capture-hostile presentation path. Your desktop-capture stays live, your stream stays smooth, and you keep true fullscreen-feel gameplay.

---

## Why this happens

On modern Windows (especially Windows 11 24H2/25H2), a borderless game window that covers the monitor edge-to-edge gets promoted to **"independent flip" / fullscreen optimizations** — a fast path that bypasses the Windows desktop compositor (DWM). That path is great for latency but invisible to desktop-duplication screen capture, which is how Discord grabs your *whole screen*. The result: the capture freezes on the last composited frame (usually the moment you tabbed in), while your own view keeps running fine.

MinePresence stops that promotion from happening.

## What it does

- Forces Minecraft into a **decorated=false borderless window** instead of exclusive fullscreen.
- Applies a configurable **safe inset** (default `1 px`) so the window isn't a perfect edge-to-edge match for the monitor — the single detail that keeps Windows compositing it through DWM, so screen capture keeps working.
- **Re-checks on focus changes** and on a configurable interval, repairing the window only if something actually changed (no per-frame busywork).
- Releases the mouse cleanly on focus loss for smooth alt-tab / click-out.
- Optional frame-pacing cap for users who want a steady, capture-friendly frame cadence (off by default — unlimited FPS).

No renderer patching, no shader hooks, no server-side code. It only touches the OS window.

## Works alongside your existing mods

MinePresence is built to **coexist**, not compete:

- **Cubes Without Borders** (and other borderless/fullscreen mods): fully supported. MinePresence detects them and switches to **Coexist Mode** — it lets that mod do the fullscreen-to-borderless conversion and simply enforces the capture-safe inset on top of its window. No fighting over the window, no flicker. You do **not** have to disable anything.
- **Sodium, Iris, Lithium, ModernFix**, shader packs, and the rest of a typical performance/visual pack: unaffected. MinePresence doesn't patch renderer internals or shaders, so it doesn't interfere with them.

## Settings

Open **Options… → MinePresence** (top-right button), or open it through **Mod Menu** if you have it installed.

| Setting | Default | What it does |
|---|---|---|
| Enabled | On | Master switch for all MinePresence behavior. |
| Borderless | On | Use a borderless window instead of native fullscreen. |
| Discord Guard | On | Applies the safe inset that keeps desktop capture alive. |
| Safe Inset | 1 px | 0 = edge-to-edge; 1 px usually avoids the capture-hostile path. Cycles 0/1/2/4/8. |
| FPS Limit | Off | Optional frame-pacing cap. Leave off for unlimited FPS. |
| FPS Cap | Unlimited | The cap used only while FPS Limit is on. |
| Auto Reapply | On | Periodically verifies the window and repairs it only if needed. |
| Verify Every | 5 s | How often Auto Reapply checks. Longer is cheaper. |
| Coexist Mode | On | With borderless mods like Cubes Without Borders: On enforces the inset on their window; Off makes MinePresence take the window over itself. |
| Restore Borders | On | Gives the title bar back when MinePresence is turned off. |
| Diagnostics | Off | Writes a status line to the log every 10 s. For bug reports. |
| Release Mouse | On | Unlocks the cursor the instant Minecraft loses focus. |

**Recommended starting point:** leave everything at its default. If Discord still freezes, keep `Safe Inset` at `1 px` (not `0`).

## If capture still freezes (Windows tips)

MinePresence is the game-side fix, but two Windows settings help it land reliably on Windows 11:

1. In Minecraft **Video Settings**, set **Fullscreen Resolution / exclusive fullscreen off** (this mod already avoids exclusive fullscreen, but make sure you aren't forcing it elsewhere).
2. If your screen share *still* freezes, disable **"Fullscreen optimizations"** for your Java runtime: right-click the `javaw.exe` your launcher uses → Properties → Compatibility → check **Disable fullscreen optimizations**. This forces the composited path system-wide for the game.

Neither is required for most setups — try the mod first.

## Compatibility

- **Loader:** Fabric only.
- **Side:** Client only (safe to have on a client connecting to any server; does nothing server-side).
- **Minecraft:** 26.2
- **Requires:** Fabric Loader 0.19.3+, Java 25.
- **Optional:** Mod Menu (config screen), Fabric API (only if your pack already uses it — MinePresence itself doesn't need it).

## No guarantees, honestly stated

No Minecraft-side mod can promise perfect Discord capture on *every* GPU, driver, Windows build, and Discord release — the capture path is ultimately controlled by Windows and Discord. What MinePresence does is the strongest game-side workaround: keep Minecraft composited, avoid exclusive fullscreen, and avoid starving desktop capture while the game is focused. On the setups it's been tested on, that's the difference between a frozen stream and a smooth one.

## Source & license

Open source under the **MIT License**. Source and issue tracker: <https://github.com/trionic1/MinePresence>
