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

/**
 * <p>Java representation of plugin's api file.
 */
public interface JVppStats extends io.fd.jvpp.JVpp {
    /**
     * Generic dispatch method for sending requests to VPP
     *
     * @throws io.fd.jvpp.VppInvocationException if send request had failed
     */
    int send(io.fd.jvpp.dto.JVppRequest request) throws io.fd.jvpp.VppInvocationException;

    int interfaceStatisticsDump() throws io.fd.jvpp.VppInvocationException;
}
