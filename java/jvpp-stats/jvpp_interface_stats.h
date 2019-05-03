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

/**
 * This file contains JNI bindings for jvpp Java API.
 */
#include <jvpp-common/jvpp_common.h>
#include <vpp-api/client/stat_client.h>

// JAVA class reference cache
jclass interfaceStatisticsDumpClass;
jclass interfaceStatisticsClass;
jclass interfaceStatisticsDetailsClass;
jclass callbackExceptionClass;

typedef struct interface_statistics {
    int context;
    int sw_if_index;
    int rx_errors;
    int rx_bytes;
    int rx_unicast_pkts;
    int rx_broadcast_pkts;
    int rx_multicast_pkts;
    int tx_errors;
    int tx_bytes;
    int tx_unicast_pkts;
    int tx_broadcast_pkts;
    int tx_multicast_pkts;

} vl_api_interface_statistics_details_t;

static int cache_class_references(JNIEnv *env) {

    interfaceStatisticsDumpClass = (jclass) (*env)->NewGlobalRef(env, (*env)->FindClass(env,
                                                                                        "io/fd/jvpp/stats/dto/InterfaceStatisticsDump"));
    if ((*env)->ExceptionCheck(env)) {
        (*env)->ExceptionDescribe(env);
        return JNI_ERR;
    }
    interfaceStatisticsDetailsClass = (jclass) (*env)->NewGlobalRef(env, (*env)->FindClass(env,
                                                                                           "io/fd/jvpp/stats/dto/InterfaceStatisticsDetails"));
    if ((*env)->ExceptionCheck(env)) {
        (*env)->ExceptionDescribe(env);
        return JNI_ERR;
    }
    interfaceStatisticsClass = (jclass) (*env)->NewGlobalRef(env, (*env)->FindClass(env,
                                                                                    "io/fd/jvpp/stats/dto/InterfaceStatistics"));
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

    if (interfaceStatisticsDumpClass) {
        (*env)->DeleteGlobalRef(env, interfaceStatisticsDumpClass);
    }
    if (interfaceStatisticsDetailsClass) {
        (*env)->DeleteGlobalRef(env, interfaceStatisticsDetailsClass);
    }
    if (interfaceStatisticsClass) {
        (*env)->DeleteGlobalRef(env, interfaceStatisticsClass);
    }
    if (callbackExceptionClass) {
        (*env)->DeleteGlobalRef(env, callbackExceptionClass);
    }
}

/**
 * Handler for interface_statistics_details message.
 */
static void
interface_statistics_details_handler(JNIEnv *env, vl_api_interface_statistics_details_t *ifc_stats, int ifc_count) {
    stats_main_t *plugin_main = &stats_main;
    jthrowable exc;
    if (CLIB_DEBUG > 1)
        clib_warning ("Received interface_statistics_details event message");

    jmethodID constructor = (*env)->GetMethodID(env, interfaceStatisticsDetailsClass, "<init>", "(II)V");

    // User does not have to provide callbacks for all VPP messages.
    // We are ignoring messages that are not supported by user.
    (*env)->ExceptionClear(env); // just in case exception occurred in different place and was not properly cleared
    jmethodID callbackMethod = (*env)->GetMethodID(env, plugin_main->callbackClass, "onInterfaceStatisticsDetails",
                                                   "(Lio/fd/jvpp/stats/dto/InterfaceStatisticsDetails;)V");
    exc = (*env)->ExceptionOccurred(env);
    if (exc) {
        clib_warning(
                "Unable to extract onInterfaceStatisticsDetails method reference from stats plugin's callbackClass. Ignoring message.\n");
        (*env)->ExceptionDescribe(env);
        (*env)->ExceptionClear(env);
        return;
    }
    jobject dto = (*env)->NewObject(env, interfaceStatisticsDetailsClass, constructor, ifc_count, ifc_stats->context);
    jfieldID interfaceStatisticsId = (*env)->GetFieldID(env, interfaceStatisticsDetailsClass, "interfaceStatistics",
                                                        "[Lio/fd/jvpp/stats/dto/InterfaceStatistics;");
    jobject stats_array = (*env)->GetObjectField(env, dto, interfaceStatisticsId);

    jmethodID ifc_stats_constructor = (*env)->GetMethodID(env, interfaceStatisticsClass, "<init>", "(IIIIIIIIIII)V");
    for (int i = 0; i < ifc_count; i++) {

        jobject element = (*env)->NewObject(env, interfaceStatisticsClass, ifc_stats_constructor,
                                            ifc_stats[i].sw_if_index,
                                            ifc_stats[i].tx_errors,
                                            ifc_stats[i].tx_multicast_pkts,
                                            ifc_stats[i].tx_unicast_pkts,
                                            ifc_stats[i].tx_broadcast_pkts,
                                            ifc_stats[i].tx_bytes,
                                            ifc_stats[i].rx_errors,
                                            ifc_stats[i].rx_multicast_pkts,
                                            ifc_stats[i].rx_unicast_pkts,
                                            ifc_stats[i].rx_broadcast_pkts,
                                            ifc_stats[i].rx_bytes);

        (*env)->SetObjectArrayElement(env, stats_array, i, element);
        if ((*env)->ExceptionOccurred(env)) {
            break;
        }
        (*env)->DeleteLocalRef(env, element);
    }
    (*env)->CallVoidMethod(env, plugin_main->callbackObject, callbackMethod, dto);
    if ((*env)->ExceptionOccurred(env)) {
        clib_warning("Unable to call callback for stats plugin's callbackClass.\n");
        (*env)->ExceptionDescribe(env);
        (*env)->ExceptionClear(env);
        return;
    }
    (*env)->DeleteLocalRef(env, stats_array);
    (*env)->DeleteLocalRef(env, dto);
}

static void
set_field(vl_api_interface_statistics_details_t *stats, int index, const char *field_name, int packets, int bytes) {
    if (strcmp(field_name, "/if/rx-error") == 0) {
        stats[index].rx_errors = packets;
    } else if (strcmp(field_name, "/if/tx-error") == 0) {
        stats[index].tx_errors = packets;
    } else if (strcmp(field_name, "/if/tx") == 0) {
        stats[index].tx_bytes = bytes;
    } else if (strcmp(field_name, "/if/tx-multicast") == 0) {
        stats[index].tx_multicast_pkts = packets;
    } else if (strcmp(field_name, "/if/tx-unicast") == 0) {
        stats[index].tx_unicast_pkts = packets;
    } else if (strcmp(field_name, "/if/tx-broadcast") == 0) {
        stats[index].tx_broadcast_pkts = packets;
    } else if (strcmp(field_name, "/if/rx") == 0) {
        stats[index].rx_bytes = bytes;
    } else if (strcmp(field_name, "/if/rx-multicast") == 0) {
        stats[index].rx_multicast_pkts = packets;
    } else if (strcmp(field_name, "/if/rx-unicast") == 0) {
        stats[index].rx_unicast_pkts = packets;
    } else if (strcmp(field_name, "/if/rx-broadcast") == 0) {
        stats[index].rx_broadcast_pkts = packets;
    }
    stats[index].sw_if_index = index;
}

static stat_segment_data_t *get_ifc_statistics_dump() {
    u8 **patterns = 0;
    u32 *dir;
    vec_add1(patterns, (u8 *) "/if/rx");
    vec_add1(patterns, (u8 *) "/if/tx");
    dir = stat_segment_ls(patterns);
    return stat_segment_dump(dir);
}

static jint getInterfaceStatisticsDump(JNIEnv *env) {
    stat_segment_data_t *res;
    int i, j, k, interface_count = 0;
    u32 my_context_id = vppjni_get_context_id(&jvpp_main);
    res = get_ifc_statistics_dump();
    if (res == NULL) {
        clib_warning("Interface Statistics dump failed.\n");
        return -1;
    }

    if (vec_len (res) > 0) {
        if ((res[0].simple_counter_vec != 0) && (vec_len (res[0].simple_counter_vec) > 0)) {
            interface_count = vec_len (res[0].simple_counter_vec[0]);
        } else if ((res[0].combined_counter_vec != 0) && (vec_len (res[0].combined_counter_vec) > 0)) {
            interface_count = vec_len (res[0].combined_counter_vec[0]);
        }
    }
    vl_api_interface_statistics_details_t ifc_stats[interface_count];
    memset(ifc_stats, 0, interface_count * sizeof(vl_api_interface_statistics_details_t));
    ifc_stats->context = my_context_id;

    for (i = 0; i < vec_len (res); i++) {
        switch (res[i].type) {
            case STAT_DIR_TYPE_COUNTER_VECTOR_SIMPLE:
                if (res[i].simple_counter_vec == 0)
                    continue;
                for (k = 0; k < vec_len (res[i].simple_counter_vec); k++)
                    for (j = 0; j < vec_len (res[i].simple_counter_vec[k]); j++) {
                        set_field(ifc_stats, j, res[i].name, res[i].simple_counter_vec[k][j], 0);
                    }
                break;

            case STAT_DIR_TYPE_COUNTER_VECTOR_COMBINED:
                if (res[i].combined_counter_vec == 0)
                    continue;
                for (k = 0; k < vec_len (res[i].combined_counter_vec); k++)
                    for (j = 0; j < vec_len (res[i].combined_counter_vec[k]); j++) {
                        set_field(ifc_stats, j, res[i].name, res[i].combined_counter_vec[k][j].packets,
                                  res[i].combined_counter_vec[k][j].bytes);
                    }
                break;

            default:;
        }
    }
    stat_segment_data_free(res);
    interface_statistics_details_handler(env, ifc_stats, interface_count);
    return ifc_stats->context;
}