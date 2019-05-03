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

package io.fd.jvpp.stats.test;


import io.fd.jvpp.JVppRegistry;
import io.fd.jvpp.stats.JVppStatsImpl;
import io.fd.jvpp.stats.JVppStatsRegistryImpl;
import io.fd.jvpp.stats.dto.InterfaceStatisticsDetailsReplyDump;
import io.fd.jvpp.stats.dto.InterfaceStatisticsDump;
import io.fd.jvpp.stats.future.FutureJVppStatsFacade;
import java.util.concurrent.Future;
import java.util.logging.Logger;

public class FutureApiTest {

    private static final Logger LOG = Logger.getLogger(FutureApiTest.class.getName());

    public static void main(String[] args) throws Exception {
        testCallbackApi(args);
    }

    private static void testCallbackApi(String[] args) throws Exception {
        LOG.info("Testing Java callback API for stats plugin");
        try (final JVppRegistry registry = new JVppStatsRegistryImpl("FutureApiTest");
             final FutureJVppStatsFacade jvpp = new FutureJVppStatsFacade(registry, new JVppStatsImpl())) {
            LOG.info("Successfully connected to VPP");
            testinterfaceStatisticsDump(jvpp);

            LOG.info("Disconnecting...");
        }
    }

    private static void testinterfaceStatisticsDump(FutureJVppStatsFacade jvpp) throws Exception {
        LOG.info("Sending InterfaceStatisticsDump request...");
        final InterfaceStatisticsDump request = new InterfaceStatisticsDump();

        final Future<InterfaceStatisticsDetailsReplyDump> replyFuture =
                jvpp.interfaceStatisticsDump(request).toCompletableFuture();
        final InterfaceStatisticsDetailsReplyDump reply = replyFuture.get();

        if (reply == null || reply.interfaceStatisticsDetails == null) {
            throw new IllegalStateException("Received null response for empty dump: " + reply);
        } else {
            LOG.info(String.format("Received interface statistics reply: %s \n", reply.interfaceStatisticsDetails));
        }
    }
}
