# TASKNOVA: Native Android AI Academic Workflow Automation App

[![Platform](https://img.shields.io/badge/Platform-Native%20Android%20%7C%20Kotlin%20%7C%20Jetpack%20Compose-blue.svg)](https://developer.android.com)
[![Backend](https://img.shields.io/badge/Backend-FastAPI%20%7C%20Python%203.11%2B%20%7C%20PostgreSQL-green.svg)](https://fastapi.tiangolo.com)
[![AI Providers](https://img.shields.io/badge/AI-OpenAI%20GPT--4o%20%7C%20Google%20Gemini%201.5%20Pro-purple.svg)](https://ai.google.dev)
[![Build Status](https://img.shields.io/badge/Android%20Build-Passing%20(APK%20%26%20AAB)-brightgreen.svg)]()

> **Core Product Principle**:  
> *"Most productivity applications tell students what they need to do. This application should help them make sure it actually gets done."*  
> The student remains in complete control. The AI prepares and executes authorized workflows, but final external submissions always require explicit student authorization.

---

## 1. Architectural Overview

AcademicFlow is built from the ground up as a native Android application following **Clean Architecture + MVVM** principles, paired with a resilient, async **Python FastAPI** cloud backend.

```
+-----------------------------------------------------------------------------------------+
|                                    NATIVE ANDROID APP                                   |
|                                                                                         |
|  Presentation:  Jetpack Compose | Material 3 | Navigation Compose | Material Icons      |
|                 5 Primary Tabs: HOME | AI ASSISTANT | TASKS | CLOUD | ACTIVITY          |
|                                                                                         |
|  Domain:        UseCases | Clean Entities | StateFlow ViewModels                        |
|                                                                                         |
|  Data Layer:    Room Database (Local Cache) | DataStore Preferences                     |
|                 Android Keystore (SecureStorage) | Retrofit & OkHttp                    |
|                 WorkManager (Background 15-min Deadline Sync)                           |
+-----------------------------------------------------------------------------------------+
                                      |   REST / JSON / OAuth
                                      v
+-----------------------------------------------------------------------------------------+
|                                  FASTAPI CLOUD BACKEND                                  |
|                                                                                         |
|  Core Services: Model-Independent AI Service (OpenAI GPT-4o & Google Gemini 1.5 Pro)   |
|                 Controlled Tool-Calling Agent Loop                                      |
|                 Command-Authorized Submission Engine (Idempotency Shield)               |
|                 Universal Task Memory (PostgreSQL + pgvector Semantic Context)          |
|                 Document Generator (Markdown + Automated FPDF2 PDF Rendering)           |
|                 Contextual Deadline Monitor (5d, 3d, 2d, 1d, 3h Alerts)                 |
|                                                                                         |
|  Connectors:    Google (Classroom, Drive v3, Gmail v1, Calendar v3)                     |
|                 Microsoft 365 (Teams Education, OneDrive, Outlook Mail, Calendar)       |
|                                                                                         |
|  Background:    Celery Workers + Redis Broker + Celery Beat Periodic Scheduler         |
+-----------------------------------------------------------------------------------------+
```

---

## 2. Key Modules & Features

### 1. Mandatory Security Engine: Command-Authorized Submission
- **Problem**: Autonomous AI agents should never blindly submit academic work without explicit human consent.
- **Solution**: The AI agent independently indexes instructions, finds or drafts the relevant document, formats academic citations, and compiles the submission package into `PREPARED_FOR_REVIEW` state.
- **Enforcement**: The backend strictly rejects submission requests unless accompanied by a verified **Student Command String** and **One-Time Idempotency Token**.
- **Auditability**: Every action is cryptographically logged in the academic audit trail with real platform transaction receipts.

### 2. Model-Independent AI Copilot
- Supports **OpenAI GPT-4o** and **Google Gemini 1.5 Pro / Flash**.
- In-flight model switching directly from the Android chat interface.
- Imports student conversation exports from ChatGPT and Google Gemini.
- Perimeter defense against prompt injection from untrusted external syllabus documents and institutional emails.

### 3. Universal Cloud Connectors
- **Google Ecosystem**: Google Classroom API v1, Google Drive API v3, Gmail API v1, Google Calendar API v3.
- **Microsoft 365 Ecosystem**: Microsoft Graph Education (Teams Classes & Assignments), OneDrive, Outlook Mail, Outlook Calendar.
- Individual connection management: each service can be connected or disconnected independently in Settings.
- Institutional Administrator Approval identification: Clearly flags institutional tenant policies when admin consent (`EduAssignments.ReadWrite.All`) is required.

### 4. Intelligent Deadline Monitoring & Follow-Up Automation
- **5 Days Before**: Assignment detected and coursework folder synchronized.
- **3 Days Before**: Progress check against syllabus.
- **2 Days Before**: Missing document warning with quick-action AI document generator.
- **1 Day Before**: High-priority urgent deadline alert.
- **3 Hours Before**: Submission readiness notification with instant review link.
- Battery-conscious execution via Android **WorkManager** and server-originated **FCM** push alerts.

### 5. Attendance Management
- Real-time subject-wise percentage calculations.
- Automatic shortage detection against customizable target policies (e.g. 75% minimum).
- Formula-driven analytics calculating exact lecture counts needed to recover or safe margin of lectures that can be missed.

### 6. Universal Task Memory
- Unified PostgreSQL vector store (`pgvector`) associating:
  - Course assignments & instructions
  - Cloud documents (PDF, DOCX, code)
  - Student chat sessions & imported AI exports
  - Submission receipts & timestamps

---

## 3. Native Android Application Structure

The Android project is organized under `android/app/src/main/`:

```
com.academic.workflow/
├── AcademicFlowApplication.kt       # Application class, notification channels, WorkManager init
├── core/
│   ├── di/AppContainer.kt           # Dependency injection container
│   ├── network/                     # Retrofit, OkHttp, AuthInterceptor, ApiService
│   ├── security/SecureStorage.kt    # Android Keystore-backed AES256 encrypted storage
│   ├── theme/                       # Material 3 Color, Type, and Theme (Light & Dark)
│   └── worker/DeadlineSyncWorker.kt # WorkManager periodic background sync
├── data/
│   ├── local/                       # Room Database (AppDatabase, Entities, DAOs)
│   ├── preferences/                 # Jetpack DataStore preferences
│   └── repository/                  # Auth, Assignment, AI, Submission, Cloud, Attendance
├── domain/model/                    # Clean Architecture domain data models
└── presentation/
    ├── MainActivity.kt              # Scaffold, Animated NavigationBar, Deep link router
    ├── navigation/                  # Screen routes & NavGraph with animated transitions
    ├── components/                  # AcademicCard, StatusBadge, ShimmerLoading, CommandDialog
    ├── auth/                        # Email & OAuth Sign-In screens
    ├── home/                        # Today's schedule, deadlines, attendance overview
    ├── assistant/                   # Conversational AI UI, model selector, export importer
    ├── tasks/                       # Filterable assignments list & calendar mode
    ├── cloud/                       # Drive & OneDrive browser + AI Document Generator
    ├── activity/                    # Full academic audit log & verified receipts
    ├── submission/                  # Command-Authorized submission confirmation screen
    └── settings/                    # Integration connectors, dark mode, server config
```

---

## 4. Backend Application Structure

The FastAPI backend is organized under `backend/app/`:

```
backend/
├── app/
│   ├── main.py                      # FastAPI app entrypoint, CORS, lifecycle
│   ├── core/                        # Settings (pydantic-settings), Security (bcrypt, JWT), Database
│   ├── models/                      # SQLAlchemy models (User, Assignment, Submission, etc.)
│   ├── schemas/                     # Pydantic schemas for request/response validation
│   ├── connectors/                  # Google Classroom/Drive/Gmail & Microsoft Teams/OneDrive/Outlook
│   ├── services/                    # AIService, AIAgent, SubmissionEngine, DocumentGenerator
│   ├── workers/                     # Celery background tasks & periodic scheduler
│   └── api/v1/                      # REST endpoints for all features
├── alembic/                         # Database migrations (PostgreSQL / SQLite)
├── tests/                           # Pytest test suite (100% passing)
├── Dockerfile                       # Multi-stage production container
└── requirements.txt                 # Pinned, tested Python dependencies
```

---

## 5. Quickstart & Installation Instructions

### Prerequisites
- **JDK 17** (Eclipse Adoptium Temurin 17 or OpenJDK 17)
- **Android SDK Platform 34** with Build Tools 34.0.0
- **Python 3.11+**
- **Docker & Docker Compose** (Optional, for containerized stack)

---

### Step 1: Start the Backend Stack

#### Option A: Docker Compose (Recommended for Production)
```bash
# Copy and configure environment variables
cp .env.example .env

# Launch PostgreSQL (with pgvector), Redis, FastAPI API, Celery Worker, and Celery Beat
docker-compose up -d --build
```
The backend API will be live at `http://localhost:8000` with interactive Swagger docs at `http://localhost:8000/docs`.

#### Option B: Local Python Virtual Environment
```bash
cd backend
python -m venv venv

# Windows
.\venv\Scripts\activate
# Linux/macOS
source venv/bin/activate

pip install -r requirements.txt
uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
```

---

### Step 2: Build the Android Application

#### Generate Debug APK
```powershell
cd android
$env:JAVA_HOME = "C:\Users\nikes\AndroidDevTools\jdk17"
$env:ANDROID_HOME = "C:\Users\nikes\AndroidDevTools\android-sdk"
& "C:\Users\nikes\AndroidDevTools\gradle-8.5\bin\gradle.bat" assembleDebug
```
The compiled debug APK will be generated at:
`android/app/build/outputs/apk/debug/app-debug.apk`

#### Install on Connected Device or Emulator
```bash
adb install -r android/app/build/outputs/apk/debug/app-debug.apk
```

#### Generate Signed Release App Bundle (AAB) & APK
```powershell
& "C:\Users\nikes\AndroidDevTools\gradle-8.5\bin\gradle.bat" bundleRelease assembleRelease
```
Artifacts generated:
- Release AAB: `android/app/build/outputs/bundle/release/app-release.aab`
- Release APK: `android/app/build/outputs/apk/release/app-release.apk`

---

## 6. Running Automated Tests

### Run Backend Test Suite (Pytest)
```bash
cd backend
$env:PYTHONPATH = "backend"
& "backend/venv/Scripts/pytest.exe" tests/ -v
```
**Results**:
- `test_auth.py`: Password hashing and JWT encoding/decoding passed.
- `test_ai_agent.py`: Tool calling, context queries, and prompt injection filters passed.
- `test_submission_engine.py`: Explicit student command enforcement and idempotency protection passed.
- `test_attendance.py`: Percentage calculations and shortage recovery math passed.
- `test_connectors.py`: Google Classroom, MS Teams, Drive, and OneDrive wrappers passed.

### Run Android Unit Test Suite (JUnit + Gradle)
```powershell
cd android
$env:JAVA_HOME = "C:\Users\nikes\AndroidDevTools\jdk17"
$env:ANDROID_HOME = "C:\Users\nikes\AndroidDevTools\android-sdk"
& "C:\Users\nikes\AndroidDevTools\gradle-8.5\bin\gradle.bat" testDebugUnitTest
```
**Results**:
- `AttendanceCalculationTest`: Verified 75% target thresholds, shortage alerts, and lecture projections.
- `CommandAuthorizationTest`: Verified idempotency tokens and security constraints on `PreparedSubmission`.

---

## 7. Security & Compliance Reference

| Category | Implementation Strategy |
|---|---|
| **AI Secret Isolation** | API keys for OpenAI and Gemini are kept exclusively on the backend server. The Android client contains 0 AI secrets. |
| **Submission Safeguard** | No automated or scheduled submissions are ever permitted. Every submission requires a manual command entered by the student on the native confirmation screen. |
| **Local Credential Safety** | Android Keystore-backed AES256-GCM encryption (`EncryptedSharedPreferences`) stores user auth tokens and OAuth refresh tokens. |
| **Data Isolation** | Multi-tenant isolation at the database level: all task memories, files, and assignments are explicitly constrained by `user_id`. |
| **Duplicate Protection** | Every prepared submission issues an `idempotency_token`. Subsequent attempts with the same token are automatically blocked. |

---

## 8. License & Institutional Disclaimer
This project is engineered for educational workflow automation. Integration with Google Workspace for Education and Microsoft 365 Education utilizes official REST APIs. Features requiring tenant-wide administrator consent (such as modifying assignments or turning in student courseWork programmatically) are documented with appropriate fallback workflows for institutions with restricted API policies.
