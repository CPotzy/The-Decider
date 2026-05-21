# The-Decider MVP Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build the MVP of The-Decider Android app — a scaffolded Kotlin/Compose/Room project with a GitHub Actions CI pipeline producing downloadable APKs, a seeded data layer, a context-aware selection engine, and a swipeable queue screen that lets the user decide → done → snooze tasks. At the end of this plan, a usable APK is installable from a GitHub Release.

**Architecture:** Native Android with Kotlin + Jetpack Compose for UI, Room for SQLite persistence, manual dependency injection (no Hilt — keeps build simple), Kotlin coroutines + Flow for async/reactive data. Single-activity app with one Compose `NavHost`. Selection logic is pure Kotlin and unit-tested without Android dependencies.

**Tech Stack:** Kotlin 2.0, Jetpack Compose (BOM 2025.01), Room 2.6, Kotlin Coroutines 1.8, JUnit 4 + kotlinx-coroutines-test, AndroidX Test (Room in-memory), GitHub Actions.

**Local prerequisites:** JDK 17 (`brew install --cask temurin@17`), Android SDK with platform 34 (install via Android Studio or `sdkmanager`). Gradle wrapper handles the Gradle version itself.

**Source spec:** `docs/superpowers/specs/2026-05-21-the-decider-design.md`

**Scope of this plan (Plan 1 of 3):** Phases 0–5 below. Out of scope for this plan but covered by future plans: Task Detail / Checklist / Focus mode, Step Breakdown via Claude API, Quick-add sheet, Task Management UI, History screen, Settings screen, Notifications (NudgeWorker), Rollover worker.

---

## File Structure (created in this plan)

```
The-Decider/
├── .github/workflows/build.yml                          # CI: build debug APK + release on tag
├── settings.gradle.kts                                  # Gradle settings
├── build.gradle.kts                                     # Root Gradle build
├── gradle/libs.versions.toml                            # Version catalog
├── gradle.properties                                    # Gradle properties
├── app/
│   ├── build.gradle.kts                                 # App module build
│   ├── proguard-rules.pro                               # Proguard (debug only — empty)
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── assets/tasks-list.md                     # Copied seed file
│       │   └── java/com/cpotzy/thedecider/
│       │       ├── App.kt                               # Application + manual DI graph
│       │       ├── MainActivity.kt                      # Single activity hosting Compose
│       │       ├── domain/
│       │       │   ├── model/
│       │       │   │   ├── Cadence.kt                   # enum + helpers (cadenceDays)
│       │       │   │   ├── Energy.kt                    # enum
│       │       │   │   ├── Duration.kt                  # enum (Quick/Short/Medium/Long)
│       │       │   │   ├── TimeWindow.kt                # enum + currentWindow()
│       │       │   │   ├── PressureTier.kt              # enum + tierFor(pressure)
│       │       │   │   └── Task.kt                      # domain model (separate from entity)
│       │       │   ├── select/
│       │       │   │   ├── PressureCalculator.kt
│       │       │   │   ├── ContextFilter.kt
│       │       │   │   ├── ModeChip.kt                  # data class for chip filters
│       │       │   │   └── SelectionService.kt
│       │       │   └── time/
│       │       │       └── Clock.kt                     # injectable Clock interface
│       │       ├── data/
│       │       │   ├── db/
│       │       │   │   ├── AppDatabase.kt
│       │       │   │   ├── Converters.kt                # Instant/enum converters
│       │       │   │   ├── entities/
│       │       │   │   │   ├── TaskEntity.kt
│       │       │   │   │   ├── StepEntity.kt
│       │       │   │   │   ├── CompletionEntity.kt
│       │       │   │   │   └── SnoozeEntity.kt
│       │       │   │   └── dao/
│       │       │   │       ├── TaskDao.kt
│       │       │   │       ├── StepDao.kt
│       │       │   │       ├── CompletionDao.kt
│       │       │   │       └── SnoozeDao.kt
│       │       │   ├── repo/
│       │       │   │   ├── TaskRepository.kt
│       │       │   │   ├── CompletionRepository.kt
│       │       │   │   └── SnoozeRepository.kt
│       │       │   └── seed/
│       │       │       └── TaskSeeder.kt                # parses bundled tasks-list.md
│       │       └── ui/
│       │           ├── queue/
│       │           │   ├── QueueScreen.kt
│       │           │   ├── QueueViewModel.kt
│       │           │   └── components/
│       │           │       ├── TaskCard.kt
│       │           │       ├── SwipeChooserSheet.kt
│       │           │       └── ModeChipRow.kt
│       │           └── theme/
│       │               ├── Color.kt
│       │               ├── Theme.kt
│       │               └── Type.kt
│       ├── test/java/com/cpotzy/thedecider/
│       │   ├── domain/
│       │   │   ├── model/
│       │   │   │   ├── CadenceTest.kt
│       │   │   │   ├── TimeWindowTest.kt
│       │   │   │   └── PressureTierTest.kt
│       │   │   └── select/
│       │   │       ├── PressureCalculatorTest.kt
│       │   │       ├── ContextFilterTest.kt
│       │   │       └── SelectionServiceTest.kt
│       │   └── data/
│       │       └── seed/
│       │           └── TaskSeederTest.kt
│       └── androidTest/java/com/cpotzy/thedecider/
│           └── data/db/
│               ├── TaskDaoTest.kt
│               ├── CompletionDaoTest.kt
│               └── SnoozeDaoTest.kt
```

---

## Phase 0 — Project Scaffolding

### Task 0.1: Initialize Gradle project files

**Files:**
- Create: `settings.gradle.kts`
- Create: `build.gradle.kts`
- Create: `gradle.properties`
- Create: `gradle/libs.versions.toml`

- [ ] **Step 1: Write `settings.gradle.kts`**

```kotlin
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "The-Decider"
include(":app")
```

- [ ] **Step 2: Write root `build.gradle.kts`**

```kotlin
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
}
```

- [ ] **Step 3: Write `gradle.properties`**

```
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
org.gradle.parallel=true
org.gradle.caching=true
android.useAndroidX=true
android.nonTransitiveRClass=true
kotlin.code.style=official
```

- [ ] **Step 4: Write `gradle/libs.versions.toml`**

```toml
[versions]
agp = "8.5.2"
kotlin = "2.0.20"
ksp = "2.0.20-1.0.25"
compose-bom = "2025.01.00"
activity-compose = "1.9.3"
lifecycle = "2.8.7"
navigation = "2.8.5"
room = "2.6.1"
coroutines = "1.8.1"
junit = "4.13.2"
junit-ext = "1.2.1"
espresso = "3.6.1"
androidx-test = "1.6.1"
coroutines-test = "1.8.1"

[libraries]
androidx-core-ktx = { module = "androidx.core:core-ktx", version = "1.13.1" }
androidx-activity-compose = { module = "androidx.activity:activity-compose", version.ref = "activity-compose" }
androidx-lifecycle-viewmodel-compose = { module = "androidx.lifecycle:lifecycle-viewmodel-compose", version.ref = "lifecycle" }
compose-bom = { module = "androidx.compose:compose-bom", version.ref = "compose-bom" }
compose-ui = { module = "androidx.compose.ui:ui" }
compose-ui-tooling-preview = { module = "androidx.compose.ui:ui-tooling-preview" }
compose-ui-tooling = { module = "androidx.compose.ui:ui-tooling" }
compose-material3 = { module = "androidx.compose.material3:material3" }
compose-foundation = { module = "androidx.compose.foundation:foundation" }
navigation-compose = { module = "androidx.navigation:navigation-compose", version.ref = "navigation" }
room-runtime = { module = "androidx.room:room-runtime", version.ref = "room" }
room-ktx = { module = "androidx.room:room-ktx", version.ref = "room" }
room-compiler = { module = "androidx.room:room-compiler", version.ref = "room" }
room-testing = { module = "androidx.room:room-testing", version.ref = "room" }
coroutines-core = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-core", version.ref = "coroutines" }
coroutines-android = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-android", version.ref = "coroutines" }
coroutines-test = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-test", version.ref = "coroutines-test" }
junit = { module = "junit:junit", version.ref = "junit" }
androidx-test-runner = { module = "androidx.test:runner", version.ref = "androidx-test" }
androidx-junit = { module = "androidx.test.ext:junit", version.ref = "junit-ext" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
```

- [ ] **Step 5: Commit**

```bash
git add settings.gradle.kts build.gradle.kts gradle.properties gradle/libs.versions.toml
git commit -m "scaffold: gradle project + version catalog"
```

---

### Task 0.2: Create app module build file

**Files:**
- Create: `app/build.gradle.kts`
- Create: `app/proguard-rules.pro`

- [ ] **Step 1: Write `app/build.gradle.kts`**

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.cpotzy.thedecider"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.cpotzy.thedecider"
        minSdk = 28
        targetSdk = 34
        versionCode = 1
        versionName = "0.1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures { compose = true }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.foundation)
    implementation(libs.navigation.compose)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)

    debugImplementation(libs.compose.ui.tooling)

    testImplementation(libs.junit)
    testImplementation(libs.coroutines.test)

    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.room.testing)
    androidTestImplementation(libs.coroutines.test)
}
```

- [ ] **Step 2: Write empty `app/proguard-rules.pro`**

```
# Empty for now — release builds disabled until signing is configured
```

- [ ] **Step 3: Commit**

```bash
git add app/build.gradle.kts app/proguard-rules.pro
git commit -m "scaffold: app module gradle config"
```

---

### Task 0.3: Manifest and Application class

**Files:**
- Create: `app/src/main/AndroidManifest.xml`
- Create: `app/src/main/java/com/cpotzy/thedecider/App.kt`
- Create: `app/src/main/java/com/cpotzy/thedecider/MainActivity.kt`

- [ ] **Step 1: Write `AndroidManifest.xml`**

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <application
        android:name=".App"
        android:label="The-Decider"
        android:allowBackup="false"
        android:supportsRtl="true"
        android:theme="@style/Theme.TheDecider">
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:theme="@style/Theme.TheDecider">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```

- [ ] **Step 2: Write `App.kt`** (manual DI graph — populated in later tasks)

```kotlin
package com.cpotzy.thedecider

import android.app.Application

class App : Application() {
    // Manual DI graph. Populated in Phase 2.
    lateinit var graph: AppGraph
        private set

    override fun onCreate() {
        super.onCreate()
        graph = AppGraph(this)
    }
}

class AppGraph(private val app: Application) {
    // Empty for now — entries added in later tasks
}
```

- [ ] **Step 3: Write `MainActivity.kt`** (placeholder Compose hello)

```kotlin
package com.cpotzy.thedecider

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.cpotzy.thedecider.ui.theme.TheDeciderTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TheDeciderTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Placeholder()
                }
            }
        }
    }
}

@Composable
private fun Placeholder() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("The-Decider — scaffold")
    }
}
```

- [ ] **Step 4: Create `res/values/themes.xml`** at `app/src/main/res/values/themes.xml`

```xml
<resources>
    <style name="Theme.TheDecider" parent="android:Theme.Material.Light.NoActionBar" />
</resources>
```

- [ ] **Step 5: Commit**

```bash
git add app/src/main/AndroidManifest.xml app/src/main/java app/src/main/res
git commit -m "scaffold: manifest, App, MainActivity placeholder"
```

---

### Task 0.4: Compose theme files

**Files:**
- Create: `app/src/main/java/com/cpotzy/thedecider/ui/theme/Color.kt`
- Create: `app/src/main/java/com/cpotzy/thedecider/ui/theme/Type.kt`
- Create: `app/src/main/java/com/cpotzy/thedecider/ui/theme/Theme.kt`

- [ ] **Step 1: Write `Color.kt`**

```kotlin
package com.cpotzy.thedecider.ui.theme

import androidx.compose.ui.graphics.Color

val Neutral = Color(0xFFEAEAEA)
val PressureAmber = Color(0xFFF5C16C)
val PressureRed = Color(0xFFE07A7A)
val SurfaceLight = Color(0xFFFAFAFA)
val OnSurfaceLight = Color(0xFF1B1B1B)
```

- [ ] **Step 2: Write `Type.kt`**

```kotlin
package com.cpotzy.thedecider.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val AppTypography = Typography(
    headlineLarge = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.SemiBold),
    titleLarge = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Medium),
    bodyLarge = TextStyle(fontSize = 16.sp),
    labelSmall = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium),
)
```

- [ ] **Step 3: Write `Theme.kt`**

```kotlin
package com.cpotzy.thedecider.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val ColorScheme = lightColorScheme(
    background = SurfaceLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
)

@Composable
fun TheDeciderTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ColorScheme,
        typography = AppTypography,
        content = content,
    )
}
```

- [ ] **Step 4: Verify build works**

Run: `./gradlew :app:assembleDebug`
Expected: BUILD SUCCESSFUL. APK at `app/build/outputs/apk/debug/app-debug.apk`.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/cpotzy/thedecider/ui/theme
git commit -m "scaffold: compose theme"
```

---

### Task 0.5: Gradle wrapper

**Files:**
- Create: `gradle/wrapper/gradle-wrapper.properties`
- Create: `gradlew`, `gradlew.bat`, `gradle/wrapper/gradle-wrapper.jar`

- [ ] **Step 1: Generate the Gradle wrapper**

Run: `gradle wrapper --gradle-version 8.10.2`
Expected: creates `gradlew`, `gradlew.bat`, and `gradle/wrapper/` files.
(If `gradle` is not installed, install via `brew install gradle` first.)

- [ ] **Step 2: Make wrapper executable**

Run: `chmod +x gradlew`

- [ ] **Step 3: Verify wrapper works**

Run: `./gradlew --version`
Expected: shows Gradle 8.10.2, Kotlin, JVM versions.

- [ ] **Step 4: Run a clean assemble through the wrapper**

Run: `./gradlew :app:assembleDebug`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 5: Commit**

```bash
git add gradlew gradlew.bat gradle/wrapper
git commit -m "scaffold: gradle wrapper"
```

---

## Phase 1 — CI/CD: Downloadable APK from GitHub

### Task 1.1: GitHub Actions workflow for debug builds

**Files:**
- Create: `.github/workflows/build.yml`

- [ ] **Step 1: Write the workflow**

```yaml
name: Build APK

on:
  push:
    branches: [ main ]
  workflow_dispatch:

jobs:
  build:
    runs-on: ubuntu-latest
    permissions:
      contents: write
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: 17

      - name: Set up Gradle
        uses: gradle/actions/setup-gradle@v3

      - name: Build debug APK
        run: ./gradlew :app:assembleDebug

      - name: Upload APK as artifact
        uses: actions/upload-artifact@v4
        with:
          name: the-decider-debug-${{ github.sha }}
          path: app/build/outputs/apk/debug/*.apk
          retention-days: 30

      - name: Update "latest" release on main
        if: github.ref == 'refs/heads/main'
        uses: softprops/action-gh-release@v2
        with:
          tag_name: latest
          name: Latest debug build
          body: |
            Latest debug APK built from commit ${{ github.sha }}.
            Install on your phone: enable "Install unknown apps" for your browser, then download and tap.
          files: app/build/outputs/apk/debug/*.apk
          make_latest: "true"
```

- [ ] **Step 2: Commit and push**

```bash
git add .github/workflows/build.yml
git commit -m "ci: build debug APK and publish to 'latest' release on every main push"
git push
```

- [ ] **Step 3: Verify the workflow runs successfully**

Run: `gh run watch`
Expected: workflow completes successfully. Check `gh release view latest` — you should see an APK attached.

- [ ] **Step 4: Download the APK on your phone**

Open `https://github.com/CPotzy/The-Decider/releases/latest` on your phone, download the APK, install it. Confirm the placeholder screen appears.

---

## Phase 2 — Data Layer

### Task 2.1: Domain enums

**Files:**
- Create: `app/src/main/java/com/cpotzy/thedecider/domain/model/Cadence.kt`
- Create: `app/src/main/java/com/cpotzy/thedecider/domain/model/Energy.kt`
- Create: `app/src/main/java/com/cpotzy/thedecider/domain/model/Duration.kt`
- Create: `app/src/main/java/com/cpotzy/thedecider/domain/model/TimeWindow.kt`
- Create: `app/src/test/java/com/cpotzy/thedecider/domain/model/CadenceTest.kt`
- Create: `app/src/test/java/com/cpotzy/thedecider/domain/model/TimeWindowTest.kt`

- [ ] **Step 1: Write failing `CadenceTest`**

```kotlin
package com.cpotzy.thedecider.domain.model

import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull

class CadenceTest {
    @Test fun `daily cadenceDays is 1`() = assertEquals(1L, Cadence.DAILY.cadenceDays)
    @Test fun `bidaily cadenceDays is 2`() = assertEquals(2L, Cadence.BIDAILY.cadenceDays)
    @Test fun `weekly cadenceDays is 7`() = assertEquals(7L, Cadence.WEEKLY.cadenceDays)
    @Test fun `biweekly cadenceDays is 14`() = assertEquals(14L, Cadence.BIWEEKLY.cadenceDays)
    @Test fun `monthly cadenceDays is 30`() = assertEquals(30L, Cadence.MONTHLY.cadenceDays)
    @Test fun `bimonthly cadenceDays is 60`() = assertEquals(60L, Cadence.BIMONTHLY.cadenceDays)
    @Test fun `anytime cadenceDays is null`() = assertNull(Cadence.ANYTIME.cadenceDays)
    @Test fun `oneoff cadenceDays is null`() = assertNull(Cadence.ONEOFF.cadenceDays)
}
```

- [ ] **Step 2: Run — should fail to compile**

Run: `./gradlew :app:testDebugUnitTest --tests "com.cpotzy.thedecider.domain.model.CadenceTest"`
Expected: compilation failure — `Cadence` not defined.

- [ ] **Step 3: Implement `Cadence`**

```kotlin
package com.cpotzy.thedecider.domain.model

enum class Cadence(val cadenceDays: Long?) {
    DAILY(1L),
    BIDAILY(2L),
    WEEKLY(7L),
    BIWEEKLY(14L),
    MONTHLY(30L),
    BIMONTHLY(60L),
    ANYTIME(null),
    ONEOFF(null);
}
```

- [ ] **Step 4: Run — should pass**

Run: `./gradlew :app:testDebugUnitTest --tests "com.cpotzy.thedecider.domain.model.CadenceTest"`
Expected: 8 tests pass.

- [ ] **Step 5: Implement `Energy.kt`**

```kotlin
package com.cpotzy.thedecider.domain.model

enum class Energy { LOW, MEDIUM, HIGH }
```

- [ ] **Step 6: Implement `Duration.kt`**

```kotlin
package com.cpotzy.thedecider.domain.model

enum class Duration(val maxMinutes: Int) {
    QUICK(5),
    SHORT(15),
    MEDIUM(30),
    LONG(Int.MAX_VALUE);
}
```

- [ ] **Step 7: Write failing `TimeWindowTest`**

```kotlin
package com.cpotzy.thedecider.domain.model

import org.junit.Test
import org.junit.Assert.assertEquals
import java.time.LocalTime

class TimeWindowTest {
    @Test fun `06_00 is MORNING`() = assertEquals(TimeWindow.MORNING, TimeWindow.atLocalTime(LocalTime.of(6, 0)))
    @Test fun `11_59 is MORNING`() = assertEquals(TimeWindow.MORNING, TimeWindow.atLocalTime(LocalTime.of(11, 59)))
    @Test fun `12_00 is AFTERNOON`() = assertEquals(TimeWindow.AFTERNOON, TimeWindow.atLocalTime(LocalTime.of(12, 0)))
    @Test fun `16_59 is AFTERNOON`() = assertEquals(TimeWindow.AFTERNOON, TimeWindow.atLocalTime(LocalTime.of(16, 59)))
    @Test fun `17_00 is EVENING`() = assertEquals(TimeWindow.EVENING, TimeWindow.atLocalTime(LocalTime.of(17, 0)))
    @Test fun `22_59 is EVENING`() = assertEquals(TimeWindow.EVENING, TimeWindow.atLocalTime(LocalTime.of(22, 59)))
    @Test fun `23_00 is NIGHT`() = assertEquals(TimeWindow.NIGHT, TimeWindow.atLocalTime(LocalTime.of(23, 0)))
    @Test fun `04_59 is NIGHT`() = assertEquals(TimeWindow.NIGHT, TimeWindow.atLocalTime(LocalTime.of(4, 59)))
    @Test fun `05_00 is MORNING`() = assertEquals(TimeWindow.MORNING, TimeWindow.atLocalTime(LocalTime.of(5, 0)))
}
```

- [ ] **Step 8: Implement `TimeWindow`**

```kotlin
package com.cpotzy.thedecider.domain.model

import java.time.LocalTime

enum class TimeWindow {
    MORNING, AFTERNOON, EVENING, NIGHT, ANYTIME;

    companion object {
        fun atLocalTime(t: LocalTime): TimeWindow = when {
            t >= LocalTime.of(5, 0) && t < LocalTime.of(12, 0) -> MORNING
            t >= LocalTime.of(12, 0) && t < LocalTime.of(17, 0) -> AFTERNOON
            t >= LocalTime.of(17, 0) && t < LocalTime.of(23, 0) -> EVENING
            else -> NIGHT
        }
    }
}
```

- [ ] **Step 9: Run all domain model tests**

Run: `./gradlew :app:testDebugUnitTest --tests "com.cpotzy.thedecider.domain.model.*"`
Expected: 17 tests pass.

- [ ] **Step 10: Commit**

```bash
git add app/src/main/java/com/cpotzy/thedecider/domain/model app/src/test/java/com/cpotzy/thedecider/domain/model
git commit -m "domain: cadence, energy, duration, time window enums + tests"
```

---

### Task 2.2: Domain Task model + Clock

**Files:**
- Create: `app/src/main/java/com/cpotzy/thedecider/domain/model/Task.kt`
- Create: `app/src/main/java/com/cpotzy/thedecider/domain/time/Clock.kt`

- [ ] **Step 1: Write `Task.kt`** (domain model — separate from entity)

```kotlin
package com.cpotzy.thedecider.domain.model

import java.time.Instant

data class Task(
    val id: Long,
    val title: String,
    val cadence: Cadence,
    val energy: Energy,
    val duration: Duration,
    val timeWindow: TimeWindow,
    val isActive: Boolean,
    val createdAt: Instant,
    val lastDoneAt: Instant?,
)
```

- [ ] **Step 2: Write `Clock.kt`** (injectable clock for tests)

```kotlin
package com.cpotzy.thedecider.domain.time

import java.time.Instant

fun interface Clock {
    fun now(): Instant

    companion object {
        val System: Clock = Clock { Instant.now() }
        fun fixed(at: Instant): Clock = Clock { at }
    }
}
```

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/cpotzy/thedecider/domain
git commit -m "domain: Task model + injectable Clock"
```

---

### Task 2.3: Room entities and converters

**Files:**
- Create: `app/src/main/java/com/cpotzy/thedecider/data/db/Converters.kt`
- Create: `app/src/main/java/com/cpotzy/thedecider/data/db/entities/TaskEntity.kt`
- Create: `app/src/main/java/com/cpotzy/thedecider/data/db/entities/StepEntity.kt`
- Create: `app/src/main/java/com/cpotzy/thedecider/data/db/entities/CompletionEntity.kt`
- Create: `app/src/main/java/com/cpotzy/thedecider/data/db/entities/SnoozeEntity.kt`

- [ ] **Step 1: Write `Converters.kt`**

```kotlin
package com.cpotzy.thedecider.data.db

import androidx.room.TypeConverter
import com.cpotzy.thedecider.domain.model.Cadence
import com.cpotzy.thedecider.domain.model.Duration
import com.cpotzy.thedecider.domain.model.Energy
import com.cpotzy.thedecider.domain.model.TimeWindow
import java.time.Instant

class Converters {
    @TypeConverter fun instantToLong(v: Instant?): Long? = v?.toEpochMilli()
    @TypeConverter fun longToInstant(v: Long?): Instant? = v?.let { Instant.ofEpochMilli(it) }

    @TypeConverter fun cadenceToString(v: Cadence): String = v.name
    @TypeConverter fun stringToCadence(v: String): Cadence = Cadence.valueOf(v)

    @TypeConverter fun energyToString(v: Energy): String = v.name
    @TypeConverter fun stringToEnergy(v: String): Energy = Energy.valueOf(v)

    @TypeConverter fun durationToString(v: Duration): String = v.name
    @TypeConverter fun stringToDuration(v: String): Duration = Duration.valueOf(v)

    @TypeConverter fun timeWindowToString(v: TimeWindow): String = v.name
    @TypeConverter fun stringToTimeWindow(v: String): TimeWindow = TimeWindow.valueOf(v)
}
```

- [ ] **Step 2: Write `TaskEntity.kt`**

```kotlin
package com.cpotzy.thedecider.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.cpotzy.thedecider.domain.model.Cadence
import com.cpotzy.thedecider.domain.model.Duration
import com.cpotzy.thedecider.domain.model.Energy
import com.cpotzy.thedecider.domain.model.TimeWindow
import java.time.Instant

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val cadence: Cadence,
    val energy: Energy,
    val duration: Duration,
    val timeWindow: TimeWindow,
    val isActive: Boolean = true,
    val createdAt: Instant,
)
```

- [ ] **Step 3: Write `StepEntity.kt`**

```kotlin
package com.cpotzy.thedecider.data.db.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "steps",
    foreignKeys = [ForeignKey(
        entity = TaskEntity::class,
        parentColumns = ["id"],
        childColumns = ["taskId"],
        onDelete = ForeignKey.CASCADE,
    )],
    indices = [Index("taskId")],
)
data class StepEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val taskId: Long,
    val order: Int,
    val content: String,
)
```

- [ ] **Step 4: Write `CompletionEntity.kt`**

```kotlin
package com.cpotzy.thedecider.data.db.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

enum class CompletionType { DONE, SKIPPED_PRESSURE_KEPT }

@Entity(
    tableName = "completions",
    foreignKeys = [ForeignKey(
        entity = TaskEntity::class,
        parentColumns = ["id"],
        childColumns = ["taskId"],
        onDelete = ForeignKey.CASCADE,
    )],
    indices = [Index("taskId"), Index("completedAt")],
)
data class CompletionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val taskId: Long,
    val completedAt: Instant,
    val type: CompletionType,
)
```

Also add converter in `Converters.kt`:

```kotlin
@TypeConverter fun completionTypeToString(v: CompletionType): String = v.name
@TypeConverter fun stringToCompletionType(v: String): CompletionType = CompletionType.valueOf(v)
```

- [ ] **Step 5: Write `SnoozeEntity.kt`**

```kotlin
package com.cpotzy.thedecider.data.db.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

enum class SnoozeKind { LATER_TODAY, TOMORROW, SKIP_CYCLE }

@Entity(
    tableName = "snoozes",
    foreignKeys = [ForeignKey(
        entity = TaskEntity::class,
        parentColumns = ["id"],
        childColumns = ["taskId"],
        onDelete = ForeignKey.CASCADE,
    )],
    indices = [Index("taskId"), Index("until")],
)
data class SnoozeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val taskId: Long,
    val until: Instant,
    val kind: SnoozeKind,
    val createdAt: Instant,
)
```

Also add converter:

```kotlin
@TypeConverter fun snoozeKindToString(v: SnoozeKind): String = v.name
@TypeConverter fun stringToSnoozeKind(v: String): SnoozeKind = SnoozeKind.valueOf(v)
```

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/cpotzy/thedecider/data/db
git commit -m "data: room entities + type converters"
```

---

### Task 2.4: Room DAOs

**Files:**
- Create: `app/src/main/java/com/cpotzy/thedecider/data/db/dao/TaskDao.kt`
- Create: `app/src/main/java/com/cpotzy/thedecider/data/db/dao/StepDao.kt`
- Create: `app/src/main/java/com/cpotzy/thedecider/data/db/dao/CompletionDao.kt`
- Create: `app/src/main/java/com/cpotzy/thedecider/data/db/dao/SnoozeDao.kt`

- [ ] **Step 1: Write `TaskDao.kt`**

```kotlin
package com.cpotzy.thedecider.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.cpotzy.thedecider.data.db.entities.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Insert suspend fun insert(task: TaskEntity): Long
    @Insert suspend fun insertAll(tasks: List<TaskEntity>): List<Long>
    @Update suspend fun update(task: TaskEntity)

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun byId(id: Long): TaskEntity?

    @Query("SELECT * FROM tasks WHERE isActive = 1")
    fun observeActive(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE isActive = 1")
    suspend fun listActive(): List<TaskEntity>

    @Query("SELECT COUNT(*) FROM tasks")
    suspend fun count(): Int

    @Query("UPDATE tasks SET isActive = :isActive WHERE id = :id")
    suspend fun setActive(id: Long, isActive: Boolean)
}
```

- [ ] **Step 2: Write `StepDao.kt`**

```kotlin
package com.cpotzy.thedecider.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.cpotzy.thedecider.data.db.entities.StepEntity

@Dao
interface StepDao {
    @Insert suspend fun insertAll(steps: List<StepEntity>)

    @Query("SELECT * FROM steps WHERE taskId = :taskId ORDER BY `order` ASC")
    suspend fun forTask(taskId: Long): List<StepEntity>
}
```

- [ ] **Step 3: Write `CompletionDao.kt`**

```kotlin
package com.cpotzy.thedecider.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.cpotzy.thedecider.data.db.entities.CompletionEntity
import com.cpotzy.thedecider.data.db.entities.CompletionType
import java.time.Instant

@Dao
interface CompletionDao {
    @Insert suspend fun insert(completion: CompletionEntity): Long

    @Query("""
        SELECT MAX(completedAt) FROM completions
        WHERE taskId = :taskId AND type = :type
    """)
    suspend fun lastOfType(taskId: Long, type: CompletionType = CompletionType.DONE): Instant?

    @Query("SELECT * FROM completions ORDER BY completedAt DESC LIMIT :limit")
    suspend fun recent(limit: Int = 100): List<CompletionEntity>
}
```

- [ ] **Step 4: Write `SnoozeDao.kt`**

```kotlin
package com.cpotzy.thedecider.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.cpotzy.thedecider.data.db.entities.SnoozeEntity
import java.time.Instant

@Dao
interface SnoozeDao {
    @Insert suspend fun insert(snooze: SnoozeEntity): Long

    @Query("""
        SELECT * FROM snoozes
        WHERE taskId = :taskId AND until > :now
        ORDER BY until DESC LIMIT 1
    """)
    suspend fun activeFor(taskId: Long, now: Instant): SnoozeEntity?

    @Query("""
        SELECT DISTINCT taskId FROM snoozes WHERE until > :now
    """)
    suspend fun activeTaskIds(now: Instant): List<Long>
}
```

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/cpotzy/thedecider/data/db/dao
git commit -m "data: room DAOs"
```

---

### Task 2.5: AppDatabase

**Files:**
- Create: `app/src/main/java/com/cpotzy/thedecider/data/db/AppDatabase.kt`

- [ ] **Step 1: Write `AppDatabase.kt`**

```kotlin
package com.cpotzy.thedecider.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.cpotzy.thedecider.data.db.dao.CompletionDao
import com.cpotzy.thedecider.data.db.dao.SnoozeDao
import com.cpotzy.thedecider.data.db.dao.StepDao
import com.cpotzy.thedecider.data.db.dao.TaskDao
import com.cpotzy.thedecider.data.db.entities.CompletionEntity
import com.cpotzy.thedecider.data.db.entities.SnoozeEntity
import com.cpotzy.thedecider.data.db.entities.StepEntity
import com.cpotzy.thedecider.data.db.entities.TaskEntity

@Database(
    entities = [TaskEntity::class, StepEntity::class, CompletionEntity::class, SnoozeEntity::class],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun stepDao(): StepDao
    abstract fun completionDao(): CompletionDao
    abstract fun snoozeDao(): SnoozeDao

    companion object {
        fun build(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, "thedecider.db").build()
    }
}
```

- [ ] **Step 2: Add schema export config** to `app/build.gradle.kts`

Add this block at the **top level** of `app/build.gradle.kts` (outside the `android { }` block, alongside `dependencies { }`):

```kotlin
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}
```

This tells Room's KSP processor to export schema JSON for each DB version.

- [ ] **Step 3: Verify compilation**

Run: `./gradlew :app:assembleDebug`
Expected: BUILD SUCCESSFUL. Schema file exists at `app/schemas/com.cpotzy.thedecider.data.db.AppDatabase/1.json`.

- [ ] **Step 4: Commit**

```bash
git add app/build.gradle.kts app/src/main/java/com/cpotzy/thedecider/data/db/AppDatabase.kt app/schemas
git commit -m "data: AppDatabase + schema export"
```

---

### Task 2.6: DAO instrumented tests

**Files:**
- Create: `app/src/androidTest/java/com/cpotzy/thedecider/data/db/TaskDaoTest.kt`
- Create: `app/src/androidTest/java/com/cpotzy/thedecider/data/db/CompletionDaoTest.kt`
- Create: `app/src/androidTest/java/com/cpotzy/thedecider/data/db/SnoozeDaoTest.kt`

These run on a connected device/emulator. Skip running them in CI for now; they verify Room integration locally.

- [ ] **Step 1: Write `TaskDaoTest.kt`**

```kotlin
package com.cpotzy.thedecider.data.db

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cpotzy.thedecider.data.db.entities.TaskEntity
import com.cpotzy.thedecider.domain.model.Cadence
import com.cpotzy.thedecider.domain.model.Duration
import com.cpotzy.thedecider.domain.model.Energy
import com.cpotzy.thedecider.domain.model.TimeWindow
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant

@RunWith(AndroidJUnit4::class)
class TaskDaoTest {
    private lateinit var db: AppDatabase
    private val dao get() = db.taskDao()

    @Before fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java,
        ).allowMainThreadQueries().build()
    }

    @After fun teardown() = db.close()

    @Test fun insertAndRetrieve() = runTest {
        val id = dao.insert(sampleTask("Test task"))
        val retrieved = dao.byId(id)
        assertEquals("Test task", retrieved?.title)
    }

    @Test fun listActiveExcludesInactive() = runTest {
        dao.insert(sampleTask("Active"))
        val inactiveId = dao.insert(sampleTask("Inactive"))
        dao.setActive(inactiveId, false)
        val active = dao.listActive()
        assertEquals(1, active.size)
        assertEquals("Active", active[0].title)
    }

    private fun sampleTask(title: String) = TaskEntity(
        title = title,
        cadence = Cadence.DAILY,
        energy = Energy.LOW,
        duration = Duration.QUICK,
        timeWindow = TimeWindow.ANYTIME,
        createdAt = Instant.parse("2026-01-01T00:00:00Z"),
    )
}
```

- [ ] **Step 2: Write `CompletionDaoTest.kt`**

```kotlin
package com.cpotzy.thedecider.data.db

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cpotzy.thedecider.data.db.entities.CompletionEntity
import com.cpotzy.thedecider.data.db.entities.CompletionType
import com.cpotzy.thedecider.data.db.entities.TaskEntity
import com.cpotzy.thedecider.domain.model.Cadence
import com.cpotzy.thedecider.domain.model.Duration
import com.cpotzy.thedecider.domain.model.Energy
import com.cpotzy.thedecider.domain.model.TimeWindow
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant

@RunWith(AndroidJUnit4::class)
class CompletionDaoTest {
    private lateinit var db: AppDatabase

    @Before fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java,
        ).allowMainThreadQueries().build()
    }

    @After fun teardown() = db.close()

    @Test fun lastDoneIsLatestDoneCompletion() = runTest {
        val taskId = db.taskDao().insert(TaskEntity(
            title = "X", cadence = Cadence.DAILY, energy = Energy.LOW, duration = Duration.QUICK,
            timeWindow = TimeWindow.ANYTIME, createdAt = Instant.parse("2026-01-01T00:00:00Z"),
        ))
        val t1 = Instant.parse("2026-01-02T10:00:00Z")
        val t2 = Instant.parse("2026-01-03T10:00:00Z")
        db.completionDao().insert(CompletionEntity(taskId = taskId, completedAt = t1, type = CompletionType.DONE))
        db.completionDao().insert(CompletionEntity(taskId = taskId, completedAt = t2, type = CompletionType.DONE))
        assertEquals(t2, db.completionDao().lastOfType(taskId, CompletionType.DONE))
    }

    @Test fun skippedDoesNotCountAsDone() = runTest {
        val taskId = db.taskDao().insert(TaskEntity(
            title = "X", cadence = Cadence.DAILY, energy = Energy.LOW, duration = Duration.QUICK,
            timeWindow = TimeWindow.ANYTIME, createdAt = Instant.parse("2026-01-01T00:00:00Z"),
        ))
        db.completionDao().insert(CompletionEntity(
            taskId = taskId, completedAt = Instant.parse("2026-01-02T10:00:00Z"),
            type = CompletionType.SKIPPED_PRESSURE_KEPT,
        ))
        assertNull(db.completionDao().lastOfType(taskId, CompletionType.DONE))
    }
}
```

- [ ] **Step 3: Write `SnoozeDaoTest.kt`**

```kotlin
package com.cpotzy.thedecider.data.db

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cpotzy.thedecider.data.db.entities.SnoozeEntity
import com.cpotzy.thedecider.data.db.entities.SnoozeKind
import com.cpotzy.thedecider.data.db.entities.TaskEntity
import com.cpotzy.thedecider.domain.model.Cadence
import com.cpotzy.thedecider.domain.model.Duration
import com.cpotzy.thedecider.domain.model.Energy
import com.cpotzy.thedecider.domain.model.TimeWindow
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant

@RunWith(AndroidJUnit4::class)
class SnoozeDaoTest {
    private lateinit var db: AppDatabase

    @Before fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java,
        ).allowMainThreadQueries().build()
    }

    @After fun teardown() = db.close()

    @Test fun activeForReturnsActiveSnooze() = runTest {
        val taskId = db.taskDao().insert(TaskEntity(
            title = "X", cadence = Cadence.DAILY, energy = Energy.LOW, duration = Duration.QUICK,
            timeWindow = TimeWindow.ANYTIME, createdAt = Instant.parse("2026-01-01T00:00:00Z"),
        ))
        val now = Instant.parse("2026-01-01T12:00:00Z")
        val until = Instant.parse("2026-01-01T18:00:00Z")
        db.snoozeDao().insert(SnoozeEntity(
            taskId = taskId, until = until, kind = SnoozeKind.LATER_TODAY, createdAt = now,
        ))
        val active = db.snoozeDao().activeFor(taskId, now)
        assertEquals(until, active?.until)
    }

    @Test fun expiredSnoozeNotReturned() = runTest {
        val taskId = db.taskDao().insert(TaskEntity(
            title = "X", cadence = Cadence.DAILY, energy = Energy.LOW, duration = Duration.QUICK,
            timeWindow = TimeWindow.ANYTIME, createdAt = Instant.parse("2026-01-01T00:00:00Z"),
        ))
        val now = Instant.parse("2026-01-01T20:00:00Z")
        db.snoozeDao().insert(SnoozeEntity(
            taskId = taskId,
            until = Instant.parse("2026-01-01T18:00:00Z"),
            kind = SnoozeKind.LATER_TODAY,
            createdAt = Instant.parse("2026-01-01T12:00:00Z"),
        ))
        assertNull(db.snoozeDao().activeFor(taskId, now))
    }
}
```

- [ ] **Step 4: Commit**

```bash
git add app/src/androidTest
git commit -m "data: DAO instrumented tests"
```

---

### Task 2.7: Repositories

**Files:**
- Create: `app/src/main/java/com/cpotzy/thedecider/data/repo/TaskRepository.kt`
- Create: `app/src/main/java/com/cpotzy/thedecider/data/repo/CompletionRepository.kt`
- Create: `app/src/main/java/com/cpotzy/thedecider/data/repo/SnoozeRepository.kt`

- [ ] **Step 1: Write `TaskRepository.kt`**

```kotlin
package com.cpotzy.thedecider.data.repo

import com.cpotzy.thedecider.data.db.dao.CompletionDao
import com.cpotzy.thedecider.data.db.dao.TaskDao
import com.cpotzy.thedecider.data.db.entities.CompletionType
import com.cpotzy.thedecider.data.db.entities.TaskEntity
import com.cpotzy.thedecider.domain.model.Task

class TaskRepository(
    private val taskDao: TaskDao,
    private val completionDao: CompletionDao,
) {
    suspend fun insertAll(entities: List<TaskEntity>) = taskDao.insertAll(entities)
    suspend fun count(): Int = taskDao.count()

    suspend fun listActiveWithLastDone(): List<Task> {
        return taskDao.listActive().map { e ->
            val lastDone = completionDao.lastOfType(e.id, CompletionType.DONE)
            Task(
                id = e.id,
                title = e.title,
                cadence = e.cadence,
                energy = e.energy,
                duration = e.duration,
                timeWindow = e.timeWindow,
                isActive = e.isActive,
                createdAt = e.createdAt,
                lastDoneAt = lastDone,
            )
        }
    }

    suspend fun setActive(id: Long, isActive: Boolean) = taskDao.setActive(id, isActive)
}
```

- [ ] **Step 2: Write `CompletionRepository.kt`**

```kotlin
package com.cpotzy.thedecider.data.repo

import com.cpotzy.thedecider.data.db.dao.CompletionDao
import com.cpotzy.thedecider.data.db.dao.TaskDao
import com.cpotzy.thedecider.data.db.entities.CompletionEntity
import com.cpotzy.thedecider.data.db.entities.CompletionType
import com.cpotzy.thedecider.domain.model.Cadence
import com.cpotzy.thedecider.domain.time.Clock

class CompletionRepository(
    private val completionDao: CompletionDao,
    private val taskDao: TaskDao,
    private val clock: Clock,
) {
    suspend fun markDone(taskId: Long) {
        val now = clock.now()
        completionDao.insert(CompletionEntity(taskId = taskId, completedAt = now, type = CompletionType.DONE))
        val task = taskDao.byId(taskId) ?: return
        if (task.cadence == Cadence.ONEOFF) {
            taskDao.setActive(taskId, false)
        }
    }

    suspend fun markSkippedKeepPressure(taskId: Long) {
        completionDao.insert(CompletionEntity(
            taskId = taskId, completedAt = clock.now(), type = CompletionType.SKIPPED_PRESSURE_KEPT,
        ))
    }
}
```

- [ ] **Step 3: Write `SnoozeRepository.kt`**

```kotlin
package com.cpotzy.thedecider.data.repo

import com.cpotzy.thedecider.data.db.dao.SnoozeDao
import com.cpotzy.thedecider.data.db.entities.SnoozeEntity
import com.cpotzy.thedecider.data.db.entities.SnoozeKind
import com.cpotzy.thedecider.domain.time.Clock
import java.time.Duration as JDuration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

class SnoozeRepository(
    private val snoozeDao: SnoozeDao,
    private val clock: Clock,
) {
    suspend fun snoozeLaterToday(taskId: Long) {
        val now = clock.now()
        val until = now.plus(JDuration.ofHours(3))
        snoozeDao.insert(SnoozeEntity(
            taskId = taskId, until = until, kind = SnoozeKind.LATER_TODAY, createdAt = now,
        ))
    }

    suspend fun snoozeTomorrow(taskId: Long, zone: ZoneId = ZoneId.systemDefault()) {
        val now = clock.now()
        val tomorrowStart = LocalDate.now(zone).plusDays(1).atTime(LocalTime.of(5, 0)).atZone(zone).toInstant()
        snoozeDao.insert(SnoozeEntity(
            taskId = taskId, until = tomorrowStart, kind = SnoozeKind.TOMORROW, createdAt = now,
        ))
    }

    suspend fun activeTaskIds(now: Instant): Set<Long> = snoozeDao.activeTaskIds(now).toSet()
}
```

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/cpotzy/thedecider/data/repo
git commit -m "data: repositories for tasks, completions, snoozes"
```

---

### Task 2.8: Asset seeding

**Files:**
- Create: `app/src/main/assets/tasks-list.md` (copy of root `tasks-list.md`)
- Create: `app/src/main/java/com/cpotzy/thedecider/data/seed/TaskSeeder.kt`
- Create: `app/src/test/java/com/cpotzy/thedecider/data/seed/TaskSeederTest.kt`

- [ ] **Step 1: Copy the task list to assets**

Run: `mkdir -p app/src/main/assets && cp tasks-list.md app/src/main/assets/tasks-list.md`

- [ ] **Step 2: Write failing `TaskSeederTest.kt`**

```kotlin
package com.cpotzy.thedecider.data.seed

import com.cpotzy.thedecider.domain.model.Cadence
import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue

class TaskSeederTest {
    private val sampleMarkdown = """
        # The-Decider — Task List

        ## Daily
        - Vacuum downstairs and upstairs
        - Brush teeth night

        ## Weekly
        - Clean bathroom
        - Dust altar

        ## Anytime (as needed)
        - Empty trash bin
    """.trimIndent()

    @Test fun parsesCadenceSections() {
        val parsed = TaskSeeder.parseMarkdown(sampleMarkdown)
        val byCadence = parsed.groupBy { it.cadence }
        assertEquals(2, byCadence[Cadence.DAILY]?.size)
        assertEquals(2, byCadence[Cadence.WEEKLY]?.size)
        assertEquals(1, byCadence[Cadence.ANYTIME]?.size)
    }

    @Test fun extractsTitles() {
        val parsed = TaskSeeder.parseMarkdown(sampleMarkdown)
        val titles = parsed.map { it.title }
        assertTrue(titles.contains("Vacuum downstairs and upstairs"))
        assertTrue(titles.contains("Clean bathroom"))
        assertTrue(titles.contains("Empty trash bin"))
    }
}
```

- [ ] **Step 3: Run — should fail to compile**

Run: `./gradlew :app:testDebugUnitTest --tests "com.cpotzy.thedecider.data.seed.TaskSeederTest"`
Expected: compile error — `TaskSeeder` not defined.

- [ ] **Step 4: Write `TaskSeeder.kt`**

```kotlin
package com.cpotzy.thedecider.data.seed

import android.content.Context
import com.cpotzy.thedecider.data.db.entities.TaskEntity
import com.cpotzy.thedecider.data.repo.TaskRepository
import com.cpotzy.thedecider.domain.model.Cadence
import com.cpotzy.thedecider.domain.model.Duration
import com.cpotzy.thedecider.domain.model.Energy
import com.cpotzy.thedecider.domain.model.TimeWindow
import com.cpotzy.thedecider.domain.time.Clock
import java.io.BufferedReader

data class SeedTask(val title: String, val cadence: Cadence)

object TaskSeeder {
    private val sectionToCadence = mapOf(
        "Daily" to Cadence.DAILY,
        "Bi-daily" to Cadence.BIDAILY,
        "Weekly" to Cadence.WEEKLY,
        "Biweekly" to Cadence.BIWEEKLY,
        "Monthly" to Cadence.MONTHLY,
        "Bimonthly" to Cadence.BIMONTHLY,
        "Anytime" to Cadence.ANYTIME,
    )

    fun parseMarkdown(text: String): List<SeedTask> {
        val tasks = mutableListOf<SeedTask>()
        var current: Cadence? = null
        for (rawLine in text.lineSequence()) {
            val line = rawLine.trim()
            if (line.startsWith("## ")) {
                val header = line.removePrefix("## ").substringBefore(" (").trim()
                current = sectionToCadence[header]
            } else if (line.startsWith("- ") && current != null) {
                val title = line.removePrefix("- ").trim()
                if (title.isNotEmpty()) tasks.add(SeedTask(title, current))
            }
        }
        return tasks
    }

    suspend fun seedIfEmpty(context: Context, repo: TaskRepository, clock: Clock) {
        if (repo.count() > 0) return
        val text = context.assets.open("tasks-list.md").bufferedReader().use(BufferedReader::readText)
        val seedTasks = parseMarkdown(text)
        val now = clock.now()
        val entities = seedTasks.map { seed ->
            TaskEntity(
                title = seed.title,
                cadence = seed.cadence,
                energy = defaultEnergy(seed.title),
                duration = defaultDuration(seed.title),
                timeWindow = defaultTimeWindow(seed.title),
                createdAt = now,
            )
        }
        repo.insertAll(entities)
    }

    private fun defaultEnergy(title: String): Energy = when {
        title.contains("HIIT", ignoreCase = true) || title.contains("Weights", ignoreCase = true) -> Energy.HIGH
        title.contains("Mow", ignoreCase = true) || title.contains("Clean", ignoreCase = true) -> Energy.MEDIUM
        else -> Energy.LOW
    }

    private fun defaultDuration(title: String): Duration = when {
        title.contains("HIIT", ignoreCase = true) || title.contains("Clean bathroom", ignoreCase = true) -> Duration.MEDIUM
        title.contains("Brush", ignoreCase = true) || title.contains("Floss", ignoreCase = true) ||
            title.contains("Scrape", ignoreCase = true) || title.contains("skincare", ignoreCase = true) -> Duration.QUICK
        else -> Duration.SHORT
    }

    private fun defaultTimeWindow(title: String): TimeWindow = when {
        title.contains("morning", ignoreCase = true) || title.contains("Morning", ignoreCase = false) -> TimeWindow.MORNING
        title.contains("night", ignoreCase = true) -> TimeWindow.EVENING
        title.contains("HIIT", ignoreCase = true) -> TimeWindow.MORNING
        else -> TimeWindow.ANYTIME
    }
}
```

- [ ] **Step 5: Run tests — should pass**

Run: `./gradlew :app:testDebugUnitTest --tests "com.cpotzy.thedecider.data.seed.TaskSeederTest"`
Expected: 2 tests pass.

- [ ] **Step 6: Wire seeding into App**

Update `App.kt`:

```kotlin
package com.cpotzy.thedecider

import android.app.Application
import com.cpotzy.thedecider.data.db.AppDatabase
import com.cpotzy.thedecider.data.repo.CompletionRepository
import com.cpotzy.thedecider.data.repo.SnoozeRepository
import com.cpotzy.thedecider.data.repo.TaskRepository
import com.cpotzy.thedecider.data.seed.TaskSeeder
import com.cpotzy.thedecider.domain.time.Clock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class App : Application() {
    lateinit var graph: AppGraph
        private set

    override fun onCreate() {
        super.onCreate()
        graph = AppGraph(this)
        graph.scope.launch {
            TaskSeeder.seedIfEmpty(this@App, graph.taskRepository, graph.clock)
        }
    }
}

class AppGraph(app: Application) {
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    val clock: Clock = Clock.System
    private val db = AppDatabase.build(app)
    val taskRepository = TaskRepository(db.taskDao(), db.completionDao())
    val completionRepository = CompletionRepository(db.completionDao(), db.taskDao(), clock)
    val snoozeRepository = SnoozeRepository(db.snoozeDao(), clock)
    val snoozeDao = db.snoozeDao()
    val taskDao = db.taskDao()
    val completionDao = db.completionDao()
}
```

- [ ] **Step 7: Commit**

```bash
git add app/src/main/assets app/src/main/java/com/cpotzy/thedecider/data/seed app/src/test/java/com/cpotzy/thedecider/data/seed app/src/main/java/com/cpotzy/thedecider/App.kt
git commit -m "data: seed tasks from bundled markdown on first launch"
```

---

## Phase 3 — Selection Engine

### Task 3.1: PressureCalculator

**Files:**
- Create: `app/src/main/java/com/cpotzy/thedecider/domain/model/PressureTier.kt`
- Create: `app/src/main/java/com/cpotzy/thedecider/domain/select/PressureCalculator.kt`
- Create: `app/src/test/java/com/cpotzy/thedecider/domain/select/PressureCalculatorTest.kt`
- Create: `app/src/test/java/com/cpotzy/thedecider/domain/model/PressureTierTest.kt`

- [ ] **Step 1: Write failing `PressureCalculatorTest.kt`**

```kotlin
package com.cpotzy.thedecider.domain.select

import com.cpotzy.thedecider.domain.model.Cadence
import com.cpotzy.thedecider.domain.model.Duration
import com.cpotzy.thedecider.domain.model.Energy
import com.cpotzy.thedecider.domain.model.Task
import com.cpotzy.thedecider.domain.model.TimeWindow
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.temporal.ChronoUnit

class PressureCalculatorTest {
    private val now = Instant.parse("2026-05-21T12:00:00Z")
    private val calc = PressureCalculator()

    private fun task(cadence: Cadence, lastDoneAt: Instant?) = Task(
        id = 1, title = "X", cadence = cadence, energy = Energy.LOW, duration = Duration.QUICK,
        timeWindow = TimeWindow.ANYTIME, isActive = true,
        createdAt = Instant.parse("2026-01-01T00:00:00Z"), lastDoneAt = lastDoneAt,
    )

    @Test fun `daily done today has pressure 0`() {
        val t = task(Cadence.DAILY, now)
        assertEquals(0.0, calc.pressure(t, now), 0.001)
    }

    @Test fun `daily done 1 day ago has pressure 0`() {
        val t = task(Cadence.DAILY, now.minus(1, ChronoUnit.DAYS))
        assertEquals(0.0, calc.pressure(t, now), 0.001)
    }

    @Test fun `daily done 2 days ago has pressure 1`() {
        val t = task(Cadence.DAILY, now.minus(2, ChronoUnit.DAYS))
        assertEquals(1.0, calc.pressure(t, now), 0.001)
    }

    @Test fun `weekly done 14 days ago has pressure 1`() {
        val t = task(Cadence.WEEKLY, now.minus(14, ChronoUnit.DAYS))
        assertEquals(1.0, calc.pressure(t, now), 0.001)
    }

    @Test fun `anytime returns small constant`() {
        val t = task(Cadence.ANYTIME, null)
        assertEquals(0.05, calc.pressure(t, now), 0.001)
    }

    @Test fun `never done daily with createdAt 5 days ago has pressure 4`() {
        val t = Task(
            id = 1, title = "X", cadence = Cadence.DAILY, energy = Energy.LOW, duration = Duration.QUICK,
            timeWindow = TimeWindow.ANYTIME, isActive = true,
            createdAt = now.minus(5, ChronoUnit.DAYS), lastDoneAt = null,
        )
        assertEquals(4.0, calc.pressure(t, now), 0.001)
    }

    @Test fun `oneoff returns small constant`() {
        val t = task(Cadence.ONEOFF, null)
        assertEquals(0.05, calc.pressure(t, now), 0.001)
    }
}
```

- [ ] **Step 2: Run — should fail to compile**

Run: `./gradlew :app:testDebugUnitTest --tests "com.cpotzy.thedecider.domain.select.PressureCalculatorTest"`
Expected: compile error — `PressureCalculator` not defined.

- [ ] **Step 3: Write `PressureCalculator.kt`**

```kotlin
package com.cpotzy.thedecider.domain.select

import com.cpotzy.thedecider.domain.model.Cadence
import com.cpotzy.thedecider.domain.model.Task
import java.time.Duration
import java.time.Instant

class PressureCalculator {
    fun pressure(task: Task, now: Instant): Double {
        val cadenceDays = task.cadence.cadenceDays ?: return ANYTIME_PRESSURE
        val reference = task.lastDoneAt ?: task.createdAt
        val daysSince = Duration.between(reference, now).toHours().toDouble() / 24.0
        val raw = (daysSince - cadenceDays) / cadenceDays
        return if (raw < 0) 0.0 else raw
    }

    companion object {
        const val ANYTIME_PRESSURE = 0.05
    }
}
```

- [ ] **Step 4: Run — should pass**

Run: `./gradlew :app:testDebugUnitTest --tests "com.cpotzy.thedecider.domain.select.PressureCalculatorTest"`
Expected: 7 tests pass.

- [ ] **Step 5: Write `PressureTier.kt` and `PressureTierTest.kt`**

`PressureTier.kt`:

```kotlin
package com.cpotzy.thedecider.domain.model

enum class PressureTier { OVERDUE, IN_WINDOW, ANYTIME;

    companion object {
        fun forPressure(pressure: Double, cadence: Cadence): PressureTier = when {
            cadence == Cadence.ANYTIME || cadence == Cadence.ONEOFF -> ANYTIME
            pressure > 1.0 -> OVERDUE
            else -> IN_WINDOW
        }
    }
}
```

`PressureTierTest.kt`:

```kotlin
package com.cpotzy.thedecider.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class PressureTierTest {
    @Test fun `pressure 0 daily is IN_WINDOW`() =
        assertEquals(PressureTier.IN_WINDOW, PressureTier.forPressure(0.0, Cadence.DAILY))

    @Test fun `pressure 1 daily is IN_WINDOW`() =
        assertEquals(PressureTier.IN_WINDOW, PressureTier.forPressure(1.0, Cadence.DAILY))

    @Test fun `pressure 1_1 daily is OVERDUE`() =
        assertEquals(PressureTier.OVERDUE, PressureTier.forPressure(1.1, Cadence.DAILY))

    @Test fun `anytime is ANYTIME tier regardless of pressure`() =
        assertEquals(PressureTier.ANYTIME, PressureTier.forPressure(99.0, Cadence.ANYTIME))

    @Test fun `oneoff is ANYTIME tier regardless of pressure`() =
        assertEquals(PressureTier.ANYTIME, PressureTier.forPressure(0.0, Cadence.ONEOFF))
}
```

- [ ] **Step 6: Run all selection + tier tests**

Run: `./gradlew :app:testDebugUnitTest --tests "com.cpotzy.thedecider.domain.*"`
Expected: all pass.

- [ ] **Step 7: Commit**

```bash
git add app/src/main/java/com/cpotzy/thedecider/domain/model/PressureTier.kt app/src/main/java/com/cpotzy/thedecider/domain/select app/src/test/java/com/cpotzy/thedecider/domain/select app/src/test/java/com/cpotzy/thedecider/domain/model/PressureTierTest.kt
git commit -m "domain: PressureCalculator + PressureTier with tests"
```

---

### Task 3.2: ContextFilter and ModeChip

**Files:**
- Create: `app/src/main/java/com/cpotzy/thedecider/domain/select/ModeChip.kt`
- Create: `app/src/main/java/com/cpotzy/thedecider/domain/select/ContextFilter.kt`
- Create: `app/src/test/java/com/cpotzy/thedecider/domain/select/ContextFilterTest.kt`

- [ ] **Step 1: Write `ModeChip.kt`**

```kotlin
package com.cpotzy.thedecider.domain.select

import com.cpotzy.thedecider.domain.model.Duration
import com.cpotzy.thedecider.domain.model.Energy

data class ModeChip(
    val label: String,
    val energyFilter: Energy? = null,
    val maxDuration: Duration? = null,
) {
    companion object {
        val All = ModeChip(label = "All")
        val LowEnergy = ModeChip(label = "Low energy", energyFilter = Energy.LOW)
        val TenMin = ModeChip(label = "10 min", maxDuration = Duration.SHORT)
        val Quick = ModeChip(label = "Quick", maxDuration = Duration.QUICK)

        val defaults = listOf(All, LowEnergy, TenMin, Quick)
    }
}
```

- [ ] **Step 2: Write failing `ContextFilterTest.kt`**

```kotlin
package com.cpotzy.thedecider.domain.select

import com.cpotzy.thedecider.domain.model.Cadence
import com.cpotzy.thedecider.domain.model.Duration
import com.cpotzy.thedecider.domain.model.Energy
import com.cpotzy.thedecider.domain.model.Task
import com.cpotzy.thedecider.domain.model.TimeWindow
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalTime

class ContextFilterTest {
    private val filter = ContextFilter()

    private fun task(timeWindow: TimeWindow, energy: Energy = Energy.LOW, duration: Duration = Duration.QUICK) = Task(
        id = 1, title = "X", cadence = Cadence.DAILY, energy = energy, duration = duration,
        timeWindow = timeWindow, isActive = true,
        createdAt = java.time.Instant.parse("2026-01-01T00:00:00Z"), lastDoneAt = null,
    )

    @Test fun `morning window task passes when its morning`() {
        val passes = filter.matches(task(TimeWindow.MORNING), LocalTime.of(8, 0), ModeChip.All)
        assertTrue(passes)
    }

    @Test fun `morning window task fails when its afternoon`() {
        val passes = filter.matches(task(TimeWindow.MORNING), LocalTime.of(14, 0), ModeChip.All)
        assertFalse(passes)
    }

    @Test fun `anytime task passes anytime`() {
        val passes = filter.matches(task(TimeWindow.ANYTIME), LocalTime.of(23, 30), ModeChip.All)
        assertTrue(passes)
    }

    @Test fun `night only allows anytime tasks`() {
        val passes = filter.matches(task(TimeWindow.MORNING), LocalTime.of(3, 0), ModeChip.All)
        assertFalse(passes)
    }

    @Test fun `low energy chip rejects high energy task`() {
        val passes = filter.matches(
            task(TimeWindow.ANYTIME, energy = Energy.HIGH),
            LocalTime.of(10, 0),
            ModeChip.LowEnergy,
        )
        assertFalse(passes)
    }

    @Test fun `quick chip rejects medium duration task`() {
        val passes = filter.matches(
            task(TimeWindow.ANYTIME, duration = Duration.MEDIUM),
            LocalTime.of(10, 0),
            ModeChip.Quick,
        )
        assertFalse(passes)
    }

    @Test fun `ten min chip accepts short and quick`() {
        val short = filter.matches(
            task(TimeWindow.ANYTIME, duration = Duration.SHORT),
            LocalTime.of(10, 0),
            ModeChip.TenMin,
        )
        val quick = filter.matches(
            task(TimeWindow.ANYTIME, duration = Duration.QUICK),
            LocalTime.of(10, 0),
            ModeChip.TenMin,
        )
        assertTrue(short)
        assertTrue(quick)
    }
}
```

- [ ] **Step 3: Write `ContextFilter.kt`**

```kotlin
package com.cpotzy.thedecider.domain.select

import com.cpotzy.thedecider.domain.model.Task
import com.cpotzy.thedecider.domain.model.TimeWindow
import java.time.LocalTime

class ContextFilter {
    fun matches(task: Task, currentTime: LocalTime, mode: ModeChip): Boolean {
        if (!matchesTimeWindow(task, currentTime)) return false
        if (mode.energyFilter != null && task.energy != mode.energyFilter) return false
        if (mode.maxDuration != null && task.duration.maxMinutes > mode.maxDuration.maxMinutes) return false
        return true
    }

    private fun matchesTimeWindow(task: Task, currentTime: LocalTime): Boolean {
        if (task.timeWindow == TimeWindow.ANYTIME) return true
        val window = TimeWindow.atLocalTime(currentTime)
        if (window == TimeWindow.NIGHT) return false  // night only allows ANYTIME
        return window == task.timeWindow
    }
}
```

- [ ] **Step 4: Run — should pass**

Run: `./gradlew :app:testDebugUnitTest --tests "com.cpotzy.thedecider.domain.select.ContextFilterTest"`
Expected: 7 tests pass.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/cpotzy/thedecider/domain/select/ModeChip.kt app/src/main/java/com/cpotzy/thedecider/domain/select/ContextFilter.kt app/src/test/java/com/cpotzy/thedecider/domain/select/ContextFilterTest.kt
git commit -m "domain: ContextFilter + ModeChip with tests"
```

---

### Task 3.3: SelectionService

**Files:**
- Create: `app/src/main/java/com/cpotzy/thedecider/domain/select/SelectionService.kt`
- Create: `app/src/test/java/com/cpotzy/thedecider/domain/select/SelectionServiceTest.kt`

- [ ] **Step 1: Write failing `SelectionServiceTest.kt`**

```kotlin
package com.cpotzy.thedecider.domain.select

import com.cpotzy.thedecider.domain.model.Cadence
import com.cpotzy.thedecider.domain.model.Duration
import com.cpotzy.thedecider.domain.model.Energy
import com.cpotzy.thedecider.domain.model.Task
import com.cpotzy.thedecider.domain.model.TimeWindow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import kotlin.random.Random

class SelectionServiceTest {
    private val now = Instant.parse("2026-05-21T10:00:00Z")  // 10am UTC, treat as morning
    private val zone = ZoneId.of("UTC")
    private val service = SelectionService(PressureCalculator(), ContextFilter())

    private fun task(
        id: Long, cadence: Cadence, lastDoneAt: Instant?,
        timeWindow: TimeWindow = TimeWindow.ANYTIME,
        energy: Energy = Energy.LOW,
        duration: Duration = Duration.QUICK,
    ) = Task(
        id = id, title = "T$id", cadence = cadence, energy = energy, duration = duration,
        timeWindow = timeWindow, isActive = true,
        createdAt = Instant.parse("2026-01-01T00:00:00Z"), lastDoneAt = lastDoneAt,
    )

    @Test fun `returns null when no candidates`() {
        val picked = service.pickNext(
            candidates = emptyList(),
            snoozedIds = emptySet(),
            now = now,
            zone = zone,
            mode = ModeChip.All,
            random = Random(0),
        )
        assertNull(picked)
    }

    @Test fun `excludes snoozed tasks`() {
        val candidates = listOf(task(1, Cadence.DAILY, now.minus(3, ChronoUnit.DAYS)))
        val picked = service.pickNext(candidates, setOf(1L), now, zone, ModeChip.All, Random(0))
        assertNull(picked)
    }

    @Test fun `prefers overdue tier over in_window`() {
        val candidates = listOf(
            task(1, Cadence.DAILY, now.minus(1, ChronoUnit.HOURS)),       // in_window pressure ~0
            task(2, Cadence.DAILY, now.minus(5, ChronoUnit.DAYS)),        // overdue pressure 4
        )
        val picked = service.pickNext(candidates, emptySet(), now, zone, ModeChip.All, Random(0))
        assertEquals(2L, picked?.id)
    }

    @Test fun `falls back to anytime tier when nothing else`() {
        val candidates = listOf(task(1, Cadence.ANYTIME, null))
        val picked = service.pickNext(candidates, emptySet(), now, zone, ModeChip.All, Random(0))
        assertNotNull(picked)
        assertEquals(1L, picked?.id)
    }

    @Test fun `low energy chip narrows candidates`() {
        val candidates = listOf(
            task(1, Cadence.DAILY, now.minus(5, ChronoUnit.DAYS), energy = Energy.HIGH),
            task(2, Cadence.DAILY, now.minus(5, ChronoUnit.DAYS), energy = Energy.LOW),
        )
        val picked = service.pickNext(candidates, emptySet(), now, zone, ModeChip.LowEnergy, Random(0))
        assertEquals(2L, picked?.id)
    }

    @Test fun `weighted random within tier is deterministic given seed`() {
        val candidates = listOf(
            task(1, Cadence.DAILY, now.minus(5, ChronoUnit.DAYS)),
            task(2, Cadence.DAILY, now.minus(5, ChronoUnit.DAYS)),
        )
        val first = service.pickNext(candidates, emptySet(), now, zone, ModeChip.All, Random(42))
        val second = service.pickNext(candidates, emptySet(), now, zone, ModeChip.All, Random(42))
        assertEquals(first?.id, second?.id)
    }

    @Test fun `daily done today is excluded from candidates by caller`() {
        // SelectionService doesn't compute "done today" — that's the caller's job.
        // This test documents the contract: caller passes only tasks whose cadence has rolled.
        val candidates = listOf(task(1, Cadence.DAILY, now))  // done now, pressure 0
        val picked = service.pickNext(candidates, emptySet(), now, zone, ModeChip.All, Random(0))
        // Pressure 0 → in_window tier → still pickable
        assertEquals(1L, picked?.id)
    }
}
```

- [ ] **Step 2: Run — should fail to compile**

Run: `./gradlew :app:testDebugUnitTest --tests "com.cpotzy.thedecider.domain.select.SelectionServiceTest"`
Expected: compile error — `SelectionService` not defined.

- [ ] **Step 3: Write `SelectionService.kt`**

```kotlin
package com.cpotzy.thedecider.domain.select

import com.cpotzy.thedecider.domain.model.PressureTier
import com.cpotzy.thedecider.domain.model.Task
import java.time.Instant
import java.time.ZoneId
import kotlin.random.Random

class SelectionService(
    private val pressureCalc: PressureCalculator,
    private val contextFilter: ContextFilter,
) {
    data class Scored(val task: Task, val pressure: Double, val tier: PressureTier)

    fun pickNext(
        candidates: List<Task>,
        snoozedIds: Set<Long>,
        now: Instant,
        zone: ZoneId,
        mode: ModeChip,
        random: Random = Random.Default,
    ): Task? {
        val currentLocalTime = now.atZone(zone).toLocalTime()
        val scored = candidates
            .asSequence()
            .filter { it.id !in snoozedIds }
            .filter { contextFilter.matches(it, currentLocalTime, mode) }
            .map { task ->
                val p = pressureCalc.pressure(task, now)
                Scored(task, p, PressureTier.forPressure(p, task.cadence))
            }
            .toList()
        if (scored.isEmpty()) return null
        val tierOrder = listOf(PressureTier.OVERDUE, PressureTier.IN_WINDOW, PressureTier.ANYTIME)
        for (tier in tierOrder) {
            val bucket = scored.filter { it.tier == tier }
            if (bucket.isNotEmpty()) {
                return weightedPick(bucket, random)
            }
        }
        return null
    }

    private fun weightedPick(bucket: List<Scored>, random: Random): Task {
        val weights = bucket.map { it.pressure + 1.0 }
        val totalWeight = weights.sum()
        val roll = random.nextDouble() * totalWeight
        var acc = 0.0
        weights.forEachIndexed { i, w ->
            acc += w
            if (roll <= acc) return bucket[i].task
        }
        return bucket.last().task
    }
}
```

- [ ] **Step 4: Run all selection tests — should pass**

Run: `./gradlew :app:testDebugUnitTest --tests "com.cpotzy.thedecider.domain.*"`
Expected: all pass.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/cpotzy/thedecider/domain/select/SelectionService.kt app/src/test/java/com/cpotzy/thedecider/domain/select/SelectionServiceTest.kt
git commit -m "domain: SelectionService with tier-prioritized weighted random pick"
```

---

### Task 3.4: Cadence-rolled filter (in repository)

**Files:**
- Modify: `app/src/main/java/com/cpotzy/thedecider/data/repo/TaskRepository.kt`

The selection service requires the caller to pass only tasks whose cadence has rolled. Repository owns that filter.

- [ ] **Step 1: Add `listEligibleForSelection` to `TaskRepository`**

```kotlin
import com.cpotzy.thedecider.domain.model.Cadence
import com.cpotzy.thedecider.domain.time.Clock
import java.time.Instant
import java.time.temporal.ChronoUnit

class TaskRepository(
    private val taskDao: TaskDao,
    private val completionDao: CompletionDao,
    private val clock: Clock,
) {
    // ... existing methods

    suspend fun listEligibleForSelection(now: Instant = clock.now()): List<Task> {
        return listActiveWithLastDone().filter { task ->
            val cadenceDays = task.cadence.cadenceDays
            if (cadenceDays == null) return@filter true  // anytime/oneoff always eligible
            val ref = task.lastDoneAt ?: task.createdAt
            ChronoUnit.HOURS.between(ref, now) >= cadenceDays * 24
        }
    }
}
```

Also update the constructor call in `AppGraph` to pass `clock`:

```kotlin
val taskRepository = TaskRepository(db.taskDao(), db.completionDao(), clock)
```

- [ ] **Step 2: Verify compilation**

Run: `./gradlew :app:assembleDebug`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/cpotzy/thedecider/data/repo/TaskRepository.kt app/src/main/java/com/cpotzy/thedecider/App.kt
git commit -m "data: TaskRepository.listEligibleForSelection filters by cadence-rolled"
```

---

## Phase 4 — Queue Screen MVP

### Task 4.1: QueueViewModel

**Files:**
- Create: `app/src/main/java/com/cpotzy/thedecider/ui/queue/QueueViewModel.kt`

- [ ] **Step 1: Write `QueueViewModel.kt`**

```kotlin
package com.cpotzy.thedecider.ui.queue

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cpotzy.thedecider.data.repo.CompletionRepository
import com.cpotzy.thedecider.data.repo.SnoozeRepository
import com.cpotzy.thedecider.data.repo.TaskRepository
import com.cpotzy.thedecider.domain.model.PressureTier
import com.cpotzy.thedecider.domain.model.Task
import com.cpotzy.thedecider.domain.select.ModeChip
import com.cpotzy.thedecider.domain.select.PressureCalculator
import com.cpotzy.thedecider.domain.select.SelectionService
import com.cpotzy.thedecider.domain.time.Clock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.ZoneId

data class QueueUiState(
    val task: Task? = null,
    val pressure: Double = 0.0,
    val tier: PressureTier = PressureTier.IN_WINDOW,
    val mode: ModeChip = ModeChip.All,
    val modeChips: List<ModeChip> = ModeChip.defaults,
    val emptyState: Boolean = false,
)

class QueueViewModel(
    private val taskRepository: TaskRepository,
    private val completionRepository: CompletionRepository,
    private val snoozeRepository: SnoozeRepository,
    private val selectionService: SelectionService,
    private val pressureCalc: PressureCalculator,
    private val clock: Clock,
    private val zone: ZoneId = ZoneId.systemDefault(),
) : ViewModel() {

    private val _state = MutableStateFlow(QueueUiState())
    val state: StateFlow<QueueUiState> = _state.asStateFlow()

    init { refresh() }

    fun setMode(mode: ModeChip) {
        _state.value = _state.value.copy(mode = mode)
        refresh()
    }

    fun acceptCurrent() {
        val current = _state.value.task ?: return
        viewModelScope.launch {
            completionRepository.markDone(current.id)
            refresh()
        }
    }

    fun snoozeCurrent(kind: SnoozeKindChoice) {
        val current = _state.value.task ?: return
        viewModelScope.launch {
            when (kind) {
                SnoozeKindChoice.LATER_TODAY -> snoozeRepository.snoozeLaterToday(current.id)
                SnoozeKindChoice.TOMORROW -> snoozeRepository.snoozeTomorrow(current.id, zone)
                SnoozeKindChoice.SKIP_CYCLE -> completionRepository.markSkippedKeepPressure(current.id)
            }
            refresh()
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            val now = clock.now()
            val candidates = taskRepository.listEligibleForSelection(now)
            val snoozed = snoozeRepository.activeTaskIds(now)
            val picked = selectionService.pickNext(
                candidates = candidates,
                snoozedIds = snoozed,
                now = now,
                zone = zone,
                mode = _state.value.mode,
            )
            if (picked == null) {
                _state.value = _state.value.copy(task = null, emptyState = true)
            } else {
                val pressure = pressureCalc.pressure(picked, now)
                val tier = PressureTier.forPressure(pressure, picked.cadence)
                _state.value = _state.value.copy(task = picked, pressure = pressure, tier = tier, emptyState = false)
            }
        }
    }
}

enum class SnoozeKindChoice { LATER_TODAY, TOMORROW, SKIP_CYCLE }
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/cpotzy/thedecider/ui/queue/QueueViewModel.kt
git commit -m "ui: QueueViewModel with refresh, accept, snooze, mode change"
```

---

### Task 4.2: TaskCard composable

**Files:**
- Create: `app/src/main/java/com/cpotzy/thedecider/ui/queue/components/TaskCard.kt`

- [ ] **Step 1: Write `TaskCard.kt`**

```kotlin
package com.cpotzy.thedecider.ui.queue.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.cpotzy.thedecider.domain.model.PressureTier
import com.cpotzy.thedecider.domain.model.Task
import com.cpotzy.thedecider.ui.theme.Neutral
import com.cpotzy.thedecider.ui.theme.PressureAmber
import com.cpotzy.thedecider.ui.theme.PressureRed
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit

@Composable
fun TaskCard(
    task: Task,
    tier: PressureTier,
    now: Instant,
    modifier: Modifier = Modifier,
) {
    val edgeColor = when (tier) {
        PressureTier.OVERDUE -> PressureRed
        PressureTier.IN_WINDOW, PressureTier.ANYTIME -> Neutral
    }
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .clip(RoundedCornerShape(20.dp)),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp,
    ) {
        Column(modifier = Modifier.padding(0.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .background(edgeColor),
            )
            Column(modifier = Modifier.padding(24.dp)) {
                Text(task.title, style = MaterialTheme.typography.headlineLarge)
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AssistChip(onClick = {}, label = { Text(task.duration.name.lowercase()) },
                        colors = AssistChipDefaults.assistChipColors(containerColor = Neutral.copy(alpha = 0.4f)))
                    AssistChip(onClick = {}, label = { Text("${task.energy.name.lowercase()} energy") },
                        colors = AssistChipDefaults.assistChipColors(containerColor = Neutral.copy(alpha = 0.4f)))
                }
                val lastDone = task.lastDoneAt
                if (lastDone != null) {
                    Spacer(Modifier.height(20.dp))
                    val daysAgo = ChronoUnit.DAYS.between(lastDone, now).coerceAtLeast(0)
                    Text(
                        "last done $daysAgo day${if (daysAgo == 1L) "" else "s"} ago",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    )
                }
            }
        }
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/cpotzy/thedecider/ui/queue/components/TaskCard.kt
git commit -m "ui: TaskCard composable with pressure tint + last done chip"
```

---

### Task 4.3: ModeChipRow composable

**Files:**
- Create: `app/src/main/java/com/cpotzy/thedecider/ui/queue/components/ModeChipRow.kt`

- [ ] **Step 1: Write `ModeChipRow.kt`**

```kotlin
package com.cpotzy.thedecider.ui.queue.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cpotzy.thedecider.domain.select.ModeChip

@Composable
fun ModeChipRow(
    chips: List<ModeChip>,
    selected: ModeChip,
    onSelect: (ModeChip) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        chips.forEach { chip ->
            FilterChip(
                selected = chip == selected,
                onClick = { onSelect(chip) },
                label = { Text(chip.label) },
            )
        }
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/cpotzy/thedecider/ui/queue/components/ModeChipRow.kt
git commit -m "ui: ModeChipRow filter chips"
```

---

### Task 4.4: SwipeChooserSheet composable

**Files:**
- Create: `app/src/main/java/com/cpotzy/thedecider/ui/queue/components/SwipeChooserSheet.kt`

- [ ] **Step 1: Write `SwipeChooserSheet.kt`**

```kotlin
package com.cpotzy.thedecider.ui.queue.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cpotzy.thedecider.ui.queue.SnoozeKindChoice

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeChooserSheet(
    onChoose: (SnoozeKindChoice) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Defer this one?", style = MaterialTheme.typography.titleLarge)
            ChooserButton("Later today", subtitle = "Comes back in a few hours") {
                onChoose(SnoozeKindChoice.LATER_TODAY)
            }
            ChooserButton("Tomorrow", subtitle = "Comes back tomorrow morning") {
                onChoose(SnoozeKindChoice.TOMORROW)
            }
            ChooserButton(
                "Skip this cycle",
                subtitle = "Logs as skipped — pressure keeps building",
            ) {
                onChoose(SnoozeKindChoice.SKIP_CYCLE)
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ChooserButton(title: String, subtitle: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 2.dp,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            Text(subtitle, style = MaterialTheme.typography.labelSmall)
        }
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/cpotzy/thedecider/ui/queue/components/SwipeChooserSheet.kt
git commit -m "ui: SwipeChooserSheet bottom sheet for snooze choices"
```

---

### Task 4.5: QueueScreen with swipe gestures

**Files:**
- Create: `app/src/main/java/com/cpotzy/thedecider/ui/queue/QueueScreen.kt`

- [ ] **Step 1: Write `QueueScreen.kt`**

```kotlin
package com.cpotzy.thedecider.ui.queue

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cpotzy.thedecider.ui.queue.components.ModeChipRow
import com.cpotzy.thedecider.ui.queue.components.SwipeChooserSheet
import com.cpotzy.thedecider.ui.queue.components.TaskCard
import java.time.Instant
import kotlin.math.abs

@Composable
fun QueueScreen(
    viewModel: QueueViewModel,
    now: Instant = Instant.now(),
) {
    val state by viewModel.state.collectAsState()
    var offsetX by remember { mutableStateOf(0f) }
    var showChooser by remember { mutableStateOf(false) }
    val density = LocalDensity.current
    val swipeThresholdPx = with(density) { 120.dp.toPx() }
    val animatedOffset by animateFloatAsState(targetValue = offsetX, label = "swipeOffset")

    Column(modifier = Modifier.fillMaxSize().padding(top = 32.dp)) {
        ModeChipRow(
            chips = state.modeChips,
            selected = state.mode,
            onSelect = { viewModel.setMode(it) },
        )
        Spacer(Modifier.height(24.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            val task = state.task
            if (task != null) {
                Box(
                    modifier = Modifier
                        .graphicsLayer(translationX = animatedOffset, rotationZ = animatedOffset / 60f)
                        .pointerInput(task.id) {
                            detectHorizontalDragGestures(
                                onDragEnd = {
                                    when {
                                        offsetX > swipeThresholdPx -> {
                                            viewModel.acceptCurrent()
                                            offsetX = 0f
                                        }
                                        offsetX < -swipeThresholdPx -> {
                                            showChooser = true
                                            offsetX = 0f
                                        }
                                        else -> offsetX = 0f
                                    }
                                },
                                onDragCancel = { offsetX = 0f },
                            ) { _, dragAmount -> offsetX += dragAmount }
                        },
                ) {
                    TaskCard(task = task, tier = state.tier, now = now)
                }
            } else if (state.emptyState) {
                Text(
                    "Nothing to decide right now.",
                    style = MaterialTheme.typography.titleLarge,
                )
            }
        }
        Spacer(Modifier.height(24.dp))
        SwipeHint()
        Spacer(Modifier.height(24.dp))
    }
    if (showChooser) {
        SwipeChooserSheet(
            onChoose = { kind ->
                viewModel.snoozeCurrent(kind)
                showChooser = false
            },
            onDismiss = { showChooser = false },
        )
    }
}

@Composable
private fun SwipeHint() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text("← later", style = MaterialTheme.typography.labelSmall)
        Text("done →", style = MaterialTheme.typography.labelSmall)
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/cpotzy/thedecider/ui/queue/QueueScreen.kt
git commit -m "ui: QueueScreen with horizontal swipe gestures"
```

---

### Task 4.6: Wire QueueScreen into MainActivity

**Files:**
- Modify: `app/src/main/java/com/cpotzy/thedecider/App.kt`
- Modify: `app/src/main/java/com/cpotzy/thedecider/MainActivity.kt`

- [ ] **Step 1: Add selection service + viewmodel factory to `AppGraph`**

```kotlin
class AppGraph(app: Application) {
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    val clock: Clock = Clock.System
    private val db = AppDatabase.build(app)
    val taskRepository = TaskRepository(db.taskDao(), db.completionDao(), clock)
    val completionRepository = CompletionRepository(db.completionDao(), db.taskDao(), clock)
    val snoozeRepository = SnoozeRepository(db.snoozeDao(), clock)

    val pressureCalculator = PressureCalculator()
    val contextFilter = ContextFilter()
    val selectionService = SelectionService(pressureCalculator, contextFilter)
}
```

Add imports for `PressureCalculator`, `ContextFilter`, `SelectionService`.

- [ ] **Step 2: Write a tiny ViewModelFactory and hook into MainActivity**

Update `MainActivity.kt`:

```kotlin
package com.cpotzy.thedecider

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cpotzy.thedecider.ui.queue.QueueScreen
import com.cpotzy.thedecider.ui.queue.QueueViewModel
import com.cpotzy.thedecider.ui.theme.TheDeciderTheme

class MainActivity : ComponentActivity() {
    private val viewModel: QueueViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val graph = (application as App).graph
                @Suppress("UNCHECKED_CAST")
                return QueueViewModel(
                    taskRepository = graph.taskRepository,
                    completionRepository = graph.completionRepository,
                    snoozeRepository = graph.snoozeRepository,
                    selectionService = graph.selectionService,
                    pressureCalc = graph.pressureCalculator,
                    clock = graph.clock,
                ) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TheDeciderTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    QueueScreen(viewModel = viewModel)
                }
            }
        }
    }
}
```

- [ ] **Step 3: Build the app**

Run: `./gradlew :app:assembleDebug`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/cpotzy/thedecider/App.kt app/src/main/java/com/cpotzy/thedecider/MainActivity.kt
git commit -m "ui: wire QueueScreen + ViewModel into MainActivity via AppGraph"
```

---

### Task 4.7: Manual smoke test on device

- [ ] **Step 1: Push to main and trigger CI build**

Run: `git push`

- [ ] **Step 2: Wait for the GitHub Actions run to finish and download the APK**

Run: `gh run watch && gh release view latest`

- [ ] **Step 3: Install on your Android phone and verify**

Open the release URL on your phone, install the APK. Verify:
- App launches without crashing
- On first launch, seed runs and a task card appears
- Swipe right → task disappears, next task appears, app records a `done` completion
- Swipe left → bottom sheet appears with 3 choices
- Each choice dismisses the sheet and triggers the right behavior:
  - "Later today" → task disappears, returns ~3 hours later (test by changing system time if you want)
  - "Tomorrow" → task disappears, returns next day
  - "Skip this cycle" → task disappears but pressure should keep building (verify in next cycle)
- Mode chips at top change which task appears
- An overdue task (set a daily task's last_done_at to 5 days ago via a manual completion + time skip if needed) shows the red edge tint

If anything fails, file an issue or fix and commit.

---

## Phase 5 — Polish

### Task 5.1: Empty state polish

**Files:**
- Modify: `app/src/main/java/com/cpotzy/thedecider/ui/queue/QueueScreen.kt`

- [ ] **Step 1: Improve the empty state UI**

Replace the empty-state branch in `QueueScreen.kt`:

```kotlin
} else if (state.emptyState) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(32.dp),
    ) {
        Text(
            "Nothing to decide right now.",
            style = MaterialTheme.typography.headlineLarge,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "Come back later, or try a different filter above.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
        )
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/cpotzy/thedecider/ui/queue/QueueScreen.kt
git commit -m "ui: polish empty state copy"
```

---

### Task 5.2: README

**Files:**
- Create: `README.md`

- [ ] **Step 1: Write the README**

```markdown
# The-Decider

ADHD-friendly Android app that surfaces one household or sadhana task at a time. Swipe right to do it, swipe left to defer. Backend schedules silently; pressure builds on overdue tasks.

## Install

Latest debug APK: https://github.com/CPotzy/The-Decider/releases/latest

On Android: enable "Install unknown apps" for your browser, download the APK, tap to install.

## Build locally

Requires JDK 17, Android SDK 34.

```
./gradlew :app:assembleDebug
```

## Tests

Unit tests (selection engine, parsing, domain logic):

```
./gradlew :app:testDebugUnitTest
```

Instrumented tests (DAOs — requires connected device or emulator):

```
./gradlew :app:connectedAndroidTest
```

## Docs

- Design spec: `docs/superpowers/specs/2026-05-21-the-decider-design.md`
- MVP implementation plan: `docs/superpowers/plans/2026-05-21-the-decider-mvp.md`
```

- [ ] **Step 2: Commit**

```bash
git add README.md
git commit -m "docs: README with install + build instructions"
```

---

### Task 5.3: Full test pass

- [ ] **Step 1: Run all unit tests**

Run: `./gradlew :app:testDebugUnitTest`
Expected: all tests pass.

- [ ] **Step 2: Run instrumented tests (if device/emulator available)**

Run: `./gradlew :app:connectedAndroidTest`
Expected: all tests pass.

- [ ] **Step 3: Final assemble**

Run: `./gradlew :app:assembleDebug`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Push final commits**

Run: `git push`

CI will produce the final MVP APK in the `latest` release. Install it. The MVP is complete.

---

## What This Plan Does NOT Include (future plans)

- **Plan 2 (Task Detail + LLM):** Task detail screen with checklist + focus mode; per-step "break this smaller" using Claude Haiku; saving regenerated sub-steps back to the DB.
- **Plan 3 (Task Management + History):** Quick-add sheet; Task management screen (full list, edit, deactivate); History screen (reverse-chrono log).
- **Plan 4 (Notifications + Settings):** NudgeWorker (smart 3x/day nudges within a configurable window); Settings screen (nudge window, max nudges, mode chip presets, encrypted API key storage); RolloverWorker (midnight cleanup).

Each future plan builds on the MVP APK and remains independently shippable.

---

## Architecture Notes for the Implementer

- **Why manual DI instead of Hilt?** Single app, single activity, simple object graph — Hilt's compile-time codegen + extra Gradle plugin is overhead this project doesn't need. The `AppGraph` class in `App.kt` is the entire DI surface. If the project grows we can swap to Hilt without changing call sites.
- **Why a domain `Task` separate from `TaskEntity`?** The entity is a storage shape (one row, no derived fields). The domain `Task` includes `lastDoneAt` which is computed from the `Completion` table. Keeping them separate means selection logic doesn't depend on Room.
- **Why `Clock` as an interface?** Lets unit tests pin `now` to a known instant without messing with system time.
- **Why no test for `QueueViewModel` in this plan?** ViewModels are integration glue — they're tested by the underlying domain tests (which we have) plus manual verification (Task 4.7). When this grows complex enough, add a `QueueViewModelTest` using `kotlinx-coroutines-test` and fake repositories. Not worth it yet.
- **Why Room instrumented tests instead of Robolectric?** Room queries depend on real SQLite — Robolectric's SQLite emulation has historically had inconsistencies. Instrumented tests are slower but accurate.
