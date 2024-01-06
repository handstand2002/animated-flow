package com.brokencircuits.animatedflow;

import java.awt.Color;
import java.lang.reflect.Field;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ColorUtil {

  public static Color get(String colorString) {

    Color color;
    try {
      Field field = Color.class.getField(colorString);
      color = (Color) field.get(null);
    } catch (Exception e) {
      color = null; // Not defined
    }
    return color;
  }
}
