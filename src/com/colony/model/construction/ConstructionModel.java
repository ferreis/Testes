package com.colony.model.construction;

import com.colony.model.BuildingType;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class ConstructionModel {
  private final BuildingType type;
  private final String displayName;
  private final Map<String, Integer> constructionCost;

  protected ConstructionModel(BuildingType type, String displayName, Map<String, Integer> constructionCost) {
    this.type = type;
    this.displayName = displayName;
    this.constructionCost = Collections.unmodifiableMap(new LinkedHashMap<>(constructionCost));
  }

  public BuildingType getType() {
    return type;
  }

  public String getDisplayName() {
    return displayName;
  }

  public Map<String, Integer> getConstructionCost() {
    return constructionCost;
  }
}
