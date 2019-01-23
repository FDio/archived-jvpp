# Copyright (c) 2018 Cisco and/or its affiliates.
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

cmake_minimum_required(VERSION 3.5 FATAL_ERROR)
add_custom_target(install-dep COMMAND make install-dep
        WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}/Requirements
        )

project(jvpp)

include(CheckCCompilerFlag)

if(CMAKE_SYSTEM_PROCESSOR MATCHES "amd64.*|x86_64.*|AMD64.*")
  set(CMAKE_C_FLAGS "-march=corei7 -mtune=corei7-avx ${CMAKE_C_FLAGS}")
elseif(CMAKE_SYSTEM_PROCESSOR MATCHES "^(aarch64.*|AARCH64.*)")
  set(CMAKE_C_FLAGS "-march=armv8-a+crc ${CMAKE_C_FLAGS}")
endif()

check_c_compiler_flag("-Wno-address-of-packed-member" compiler_flag_no_address_of_packed_member)
if (compiler_flag_no_address_of_packed_member)
  add_definitions(-Wno-address-of-packed-member)
endif()

message("\nEnvironment Variables:")

# JVPP RELATED VARIABLES
unset(JVPP_VERSION)
unset(JAPI_LIB_VERSION)
execute_process(
  WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}/
  COMMAND ./version
  OUTPUT_VARIABLE JVPP_VERSION
  OUTPUT_STRIP_TRAILING_WHITESPACE
)
string(REPLACE "-" ";" JAPI_LIB_VERSION ${JVPP_VERSION})
list(GET JAPI_LIB_VERSION 0 JAPI_LIB_VERSION)

message("    JVPP Main Version: ${JVPP_VERSION}")
message("    JVPP Version: ${JAPI_LIB_VERSION}")

# OS RELATED VARIABLES
unset(RELEASE_ID)
unset(RELEASE_CODENAME)
find_program(LSB_RELEASE_EXEC lsb_release)
execute_process(COMMAND ${LSB_RELEASE_EXEC} -is
        OUTPUT_VARIABLE RELEASE_ID
        OUTPUT_STRIP_TRAILING_WHITESPACE
        )

execute_process(COMMAND ${LSB_RELEASE_EXEC} -cs
        OUTPUT_VARIABLE RELEASE_CODENAME
        OUTPUT_STRIP_TRAILING_WHITESPACE
        )

message("    OS Release Id: ${RELEASE_ID} ")
message("    OS Release Codename: ${RELEASE_CODENAME} ")


set(CMAKE_LIBRARY_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}/build-root/lib)
set(CMAKE_INSTALL_RPATH ${CMAKE_INSTALL_PREFIX}/lib)
set(CMAKE_INSTALL_MESSAGE NEVER)

find_package(Threads REQUIRED)

unset(dirlist)

macro(subdirlist dirlist dirpath)
  file(GLOB dirs RELATIVE ${dirpath} ${dirpath}/*)
  foreach(dir ${dirs})
    if(IS_DIRECTORY ${dirpath}/${dir})
      list(APPEND dirlist ${dirpath}/${dir})
    endif()
  endforeach()
endmacro()

#list(APPEND dirlist $ENV{JAVA_HOME})
#subdirlist(dirlist /usr/lib/jvm)
#subdirlist(dirlist /usr/lib64/jvm)
#
#unset(JAVA_HOME_SET)
#find_path(JAVA_HOME_SET NAMES include/jni.h PATHS ${dirlist})
#if (NOT JAVA_HOME_SET)
#  message("JAVA_HOME is not found")
#else()
#  message("JAVA HOME: ${JAVA_HOME}")
#  set(ENV{JAVA_HOME} "${JAVA_HOME_SET}")
#endif()

message("\nJAVA:")
message("    JAVA_HOME: $ENV{JAVA_HOME}")

find_package(Java 1.8 REQUIRED COMPONENTS Development)
message("JAVA: ${Java}")
get_filename_component(jvm_path ${Java_JAVAC_EXECUTABLE} DIRECTORY)
set (Java_INCLUDE_DIRS ${jvm_path}/../include ${jvm_path}/../include/linux)

message("    Java headers: ${Java_INCLUDE_DIRS}")
message("    Java compiler: ${Java_JAVAC_EXECUTABLE}")
add_subdirectory(java)

#find_package(vpp REQUIRED)
