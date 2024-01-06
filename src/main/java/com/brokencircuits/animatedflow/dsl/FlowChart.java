package com.brokencircuits.animatedflow.dsl;

import java.time.Duration;
import java.util.Collection;
import java.util.Comparator;
import lombok.Data;

@Data
public class FlowChart implements Diagram {

  private String backgroundColor = "BLACK";
  private int width = 200;
  private int height = 200;
  private boolean loop = true;
  private int fps = 25;
  private Duration totalLength;
  private Duration lastFrameDuration = Duration.ofSeconds(1);
  private Collection<DiagramNode> items;
  private Collection<DiagramNodeTransformation> transforms;

  public Duration getTotalLength() {
    if (totalLength == null) {
      totalLength = transforms.stream().map(DiagramNodeTransformation::getEndTime).max(
          Comparator.comparingLong(Duration::toMillis)).orElse(Duration.ZERO);
      totalLength = totalLength.plus(lastFrameDuration);
    }
    return totalLength;
  }
}
