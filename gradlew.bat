@echo off
where gradle >nul 2>nul
if %ERRORLEVEL% EQU 0 (
  gradle %*
  exit /b %ERRORLEVEL%
)
echo Gradle non trovato. Su GitHub usa .github\workflows\android-ci.yml: Gradle 8.13 viene installato automaticamente.
exit /b 1
