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

package io.fd.jvpp.stats.dto;

import java.util.Objects;

public class InterfaceStatistics {

    public int swIfIndex;
    public int outErrors;
    public int outMulticastPkts;
    public int outUnicastPkts;
    public int outBroadcastPkts;
    public int outBytes;
    public int inErrors;
    public int inMulticastPkts;
    public int inUnicastPkts;
    public int inBroadcastPkts;
    public int inBytes;

    public InterfaceStatistics(final int swIfIndex, final int outErrors, final int outMulticastPkts,
                               final int outUnicastPkts, final int outBroadcastPkts,
                               final int outBytes, final int inErrors, final int inMulticastPkts,
                               final int inUnicastPkts, final int inBroadcastPkts,
                               final int inBytes) {
        this.swIfIndex = swIfIndex;
        this.outErrors = outErrors;
        this.outMulticastPkts = outMulticastPkts;
        this.outUnicastPkts = outUnicastPkts;
        this.outBroadcastPkts = outBroadcastPkts;
        this.outBytes = outBytes;
        this.inErrors = inErrors;
        this.inMulticastPkts = inMulticastPkts;
        this.inUnicastPkts = inUnicastPkts;
        this.inBroadcastPkts = inBroadcastPkts;
        this.inBytes = inBytes;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        final InterfaceStatistics otherIfcStats = (InterfaceStatistics) other;
        return swIfIndex == otherIfcStats.swIfIndex &&
                outErrors == otherIfcStats.outErrors &&
                outMulticastPkts == otherIfcStats.outMulticastPkts &&
                outUnicastPkts == otherIfcStats.outUnicastPkts &&
                outBroadcastPkts == otherIfcStats.outBroadcastPkts &&
                outBytes == otherIfcStats.outBytes &&
                inErrors == otherIfcStats.inErrors &&
                inMulticastPkts == otherIfcStats.inMulticastPkts &&
                inUnicastPkts == otherIfcStats.inUnicastPkts &&
                inBroadcastPkts == otherIfcStats.inBroadcastPkts &&
                inBytes == otherIfcStats.inBytes;
    }

    @Override
    public int hashCode() {
        return Objects
                .hash(swIfIndex, outErrors, outMulticastPkts, outUnicastPkts, outBroadcastPkts, outBytes, inErrors,
                        inMulticastPkts, inUnicastPkts, inBroadcastPkts, inBytes);
    }

    @Override
    public String toString() {
        return "InterfaceStatistics{" +
                "swIfIndex=" + swIfIndex +
                ", outErrors=" + outErrors +
                ", outMulticastPkts=" + outMulticastPkts +
                ", outUnicastPkts=" + outUnicastPkts +
                ", outBroadcastPkts=" + outBroadcastPkts +
                ", outBytes=" + outBytes +
                ", inErrors=" + inErrors +
                ", inMulticastPkts=" + inMulticastPkts +
                ", inUnicastPkts=" + inUnicastPkts +
                ", inBroadcastPkts=" + inBroadcastPkts +
                ", inBytes=" + inBytes +
                '}';
    }
}
