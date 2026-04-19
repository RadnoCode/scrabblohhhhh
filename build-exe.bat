@echo off
setlocal enabledelayedexpansion

echo ============================================
echo   Scrabblohhhhh - Build EXE Packager
echo ============================================
echo.

REM --- Check JAVA_HOME ---
if "%JAVA_HOME%"=="" (
    echo [ERROR] JAVA_HOME is not set. Please install JDK 25 and set JAVA_HOME.
    pause
    exit /b 1
)

where jpackage >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo [ERROR] jpackage not found in PATH. Ensure JDK 25 is installed.
    pause
    exit /b 1
)

REM --- Configuration ---
set "PROJECT_DIR=%~dp0"
set "TARGET_DIR=%PROJECT_DIR%target"
set "FAT_JAR=%TARGET_DIR%\scrabblohhhhh-1.0-SNAPSHOT-fat.jar"
set "NATIVE_DIR=%PROJECT_DIR%native"
set "OUTPUT_DIR=%PROJECT_DIR%dist"
set "APP_NAME=Scrabblohhhhh"
set "APP_VERSION=1.0"
set "MAIN_CLASS=com.kotva.launcher.MainApp"

REM --- Step 1: Maven Build ---
echo [1/3] Building fat JAR with Maven...
cd /d "%PROJECT_DIR%"
call mvn clean package -DskipTests -q
if %ERRORLEVEL% neq 0 (
    echo [ERROR] Maven build failed.
    pause
    exit /b 1
)

if not exist "%FAT_JAR%" (
    echo [ERROR] Fat JAR not found at: %FAT_JAR%
    echo        Expected: scrabblohhhhh-1.0-SNAPSHOT-fat.jar
    pause
    exit /b 1
)
echo        Fat JAR created successfully.
echo.

REM --- Step 2: Prepare temp directory with native libs ---
echo [2/3] Preparing native libraries...
set "TEMP_INPUT=%TARGET_DIR%\jpackage-input"
if exist "%TEMP_INPUT%" rmdir /s /q "%TEMP_INPUT%"
mkdir "%TEMP_INPUT%"

copy "%FAT_JAR%" "%TEMP_INPUT%\scrabblohhhhh.jar" >nul

REM Copy native DLLs into the input folder so jpackage bundles them
if exist "%NATIVE_DIR%\quackle_ffm.dll" (
    copy "%NATIVE_DIR%\quackle_ffm.dll" "%TEMP_INPUT%\" >nul
)
if exist "%NATIVE_DIR%\libwinpthread-1.dll" (
    copy "%NATIVE_DIR%\libwinpthread-1.dll" "%TEMP_INPUT%\" >nul
)
echo        Native libraries staged.
echo.

REM --- Step 3: Run jpackage ---
echo [3/3] Creating EXE with jpackage...
if exist "%OUTPUT_DIR%" rmdir /s /q "%OUTPUT_DIR%"

jpackage ^
    --type app-image ^
    --name "%APP_NAME%" ^
    --app-version "%APP_VERSION%" ^
    --input "%TEMP_INPUT%" ^
    --main-jar scrabblohhhhh.jar ^
    --main-class "%MAIN_CLASS%" ^
    --dest "%OUTPUT_DIR%" ^
    --java-options "--enable-native-access=ALL-UNNAMED" ^
    --java-options "-Djava.library.path=$APPDIR"

if %ERRORLEVEL% neq 0 (
    echo [ERROR] jpackage failed. See output above.
    pause
    exit /b 1
)

REM --- Copy Quackle data directory if it exists ---
set "QUACKLE_DATA=%PROJECT_DIR%quackle-master\data"
if exist "%QUACKLE_DATA%" (
    echo        Copying Quackle data files...
    xcopy "%QUACKLE_DATA%" "%OUTPUT_DIR%\%APP_NAME%\app\data\" /e /i /q >nul
)

echo.
echo ============================================
echo   BUILD SUCCESSFUL!
echo   Output: %OUTPUT_DIR%\%APP_NAME%\
echo   EXE:    %OUTPUT_DIR%\%APP_NAME%\%APP_NAME%.exe
echo ============================================
echo.
pause
