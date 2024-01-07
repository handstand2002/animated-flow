package com.brokencircuits.animatedflow.dsl;

import java.time.Duration;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiagramNodeTransformation {

  private String objectId;
  private Duration startTime;
  private Duration endTime;
  private Integer newX;
  private Integer newY;
  private String newLocationReference;

}
