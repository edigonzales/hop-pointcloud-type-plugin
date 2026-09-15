package ch.so.agi.hop.pointcloud;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Explicit bounded wire format; never invokes Java object deserialization. */
public final class PointCloudCodec {
  public static final int MAX_BYTES = 16 * 1024 * 1024;

  private static final int MAGIC = 0x50434431;

  private PointCloudCodec() {}

  public static byte[] encode(PointCloudDataset value) throws IOException {
    var bytes = new ByteArrayOutputStream();
    try (var out =
        new DataOutputStream(
            new FilterOutputStream(bytes) {
              private int written;

              private void reserve(int count) throws IOException {
                if (count > MAX_BYTES - written)
                  throw new IOException("Point cloud value exceeds size limit");
                written += count;
              }

              @Override
              public void write(int b) throws IOException {
                reserve(1);
                out.write(b);
              }

              @Override
              public void write(byte[] b, int off, int len) throws IOException {
                reserve(len);
                out.write(b, off, len);
              }
            })) {
      out.writeInt(MAGIC);
      out.writeBoolean(value != null);
      if (value != null) {
        text(out, value.source().location());
        text(out, value.source().identity());
        descriptor(out, value.descriptor());
        out.writeInt(value.plan().size());
        for (var stage : value.plan().stages()) {
          text(out, stage.type());
          out.writeInt(stage.options().size());
          for (var option : stage.options().entrySet()) {
            text(out, option.getKey());
            text(out, option.getValue());
          }
          if (bytes.size() > MAX_BYTES) throw new IOException("Point cloud value exceeds size limit");
        }
      }
    }
    if (bytes.size() > MAX_BYTES) throw new IOException("Point cloud value exceeds size limit");
    return bytes.toByteArray();
  }

  public static PointCloudDataset decode(byte[] bytes) throws IOException {
    if (bytes.length > MAX_BYTES) throw new IOException("Point cloud value exceeds size limit");
    try (var in = new DataInputStream(new ByteArrayInputStream(bytes))) {
      if (in.readInt() != MAGIC) throw new IOException("Unsupported point cloud wire version");
      PointCloudDataset result = null;
      if (in.readBoolean()) {
        var reference = new PointCloudReference(text(in), text(in));
        var descriptor = descriptor(in);
        int stageCount = count(in, PdalPlan.MAX_STAGES);
        var stages = new ArrayList<PdalStage>();
        for (int i = 0; i < stageCount; i++) {
          String type = text(in);
          int optionCount = count(in, 64);
          var options = new LinkedHashMap<String, String>();
          for (int o = 0; o < optionCount; o++) options.put(text(in), text(in));
          stages.add(new PdalStage(type, options));
        }
        result = new PointCloudDataset(reference, descriptor, new PdalPlan(stages));
      }
      if (in.available() != 0) throw new IOException("Trailing point cloud data");
      return result;
    } catch (IllegalArgumentException | NullPointerException e) {
      throw new IOException("Invalid point cloud value", e);
    }
  }

  private static void descriptor(DataOutputStream o, PointCloudDescriptor d) throws IOException {
    text(o, d.crsWkt());
    text(o, d.authority());
    o.writeLong(d.pointCount());
    var b = d.bounds();
    for (double v : new double[] {b.minX(), b.minY(), b.minZ(), b.maxX(), b.maxY(), b.maxZ()})
      o.writeDouble(v);
    o.writeInt(d.dimensions().size());
    for (var dimension : d.dimensions()) {
      text(o, dimension.name());
      o.writeByte(dimension.type().ordinal());
    }
  }

  private static PointCloudDescriptor descriptor(DataInputStream i) throws IOException {
    String crsWkt = text(i);
    String authority = text(i);
    long pointCount = i.readLong();
    var bounds =
        new PointCloudBounds(
            i.readDouble(),
            i.readDouble(),
            i.readDouble(),
            i.readDouble(),
            i.readDouble(),
            i.readDouble());
    int dimensionCount = count(i, 4096);
    var dimensions = new ArrayList<PointDimension>();
    var types = PointDataType.values();
    for (int d = 0; d < dimensionCount; d++) {
      String name = text(i);
      int ordinal = i.readUnsignedByte();
      if (ordinal >= types.length) throw new IOException("Unknown point data type");
      dimensions.add(new PointDimension(name, types[ordinal]));
    }
    return new PointCloudDescriptor(crsWkt, authority, pointCount, bounds, dimensions);
  }

  private static int count(DataInputStream in, int max) throws IOException {
    int n = in.readInt();
    if (n < 0 || n > max) throw new IOException("Invalid point cloud collection length");
    return n;
  }

  private static void text(DataOutputStream o, String s) throws IOException {
    if (s == null) {
      o.writeInt(-1);
      return;
    }
    byte[] b = s.getBytes(StandardCharsets.UTF_8);
    if (b.length > MAX_BYTES) throw new IOException("Point cloud text exceeds limit");
    o.writeInt(b.length);
    o.write(b);
  }

  private static String text(DataInputStream i) throws IOException {
    int n = i.readInt();
    if (n == -1) return null;
    if (n < 0 || n > MAX_BYTES || n > i.available())
      throw new IOException("Invalid point cloud string length");
    byte[] b = i.readNBytes(n);
    return StandardCharsets.UTF_8.newDecoder().decode(java.nio.ByteBuffer.wrap(b)).toString();
  }
}
