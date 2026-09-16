package ch.so.agi.hop.pointcloud;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * A single provider stage of a point cloud pipeline (for example {@code readers.las},
 * {@code filters.crop} or {@code writers.copc}).
 *
 * <p>Option values are plain text. Rendering them as pipeline JSON is the responsibility of the
 * execution backend, which knows the provider semantics.
 */
public record PdalStage(String type, Map<String, String> options) {
  private static final Pattern TYPE = Pattern.compile("[a-z][a-z0-9_]*\\.[a-z][A-Za-z0-9_]*");
  private static final Pattern OPTION = Pattern.compile("[A-Za-z][A-Za-z0-9_]*");
  private static final int MAX_OPTIONS = 64;
  private static final int MAX_OPTION_LENGTH = 64 * 1024;

  public PdalStage {
    Objects.requireNonNull(type);
    Objects.requireNonNull(options);
    if (!TYPE.matcher(type).matches()) throw new IllegalArgumentException("Invalid stage type: " + type);
    if (options.size() > MAX_OPTIONS) throw new IllegalArgumentException("Too many stage options");
    var copy = new LinkedHashMap<String, String>();
    for (var entry : options.entrySet()) {
      String key = Objects.requireNonNull(entry.getKey(), "option key");
      String value = Objects.requireNonNull(entry.getValue(), "option value");
      if (!OPTION.matcher(key).matches()) throw new IllegalArgumentException("Invalid option name: " + key);
      if (value.length() > MAX_OPTION_LENGTH) throw new IllegalArgumentException("Option value too long: " + key);
      copy.put(key, value);
    }
    options = Collections.unmodifiableMap(copy);
  }

  public static PdalStage of(String type) {
    return new PdalStage(type, Map.of());
  }

  public static PdalStage of(String type, Map<String, String> options) {
    return new PdalStage(type, options);
  }

  /** Returns the option value or {@code null} when the option is not set. */
  public String option(String key) {
    return options.get(key);
  }

  @Override
  public String toString() {
    return options.isEmpty() ? type : type + " " + options;
  }
}
