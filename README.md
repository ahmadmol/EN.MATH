# EN.MATH

Android mathematics learning demo built with Kotlin, Jetpack Compose and Material 3. Arabic RTL is the default; English LTR and System/Light/Dark themes are supported. Student and teacher experiences have separate five-tab navigation, with a navigation rail on wide displays and adaptive two-column content.

## Build and run

Open this folder in Android Studio, select JDK 17 for Gradle, install Android SDK 35, and run `app` on API 24 or later.

```sh
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
```

Windows: use `gradlew.bat`. AGP 8.9.2, Kotlin 2.1.20, Gradle 8.11.1, Compose BOM 2025.04.01, Navigation 2.8.9 and Lifecycle 2.8.7 are retained.

```sh
adb devices
adb -s DEVICE_SERIAL install -r app/build/outputs/apk/debug/app-debug.apk
adb -s DEVICE_SERIAL shell am start -n com.ahmadmol.enmath/.MainActivity
```

## Demo walkthrough

- Login accepts a valid email and an eight-character password. OTP is **123456**. Registration/recovery validate input but do not create real accounts or send mail.
- Student: Home, Study, Solver, Progress, Profile. Study opens books, topics, lessons, six-question practice sets and four-question mini quizzes.
- Practice supports choice, numeric, expression and true/false answers, hints, retry, explanation and results. Mastery is a transparent demo calculation: 40% completed lessons + 40% practice accuracy + 20% best quiz.
- Solver has cursor insertion, undo/delete/clear, six math-key groups, live offline KaTeX preview, seven curated examples, step explanations, methods, bookmarks and persisted history. Unrecognized problems explicitly remain unsupported.
- Explore graphs safe polynomials and basic trigonometric functions, including x^2, sin(x) and 2x+1; axes, grid, pan, zoom, visibility, known intercepts and a value table are available.
- Assignments support upcoming/completed/overdue lists, work, confirmed submission and results with teacher feedback.
- Teacher: Dashboard, Classes, Assignments, Resources, Profile. Create a class/invite code, inspect students, publish assignments through four steps, inspect responses, add feedback, and create/share resources to a class.
- Sign out and switch demo roles to see Ahmed's latest progress, submissions and teacher-shared resources reflected consistently across both roles.
- Search, library, inbox, reminder preferences, language, theme and local data persist on the device.

## Architecture

Source: `app/src/main/java/com/ahmadmol/enmath/`.

| Area | Responsibility |
| --- | --- |
| core/model | Typed catalog, session, classroom, question, attempt, submission and resource models |
| core/data | Seed data, repository, versioned local persistence, grading/mastery/statistics |
| core/AppViewModel.kt | Session and repository-backed actions shared across features |
| core/design | Theme, typography, spacing, components, localized keys and offline math rendering |
| core/navigation | Public/student/teacher graphs, role guards, adaptive navigation |
| features/onboarding, features/auth | Entry and validated demo account workflows |
| features/courses, features/practice | Study, lessons, checkpoints, practice, quizzes/results |
| features/solver, features/explore | Replaceable SolverEngine and GraphEngine implementations |
| Student hub features | Home, progress, assignments, library, search, notifications, profile/settings |
| features/teacher | Dashboard, classrooms/students, assignment builder/results, resources |

Screen state comes from the repository; UI does not invent separate statistics. LearningRules is shared between student and teacher views. ProductCodec persists user-generated demo records. Repository and engine interfaces are boundaries for future services; curriculum is seeded locally.

Static UI strings are in `res/values/ui_strings.xml` and `res/values-ar/ui_strings.xml`, accessed through typed TextKey values. Bilingual lesson/seed content is modeled as data. `tools/localize.py` maintains resources. Fonts and KaTeX are bundled offline; licenses and hashes are in `docs/licenses/` and `docs/vendor-assets.json`.

## Dataset and limits

Two books, eight topics, 24 representative lessons, 48 mixed questions, 16 practice/quiz sets, Ahmed plus 24 classmates, two classes, seeded assignments/submissions, four-week Ahmed activity, notifications and teacher resources form one consistent local demo.

This is a functional prototype with simulated accounts and a shared local persona, not production authentication or user isolation. There is no backend, AI/general solver, real OCR, email delivery, remote enrollment or scheduled/push notification service. Camera/upload recognition and external sharing are explicit placeholders. Lessons demonstrate the structure rather than a verified complete official syllabus. Graph parsing is limited. Backend schema/access guidance is documented, not provisioned.

## Validation and documentation

- DemoLogicTest and ProductRulesTest cover answers, unsupported inputs, validation, curriculum consistency, mastery and grading.
- AppFlowTest and DataPersistenceTest cover accounts/navigation, study/practice, assignments, teacher publishing/resources/feedback, solver/history/scan/graphs, preferences and persistence.
- Instrumentation resets local demo preferences: use a dedicated emulator rather than a phone whose data must be retained.
- [Audit](docs/AUDIT.md), [routes](docs/ROUTES.md), [backend plan](docs/BACKEND.md), [implementation report](docs/IMPLEMENTATION-REPORT.md).
- Build/UI logs and screenshots: docs/qa and docs/screenshots.

No commit or push is part of this delivery.
