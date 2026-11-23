param (
    [string]$Target = "GameLauncher"
)

Set-Location $PSScriptRoot

$mainClass = "com.team.gardendefense.main.GameLauncher"
if ($Target -eq "LevelEditor" -or $Target -eq "editor") {
    $mainClass = "com.team.gardendefense.editor.LevelEditor"
}

Write-Host "========================================================" -ForegroundColor Green
Write-Host "  Dang bien dich va khoi dong tro choi Bao Ve Khu Vuon..." -ForegroundColor Green
Write-Host "  Lop khoi chay: $mainClass" -ForegroundColor Green
Write-Host "========================================================" -ForegroundColor Green

if (-not $env:JAVA_HOME) {
    if (Test-Path "C:\Program Files\Java\jdk-26.0.2") {
        $env:JAVA_HOME = "C:\Program Files\Java\jdk-26.0.2"
    } elseif (Test-Path "C:\Program Files\Java\latest") {
        $env:JAVA_HOME = "C:\Program Files\Java\latest"
    }
}
if ($env:JAVA_HOME) {
    $env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
}

javac --release 11 -encoding UTF-8 -sourcepath src/main/java -cp "lib\gson-2.10.1.jar" -d target/classes src/main/java/com/team/gardendefense/main/GameLauncher.java src/main/java/com/team/gardendefense/editor/LevelEditor.java

if ($LASTEXITCODE -eq 0) {
    java -cp "target/classes;src/main/resources;lib\gson-2.10.1.jar" $mainClass
} else {
    Write-Host "[LOI] Bien dich khong thanh cong! Vui long kiem tra JDK." -ForegroundColor Red
}
