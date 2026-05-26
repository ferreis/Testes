package com.colony.model.agent;

public class WorkerModel extends AgentModel {
  private final String specialization;

  public WorkerModel(String name, String specialization) {
    super(name, "trabalhador");
    this.specialization = specialization;
  }

  public String getSpecialization() {
    return specialization;
  }
}