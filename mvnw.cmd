@ECHO OFF
SETLOCAL

IF NOT DEFINED JAVA_HOME (
  SET JAVA_EXE=java
) ELSE (
  SET JAVA_EXE=%JAVA_HOME%\bin\java.exe
)

SET WRAPPER_DIR=%~dp0.mvn\wrapper
SET WRAPPER_JAR=%WRAPPER_DIR%\maven-wrapper.jar
SET WRAPPER_PROPERTIES=%WRAPPER_DIR%\maven-wrapper.properties

IF NOT EXIST "%WRAPPER_JAR%" (
  CALL "%WRAPPER_DIR%\maven-wrapper-download.cmd" "%WRAPPER_DIR%" "%WRAPPER_PROPERTIES%" "%WRAPPER_JAR%"
  IF %ERRORLEVEL% NEQ 0 EXIT /B %ERRORLEVEL%
)

"%JAVA_EXE%" -Dmaven.multiModuleProjectDirectory=%CD% -cp "%WRAPPER_JAR%" org.apache.maven.wrapper.MavenWrapperMain %*
