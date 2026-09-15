package ch.so.agi.hop.pointcloud;

/**
 * Result of executing a point cloud pipeline.
 *
 * <p>Plain immutable value without native handles: point count, provider metadata JSON and the
 * captured execution log.
 */
public record PointCloudExecution(long pointCount, String metadataJson, String log) {
  public PointCloudExecution {
    if (pointCount < 0) throw new IllegalArgumentException("pointCount must not be negative");
    metadataJson = metadataJson == null ? "" : metadataJson;
    log = log == null ? "" : log;
  }
}
