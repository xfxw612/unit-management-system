@echo off
REM ============================================================
REM  Unit Management System - one click start (H2 in-memory db)
REM  Requires: JDK 17+ and Maven 3.6+ on PATH
REM ============================================================
setlocal

where java >nul 2>nul
if errorlevel 1 (
  echo [ERROR] java not found. Install JDK 17+ and add its bin to PATH.
  echo         https://adoptium.net/
  exit /b 1
)

where mvn >nul 2>nul
if errorlevel 1 (
  echo [ERROR] mvn not found. Install Maven 3.6+ and add its bin to PATH.
  echo         https://maven.apache.org/download.cgi
  exit /b 1
)

echo Starting with H2 in-memory db on port 8088 ...
echo   URL     : http://localhost:8088/login.html
echo   Account : admin / admin123
echo.
call mvn -q spring-boot:run

endlocal
