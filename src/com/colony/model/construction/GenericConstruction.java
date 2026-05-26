package com.colony.model.construction;

import com.colony.model.BuildingType;
import java.util.Collections;

public class GenericConstruction extends ConstructionModel {
  public GenericConstruction(BuildingType type) {
    super(type, type.getName(), Collections.emptyMap());
  }
}
