package ch.so.agi.hop.pointcloud;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * A named point dimension with its storage type.
 *
 * <p>Dimension names are provider names such as {@code X}, {@code Intensity} or
 * {@code HeightAboveGround}. They are case sensitive and limited to letters, digits and
 * underscores.
 */
public record PointDimension(String name, PointDataType type) {
  private static final Pattern NAME = Pattern.compile("[A-Za-z][A-Za-z0-9_]{0,63}");

  public PointDimension {
    Objects.requireNonNull(name);
    Objects.requireNonNull(type);
    if (!NAME.matcher(name).matches())
      throw new IllegalArgumentException("Invalid point dimension name: " + name);
  }

  public static PointDimension of(String name, PointDataType type) {
    return new PointDimension(name, type);
  }

  @Override
  public String toString() {
    return name + " " + type;
  }
}
