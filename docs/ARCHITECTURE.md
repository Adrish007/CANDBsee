# CANDBsee: Multiplatform CAN Database (DBC) Architecture

**CANDBsee (DBsee)** is a modern, responsive Kotlin Multiplatform (KMP) application engineered with **Compose Multiplatform** to run across **Desktop (JVM)** and **Web (WasmJS / JS)**.

---

## 🏗️ System Architecture & Data Pipeline

```mermaid
graph TD
    subgraph Storage & IO
        A["Raw .dbc File / Sample EV DBC"]
        B["FileController (JVM / JS / Wasm)"]
    end
    
    subgraph Parser & Domain Engine
        C["DbcParser.parse()"]
        D["DbcNetwork (Domain Model)"]
        E["DbcParser.serialize()"]
    end
    
    subgraph Interactive Studios (Compose Multiplatform)
        F["Overview Telemetry Dashboard"]
        G["ECU Nodes & Nodewise Inspector"]
        H["Messages & 64-Bit Matrix Layout"]
        I["Global Signals Index"]
        J["Live Raw Editor"]
        K["Modal Dialogs (Attributes, Signals, Nodes, Messages)"]
    end

    A --> B
    B -->|Content String| C
    C --> D
    D --> F & G & H & I & J & K
    H & I & J & K -->|User Visual Edits| D
    D --> E
    E -->|Export .dbc| B
```

---

## 📁 Module Organization

| Path | Target | Description |
| :--- | :--- | :--- |
| `shared/src/commonMain` | Common | Core data models, parser/serializer, Compose UI screens & components |
| `shared/src/wasmJsMain` | WasmJS | WebAssembly DOM file dialogs (`FileController.wasmJs.kt`) |
| `shared/src/jsMain` | JS | Legacy JS fallback file handling (`FileController.js.kt`) |
| `shared/src/jvmMain` | JVM | Desktop Swing `JFileChooser` file dialogs (`FileController.jvm.kt`) |
| `desktopApp` | JVM | Desktop packaging and JVM `main()` entry |
| `webApp` | WasmJS | Web application packaging, HTML shell, and Wasm `CanvasBasedWindow` |

---

## 🧩 Key Domain Models (`shared/.../model/DbcModel.kt`)

* **`DbcNetwork`**: Root model containing:
  * `nodes: List<DbcNode>`
  * `messages: List<DbcMessage>`
  * `attributeDefinitions: Map<String, AttributeDefinition>`
  * `attributeDefaults: Map<String, String>`
  * `networkAttributes: Map<String, String>`
  * `comments: Map<String, String>`
  * `valueDescriptions: Map<Pair<Long, String>, Map<Long, String>>`
* **`DbcMessage`**: CAN frame representing `BO_`:
  * `id: Long` (contains extended ID flag `0x80000000L`)
  * `name: String`, `dlc: Int`, `transmitter: String`
  * `signals: List<DbcSignal>`
  * `attributes: Map<String, String>`
* **`DbcSignal`**: Signal representing `SG_`:
  * `name: String`, `startBit: Int`, `length: Int`
  * `endianness: SignalEndianness` (LITTLE_ENDIAN / BIG_ENDIAN)
  * `isSigned: Boolean`, `factor: Double`, `offset: Double`, `minimum: Double`, `maximum: Double`
  * `unit: String`, `receivers: List<String>`, `attributes: Map<String, String>`
* **`AttributeDefinition`**: Strongly-typed DBC attributes:
  * `objectType: AttributeObjectType` (NETWORK, NODE, MESSAGE, SIGNAL, ENVIRONMENT)
  * `valueType: AttributeValueType` (INT, FLOAT, STRING, ENUM, HEX)
  * `minimum: Double`, `maximum: Double`, `enumValues: List<String>`, `defaultValue: String`

---

## 🖥️ Screen & UI Layout

1. **Network Overview (`NetworkOverviewScreen.kt`)**: Metric counters, bus load cards, node topology breakdown.
2. **Messages (`MessagesScreen.kt`)**: Message list with transmitter filter, message inspector, and **64-Bit Matrix Layout** visualizer (8 bytes × 8 bits).
3. **Signals (`SignalsScreen.kt`)**: Global signal index across all frames with full ECU filtering (`Filter ECUs:`) checking both transmitters (TX) and receivers (RX).
4. **Nodes (`NodesScreen.kt`)**: ECU cards and nodewise dependency tracing (TX frames and RX frames per ECU).
5. **Raw Editor (`RawEditorScreen.kt`)**: Syntax-highlighted text editor for raw DBC format.
6. **Pop-up Dialogs (`Dialogs.kt` & `AttributeForms.kt`)**: Creation and editing modals with embedded strongly-typed attributes.
