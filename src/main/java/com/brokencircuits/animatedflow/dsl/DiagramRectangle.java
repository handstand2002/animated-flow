package com.brokencircuits.animatedflow.dsl;


import com.brokencircuits.animatedflow.ColorUtil;
import com.brokencircuits.animatedflow.evaluator.EvaluationContext;
import java.awt.Color;
import java.awt.Graphics;
import java.time.Duration;
import java.util.Collection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiagramRectangle implements DiagramNode {

  private String id;
  private int height;
  private int width;
  private Integer x;
  private Integer y;
  private String locationReference;

  @Default
  private String fillColor = "BLACK";
  @Default
  private String outlineColor = "BLACK";

  @Override
  public void draw(Duration atTime,
      Collection<DiagramNodeTransformation> applicableTransformations,
      EvaluationContext ctx) {

    Coordinates newCoordinates = ctx.getLocationResolver().resolve(this, atTime);
    int currentFrameX = newCoordinates.x();
    int currentFrameY = newCoordinates.y();

    Graphics g = ctx.getGraphics();
    Color origColor = g.getColor();

    if (this.fillColor != null) {
      g.setColor(ColorUtil.get(this.fillColor));
      g.fillRect(currentFrameX, currentFrameY, width, height);
      g.setColor(origColor);
    }

    if (outlineColor != null) {
      g.setColor(ColorUtil.get(outlineColor));
      g.drawRect(currentFrameX, currentFrameY, width, height);
      g.setColor(origColor);
    }

    ctx.getLocationResolver().addDrawnObject(id, newCoordinates);
  }
}
