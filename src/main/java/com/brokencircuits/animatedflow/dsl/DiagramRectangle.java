package com.brokencircuits.animatedflow.dsl;


import com.brokencircuits.animatedflow.ColorUtil;
import com.brokencircuits.animatedflow.Stats;
import com.brokencircuits.animatedflow.Stats.Task;
import com.brokencircuits.animatedflow.evaluator.EvaluationContext;
import com.brokencircuits.animatedflow.evaluator.LocationalNode;
import java.awt.Color;
import java.awt.Graphics;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;

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
  private String text;
  private NodeTextConfig textConfig = new NodeTextConfig();

  @Override
  public void draw(Graphics g, Duration atTime,
      Collection<DiagramNodeTransformation> applicableTransformations,
      EvaluationContext ctx) {

    Task sortTask = Stats.start("Sort Transforms");
    List<DiagramNodeTransformation> transformations = new ArrayList<>(applicableTransformations);
    transformations.sort(Comparator.comparingLong(t -> t.getStartTime().toMillis()));
    sortTask.stop();

    Task findTransformsTask = Stats.start("Find Transforms");
    Optional<DiagramNodeTransformation> lastApplied = Optional.empty();
    Optional<DiagramNodeTransformation> currentTransform = Optional.empty();
    for (DiagramNodeTransformation transformation : transformations) {
      if (transformation.getEndTime().compareTo(atTime) <= 0) {
        lastApplied = Optional.of(transformation);
      } else if (transformation.getStartTime().compareTo(atTime) <= 0
          && transformation.getEndTime().compareTo(atTime) > 0) {
        currentTransform = Optional.of(transformation);
        break;
      }
    }
    findTransformsTask.stop();

    Task updateCoordinatesTask = Stats.start("Update coordinates");

    Coordinates preTransformCoordinates = lastApplied.map(LocationalNode::from)
        .map(ctx.getReferenceGridResolver()::resolve)
        .orElseGet(() -> ctx.getReferenceGridResolver().resolve(LocationalNode.from(this)));
    int currentFrameX = preTransformCoordinates.x();
    int currentFrameY = preTransformCoordinates.y();

    if (currentTransform.isPresent()) {
      DiagramNodeTransformation transform = currentTransform.get();
      long transformTotalTime =
          transform.getEndTime().toMillis() - transform.getStartTime().toMillis();
      double atTimePcnt =
          (atTime.toMillis() - transform.getStartTime().toMillis()) / (double) transformTotalTime;

      Coordinates postTransformCoordinates = ctx.getReferenceGridResolver()
          .resolve(LocationalNode.from(currentTransform.get()));

      currentFrameX = (int) (preTransformCoordinates.x()
          + ((postTransformCoordinates.x() - preTransformCoordinates.x()) * atTimePcnt));
      currentFrameY = (int) (preTransformCoordinates.y()
          + ((postTransformCoordinates.y() - preTransformCoordinates.y()) * atTimePcnt));
    }
    updateCoordinatesTask.stop();

    Color origColor = g.getColor();

    Task fillColorTask = Stats.start("FillColor");
    if (this.fillColor != null) {
      g.setColor(ColorUtil.get(this.fillColor));
      g.fillRect(currentFrameX, currentFrameY, width, height);
      g.setColor(origColor);
    }
    fillColorTask.stop();

    Task outlineColorTask = Stats.start("OutlineColor");
    if (outlineColor != null) {
      g.setColor(ColorUtil.get(outlineColor));
      g.drawRect(currentFrameX, currentFrameY, width, height);
      g.setColor(origColor);
    }
    outlineColorTask.stop();

    Task drawTextTask = Stats.start("DrawText");
    if (text != null) {
      textConfig.draw(g, text, new Coordinates(currentFrameX, currentFrameY));
    }
    drawTextTask.stop();
  }
}
