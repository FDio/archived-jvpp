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

package io.fd.jvpp.stats;

import io.fd.jvpp.JVpp;
import io.fd.jvpp.JVppRegistry;
import io.fd.jvpp.VppInvocationException;
import io.fd.jvpp.callback.JVppCallback;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Objects.requireNonNull;

/**
 * Stats implementation of JVppRegistry.
 */
public final class JVppStatsRegistryImpl implements JVppRegistry {

    private final VppStatsJNIConnection connection;
    // Unguarded concurrent map, no race conditions expected on top of that
    private final Map<String, JVppCallback> pluginRegistry;

    public JVppStatsRegistryImpl(final String clientName) throws IOException {
        connection = new VppStatsJNIConnection(clientName);
        connection.connect();
        pluginRegistry = new ConcurrentHashMap<>();
    }

    @Override
    public VppStatsJNIConnection getConnection() {
        return connection;
    }

    @Override
    public void register(final JVpp jvpp, final JVppCallback callback) {
        requireNonNull(jvpp, "jvpp should not be null");
        requireNonNull(callback, "Callback should not be null");
        final String name = jvpp.getClass().getName();
        if (pluginRegistry.containsKey(name)) {
            throw new IllegalArgumentException(
                    String.format("Callback for plugin %s was already registered", name));
        }
        jvpp.init(this, callback, connection.getStatsConnectionInfo().queueAddress,
                connection.getStatsConnectionInfo().clientIndex);
        pluginRegistry.put(name, callback);
    }

    @Override
    public void unregister(final String name) {
        requireNonNull(name, "Plugin name should not be null");
        final JVppCallback previous = pluginRegistry.remove(name);
        assertPluginWasRegistered(name, previous);
    }

    @Override
    public JVppCallback get(final String name) {
        requireNonNull(name, "Plugin name should not be null");
        JVppCallback value = pluginRegistry.get(name);
        assertPluginWasRegistered(name, value);
        return value;
    }

    private native int controlPing0() throws VppInvocationException;

    @Override
    public int controlPing(Class<? extends JVpp> clazz) throws VppInvocationException {
        return controlPing0();
    }

    private static void assertPluginWasRegistered(final String name, final JVppCallback value) {
        if (value == null) {
            throw new IllegalArgumentException(String.format("Callback for plugin %s is not registered", name));
        }
    }

    @Override
    public void close() throws Exception {
        connection.close();
    }
}
