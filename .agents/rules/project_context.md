# CANDBsee Rules & Knowledge

## Context Summary
- **App Name**: DBsee (CAN DBC Editor)
- **Tech Stack**: Kotlin Multiplatform (Compose Multiplatform), targeting Desktop (JVM) and Web (WasmJS / JS).
- **Core Architecture**: See `docs/ARCHITECTURE.md`.
- **Roadmap & Priorities**: See `docs/ROADMAP.md`.
  - Phase 1 (Attributes Engine) is complete.
  - Phase 2 (Floating-Point Signals & Global Value Tables) is the current priority.

## Core Rules for Agents
1. **Dialog-first UI**: Do NOT place attributes or signal/message editing in sidebars. The user explicitly chose modal pop-up dialogs (`Dialogs.kt`).
2. **WebAssembly WasmJS Safe**:
   - JS code strings in `js("""...""")` must wrap multiple statements in curly braces `{ ... }`.
   - Never use `@Preview` in common code (unsupported on Wasm).
   - If a `kotlin.time.toFixed` LinkError occurs, clean the build with `./gradlew.bat clean`.
3. **Safe File Operations**: Do not run PowerShell multi-line scripts that interpolate Kotlin string variables. Use direct file edit tools.
