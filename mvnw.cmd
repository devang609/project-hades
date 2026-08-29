@echo off
:: Maven Wrapper script for Windows

setlocal

set "MAVEN_PROJECTBASEDIR=%~dp0"
set "MAVEN_WRAPPER_PROPERTIES=%MAVEN_PROJECTBASEDIR%.mvn\wrapper\maven-wrapper.properties"

set "MAVEN_DIST_URL=https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.16/apache-maven-3.9.16-bin.zip"

if exist "%MAVEN_WRAPPER_PROPERTIES%" (
    for /f "tokens=1,* delims==" %%a in ('findstr "distributionUrl" "%MAVEN_WRAPPER_PROPERTIES%"') do set "MAVEN_DIST_URL=%%b"
)

set "MAVEN_HOME=%USERPROFILE%\.m2\wrapper\dists\apache-maven-3.9.16"

if not exist "%MAVEN_HOME%\apache-maven-3.9.16\bin\mvn.cmd" (
    echo Downloading Maven from %MAVEN_DIST_URL%
    mkdir "%MAVEN_HOME%" 2>nul
    powershell -Command "Invoke-WebRequest -Uri '%MAVEN_DIST_URL%' -OutFile '%MAVEN_HOME%\maven.zip'"
    powershell -Command "Expand-Archive -Path '%MAVEN_HOME%\maven.zip' -DestinationPath '%MAVEN_HOME%' -Force"
    del "%MAVEN_HOME%\maven.zip"
)

"%MAVEN_HOME%\apache-maven-3.9.16\bin\mvn.cmd" %*
