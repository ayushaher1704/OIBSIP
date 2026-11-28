@echo off
cd /d "%~dp0"
javac -d src src\*.java
if %errorlevel% neq 0 (
    pause
    exit /b %errorlevel%
)
java -cp "src;lib\sqlite-jdbc.jar" Main
del /q src\*.class 2>nul

