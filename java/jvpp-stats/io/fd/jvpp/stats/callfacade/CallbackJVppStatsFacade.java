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

package io.fd.jvpp.stats.callfacade;

import io.fd.jvpp.notification.EventRegistry;

/**
 * <p>Default implementation of CallbackStatsJVpp interface.
 */
public final class CallbackJVppStatsFacade implements CallbackJVppStats {

    private final io.fd.jvpp.stats.JVppStats jvpp;
    private final java.util.Map<Integer, io.fd.jvpp.callback.JVppCallback> callbacks;

    /**
     * <p>Create CallbackJVppStatsFacade object for provided JVpp instance.
     * Constructor internally creates CallbackJVppFacadeCallback class for processing callbacks
     * and then connects to provided JVpp instance
     *
     * @param jvpp provided io.fd.jvpp.JVpp instance
     * @throws java.io.IOException in case instance cannot connect to JVPP
     */
    public CallbackJVppStatsFacade(final io.fd.jvpp.JVppRegistry registry, final io.fd.jvpp.stats.JVppStats jvpp)
            throws java.io.IOException {
        this.jvpp = java.util.Objects.requireNonNull(jvpp, "jvpp is null");
        this.callbacks = new java.util.HashMap<>();
        java.util.Objects.requireNonNull(registry, "JVppRegistry should not be null");
        registry.register(jvpp, new CallbackJVppStatsFacadeCallback(this.callbacks));
    }

    @Override
    public void close() throws Exception {
        jvpp.close();
    }

    public final void interfaceStatisticsDump(io.fd.jvpp.stats.callback.InterfaceStatisticsDetailsCallback callback)
            throws io.fd.jvpp.VppInvocationException {
        synchronized (callbacks) {
            callbacks.put(jvpp.interfaceStatisticsDump(), callback);
        }
    }

    @Override
    public EventRegistry getEventRegistry() {
        return null;
    }
}
