package com.brokencircuits.animatedflow.dsl;

import com.brokencircuits.animatedflow.evaluator.EvaluationContext;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import java.time.Duration;
import java.util.Collection;

@JsonTypeInfo(use = Id.NAME, property = "type", include = As.EXISTING_PROPERTY)
@JsonSubTypes({
    @Type(value = DiagramRectangle.class, name = "rectangle"),
    @Type(value = DiagramReferenceGrid.class, name = "grid"),
    @Type(value = DiagramText.class, name = "text"),
})
public interface DiagramNode {

  String getId();

  Integer getX();

  Integer getY();

  String getLocationReference();

  void draw(Duration frame,
      Collection<DiagramNodeTransformation> applicableTransformations,
      EvaluationContext ctx);
}
