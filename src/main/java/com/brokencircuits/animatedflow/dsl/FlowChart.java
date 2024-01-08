package com.brokencircuits.animatedflow.dsl;

import java.time.Duration;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import lombok.Data;

@Data
public class FlowChart implements Diagram {

  private String backgroundColor = "WHITE";
  private int width = 200;
  private int height = 200;
  private boolean loop = true;
  private int fps = 25;
  private Duration totalLength;
  private Duration lastFrameDuration = Duration.ofSeconds(1);
  private Collection<DiagramNode> items = new LinkedList<>();
  private Collection<DiagramNodeTransformation> transforms = new LinkedList<>();

  /*
  items:
    - type: grid
      id: t1
      x: 0
      y: 0
      span-width: 20
      span-height: 20
    - type: rectangle
      id: o1
      location-reference: t1[0][0]
    - type: text
      location-reference: o1

   */

  public Duration getTotalLength() {
    if (totalLength == null) {
      totalLength = transforms.stream()
          .map(DiagramNodeTransformation::getEndTime)
          .max(Comparator.comparingLong(Duration::toMillis)).orElse(Duration.ZERO);
      totalLength = totalLength.plus(lastFrameDuration);
    }
    return totalLength;
  }
}
