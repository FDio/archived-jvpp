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

package io.fd.jvpp.stats;

import static io.fd.jvpp.NativeLibraryLoader.loadLibrary;
import static java.lang.String.format;

import io.fd.jvpp.stats.dto.InterfaceStatistics;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JVppClientStatsImpl implements AutoCloseable {

    private boolean connected;

    private static final Logger LOG = Logger.getLogger(JVppClientStatsImpl.class.getName());

    static {
            final String libName = "libjvpp_stats.so";
            try {
                loadLibrary(libName, JVppClientStatsImpl.class);
            } catch (IOException e) {
                LOG.log(Level.SEVERE, format("Can't find vpp jni library: %s", libName), e);
                throw new ExceptionInInitializerError(e);
            }
        }

    private static native InterfaceStatistics[] interfaceStatisticsDump();
    private static native int statSegmentConnect();
    private static native void statSegmentDisconnect();

    public synchronized List<InterfaceStatistics> getInterfaceStatistics() {
        if (!this.connected) {
            LOG.severe("Unable to dump statistics. Client isn't connected. Try reconnecting.");
            return null;
        }
        InterfaceStatistics[] statDump = interfaceStatisticsDump();
        List<InterfaceStatistics> statistics = new LinkedList();
        if (statDump != null) {
            statistics = Arrays.asList(statDump);
        }
        return statistics;
    }

    public JVppClientStatsImpl() {
        connectClient();
    }

    public boolean reconnect() {
        connectClient();
        return this.connected;
    }

    private void connectClient() {
        if (!this.connected) {
            if (statSegmentConnect() == 0) {
                this.connected = true;
            }
        }
    }

    public boolean isConnected() {
        return connected;
    }

    public void close() {
        if (this.connected) {
            statSegmentDisconnect();
            this.connected = false;
        }
    }
}
