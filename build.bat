@echo off
rem ----------------------------------------------------------------------------
rem build.bat - Win32 Build Script for AraMorph
rem
rem ----------------------------------------------------------------------------

rem ----- Copy Xalan and Xerces for the build system    ------------------------
rem if not exist "tools\lib\xerces*.jar" copy lib\core\xerces*.jar tools\lib
rem if not exist "tools\lib\xalan*.jar" copy lib\core\xalan*.jar tools\lib
rem if not exist "tools\lib\xml-api*.jar" copy lib\core\xml-api*.jar tools\lib

rem ----- Verify and Set Required Environment Variables ------------------------

rem ----- Use Java in JAVA_HOME if JAVA_HOME is set.
set OLD_PATH=%PATH%
if "%JAVA_HOME%" == "" goto noJavaHome
echo Using Java from %JAVA_HOME%
set PATH=%JAVA_HOME%\bin
:noJavaHome

rem ----- Use Ant shipped with AraMorph. Ignore installed in the system Ant
set OLD_ANT_HOME=%ANT_HOME%
set ANT_HOME=tools

call %ANT_HOME%\bin\ant %1 %2 %3 %4 %5 %6 %7 %8 %9

rem ----- Restore ANT_HOME
set ANT_HOME=%OLD_ANT_HOME%
set OLD_ANT_HOME=

rem ----- Restore PATH
set PATH=%OLD_PATH%
set OLD_PATH=

