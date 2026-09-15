package ch.so.agi.hop.pointcloud;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;

/**
 * Logical schema of a point cloud: CRS, point count, bounds and dimensions.
 *
 * <p>The CRS is described by its well-known text and, when available, by an authority code such as
 * {@code EPSG:2056}. A point count of {@code -1} means unknown. Dimensions are never empty and
 * names are unique; their order is the provider order.
 */
public record PointCloudDescriptor(
    String crsWkt,
    String authority,
    long pointCount,
    PointCloudBounds bounds,
    List<PointDimension> dimensions) {

  public static final long UNKNOWN_POINT_COUNT = -1L;

  public PointCloudDescriptor {
    Objects.requireNonNull(bounds);
    if (pointCount < UNKNOWN_POINT_COUNT) throw new IllegalArgumentException("Invalid point count");
    dimensions = List.copyOf(dimensions);
    if (dimensions.isEmpty()) throw new IllegalArgumentException("At least one dimension is required");
    if (dimensions.size() > 4096) throw new IllegalArgumentException("Too many point dimensions");
    var names = new HashSet<String>();
    for (var dimension : dimensions)
      if (!names.add(dimension.name()))
        throw new IllegalArgumentException("Duplicate point dimension: " + dimension.name());
  }

  public PointCloudDescriptor withPointCount(long newPointCount) {
    return new PointCloudDescriptor(crsWkt, authority, newPointCount, bounds, dimensions);
  }

  public PointCloudDescriptor withBounds(PointCloudBounds newBounds) {
    return new PointCloudDescriptor(crsWkt, authority, pointCount, newBounds, dimensions);
  }

  public PointCloudDescriptor withCrs(String newCrsWkt, String newAuthority) {
    return new PointCloudDescriptor(newCrsWkt, newAuthority, pointCount, bounds, dimensions);
  }

  public PointCloudDescriptor withDimensions(List<PointDimension> newDimensions) {
    return new PointCloudDescriptor(crsWkt, authority, pointCount, bounds, newDimensions);
  }

  public boolean hasPointCount() {
    return pointCount != UNKNOWN_POINT_COUNT;
  }

  @Override
  public String toString() {
    return "PointCloud["
        + (authority == null ? "unknown CRS" : authority)
        + ", "
        + (hasPointCount() ? pointCount + " points" : "unknown points")
        + ", "
        + dimensions.size()
        + " dimensions]";
  }
}
