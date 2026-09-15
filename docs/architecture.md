# Point cloud value contract

## Data model

`PointCloudDataset` is an immutable value: a normalized `PointCloudReference` (source), a
`PointCloudDescriptor` (logical schema) and a `PdalPlan` (provider pipeline). Appending a stage or
a complete plan returns a new dataset and replaces the descriptor with the schema after that stage.
Existing instances never change, so values can be cloned, compared and shared between transforms
and pipeline branches for free.

`PointCloudDescriptor` carries the CRS (well-known text plus optional authority code such as
`EPSG:2056`), the point count (`-1` = unknown), axis aligned bounds and the ordered list of
`PointDimension` entries (name plus `PointDataType`). Dimension names are unique and limited to
letters, digits and underscores; at least one dimension is required.

`PdalPlan` is plain data - an ordered list of `PdalStage(type, options)`. Stage types look like
`readers.las`, `filters.crop` or `writers.copc`; options are plain text in insertion order. A plan
starts with the reader stage of the source, filter stages are appended by transforms and the writer
stage is appended by the consuming writer transform. Rendering a plan as provider pipeline JSON is
the job of the execution backend, which knows the provider semantics. V1 limits a plan to 128
stages and a stage to 64 options; option values are limited to 64 KiB each.

## Transport

`PointCloudCodec` writes a versioned, explicit binary format (16 MiB per value) and never uses Java
object deserialization. It preserves source reference, descriptor and the complete plan. Values
without a dataset are supported (a single flag byte). Comparisons and hashes operate on this
encoded form: they describe reference, schema and plan - not point content. Long operations chains
that exceed a limit fail while planning, not while writing.

## Execution

`PointCloudBackend` is the provider SPI:

```java
PointCloudDescriptor describe(String location) throws Exception;
PointCloudExecution execute(PdalPlan plan, BooleanSupplier stopped) throws Exception;
```

`describe` returns the schema of a source without materializing points. `execute` runs a complete
pipeline (reader ... writer) and returns `PointCloudExecution` (point count, metadata JSON, log) -
a plain value without native handles. A backend instance belongs to one transform copy and is not
shared between threads. Implementations live outside this repository
(`hop-pdal-plugin` uses `pdal-ffm-core` with a bundled PDAL runtime).

Opening point data for actual coordinate access is deliberately not part of V1. A later
`PointCloudReader`/`PointBlock` extension will return read-only, block-wise views instead of
copying whole point clouds into the JVM heap.

## Hop integration

`ValueMetaPointCloud` (type id `727838`) extends `ValueMetaBase` and registers with
`@ValueMetaPlugin` in the shared class loader group `sogeo-pointcloud`. The Jandex index is
generated at build time; Hop discovers annotated value types only in plugin JARs that contain
`META-INF/jandex.idx`.

The plugin ZIP contains exactly two JARs: the annotated type JAR in the plugin root and the shared
model JAR in `lib/`. The model JAR must exist in exactly one plugin directory - consuming transform
plugins declare this plugin and its `lib` folder in their `dependencies.xml` instead of copying the
JARs. This keeps `PointCloudDataset` a single class identity across the class loader groups of Hop.
