@ECHO OFF
SET DIRNAME=%~dp0
IF "%DIRNAME%"=="" SET DIRNAME=.
SET APP_HOME=%DIRNAME%
SET CLASSPATH=%APP_HOME%\gradle\wrapper\gradle-wrapper.jar
java -Dorg.gradle.appname=%~n0 -classpath "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*
