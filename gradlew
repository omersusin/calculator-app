#!/bin/sh
APP_BASE_NAME=${0##*/}
APP_HOME=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd -P)
CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar

if [ -n "$JAVA_HOME" ] ; then
  if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
    JAVACMD=$JAVA_HOME/jre/sh/java
  else
    JAVACMD=$JAVA_HOME/bin/java
  fi
else
  JAVACMD=java
fi

exec "$JAVACMD" -Dorg.gradle.appname="$APP_BASE_NAME" -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
