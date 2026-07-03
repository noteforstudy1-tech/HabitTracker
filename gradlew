#!/bin/sh
#
# Copyright © 2015-2021 the original authors.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      https://www.apache.org/licenses/LICENSE-2.0
#
# Gradle start up script for UN*X

##############################################################################
# Determine the Java command to use to start the JVM.
##############################################################################
if [ -n "$JAVA_HOME" ] ; then
    if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
        JAVACMD="$JAVA_HOME/jre/sh/java"
    else
        JAVACMD="$JAVA_HOME/bin/java"
    fi
    if [ ! -x "$JAVACMD" ] ; then
        die "ERROR: JAVA_HOME is set to an invalid directory: $JAVA_HOME"
    fi
else
    JAVACMD="java"
    if ! command -v java > /dev/null 2>&1
    then
        die "ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH."
    fi
fi

# Increase the maximum file descriptors if we can.
MAX_FD="maximum"
warn () {
    echo "$*"
} >&2

die () {
    echo
    echo "$*"
    echo
    exit 1
} >&2

if ! "$cygwin" && ! "$darwin" && ! "$nonstop" ; then
    case $MAX_FD in
      max*)
          # In POSIX sh, ulimit -H is undefined. That's why the result is checked to see if it worked.
          # shellcheck disable=SC2039,SC3045
          MAX_FD=$( ulimit -H -n ) ||
              warn "Could not query maximum file descriptor limit"
          ;;
    esac
    case $MAX_FD in
      '' | soft) :;;
      *)
          # In POSIX sh, ulimit -n is undefined. That's why the result is checked to see if it worked.
          # shellcheck disable=SC2039,SC3045
          ulimit -n "$MAX_FD" ||
              warn "Could not set maximum file descriptor limit to $MAX_FD"
    esac
fi

# Collect all arguments for the java command;
APP_HOME=$( cd "${APP_HOME:-./}" && pwd -P ) || die "APP_HOME path not found"
APP_NAME="Gradle"
APP_BASE_NAME=${0##*/}
DEFAULT_JVM_OPTS='"-Xmx64m" "-Xms64m"'

# Use the maximum available, or set MAX_FD != -1 to use that value.
cygwin=false
darwin=false
nonstop=false
case "$( uname )" in
  CYGWIN* )
    cygwin=true
    ;;
  Darwin* )
    darwin=true
    ;;
  MSYS* | MINGW* )
    msys=true
    ;;
  NONSTOP* )
    nonstop=true
    ;;
esac

CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar

# Execute Gradle
exec "$JAVACMD" \
     $DEFAULT_JVM_OPTS \
     $JAVA_OPTS \
     $GRADLE_OPTS \
     "-Dorg.gradle.appname=$APP_BASE_NAME" \
     -classpath "$CLASSPATH" \
     org.gradle.wrapper.GradleWrapperMain \
     "$@"
