/*
 * Copyright (c) 2019 PANTHEON.tech.
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

#include "io_fd_jvpp_stats_JVppClientStatsImpl.h"
#include "jvpp_stats.h"
#include <vpp-api/client/stat_client.h>
#include <vppinfra/vec.h>
#include <vppinfra/format.h>


void set_field(int_stats_t* stats, int index, const char* field_name, int packets, int bytes) {
    if (strcmp(field_name, "/if/rx-error") == 0) {
        stats[index].rx_errors = packets;
    } else if (strcmp(field_name, "/if/tx-error") == 0) {
        stats[index].tx_errors = packets;
    } else if (strcmp(field_name, "/if/tx") == 0) {
        stats[index].tx_bytes = bytes;
    } else if (strcmp(field_name, "/if/tx-multicast") == 0) {
        stats[index].tx_multicast_pkts= packets;
    } else if (strcmp(field_name, "/if/tx-unicast") == 0) {
        stats[index].tx_unicast_pkts= packets;
    } else if (strcmp(field_name, "/if/tx-broadcast") == 0) {
        stats[index].tx_broadcast_pkts= packets;
    } else if (strcmp(field_name, "/if/rx") == 0) {
        stats[index].rx_bytes = bytes;
    } else if (strcmp(field_name, "/if/rx-multicast") == 0) {
        stats[index].rx_multicast_pkts= packets;
    } else if (strcmp(field_name, "/if/rx-unicast") == 0) {
        stats[index].rx_unicast_pkts= packets;
    } else if (strcmp(field_name, "/if/rx-broadcast") == 0) {
        stats[index].rx_broadcast_pkts= packets;
    }
}
stat_segment_data_t* get_statistics_dump() {
    u8 **patterns = 0;
    u32 *dir;
    u8 * stat_segment_name = (u8 *) STAT_SEGMENT_SOCKET_FILE;
    vec_add1(patterns, (u8*)"/if/rx");
    vec_add1(patterns, (u8*)"/if/tx");
    int rv = stat_segment_connect((char*)stat_segment_name);
    if (rv < 0) {
        fprintf(stderr, "Couldn't connect to %s. Check if socket exists/permissions.(ret stat: %d)\n",
                stat_segment_name, rv);
        return NULL;
    }
    dir = stat_segment_ls(patterns);
    return stat_segment_dump(dir);
}

JNIEXPORT jobjectArray JNICALL Java_io_fd_jvpp_stats_JVppClientStatsImpl_interfaceStatisticsDump(JNIEnv *env,
        jclass jclazz) {

    stat_segment_data_t *res;
    int i, j, k, interface_count;

    res = get_statistics_dump();
    if (res == NULL) {
        fprintf (stderr, "Interface Statistics dump failed.\n");
        return NULL;
    }

    if (vec_len (res) > 0) {
        if ((res[0].simple_counter_vec != 0) && (vec_len (res[0].simple_counter_vec) > 0)) {
             interface_count = vec_len (res[0].simple_counter_vec[0]);
        } else if ((res[0].combined_counter_vec != 0) && (vec_len (res[0].combined_counter_vec) > 0)) {
            interface_count = vec_len (res[0].combined_counter_vec[0]);
        }
    }
    int_stats_t ifc_stats[interface_count];
    memset(ifc_stats, 0, interface_count*sizeof(int_stats_t));

    for (i = 0; i < vec_len (res); i++)
    {
        switch (res[i].type)
        {
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

            default:
                ;
        }
    }
    stat_segment_data_free (res);

    jobjectArray retArray;
    jclass ifcStatsClass = (*env)->FindClass(env, "io/fd/jvpp/stats/dto/InterfaceStatistics");
    retArray= (jobjectArray)(*env)->NewObjectArray(env, interface_count,ifcStatsClass, NULL);
    jmethodID constructor = (*env)->GetMethodID(env, ifcStatsClass, "<init>", "(IIIIIIIIIII)V");

    for (int i = 0; i < interface_count; i++) {
        jobject newClientObj =  (*env)->NewObject(env, ifcStatsClass, constructor, i,
        ifc_stats[i].tx_errors, ifc_stats[i].tx_multicast_pkts, ifc_stats[i].tx_unicast_pkts,
        ifc_stats[i].tx_broadcast_pkts, ifc_stats[i].tx_bytes, ifc_stats[i].rx_errors, ifc_stats[i].rx_multicast_pkts,
        ifc_stats[i].rx_unicast_pkts, ifc_stats[i].rx_broadcast_pkts, ifc_stats[i].rx_bytes);
        (*env)->SetObjectArrayElement(env,retArray,i,newClientObj);
    }
    return retArray;
}
