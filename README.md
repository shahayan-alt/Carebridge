# 🩺 CareBridge

### *Empowering Elderly Care through Trusted Companionship & Booking*

![Platform](https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Language](https://img.shields.io/badge/Kotlin-100%25-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Architecture](https://img.shields.io/badge/Architecture-MVVM%20%2B%20Clean-orange?style=for-the-badge)
![Backend](https://img.shields.io/badge/Backend-Firebase-FFCA28?style=for-the-badge&logo=firebase&logoColor=black)
![License](https://img.shields.io/badge/License-Academic%20Use-blue?style=for-the-badge)
![Status](https://img.shields.io/badge/Status-Final%20Year%20Project-success?style=for-the-badge)

---

## 📖 Project Overview

**CareBridge** is a native Android application designed to bridge the trust gap between families seeking reliable elder care and verified, professional caregivers/companions. Modern families — often geographically distant or time-constrained — struggle to locate caregivers who are both **skilled** and **verifiably trustworthy**. Existing solutions are frequently informal, unverified, or burdened by third-party commission structures that inflate costs for both parties.

CareBridge solves this by providing a **single, self-contained Android platform** where families can discover KYC-verified caregivers, book time-bound service visits, track those visits in real time, and receive transparent, automatically generated bills — all **without routing payments through a commissioned third-party gateway**. Settlement between the family and caregiver happens directly, with CareBridge acting purely as the **trust, discovery, and logging layer**.

### 🎯 Core Objectives

- ✅ Establish **verified trust** via a mandatory Admin-reviewed KYC process for every caregiver.
- ✅ Enable **frictionless discovery** of caregivers through skill, location, and rate-based filtering.
- ✅ Provide **accurate, timestamp-driven hour logging** through a real-time check-in/check-out system.
- ✅ Automate **transparent billing** using a deterministic, auditable calculation engine.
- ✅ Maintain **accountability** through a mutual rating and review system.
- ✅ Operate entirely within **Firebase's free Spark Tier**, proving the architecture is viable without recurring infrastructure cost — a key academic constraint.

---

## 👥 Key Features by User Role

### 🏠 Family / Client
- Search and filter caregivers by **skillset, location, and hourly rate**.
- View detailed caregiver profiles, verification badges, and past reviews.
- Send booking requests and track their **pending / accepted / rejected / completed** status.
- Monitor **live session status** once a caregiver checks in.
- Receive an **auto-generated, itemized bill** after each completed visit.
- Submit **1–5 star ratings** and written feedback post-session.
- Receive real-time push notifications for booking updates.

### 🧑‍⚕️ Caregiver / Companion
- Create and manage a detailed professional profile (skills, experience, hourly rate).
- Upload **KYC identity documents** and profile photo for Admin verification.
- View and respond to incoming booking requests (**Accept / Reject**).
- **Check-in** at the start of a visit and **check-out** at the end — driving automatic hour calculation.
- View earnings history based on logged, billed sessions.
- Receive push notifications for new bookings and status changes.

### 🛡️ Admin
- Review submitted **KYC documents** (ID proof, certifications) in a dedicated verification queue.
- **Approve / Reject** caregiver applications with a single status toggle.
- Suspend or reactivate user accounts (Family or Caregiver) for policy violations.
- Monitor platform-wide booking and verification activity.

---

## 🧩 8 Core Modules Breakdown

| # | Module | Description | Key Technology |
|---|--------|-------------|-----------------|
| 1 | **Login & Signup** | Secure account creation and login using email/password, with a mandatory email verification link before full app access is granted. | Firebase Authentication |
| 2 | **Dynamic Role Selection** | Post-authentication role assignment (Family, Caregiver, Admin) that dynamically routes users to role-specific dashboards and navigation graphs. | Kotlin + Firestore `role` field |
| 3 | **Search & Book** | Filterable caregiver discovery engine (skills, location, hourly rate) paired with an end-to-end booking request/response workflow. | Firestore Queries, RecyclerView |
| 4 | **Bill & Invoice Generation** | Automatically computes itemized invoices from logged session hours and displays direct-settlement payment status (no payment gateway). | Native Kotlin Billing Engine |
| 5 | **Push Notifications** | Real-time alerts for booking requests, acceptances, rejections, check-in/out events, and verification status changes. | Firebase Cloud Messaging (FCM) |
| 6 | **Service Visit Tracking** | Caregiver-initiated check-in/check-out with server-side timestamping to ensure tamper-resistant, accurate hour logging. | Firestore Timestamps |
| 7 | **Rating & Review System** | Post-session 1–5 star rating with optional written feedback, aggregated into a caregiver's public profile score. | Firestore Sub-collections |
| 8 | **Admin Verification Panel** | Centralized dashboard for Admins to inspect KYC documents and toggle caregiver verification status. | Firebase Storage + Firestore |

---

## 🏗️ System Architecture & Data Flow

CareBridge follows **MVVM (Model-View-ViewModel)** layered on top of **Clean Architecture** principles, separating concerns into distinct, testable layers.

```
┌──────────────────────────────────────────────────────────────┐
│                         PRESENTATION LAYER                     │
│   Activities / Fragments  ⇄  ViewModels  ⇄  UI State (LiveData)│
└───────────────────────────────┬────────────────────────────────┘
                                 │
┌───────────────────────────────▼────────────────────────────────┐
│                          DOMAIN LAYER                            │
│        Use Cases  /  Interactors  /  Business Rules              │
│   (BillCalculationUseCase, BookingValidationUseCase, etc.)       │
└───────────────────────────────┬────────────────────────────────┘
                                 │
┌───────────────────────────────▼────────────────────────────────┐
│                           DATA LAYER                              │
│     Repositories  ⇄  Data Sources (Firestore / Storage / FCM)    │
└───────────────────────────────┬────────────────────────────────┘
                                 │
┌───────────────────────────────▼────────────────────────────────┐
│                    FIREBASE (Spark Free Tier)                     │
│  Authentication │ Cloud Firestore │ Cloud Storage │ FCM            │
└────────────────────────────────────────────────────────────────┘
```

### High-Level Data Flow (Booking → Visit → Bill)

1. **Family** searches caregivers → Repository queries `caregiver_profiles` (filtered by skills/location/rate).
2. **Family** sends a booking request → written to `bookings` collection with status `PENDING`.
3. **Caregiver** receives an FCM push notification → accepts/rejects → `bookings.status` updated.
4. On the scheduled date, **Caregiver** performs **Check-In** → `checkInTimestamp` written server-side.
5. At session end, **Caregiver** performs **Check-Out** → `checkOutTimestamp` written; total hours derived.
6. **Bill Calculation Use Case** triggers automatically → generates an itemized invoice → stored under the booking document.
7. **Family** views the bill, marks it as settled (direct/offline payment), and submits a **Review**.
8. Review is written to `reviews`, and the caregiver's aggregate rating is recalculated.

---

## 🗄️ Cloud Firestore Database Schema

> CareBridge uses a **NoSQL document-based schema**, optimized for read-heavy operations on the Firebase Spark (free) tier.

### Collection: `users`
```json
{
  "userId": "string (Firebase Auth UID)",
  "fullName": "string",
  "email": "string",
  "phoneNumber": "string",
  "role": "FAMILY | CAREGIVER | ADMIN",
  "profileImageUrl": "string (Storage URL)",
  "isEmailVerified": "boolean",
  "createdAt": "timestamp",
  "isActive": "boolean"
}
```

### Collection: `caregiver_profiles`
```json
{
  "caregiverId": "string (references users/{userId})",
  "skills": ["array<string>"],
  "experienceYears": "number",
  "hourlyRate": "number",
  "location": {
    "address": "string",
    "geoPoint": "GeoPoint"
  },
  "bio": "string",
  "kycDocumentUrl": "string (Storage URL)",
  "verificationStatus": "PENDING | VERIFIED | REJECTED",
  "averageRating": "number",
  "totalReviews": "number",
  "isAvailable": "boolean"
}
```

### Collection: `bookings`
```json
{
  "bookingId": "string (auto-generated)",
  "familyId": "string (references users/{userId})",
  "caregiverId": "string (references users/{userId})",
  "scheduledDate": "timestamp",
  "status": "PENDING | ACCEPTED | REJECTED | IN_PROGRESS | COMPLETED | CANCELLED",
  "checkInTimestamp": "timestamp | null",
  "checkOutTimestamp": "timestamp | null",
  "totalLoggedHours": "number",
  "bill": {
    "hourlyRate": "number",
    "loggedHours": "number",
    "subtotal": "number",
    "platformFee": "number",
    "totalAmount": "number",
    "settlementStatus": "UNSETTLED | SETTLED"
  }
}
```

### Collection: `reviews`
```json
{
  "reviewId": "string (auto-generated)",
  "bookingId": "string (references bookings/{bookingId})",
  "familyId": "string",
  "caregiverId": "string",
  "rating": "number (1-5)",
  "comment": "string",
  "createdAt": "timestamp"
}
```

---

## 💰 Bill Calculation Formula & Logic

CareBridge's billing engine is a **pure, native Kotlin computation module** — deliberately excluding any integrated payment gateway. This keeps the platform commission-free; settlement occurs directly between Family and Caregiver, and the app's role is limited to accurate calculation and record-keeping.

### Formula

```
Subtotal        = Total Logged Hours × Caregiver Hourly Rate
Platform Fee    = Flat Fee (fixed, configurable constant)
Total Amount    = Subtotal + Platform Fee
```

### Worked Example

```
Check-In:   2025-10-10  09:00:00
Check-Out:  2025-10-10  13:30:00
------------------------------------------
Total Logged Hours   = 4.5 hours
Hourly Rate          = ₹250 / hour
Subtotal             = 4.5 × 250 = ₹1,125
Platform Fee (flat)  = ₹50
------------------------------------------
Total Amount Due     = ₹1,175
Settlement Status    = UNSETTLED → (updated to SETTLED once confirmed)
```

### Kotlin Implementation Sketch

```kotlin
data class BillDetails(
    val hourlyRate: Double,
    val loggedHours: Double,
    val subtotal: Double,
    val platformFee: Double,
    val totalAmount: Double,
    val settlementStatus: SettlementStatus
)

object BillCalculationUseCase {

    private const val FLAT_PLATFORM_FEE = 50.0

    fun calculate(checkIn: Long, checkOut: Long, hourlyRate: Double): BillDetails {
        val loggedHours = (checkOut - checkIn) / (1000.0 * 60 * 60)
        val subtotal = loggedHours * hourlyRate
        val total = subtotal + FLAT_PLATFORM_FEE

        return BillDetails(
            hourlyRate = hourlyRate,
            loggedHours = loggedHours,
            subtotal = subtotal,
            platformFee = FLAT_PLATFORM_FEE,
            totalAmount = total,
            settlementStatus = SettlementStatus.UNSETTLED
        )
    }
}

enum class SettlementStatus { UNSETTLED, SETTLED }
```

---

## 🛠️ Technology Stack

| Layer | Technology |
|-------|-----------|
| **Mobile Platform** | Android Native |
| **Language** | Kotlin |
| **Architecture** | MVVM + Clean Architecture |
| **Database** | Cloud Firestore (NoSQL) |
| **File Storage** | Firebase Cloud Storage |
| **Authentication** | Firebase Authentication (Email/Password + Email Verification) |
| **Messaging** | Firebase Cloud Messaging (FCM) |
| **Billing Engine** | Native Kotlin Computation (No Payment Gateway) |
| **Async / Reactive** | Kotlin Coroutines & Flow |
| **DI (Optional)** | Hilt / Koin |
| **UI Toolkit** | XML Layouts / Jetpack Components |

### ⚠️ Firebase Spark (Free) Tier Constraints

Since CareBridge is built as an **academic proof-of-concept**, it intentionally operates within Firebase's free **Spark Plan**, which imposes the following limits the architecture is designed to respect:

- **Cloud Firestore:** 1 GiB total storage, 50K reads / 20K writes / 20K deletes per day.
- **Cloud Storage:** 5 GB total storage, 1 GB/day download bandwidth.
- **Cloud Messaging (FCM):** Unlimited and free at all tiers.
- **Authentication:** Unlimited email/password sign-ins at no cost.
- **No Cloud Functions (Blaze-only):** All business logic (e.g., bill calculation, average rating updates) is therefore performed **client-side in Kotlin** rather than via server-side triggers.
- **No integrated payment gateway:** Removing this dependency avoids Blaze-tier billing requirements and third-party commission overhead entirely.

---

## 📁 Project Directory / Package Structure

```
CareBridge/
├── app/
│   ├── src/main/java/com/carebridge/app/
│   │   ├── di/                          # Dependency Injection modules
│   │   ├── data/
│   │   │   ├── remote/                  # Firestore / Storage / FCM data sources
│   │   │   ├── repository/              # Repository implementations
│   │   │   └── model/                   # DTOs / Firestore data models
│   │   ├── domain/
│   │   │   ├── model/                   # Domain entities
│   │   │   ├── repository/              # Repository interfaces
│   │   │   └── usecase/                 # Business logic (BillCalculationUseCase, etc.)
│   │   ├── presentation/
│   │   │   ├── auth/                    # Login / Signup / Email Verification
│   │   │   ├── roleselection/           # Dynamic Role Selection
│   │   │   ├── family/                  # Search, Book, Track, Bills, Reviews
│   │   │   ├── caregiver/               # Profile, KYC Upload, Bookings, Check-in/out
│   │   │   ├── admin/                   # KYC Verification Panel
│   │   │   └── common/                  # Shared UI components, adapters
│   │   ├── service/
│   │   │   └── fcm/                     # FirebaseMessagingService implementation
│   │   ├── utils/                       # Extensions, Constants, Validators
│   │   └── CareBridgeApp.kt             # Application class
│   ├── src/main/res/                    # Layouts, drawables, strings, themes
│   └── AndroidManifest.xml
├── gradle/
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

---

## ⚙️ Setup & Installation Guide

### Prerequisites
- **Android Studio** (Hedgehog or later recommended)
- **JDK 17+**
- A **Firebase Project** (free Spark Plan is sufficient)
- Minimum SDK: **API 24 (Android 7.0)**

### Steps

1. **Clone the repository**
   ```bash
   git clone https://github.com/<your-username>/CareBridge.git
   cd CareBridge
   ```

2. **Create a Firebase Project**
   - Go to the [Firebase Console](https://console.firebase.google.com/).
   - Create a new project (Spark/free plan).
   - Register an Android app with package name `com.carebridge.app`.

3. **Enable required Firebase services**
   - **Authentication** → Enable Email/Password sign-in method.
   - **Cloud Firestore** → Create database in production/test mode.
   - **Cloud Storage** → Enable default storage bucket.
   - **Cloud Messaging** → No manual setup required beyond registration.

4. **Download configuration file**
   - Download `google-services.json` from Project Settings.
   - Place it inside the `app/` directory of the cloned project.

5. **Configure Firestore Security Rules**
   - Apply role-based rules restricting `caregiver_profiles` writes to the owning caregiver and `verificationStatus` changes to Admin accounts only. (Sample rules can be added to `firestore.rules`.)

6. **Open in Android Studio**
   - Open the project folder in Android Studio.
   - Let Gradle sync complete (this will pull all Firebase BoM and Kotlin dependencies).

7. **Build & Run**
   ```bash
   ./gradlew assembleDebug
   ```
   - Or simply click **Run ▶** in Android Studio on an emulator or physical device (API 24+).

8. **Seed Admin Account (Manual, one-time)**
   - Sign up normally through the app, then manually set that user's `role` field to `ADMIN` in the Firestore console to access the Verification Panel.

---

## 🚀 Future Enhancements

- 🔔 In-app chat between Family and Caregiver before booking confirmation.
- 🗺️ Live GPS tracking of caregiver location during an active visit.
- 📄 PDF export of generated invoices.
- 🌐 Multi-language support for wider accessibility among elderly families.
- 💳 Optional integrated payment gateway (Razorpay/Stripe) for in-app settlement, upon migration to Blaze tier.
- 🧠 ML-based caregiver recommendation engine based on past booking patterns.
- 📊 Admin analytics dashboard for platform-wide insights.

---

## 🎓 Academic Credits

**Project Title:** CareBridge — Empowering Elderly Care through Trusted Companionship & Booking
**Category:** Final Year Academic Project (B.E./B.Tech Computer Science / IT)
**Platform:** Android Native (Kotlin)

> This project was developed as part of the final-year academic curriculum to demonstrate applied skills in Android application development, Clean Architecture design, Firebase backend integration, and real-world problem solving in the elderly care services domain.

---

<p align="center">
  <i>CareBridge — Because trust shouldn't be a luxury in elderly care.</i>
</p>
