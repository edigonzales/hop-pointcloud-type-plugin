# hop-pointcloud-type-plugin

Apache Hop **Point Cloud value type**: a backend-neutral data model for referenced point cloud
datasets, with no dependency on PDAL or any other native runtime.

The plugin provides the Hop value `PointCloudDataset` and the `ValueMetaPointCloud` type so that
point cloud references, schemas and processing plans can flow through Hop pipelines like any other
value. Execution is provided by a separate backend plugin
([hop-pdal-plugin](https://github.com/edigonzales/hop-pdal-plugin), based on
[pdal-java-bindings](https://github.com/edigonzales/pdal-java-bindings)).

## Modules

| Module | Description |
| --- | --- |
| `hop-pointcloud-core` | Backend-neutral model: `PointCloudDataset`, `PointCloudDescriptor`, `PointDimension`, `PdalPlan`, `PointCloudCodec`, `PointCloudBackend` SPI |
| `hop-pointcloud-type` | `ValueMetaPointCloud` registered as Hop value type (`sogeo-pointcloud` class loader group) |
| `assemblies` | Builds the installable `hop-pointcloud-type-plugin-*.zip` |

## Requirements

- JDK 21+ (the plugin builds with release 21 and runs on Java 21 and 25)
- Apache Hop 2.19.0
- Maven 3.9+

## Build

```sh
mvn -B -ntp clean verify
python scripts/verify-package.py
```

The assembly is written to
`assemblies/target/hop-pointcloud-type-plugin-0.1.0-SNAPSHOT.zip` and contains exactly two JARs:

```
plugins/misc/hop-pointcloud-type/hop-pointcloud-type-0.1.0-SNAPSHOT.jar
plugins/misc/hop-pointcloud-type/lib/hop-pointcloud-core-0.1.0-SNAPSHOT.jar
```

## Installation

Extract the ZIP into the Hop client directory (the archive already contains the `plugins/` path):

```sh
unzip hop-pointcloud-type-plugin-0.1.0-SNAPSHOT.zip -d /opt/hop
```

The shared model must exist in exactly one plugin directory; transform plugins declare
`plugins/misc/hop-pointcloud-type` and its `lib` folder through their `dependencies.xml` instead of
copying the JARs.

## Model

```java
PointCloudDataset dataset = PointCloudDataset.of(
    new PointCloudReference("/data/tile.laz", null),
    new PointCloudDescriptor(
        crsWkt, "EPSG:2056", 125_391_223,
        new PointCloudBounds(2600000, 1200000, 400, 2601000, 1201000, 900),
        List.of(PointDimension.of("X", PointDataType.FLOAT64), ...)),
    PdalPlan.of(PdalStage.of("readers.las", Map.of("filename", "/data/tile.laz"))));

PointCloudDataset cropped = dataset.append(
    PdalStage.of("filters.crop", Map.of("bounds", "([2600000,2600500],[1200000,1200500])")),
    dataset.descriptor().withPointCount(-1));
```

Values are immutable, contain no native handles and are serialized with an explicit bounded wire
format (`PointCloudCodec`, 16 MiB value limit, at most 128 stages). Comparisons and hashes describe
the reference, schema and plan - never point equality. See `docs/architecture.md` for the full
contract.

## Related repositories

- `pdal-java-bindings`: FFM bindings and bundled PDAL runtime (`Pdal.execute`)
- `hop-pdal-plugin`: Hop transforms (reader, writer, info, crop, reproject, filters) that implement
  `PointCloudBackend` and fuse the plan of consecutive transforms into a single PDAL pipeline

## Roadmap

- Point cloud block access (`PointBlock`) for transforms that need actual coordinates
- Statistics/info transforms on top of the descriptor
- Optional Swiss PROJ grid subset for the native runtime (see `pdal-java-bindings`)
