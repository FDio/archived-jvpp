/*
 * Copyright (c) 2016 Cisco and/or its affiliates.
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

package io.fd.jvpp.core.examples;

import io.fd.jvpp.JVpp;
import io.fd.jvpp.JVppRegistry;
import io.fd.jvpp.JVppRegistryImpl;
import io.fd.jvpp.VppCallbackException;
import io.fd.jvpp.core.JVppCoreImpl;
import io.fd.jvpp.core.callback.GetNodeIndexReplyCallback;
import io.fd.jvpp.core.callback.ShowVersionReplyCallback;
import io.fd.jvpp.core.callback.SwInterfaceDetailsCallback;
import io.fd.jvpp.core.dto.GetNodeIndex;
import io.fd.jvpp.core.dto.GetNodeIndexReply;
import io.fd.jvpp.core.dto.ShowVersion;
import io.fd.jvpp.core.dto.ShowVersionReply;
import io.fd.jvpp.core.dto.SwInterfaceDetails;
import io.fd.jvpp.core.dto.SwInterfaceDump;
import java.nio.charset.StandardCharsets;

public class CallbackApiExample {

    public static void main(String[] args) throws Exception {
        testCallbackApi();
    }

    private static void testCallbackApi() throws Exception {
        System.out.println("Testing Java callback API with JVppRegistry");
        try (final JVppRegistry registry = new JVppRegistryImpl("CallbackApiExample");
             final JVpp jvpp = new JVppCoreImpl()) {
            registry.register(jvpp, new TestCallback());

            System.out.println("Sending ShowVersion request...");
            final int result = jvpp.send(new ShowVersion());
            System.out.printf("ShowVersion send result = %d%n", result);

            System.out.println("Sending GetNodeIndex request...");
            GetNodeIndex getNodeIndexRequest = new GetNodeIndex();
            getNodeIndexRequest.nodeName = "non-existing-node".getBytes(StandardCharsets.UTF_8);
            jvpp.send(getNodeIndexRequest);

            System.out.println("Sending SwInterfaceDump request...");
            SwInterfaceDump swInterfaceDumpRequest = new SwInterfaceDump();
            swInterfaceDumpRequest.nameFilterValid = false;
            swInterfaceDumpRequest.nameFilter = "";
            jvpp.send(swInterfaceDumpRequest);

            Thread.sleep(1000);
            System.out.println("Disconnecting...");
        }
        Thread.sleep(1000);
    }

    static class TestCallback implements GetNodeIndexReplyCallback, ShowVersionReplyCallback, SwInterfaceDetailsCallback {

        @Override
        public void onGetNodeIndexReply(final GetNodeIndexReply msg) {
            System.out.printf("Received GetNodeIndexReply: %s%n", msg);
        }

        @Override
        public void onShowVersionReply(final ShowVersionReply msg) {
            System.out.printf("Received ShowVersionReply: context=%d, program=%s, version=%s, "
                    + "buildDate=%s, buildDirectory=%s%n",
                msg.context,
                msg.program,
                msg.version,
                msg.buildDate,
                msg.buildDirectory);
        }

        @Override
        public void onSwInterfaceDetails(final SwInterfaceDetails msg) {
            System.out.printf("Received SwInterfaceDetails: interfaceName=%s, l2AddressLength=%d, flags=%s, "
                    + "linkSpeed=%d, linkMtu=%d%n",
                msg.interfaceName, msg.l2Address.macaddress.length, msg.flags.getOptions(),
                msg.linkSpeed, (int) msg.linkMtu);
        }

        @Override
        public void onError(VppCallbackException ex) {
            System.out.printf("Received onError exception: call=%s, context=%d, retval=%d%n", ex.getMethodName(),
                ex.getCtxId(), ex.getErrorCode());
        }
    }
}
