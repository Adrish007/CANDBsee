# CANDBsee: Development & Build Guide

## Prerequisites

* **JDK 21** (e.g. Eclipse Temurin or JetBrains Runtime 21)
* **Node.js** (managed automatically by Kotlin/Wasm Gradle plugin)
* **IntelliJ IDEA** or **Android Studio** (with Kotlin Multiplatform plugin)

---

## 🚀 Running the Application

### 1. Desktop Application (JVM)
Runs native desktop Compose window:
```powershell
.\gradlew.bat :desktopApp:run
```

### 2. Web Application (Wasm / WebAssembly)
Runs a local development server with hot-reloading:
```powershell
.\gradlew.bat :webApp:wasmJsBrowserDevelopmentRun
```
*App will be available at `http://localhost:8080` (or the port specified in terminal).*

### 3. Web Application Production Distribution (Wasm)
Produces the production-optimized static site in `webApp/build/dist/wasmJs/productionExecutable`:
```powershell
.\gradlew.bat :webApp:wasmJsBrowserDistribution
```

---

## 🛠️ Common Troubleshooting & Fixes

### LinkError: `kotlin.time.toFixed`
* **Symptom**: Browser console shows:
  ```text
  WebAssembly.instantiate(): Import #1 "js_code" "kotlin.time.toFixed": function import requires a callable
  ```
* **Cause**: Webpack's dead-code elimination in incremental builds strips stdlib functions from the generated JavaScript bridge file (`.mjs`), while the `.wasm` binary still imports it.
* **Solution**: Kill any running `node` compiler processes, clean the build cache, and recompile:
  ```powershell
  Stop-Process -Name "node" -Force -ErrorAction SilentlyContinue
  .\gradlew.bat clean :webApp:wasmJsBrowserDistribution
  ```

### File Locks on Windows during `:clean`
* If `:webApp:clean` fails due to file locks on `productionExecutable`, kill any node processes holding Webpack files:
  ```powershell
  Stop-Process -Name "node" -Force -ErrorAction SilentlyContinue
  ```
