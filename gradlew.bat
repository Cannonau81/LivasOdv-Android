@echo off
setlocal
set GRADLE_VERSION=8.13
set BASE_DIR=%~dp0
set BOOTSTRAP_DIR=%BASE_DIR%.gradle-bootstrap
set GRADLE_HOME=%BOOTSTRAP_DIR%\gradle-%GRADLE_VERSION%
set GRADLE_BIN=%GRADLE_HOME%\bin\gradle.bat
set ZIP=%BOOTSTRAP_DIR%\gradle-%GRADLE_VERSION%-bin.zip
set URL=https://services.gradle.org/distributions/gradle-%GRADLE_VERSION%-bin.zip

if not exist "%GRADLE_BIN%" (
  if not exist "%BOOTSTRAP_DIR%" mkdir "%BOOTSTRAP_DIR%"
  powershell -NoProfile -ExecutionPolicy Bypass -Command "$ProgressPreference='SilentlyContinue'; Invoke-WebRequest -UseBasicParsing -Uri '%URL%' -OutFile '%ZIP%'"
  powershell -NoProfile -ExecutionPolicy Bypass -Command "Expand-Archive -LiteralPath '%ZIP%' -DestinationPath '%BOOTSTRAP_DIR%' -Force"
)
call "%GRADLE_BIN%" %*
