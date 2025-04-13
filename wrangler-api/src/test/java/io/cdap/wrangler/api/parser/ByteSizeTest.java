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

public class ByteSizeTest {
    @Test
    public void testByteParsing() {
        // Test valid byte units
        assertEquals(1000L, new ByteSize("1KB").getBytes()); // 1KB = 1000 bytes
        assertEquals(1500L, new ByteSize("1.5KB").getBytes());
        assertEquals(1_000_000L, new ByteSize("1MB").getBytes());
        assertEquals(1_000_000_000L, new ByteSize("1GB").getBytes());

        // Binary units (IEC)
        assertEquals(1_048_576L, new ByteSize("1MiB").getBytes()); // 1024 * 1024
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidByteUnit() {
        new ByteSize("10XB"); // Should throw exception
    }

    @Test
    public void testWhitespaceHandling() {
        assertEquals(1000L, new ByteSize("1 KB").getBytes());
        assertEquals(1000L, new ByteSize("1   KB").getBytes());
    }
}