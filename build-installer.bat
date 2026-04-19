@echo off
setlocal enabledelayedexpansion

echo ============================================
echo   Scrabblohhhhh - EXE Installer Builder
echo ============================================
echo.

set "PROJECT_DIR=%~dp0"
set "REPO_WIX_EXT_DIR=%PROJECT_DIR%..\.wix\extensions"
set "USER_WIX_EXT_DIR=%USERPROFILE%\.wix\extensions"
set "WIX_EXT_VERSION=7.0.0"

REM --- Check JAVA_HOME ---
if "%JAVA_HOME%"=="" (
    echo [INFO] JAVA_HOME not set, attempting auto-detect...
    for /f "tokens=*" %%i in ('where java 2^>nul') do (
        set "JAVA_BIN=%%i"
        goto :found_java
    )
    echo [ERROR] Java not found. Please install JDK 25 and set JAVA_HOME.
    pause
    exit /b 1
)
goto :java_ready

:found_java
REM Resolve: ...\bin\java.exe -> ...
for %%A in ("%JAVA_BIN%") do set "BIN_DIR=%%~dpA"
for %%A in ("%BIN_DIR%..") do set "JAVA_HOME=%%~fA"
echo [INFO] Auto-detected JAVA_HOME: %JAVA_HOME%

:java_ready
echo [INFO] Using JAVA_HOME: %JAVA_HOME%

where jpackage >nul 2>&1
if %ERRORLEVEL% neq 0 (
    REM Try using JAVA_HOME directly
    if exist "%JAVA_HOME%\bin\jpackage.exe" (
        set "PATH=%JAVA_HOME%\bin;%PATH%"
    ) else (
        echo [ERROR] jpackage not found. Ensure JDK 25 is installed.
        pause
        exit /b 1
    )
)

REM --- Check WiX ---
where wix >nul 2>&1
if %ERRORLEVEL% neq 0 (
    REM Try dotnet tools path
    set "DOTNET_TOOLS=%USERPROFILE%\.dotnet\tools"
    if exist "!DOTNET_TOOLS!\wix.exe" (
        set "PATH=!DOTNET_TOOLS!;!PATH!"
        echo [INFO] Found WiX in dotnet tools directory.
    ) else (
        echo [ERROR] WiX Toolset not found.
        echo        Install with: dotnet tool install --global wix
        pause
        exit /b 1
    )
)

call :ensure_wix_extension "WixToolset.Util.wixext"
if %ERRORLEVEL% neq 0 exit /b 1
call :ensure_wix_extension "WixToolset.UI.wixext"
if %ERRORLEVEL% neq 0 exit /b 1

REM --- Configuration ---
set "TARGET_DIR=%PROJECT_DIR%target"
set "FAT_JAR=%TARGET_DIR%\scrabblohhhhh-1.0-SNAPSHOT-fat.jar"
set "NATIVE_DIR=%PROJECT_DIR%native"
set "OUTPUT_DIR=%PROJECT_DIR%dist-installer"
set "APP_NAME=Scrabblohhhhh"
set "APP_VERSION=1.0"
set "MAIN_CLASS=com.kotva.launcher.MainApp"

REM --- Step 1: Maven Build ---
echo.
echo [1/4] Building fat JAR with Maven...
cd /d "%PROJECT_DIR%"
if exist "%TARGET_DIR%\jpackage-temp" (
    attrib -r "%TARGET_DIR%\jpackage-temp" /s /d >nul 2>&1
    rmdir /s /q "%TARGET_DIR%\jpackage-temp"
)
call mvn clean package -DskipTests -q
if %ERRORLEVEL% neq 0 (
    echo [ERROR] Maven build failed.
    pause
    exit /b 1
)

if not exist "%FAT_JAR%" (
    echo [ERROR] Fat JAR not found at: %FAT_JAR%
    pause
    exit /b 1
)
echo        Fat JAR created successfully.

REM --- Step 2: Stage files ---
echo.
echo [2/4] Staging input files...
set "TEMP_INPUT=%TARGET_DIR%\jpackage-input"
if exist "%TEMP_INPUT%" rmdir /s /q "%TEMP_INPUT%"
mkdir "%TEMP_INPUT%"

copy "%FAT_JAR%" "%TEMP_INPUT%\scrabblohhhhh.jar" >nul

if exist "%NATIVE_DIR%\quackle_ffm.dll" (
    copy "%NATIVE_DIR%\quackle_ffm.dll" "%TEMP_INPUT%\" >nul
)
if exist "%NATIVE_DIR%\libwinpthread-1.dll" (
    copy "%NATIVE_DIR%\libwinpthread-1.dll" "%TEMP_INPUT%\" >nul
)

REM Copy Quackle data directory
set "QUACKLE_DATA=%PROJECT_DIR%quackle-master\data"
if exist "%QUACKLE_DATA%" (
    xcopy "%QUACKLE_DATA%" "%TEMP_INPUT%\data\" /e /i /q >nul
)
echo        Input files staged.

REM --- Step 3: Generate EXE installer ---
echo.
echo [3/4] Creating EXE installer with jpackage...
echo        (This may take a minute...)
if exist "%OUTPUT_DIR%" rmdir /s /q "%OUTPUT_DIR%"

jpackage ^
    --type exe ^
    --name "%APP_NAME%" ^
    --app-version "%APP_VERSION%" ^
    --input "%TEMP_INPUT%" ^
    --main-jar scrabblohhhhh.jar ^
    --main-class "%MAIN_CLASS%" ^
    --dest "%OUTPUT_DIR%" ^
    --vendor "Kotva" ^
    --description "Scrabblohhhhh - A Scrabble Desktop Game" ^
    --win-dir-chooser ^
    --win-shortcut ^
    --win-shortcut-prompt ^
    --win-menu ^
    --win-menu-group "Scrabblohhhhh" ^
    --java-options "--enable-native-access=ALL-UNNAMED" ^
    --java-options "-Djava.library.path=$APPDIR"

if %ERRORLEVEL% neq 0 (
    echo.
    echo [ERROR] jpackage failed. See output above.
    pause
    exit /b 1
)

REM --- Step 4: Show result ---
echo.
echo [4/4] Locating installer...
for %%f in ("%OUTPUT_DIR%\%APP_NAME%*.exe") do (
    set "INSTALLER_FILE=%%f"
)

echo.
echo ============================================
echo   BUILD SUCCESSFUL!
echo   Installer: !INSTALLER_FILE!
echo ============================================
echo.
echo   The installer will:
echo     - Let users choose install directory
echo     - Create a Start Menu shortcut
echo     - Create a Desktop shortcut (optional)
echo     - Bundle a private JRE (no Java needed)
echo.
pause
exit /b 0

:ensure_wix_extension
set "EXT_NAME=%~1"
set "EXT_CACHE_DIR=%USER_WIX_EXT_DIR%\%~1"

if exist "!EXT_CACHE_DIR!" (
    echo [INFO] WiX extension already available: %~1
    exit /b 0
)

if exist "%REPO_WIX_EXT_DIR%\%~1" (
    echo [INFO] Restoring WiX extension from repository cache: %~1
    if not exist "%USER_WIX_EXT_DIR%" mkdir "%USER_WIX_EXT_DIR%"
    xcopy "%REPO_WIX_EXT_DIR%\%~1" "!EXT_CACHE_DIR!\" /e /i /q /y >nul
    if exist "!EXT_CACHE_DIR!" (
        exit /b 0
    )
)

echo [INFO] Installing WiX extension: %~1
wix extension add -g %~1/%WIX_EXT_VERSION% >nul
if %ERRORLEVEL% neq 0 (
    echo [ERROR] Failed to install WiX extension: %~1
    echo        Try running: wix extension add -g %~1/%WIX_EXT_VERSION%
    pause
    exit /b 1
)

exit /b 0
