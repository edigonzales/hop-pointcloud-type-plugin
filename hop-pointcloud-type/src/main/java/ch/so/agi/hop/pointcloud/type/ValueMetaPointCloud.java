package ch.so.agi.hop.pointcloud.type;

import ch.so.agi.hop.pointcloud.*;
import java.io.*;
import java.util.Arrays;
import org.apache.hop.core.exception.*;
import org.apache.hop.core.row.IValueMeta;
import org.apache.hop.core.row.value.ValueMetaBase;
import org.apache.hop.core.row.value.ValueMetaPlugin;

@ValueMetaPlugin(
    id = "" + ValueMetaPointCloud.TYPE_POINT_CLOUD,
    name = "Point Cloud",
    description = "A referenced point cloud dataset",
    classLoaderGroup = "sogeo-pointcloud")
public final class ValueMetaPointCloud extends ValueMetaBase {
  public static final int TYPE_POINT_CLOUD = 727838;

  public ValueMetaPointCloud() {
    this(null);
  }

  public ValueMetaPointCloud(String name) {
    super(name, TYPE_POINT_CLOUD);
  }

  @Override
  public Class<?> getNativeDataTypeClass() {
    return PointCloudDataset.class;
  }

  public PointCloudDataset getPointCloud(Object value) throws HopValueException {
    if (value == null || value instanceof PointCloudDataset) return (PointCloudDataset) value;
    throw new HopValueException("Expected a Point Cloud value; use Point Cloud Reader for paths");
  }

  @Override
  public Object cloneValueData(Object value) throws HopValueException {
    return getPointCloud(value);
  }

  @Override
  public String getString(Object value) throws HopValueException {
    return value == null ? null : getPointCloud(value).toString();
  }

  @Override
  public Object getNativeDataType(Object value) throws HopValueException {
    return getPointCloud(value);
  }

  @Override
  public Object convertData(IValueMeta source, Object value) throws HopValueException {
    if (value == null) return null;
    if (source.getType() == TYPE_POINT_CLOUD) return getPointCloud(value);
    throw new HopValueException("Use Point Cloud Reader to construct Point Cloud values");
  }

  @Override
  public byte[] getBinary(Object value) throws HopValueException {
    try {
      return PointCloudCodec.encode(getPointCloud(value));
    } catch (IOException e) {
      throw new HopValueException("Cannot encode point cloud", e);
    }
  }

  @Override
  public int compare(Object a, Object b) throws HopValueException {
    if (a == b) return 0;
    if (a == null) return -1;
    if (b == null) return 1;
    int comparison = Arrays.compareUnsigned(getBinary(a), getBinary(b));
    return isSortedDescending() ? -comparison : comparison;
  }

  @Override
  public int hashCode(Object value) throws HopValueException {
    return value == null ? 0 : Arrays.hashCode(getBinary(value));
  }

  @Override
  public void writeData(DataOutputStream out, Object value) throws HopFileException {
    try {
      byte[] bytes = PointCloudCodec.encode(getPointCloud(value));
      out.writeInt(bytes.length);
      out.write(bytes);
    } catch (Exception e) {
      throw new HopFileException("Cannot write Point Cloud value", e);
    }
  }

  @Override
  public Object readData(DataInputStream in) throws HopFileException {
    try {
      int n = in.readInt();
      if (n < 0 || n > PointCloudCodec.MAX_BYTES) throw new IOException("Invalid Point Cloud length");
      byte[] bytes = new byte[n];
      in.readFully(bytes);
      return PointCloudCodec.decode(bytes);
    } catch (Exception e) {
      throw new HopFileException("Cannot read Point Cloud value", e);
    }
  }
}
