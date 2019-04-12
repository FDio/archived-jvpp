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

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.fd.jvpp.stats.dto.InterfaceStatistics;

public class JVppClientStatsImpl {

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

    public static native InterfaceStatistics[] interfaceStatisticsDump();
}
