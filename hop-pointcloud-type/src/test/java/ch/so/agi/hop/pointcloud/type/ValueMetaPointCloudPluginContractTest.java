package ch.so.agi.hop.pointcloud.type;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.hop.core.row.value.ValueMetaPlugin;
import org.junit.jupiter.api.Test;

class ValueMetaPointCloudPluginContractTest {

  @Test
  void shouldUseSharedPointCloudClassLoaderGroup() {
    ValueMetaPlugin plugin = ValueMetaPointCloud.class.getAnnotation(ValueMetaPlugin.class);

    assertThat(plugin).isNotNull();
    assertThat(plugin.id()).isEqualTo(String.valueOf(ValueMetaPointCloud.TYPE_POINT_CLOUD));
    assertThat(plugin.classLoaderGroup()).isEqualTo("sogeo-pointcloud");
  }
}
