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

find_path(VNET_INCLUDE_DIR NAMES vnet/api_errno.h)
find_library(VPPINFRA_DIR NAMES vppinfra REQUIRED)
find_library(VLIBMEMORYCLIENT_DIR NAMES vlibmemoryclient REQUIRED)
find_library(SVM_DIR NAMES svm REQUIRED)

include_directories(${VNET_INCLUDE_DIR}
        ${VNET_INCLUDE_DIR}/vpp_plugins
        ${VNET_INCLUDE_DIR}/vpp_api
        ${Java_INCLUDE_DIRS}
        ${CMAKE_CURRENT_SOURCE_DIR}
        ${CMAKE_BINARY_DIR/../vpp/plugins})

add_compile_options(-Wall)
############# Common package ##################
add_library(jvpp_common SHARED jvpp-common/jvpp_common.c)
set_target_properties(jvpp_common PROPERTIES SOVERSION ${JAPI_LIB_VERSION})
target_link_libraries(jvpp_common ${VPPINFRA_DIR})
install(TARGETS jvpp_common DESTINATION lib COMPONENT libjvpp_common)
install(FILES jvpp-common/jvpp_common.h DESTINATION include/japi/)

set(JVPP_LIBS jvpp_common ${VPPINFRA_DIR} ${VLIBMEMORYCLIENT_DIR} ${SVM_DIR}
        Threads::Threads m rt)

############# Registry package ##################
set(PACKAGE_DIR_JVPP_REGISTRY io/fd/jvpp)
unset(files)
FILE(GLOB files RELATIVE
        ${CMAKE_CURRENT_SOURCE_DIR}
        ${CMAKE_CURRENT_SOURCE_DIR}/jvpp-registry/${PACKAGE_DIR_JVPP_REGISTRY}/*.java
        ${CMAKE_CURRENT_SOURCE_DIR}/jvpp-registry/${PACKAGE_DIR_JVPP_REGISTRY}/*/*.java
        )

add_custom_target(jvpp-registry-classes)
add_custom_command(TARGET jvpp-registry-classes
        PRE_BUILD
        COMMAND mkdir -p jvpp-registry/target
        COMMAND mkdir -p ${CMAKE_CURRENT_BINARY_DIR}/jvpp-registry
        COMMAND ${Java_JAVAC_EXECUTABLE}
        ARGS -source 1.8 -target 1.8 -d ${CMAKE_CURRENT_SOURCE_DIR}/jvpp-registry/target -h jvpp-registry ${files}
        WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}
        )

add_library(jvpp_registry SHARED jvpp-registry/jvpp_registry.c)
target_link_libraries(jvpp_registry ${JVPP_LIBS})
include_directories(jvpp-registry)
add_dependencies(jvpp_registry jvpp_common jvpp-registry-classes)

add_custom_target(jvpp-registry)
add_dependencies(jvpp-registry jvpp_registry)
add_custom_command(TARGET jvpp-registry
        PRE_BUILD
        COMMAND cp ${CMAKE_BINARY_DIR}/build-root/lib/libjvpp_registry.so jvpp-registry/target
        COMMAND ${Java_JAR_EXECUTABLE} ARGS cf
        ${CMAKE_CURRENT_BINARY_DIR}/jvpp-registry-${JAPI_LIB_VERSION}.jar
        -C jvpp-registry/target .
        COMMAND rm ARGS -rf jvpp-registry/target
        jvpp-registry/io_fd_jvpp_*.h
        WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}
        COMMENT "JAR_GEN registry"
        )
install(
        FILES ${CMAKE_CURRENT_BINARY_DIR}/jvpp-registry-${JAPI_LIB_VERSION}.jar
        DESTINATION share/java
)

############## Functions #########################
function(japigen name)
    if (NOT VPP_JAVA_APIGEN)
        set(VPP_JAVA_APIGEN ${CMAKE_CURRENT_SOURCE_DIR}/jvpp/gen/jvpp_gen.py)
    endif ()
    add_custom_target(japigen-${name} DEPENDS jvpp-registry)
    add_custom_command(TARGET japigen-${name}
            POST_BUILD
            COMMAND mkdir -p jvpp-${name}/target
            COMMAND mkdir -p ${CMAKE_CURRENT_BINARY_DIR}/jvpp-${name}
            COMMAND ${VPP_JAVA_APIGEN}
            ARGS --plugin_name ${name} --root_dir jvpp-${name} -i ${ARGN}
            COMMAND find jvpp-${name} -name \*.java > jvpp-${name}/jvpp-${name}.files
    COMMAND ${Java_JAVAC_EXECUTABLE}
    ARGS -source 1.8 -target 1.8 -cp ${CMAKE_CURRENT_BINARY_DIR}/jvpp-registry-${JAPI_LIB_VERSION}.jar -d
    ${CMAKE_CURRENT_SOURCE_DIR}/jvpp-${name}/target -h jvpp-${name}
    @jvpp-${name}/jvpp-${name}.files
    WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}
    COMMENT "JAVA_API_GEN ${name}"
    )
endfunction()

function(jargen name)
    add_custom_command(TARGET jvpp_${name}
            POST_BUILD
            COMMAND cp ${CMAKE_BINARY_DIR}/build-root/lib/libjvpp_${name}.so jvpp-${name}/target
            COMMAND ${Java_JAR_EXECUTABLE} ARGS cf
            ${CMAKE_CURRENT_BINARY_DIR}/jvpp-${name}-${JAPI_LIB_VERSION}.jar
            -C jvpp-${name}/target .
            COMMAND rm ARGS -rf jvpp-${name}/target jvpp-${name}/jvpp-${name}.files
            jvpp-${name}/jvpp_${name}_gen.h jvpp-${name}/io_fd_jvpp_*.h
            jvpp-registry/io_fd_jvpp_*.h
            WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}
            COMMENT "JAR_GEN ${name}"
            )
    install(
            FILES ${CMAKE_CURRENT_BINARY_DIR}/jvpp-${name}-${JAPI_LIB_VERSION}.jar
            DESTINATION share/java
    )
endfunction()

function(java_api_binding name src_file)
    japigen(${name} ${ARGN})
    add_library(jvpp_${name} SHARED jvpp-${name}/jvpp_${src_file}.c)
    target_link_libraries(jvpp_${name} ${JVPP_LIBS})
    include_directories(jvpp-${name})
    add_dependencies(jvpp_${name} jvpp_common jvpp_registry japigen-${name})
    jargen(${name})
endfunction()

############ Core Package #######################
unset(files)
unset(corefiles)

if (${RELEASE_ID} IN_LIST DebianBasedOS)
    execute_process(COMMAND dpkg-query -L vpp
            COMMAND grep -v memclnt
            COMMAND grep api.json OUTPUT_VARIABLE corefiles)
    STRING(REGEX REPLACE "\n" ";" corefiles "${corefiles}")
elseif (${RELEASE_ID} IN_LIST RHBasedOS)
    # Exclude vpp plugin api files. VPP package should not contain vpp-plugins api files and vice-versa. Temporary fixing
    # by excluding them manually.
    execute_process(COMMAND rpm -ql vpp
            COMMAND grep -v memclnt
            COMMAND grep -v abf
            COMMAND grep -v acl
            COMMAND grep -v avf
            COMMAND grep -v cdp
            COMMAND grep -v ct6
            COMMAND grep -v dpdk
            COMMAND grep -v flowprobe
            COMMAND grep -v gbp
            COMMAND grep -v gtpu
            COMMAND grep -v igmp
            COMMAND grep -v ikev2
            COMMAND grep -v ioam_cache
            COMMAND grep -v ioam_export
            COMMAND grep -v ioam_vxlan_gpe
            COMMAND grep -v l2e
            COMMAND grep -v lacp
            COMMAND grep -v lb
            COMMAND grep -v mactime
            COMMAND grep -v map
            COMMAND grep -v memif
            COMMAND grep -v nat
            COMMAND grep -v nsh
            COMMAND grep -v nsim
            COMMAND grep -v pot
            COMMAND grep -v pppoe
            COMMAND grep -v stn
            COMMAND grep -v svs
            COMMAND grep -v trace
            COMMAND grep -v udp_ping
            COMMAND grep -v vmxnet3
            COMMAND grep -v vxlan_gpe_ioam_export
            COMMAND grep api.json OUTPUT_VARIABLE corefiles)
    STRING(REGEX REPLACE "\n" ";" corefiles "${corefiles}")
else ()
    message(FATAL_ERROR "Unsupported OS, CMake will now exit.")
endif ()

FILE(GLOB_RECURSE files RELATIVE
        ${CMAKE_CURRENT_SOURCE_DIR}
        ${corefiles}
        )

# message("COREFILES: ${corefiles}")
java_api_binding(core core ${files})

############ Plugin Packages #######################
unset(files)
unset(pluginfiles)
unset(ACL_JSON_FILE)
unset(NAT_JSON_FILE)
unset(NSH_JSON_FILE)
unset(GTPU_JSON_FILE)
unset(PPPOE_JSON_FILE)
unset(IOAM_TRACE_JSON_FILE)
unset(IOAM_POT_JSON_FILE)
unset(IOAM_EXPORT_JSON_FILE)
unset(IKEV2_JSON_FILE)

if (${RELEASE_ID} IN_LIST DebianBasedOS)
    execute_process(COMMAND dpkg-query -L vpp-plugin-core
            COMMAND grep api.json OUTPUT_VARIABLE pluginfiles)
    STRING(REGEX REPLACE "\n" ";" pluginfiles "${pluginfiles}")
elseif (${RELEASE_ID} IN_LIST RHBasedOS)
    execute_process(COMMAND rpm -ql vpp-plugins
            COMMAND grep api.json OUTPUT_VARIABLE pluginfiles)
    STRING(REGEX REPLACE "\n" ";" pluginfiles "${pluginfiles}")
endif ()

# message("PLUGINFILES: ${pluginfiles}")
FILE(GLOB_RECURSE files RELATIVE
        ${CMAKE_CURRENT_SOURCE_DIR}
        ${pluginfiles}
        )

foreach (FILE ${pluginfiles})
    if ("${FILE}" MATCHES "/acl.api.json")
        set(ACL_JSON_FILE ${FILE})
    endif ()
    if ("${FILE}" MATCHES "/nat.api.json")
        set(NAT_JSON_FILE ${FILE})
    endif ()
    if ("${FILE}" MATCHES "/nsh.api.json")
        set(NSH_JSON_FILE ${FILE})
    endif ()
    if ("${FILE}" MATCHES "/gtpu.api.json")
        set(GTPU_JSON_FILE ${FILE})
    endif ()
    if ("${FILE}" MATCHES "/pppoe.api.json")
        set(PPPOE_JSON_FILE ${FILE})
    endif ()
    if ("${FILE}" MATCHES "/trace.api.json")
        set(IOAM_TRACE_JSON_FILE ${FILE})
    endif ()
    if ("${FILE}" MATCHES "/pot.api.json")
        set(IOAM_POT_JSON_FILE ${FILE})
    endif ()
    if ("${FILE}" MATCHES "/ioam_export.api.json")
        set(IOAM_EXPORT_JSON_FILE ${FILE})
    endif ()
    if ("${FILE}" MATCHES "/ikev2.api.json")
        set(IKEV2_JSON_FILE ${FILE})
    endif ()
endforeach ()

if (ACL_JSON_FILE)
    java_api_binding(acl acl ${ACL_JSON_FILE})
endif ()

if (NAT_JSON_FILE)
    java_api_binding(nat nat ${NAT_JSON_FILE})
endif ()

if (NSH_JSON_FILE)
    java_api_binding(nsh nsh ${NSH_JSON_FILE})
endif ()

if (GTPU_JSON_FILE)
    java_api_binding(gtpu gtpu ${GTPU_JSON_FILE})
endif ()

if (PPPOE_JSON_FILE)
    java_api_binding(pppoe pppoe ${PPPOE_JSON_FILE})
endif ()

if (IOAM_TRACE_JSON_FILE)
    java_api_binding(ioamtrace ioam_trace ${IOAM_TRACE_JSON_FILE})
endif ()

if (IOAM_POT_JSON_FILE)
    java_api_binding(ioampot ioam_pot ${IOAM_POT_JSON_FILE})
endif ()

if (IOAM_EXPORT_JSON_FILE)
    java_api_binding(ioamexport ioam_export ${IOAM_EXPORT_JSON_FILE})
endif ()

if (IKEV2_JSON_FILE)
    java_api_binding(ikev2 ikev2 ${IKEV2_JSON_FILE})
endif ()

# Package Generator  #######################################################

unset(vpp_version)
set(COLUMNS 200)

set(CPACK_PACKAGE_DESCRIPTION "VPP-Java-API-bindings
  This package contains VPP java api bindings.")
set(CPACK_PACKAGE_NAME "vpp-api-java")
set(CPACK_PACKAGE_VERSION "${JVPP_VERSION}")
set(CPACK_PACKAGE_SECTION net)
set(CPACK_PACKAGE_VENDOR "Cisco")
set(CPACK_PACKAGE_CONTACT "jvpp-dev@lists.fd.io")
set(CPACK_OUTPUT_FILE_PREFIX build-root/packages)
set(CPACK_PACKAGE_FILE_NAME "vpp-api-java_${JVPP_VERSION}")
if (${RELEASE_ID} IN_LIST DebianBasedOS)
    set(CPACK_GENERATOR DEB)
    execute_process(COMMAND ./version deb-vpp-version OUTPUT_VARIABLE vpp_version)
    string(STRIP "${vpp_version}" vpp_version)
    set(CPACK_DEBIAN_PACKAGE_NAME "${CPACK_PACKAGE_NAME}")
    set(CPACK_DEBIAN_PACKAGE_VERSION "${CPACK_PACKAGE_VERSION}")
    set(CPACK_DEBIAN_PACKAGE_DESCRIPTION "${CPACK_PACKAGE_DESCRIPTION}")
    set(CPACK_DEBIAN_PACKAGE_SECTION "${CPACK_PACKAGE_SECTION}")
    set(CPACK_DEBIAN_PACKAGE_FILE_NAME ${CPACK_PACKAGE_FILE_NAME})
    set(CPACK_DEBIAN_PACKAGE_PRIORITY extra)
    set(CPACK_DEBIAN_PACKAGE_DEPENDS "vpp (>= ${vpp_version}), vpp-plugin-core (>= ${vpp_version})")

    # Print Debian package summary
    message("\nDebian package:")
    message("    Name: ${CPACK_DEBIAN_PACKAGE_NAME}")
    message("    Version: ${CPACK_DEBIAN_PACKAGE_VERSION}")
    message("    Description: ${CPACK_DEBIAN_PACKAGE_DESCRIPTION}")
    message("    File name: ${CPACK_DEBIAN_PACKAGE_FILE_NAME}")
    message("    Depends on: ${CPACK_DEBIAN_PACKAGE_DEPENDS}\n")

elseif (${RELEASE_ID} IN_LIST RHBasedOS)
    set(CPACK_GENERATOR RPM)
    execute_process(COMMAND ./version rpm-vpp-version OUTPUT_VARIABLE vpp_version)
    string(STRIP "${vpp_version}" vpp_version)
    # Excluding /usr/share/java top level directory due to conflict with javapackages-tools (openjdk dependency)
    set(CPACK_RPM_EXCLUDE_FROM_AUTO_FILELIST_ADDITION "/usr/share/java")
    set(CPACK_RPM_PACKAGE_NAME "${CPACK_PACKAGE_NAME}")
    set(CPACK_RPM_PACKAGE_DESCRIPTION "${CPACK_PACKAGE_DESCRIPTION}")
    set(CPACK_RPM_PACKAGE_SECTION "${CPACK_PACKAGE_SECTION}")
    execute_process(COMMAND uname -m OUTPUT_VARIABLE CPACK_RPM_PACKAGE_ARCHITECTURE)
    string(STRIP "${CPACK_RPM_PACKAGE_ARCHITECTURE}" CPACK_RPM_PACKAGE_ARCHITECTURE)
    execute_process(COMMAND ./version rpm-version OUTPUT_VARIABLE CPACK_RPM_PACKAGE_VERSION)
    string(STRIP "${CPACK_RPM_PACKAGE_VERSION}" CPACK_RPM_PACKAGE_VERSION)
    execute_process(COMMAND ./version rpm-release OUTPUT_VARIABLE CPACK_RPM_PACKAGE_RELEASE)
    string(STRIP "${CPACK_RPM_PACKAGE_RELEASE}" CPACK_RPM_PACKAGE_RELEASE)
    execute_process(COMMAND ./version os-version-id OUTPUT_VARIABLE OS_VERSION_ID)
    string(STRIP "${OS_VERSION_ID}" OS_VERSION_ID)

    # according to naming conventions file name should consist of:
    # name-version-release.architecture.rpm (e.g. vpp-api-java-19.04-SNAPSHOT~9_ge3665c6~b7.x86_64.rpm)
    if (${RELEASE_ID} MATCHES "CentOS" AND ${OS_VERSION_ID} MATCHES "7")
        set(CPACK_PACKAGE_FILE_NAME "${CPACK_RPM_PACKAGE_NAME}-${CPACK_RPM_PACKAGE_VERSION}-${CPACK_RPM_PACKAGE_RELEASE}.${CPACK_RPM_PACKAGE_ARCHITECTURE}")
    endif ()
    set(CPACK_RPM_PACKAGE_PRIORITY extra)
    set(CPACK_RPM_PACKAGE_DEPENDS "vpp (>= ${vpp_version}), vpp-plugins (>= ${vpp_version})")

    # Print CentOS package summary
    message("\nRPM package:")
    message("    Name: ${CPACK_RPM_PACKAGE_NAME}")
    message("    Version: ${CPACK_RPM_PACKAGE_VERSION}")
    message("    Release: ${CPACK_RPM_PACKAGE_RELEASE}")
    message("    Description: ${CPACK_RPM_PACKAGE_DESCRIPTION}")
    message("    File name: ${CPACK_PACKAGE_FILE_NAME}")
    message("    Depends on: ${CPACK_RPM_PACKAGE_DEPENDS}\n")
endif ()
if (PackZip)
    list(APPEND CPACK_GENERATOR ZIP)
endif ()
if (PackTgz)
    list(APPEND CPACK_GENERATOR TGZ)
endif ()
include(CPack)
