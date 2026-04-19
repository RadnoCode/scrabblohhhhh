Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $PSScriptRoot
$targetDir = Join-Path $projectRoot "target"
$appInputDir = Join-Path $targetDir "app-input"
$distDir = Join-Path $targetDir "dist"
$packageName = "Scrabblohhhhh"
$appVersion = "1.0.0"
$mainJar = "scrabblohhhhh-1.0-SNAPSHOT.jar"
$iconPath = Join-Path $projectRoot "quackle-master\quacker\quacker.ico"

Write-Host "Packaging $packageName from $projectRoot"

Push-Location $projectRoot
try {
    & mvn "-DskipTests" clean package dependency:copy-dependencies "-DincludeScope=runtime" "-DoutputDirectory=$appInputDir"

    $builtJar = Join-Path $targetDir $mainJar
    if (-not (Test-Path $builtJar)) {
        throw "Built jar not found: $builtJar"
    }

    New-Item -ItemType Directory -Force -Path $appInputDir | Out-Null
    Copy-Item $builtJar -Destination $appInputDir -Force

    $nativeSource = Join-Path $projectRoot "native"
    $nativeTarget = Join-Path $appInputDir "native"
    New-Item -ItemType Directory -Force -Path $nativeTarget | Out-Null
    Copy-Item (Join-Path $nativeSource "*") -Destination $nativeTarget -Recurse -Force

    $dataSource = Join-Path $projectRoot "quackle-master\data"
    $dataTarget = Join-Path $appInputDir "quackle-data"
    New-Item -ItemType Directory -Force -Path $dataTarget | Out-Null
    Copy-Item (Join-Path $dataSource "*") -Destination $dataTarget -Recurse -Force

    if (Test-Path $distDir) {
        Remove-Item $distDir -Recurse -Force
    }
    New-Item -ItemType Directory -Force -Path $distDir | Out-Null

    $jpackageArgs = @(
        "--type", "app-image",
        "--dest", $distDir,
        "--input", $appInputDir,
        "--name", $packageName,
        "--app-version", $appVersion,
        "--main-jar", $mainJar,
        "--main-class", "com.kotva.launcher.MainApp",
        "--java-options", "--enable-native-access=ALL-UNNAMED",
        "--win-console"
    )

    if (Test-Path $iconPath) {
        $jpackageArgs += @("--icon", $iconPath)
    }

    & jpackage @jpackageArgs

    Write-Host ""
    Write-Host "Packaged app image:"
    Write-Host "  $distDir\$packageName"
    Write-Host "Launcher:"
    Write-Host "  $distDir\$packageName\$packageName.exe"
}
finally {
    Pop-Location
}
