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

javac -encoding UTF-8 -sourcepath src/main/java -cp "lib\gson-2.10.1.jar" -d target/classes src/main/java/com/team/gardendefense/main/GameLauncher.java src/main/java/com/team/gardendefense/editor/LevelEditor.java

if ($LASTEXITCODE -eq 0) {
    java -cp "target/classes;lib\gson-2.10.1.jar" $mainClass
} else {
    Write-Host "[LOI] Bien dich khong thanh cong! Vui long kiem tra JDK." -ForegroundColor Red
}
