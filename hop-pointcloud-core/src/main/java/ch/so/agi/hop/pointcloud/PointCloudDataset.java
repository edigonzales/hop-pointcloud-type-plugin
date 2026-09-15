package ch.so.agi.hop.pointcloud;

import java.util.Objects;

/**
 * Immutable Hop value: a referenced point cloud dataset with its logical schema and pipeline plan.
 *
 * <p>Appending a stage returns a new dataset; existing instances never change. The value contains
 * no open files and no native handles, so it can be cloned, compared, serialized and passed
 * between transforms and pipeline branches cheaply. Readers and writers materialize data; the
 * dataset itself is descriptive.
 */
public record PointCloudDataset(
    PointCloudReference source, PointCloudDescriptor descriptor, PdalPlan plan) {
  public PointCloudDataset {
    Objects.requireNonNull(source);
    Objects.requireNonNull(descriptor);
    Objects.requireNonNull(plan);
    if (plan.isEmpty())
      throw new IllegalArgumentException("Point cloud plan must contain at least the reader stage");
  }

  public static PointCloudDataset of(
      PointCloudReference source, PointCloudDescriptor descriptor, PdalPlan plan) {
    return new PointCloudDataset(source, descriptor, plan);
  }

  /** Appends one stage and replaces the logical schema with the schema after that stage. */
  public PointCloudDataset append(PdalStage stage, PointCloudDescriptor result) {
    Objects.requireNonNull(stage);
    Objects.requireNonNull(result);
    return new PointCloudDataset(source, result, plan.append(stage));
  }

  /** Appends a complete provider pipeline (for example an imported raw pipeline). */
  public PointCloudDataset append(PdalPlan additional, PointCloudDescriptor result) {
    Objects.requireNonNull(additional);
    Objects.requireNonNull(result);
    return new PointCloudDataset(source, result, plan.append(additional));
  }

  @Override
  public String toString() {
    return "PointCloudDataset["
        + source.location()
        + ", "
        + descriptor
        + ", "
        + plan.size()
        + " stages]";
  }
}
