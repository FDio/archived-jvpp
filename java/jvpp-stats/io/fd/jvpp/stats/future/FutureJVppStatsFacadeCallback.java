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

import io.fd.jvpp.stats.dto.InterfaceStatisticsDetailsReplyDump;

/**
 * <p>Async facade callback setting values to future objects
 */
public final class FutureJVppStatsFacadeCallback implements io.fd.jvpp.stats.callback.JVppStatsGlobalCallback {

    private static final java.util.logging.Logger LOG = java.util.logging.Logger.getLogger(
            FutureJVppStatsFacadeCallback.class.getName());
    private final java.util.Map<Integer, java.util.concurrent.CompletableFuture<? extends io.fd.jvpp.dto.JVppReply<?>>>
            requests;

    public FutureJVppStatsFacadeCallback(
            final java.util.Map<Integer, java.util.concurrent.CompletableFuture<? extends io.fd.jvpp.dto.JVppReply<?>>> requestMap) {
        this.requests = requestMap;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onError(io.fd.jvpp.VppCallbackException reply) {
        final java.util.concurrent.CompletableFuture<io.fd.jvpp.dto.JVppReply<?>> completableFuture;

        synchronized (requests) {
            completableFuture = (java.util.concurrent.CompletableFuture<io.fd.jvpp.dto.JVppReply<?>>) requests
                    .get(reply.getCtxId());
        }

        if (completableFuture != null) {
            completableFuture.completeExceptionally(reply);

            synchronized (requests) {
                requests.remove(reply.getCtxId());
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onInterfaceStatisticsDetails(final io.fd.jvpp.stats.dto.InterfaceStatisticsDetails reply) {
        io.fd.jvpp.stats.future.AbstractFutureJVppInvoker.CompletableDumpFuture<InterfaceStatisticsDetailsReplyDump>
                completableFuture;
        final int replyId = reply.context;
        if (LOG.isLoggable(java.util.logging.Level.FINE)) {
            LOG.fine(String.format("Received InterfaceStatisticsDetails event message: %s", reply));
        }
        synchronized (requests) {
            completableFuture =
                    (io.fd.jvpp.stats.future.AbstractFutureJVppInvoker.CompletableDumpFuture<InterfaceStatisticsDetailsReplyDump>) requests
                            .get(replyId);

            if (completableFuture == null) {
                // reply received before writer created future,
                // create new future, and put into map to notify sender that reply is already received,
                // following details replies will add information to this future
                completableFuture =
                        new io.fd.jvpp.stats.future.AbstractFutureJVppInvoker.CompletableDumpFuture<>(replyId,
                                new InterfaceStatisticsDetailsReplyDump());
                requests.put(replyId, completableFuture);
            }
            completableFuture.getReplyDump().interfaceStatisticsDetails = reply;
        }
    }
}
