package com.brokencircuits.animatedflow.evaluator;

import com.brokencircuits.animatedflow.dsl.DiagramNode;
import com.brokencircuits.animatedflow.dsl.DiagramNodeTransformation;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class LocationalNode {

  private final Integer x;
  private final Integer y;
  private final String locationReference;

  public static LocationalNode from(DiagramNode node) {
    return new LocationalNode(node.getX(), node.getY(), node.getLocationReference());
  }

  public static LocationalNode from(DiagramNodeTransformation transformation) {
    return new LocationalNode(transformation.getNewX(), transformation.getNewY(),
        transformation.getNewLocationReference());
  }
}
