package ch.so.agi.hop.pointcloud;

/**
 * Storage type of a single point dimension.
 *
 * <p>The set mirrors the provider dimension types (signed/unsigned integers of 8, 16, 32 and 64
 * bit, 32 and 64 bit floating point) without depending on a provider library.
 */
public enum PointDataType {
  INT8,
  UINT8,
  INT16,
  UINT16,
  INT32,
  UINT32,
  INT64,
  UINT64,
  FLOAT32,
  FLOAT64;

  public boolean isFloatingPoint() {
    return this == FLOAT32 || this == FLOAT64;
  }
}
