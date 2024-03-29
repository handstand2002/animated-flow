package com.brokencircuits.animatedflow.dsl;


import com.brokencircuits.animatedflow.evaluator.EvaluationContext;
import com.brokencircuits.animatedflow.evaluator.LocationResolver;
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
public class DiagramReferenceGrid implements DiagramNode {

  private String id;
  private Integer x;
  private Integer y;
  private String locationReference;
  private int spanWidth;
  private int spanHeight;

  @Override
  public void draw(Duration atTime,
      Collection<DiagramNodeTransformation> applicableTransformations,
      EvaluationContext ctx) {
    LocationResolver resolver = ctx.getLocationResolver();

    Coordinates location = resolver.resolve(this, atTime);
    resolver.addDrawnGrid(id, location, spanWidth, spanHeight);
  }
}
