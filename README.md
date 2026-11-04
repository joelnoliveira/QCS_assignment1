# Fault-Tolerant Automotive Control Software

## Index
- [Project Overview](#project-overview)
- [Implementation](#implementation)
- [Repository Structure](#repository-structure)

## Project Overview

This academic project focuses on the development of fault-tolerant software for an automotive control system. Since software plays a fundamental role in implementing modern vehicle functionality, and given the complexity of these systems and the potential consequences of failures, fault tolerance is an important aspect of ensuring their reliability and safety.
Two control functions were implemented: Cruise Control, which maintains the vehicle's speed according to the desired setting, and Climate Control, which monitors the temperature within the passenger compartment and keeps it within the user's desired range.
The project explores the application of fault-tolerance mechanisms to these control functions, addressing the possibility of faults during system operation.

- **Course**: Software Quality and Dependability.
- **Technologies**: Java.

## Implementation
The developed functions include the following fault prevention and fault tolerance mechanisms:

- **Fault prevention**:
    - **Null-value checks**: Verify that input values are not null before processing.
    - **Input boundary checks**: Verify that input values remain within predefined limits, as values outside these limits may indicate a faulty sensor.
- **Fault tolerance**:
    - **Pair-and-a-Spare** approach: When three sensors of the same type are available (two active sensors and one spare), if the readings from the two active sensors differ, the spare sensor is used to determine which reading is correct.
    - **Dual Modular Redundancy (DMR)** as a backup: When only two sensors of the same type are available (instead of the three required by the Pair-and-a-Spare approach), their readings are compared to detect disagreements.
    - **Redundant computation**: Repeat computations to mitigate transient errors.
    - **N-Version Programming**: Use independently developed implementations of the same functionality to reduce the impact of software failures caused by programming errors.
    - **Watchdog timers**: Detect and recover from unresponsive execution, such as deadlocks or infinite loops.
    - **CRC (Cyclic Redundancy Check) and variable replicas**: Use CRC checks and duplicate copies of variables, initialized at declaration and compared before computation, to verify and ensure data integrity.

## Repository Structure

``` text
.
├── ClimateControl
│   ├── ClimateControlBaseline.java
│   └── ClimateControlFault.java -> Fault tolerant version
├── CruiseControl
│   ├── CruiseControlBaseline.java
│   ├── CruiseControlFaultTolerant.java -> Fault tolerant versions
│   └── MainCruiseControl.java -> Fault tolerant version
├── README.md
└── docs
    └── assigment.pdf
```