/*
 * Copyright © 2017-2024 Cask Data, Inc.
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

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import io.cdap.wrangler.api.annotations.PublicEvolving;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Token for representing time duration values with units (e.g., 100ms, 5s, 2h)
 */
@PublicEvolving
public class TimeDuration implements Token {
    private static final Pattern TIME_PATTERN = Pattern.compile("^(\\d+(?:\\.\\d+)?)\\s*([numhds]?s?)$",
            Pattern.CASE_INSENSITIVE);

    private final String originalValue;
    private final long nanoseconds;

    public TimeDuration(String value) {
        this.originalValue = value;
        this.nanoseconds = parseDuration(value.trim());
    }

    @Override
    public Object value() {
        return originalValue;
    }

    @Override
    public TokenType type() {
        return TokenType.TIME_DURATION;
    }

    @Override
    public JsonElement toJson() {
        return new JsonPrimitive(originalValue);
    }

    public long getNanoseconds() {
        return nanoseconds;
    }

    private long parseDuration(String value) {
        Matcher matcher = TIME_PATTERN.matcher(value);
        if (!matcher.matches()) {
            throw new IllegalArgumentException(
                    String.format("'%s' is not a valid time duration. Expected formats like 100ms, 5s, 2h", value));
        }

        double duration = Double.parseDouble(matcher.group(1));
        String unit = matcher.group(2).toLowerCase();

        switch (unit) {
            case "ns":
                return (long) duration;
            case "us":
                return (long) (duration * 1000L);
            case "ms":
                return (long) (duration * 1000L * 1000);
            case "s":
                return (long) (duration * 1000L * 1000 * 1000);
            case "m":
                return (long) (duration * 60L * 1000 * 1000 * 1000);
            case "h":
                return (long) (duration * 60L * 60 * 1000 * 1000 * 1000);
            case "d":
                return (long) (duration * 24L * 60 * 60 * 1000 * 1000 * 1000);
            default: // unit-less defaults to milliseconds
                return (long) (duration * 1000L * 1000);
        }
    }
}