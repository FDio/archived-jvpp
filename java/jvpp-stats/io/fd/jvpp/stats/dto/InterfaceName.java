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

import java.util.Objects;

public class InterfaceName {

    public int swIfIndex;
    public String name;

    public InterfaceName(final int swIfIndex, final String name) {
        this.swIfIndex = swIfIndex;
        this.name = name;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        final InterfaceName otherIfc = (InterfaceName) other;
        return swIfIndex == otherIfc.swIfIndex && name.equals(otherIfc.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(swIfIndex, name);
    }

    @Override
    public String toString() {
        return "InterfaceName{" +
                "swIfIndex=" + swIfIndex +
                ", name=" + name +
                '}';
    }
}
