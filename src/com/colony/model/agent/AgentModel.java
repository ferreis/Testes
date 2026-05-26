package com.colony.model.agent;

public abstract class AgentModel {
  private final String name;
  private final String role;

  protected AgentModel(String name, String role) {
    this.name = name;
    this.role = role;
  }

  public String getName() {
    return name;
  }

  public String getRole() {
    return role;
  }
}