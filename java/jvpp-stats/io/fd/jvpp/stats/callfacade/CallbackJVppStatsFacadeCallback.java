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

/**
 * <p>Implementation of JVppGlobalCallback interface for Java Callback API.
 */
public final class CallbackJVppStatsFacadeCallback implements io.fd.jvpp.stats.callback.JVppStatsGlobalCallback {

    private static final java.util.logging.Logger LOG = java.util.logging.Logger.getLogger(
            CallbackJVppStatsFacadeCallback.class.getName());
    private final java.util.Map<Integer, io.fd.jvpp.callback.JVppCallback> requests;

    public CallbackJVppStatsFacadeCallback(final java.util.Map<Integer, io.fd.jvpp.callback.JVppCallback> requestMap) {
        this.requests = requestMap;
    }

    @Override
    public void onError(io.fd.jvpp.VppCallbackException reply) {

        io.fd.jvpp.callback.JVppCallback failedCall;
        synchronized (requests) {
            failedCall = requests.remove(reply.getCtxId());
        }

        if (failedCall != null) {
            try {
                failedCall.onError(reply);
            } catch (RuntimeException ex) {
                ex.addSuppressed(reply);
                LOG.log(java.util.logging.Level.WARNING,
                        String.format("Callback: %s failed while handling exception: %s", failedCall, reply), ex);
            }
        }
    }

    @Override
    public void onInterfaceStatisticsDetails(final io.fd.jvpp.stats.dto.InterfaceStatisticsDetails reply) {

        io.fd.jvpp.stats.callback.InterfaceStatisticsDetailsCallback callback;
        final int replyId = reply.context;
        if (LOG.isLoggable(java.util.logging.Level.FINE)) {
            LOG.fine(String.format("Received InterfaceStatisticsDetails event message: %s", reply));
        }
        synchronized (requests) {
            callback = (io.fd.jvpp.stats.callback.InterfaceStatisticsDetailsCallback) requests.remove(replyId);
        }

        if (callback != null) {
            callback.onInterfaceStatisticsDetails(reply);
        }
    }

    @Override
    public void onInterfaceNamesDetails(final io.fd.jvpp.stats.dto.InterfaceNamesDetails reply) {

        io.fd.jvpp.stats.callback.InterfaceNamesDetailsCallback callback;
        final int replyId = reply.context;
        if (LOG.isLoggable(java.util.logging.Level.FINE)) {
            LOG.fine(String.format("Received InterfaceNamesDetails event message: %s", reply));
        }
        synchronized (requests) {
            callback = (io.fd.jvpp.stats.callback.InterfaceNamesDetailsCallback) requests.remove(replyId);
        }

        if (callback != null) {
            callback.onInterfaceNamesDetails(reply);
        }
    }
}
