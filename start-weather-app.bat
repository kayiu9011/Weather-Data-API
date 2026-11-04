@echo off
setlocal
cd /d "%~dp0"

where mvn >nul 2>nul
if errorlevel 1 (
    echo Maven was not found. Install Maven and make sure the mvn command is available.
    echo See README.md for the project requirements.
    pause
    exit /b 1
)

echo Starting Weather Data API at http://localhost:8081 ...
echo Keep this window open while using the application.
echo Press Ctrl+C here when you want to stop the server.
echo.

start "" /b powershell -NoProfile -WindowStyle Hidden -Command "$url = 'http://localhost:8081'; for ($attempt = 0; $attempt -lt 90; $attempt++) { try { $response = Invoke-WebRequest -UseBasicParsing -Uri ($url + '/api/health') -TimeoutSec 1; if ($response.StatusCode -eq 200) { Start-Process $url; exit 0 } } catch { }; Start-Sleep -Seconds 1 }"

call mvn spring-boot:run

echo.
echo Weather Data API has stopped.
pause
