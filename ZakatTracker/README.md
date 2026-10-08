# Zakat Tracker (Android)

A private, offline-first Android app for tracking your Zakat obligations: define a Zakat year,
set the total payable amount, record payments against categories over time, and see dashboards
and reports of what's paid and what's still due. All data lives in a local SQLite (Room)
database on your phone - nothing is sent anywhere unless you explicitly export and share it.

## Features

- **Zakat years** - define a start/end date range and the total Zakat payable for that period.
  Create as many years as you like; the dashboard always shows the current one plus a
  comparison against the previous year.
- **Categories** - a starter set (Poor & Needy, Relatives, Orphans, Education, Medical Aid,
  Mosque/Madrasa, Other) that you can rename, add to, or hide at any time. A category already
  used in a payment is hidden rather than deleted, so old reports keep their labels.
- **Payments** - add, edit, or delete any payment, with an amount, date, category, optional
  recipient name and note. Search payments within a year.
- **Dashboard** - current year payable/paid/due, a progress bar, last year's summary, a
  category breakdown chart, and your 5 most recent payments.
- **Reports** - a yearly payable/paid/due table, a category breakdown for any year, and a
  custom date-range report you can export as a CSV file and share.
- **6-digit PIN lock** - the app opens to a PIN screen. The default PIN is **009900**; you are
  required to set your own PIN the first time you successfully unlock with it. PIN hashes are
  stored with `EncryptedSharedPreferences` (Android Keystore-backed), never in plain text.
  Fingerprint unlock can be turned on afterwards in Settings, on devices that support it.
- **Backup & restore** - export the entire SQLite database as a `.db` file through the normal
  Android share sheet (Gmail, Google Drive, Files, etc. all work as share targets), and import
  a previously exported file back in (this fully replaces the data currently in the app, with a
  confirmation step first).
- **Deep green & gold** Material 3 theme, with a matching dark mode.

## Important: this project could not be compiled inside the assistant's sandbox

The cloud workspace this app was built in has no network access to Google's Android SDK
servers or to Maven Central/Google's Maven repository (both are blocked by this session's
network policy), so an actual `.apk` file could not be assembled here - **there was no way to
produce a working installable file directly in that chat**. What you have instead is the
complete, ready-to-build Android Studio project (full Kotlin source, Gradle config,
resources). Every file was written carefully and cross-checked by hand (import-by-import) for
correctness, but it has not gone through a real compiler, so treat the two build options below
as the way to get (and verify) your first working APK.

You have two ways to turn this into an installed app, and you don't need to pick a side -
option A needs nothing installed on your computer at all.

### Option A - GitHub Actions (no Android Studio needed)

1. Create a new repository on [github.com](https://github.com) (public or private, your choice).
2. Upload this entire folder's contents to that repository (via the GitHub web uploader, or
   `git init && git add . && git commit -m "Initial commit" && git remote add origin <your-repo-url> && git push -u origin main`
   from a terminal that has `git` installed).
3. On GitHub, open the **Actions** tab of your new repository. A workflow called
   **Build APK** runs automatically on every push (it's already included in
   `.github/workflows/build-apk.yml`), or you can click **Run workflow** to trigger it by hand.
4. When the run finishes (a few minutes), open it and scroll to **Artifacts** -
   download **zakat-tracker-apk**, which contains `app-debug.apk`.
5. Copy `app-debug.apk` to your Android phone and tap it to install (you'll need to allow
   "Install unknown apps" for whichever app you used to open the file - Android will prompt you
   for this the first time).

This APK is debug-signed, which is exactly what you want for installing on your own phone.

### Option B - Android Studio on your computer

1. Install [Android Studio](https://developer.android.com/studio) (free). The first launch
   downloads the Android SDK automatically.
2. Choose **Open** and select this project's folder (the one with `settings.gradle.kts` in it).
3. Let Gradle sync finish (Android Studio will download the exact Gradle/AGP/library versions
   this project pins - that needs an internet connection the first time).
4. Plug in your Android phone (with USB debugging enabled) or start an emulator, then click the
   green **Run ▶** button. Android Studio builds and installs the app for you.
5. To get a standalone `.apk` file instead: **Build ▸ Build Bundle(s) / APK(s) ▸ Build APK(s)**,
   then **locate** it from the notification that appears
   (`app/build/outputs/apk/debug/app-debug.apk`).

If Android Studio offers to upgrade the Android Gradle Plugin/Gradle version when you open the
project, accepting that upgrade is fine - the project's a mainstream, recent combination and
should update cleanly.

## Project layout

```
app/src/main/java/com/zakattracker/app/
  data/            Room entities, DAOs, the database, the repository, backup/export logic
  security/        PIN hashing and storage (PinManager)
  di/              A tiny hand-rolled dependency container (AppContainer) - no DI framework
  ui/              Every screen, its ViewModel, shared components, navigation, and theme
```

## Changing the default PIN before you ship this to someone else

If you're setting this phone up for someone else, it's still safest to leave the default PIN
(009900) in place and let *them* change it on first login - that's exactly the flow built in.
If you'd rather change the compiled-in default yourself, edit `DEFAULT_PIN` in
`app/src/main/java/com/zakattracker/app/security/PinManager.kt`.

## Publishing to the Play Store (optional, later)

The debug APK above is perfect for personal use or sharing within your organisation directly.
If you ever want to publish this on the Play Store, you'll need a release signing key
(`keytool -genkey -v -keystore release.keystore -alias zakattracker -keyalg RSA -keysize 2048 -validity 10000`)
and a `signingConfig` added to `app/build.gradle.kts`'s `release` build type - Android Studio's
**Build ▸ Generate Signed Bundle/APK** wizard will create both for you interactively.
