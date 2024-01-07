package com.brokencircuits.animatedflow.dsl;


import com.brokencircuits.animatedflow.ColorUtil;
import com.brokencircuits.animatedflow.Stats;
import com.brokencircuits.animatedflow.Stats.Task;
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
  private int x;
  private int y;
  @Default
  private String fillColor = "BLACK";
  @Default
  private String outlineColor = "BLACK";
  private String text;
  private NodeTextConfig textConfig = new NodeTextConfig();

  @Override
  public void draw(Graphics g, Duration atTime,
      Collection<DiagramNodeTransformation> applicableTransformations) {

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
    int effectiveX = lastApplied.map(DiagramNodeTransformation::getNewX).orElse(this.getX());
    int effectiveY = lastApplied.map(DiagramNodeTransformation::getNewY).orElse(this.getY());
    if (currentTransform.isPresent()) {
      DiagramNodeTransformation transform = currentTransform.get();
      long transformTotalTime =
          transform.getEndTime().toMillis() - transform.getStartTime().toMillis();
      double atTimePcnt =
          (atTime.toMillis() - transform.getStartTime().toMillis()) / (double) transformTotalTime;

      effectiveX = (int) (effectiveX + ((transform.getNewX() - effectiveX) * atTimePcnt));
      effectiveY = (int) (effectiveY + ((transform.getNewY() - effectiveY) * atTimePcnt));
    }
    updateCoordinatesTask.stop();

    Color origColor = g.getColor();

    Task fillColorTask = Stats.start("FillColor");
    if (this.fillColor != null) {
      g.setColor(ColorUtil.get(this.fillColor));
      g.fillRect(effectiveX, effectiveY, width, height);
      g.setColor(origColor);
    }
    fillColorTask.stop();

    Task outlineColorTask = Stats.start("OutlineColor");
    if (outlineColor != null) {
      g.setColor(ColorUtil.get(outlineColor));
      g.drawRect(effectiveX, effectiveY, width, height);
      g.setColor(origColor);
    }
    outlineColorTask.stop();

    Task drawTextTask = Stats.start("DrawText");
    if (text != null) {
      textConfig.draw(g, text, new Coordinates(effectiveX, effectiveY));
    }
    drawTextTask.stop();
  }
}
