@echo off
pushd "%~dp0"
chcp 65001 >nul
title Bao Ve Khu Vuon - Plants vs Zombies

set MAIN_CLASS=com.team.gardendefense.main.GameLauncher
if /i "%~1"=="LevelEditor" set MAIN_CLASS=com.team.gardendefense.editor.LevelEditor
if /i "%~1"=="editor" set MAIN_CLASS=com.team.gardendefense.editor.LevelEditor

echo ========================================================
echo   Dang bien dich va khoi dong tro choi Bao Ve Khu Vuon...
echo   Lop khoi chay: %MAIN_CLASS%
echo ========================================================

rem Bien dich ma nguon
javac -encoding UTF-8 -sourcepath src/main/java -cp "lib\gson-2.10.1.jar" -d target/classes src/main/java/com/team/gardendefense/main/GameLauncher.java src/main/java/com/team/gardendefense/editor/LevelEditor.java

if %errorlevel% neq 0 (
    echo.
    echo [LOI] Bien dich khong thanh cong!
    echo Vui long dam bao da cai dat JDK tu 11 tro len.
    popd
    pause
    exit /b %errorlevel%
)

rem Chay game
java -cp "target/classes;lib\gson-2.10.1.jar" %MAIN_CLASS%

if %errorlevel% neq 0 (
    echo.
    echo [THONG BAO] Game da dong.
    pause
)
popd
