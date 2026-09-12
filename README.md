# SaveBetter — Personal Finance App

A production-quality Android personal finance application built with Kotlin + Jetpack Compose, designed for Bangladesh (BDT/৳).

SaveBetter is architected from the ground up as an **offline-first, local-source-of-truth** financial assistant. While initial remote synchronization is backed by Firebase Authentication and Cloud Firestore, the application layer is strictly decoupled so that Firebase can be completely replaced by a custom backend such as a **Django REST Framework API + PostgreSQL** without rewriting any Compose UI, ViewModel, or business logic.

---

## 🔥 Firebase Setup

> **⚠️ IMPORTANT: The `google-services.json` in this repository is a PLACEHOLDER.**  
> The app will not connect to Firebase until you replace it with your real configuration.

### Steps to configure Firebase

1. Create a Firebase project at [console.firebase.google.com](https://console.firebase.google.com)
2. Add an Android app with package name **`com.example.savebetter`**
3. Download the `google-services.json` file
4. Place the downloaded file into `app/google-services.json`
5. Enable the following in the Firebase console:
   - **Authentication** → Enable Email/Password and Google Sign-In
   - **Cloud Firestore** → Create database in production mode
   - **Cloud Messaging (FCM)**
6. Deploy Firestore Security Rules:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{uid}/{document=**} {
      allow read, write: if request.auth != null && request.auth.uid == uid;
    }
  }
}
```

### Google Sign-In Setup

Add your machine's SHA-1 debug fingerprint to the Firebase project:

```bash
./gradlew signingReport
```

Copy the `SHA-1` from the debug variant and add it to **Firebase Console → Project Settings → Your Android App → Add fingerprint**.

---

## 🏗️ Architecture

```
                  ┌──────────────────────┐
                  │      Compose UI      │
                  └──────────┬───────────┘
                             │
                             ▼
                  ┌──────────────────────┐
                  │      ViewModel       │
                  └──────────┬───────────┘
                             │
                             ▼
                  ┌──────────────────────┐
                  │       UseCases       │
                  └──────────┬───────────┘
                             │
                             ▼
                  ┌──────────────────────┐
                  │     Repositories     │
                  └───────┬───────┬──────┘
                          │       │
             ┌────────────┘       └────────────┐
             ▼                                 ▼
    ┌─────────────────┐             ┌─────────────────────┐
    │  Room Database  │             │  RemoteDataSource   │
    │ (Local Source   │             │     (Interface)     │
    │   of Truth)     │             └──────────┬──────────┘
    └────────┬────────┘                        │
             │                   ┌─────────────┴─────────────┐
             │                   ▼                           ▼
             │         ┌───────────────────┐       ┌───────────────────┐
             │         │ Firebase (Active) │       │   Django (Future) │
             │         └───────────────────┘       └───────────────────┘
             │
             ▼
        Compose Flow
             │
             ▼
        Reactive UI
```

### Core Architectural Rules

| Rule | Implementation & Guarantee |
|------|----------------------------|
| **Room is Local Source of Truth** | The UI observes Room-backed Kotlin `Flow`s. UI screens never wait for remote network responses to update state. |
| **Local-First Writes** | When a user creates or edits data: `UI → UseCase → Repository → Room (immediate)` with `syncStatus = PENDING`, followed by asynchronous background sync. |
| **Firebase Boundary Isolation** | Firebase classes (`FirebaseAuth`, `FirebaseUser`, `DocumentSnapshot`, `FirebaseFirestoreException`) are strictly restricted to `core/data/remote/firebase/`. They never leak into UI, ViewModels, Use Cases, or Repository interfaces. |
| **User Identity Abstraction** | User identity is encapsulated by `@JvmInline value class UserId(val value: String)`. The domain layer works with `UserId`, not raw Firebase strings or objects. |
| **Replaceable Remote Layer** | Swapping Firebase for a custom REST API (e.g., Django + PostgreSQL) requires only implementing new `RemoteDataSource` interfaces and updating Hilt DI bindings. |

---

## 📦 Package Structure

```
com.example.savebetter
│
├── core/
│   ├── designsystem/     ← Color tokens, typography, custom components (Step 2)
│   ├── i18n/             ← Numeral conversion (123 ↔ ১২৩), currency formatter (BDT/৳) (Step 3)
│   ├── common/           ← Constants, Result wrappers, shared utilities
│   ├── data/
│   │   ├── local/        ← Room Database, Entities, DAOs, Converters, DataStore
│   │   ├── remote/       ← RemoteDataSource interfaces, Firebase implementations, REST models
│   │   ├── repository/   ← Repository implementations coordinating Room + Remote
│   │   └── mapper/       ← Entity ↔ Domain Model ↔ DTO mappers
│   ├── domain/
│   │   ├── model/        ← Pure Kotlin domain models (UserId, Expense, Category, etc.)
│   │   └── usecase/      ← Reusable business logic (Advice engine, summaries, budgets)
│   ├── auth/             ← AuthRepository interface and AuthUser model (Step 1)
│   ├── sync/             ← SyncWorker, SyncManager, conflict resolver, push/pull logic
│   └── di/               ← Hilt dependency injection modules
│
├── feature/
│   ├── auth/             ← Login, Sign Up, Forgot Password, AuthGate (Step 1)
│   ├── onboarding/       ← Setup salary, expense targets, savings goal (Step 5)
│   ├── home/             ← Dashboard with weekly/monthly status, recent expenses, FAB (Step 6)
│   ├── addexpense/       ← Custom keypad, quick chips, category creation (Step 7)
│   ├── weekly/           ← Weekly pacing, daily trend chart, deterministic advice (Step 8)
│   ├── monthly/          ← Category donut chart, month-over-month comparison, reduction engine (Step 9)
│   ├── reconciliation/   ← Out-of-note cash reconciliation and discrepancy checks (Step 10)
│   ├── debts/            ← Receivable/Payable management, net balance (Step 11)
│   └── settings/         ← Profile, theme, language, notifications, sync, account deletion (Step 12)
│
└── navigation/           ← Navigation Compose NavHost, routes, and bottom bar destinations
```

---

## 🔐 Authentication Architecture

Authentication is abstracted behind the pure Kotlin domain interface `AuthRepository`:

```kotlin
interface AuthRepository {
    fun observeAuthState(): Flow<AuthUser?>
    suspend fun signIn(email: String, password: String): Result<AuthUser>
    suspend fun signUp(email: String, password: String): Result<AuthUser>
    suspend fun signInWithGoogle(idToken: String): Result<AuthUser>
    suspend fun sendPasswordResetEmail(email: String): Result<Unit>
    suspend fun signOut()
    suspend fun deleteAccount(): Result<Unit>
}
```

- **Current Implementation:** `FirebaseAuthRepository` using Firebase Authentication.
- **Future Implementation:** `DjangoAuthRepository` backed by JWT/Token auth (no UI or UseCase modifications required).
- **Session Restoration:** Centralized in `AuthGateViewModel` observing `AuthRepository.observeAuthState()`. No custom "remember me" flag needed; persistent tokens are honored automatically.

---

## 🗄️ Room Local Database & Sync Architecture

Room is the **durable local source of truth**.

### Data Flow
- **Writes:** `UI → UseCase → Repository → Room (syncStatus = PENDING) → Enqueue SyncWorker → Remote`
- **Reads:** `Room → Flow<T> → ViewModel → StateFlow<UiState> → Compose UI`
- **Pulls:** `RemoteDataSource.fetchUpdated() → Conflict Resolution → Room upsert`

### Soft Deletion & Tombstones
When an entity is deleted, it is marked with:
```kotlin
isDeleted = true
deletedAt = System.currentTimeMillis()
syncStatus = SyncStatus.PENDING
```
This ensures deleted records synchronize across devices as deletions rather than resurrecting during remote pulls.

### Conflict Resolution Strategy
- **Strategy:** Timestamp-authoritative resolution (`updatedAt`).
- If remote `updatedAt` > local `updatedAt`, remote state replaces local state.
- If local changes are `PENDING` with a newer timestamp, local state is pushed to remote.

---

## ⚙️ DataStore Preferences

Preferences are managed using Jetpack DataStore Preferences via `AppSettingsDataStore`:
- **App Language:** English (`en`) or Bangla (`bn`)
- **Theme Mode:** System default, Light, or Dark
- **Haptic Vibration:** Enabled / Disabled
- **Notification Toggles:** Master switch, Weekly target alert, Monthly budget alert, Reconciliation reminder

---

## 🌍 Localization & Currency

- **Languages:** English (`values/strings.xml`) and Bangla (`values-bn/strings.xml`).
- **Zero Hardcoded Strings:** All user-facing text, alerts, advice, category labels, validation errors, and accessibility descriptions are localized.
- **Numerals:** Full bi-directional support for English (`123`) and Bengali numerals (`১২৩`) via `NumeralConverter`.
- **Currency:** Strictly **BDT / ৳** (never ₹).
- **Precision:** Stored internally as `Long amountMinor` (paisa, where `100 paisa = 1 BDT`) to prevent IEEE 754 floating-point rounding errors.

---

## 🔔 Notification Architecture

- Local scheduled reminders are scheduled via **WorkManager**:
  - Weekly spending pace check
  - Monthly budget limit warning
  - Month-end cash reconciliation reminder (scheduled near the 27th of the month)
- **FCM (Firebase Cloud Messaging):** `SaveBetterFirebaseMessagingService` is configured to handle remote push notifications in future updates.

---

## 🗑️ Account Deletion vs. Logout

- **Logout (`signOut`):** Clears session tokens, cancels active sync workers, and redirects the user to Login. Local Room data is kept intact for the user.
- **Account Deletion (`DeleteAccountUseCase`):**
  1. Re-authenticates the user if required by Firebase security rules.
  2. Permanently removes remote user data from Firestore `/users/{uid}`.
  3. Deletes the Firebase Authentication user account.
  4. Cascades local database cleanup via Room DAOs.
  5. Clears user preferences in DataStore and cancels all scheduled WorkManager jobs.
  6. Navigates back to the unauthenticated login state.

---

## ♿ Accessibility & Visual Polish (Step 18)

- **TalkBack Semantics:** All interactive buttons (`AppTopBar` back button, month/week selectors, FAB, tabs) include localized `contentDescription`s.
- **Touch Target Standard:** All interactive elements meet or exceed Android's minimum **48dp × 48dp** touch target recommendation.
- **Navigation Semantics:** Bottom navigation items utilize `Role.Tab` with selectable semantic state.
- **Chart Accessibility:** `DonutChart` and `DailyTrendBarChart` expose semantic descriptions for screen readers.
- **Dark Mode Support:** All screens adapt between Light (`#FBF6EA` paper) and Dark (`#20281F` dark paper) using dynamic tokens from `SaveBetterTheme.colors`.
- **Text Scaling:** Flexible layouts accommodate Android dynamic system font scaling without text clipping.

---

## 🔄 Future Backend Migration

The application was purposefully designed so that Firebase can be replaced by a custom backend (such as Django REST Framework + PostgreSQL) with **zero changes to Compose UI, ViewModels, Use Cases, or Room DAOs**.

### Architectural Transition

#### Current (Firebase):
```
Android App
     ↓
Repository
     ↓
Room + Firebase
     ↓
Firestore
```

#### Future (Django REST Framework):
```
Android App
     ↓
Repository
     ↓
Room + Django REST API
     ↓
PostgreSQL
```

### Steps to Replace Firebase with Django REST API

1. **Implement Auth Repository:**
   - Create `DjangoAuthRepository : AuthRepository` using Retrofit to call `/api/v1/auth/login/`, `/api/v1/auth/register/`, `/api/v1/auth/refresh/`.
2. **Implement Remote Data Sources:**
   - Create `DjangoExpenseRemoteDataSource : ExpenseRemoteDataSource`
   - Create `DjangoCategoryRemoteDataSource : CategoryRemoteDataSource`
   - Create `DjangoTargetRemoteDataSource : TargetRemoteDataSource`
   - Create `DjangoDebtCreditRemoteDataSource : DebtCreditRemoteDataSource`
   - Create `DjangoReconciliationRemoteDataSource : ReconciliationRemoteDataSource`
3. **Update Hilt DI Module:**
   - In `RemoteDataSourceModule.kt`, replace `@Binds` bindings pointing from Firebase implementations to the Django implementations.
4. **Detailed Specification:**
   - See [docs/REST_BACKEND_SPECIFICATION.md](docs/REST_BACKEND_SPECIFICATION.md) for full endpoint schemas, request/response DTOs, and JWT token rotation flows.

---

## 🛠️ Developer Extension Guides

### How to Add a New Category
1. Add the category `nameKey` string resource in both `values/strings.xml` and `values-bn/strings.xml`.
2. Insert a default seed category record in `DatabaseModule.kt` or allow the user to create a custom category via the "Add Custom Category" dialog in the Add Expense screen.

### How to Add a New Advice Rule
1. Add a deterministic evaluation rule in `GetWeeklyAdviceUseCase.kt` or `GetMonthlyReductionSuggestionsUseCase.kt`.
2. Define localized advisory title and description strings in `values/strings.xml` and `values-bn/strings.xml`.
3. Ensure the rule relies strictly on local Room financial entities without external third-party API dependencies.

---

## 📋 Step Completion Status

| Step | Description | Status |
|:----:|-------------|:------:|
| **0** | Project Setup & Architecture Foundation | ✅ Complete |
| **1** | Firebase Authentication + Session Handling | ✅ Complete |
| **2** | Design System (Colors, Typography, Components) | ✅ Complete |
| **3** | Localization (English / Bangla, BDT/৳, Numeral Conversion) | ✅ Complete |
| **4** | Room Database + Backend-Agnostic Data Architecture | ✅ Complete |
| **5** | Authenticated User Profile + Onboarding Flow | ✅ Complete |
| **6** | Home Dashboard (Weekly/Monthly Summary, Budget Warnings) | ✅ Complete |
| **7** | Add Expense (Keypad, Category Chips, Tombstones) | ✅ Complete |
| **8** | Weekly Detail (Daily Trend, Budget Pacing, Advice Engine) | ✅ Complete |
| **9** | Monthly Analysis (Donut Chart, Reduction Engine, Outlier Filtering) | ✅ Complete |
| **10** | Out-of-Note Reconciliation (Cash Flow Balance & Month-End Check) | ✅ Complete |
| **11** | Debts & Credits / দেনা-পাওনা (Receivables & Payables) | ✅ Complete |
| **12** | Settings (Profile, Theme, Language, Notifications, Data & Sync) | ✅ Complete |
| **13** | Account Deletion (Cascade Cleanup & Re-authentication) | ✅ Complete |
| **14** | Background Synchronization (WorkManager Push/Pull/Conflicts) | ✅ Complete |
| **15** | Multi-Device Behavior (Room as Local Source of Truth) | ✅ Complete |
| **16** | Django REST Backend Migration Readiness & Specification | ✅ Complete |
| **17** | Unit & Compose UI Test Suite | ✅ Complete |
| **18** | Accessibility, Visual Polish & README Finalization | ✅ Complete |

---

## 🧪 Verification & Testing

To run the complete test suite:

```bash
# Run unit tests
./gradlew testDebugUnitTest

# Assemble debug APK
./gradlew assembleDebug
```
