package com.brokencircuits.animatedflow.evaluator;

import com.brokencircuits.animatedflow.dsl.Coordinates;
import com.brokencircuits.animatedflow.dsl.DiagramNode;
import com.brokencircuits.animatedflow.dsl.DiagramNodeTransformation;
import com.brokencircuits.animatedflow.dsl.DiagramReferenceGrid;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LocationResolver {

  private final Map<String, List<DiagramNodeTransformation>> transformationsByIdOrdered;

  // per frame state
  private final Map<String, LocationDetails> drawnObjects = new HashMap<>();

  public void clearFrameState() {
    drawnObjects.clear();
  }

  private record LocationDetails(Coordinates coordinates, int spanWidth, int spanHeight) {}

  private static final Pattern REF_PATTERN = Pattern.compile(
      "(?<id>[^\\[]+)\\[(?<firstIndex>\\d+)]\\[(?<secondIndex>\\d+)]");

  public LocationResolver(Collection<DiagramNodeTransformation> transformations) {

    Map<String, List<DiagramNodeTransformation>> transformationsByIdOrdered = new HashMap<>();
    transformations.stream()
        .sorted(Comparator.comparingLong(d -> d.getStartTime().toMillis()))
        .forEach(t -> transformationsByIdOrdered.computeIfAbsent(t.getObjectId(),
            k -> new LinkedList<>()).add(t));
    this.transformationsByIdOrdered = Collections.unmodifiableMap(transformationsByIdOrdered);
  }

  public Coordinates resolve(DiagramNode node, Duration atTime) {
    Coordinates coordinates = getCoordinates(node.getLocationReference(), node.getX(),
        node.getY());

    List<DiagramNodeTransformation> transformations = transformationsByIdOrdered.get(node.getId());
    if (transformations != null) {
      DiagramNodeTransformation lastCompleteTransform = null;
      DiagramNodeTransformation inProgressTransform = null;
      for (DiagramNodeTransformation transformation : transformations) {
        if (transformation.getEndTime().compareTo(atTime) <= 0) {
          lastCompleteTransform = transformation;
        } else if (transformation.getStartTime().compareTo(atTime) <= 0
            && transformation.getEndTime().compareTo(atTime) > 0) {
          inProgressTransform = transformation;
          break;
        }
      }

      // if there has been a transformation applied previously, shift to the resulting coordinates
      // from that transform
      if (lastCompleteTransform != null) {
        coordinates = getCoordinates(lastCompleteTransform.getNewLocationReference(),
            lastCompleteTransform.getNewX(), lastCompleteTransform.getNewY());
      }

      // if there is a transformation in progress, apply transformation according to atTime
      //  e.g. if the transform results in moving an object 100px right, and atTime is 25% through
      //       the transform duration, then object should be 25px right of original position
      if (inProgressTransform != null) {
        coordinates = applyInProgressTransform(coordinates, inProgressTransform, atTime);
      }
    }

    return coordinates;
  }

  private Coordinates applyInProgressTransform(Coordinates preTransformCoordinates,
      DiagramNodeTransformation transform, Duration atTime) {

    int preTransformX = preTransformCoordinates.x();
    int preTransformY = preTransformCoordinates.y();
    Coordinates postTransformCoordinates = getCoordinates(transform.getNewLocationReference(),
        transform.getNewX(), transform.getNewY());
    int postTransformX = postTransformCoordinates.x();
    int postTransformY = postTransformCoordinates.y();
    int transformOffsetX = postTransformX - preTransformX;
    int transformOffsetY = postTransformY - preTransformY;

    long transformTotalTimeMs =
        transform.getEndTime().toMillis() - transform.getStartTime().toMillis();
    long msIntoTransform = atTime.toMillis() - transform.getStartTime().toMillis();
    double transformCompletePcnt = (double) msIntoTransform / transformTotalTimeMs;

    int newX = preTransformX + Math.toIntExact(Math.round(transformOffsetX * transformCompletePcnt));
    int newY = preTransformY + Math.toIntExact(Math.round(transformOffsetY * transformCompletePcnt));
    return new Coordinates(newX, newY);
  }

  private Coordinates getCoordinates(String locationReference, Integer offsetX, Integer offsetY) {
    Coordinates originalCoordinates = new Coordinates(0, 0);
    if (locationReference != null) {
      originalCoordinates = referenceCoordinates(locationReference);
    }
    if (offsetX != null) {
      originalCoordinates = new Coordinates(originalCoordinates.x() + offsetX,
          originalCoordinates.y());
    }
    if (offsetY != null) {
      originalCoordinates = new Coordinates(originalCoordinates.x(),
          originalCoordinates.y() + offsetY);
    }
    return originalCoordinates;
  }

  public void addDrawnObject(String id, Coordinates coordinates) {
    addDrawnGrid(id, coordinates, 0, 0);
  }

  public void addDrawnGrid(String id, Coordinates coordinates, int spanWidth, int spanHeight) {
    if (id != null) {
      if (drawnObjects.containsKey(id)) {
        throw new IllegalStateException("Multiple objects drawn with the same ID: " + id);
      }
      drawnObjects.put(id, new LocationDetails(coordinates, spanWidth, spanHeight));
    }
  }

  private Coordinates referenceCoordinates(String locationReference) {

    if (drawnObjects.containsKey(locationReference)) {
      return drawnObjects.get(locationReference).coordinates();
    }

    Matcher matcher = REF_PATTERN.matcher(locationReference);
    if (!matcher.find()) {
      throw new IllegalStateException("Invalid location reference: " + locationReference);
    }
    String gridId = matcher.group("id");
    int firstIndex = Integer.parseInt(matcher.group("firstIndex"));
    int secondIndex = Integer.parseInt(matcher.group("secondIndex"));

    LocationDetails locationDetails = drawnObjects.get(gridId);
    Objects.requireNonNull(locationDetails, "Grid not defined: " + gridId);

    int x = locationDetails.coordinates().x() + firstIndex * locationDetails.spanWidth();
    int y = locationDetails.coordinates().y() + secondIndex * locationDetails.spanHeight();
    return new Coordinates(x, y);
  }
}
