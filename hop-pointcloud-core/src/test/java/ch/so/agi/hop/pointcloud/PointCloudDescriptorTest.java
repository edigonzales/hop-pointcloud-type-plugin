package ch.so.agi.hop.pointcloud;

import static org.assertj.core.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;

class PointCloudDescriptorTest {

  private static final PointCloudBounds BOUNDS = new PointCloudBounds(0, 0, 0, 10, 10, 5);
  private static final List<PointDimension> DIMENSIONS =
      List.of(
          PointDimension.of("X", PointDataType.FLOAT64),
          PointDimension.of("Y", PointDataType.FLOAT64),
          PointDimension.of("Z", PointDataType.FLOAT64),
          PointDimension.of("Intensity", PointDataType.UINT16));

  @Test
  void keepsSchemaAndDerivesValues() {
    var descriptor = new PointCloudDescriptor("WKT", "EPSG:2056", 42, BOUNDS, DIMENSIONS);

    assertThat(descriptor.hasPointCount()).isTrue();
    assertThat(descriptor.withPointCount(7).pointCount()).isEqualTo(7);
    assertThat(descriptor.withPointCount(7).dimensions()).isEqualTo(DIMENSIONS);
    assertThat(descriptor.withBounds(new PointCloudBounds(1, 1, 1, 2, 2, 2)).bounds())
        .isEqualTo(new PointCloudBounds(1, 1, 1, 2, 2, 2));
    assertThat(descriptor.withCrs("OTHER", "EPSG:21781").authority()).isEqualTo("EPSG:21781");
    assertThat(descriptor.withDimensions(DIMENSIONS.subList(0, 3)).dimensions()).hasSize(3);
    assertThat(descriptor.toString()).contains("EPSG:2056").contains("42 points");
  }

  @Test
  void supportsUnknownPointCount() {
    var descriptor =
        new PointCloudDescriptor(null, null, PointCloudDescriptor.UNKNOWN_POINT_COUNT, BOUNDS, DIMENSIONS);
    assertThat(descriptor.hasPointCount()).isFalse();
    assertThat(descriptor.toString()).contains("unknown points").contains("unknown CRS");
  }

  @Test
  void rejectsInvalidDescriptors() {
    assertThatThrownBy(
            () -> new PointCloudDescriptor(null, null, -2, BOUNDS, DIMENSIONS))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("point count");
    assertThatThrownBy(() -> new PointCloudDescriptor(null, null, 0, BOUNDS, List.of()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("dimension");
    assertThatThrownBy(
            () ->
                new PointCloudDescriptor(
                    null,
                    null,
                    0,
                    BOUNDS,
                    List.of(
                        PointDimension.of("X", PointDataType.FLOAT64),
                        PointDimension.of("X", PointDataType.FLOAT32))))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Duplicate");
  }

  @Test
  void validatesBounds() {
    assertThat(new PointCloudBounds(0, 0, 0, 0, 0, 0).empty()).isTrue();
    assertThat(new PointCloudBounds(0, 0, 0, 1, 1, 1).empty()).isFalse();
    assertThatThrownBy(() -> new PointCloudBounds(1, 0, 0, 0, 1, 1))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new PointCloudBounds(0, 0, 0, Double.NaN, 1, 1))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void validatesDimensionNames() {
    assertThat(PointDimension.of("HeightAboveGround", PointDataType.FLOAT32).toString())
        .isEqualTo("HeightAboveGround FLOAT32");
    assertThatThrownBy(() -> PointDimension.of("", PointDataType.FLOAT32))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> PointDimension.of("1X", PointDataType.FLOAT32))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> PointDimension.of("X Y", PointDataType.FLOAT32))
        .isInstanceOf(IllegalArgumentException.class);
    assertThat(PointDataType.FLOAT64.isFloatingPoint()).isTrue();
    assertThat(PointDataType.UINT8.isFloatingPoint()).isFalse();
  }
}
