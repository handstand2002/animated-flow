package com.brokencircuits.animatedflow.evaluator;

import java.awt.Graphics;
import lombok.Builder;
import lombok.Value;

@Builder(toBuilder = true)
@Value
public class EvaluationContext {

  LocationResolver locationResolver;
  Graphics graphics;

  public Graphics getGraphics() {
    if (graphics == null) {
      throw new IllegalStateException("Graphics are not accessible in this context");
    }
    return graphics;
  }

  public void clearFrameState() {
    locationResolver.clearFrameState();
  }
}
