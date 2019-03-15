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

import static java.util.Objects.requireNonNull;

import io.fd.jvpp.JVppRegistry;
import io.fd.jvpp.JVppRegistryImpl;
import io.fd.jvpp.core.JVppCoreImpl;
import io.fd.jvpp.core.dto.CreateSubif;
import io.fd.jvpp.core.dto.CreateSubifReply;
import io.fd.jvpp.core.dto.SwInterfaceDetailsReplyDump;
import io.fd.jvpp.core.dto.SwInterfaceDump;
import io.fd.jvpp.core.future.FutureJVppCoreFacade;
import io.fd.jvpp.core.types.InterfaceIndex;
import io.fd.jvpp.core.types.SubIfFlags;

/**jvpp-core/io/fd/jvpp/core/examples/CreateSubInterfaceExample.java
 * <p>Tests sub-interface creation.<br> Equivalent to:<br>
 *
 * <pre>{@code
 * vppctl create sub GigabitEthernet0/9/0 1 dot1q 100 inner-dot1q any
 * }
 * </pre>
 *
 * To verify invoke:<br>
 * <pre>{@code
 * vpp_api_test json
 * vat# sw_interface_dump
 * }
 */
public class CreateSubInterfaceExample {

    private static SwInterfaceDump createSwInterfaceDumpRequest(final String ifaceName) {
        SwInterfaceDump request = new SwInterfaceDump();
        request.nameFilter = ifaceName;
        request.nameFilterValid = true;
        return request;
    }

    private static void requireSingleIface(final SwInterfaceDetailsReplyDump response, final String ifaceName) {
        if (response.swInterfaceDetails.size() != 1) {
            throw new IllegalStateException(
                String.format("Expected one interface matching filter %s but was %d", ifaceName,
                    response.swInterfaceDetails.size()));
        }
    }

    private static CreateSubif createSubifRequest(final InterfaceIndex swIfIndex, final int subId) {
        CreateSubif request = new CreateSubif();
        request.swIfIndex = swIfIndex; // super interface id
        request.subId = subId;
        request.subIfFlags = new SubIfFlags();
        request.subIfFlags.add(SubIfFlags.SubIfFlagsOptions.SUB_IF_API_FLAG_TWO_TAGS);
        request.subIfFlags.add(SubIfFlags.SubIfFlagsOptions.SUB_IF_API_FLAG_EXACT_MATCH);
        request.subIfFlags.add(SubIfFlags.SubIfFlagsOptions.SUB_IF_API_FLAG_INNER_VLAN_ID_ANY);
        request.outerVlanId = 100;
        request.innerVlanId = 0;
        return request;
    }

    private static void print(CreateSubifReply reply) {
        System.out.printf("CreateSubifReply: %s%n", reply);
    }

    private static void testCreateSubInterface() throws Exception {
        System.out.println("Testing sub-interface creation using Java callback API");
        try (final JVppRegistry registry = new JVppRegistryImpl("CreateSubInterfaceExample");
             final FutureJVppCoreFacade jvppFacade = new FutureJVppCoreFacade(registry, new JVppCoreImpl())) {
            System.out.println("Successfully connected to VPP");
            Thread.sleep(1000);

            final String ifaceName = "Gigabitethernet0/8/0";

            final SwInterfaceDetailsReplyDump swInterfaceDetails =
                jvppFacade.swInterfaceDump(createSwInterfaceDumpRequest(ifaceName)).toCompletableFuture().get();

            requireNonNull(swInterfaceDetails, "swInterfaceDump returned null");
            requireNonNull(swInterfaceDetails.swInterfaceDetails, "swInterfaceDetails is null");
            requireSingleIface(swInterfaceDetails, ifaceName);

            final InterfaceIndex swIfIndex = swInterfaceDetails.swInterfaceDetails.get(0).swIfIndex;
            final int subId = 1;

            final CreateSubifReply createSubifReply =
                jvppFacade.createSubif(createSubifRequest(swIfIndex, subId)).toCompletableFuture().get();
            print(createSubifReply);

            final String subIfaceName = "Gigabitethernet0/8/0." + subId;
            final SwInterfaceDetailsReplyDump subIface =
                jvppFacade.swInterfaceDump(createSwInterfaceDumpRequest(subIfaceName)).toCompletableFuture().get();
            requireNonNull(swInterfaceDetails, "swInterfaceDump returned null");
            requireNonNull(subIface.swInterfaceDetails, "swInterfaceDump returned null");
            requireSingleIface(swInterfaceDetails, ifaceName);

            System.out.println("Disconnecting...");
        }
        Thread.sleep(1000);
    }

    public static void main(String[] args) throws Exception {
        testCreateSubInterface();
    }
}
