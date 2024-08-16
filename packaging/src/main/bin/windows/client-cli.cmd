@echo off

if not exist "%JAVA_HOME%\bin\javaw.exe" echo Please set the JAVA_HOME variable in your environment, We need java(x64)! jdk8 or later is better! & EXIT /B 1
set "JAVA=%JAVA_HOME%\bin\java.exe"

setlocal enabledelayedexpansion

set JARBOOT_HOME=%~dp0
set JARBOOT_HOME=%JARBOOT_HOME:~0,-13%

cd "%JARBOOT_HOME%"

set "TOOL_JAR=components/jarboot-tools.jar io.github.majianzheng.jarboot.tools.client.JarbootClientCli"
set "DAEMON_VM=-Xms50m -Xmx100m -XX:+UseG1GC -XX:MaxGCPauseMillis=500"
set "CLIENT_CMD="%JAVA%" %DAEMON_VM% -cp %TOOL_JAR% %*"

%CLIENT_CMD%
echo Bye.
