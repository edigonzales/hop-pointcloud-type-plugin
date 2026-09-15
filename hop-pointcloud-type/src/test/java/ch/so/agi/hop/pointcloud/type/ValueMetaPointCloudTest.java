package ch.so.agi.hop.pointcloud.type;

import static org.assertj.core.api.Assertions.*;

import ch.so.agi.hop.pointcloud.*;
import java.io.*;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ValueMetaPointCloudTest {
  @Test
  void streamCloneComparisonAndInvalidConversion() throws Exception {
    var value =
        PointCloudDataset.of(
            new PointCloudReference("/data/input.laz", null),
            new PointCloudDescriptor(
                null,
                "EPSG:2056",
                1000,
                new PointCloudBounds(0, 0, 0, 10, 10, 5),
                List.of(
                    PointDimension.of("X", PointDataType.FLOAT64),
                    PointDimension.of("Y", PointDataType.FLOAT64),
                    PointDimension.of("Z", PointDataType.FLOAT64))),
            PdalPlan.of(PdalStage.of("readers.las", Map.of("filename", "/data/input.laz"))));
    var meta = new ValueMetaPointCloud("pointcloud");

    var bytes = new ByteArrayOutputStream();
    meta.writeData(new DataOutputStream(bytes), value);
    var decoded = meta.readData(new DataInputStream(new ByteArrayInputStream(bytes.toByteArray())));

    assertThat(decoded).isEqualTo(value);
    assertThat(meta.compare(value, decoded)).isZero();
    assertThat(meta.hashCode(value)).isEqualTo(meta.hashCode(decoded));
    assertThat(meta.cloneValueData(value)).isSameAs(value);
    assertThat(meta.getNativeDataTypeClass()).isEqualTo(PointCloudDataset.class);
    assertThat(meta.getString(value)).contains("input.laz");

    var other =
        PointCloudDataset.of(
            new PointCloudReference("/data/other.laz", null),
            value.descriptor(),
            PdalPlan.of(PdalStage.of("readers.las")));
    assertThat(meta.compare(value, other)).isNegative();
    meta.setSortedDescending(true);
    assertThat(meta.compare(value, other)).isPositive();

    assertThat(meta.convertData(meta, value)).isSameAs(value);
    assertThat(meta.convertData(meta, null)).isNull();
    assertThatThrownBy(() -> meta.getPointCloud("input.laz"))
        .hasMessageContaining("Point Cloud Reader");
    assertThatThrownBy(() -> meta.convertData(new ValueMetaPointCloud(), "input.laz"))
        .isInstanceOf(org.apache.hop.core.exception.HopValueException.class);
  }
}
