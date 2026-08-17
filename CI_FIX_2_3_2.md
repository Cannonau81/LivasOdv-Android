# LivasODV Android 2.3.2 - CI Wrapper Fix

- Rimosso `gradle/actions/setup-gradle@v4`, che falliva su GitHub con HTTP 429/503.
- La CI usa esclusivamente il Gradle Wrapper del repository (`./gradlew`).
- Wrapper verificato su Gradle 8.13.
- Pipeline: Java 17, SDK 36, test, lint, assembleDebug e artifact APK.
- Versione app: 2.3.2, versionCode 232.
