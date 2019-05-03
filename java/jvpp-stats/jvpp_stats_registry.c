/*
 * Copyright (c) 2019 Cisco and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include <vnet/vnet.h>

#include <jni.h>
#include "io_fd_jvpp_stats_VppStatsJNIConnection.h"
#include "io_fd_jvpp_stats_JVppStatsRegistryImpl.h"
#include <jvpp-common/jvpp_common.h>

/*
 * The Java runtime isn't compile w/ -fstack-protector,
 * so we have to supply missing external references for the
 * regular vpp libraries.
 */
void __stack_chk_guard(void) __attribute__((weak));
void __stack_chk_guard(void) {
}

typedef struct {
    jobject registryObject;
    jclass registryClass;
    jclass callbackExceptionClass;

    /* Connected indication */
    volatile u8 is_connected;
    u32 vpe_pid;
} jvpp_stats_registry_main_t;

jvpp_stats_registry_main_t jvpp_stats_registry_main __attribute__((aligned (64)));

// JAVA class reference cache
jclass connectionInfoClass;
jclass callbackExceptionClass;

JNIEXPORT jint JNICALL Java_io_fd_jvpp_stats_JVppStatsRegistryImpl_controlPing0(JNIEnv *env, jobject registryObject) {
    u32 my_context_id = vppjni_get_context_id(&jvpp_main);
    jvpp_stats_registry_main_t *rm = &jvpp_stats_registry_main;

    if (rm->registryObject == 0) {
        rm->registryObject = (*env)->NewGlobalRef(env, registryObject);
    }
    if (rm->registryClass == 0) {
        rm->registryClass = (jclass) (*env)->NewGlobalRef(env, (*env)->GetObjectClass(env, registryObject));
    }

    rm->is_connected = 1;
    return my_context_id;
}

JNIEXPORT jobject JNICALL
Java_io_fd_jvpp_stats_VppStatsJNIConnection_statsConnect(JNIEnv *env, jclass obj, jstring clientName) {
    // int rv;
    const char *client_name;
    jvpp_main_t * jm = &jvpp_main;
    jobject  con_info;
    jthrowable exc;
    jvpp_stats_registry_main_t * rm = &jvpp_stats_registry_main;

    jmethodID connectionInfoConstructor = (*env)->GetMethodID(env, connectionInfoClass, "<init>", "(JIII)V");
    exc = (*env)->ExceptionOccurred(env);
    if (exc) {
        clib_warning(
                "Unable to extract connectionInfoClass constructor. Ignoring message.\n");
        (*env)->ExceptionDescribe(env);
        (*env)->ExceptionClear(env);

        con_info = (*env)->NewObject(env, connectionInfoClass, connectionInfoConstructor, 0, 0,
                                     VNET_API_ERROR_INVALID_REGISTRATION, 0);
        if ((*env)->ExceptionCheck(env)) {
            (*env)->ExceptionDescribe(env);
            (*env)->ExceptionClear(env);
            clib_warning("StatsConnectionInfo: Invalid Registration!\n");
        }
        return con_info;
    }

    if (rm->is_connected) {
        con_info = (*env)->NewObject(env, connectionInfoClass, connectionInfoConstructor, 0, 0,
                                     VNET_API_ERROR_ALREADY_CONNECTED, 0);
        if ((*env)->ExceptionCheck(env)) {
            (*env)->ExceptionDescribe(env);
            (*env)->ExceptionClear(env);
            clib_warning("StatsConnectionInfo: Already connected.\n");
        }
        return con_info;
    }

    client_name = (*env)->GetStringUTFChars(env, clientName, 0);

    if (!client_name) {
        con_info = (*env)->NewObject(env, connectionInfoClass, connectionInfoConstructor, 0, 0,
                                     VNET_API_ERROR_INVALID_VALUE, 0);
        if ((*env)->ExceptionCheck(env)) {
            (*env)->ExceptionDescribe(env);
            (*env)->ExceptionClear(env);
            clib_warning("StatsConnectionInfo: Invalid Client name!\n");
        }
        return con_info;
    }

    (*env)->ReleaseStringUTFChars(env, clientName, client_name);

    con_info = (*env)->NewObject(env, connectionInfoClass, connectionInfoConstructor,
                                 (jlong) pointer_to_uword(jm->vl_input_queue), (jint) jm->my_client_index, (jint) 0,
                                 (jint) rm->vpe_pid);

    if ((*env)->ExceptionCheck(env)) {
        (*env)->ExceptionDescribe(env);
        (*env)->ExceptionClear(env);
        clib_warning("StatsConnectionInfo: Unspecified Error! Exiting.\n");
        return NULL;
    }

    return con_info;
}


JNIEXPORT void JNICALL Java_io_fd_jvpp_stats_VppStatsJNIConnection_statsDisconnect(
        JNIEnv *env, jclass clazz) {
    jvpp_stats_registry_main_t * rm = &jvpp_stats_registry_main;
    rm->is_connected = 0; // TODO make thread safe
    // cleanup:
    if (rm->registryObject) {
        (*env)->DeleteGlobalRef(env, rm->registryObject);
        rm->registryObject = 0;
    }
    if (rm->registryClass) {
        (*env)->DeleteGlobalRef(env, rm->registryClass);
        rm->registryClass = 0;
    }
}

static int cache_class_references(JNIEnv *env) {
    connectionInfoClass =
            (jclass) (*env)->NewGlobalRef(env, (*env)->FindClass(env,
                                                                 "io/fd/jvpp/stats/VppStatsJNIConnection$StatsConnectionInfo"));
    if ((*env)->ExceptionCheck(env)) {
        (*env)->ExceptionDescribe(env);
        return JNI_ERR;
    }

    callbackExceptionClass = (jclass) (*env)->NewGlobalRef(env,
                                                           (*env)->FindClass(env, "io/fd/jvpp/VppCallbackException"));
    if ((*env)->ExceptionCheck(env)) {
        (*env)->ExceptionDescribe(env);
        return JNI_ERR;
    }
    return 0;
}

static void delete_class_references(JNIEnv *env) {

    if (connectionInfoClass) {
        (*env)->DeleteGlobalRef(env, connectionInfoClass);
    }
    if (callbackExceptionClass) {
        (*env)->DeleteGlobalRef(env, callbackExceptionClass);
    }
}

jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    jvpp_main_t * jm = &jvpp_main;
    jvpp_stats_registry_main_t * rm = &jvpp_stats_registry_main;
    JNIEnv* env;

    if ((*vm)->GetEnv(vm, (void**) &env, JNI_VERSION_1_8) != JNI_OK) {
        return JNI_EVERSION;
    }

    if (cache_class_references(env) != 0) {
        clib_warning ("Failed to cache class references\n");
        return JNI_ERR;
    }

    rm->callbackExceptionClass = connectionInfoClass;
    jm->jvm = vm;

    if (cache_class_references(env) != 0) {
        clib_warning ("Failed to cache class references\n");
        return JNI_ERR;
    }

    return JNI_VERSION_1_8;
}

void JNI_OnUnload(JavaVM *vm, void *reserved) {
    jvpp_main_t * jm = &jvpp_main;
    jvpp_stats_registry_main_t * rm = &jvpp_stats_registry_main;

    JNIEnv* env;
    if ((*vm)->GetEnv(vm, (void**) &env, JNI_VERSION_1_8) != JNI_OK) {
        return;
    }

    delete_class_references(env);
    rm->callbackExceptionClass = NULL;

    jm->jenv = NULL;
    jm->jvm = NULL;
}
