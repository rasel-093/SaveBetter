# SaveBetter — RESTful Backend Migration Specification

This document provides the technical contract, API specifications, and architectural guide for replacing Firebase Authentication and Firestore with a custom RESTful API backend in **SaveBetter**.

As designed, the backend can be implemented in **any technology stack**:
- **Python:** Django REST Framework, FastAPI, Flask
- **Java / Kotlin:** Spring Boot, Ktor, Micronaut
- **Node.js / TypeScript:** Express, NestJS, Fastify
- **Go:** Gin, Fiber, Echo
- **C# / .NET:** ASP.NET Core Web API
- **PHP:** Laravel, Symfony

---

## 1. Architectural Proof of Decoupling

The SaveBetter architecture strictly isolates network and data layers:

```
Compose UI (Screens & Components)
           │
           ▼
ViewModels (UI State)
           │
           ▼
Domain Use Cases
           │
           ▼
Repository Interfaces (AuthRepository, ExpenseRepository, etc.)
           │
     ┌─────┴──────────────────────────────────┐
     ▼                                        ▼
Room Database                           RemoteDataSource
(Single Local Source of Truth)                 │
                                        ┌──────┴────────────────┐
                                        ▼                       ▼
                           Firebase RemoteDataSource   REST RemoteDataSource
                                   (Current)             (Any REST Stack)
```

### Zero Leaks Guarantee:
- **UI / ViewModel / UseCase Layers:** Never import or reference Firebase, Firestore, or network HTTP libraries.
- **Room Local Database:** Stores all entities locally (`ExpenseEntity`, `CategoryEntity`, etc.) and acts as the immediate local-first data source.
- **Remote Data Sources:** All network communication is abstracted behind clean Kotlin interfaces:
  - `AuthRemoteDataSource`
  - `ExpenseRemoteDataSource`
  - `CategoryRemoteDataSource`
  - `TargetRemoteDataSource`
  - `DebtCreditRemoteDataSource`
  - `UserProfileRemoteDataSource`

To switch from Firebase to your REST backend, only the bindings in `core/di/DataModule.kt` and `core/di/AuthModule.kt` need to be changed!

---

## 2. Global REST API Conventions

- **Base URL:** `https://api.yourdomain.com/api/v1/`
- **Content-Type:** `application/json; charset=utf-8`
- **Authentication Scheme:** `Authorization: Bearer <JWT_ACCESS_TOKEN>`
- **Money Values:** Integer / `Long` in minor units (BDT Poisha / Cents). For example, `৳ 350.50` is sent as `35050`. No floating-point precision loss.
- **Timestamps:** ISO-8601 UTC string (`2026-09-12T14:00:00Z`).
- **Standard HTTP Status Codes:**
  - `200 OK`: Successful retrieval or update
  - `201 Created`: Successful creation
  - `204 No Content`: Successful deletion
  - `400 Bad Request`: Validation failure
  - `401 Unauthorized`: Token expired / missing
  - `403 Forbidden`: Access denied
  - `404 Not Found`: Resource not found
  - `409 Conflict`: Sync conflict (version / timestamp mismatch)

---

## 3. Endpoints & Schemas

### 3.1 Authentication

#### `POST /api/v1/auth/login/`
Request:
```json
{
  "email": "user@example.com",
  "password": "SecurePassword123!"
}
```
Response (`200 OK`):
```json
{
  "user_id": "usr_998877",
  "email": "user@example.com",
  "display_name": "Rahim Ahmed",
  "access_token": "eyJhbGciOi...",
  "refresh_token": "dGhpc2lz...",
  "token_type": "Bearer"
}
```

#### `POST /api/v1/auth/register/`
Request:
```json
{
  "email": "user@example.com",
  "password": "SecurePassword123!",
  "display_name": "Rahim Ahmed"
}
```
Response (`201 Created`): Same as `RestAuthResponse`.

#### `POST /api/v1/auth/google/`
Request:
```json
{
  "id_token": "<Google_OAuth2_IdToken>"
}
```
Response (`200 OK`): Same as `RestAuthResponse`.

#### `POST /api/v1/auth/password-reset/`
Request:
```json
{
  "email": "user@example.com"
}
```
Response (`200 OK`): `{}`

---

### 3.2 Expenses

#### `GET /api/v1/users/{userId}/expenses/`
Response (`200 OK`):
```json
[
  {
    "id": "e4a77b8f-b98a-4d24-8eef-6f29871c828d",
    "user_id": "usr_998877",
    "amount_minor": 150000,
    "category_id": "def_cat_grocery_usr_998877",
    "note": "Weekly Bazaar",
    "date": "2026-09-12T09:30:00Z",
    "created_at": "2026-09-12T09:30:00Z",
    "updated_at": "2026-09-12T09:30:00Z",
    "is_deleted": false,
    "deleted_at": null
  }
]
```

#### `POST /api/v1/users/{userId}/expenses/`
Request: Same JSON as single expense object above.
Response (`200 OK` / `201 Created`): `{}`

#### `DELETE /api/v1/users/{userId}/expenses/{expenseId}/`
Response (`200 OK` / `204 No Content`): `{}`

---

### 3.3 Categories

#### `GET /api/v1/users/{userId}/categories/`
Response (`200 OK`):
```json
[
  {
    "id": "cat_custom_01",
    "user_id": "usr_998877",
    "name_key": null,
    "custom_name": "Freelance Gear",
    "icon": "laptop",
    "color_token": "cat3",
    "is_default": false,
    "created_at": "2026-09-12T09:00:00Z",
    "updated_at": "2026-09-12T09:00:00Z",
    "is_deleted": false
  }
]
```

#### `POST /api/v1/users/{userId}/categories/`
Request: Same JSON as single category object.
Response (`200 OK` / `201 Created`): `{}`

---

### 3.4 Targets & Salary Records

#### `GET /api/v1/users/{userId}/targets/weekly/`
```json
[
  {
    "id": "wt_2026_w37",
    "user_id": "usr_998877",
    "week_start": "2026-09-07",
    "week_end": "2026-09-13",
    "target_amount_minor": 500000,
    "updated_at": "2026-09-07T00:00:00Z"
  }
]
```

#### `GET /api/v1/users/{userId}/targets/monthly/`
```json
[
  {
    "id": "mt_2026_09",
    "user_id": "usr_998877",
    "month": 9,
    "year": 2026,
    "target_amount_minor": 2000000,
    "saving_goal_minor": 500000,
    "updated_at": "2026-09-01T00:00:00Z"
  }
]
```

#### `GET /api/v1/users/{userId}/salary-records/`
```json
[
  {
    "id": "sr_2026_09",
    "user_id": "usr_998877",
    "month": 9,
    "year": 2026,
    "salary_amount_minor": 3500000,
    "hand_remaining_amount_minor": 1200000,
    "updated_at": "2026-09-12T00:00:00Z"
  }
]
```

---

### 3.5 Debts & Credits (দেনা-পাওনা)

#### `GET /api/v1/users/{userId}/debts-credits/`
```json
[
  {
    "id": "dc_001",
    "user_id": "usr_998877",
    "direction": "RECEIVABLE",
    "person_name": "Tariq",
    "amount_minor": 250000,
    "note": "Shared project expense",
    "date": "2026-09-10T12:00:00Z",
    "due_date": "2026-09-25T00:00:00Z",
    "is_settled": false,
    "created_at": "2026-09-10T12:00:00Z",
    "updated_at": "2026-09-10T12:00:00Z",
    "is_deleted": false,
    "deleted_at": null
  }
]
```

---

### 3.6 User Profile

#### `GET /api/v1/users/{userId}/profile/`
```json
{
  "id": "usr_998877",
  "name": "Rahim Ahmed",
  "email": "user@example.com",
  "monthly_salary_minor": 3500000,
  "preferred_language": "bn",
  "onboarding_completed": true,
  "created_at": "2026-09-01T00:00:00Z",
  "updated_at": "2026-09-12T14:00:00Z"
}
```

---

## 4. How to Switch from Firebase to REST in SaveBetter

Switching requires changing only two dependency injection bindings in Hilt:

### In `core/di/DataModule.kt`:
```kotlin
// Change Firebase bindings:
// @Binds abstract fun bindExpenseRemoteDataSource(impl: FirebaseExpenseRemoteDataSource): ExpenseRemoteDataSource

// To REST bindings:
@Binds
abstract fun bindExpenseRemoteDataSource(impl: RestExpenseRemoteDataSource): ExpenseRemoteDataSource

@Binds
abstract fun bindCategoryRemoteDataSource(impl: RestCategoryRemoteDataSource): CategoryRemoteDataSource

@Binds
abstract fun bindTargetRemoteDataSource(impl: RestTargetRemoteDataSource): TargetRemoteDataSource

@Binds
abstract fun bindDebtCreditRemoteDataSource(impl: RestDebtCreditRemoteDataSource): DebtCreditRemoteDataSource

@Binds
abstract fun bindUserProfileRemoteDataSource(impl: RestUserProfileRemoteDataSource): UserProfileRemoteDataSource
```

### In `core/di/AuthModule.kt`:
```kotlin
// Change from FirebaseAuthRemoteDataSource:
@Binds
abstract fun bindAuthRemoteDataSource(impl: RestAuthRemoteDataSource): AuthRemoteDataSource
```

**That's it!** No ViewModels, Composables, Room entities, or business use-cases need any change.
