#!/usr/bin/env bash

# Copyright (c) 2019 Cisco and/or its affiliates.
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at:
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

MAIN_VER="$(./version | cut -f1 -d"-")"
VERSION="$(./version | cut -f1 -d"~")"

echo "Main version: ${MAIN_VER}"
echo "full version: ${VERSION}"
cd ./java

echo "Installing jars to Maven:"
mvn install:install-file -Dfile=jvpp-registry-${MAIN_VER}.jar -DgroupId=io.fd.vpp     -DartifactId=jvpp-registry -Dversion=${VERSION} -Dpackaging=jar
mvn install:install-file -Dfile=jvpp-core-${MAIN_VER}.jar -DgroupId=io.fd.vpp     -DartifactId=jvpp-core -Dversion=${VERSION} -Dpackaging=jar
mvn install:install-file -Dfile=jvpp-ioampot-${MAIN_VER}.jar -DgroupId=io.fd.vpp     -DartifactId=jvpp-ioampot -Dversion=${VERSION} -Dpackaging=jar
mvn install:install-file -Dfile=jvpp-ioamtrace-${MAIN_VER}.jar -DgroupId=io.fd.vpp     -DartifactId=jvpp-ioamtrace -Dversion=${VERSION} -Dpackaging=jar
mvn install:install-file -Dfile=jvpp-ioamexport-${MAIN_VER}.jar -DgroupId=io.fd.vpp     -DartifactId=jvpp-ioamexport -Dversion=${VERSION} -Dpackaging=jar
mvn install:install-file -Dfile=jvpp-nat-${MAIN_VER}.jar -DgroupId=io.fd.vpp     -DartifactId=jvpp-nat -Dversion=${VERSION} -Dpackaging=jar
mvn install:install-file -Dfile=jvpp-nsh-${MAIN_VER}.jar -DgroupId=io.fd.vpp     -DartifactId=jvpp-nsh -Dversion=${VERSION} -Dpackaging=jar
mvn install:install-file -Dfile=jvpp-acl-${MAIN_VER}.jar -DgroupId=io.fd.vpp     -DartifactId=jvpp-acl -Dversion=${VERSION} -Dpackaging=jar

echo "all done."