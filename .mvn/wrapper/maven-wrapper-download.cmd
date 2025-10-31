@ECHO OFF
SETLOCAL ENABLEDELAYEDEXPANSION

SET WRAPPER_DIR=%~1
SET PROPERTIES_FILE=%~2
SET JAR_FILE=%~3

IF NOT EXIST "%PROPERTIES_FILE%" (
  ECHO Cannot find %PROPERTIES_FILE%
  EXIT /B 1
)

FOR /F "tokens=1,* delims==" %%A IN (%PROPERTIES_FILE%) DO (
  IF "%%A"=="wrapperUrl" SET WRAPPER_URL=%%B
)

IF NOT DEFINED WRAPPER_URL (
  ECHO wrapperUrl is not set in %PROPERTIES_FILE%
  EXIT /B 1
)

IF NOT EXIST "%WRAPPER_DIR%" (
  MKDIR "%WRAPPER_DIR%"
)

SET TMP_JAR=%WRAPPER_DIR%\maven-wrapper.jar.part

WHERE curl >NUL 2>&1
IF %ERRORLEVEL% EQU 0 (
  curl -fsSL "%WRAPPER_URL%" -o "%TMP_JAR%"
) ELSE (
  WHERE powershell >NUL 2>&1
  IF %ERRORLEVEL% EQU 0 (
    powershell -Command "(New-Object System.Net.WebClient).DownloadFile('%WRAPPER_URL%', '%TMP_JAR%')"
  ) ELSE (
    ECHO curl or PowerShell is required to download the Maven wrapper.
    EXIT /B 1
  )
)

MOVE /Y "%TMP_JAR%" "%JAR_FILE%" >NUL
