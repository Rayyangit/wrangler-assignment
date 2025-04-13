
/*
 * Copyright © 2025 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package io.cdap.wrangler.api.parser;

import static org.junit.Assert.*;

import org.junit.Test;

public class TimeDurationTest {
    @Test
    public void testDurationParsing() {
        // Test valid time units
        assertEquals(1000000L, new TimeDuration("1ms").getNanoseconds());
        assertEquals(1000000000L, new TimeDuration("1s").getNanoseconds());
        assertEquals(2100000000L, new TimeDuration("2.1s").getNanoseconds());
        assertEquals(3600000000000L, new TimeDuration("1h").getNanoseconds());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidTimeUnit() {
        new TimeDuration("10zs"); // Should throw exception
    }

    @Test
    public void testUnitlessDefaults() {
        assertEquals(1000000L, new TimeDuration("1").getNanoseconds()); // Defaults to ms
    }
}