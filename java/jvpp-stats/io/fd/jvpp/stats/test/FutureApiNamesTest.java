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
import io.fd.jvpp.stats.dto.InterfaceNamesDetailsReplyDump;
import io.fd.jvpp.stats.dto.InterfaceNamesDump;
import io.fd.jvpp.stats.future.FutureJVppStatsFacade;
import java.util.concurrent.Future;
import java.util.logging.Logger;

public class FutureApiNamesTest {

    private static final Logger LOG = Logger.getLogger(FutureApiNamesTest.class.getName());

    public static void main(String[] args) throws Exception {
        testCallbackApi(args);
    }

    private static void testCallbackApi(String[] args) throws Exception {
        LOG.info("Testing Java callback API for stats plugin");
        try (final JVppRegistry registry = new JVppStatsRegistryImpl("FutureApiTest");
             final FutureJVppStatsFacade jvpp = new FutureJVppStatsFacade(registry, new JVppStatsImpl())) {
            LOG.info("Successfully connected to VPP");
            testinterfaceNamesDump(jvpp);

            LOG.info("Disconnecting...");
        }
    }

    private static void testinterfaceNamesDump(FutureJVppStatsFacade jvpp) throws Exception {
        LOG.info("Sending InterfaceNamesDump request...");
        final InterfaceNamesDump request = new InterfaceNamesDump();

        final Future<InterfaceNamesDetailsReplyDump> replyFuture =
                jvpp.interfaceNamesDump(request).toCompletableFuture();
        final InterfaceNamesDetailsReplyDump reply = replyFuture.get();

        if (reply == null || reply.interfaceNamesDetails == null) {
            throw new IllegalStateException("Received null response for empty dump: " + reply);
        } else {
            LOG.info(String.format("Received interface names reply: %s \n", reply.interfaceNamesDetails));
        }
    }
}
