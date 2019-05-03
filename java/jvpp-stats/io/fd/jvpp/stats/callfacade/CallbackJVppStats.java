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

import io.fd.jvpp.stats.callback.InterfaceStatisticsDetailsCallback;

/**
 * <p>Callback Java API representation of io.fd.jvpp.stats plugin.
 */
public interface CallbackJVppStats extends io.fd.jvpp.notification.EventRegistryProvider, AutoCloseable {

    // TODO add send
    void interfaceStatisticsDump(InterfaceStatisticsDetailsCallback callback) throws io.fd.jvpp.VppInvocationException;
}
