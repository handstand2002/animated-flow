package com.brokencircuits.animatedflow.dsl;


import com.brokencircuits.animatedflow.evaluator.EvaluationContext;
import java.awt.Graphics;
import java.time.Duration;
import java.util.Collection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiagramText implements DiagramNode {

  private String id;
  private Integer x;
  private Integer y;
  private String locationReference;
  private String text;
  private NodeTextConfig textConfig = new NodeTextConfig();

  @Override
  public void draw(Duration atTime,
      Collection<DiagramNodeTransformation> applicableTransformations,
      EvaluationContext ctx) {

    Coordinates location = ctx.getLocationResolver().resolve(this, atTime);

    Graphics g = ctx.getGraphics();
    if (text != null) {
      textConfig.draw(g, text, new Coordinates(location.x(), location.y()));
    }
    ctx.getLocationResolver().addDrawnObject(id, location);
  }
}
