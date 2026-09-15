package ch.so.agi.hop.pointcloud;

import static org.assertj.core.api.Assertions.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class PdalStageTest {

  @Test
  void keepsOptionOrderAndValues() {
    var options = new LinkedHashMap<String, String>();
    options.put("filename", "/data/tile.laz");
    options.put("count", "1000");
    var stage = PdalStage.of("readers.las", options);

    assertThat(stage.type()).isEqualTo("readers.las");
    assertThat(stage.options()).containsExactlyEntriesOf(options);
    assertThat(stage.option("filename")).isEqualTo("/data/tile.laz");
    assertThat(stage.option("missing")).isNull();
    assertThat(stage.toString()).contains("readers.las");
  }

  @Test
  void validatesTypes() {
    assertThatThrownBy(() -> PdalStage.of("readers", Map.of()))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> PdalStage.of("Readers.LAS", Map.of()))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> PdalStage.of("filters.crop", Map.of("bad-name", "1")))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> PdalStage.of("filters.crop", Map.of("bounds", null)))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void enforcesStageLimits() {
    assertThatThrownBy(
            () -> {
              var plan = PdalPlan.of();
              for (int i = 0; i < PdalPlan.MAX_STAGES + 1; i++) plan = plan.append(PdalStage.of("filters.crop"));
            })
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("128");
  }

  @Test
  void plansAreImmutable() {
    var plan = PdalPlan.of(PdalStage.of("readers.las"));
    var extended = plan.append(PdalStage.of("filters.crop"));

    assertThat(plan.size()).isEqualTo(1);
    assertThat(extended.size()).isEqualTo(2);
    assertThat(extended.isEmpty()).isFalse();
    assertThat(PdalPlan.empty().isEmpty()).isTrue();
  }

  @Test
  void appendsCompletePlans() {
    var left = PdalPlan.of(PdalStage.of("readers.las"));
    var right = PdalPlan.of(PdalStage.of("filters.crop"), PdalStage.of("writers.laz"));

    assertThat(left.append(right).size()).isEqualTo(3);
    assertThat(left.size()).isEqualTo(1);
  }
}
