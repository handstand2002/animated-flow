package com.brokencircuits.animatedflow.evaluator;

import com.brokencircuits.animatedflow.ColorUtil;
import com.brokencircuits.animatedflow.DiagramFrame;
import com.brokencircuits.animatedflow.Stats;
import com.brokencircuits.animatedflow.dsl.DiagramNode;
import com.brokencircuits.animatedflow.dsl.DiagramNodeTransformation;
import com.brokencircuits.animatedflow.dsl.FlowChart;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.time.Duration;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class FlowChartEvaluator {

  private record PreRenderFrameDetails(long id, Duration length) {

  }

  public List<DiagramFrame> renderAnimated(FlowChart chart) {
    long startTime = System.nanoTime();
    List<PreRenderFrameDetails> preCalculatedFrames = calculateFrames(chart);
    EvaluationContext ctx = EvaluationContext.builder()
        .referenceGridResolver(new ReferenceGridResolver(chart.getGrids()))
        .build();

    List<DiagramFrame> frames = new LinkedList<>();
    Duration currentTime = Duration.ZERO;
    for (PreRenderFrameDetails preCalculatedFrame : preCalculatedFrames) {
      BufferedImage frameImg = render(chart, currentTime, ctx);
      frames.add(new DiagramFrame(frameImg, preCalculatedFrame.length()));
      currentTime = currentTime.plus(preCalculatedFrame.length());
    }
    log.info("Rendered all {} frames in {}", frames.size(),
        Duration.ofNanos(System.nanoTime() - startTime));
    Stats.log();
    Stats.reset();
    return frames;
  }

  private List<PreRenderFrameDetails> calculateFrames(FlowChart chart) {
    long nextId = 0;

    Duration millisPerTransformFrame = Duration.ofMillis(1000 / chart.getFps());

    List<DiagramNodeTransformation> transformations = chart.getTransforms().stream()
        .sorted(Comparator.comparingLong(t -> t.getStartTime().toMillis()))
        .toList();

    Duration durationCrawler = Duration.ZERO;
    List<PreRenderFrameDetails> frames = new LinkedList<>();
    while (durationCrawler.compareTo(chart.getTotalLength()) < 0) {
      Duration frameDuration;
      if (isInTransform(durationCrawler, transformations)) {
        Duration frameEndAt = durationCrawler.plus(millisPerTransformFrame);
        frameDuration = Duration.ofMillis(frameEndAt.toMillis() - durationCrawler.toMillis());
        frames.add(new PreRenderFrameDetails(nextId++, frameDuration));
        durationCrawler = frameEndAt;
      } else {
        Duration frameEndAt = findNextTransformStart(transformations, durationCrawler);
        if (frameEndAt == null) {
          frameEndAt = chart.getTotalLength();
        }
        frameDuration = Duration.ofMillis(frameEndAt.toMillis() - durationCrawler.toMillis());
        frames.add(new PreRenderFrameDetails(nextId++, frameDuration));
        durationCrawler = frameEndAt;
      }
    }

    return frames;
  }

  private boolean isInTransform(Duration atTime,
      List<DiagramNodeTransformation> transformationsSortedByStartTime) {
    for (DiagramNodeTransformation transformation : transformationsSortedByStartTime) {
      if (transformation.getStartTime().compareTo(atTime) <= 0
          && transformation.getEndTime().compareTo(atTime) > 0) {
        return true;
      }
    }
    return false;
  }

  private Duration findNextTransformStart(List<DiagramNodeTransformation> transformations,
      Duration fromTime) {
    for (DiagramNodeTransformation transformation : transformations) {
      if (transformation.getStartTime().compareTo(fromTime) > 0) {
        return transformation.getStartTime();
      }
    }
    return null;
  }

  public BufferedImage render(FlowChart chart, Duration atTime,
      EvaluationContext ctx) {

    int height = chart.getHeight();
    int width = chart.getWidth();

    BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    Graphics g = bufferedImage.getGraphics();

    Color color = ColorUtil.get(chart.getBackgroundColor());
    g.setColor(color);
    g.fillRect(0, 0, width, height);
    g.setColor(Color.WHITE);

    for (DiagramNode item : chart.getItems()) {
      List<DiagramNodeTransformation> applicableTransformations = chart.getTransforms().stream()
          .filter(t -> Objects.equals(t.getObjectId(), item.getId()))
          .toList();
      Color origColor = g.getColor();

      item.draw(g, atTime, applicableTransformations, ctx);
      g.setColor(origColor);
    }

    return bufferedImage;
  }

}
