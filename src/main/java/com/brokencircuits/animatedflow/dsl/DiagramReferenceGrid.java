package com.brokencircuits.animatedflow.dsl;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiagramReferenceGrid {

  // Allow users to specify an invisible grid ahead of time, which allows items to be set
  // (or moved to) specific points in the grid without having to know exact coordinates.
  /* e.g. grid named 'r1' which is 7x3:
      |-------|
      |       |
      |   x   |
      |       |

      an object may be placed in the grid by referencing 'r1[3][1]'

  */
  private String id;
  private int x;
  private int y;
  @Default private int horizontalSpanWidth = 1;
  @Default private int verticalSpanWidth = 1;
}
