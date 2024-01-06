package com.brokencircuits.animatedflow;

import java.awt.image.BufferedImage;
import java.time.Duration;
import lombok.Value;

@Value
public class DiagramFrame {

  BufferedImage image;
  Duration duration;
}
