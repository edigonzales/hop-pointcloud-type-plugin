package ch.so.agi.hop.pointcloud;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

class PointCloudReferenceTest {

  @Test
  void normalizesLocalPaths() {
    var reference = new PointCloudReference("data/./tile.laz", null);
    assertThat(reference.location()).endsWith("data/tile.laz");
    assertThat(reference.location()).startsWith("/");
    assertThat(reference.remote()).isFalse();
  }

  @Test
  void keepsPublicHttpUrls() {
    var reference = new PointCloudReference("https://example.org/tile.copc.laz", "etag-1");
    assertThat(reference.location()).isEqualTo("https://example.org/tile.copc.laz");
    assertThat(reference.identity()).isEqualTo("etag-1");
    assertThat(reference.remote()).isTrue();
  }

  @Test
  void rejectsCredentialsInUrls() {
    assertThatThrownBy(() -> new PointCloudReference("https://user:secret@example.org/a.laz", null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Credentials");
  }

  @Test
  void rejectsUnsupportedProtocols() {
    assertThatThrownBy(() -> new PointCloudReference("s3://bucket/a.laz", null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("protocol");
  }

  @Test
  void rejectsBlankLocation() {
    assertThatThrownBy(() -> new PointCloudReference("  ", null))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
