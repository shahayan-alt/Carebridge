# CareBridge — Complete UML Diagram Suite & Architectural Documentation

This repository contains the complete set of **6 official UML Diagrams** for the **CareBridge** Native Android application (MVVM + Clean Architecture with Firebase backend).

Every diagram reflects the actual implementation files, classes, workflows, data models, and cloud infrastructure of the CareBridge application.

---

## Table of Diagrams

| # | Diagram Name | File Formats | Purpose |
|---|:---|:---|:---|
| **1** | [Use Case Diagram](#1-use-case-diagram) | `.mmd`, `.puml`, `.png` | User roles (Family, Caregiver, Admin) and system boundary use cases |
| **2** | [Class Diagram](#2-class-diagram) | `.mmd`, `.puml`, `.png` | Domain entities, use cases, repository contracts, ViewModels, and data mappings |
| **3** | [Activity Diagram](#3-activity-diagram) | `.mmd`, `.puml`, `.png` | End-to-end booking lifecycle, service delivery, check-in/out, and billing |
| **4** | [Sequence Diagram](#4-sequence-diagram) | `.mmd`, `.puml`, `.png` | Asynchronous Coroutine interaction flow between Presentation, Domain, Data, and Firestore |
| **5** | [Component Diagram](#5-component-diagram) | `.mmd`, `.puml`, `.png` | Clean Architecture layer components, Dependency Injection, and Firebase cloud adapters |
| **6** | [Deployment Diagram](#6-deployment-diagram) | `.mmd`, `.puml`, `.png` | Hardware devices, Android ART runtime, network protocols (gRPC/HTTPS/FCM), and GCP Firebase nodes |

---

## 1. Use Case Diagram

### Architectural Description
The Use Case diagram specifies the functionality of CareBridge across its three primary human actors and secondary cloud services:
1. **Family Client**: Discovers certified caregivers using multi-criteria filters (skills, hourly rate, verification), books appointments, monitors visit status in real time, settles offline payments, and leaves ratings.
2. **Professional Caregiver**: Manages profile credentials, uploads KYC verification documents (compressed to Base64 to support zero-cost Spark tier), accepts/declines requests, logs check-in/check-out timestamps, and views calculated bills.
3. **System Administrator**: Audits caregiver identity documents, approves/rejects verification requests, and manages user accounts (activate/suspend).
4. **Firebase Cloud Backend**: Secondary actor providing Firebase Authentication, Cloud Firestore real-time snapshots, and FCM push notifications.

- **Diagram File**: `01_Use_Case_Diagram.png`
- **Mermaid Source**: `01_Use_Case_Diagram.mmd`
- **PlantUML Source**: `01_Use_Case_Diagram.puml`

---

## 2. Class Diagram

### Architectural Description
The Class diagram models the Clean Architecture + MVVM structure implemented in Kotlin:
- **Domain Entities & Value Objects**: `User`, `CaregiverProfile`, `CaregiverLocation`, `Booking`, `BillDetails`, `Review`, and enumerations (`UserRole`, `VerificationStatus`, `BookingStatus`, `SettlementStatus`).
- **Domain Use Cases**:
  - `BillCalculationUseCase` (Singleton calculating exact duration arithmetic, applying hourly rates, and adding flat platform fee ₹50.0).
  - Auth, Caregiver, Booking, Admin, and Review use cases.
- **Repository Contracts (Interfaces)**: `AuthRepository`, `CaregiverRepository`, `BookingRepository`, `AdminRepository`, `ReviewRepository`.
- **Data Layer Implementations**: `AuthRepositoryImpl`, `CaregiverRepositoryImpl`, `BookingRepositoryImpl`, `AdminRepositoryImpl`, `ReviewRepositoryImpl`, accompanied by `FirestoreParsers` for defensive numeric type casting.
- **Presentation Layer**: `AuthViewModel`, `FamilyViewModel`, `CaregiverViewModel`, `AdminViewModel`, with singleton dependency injection managed by `ServiceLocator`.

- **Diagram File**: `02_Class_Diagram.png`
- **Mermaid Source**: `02_Class_Diagram.mmd`
- **PlantUML Source**: `02_Class_Diagram.puml`

---

## 3. Activity Diagram

### Architectural Description
The Activity diagram traces the end-to-end operational flow across four distinct swimlanes (`Family Client`, `Caregiver Provider`, `System / Use Cases`, and `Cloud Firestore`):
1. **Discovery & Scheduling**: Family inputs search criteria -> selects caregiver -> schedules visit date/time -> Firestore writes booking with `status: PENDING`.
2. **Acceptance / Decline**: Caregiver receives FCM/snapshot alert -> decides whether to accept or decline.
3. **Visit Execution**: Caregiver arrives -> taps "Check-In" -> System records `checkInTimestamp` and transitions to `IN_PROGRESS` -> service is performed.
4. **Automated Bill Calculation**: Caregiver taps "Check-Out" -> `BillCalculationUseCase.calculate()` computes:
   $$	ext{Duration} = 	ext{CheckOut} - 	ext{CheckIn}$$
   $$	ext{Logged Hours} = 	ext{round}\left(rac{	ext{Duration}}{3,600,000}, 2ight)$$
   $$	ext{Subtotal} = 	ext{Logged Hours} 	imes 	ext{Hourly Rate}$$
   $$	ext{Total Amount} = 	ext{Subtotal} + 	ext{Platform Fee (₹50.0)}$$
5. **Settlement & Feedback**: Status set to `COMPLETED` -> Family inspects invoice on `BookingDetailActivity` -> pays offline -> taps "Mark Settled" -> submits 1-5 star review.

- **Diagram File**: `03_Activity_Diagram.png`
- **Mermaid Source**: `03_Activity_Diagram.mmd`
- **PlantUML Source**: `03_Activity_Diagram.puml`

---

## 4. Sequence Diagram

### Architectural Description
The Sequence diagram documents the asynchronous runtime messages exchanged during visit session execution:
- Uses Kotlin Coroutines (`viewModelScope.launch`, `withContext(Dispatchers.IO)`) and Kotlin `Flow`/`callbackFlow`.
- Shows defensive error handling via `Resource.Loading`, `Resource.Success`, and `Resource.Error`.
- Captures the exact call sequence from `BookingDetailActivity` checking in, retrieving hourly rates from `CaregiverRepositoryImpl`, calculating the bill via `BillCalculationUseCase`, updating Firestore, and triggering the review submission dialog.

- **Diagram File**: `04_Sequence_Diagram.png`
- **Mermaid Source**: `04_Sequence_Diagram.mmd`
- **PlantUML Source**: `04_Sequence_Diagram.puml`

---

## 5. Component Diagram

### Architectural Description
The Component diagram visualizes the modular decoupling of the application:
- **Presentation Component**: Subsystems for Auth, Family, Caregiver, and Admin UIs, backed by ViewModels and Android ViewBinding.
- **Domain Component**: Framework-independent business layer containing POJO entities, use cases, and abstract repository ports.
- **Data Component**: Firebase integration adapters, DTO converters, defensive snapshot parsers (`FirestoreParsers`), and the in-memory Base64 JPEG compressor.
- **Dependency Injection**: Central `ServiceLocator` providing singleton instances across the entire application graph.
- **External Cloud**: Google Firebase Auth, Cloud Firestore, and FCM gateways.

- **Diagram File**: `05_Component_Diagram.png`
- **Mermaid Source**: `05_Component_Diagram.mmd`
- **PlantUML Source**: `05_Component_Diagram.puml`

---

## 6. Deployment Diagram

### Architectural Description
The Deployment diagram details the physical and virtual runtime topology:
- **Client Mobile Device**: Physical Android devices or AVD emulators running Android OS (API 26 to API 34) on ARM64/x86_64 architectures executing `com.carebridge.app` inside the Android Runtime (ART) with local SQLite/LRU Firestore offline caching.
- **Network Layer**: Encrypted transport over HTTPS / TLS 1.3 (TCP 443), bi-directional gRPC over HTTP/2 for Firestore snapshot streams, and persistent FCM socket connections (ports 5228-5230).
- **Google Cloud Platform / Firebase Infrastructure**:
  - Firebase Authentication Identity server.
  - Cloud Firestore serverless multi-region NoSQL cluster enforcing `firestore.rules`.
  - Firebase Cloud Messaging push notification dispatch cluster.

- **Diagram File**: `06_Deployment_Diagram.png`
- **Mermaid Source**: `06_Deployment_Diagram.mmd`
- **PlantUML Source**: `06_Deployment_Diagram.puml`

---

## Verification & Academic Standards
- **Standard UML 2.5 Compliance**: All diagrams adhere to standard UML notations (Actors, Use Cases, Classes, Activities, Lifelines, Components, and Nodes).
- **Codebase Precision**: Class names, fields, methods, enumerations, and workflows match the actual Kotlin source code in `A:\Project\app\src\main\java\com\carebridge\app`.
- **Presentation & Viva Ready**: High-resolution PNG images are formatted with clean white backgrounds, padding, and legible typography suitable for academic project reports, viva slides, and printed journals.
