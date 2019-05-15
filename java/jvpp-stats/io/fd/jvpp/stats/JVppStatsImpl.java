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

import io.fd.jvpp.JVppRegistry;
import io.fd.jvpp.VppConnection;
import io.fd.jvpp.VppInvocationException;
import io.fd.jvpp.callback.JVppCallback;
import io.fd.jvpp.dto.ControlPing;
import io.fd.jvpp.dto.JVppRequest;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;
import java.util.logging.Logger;

public class JVppStatsImpl implements io.fd.jvpp.stats.JVppStats {

    private final static Logger LOG = Logger.getLogger(JVppStatsImpl.class.getName());
    private static final java.lang.String LIBNAME = "libjvpp_stats.so";

    static {
        try {
            loadLibrary();
        } catch (Exception e) {
            LOG.severe("Can't find jvpp jni library: " + LIBNAME);
            throw new ExceptionInInitializerError(e);
        }
    }

    private VppConnection connection;
    private JVppRegistry registry;

    private static void loadStream(final InputStream is) throws IOException {
        final Set<PosixFilePermission> perms = PosixFilePermissions.fromString("rwxr-x---");
        final Path p = Files.createTempFile(LIBNAME, null, PosixFilePermissions.asFileAttribute(perms));
        try {
            Files.copy(is, p, StandardCopyOption.REPLACE_EXISTING);

            try {
                Runtime.getRuntime().load(p.toString());
            } catch (UnsatisfiedLinkError e) {
                throw new IOException("Failed to load library " + p, e);
            }
        } finally {
            try {
                Files.deleteIfExists(p);
            } catch (IOException e) {
            }
        }
    }

    private static void loadLibrary() throws IOException {
        try (final InputStream is = JVppStatsImpl.class.getResourceAsStream('/' + LIBNAME)) {
            if (is == null) {
                throw new IOException("Failed to open library resource " + LIBNAME);
            }
            loadStream(is);
        }
    }

    private static native void init0(final JVppCallback callback, final long queueAddress, final int clientIndex);

    private static native int interfaceStatisticsDump0() throws io.fd.jvpp.VppInvocationException;

    private static native int interfaceNamesDump0() throws io.fd.jvpp.VppInvocationException;

    private static native void close0();

    @Override
    public int send(final JVppRequest request) throws VppInvocationException {
        return request.send(this);
    }

    @Override
    public void init(final JVppRegistry registry, final JVppCallback callback, final long queueAddress,
                     final int clientIndex) {
        this.registry = java.util.Objects.requireNonNull(registry, "registry should not be null");
        this.connection = java.util.Objects.requireNonNull(registry.getConnection(), "connection should not be null");
        connection.checkActive();
        init0(callback, queueAddress, clientIndex);
    }

    @Override
    public int controlPing(final ControlPing controlPing) {
        return 1;
    }

    @Override
    public int interfaceStatisticsDump() throws VppInvocationException {
        connection.checkActive();
        LOG.fine("Sending interfaceStatisticsDump event message");
        int result = interfaceStatisticsDump0();
        if (result < 0) {
            throw new io.fd.jvpp.VppInvocationException("interfaceStatisticsDump", result);
        }
        return result;
    }

    @Override
    public int interfaceNamesDump() throws VppInvocationException {
        connection.checkActive();
        LOG.fine("Sending interfaceNamesDump event message");
        int result = interfaceNamesDump0();
        if (result < 0) {
            throw new io.fd.jvpp.VppInvocationException("interfaceNamesDump", result);
        }
        return result;
    }

    @Override
    public void close() {
        close0();
    }
}
