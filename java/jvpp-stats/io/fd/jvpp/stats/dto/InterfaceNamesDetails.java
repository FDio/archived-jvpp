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

package io.fd.jvpp.stats.dto;

/**
 * <p>This class represents reply DTO for InterfaceNamesDetails.
 */
public final class InterfaceNamesDetails implements io.fd.jvpp.dto.JVppReply<InterfaceNamesDump> {
    public int context;
    public InterfaceName[] interfaceNames;
    public int length;

    public InterfaceNamesDetails(int length, int context) {
        this.context = context;
        this.length = length;
        this.interfaceNames = new InterfaceName[length];
    }

    @Override
    @io.fd.jvpp.coverity.SuppressFBWarnings("UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD")
    public int hashCode() {
        return java.util.Objects.hash(context, interfaceNames, length);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final InterfaceNamesDetails other = (InterfaceNamesDetails) o;

        if (!java.util.Objects.equals(this.context, other.context)) {
            return false;
        }
        if (!java.util.Arrays.equals(this.interfaceNames, other.interfaceNames)) {
            return false;
        }
        if (!java.util.Objects.equals(this.length, other.length)) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public String toString() {
        return "InterfaceNamesDetails{" +
                "context=" + context +
                ", interfaceNames=" + java.util.Arrays.toString(interfaceNames) +
                ", length=" + length +
                '}';
    }
}
