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

project(jvpp)
# Add custom modules
set(CMAKE_MODULE_PATH "${CMAKE_SOURCE_DIR}/modules")
include(CheckCCompilerFlag)
include(modules/misc.cmake)
include(modules/cpu.cmake)
include(modules/ccache.cmake)

find_package(Threads REQUIRED)

# OPTIONS
message(INFO "\nSystem:")
info("Architecture" ${CMAKE_SYSTEM_PROCESSOR})
info("C Compiler" ${CMAKE_C_COMPILER})
info("C Flags" ${CMAKE_C_FLAGS})
info("C++ Compiler" ${CMAKE_CXX_COMPILER})
info("C++ Flags" "${CMAKE_CXX_FLAGS}")

# OPTIONS
option(PackZip "Create ZIP package (default: fasle)" false)
option(PackTgz "Create Tgz package (default: fasle)" false)

message(INFO "\nOptions:")
info("JVPP_USE_CCACHE" ${JVPP_USE_CCACHE})
info("PackZip" ${PackZip})
info("PackTgz" ${PackTgz})

message(INFO "\nEnvironment Variables:")

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

info("JVPP Main Version" ${JVPP_VERSION})
info("JVPP Version" ${JAPI_LIB_VERSION})

info("OS Release Id" ${RELEASE_ID})
info("OS Release Codename" ${RELEASE_CODENAME})


set(CMAKE_LIBRARY_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}/build-root/lib)
set(CMAKE_INSTALL_RPATH ${CMAKE_INSTALL_PREFIX}/lib)
set(CMAKE_INSTALL_MESSAGE NEVER)

unset(dirlist)

macro(subdirlist dirlist dirpath)
    file(GLOB dirs RELATIVE ${dirpath} ${dirpath}/*)
    foreach (dir ${dirs})
        if (IS_DIRECTORY ${dirpath}/${dir})
            list(APPEND dirlist ${dirpath}/${dir})
        endif ()
    endforeach ()
endmacro()

list(APPEND dirlist $ENV{JAVA_HOME})
subdirlist(dirlist /usr/lib/jvm)
subdirlist(dirlist /usr/lib64/jvm)

unset(JAVA_HOME_SET)
find_path(JAVA_HOME_SET NAMES include/jni.h PATHS ${dirlist})
if (NOT JAVA_HOME_SET)
  message("JAVA_HOME is not found")
else()
  set(ENV{JAVA_HOME} "${JAVA_HOME_SET}")
endif()

message(INFO "\nJAVA:")
find_package(Java 1.8 REQUIRED COMPONENTS Development)
get_filename_component(jvm_path ${Java_JAVAC_EXECUTABLE} DIRECTORY)
set(Java_INCLUDE_DIRS ${jvm_path}/../include ${jvm_path}/../include/linux)
info("Java home" $ENV{JAVA_HOME})
info("Java" ${Java_JAVA_EXECUTABLE})
info("Java version" ${Java_VERSION_STRING})
info("Java headers" ${Java_INCLUDE_DIRS})
info("Javac" ${Java_JAVAC_EXECUTABLE})

execute_process(COMMAND ./scripts/verify-installed vpp-* OUTPUT_VARIABLE VPP_INSTALLED)
#string(STRIP "${VPP_INSTALLED}" VPP_INSTALLED)

if ("${VPP_INSTALLED}" MATCHES "vpp-dev")
  message(INFO "\nVPP instalation found:")
  info("Packages" "${VPP_INSTALLED}")
else()
  if (${RELEASE_ID} IN_LIST DebianBasedOS)
    set(REQUIREMENTS "vpp, vpp-dev, libvppinfra, vpp-plugin-core")
  else()
    set(REQUIREMENTS "vpp, vpp-devel, vpp-plugins")
  endif()
  message(FATAL_ERROR "Error VPP is not installed.\nPlease install: ${REQUIREMENTS}.")
endif()

add_subdirectory(java)
add_subdirectory(docs)
# enable Tests
include(FindPythonInterp)
if(PYTHONINTERP_FOUND)
    enable_testing()

    execute_process(COMMAND ${PYTHON_EXECUTABLE} list_tests.py OUTPUT_VARIABLE STR_TESTS
            WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}/tests/
            OUTPUT_STRIP_TRAILING_WHITESPACE
            ERROR_STRIP_TRAILING_WHITESPACE)

    separate_arguments(TEST_LIST UNIX_COMMAND ${STR_TESTS})

    message(INFO "\nJVPP tests:")
       set(TEST_ID 0)
    foreach(JVPPTEST ${TEST_LIST})
        MATH(EXPR TEST_ID "${TEST_ID}+1")
        string(REPLACE "test_jvpp.TestJVpp." "" JVPPTESTNAME ${JVPPTEST})
        camel_case_string(${JVPPTESTNAME} JVPPTESTNAME)
        info("${TEST_ID}." "${JVPPTESTNAME}")
        set(CTEST_OUTPUT_ON_FAILURE=1)
        set(ENV{V} "2")
        add_test(NAME ${JVPPTESTNAME}
                COMMAND ${PYTHON_EXECUTABLE} "-m" "unittest" "-v" ${JVPPTEST}
                WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}/tests/)
    endforeach(JVPPTEST)
else()
    message(WARNING "Python interpreter not found. Tests are disabled.")
endif()
message(INFO \nConfiguration results:)
