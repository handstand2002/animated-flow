package com.brokencircuits.animatedflow.evaluator;

import com.brokencircuits.animatedflow.dsl.Coordinates;
import com.brokencircuits.animatedflow.dsl.DiagramReferenceGrid;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ReferenceGridResolver {

  private final Collection<DiagramReferenceGrid> grids;
  private final Map<String, DiagramReferenceGrid> gridsById = new HashMap<>();
  private static final Pattern REF_PATTERN = Pattern.compile(
      "(?<id>[^\\[]+)\\[(?<firstIndex>\\d+)]\\[(?<secondIndex>\\d+)]");

  public Coordinates resolve(LocationalNode node) {
    if (node.getX() != null && node.getY() != null) {
      return new Coordinates(node.getX(), node.getY());
    }
    if (node.getLocationReference() != null) {
      return resolve(node.getLocationReference());
    }
    throw new IllegalStateException("No location parameters provided");
  }

  private Coordinates resolve(String locationReference) {
    if (gridsById.isEmpty() && !grids.isEmpty()) {
      populateGridsById();
    }
    Matcher matcher = REF_PATTERN.matcher(locationReference);
    if (!matcher.find()) {
      throw new IllegalStateException("Invalid location reference: " + locationReference);
    }
    String gridId = matcher.group("id");
    int firstIndex = Integer.parseInt(matcher.group("firstIndex"));
    int secondIndex = Integer.parseInt(matcher.group("secondIndex"));

    DiagramReferenceGrid grid = gridsById.get(gridId);
    Objects.requireNonNull(grid, "Grid not defined: " + gridId);

    int x = firstIndex * grid.getHorizontalSpanWidth();
    int y = secondIndex * grid.getVerticalSpanWidth();
    return new Coordinates(x, y);
  }

  private void populateGridsById() {
    for (DiagramReferenceGrid grid : grids) {
      if (gridsById.containsKey(grid.getId())) {
        throw new IllegalStateException("Multiple grids with same ID: " + grid.getId());
      }
      gridsById.put(grid.getId(), grid);
    }
  }

}
