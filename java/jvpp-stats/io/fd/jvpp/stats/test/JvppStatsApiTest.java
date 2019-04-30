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

package io.fd.jvpp.stats.test;

import io.fd.jvpp.stats.JVppClientStatsImpl;
import io.fd.jvpp.stats.dto.InterfaceStatistics;

class JvppStatsApiTest {

    public static void main(String[] args) {

        JVppClientStatsImpl myImpl = new JVppClientStatsImpl();
        InterfaceStatistics[] statistics = myImpl.interfaceStatisticsDump();

        if (statistics == null) {
            System.out.println("in java - dump was null");
            return;
        }

        if (statistics.length == 0) {
            System.out.println("in java - dump was empty");
            return;
        }

        for (int i = 0; i < statistics.length; i++) {
            System.out.println(statistics[i].toString());
        }
    }
}
