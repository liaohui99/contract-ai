#!/bin/sh
# ----------------------------------------------------------------------------
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
# ----------------------------------------------------------------------------

# ----------------------------------------------------------------------------
# Apache Maven Wrapper startup script, version 3.3.2
#
# Optional ENV vars:
#   MVNW_REPOURL - repo url base for downloading maven distribution
#   MVNW_USERNAME/MVNW_PASSWORD - user and password for downloading maven
#   MVNW_VERBOSE - true: enable verbose log; others: silence the output
# ----------------------------------------------------------------------------

if [ -z "$MVNW_REPOURL" ]; then
  MVNW_REPOURL="https://repo.maven.apache.org/maven2"
fi
MVNW_USERNAME="${MVNW_USERNAME:-}"
MVNW_PASSWORD="${MVNW_PASSWORD:-}"

# Detect current OS and architecture
OS_ARCH=$(uname -s | tr '[:upper:]' '[:lower:]')-$(uname -m | tr '[:upper:]' '[:lower:]')

# Use the version specified in the property file
WRAPPER_VERSION=$(grep "distributionUrl=" .mvn/wrapper/maven-wrapper.properties | cut -d '#' -f 1 | sed 's/^.*\/\(apache-maven-[0-9.]*\)-bin.zip/\1/' | head -n 1)
if [ -z "$WRAPPER_VERSION" ]; then
  # Fallback to a known version if the property file doesn't contain the expected format
  WRAPPER_VERSION="apache-maven-3.9.6"
fi

# Use the binary wrapper
WRAPPER_JAR=".mvn/wrapper/maven-wrapper.jar"

# Maven command line arguments
MAVEN_CMD_LINE_ARGS="$@"

# Execute the command
exec java -Dmaven.multiModuleProjectDirectory="`pwd`" \
  -Dmaven.home="/tmp/$WRAPPER_VERSION" \
  -Dmaven.wrapper.repo.url="${MVNW_REPOURL}" \
  -Dmaven.wrapper.repo.username="${MVNW_USERNAME}" \
  -Dmaven.wrapper.repo.password="${MVNW_PASSWORD}" \
  -jar "$WRAPPER_JAR" $MAVEN_CMD_LINE_ARGS