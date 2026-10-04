package com.example.candbsee.sample

import com.example.candbsee.model.DbcNetwork
import com.example.candbsee.parser.DbcParser

object SampleDbc {
    val RAW_DBC_TEXT = """
VERSION "EV_POWERTRAIN_V2.4"

NS_ : 
	NS_DESC_
	CM_
	BA_DEF_
	BA_
	VAL_

BS_: 

BU_: VCU BMS Inverter Charger SteeringECU BrakeECU

BO_ 256 VCU_VehicleStatus: 8 VCU
 SG_ VCU_OperatingMode : 0|4@1+ (1,0) [0|15] "" BMS,Inverter,Charger
 SG_ VCU_VehicleSpeed : 4|12@1+ (0.1,0) [0|250] "km/h" BMS,SteeringECU,BrakeECU
 SG_ VCU_GearPosition : 16|4@1+ (1,0) [0|7] "" Inverter,BMS
 SG_ VCU_BrakePedalPosition : 24|8@1+ (0.4,0) [0|100] "%" BrakeECU,Inverter
 SG_ VCU_ThrottlePosition : 32|8@1+ (0.4,0) [0|100] "%" Inverter
 SG_ VCU_SystemReady : 40|1@1+ (1,0) [0|1] "" BMS,Inverter,Charger,SteeringECU,BrakeECU
 SG_ VCU_FaultCode : 48|16@1+ (1,0) [0|65535] "" BMS,Inverter

BO_ 257 VCU_TorqueRequest: 8 VCU
 SG_ VCU_TargetTorque : 0|16@1- (0.5,-500) [-500|500] "Nm" Inverter
 SG_ VCU_MaxRegenTorque : 16|16@1+ (0.5,0) [0|500] "Nm" Inverter,BrakeECU
 SG_ VCU_DriveMode : 32|8@1+ (1,0) [0|5] "" Inverter
 SG_ VCU_TorqueEnable : 40|1@1+ (1,0) [0|1] "" Inverter
 SG_ VCU_RollingCounter : 56|8@1+ (1,0) [0|255] "" Inverter

BO_ 512 BMS_StateOfCharge: 8 BMS
 SG_ BMS_SoC : 0|16@1+ (0.01,0) [0|100] "%" VCU,Inverter,Charger
 SG_ BMS_SoH : 16|8@1+ (0.5,0) [0|100] "%" VCU
 SG_ BMS_RemainingCap : 24|16@1+ (0.1,0) [0|500] "Ah" VCU
 SG_ BMS_ContactorState : 40|4@1+ (1,0) [0|15] "" VCU,Inverter,Charger
 SG_ BMS_InterlockStatus : 44|1@1+ (1,0) [0|1] "" VCU

BO_ 513 BMS_VoltagesAndCurrent: 8 BMS
 SG_ BMS_PackVoltage : 0|16@1+ (0.1,0) [0|1000] "V" VCU,Inverter,Charger
 SG_ BMS_PackCurrent : 16|16@1- (0.1,-1000) [-1000|1000] "A" VCU,Inverter,Charger
 SG_ BMS_MaxDischargeCurrent : 32|16@1+ (0.1,0) [0|1000] "A" VCU,Inverter
 SG_ BMS_MaxChargeCurrent : 48|16@1+ (0.1,0) [0|1000] "A" VCU,Charger

BO_ 514 BMS_Temperatures: 8 BMS
 SG_ BMS_MaxCellTemp : 0|8@1- (1,-40) [-40|150] "C" VCU,Inverter,Charger
 SG_ BMS_MinCellTemp : 8|8@1- (1,-40) [-40|150] "C" VCU
 SG_ BMS_AvgCellTemp : 16|8@1- (1,-40) [-40|150] "C" VCU
 SG_ BMS_CoolantTemp : 24|8@1- (1,-40) [-40|150] "C" VCU
 SG_ BMS_ThermalWarning : 32|1@1+ (1,0) [0|1] "" VCU,Inverter

BO_ 768 Inverter_MotorStatus: 8 Inverter
 SG_ INV_MotorSpeed : 0|16@1- (1,-20000) [-20000|20000] "RPM" VCU,BrakeECU
 SG_ INV_ActualTorque : 16|16@1- (0.5,-500) [-500|500] "Nm" VCU,BrakeECU
 SG_ INV_InverterState : 32|4@1+ (1,0) [0|15] "" VCU
 SG_ INV_DeratingActive : 36|1@1+ (1,0) [0|1] "" VCU
 SG_ INV_IGBTTemp : 40|8@1- (1,-40) [-40|200] "C" VCU
 SG_ INV_MotorTemp : 48|8@1- (1,-40) [-40|200] "C" VCU

BO_ 769 Inverter_PhaseCurrents: 8 Inverter
 SG_ INV_PhaseCurrentU : 0|16@1- (0.1,-1500) [-1500|1500] "A" VCU
 SG_ INV_PhaseCurrentV : 16|16@1- (0.1,-1500) [-1500|1500] "A" VCU
 SG_ INV_PhaseCurrentW : 32|16@1- (0.1,-1500) [-1500|1500] "A" VCU
 SG_ INV_DCBusVoltage : 48|16@1+ (0.1,0) [0|1000] "V" VCU,BMS

BO_ 1024 Charger_Status: 8 Charger
 SG_ CHR_ChargingState : 0|4@1+ (1,0) [0|15] "" VCU,BMS
 SG_ CHR_OutputVoltage : 8|16@1+ (0.1,0) [0|1000] "V" VCU,BMS
 SG_ CHR_OutputCurrent : 24|16@1+ (0.1,0) [0|500] "A" VCU,BMS
 SG_ CHR_ACGridVoltage : 40|16@1+ (0.5,0) [0|500] "V" VCU
 SG_ CHR_Temp : 56|8@1- (1,-40) [-40|150] "C" VCU

BO_ 1280 Steering_AngleData: 6 SteeringECU
 SG_ STR_SteeringAngle : 0|16@0- (0.1,-720) [-720|720] "deg" VCU,BrakeECU
 SG_ STR_SteeringRate : 16|16@0- (1,-1000) [-1000|1000] "deg/s" VCU,BrakeECU
 SG_ STR_HandTorque : 32|8@1+ (0.1,0) [0|25] "Nm" VCU
 SG_ STR_Status : 40|4@1+ (1,0) [0|15] "" VCU

BO_ 1281 Brake_Status: 8 BrakeECU
 SG_ BRK_BrakePressure : 0|16@1+ (0.1,0) [0|300] "bar" VCU,Inverter
 SG_ BRK_ABSActive : 16|1@1+ (1,0) [0|1] "" VCU,Inverter,SteeringECU
 SG_ BRK_ESCActive : 17|1@1+ (1,0) [0|1] "" VCU,Inverter
 SG_ BRK_RegenInhibit : 18|1@1+ (1,0) [0|1] "" VCU,Inverter
 SG_ BRK_WheelSpeedFL : 24|10@1+ (0.25,0) [0|250] "km/h" VCU,Inverter
 SG_ BRK_WheelSpeedFR : 34|10@1+ (0.25,0) [0|250] "km/h" VCU,Inverter
 SG_ BRK_WheelSpeedRL : 44|10@1+ (0.25,0) [0|250] "km/h" VCU,Inverter
 SG_ BRK_WheelSpeedRR : 54|10@1+ (0.25,0) [0|250] "km/h" VCU,Inverter

CM_ BU_ VCU "Vehicle Control Unit - Central Powertrain Supervisor ECU";
CM_ BU_ BMS "Battery Management System - High Voltage Li-Ion Pack Supervisor";
CM_ BU_ Inverter "Traction Motor Inverter controlling the Permanent Magnet Synchronous Motor";
CM_ BU_ Charger "On-Board AC/DC Battery Charger Unit";
CM_ BU_ SteeringECU "Electric Power Steering Supervisor & Sensor Processing Unit";
CM_ BU_ BrakeECU "Electro-Hydraulic Braking, ABS & Stability Control Supervisor";

CM_ BO_ 256 "Primary VCU telemetry broadcast every 10ms";
CM_ BO_ 512 "BMS State of Charge and Pack Health telemetry cast every 100ms";
CM_ BO_ 768 "Real-time motor drive torque and speed telemetry broadcast every 5ms";
CM_ BO_ 1280 "Steering angle sensor frame (Motorola Big Endian bit orientation)";

CM_ SG_ 256 VCU_OperatingMode "0: OFF, 1: INIT, 2: STANDBY, 3: DRIVE_READY, 4: LIMP_HOME, 5: CHARGING";
CM_ SG_ 512 BMS_SoC "State of charge measured via coulomb counting and open circuit voltage compensation";

VAL_ 256 VCU_OperatingMode 0 "OFF" 1 "INIT" 2 "STANDBY" 3 "DRIVE_READY" 4 "LIMP_HOME" 5 "CHARGING" 15 "ERROR" ;
VAL_ 256 VCU_GearPosition 0 "PARK" 1 "REVERSE" 2 "NEUTRAL" 3 "DRIVE" 4 "LOW" ;
VAL_ 257 VCU_DriveMode 0 "ECO" 1 "NORMAL" 2 "SPORT" 3 "SNOW" 4 "CUSTOM" ;
VAL_ 512 BMS_ContactorState 0 "OPEN" 1 "PRECHARGE" 2 "CLOSED" 3 "WELDED_FAULT" ;
VAL_ 768 INV_InverterState 0 "IDLE" 1 "MAGNETIZING" 2 "RUNNING" 3 "FAULT_LOCKOUT" ;
VAL_ 1024 CHR_ChargingState 0 "DISCONNECTED" 1 "CONNECTED_IDLE" 2 "CC_CHARGING" 3 "CV_CHARGING" 4 "COMPLETE" 5 "FAULT" ;
VAL_ 1280 STR_Status 0 "OK" 1 "INIT" 2 "DEGRADED" 3 "FAULT" ;

""".trimIndent()

    val network: DbcNetwork by lazy {
        DbcParser.parse("EV_Powertrain_V2.4.dbc", RAW_DBC_TEXT)
    }
}
