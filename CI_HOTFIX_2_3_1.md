# LivasODV Android 2.3.1 - CI Hotfix

Correzione GitHub Actions:
- Gradle 8.13 forzato esplicitamente.
- Controllo bloccante della versione Gradle prima della compilazione.
- Android SDK 36.
- Test, lint e assembleDebug.
- Artifact APK `LivasODV-Android-2.3.1-RC-debug`.

Se nei log non compare `=== LivasODV Android 2.3.1 CI HOTFIX ===`, GitHub sta eseguendo un vecchio workflow presente nel repository.
