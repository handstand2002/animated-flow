package com.brokencircuits.animatedflow.dsl;

import com.brokencircuits.animatedflow.ColorUtil;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NodeTextConfig {

  @Default private String fontName = "Dialog";
  @Default private FontStyle style = FontStyle.PLAIN;
  @Default private int size = 12;
  @Default private String color = "WHITE";
  @Default private int padding = 5;
  @Default private int paddingLeft = 0;
  @Default private int paddingTop = 10;

  public void draw(Graphics g, String text, Coordinates coordinates) {
    Color origColor = g.getColor();
    Font origFont = g.getFont();

    g.setColor(ColorUtil.get(color));
    //noinspection MagicConstant
    g.setFont(new Font(fontName, style.getId(), size));
    g.drawString(text, coordinates.x() + padding + paddingLeft,
        coordinates.y() + padding + paddingTop);

    g.setFont(origFont);
    g.setColor(origColor);
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
