package com.brokencircuits.animatedflow.dsl;


import com.brokencircuits.animatedflow.ColorUtil;
import com.brokencircuits.animatedflow.evaluator.EvaluationContext;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.time.Duration;
import java.util.Collection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

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
  private String fontName = "Dialog";
  private FontStyle style = FontStyle.PLAIN;
  private int size = 12;
  private String color = "WHITE";
  private int padding = 5;
  private int paddingLeft = 0;
  private int paddingTop = 10;

  @Override
  public void draw(Duration atTime,
      Collection<DiagramNodeTransformation> applicableTransformations,
      EvaluationContext ctx) {

    Coordinates location = ctx.getLocationResolver().resolve(this, atTime);

    Graphics g = ctx.getGraphics();
    if (text != null) {

      Color origColor = g.getColor();
      Font origFont = g.getFont();

      g.setColor(ColorUtil.get(color));
      //noinspection MagicConstant
      g.setFont(new Font(fontName, style.getId(), size));
      g.drawString(text, location.x() + padding + paddingLeft,
          location.y() + padding + paddingTop);

      g.setFont(origFont);
      g.setColor(origColor);


    }
    ctx.getLocationResolver().addDrawnObject(id, location);
  }

  @RequiredArgsConstructor
  @Getter
  public enum FontStyle {
    PLAIN(Font.PLAIN),
    BOLD(Font.BOLD),
    ITALIC(Font.ITALIC);
    private final int id;
  }
}
