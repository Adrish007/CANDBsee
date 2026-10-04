# CANDBsee: Agent Context & Project Guidelines

> **Notice for Antigravity Agents**: This repository contains **CANDBsee (DBsee)**, a Kotlin Multiplatform (KMP) CAN Database (`.dbc`) editor and analyzer targeting **Desktop (JVM)** and **Web (WasmJS / JS)** using Compose Multiplatform.

---

## 🧭 Project Status & Roadmap

The project is undergoing an incremental feature expansion based on the roadmap in [`docs/ROADMAP.md`](docs/ROADMAP.md):

* **Phase 1: Strongly-Typed Attributes Engine (`BA_DEF_`, `BA_DEF_DEF_`, `BA_`)** — ✅ **COMPLETED**
  * Parser and serializer support in `DbcParser.kt`.
  * Strongly-typed data models in `DbcModel.kt` (`AttributeDefinition`, `AttributeObjectType`, `AttributeValueType`).
  * Attribute editing embedded directly in modal pop-ups via `AttributesEditorForm.kt` (`NewNodeDialog`, `NewMessageDialog`, `SignalEditDialog`).
  * Signals screen ECU filter expanded to all ECUs (Transmitters & Receivers) labeled `"Filter ECUs:"`.
* **Phase 2: Floating-Point Signals & Global Value Tables** — ⏳ **UPCOMING (NEXT TASK)**
  * IEEE 754 float/double signals (`SIG_VALTYPE_`).
  * Global reusable value tables (`VAL_TABLE_`).
* **Phase 3: Extended Multiplexing & Multiple Transmitters** — ⏳ **PLANNED**
  * Multiple transmitting ECUs per frame (`BO_TX_BU_`).
  * Extended multiplexing (`SG_MUL_VAL_`).
* **Phase 4: Two-Way Raw DBC Synchronization & Linting** — ⏳ **PLANNED**
  * Direct editing in `RawEditorScreen.kt` synced back to `DbcNetwork` domain model with error diagnostics.

---

## 🏛️ Architecture & Codebase Map

| Subproject / Module | Purpose | Key Files |
| :--- | :--- | :--- |
| **`shared`** | Common domain models, parser, platform abstractions, Compose UI | `model/DbcModel.kt`, `parser/DbcParser.kt`, `sample/SampleDbc.kt`, `file/FileController.kt`, `ui/*` |
| **`webApp`** | Web entry point for Kotlin/Wasm (`wasmJs`) & Webpack assets | `webApp/src/webMain/kotlin/com/example/candbsee/main.kt`, `resources/index.html` |
| **`desktopApp`** | Desktop entry point for JVM (Windows/macOS/Linux) | `desktopApp/src/main/kotlin/com/example/candbsee/main.kt` |

Detailed architecture documentation is available in [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md).

---

## 🎨 UI/UX Design Guidelines

1. **Pop-up Dialogs, Not Side Panels**: The user explicitly rejected a dedicated right-side properties panel. Always integrate property and attribute editing directly into modal dialogs (`Dialogs.kt`).
2. **Branding**: Title is **"DBsee"**, subtitle is **"CAN DBC Editor"**. No vector logo. Quick action buttons titled **"Add"**.
3. **Theme & Tokens**: Dark mode palette based on Tailwind Slate (`CanDbTheme` in `Theme.kt`). Use `CanDbTheme.CardBg`, `CanDbTheme.AccentEmerald`, `CanDbTheme.AccentCyan`, etc.
4. **Navigation**: Sidebar is collapsable (`NavSidebar.kt` inside `Components.kt`).

---

## ⚠️ Critical Technical Constraints & Platform Quirks

1. **Kotlin/Wasm Linker Cache Bug**:
   * *Symptom*: WebAssembly browser error: `Import #1 "js_code" "kotlin.time.toFixed": function import requires a callable`.
   * *Cause*: Webpack dead-code elimination sometimes strips stdlib JS bridge functions during incremental production builds.
   * *Fix*: Terminate any lingering `node` processes, run `./gradlew.bat clean`, and rebuild.
2. **WasmJS JavaScript Interop (`js("""...""")`)**:
   * JS string templates with multiple statements **must** be wrapped in curly braces `{ ... }` (see `FileController.wasmJs.kt`).
3. **No `@Preview` in Web / Common Code**:
   * KMP WasmJS cannot resolve `org.jetbrains.compose.ui.tooling.preview.Preview`. Keep `@Preview` annotations out of common Compose code.
4. **Safe File Edits**:
   * Always use IDE file edit tools (`replace_file_content` / `write_to_file`) rather than piping multiline strings in PowerShell to avoid string interpolation bugs with `$` and quotes.
