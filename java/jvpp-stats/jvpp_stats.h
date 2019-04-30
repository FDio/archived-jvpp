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

#ifndef PROJECT_JVPP_STATS_H
#define PROJECT_JVPP_STATS_H

#endif //PROJECT_JVPP_STATS_H


typedef struct interface_statistics{
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
} int_stats_t;

int_stats_t interface_statistics __attribute__((aligned (64)));
