/*
 * Copyright (c) 2019 PANTHEON.tech., Cisco and/or its affiliates.
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

#include "io_fd_jvpp_stats_JVppStatsImpl.h"
#include "jvpp_stats.h"
#include "jvpp_interface_stats.h"
#include <vppinfra/vec.h>
#include <vppinfra/format.h>
#include <vnet/api_errno.h>
#include <vlibapi/api.h>
#include <vlibmemory/api.h>
#include <jni.h>
#include <vnet/vnet.h>
#include <jvpp-common/jvpp_common.h>
#include <vpp/api/vpe_msg_enum.h>

#define vl_typedefs             /* define message structures */

#include <vpp/api/vpe_all_api_h.h>

#undef vl_typedefs

JNIEXPORT void JNICALL Java_io_fd_jvpp_stats_JVppStatsImpl_init0
        (JNIEnv *env, jclass clazz, jobject callback, jlong queue_address, jint my_client_index) {
    stats_main_t *plugin_main = &stats_main;
    plugin_main->my_client_index = my_client_index;
    plugin_main->vl_input_queue = uword_to_pointer (queue_address, svm_queue_t *);

    plugin_main->callbackObject = (*env)->NewGlobalRef(env, callback);
    plugin_main->callbackClass = (jclass) (*env)->NewGlobalRef(env, (*env)->GetObjectClass(env, callback));

    int rv = stat_segment_connect(STAT_SEGMENT_SOCKET_FILE);
    if (rv < 0) {
        clib_warning("Couldn't connect to %s. Check if socket exists/permissions.(ret stat: %d)\n",
                     STAT_SEGMENT_SOCKET_FILE, rv);
        return;
    }
}

JNIEXPORT void JNICALL Java_io_fd_jvpp_stats_JVppStatsImpl_close0
        (JNIEnv *env, jclass clazz) {
    stats_main_t *plugin_main = &stats_main;
    stat_segment_disconnect();
    // cleanup:
    (*env)->DeleteGlobalRef(env, plugin_main->callbackClass);
    (*env)->DeleteGlobalRef(env, plugin_main->callbackObject);

    plugin_main->callbackClass = NULL;
    plugin_main->callbackObject = NULL;
}

JNIEXPORT jint JNICALL Java_io_fd_jvpp_stats_JVppStatsImpl_interfaceStatisticsDump0(JNIEnv *env, jclass jclazz) {
    return getInterfaceStatisticsDump(env);
}

JNIEXPORT jint JNICALL Java_io_fd_jvpp_stats_JVppStatsImpl_interfaceNamesDump0(JNIEnv *env, jclass jclazz) {
    return getInterfaceNamesDump(env);
}

/* Attach thread to JVM and cache class references when initiating JVPP Stats */
jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv *env;
    if ((*vm)->GetEnv(vm, (void **) &env, JNI_VERSION_1_8) != JNI_OK) {
        return JNI_EVERSION;
    }

    if (cache_class_references(env) != 0) {
        clib_warning ("Failed to cache class references\n");
        return JNI_ERR;
    }

    return JNI_VERSION_1_8;
}

/* Clean up cached references when disposing JVPP Stats */
void JNI_OnUnload(JavaVM *vm, void *reserved) {
    JNIEnv *env;
    if ((*vm)->GetEnv(vm, (void **) &env, JNI_VERSION_1_8) != JNI_OK) {
        return;
    }
    delete_class_references(env);
}