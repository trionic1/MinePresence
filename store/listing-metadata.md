# Publishing cheat-sheet — fill in the blanks

Everything you need to paste into CurseForge and Modrinth. The long description
lives in `listing-description.md` (Markdown; both sites accept Markdown).

Placeholders you must fill: **<YOUR_GITHUB_URL>**, **<YOUR_AUTHOR_NAME>**.

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
- **Source URL:** <YOUR_GITHUB_URL>
- **Issues URL:** <YOUR_GITHUB_URL>/issues

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
  - Source: <YOUR_GITHUB_URL>
  - Issues: <YOUR_GITHUB_URL>/issues
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

- [ ] Fill `<YOUR_GITHUB_URL>` in `listing-description.md` and this file.
- [ ] Set author/contact in `src/main/resources/fabric.mod.json` (`authors`, `contact.homepage`, `contact.sources`, `contact.issues`) — currently placeholder "minepresencemod contributors" with empty contact. Ask me and I'll wire these in once you have the GitHub URL.
- [ ] Optional: add an icon (CurseForge 400x400, Modrinth 512x512 recommended) and a screenshot or short clip of a smooth Discord share.
- [ ] Confirm the jar in `build/libs/` is `0.3.0` and loads cleanly (it does on your instance).
