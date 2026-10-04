# CANDBsee Feature Completeness Roadmap

This roadmap documents the implementation progress and future phases for the CAN DBC Editor.

---

## 🚦 Implementation Status

### ✅ Phase 1: Strongly-Typed Attributes Engine — **COMPLETED**
- [x] **Data Model**: Added `AttributeDefinition`, `AttributeObjectType`, and `AttributeValueType` to `DbcModel.kt`.
- [x] **DBC Parser & Serializer**:
  - `BA_DEF_`: Attribute definition parsing with types (INT, HEX, FLOAT, STRING, ENUM) and ranges.
  - `BA_DEF_DEF_`: Attribute default values.
  - `BA_`: Assigned attribute values mapped to Nodes, Messages, and Signals.
- [x] **UI Integration**:
  - Integrated `AttributesEditorForm` into creation & edit modal dialogs (`NewNodeDialog`, `NewMessageDialog`, `SignalEditDialog`).
  *Note: User explicitly rejected right-side property sidebars; attributes must remain within pop-up modals.*
- [x] **Signals Screen Filter**:
  - Extended ECU filter to cover all ECUs (Transmitters and Receivers) with clean label `"Filter ECUs:"`.

---

### ⏳ Phase 2: Floating-Point Signals & Global Value Tables — **NEXT UP**

1. **IEEE Floating-Point Signals (`SIG_VALTYPE_`)**:
   - **Syntax**: `SIG_VALTYPE_ <MessageId> <SignalName> : <Type>;` where `1 = 32-bit float`, `2 = 64-bit double`.
   - **Action**: Parse `SIG_VALTYPE_` in `DbcParser.kt` and serialize back. Update `DbcSignal.valueType` to distinguish between standard integer/scaled signals vs IEEE float/double.
   - **UI**: Add Float/Double selector in `SignalEditDialog`.

2. **Global Value Tables (`VAL_TABLE_`)**:
   - **Syntax**: `VAL_TABLE_ <TableName> <Value1> "<Desc1>" <Value2> "<Desc2>" ... ;`
   - **Action**: Parse global `VAL_TABLE_` definitions in `DbcParser.kt` at the file header and store them in `DbcNetwork.globalValueTables`. Allow signals to link to global value tables.
   - **UI**: Manage global tables and allow selecting an existing global table when editing a signal's value descriptions.

---

### ⏳ Phase 3: Advanced Routing & Multiplexing — **PLANNED**

1. **Multiple Transmitters (`BO_TX_BU_`)**:
   - **Syntax**: `BO_TX_BU_ <MessageId> : <Transmitter1>,<Transmitter2>,...;`
   - **Action**: Support multiple transmitting ECUs in `DbcMessage` instead of just a single transmitter string.
2. **Extended Multiplexing (`SG_MUL_VAL_`)**:
   - **Syntax**: `SG_MUL_VAL_ <MessageId> <SignalName> <MuxSwitchName> <MuxValueRanges>;`
   - **Action**: Support complex multiplexing dependencies where signals are conditionally active across multiplexer value ranges.

---

### ⏳ Phase 4: Two-Way Raw DBC Editor Sync & Diagnostics — **PLANNED**

1. **Two-Way Synchronization**:
   - Save edits in `RawEditorScreen.kt` back to the live `DbcNetwork` domain model.
2. **Error Diagnostics**:
   - Show inline line/column syntax error highlights when invalid DBC syntax is typed into the raw editor.
