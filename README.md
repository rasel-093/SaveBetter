# SaveBetter — Personal Finance App

A production-quality Android personal finance application built with Kotlin + Jetpack Compose, designed for Bangladesh (BDT/৳).

---

## 🔥 Firebase Setup

> **⚠️ IMPORTANT: The `google-services.json` in this repository is a PLACEHOLDER.**
> The app will not connect to Firebase until you replace it with your real configuration.

### Steps to configure Firebase

1. Create a Firebase project at [console.firebase.google.com](https://console.firebase.google.com)
2. Add an Android app with package name **`com.example.savebetter`**
3. Download the `google-services.json` file
4. Replace `app/google-services.json` with your downloaded file
5. Enable the following in Firebase console:
   - **Authentication** → Email/Password + Google Sign-In
   - **Cloud Firestore** → Create database in production mode
   - **Cloud Messaging**

### Firestore Security Rules

Deploy these rules before any production use (Step 4 will expand them):

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{uid}/{document=**} {
      allow read, write: if request.auth != null && request.auth.uid == uid;
    }
  }
}
```

### Google Sign-In

Add your SHA-1 fingerprint to the Firebase project:

```bash
./gradlew signingReport
```

Copy the SHA-1 and add it to Firebase console → Project Settings → Your Android App.

---

## 🏗️ Architecture

```
Compose UI
    ↓
ViewModel
    ↓
Use Cases  (no Firebase knowledge)
    ↓
Repository Interfaces  (no Firebase knowledge)
    ↓              ↓
Room DB     RemoteDataSource interface
(local         ↓               ↓
 source     Firebase      Django (future)
 of truth)  implementation
```

### Key Rules

| Rule | Description |
|------|-------------|
| **Firebase boundary** | Firebase classes never leave `core/data/remote/firebase/` |
| **Room is source of truth** | UI always observes Room-backed Flows |
| **Local-first writes** | Writes go to Room first, then sync to remote |
| **Replaceable backend** | Firebase → Django REST API requires only new `RemoteDataSource` implementations |

---

## 📦 Package Structure

```
com.example.savebetter
│
├── core/
│   ├── designsystem/     ← Colors, typography, components (Step 2)
│   ├── i18n/             ← Localisation utilities (Step 3)
│   ├── common/           ← Shared utilities, extensions
│   ├── data/
│   │   ├── local/        ← Room DB, DAOs, TypeConverters, DataStore
│   │   ├── remote/       ← RemoteDataSource interfaces + Firebase implementations
│   │   ├── repository/   ← Repository implementations
│   │   └── mapper/       ← Entity ↔ Domain Model ↔ Firebase DTO mappers
│   ├── domain/
│   │   ├── model/        ← Domain models (no Android/Firebase dependencies)
│   │   └── usecase/      ← Business logic use cases
│   ├── auth/             ← AuthRepository interface + domain model
│   ├── sync/             ← SyncWorker, FCM service
│   └── di/               ← Hilt modules
│
├── feature/
│   ├── auth/             ← Login, SignUp, ForgotPassword (Step 1)
│   ├── onboarding/       ← Onboarding flow (Step 5)
│   ├── home/             ← Dashboard (Step 6)
│   ├── addexpense/       ← Add/Edit expense (Step 7)
│   ├── weekly/           ← Weekly detail (Step 8)
│   ├── monthly/          ← Monthly analysis (Step 9)
│   ├── reconciliation/   ← Out-of-note reconciliation (Step 10)
│   ├── debts/            ← Debts & credits (Step 11)
│   └── settings/         ← Settings (Step 12)
│
└── navigation/           ← NavHost and route definitions
```

---

## 🔐 Authentication Architecture

Authentication is abstracted behind `AuthRepository`:

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

- Current implementation: `FirebaseAuthRepository`
- Future implementation: `DjangoAuthRepository` (no UI changes needed)

`FirebaseUser` is never exposed outside `core/data/remote/firebase/`.

---

## 🗄️ Room Database Architecture

Room is the **primary local source of truth**. The UI never waits for Firebase.

### Write flow
```
UI → UseCase → Repository → Room (immediate) → SyncWorker → Firebase
```

### Read flow
```
Firebase → SyncWorker → Room → Flow → UI
```

### Sync status
Every syncable entity has a `syncStatus` field:
- `SYNCED` — matches remote
- `PENDING` — local change not yet pushed
- `CONFLICT` — server and local versions differ

### Soft deletion
Deleted records use tombstones (`isDeleted = true, deletedAt = ...`) so they
are not resurrected from other devices during sync.

---

## 🌍 Localisation

Supported languages:
- **English** — `values/strings.xml`
- **বাংলা (Bangla)** — `values-bn/strings.xml`

Language selection is persisted in DataStore. Uses Android per-app language APIs
(API 33+) with a DataStore-based fallback for API 26–32.

Currency: **BDT / ৳** (never ₹)
Money stored as: `Long amountMinor` (paisa)

---

## 🔔 Notifications

| Type | Mechanism |
|------|-----------|
| Weekly target warning | WorkManager (local) |
| Monthly target warning | WorkManager (local) |
| Month-end reconciliation reminder | WorkManager (local) |
| Server-triggered notifications | FCM (future) |

---

## 🗑️ Account Deletion

Account deletion is separate from logout. Deletion:
1. Cancels all pending WorkManager jobs
2. Clears Room database for the user
3. Clears user DataStore preferences
4. Deletes Firestore user data
5. Deletes Firebase Auth account (may require re-authentication)
6. Returns to Login screen

---

## 🔄 Future Backend Migration

### Current architecture
```
Android App
    ↓
Repository (interface)
    ↓              ↓
Room DB     FirebaseExpenseRemoteDataSource
                ↓
         Cloud Firestore
```

### Future architecture (Django)
```
Android App
    ↓
Repository (interface — UNCHANGED)
    ↓              ↓
Room DB     DjangoExpenseRemoteDataSource (NEW)
                ↓
         Django REST API → PostgreSQL
```

To migrate:
1. Implement `DjangoAuthRepository : AuthRepository`
2. Implement `DjangoExpenseRemoteDataSource : ExpenseRemoteDataSource`
3. Implement equivalent `DjangoXxxRemoteDataSource` for each entity
4. Swap Hilt bindings in `RemoteDataSourceModule`
5. **No changes** to Compose UI, ViewModels, use cases, domain models, or Room

---

## 🛠️ Development

### Build

```bash
./gradlew :app:assembleDebug
```

### Run tests

```bash
./gradlew :app:test
./gradlew :app:connectedAndroidTest
```

### Add a new category

1. Add a `nameKey` string resource in `values/strings.xml` and `values-bn/strings.xml`
2. Add a default `CategoryEntity` seeded in `DatabaseModule` or initial data setup

### Add a new advice rule

Add a new rule in `GetWeeklyAdviceUseCase` or `GetMonthlyReductionSuggestionsUseCase`.
All advice text must use string resources — no hardcoded strings.

---

## 📋 Step Completion Status

| Step | Description | Status |
|------|-------------|--------|
| 0 | Project Setup & Architecture Foundation | ✅ Complete |
| 1 | Firebase Authentication + Session Handling | ✅ Complete |
| 2 | Design System | ✅ Complete |
| 3 | Localisation | ✅ Complete |
| 4 | Room Database + Data Architecture | ✅ Complete |
| 5 | User Profile + Onboarding | ✅ Complete |
| 6 | Home Dashboard | ✅ Complete |
| 7 | Add Expense | ✅ Complete |
| 8 | Weekly Detail | ✅ Complete |
| 9 | Monthly Analysis | ✅ Complete |
| 10 | Out-of-Note Reconciliation | ✅ Complete |
| 11 | Debts & Credits | ✅ Complete |
| 12 | Settings | ✅ Complete |
| 13 | Account Deletion | ✅ Complete |
| 14 | Background Synchronisation | ✅ Complete |
| 15 | Multi-Device Behavior | ✅ Complete |
| 16 | Django Migration Readiness | ⏳ Pending |
| 17 | Tests | ⏳ Pending |
| 18 | Accessibility, Polish & README | ⏳ Pending |
