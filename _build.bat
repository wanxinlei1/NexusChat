@echo off
set JAVA_HOME=C:\Program Files\Zulu\zulu-17
set PATH=%JAVA_HOME%\bin;%PATH%
cd /d C:\Users\wxl\Desktop\chat
call gradlew.bat assembleDebug --no-daemon --warning-mode=none 2>&1
