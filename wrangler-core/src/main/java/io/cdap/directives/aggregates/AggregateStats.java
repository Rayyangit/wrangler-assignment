/*
 *  Copyright © 2024
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and limitations under the License.
 */

package io.cdap.directives.aggregates;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.cdap.cdap.api.annotation.Description;
import io.cdap.cdap.api.annotation.Name;
import io.cdap.cdap.api.annotation.Plugin;
import io.cdap.wrangler.api.Arguments;
import io.cdap.wrangler.api.Directive;
import io.cdap.wrangler.api.DirectiveExecutionException;
import io.cdap.wrangler.api.DirectiveParseException;
import io.cdap.wrangler.api.EntityCountMetric;
import io.cdap.wrangler.api.ExecutorContext;
import io.cdap.wrangler.api.Row;
import io.cdap.wrangler.api.annotations.Categories;
import io.cdap.wrangler.api.parser.Identifier;
import io.cdap.wrangler.api.parser.TokenType;
import io.cdap.wrangler.api.parser.UsageDefinition;

@Plugin(type = Directive.TYPE)
@Name("aggregate-stats")
@Categories(categories = { "aggregate" })
@Description("Aggregates byte size and time duration values in a column, like 10KB + 20MB or 150ms + 2s.")
public class AggregateStats implements Directive {

  private String column;

  @Override
  public UsageDefinition define() {
    UsageDefinition.Builder builder = UsageDefinition.builder("aggregate-stats");
    builder.define("column", TokenType.IDENTIFIER);
    return builder.build();
  }

  @Override
  public void initialize(Arguments arguments) throws DirectiveParseException {
    this.column = ((Identifier) arguments.value("column")).value();
  }

  @Override
  public void destroy() {
    // no-op
  }

  @Override
  public List<Row> execute(List<Row> rows, ExecutorContext context) throws DirectiveExecutionException {
    long totalBytes = 0L;
    long totalTimeMs = 0L;
    int count = 0;

    for (Row row : rows) {
      Object value = row.getValue(column);
      if (value instanceof String) {
        String strVal = ((String) value).trim();
        try {
          if (isByteSize(strVal)) {
            totalBytes += parseBytes(strVal);
          } else if (isTimeDuration(strVal)) {
            totalTimeMs += parseDuration(strVal);
          }
          count++;
        } catch (Exception e) {
          throw new DirectiveExecutionException("aggregate-stats", "Failed to parse value: " + strVal, e);
        }
      }
    }

    Map<String, Object> stats = new HashMap<>();
    stats.put("total_bytes", totalBytes);
    stats.put("total_time_ms", totalTimeMs);
    stats.put("average_bytes", count > 0 ? totalBytes / count : 0);
    stats.put("average_time_ms", count > 0 ? totalTimeMs / count : 0);

    Row summaryRow = new Row();
    for (Map.Entry<String, Object> entry : stats.entrySet()) {
      summaryRow.add(entry.getKey(), entry.getValue());
    }

    return Collections.singletonList(summaryRow);
  }

  private boolean isByteSize(String input) {
    return input.toLowerCase().matches(".*\\d+\\s*(b|kb|mb|gb|tb)$");
  }

  private boolean isTimeDuration(String input) {
    return input.toLowerCase().matches(".*\\d+\\s*(ms|s|m|h)$");
  }

  private long parseBytes(String input) {
    Matcher matcher = Pattern.compile("(?i)(\\d+(?:\\.\\d+)?)\\s*(b|kb|mb|gb|tb)").matcher(input.trim());
    if (matcher.matches()) {
      double number = Double.parseDouble(matcher.group(1));
      String unit = matcher.group(2).toLowerCase();
      switch (unit) {
        case "tb":
          return (long) (number * 1024 * 1024 * 1024 * 1024);
        case "gb":
          return (long) (number * 1024 * 1024 * 1024);
        case "mb":
          return (long) (number * 1024 * 1024);
        case "kb":
          return (long) (number * 1024);
        default:
          return (long) number; // bytes
      }
    }
    return 0;
  }

  private long parseDuration(String input) {
    Matcher matcher = Pattern.compile("(?i)(\\d+(?:\\.\\d+)?)\\s*(ms|s|m|h)").matcher(input.trim());
    if (matcher.matches()) {
      double number = Double.parseDouble(matcher.group(1));
      String unit = matcher.group(2).toLowerCase();
      switch (unit) {
        case "h":
          return (long) (number * 3600000);
        case "m":
          return (long) (number * 60000);
        case "s":
          return (long) (number * 1000);
        case "ms":
          return (long) number;
      }
    }
    return 0;
  }

  @Override
  public List<EntityCountMetric> getCountMetrics() {
    return null;
  }
}
