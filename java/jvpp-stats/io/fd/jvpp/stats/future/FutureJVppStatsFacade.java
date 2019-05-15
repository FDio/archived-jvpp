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

package io.fd.jvpp.stats.future;

import io.fd.jvpp.notification.EventRegistry;
import io.fd.jvpp.stats.dto.InterfaceNamesDetailsReplyDump;
import io.fd.jvpp.stats.dto.InterfaceNamesDump;
import io.fd.jvpp.stats.dto.InterfaceStatisticsDetailsReplyDump;
import java.util.concurrent.CompletionStage;

/**
 * <p>Implementation of FutureJVpp based on AbstractFutureJVppInvoker
 */
public class FutureJVppStatsFacade extends io.fd.jvpp.stats.future.AbstractFutureJVppInvoker
        implements FutureJVppStats {

    /**
     * <p>Create FutureJVppStatsFacade object for provided JVpp instance.
     * Constructor internally creates FutureJVppFacadeCallback class for processing callbacks
     * and then connects to provided JVpp instance
     *
     * @param jvpp provided io.fd.jvpp.JVpp instance
     * @throws java.io.IOException in case instance cannot connect to JVPP
     */
    public FutureJVppStatsFacade(final io.fd.jvpp.JVppRegistry registry, final io.fd.jvpp.JVpp jvpp)
            throws java.io.IOException {
        super(jvpp, registry, new java.util.HashMap<>());
        java.util.Objects.requireNonNull(registry, "JVppRegistry should not be null");
        registry.register(jvpp, new FutureJVppStatsFacadeCallback(getRequests()));
    }

    @Override
    public java.util.concurrent.CompletionStage<InterfaceStatisticsDetailsReplyDump> interfaceStatisticsDump(
            io.fd.jvpp.stats.dto.InterfaceStatisticsDump request) {
        return send(request, new InterfaceStatisticsDetailsReplyDump());
    }

    @Override
    public CompletionStage<InterfaceNamesDetailsReplyDump> interfaceNamesDump(final InterfaceNamesDump request) {
        return send(request, new InterfaceNamesDetailsReplyDump());
    }

    @Override
    public EventRegistry getEventRegistry() {
        return null;
    }
}
