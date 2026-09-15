package ch.so.agi.hop.pointcloud;

/**
 * Execution context for point cloud data.
 *
 * <p>Implementations live in separate repositories (for example a PDAL based backend using
 * {@code pdal-ffm-core}) so that the Hop value model and its type plugin stay free of native
 * runtime dependencies. A backend instance belongs to one consuming transform copy and is not
 * shared between threads or pipeline branches.
 */
public interface PointCloudBackend {
  /** Reads the logical schema of a point cloud source. */
  PointCloudDescriptor describe(String location) throws Exception;

  /**
   * Executes the given pipeline ({@code reader ... writer}) and returns execution information.
   *
   * @param plan complete provider pipeline including reader and writer stages
   * @param stopped polled by long running implementations; may be {@code null}
   */
  PointCloudExecution execute(PdalPlan plan, java.util.function.BooleanSupplier stopped)
      throws Exception;
}
