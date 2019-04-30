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

public class InterfaceStatistics {

    private int swIfIndex;
    private int outErrors;
    private int outMulticastPkts;
    private int outUnicastPkts;
    private int outBroadcastPkts;
    private int outBytes;
    private int inErrors;
    private int inMulticastPkts;
    private int inUnicastPkts;
    private int inBroadcastPkts;
    private int inBytes;

    public InterfaceStatistics(final int swIfIndex, final int outErrors, final int outMulticastPkts,
                               final int outUnicastPkts,
                               final int outBroadcastPkts, final int outBytes, final int inErrors,
                               final int inMulticastPkts, final int inUnicastPkts,
                               final int inBroadcastPkts, final int inBytes) {
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

    public int getSwIfIndex() {
        return swIfIndex;
    }

    public int getOutErrors() {
        return outErrors;
    }

    public int getOutMulticastPkts() {
        return outMulticastPkts;
    }

    public int getOutUnicastPkts() {
        return outUnicastPkts;
    }

    public int getOutBroadcastPkts() {
        return outBroadcastPkts;
    }

    public int getOutBytes() {
        return outBytes;
    }

    public int getInErrors() {
        return inErrors;
    }

    public int getInMulticastPkts() {
        return inMulticastPkts;
    }

    public int getInUnicastPkts() {
        return inUnicastPkts;
    }

    public int getInBroadcastPkts() {
        return inBroadcastPkts;
    }

    public int getInBytes() {
        return inBytes;
    }

    @java.lang.Override
    public java.lang.String toString() {
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
