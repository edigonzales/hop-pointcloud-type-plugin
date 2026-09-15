package ch.so.agi.hop.pointcloud;

/**
 * Immutable reference to a point cloud source.
 *
 * <p>Local paths are normalized to absolute paths; remote references must be public HTTP(S) URLs.
 * The optional {@code identity} carries provider-specific change detection information (for
 * example file size and modification time); it never contains credentials.
 */
public record PointCloudReference(String location, String identity) {
  public PointCloudReference {
    java.util.Objects.requireNonNull(location);
    if (location.isBlank()) throw new IllegalArgumentException("Point cloud source is required");
    if (location.startsWith("http://") || location.startsWith("https://")) {
      var uri = java.net.URI.create(location);
      if (uri.getUserInfo() != null)
        throw new IllegalArgumentException("Credentials in point cloud URLs are unsupported");
    } else {
      if (location.contains("://")) throw new IllegalArgumentException("Unsupported point cloud protocol");
      location = java.nio.file.Path.of(location).toAbsolutePath().normalize().toString();
    }
  }

  public boolean remote() {
    return location.startsWith("http://") || location.startsWith("https://");
  }
}
