# Publishing cheat-sheet — fill in the blanks

Everything you need to paste into CurseForge and Modrinth. The long description
lives in `listing-description.md` (Markdown; both sites accept Markdown).

Repo: <https://github.com/trionic1/MinePresence> — author: trionic1

---

## Shared basics

| Field | Value |
|---|---|
| Mod name | MinePresence |
| Mod ID / slug | `minepresencemod` (Modrinth slug suggestion: `minepresence`) |
| Version | 0.3.0 |
| Summary (short, ~80 chars) | Share your whole screen on Discord without Minecraft freezing. |
| Minecraft version | 26.2 |
| Mod loader | Fabric |
| Environment | Client only |
| License | MIT |
| Java | 25+ |
| Fabric Loader | 0.19.3+ |

**One-line tagline options** (pick one for the summary field):
- "Share your whole screen on Discord without Minecraft freezing."
- "Composited borderless window so full-screen Discord share stays smooth."
- "Fixes borderless-fullscreen screen-capture freezes for streamers."

---

## CurseForge

- **Project type:** Mod
- **Categories:** Utility & QoL *(primary)*; optionally Performance
- **Game versions:** Minecraft 26.2
- **Mod loader:** Fabric
- **Environment tag:** Client
- **License:** MIT (select "MIT License" in the dropdown)
- **Description:** paste `listing-description.md`
- **Relations / Dependencies:**
  - Optional: Fabric API — *Optional*
  - Optional: Mod Menu — *Optional*
  - (Do NOT mark Sodium/Iris/Lithium/ModernFix/Cubes Without Borders — they're compatible, not dependencies.)
- **Source URL:** https://github.com/trionic1/MinePresence
- **Issues URL:** https://github.com/trionic1/MinePresence/issues

## Modrinth

- **Project type:** Mod
- **Summary:** the short tagline above
- **Categories:** Utility *(primary)*; optionally Optimization
- **Loaders:** Fabric
- **Game versions:** 26.2
- **Environment:** Client — **Required**; Server — **Unsupported**
- **License:** MIT
- **Body:** paste `listing-description.md`
- **Links:**
  - Source: https://github.com/trionic1/MinePresence
  - Issues: https://github.com/trionic1/MinePresence/issues
- **Dependencies:**
  - Fabric API — Optional
  - Mod Menu — Optional

---

## Version / file upload

- **File:** `build/libs/minepresencemod-0.3.0.jar`
- **Release channel:** Release (or Beta if you want a soak period first)
- **Version number:** 0.3.0
- **Version name:** MinePresence 0.3.0
- **Changelog:** see `CHANGELOG-0.3.0.md`
- **Supported MC:** 26.2
- **Supported loader:** Fabric
- **Do NOT upload** `-sources.jar` as the primary file (optional secondary if you want).

---

## Before you upload — quick checklist

- [x] GitHub URL filled in `listing-description.md` and this file.
- [x] Author/contact set in `src/main/resources/fabric.mod.json`.
- [ ] Optional: add an icon (CurseForge 400x400, Modrinth 512x512 recommended) and a screenshot or short clip of a smooth Discord share.
- [ ] Confirm the jar in `build/libs/` is `0.3.0` and loads cleanly (it does on your instance).
