package ch.so.agi.hop.pointcloud;

/**
 * Axis aligned bounds of a point cloud in its CRS units.
 *
 * <p>A point cloud without points has degenerate bounds where minimum and maximum are equal.
 */
public record PointCloudBounds(
    double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
  public PointCloudBounds {
    for (double v : new double[] {minX, minY, minZ, maxX, maxY, maxZ})
      if (!Double.isFinite(v)) throw new IllegalArgumentException("Non-finite point cloud bounds");
    if (minX > maxX || minY > maxY || minZ > maxZ)
      throw new IllegalArgumentException("Invalid point cloud bounds");
  }

  public static PointCloudBounds of(
      double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
    return new PointCloudBounds(minX, minY, minZ, maxX, maxY, maxZ);
  }

  public boolean empty() {
    return minX == maxX && minY == maxY && minZ == maxZ;
  }
}
