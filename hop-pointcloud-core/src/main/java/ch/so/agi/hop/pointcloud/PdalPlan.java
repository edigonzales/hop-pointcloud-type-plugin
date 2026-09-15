package ch.so.agi.hop.pointcloud;

import java.util.ArrayList;
import java.util.List;

/**
 * Ordered, immutable provider pipeline of a point cloud dataset.
 *
 * <p>The plan starts with the reader stage of the source and contains every filter stage applied
 * by Hop transforms. A writer stage is appended by the consuming writer transform. The plan is
 * plain data: it does not know how to execute itself.
 */
public record PdalPlan(List<PdalStage> stages) {
  public static final int MAX_STAGES = 128;

  public PdalPlan {
    stages = List.copyOf(stages);
    if (stages.size() > MAX_STAGES)
      throw new IllegalArgumentException("Point cloud pipeline exceeds " + MAX_STAGES + " stages");
  }

  public static PdalPlan empty() {
    return new PdalPlan(List.of());
  }

  public static PdalPlan of(PdalStage... stages) {
    return new PdalPlan(List.of(stages));
  }

  public PdalPlan append(PdalStage stage) {
    if (stages.size() >= MAX_STAGES)
      throw new IllegalArgumentException("Point cloud pipeline exceeds " + MAX_STAGES + " stages");
    var next = new ArrayList<>(stages);
    next.add(stage);
    return new PdalPlan(next);
  }

  public PdalPlan append(PdalPlan other) {
    if (stages.size() + other.stages.size() > MAX_STAGES)
      throw new IllegalArgumentException("Point cloud pipeline exceeds " + MAX_STAGES + " stages");
    var next = new ArrayList<>(stages);
    next.addAll(other.stages);
    return new PdalPlan(next);
  }

  public boolean isEmpty() {
    return stages.isEmpty();
  }

  public int size() {
    return stages.size();
  }

  @Override
  public String toString() {
    return stages.toString();
  }
}
