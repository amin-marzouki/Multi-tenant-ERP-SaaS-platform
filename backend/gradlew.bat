@echo off
setlocal

set ROOT_DIR=%~dp0
set GRADLE_VERSION=8.9
set GRADLE_HOME=%ROOT_DIR%.gradle
set GRADLE_DIST=%GRADLE_HOME%\gradle-%GRADLE_VERSION%
set ARCHIVE=%GRADLE_HOME%\gradle-%GRADLE_VERSION%-bin.zip

if not exist "%GRADLE_HOME%" mkdir "%GRADLE_HOME%"

if not exist "%GRADLE_DIST%\bin\gradle.bat" (
    if not exist "%ARCHIVE%" (
        echo Downloading Gradle %GRADLE_VERSION%...
        powershell -NoProfile -ExecutionPolicy Bypass -Command "Invoke-WebRequest -Uri 'https://services.gradle.org/distributions/gradle-%GRADLE_VERSION%-bin.zip' -OutFile '%ARCHIVE%'"
    )

    if exist "%GRADLE_DIST%" rmdir /s /q "%GRADLE_DIST%"
    powershell -NoProfile -ExecutionPolicy Bypass -Command "Expand-Archive -Path '%ARCHIVE%' -DestinationPath '%GRADLE_HOME%' -Force"
)

call "%GRADLE_DIST%\bin\gradle.bat" %*
