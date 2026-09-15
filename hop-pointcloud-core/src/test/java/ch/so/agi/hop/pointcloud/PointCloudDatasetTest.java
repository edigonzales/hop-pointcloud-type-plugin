package ch.so.agi.hop.pointcloud;

import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class PointCloudDatasetTest {

  private static final PointCloudDescriptor DESCRIPTOR =
      new PointCloudDescriptor(
          null,
          "EPSG:2056",
          100,
          new PointCloudBounds(0, 0, 0, 10, 10, 5),
          List.of(
              PointDimension.of("X", PointDataType.FLOAT64),
              PointDimension.of("Y", PointDataType.FLOAT64),
              PointDimension.of("Z", PointDataType.FLOAT64)));

  @Test
  void appendsStagesWithoutMutating() {
    var dataset =
        PointCloudDataset.of(
            new PointCloudReference("/data/tile.laz", null),
            DESCRIPTOR,
            PdalPlan.of(PdalStage.of("readers.las", Map.of("filename", "/data/tile.laz"))));

    var cropped =
        dataset.append(
            PdalStage.of("filters.crop", Map.of("bounds", "([0,5],[0,5],[0,5])")),
            DESCRIPTOR.withPointCount(50));

    assertThat(dataset.plan().size()).isEqualTo(1);
    assertThat(dataset.descriptor().pointCount()).isEqualTo(100);
    assertThat(cropped.plan().size()).isEqualTo(2);
    assertThat(cropped.descriptor().pointCount()).isEqualTo(50);
    assertThat(cropped.source()).isEqualTo(dataset.source());
    assertThat(cropped.plan().stages().get(1).type()).isEqualTo("filters.crop");
  }

  @Test
  void appendsCompletePlans() {
    var dataset =
        PointCloudDataset.of(
            new PointCloudReference("/data/tile.laz", null),
            DESCRIPTOR,
            PdalPlan.of(PdalStage.of("readers.las")));

    var result =
        dataset.append(
            PdalPlan.of(PdalStage.of("filters.reprojection"), PdalStage.of("writers.laz")),
            DESCRIPTOR);

    assertThat(result.plan().size()).isEqualTo(3);
    assertThat(result.toString()).contains("tile.laz").contains("3 stages");
  }

  @Test
  void requiresReaderStage() {
    assertThatThrownBy(
            () ->
                PointCloudDataset.of(
                    new PointCloudReference("/data/tile.laz", null),
                    DESCRIPTOR,
                    PdalPlan.empty()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("reader");
  }
}
