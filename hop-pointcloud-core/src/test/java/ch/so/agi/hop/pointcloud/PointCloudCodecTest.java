package ch.so.agi.hop.pointcloud;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import org.junit.jupiter.api.Test;

class PointCloudCodecTest {

  private static PointCloudDataset dataset() {
    var readerOptions = new LinkedHashMap<String, String>();
    readerOptions.put("filename", "/data/tile.laz");
    readerOptions.put("extra_dims", "all");

    return PointCloudDataset.of(
        new PointCloudReference("/data/tile.laz", "mtime:12345"),
        new PointCloudDescriptor(
            "PROJCS[...]",
            "EPSG:2056",
            125391223,
            new PointCloudBounds(2600000, 1200000, 400, 2601000, 1201000, 900),
            List.of(
                PointDimension.of("X", PointDataType.FLOAT64),
                PointDimension.of("Y", PointDataType.FLOAT64),
                PointDimension.of("Z", PointDataType.FLOAT64),
                PointDimension.of("Intensity", PointDataType.UINT16),
                PointDimension.of("ReturnNumber", PointDataType.UINT8))),
        PdalPlan.of(
            PdalStage.of("readers.las", readerOptions),
            PdalStage.of("filters.crop", java.util.Map.of("bounds", "([0,100],[0,100])")),
            PdalStage.of("filters.smrf", java.util.Map.of("scalar", "1.25"))));
  }

  @Test
  void roundTripsDataset() throws IOException {
    var value = dataset();
    var decoded = PointCloudCodec.decode(PointCloudCodec.encode(value));

    assertThat(decoded).isEqualTo(value);
    assertThat(decoded.plan().stages().get(0).option("extra_dims")).isEqualTo("all");
    assertThat(decoded.descriptor().dimensions()).hasSize(5);
  }

  @Test
  void roundTripsNull() throws IOException {
    assertThat(PointCloudCodec.decode(PointCloudCodec.encode(null))).isNull();
  }

  @Test
  void rejectsForeignPayloads() {
    assertThatThrownBy(() -> PointCloudCodec.decode(new byte[] {1, 2, 3, 4}))
        .isInstanceOf(IOException.class)
        .hasMessageContaining("wire version");

    assertThatThrownBy(() -> PointCloudCodec.decode(withTrailingByte(dataset())))
        .isInstanceOf(IOException.class)
        .hasMessageContaining("Trailing");

    assertThatThrownBy(() -> PointCloudCodec.decode(new byte[PointCloudCodec.MAX_BYTES + 1]))
        .isInstanceOf(IOException.class)
        .hasMessageContaining("size limit");
  }

  private static byte[] withTrailingByte(PointCloudDataset value) throws IOException {
    var encoded = PointCloudCodec.encode(value);
    return java.util.Arrays.copyOf(encoded, encoded.length + 1);
  }

  @Test
  void rejectsTruncatedPayloads() throws IOException {
    var encoded = PointCloudCodec.encode(dataset());
    var truncated = java.util.Arrays.copyOf(encoded, encoded.length / 2);
    assertThatThrownBy(() -> PointCloudCodec.decode(truncated)).isInstanceOf(IOException.class);
  }

  @Test
  void failsOnUnsupportedWireVersion() {
    byte[] wrongMagic = new byte[8];
    wrongMagic[3] = (byte) 0x99;
    assertThatThrownBy(() -> PointCloudCodec.decode(wrongMagic))
        .isInstanceOf(IOException.class)
        .hasMessageContaining("wire version");
  }
}
