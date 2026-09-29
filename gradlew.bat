@echo off
setlocal
set GRADLE_VERSION=8.9
set CACHE_DIR=%USERPROFILE%\.gradle\wrapper\dists\gradle-%GRADLE_VERSION%-bin\local
set GRADLE_BIN=%CACHE_DIR%\gradle-%GRADLE_VERSION%\bin\gradle.bat
if exist "%GRADLE_BIN%" goto run
where java >nul 2>nul || (echo Java 17 saknas. & exit /b 1)
where curl >nul 2>nul || (echo curl saknas. & exit /b 1)
powershell -NoProfile -Command "New-Item -ItemType Directory -Force '%CACHE_DIR%' | Out-Null; Invoke-WebRequest 'https://services.gradle.org/distributions/gradle-%GRADLE_VERSION%-bin.zip' -OutFile '%CACHE_DIR%\gradle.zip'; Expand-Archive -Force '%CACHE_DIR%\gradle.zip' '%CACHE_DIR%'; Remove-Item '%CACHE_DIR%\gradle.zip'" || exit /b 1
:run
call "%GRADLE_BIN%" -p "%~dp0" %*
