@REM ----------------------------------------------------------------------------
@REM Maven Wrapper startup batch script
@REM ----------------------------------------------------------------------------

@IF "%DEBUG%"=="" @ECHO OFF
@setlocal

set MVNW_VERBOSE=false

SET WRAPPER_JAR="%~dp0\.mvn\wrapper\maven-wrapper.jar"
SET WRAPPER_URL="https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar"

@REM Download maven-wrapper.jar if not found
IF NOT EXIST %WRAPPER_JAR% (
    powershell -Command "(New-Object Net.WebClient).DownloadFile('%WRAPPER_URL:"=%', '%WRAPPER_JAR:"=%')" || (
        echo Failed to download maven-wrapper.jar
        exit /b 1
    )
)

SET MAVEN_PROJECTBASEDIR=%~dp0
SET MAVEN_OPTS=-Xmx512m

FOR /F "tokens=*" %%G IN ('type "%~dp0\.mvn\wrapper\maven-wrapper.properties" ^| findstr "distributionUrl"') DO SET DIST_URL=%%G
SET DIST_URL=%DIST_URL:distributionUrl=%
SET DIST_URL=%DIST_URL:~1%

SET MAVEN_HOME=%USERPROFILE%\.m2\wrapper\dists\apache-maven-3.9.6
SET MAVEN_CMD=%MAVEN_HOME%\bin\mvn.cmd

IF NOT EXIST "%MAVEN_CMD%" (
    echo Downloading Maven distribution...
    IF NOT EXIST "%MAVEN_HOME%" mkdir "%MAVEN_HOME%"
    SET ZIP_PATH=%TEMP%\maven.zip
    powershell -Command "Invoke-WebRequest -Uri '%DIST_URL%' -OutFile '%TEMP%\maven.zip'" || (
        echo Failed to download Maven
        exit /b 1
    )
    powershell -Command "Expand-Archive -Force '%TEMP%\maven.zip' '%USERPROFILE%\.m2\wrapper\dists'"
    del "%TEMP%\maven.zip"

    @REM Find the extracted directory
    FOR /D %%D IN ("%USERPROFILE%\.m2\wrapper\dists\apache-maven-*") DO SET MAVEN_HOME=%%D
    SET MAVEN_CMD=!MAVEN_HOME!\bin\mvn.cmd
)

"%MAVEN_HOME%\bin\mvn.cmd" %*
