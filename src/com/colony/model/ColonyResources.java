package com.colony.model;

import java.util.HashMap;
import java.util.Map;

import java.util.concurrent.ConcurrentHashMap;

public class ColonyResources {
  private Map<String, Integer> resources;

  public ColonyResources() {
    resources = new ConcurrentHashMap<>();
    resources.put("madeira", 450);
    resources.put("pedra", 400);
    resources.put("ferro", 300);
    resources.put("comida", 50);
    resources.put("agua", 20);
    resources.put("vara de pesca", 1);
  }

  public int get(String resource) {
    return resources.getOrDefault(resource.toLowerCase(), 0);
  }

  public void add(String resource, int amount) {
    resources.merge(resource.toLowerCase(), amount, Integer::sum);
  }

  public synchronized boolean consume(String resource, int amount) {
    String key = resource.toLowerCase();
    int current = resources.getOrDefault(key, 0);
    if (current >= amount) {
      resources.put(key, current - amount);
      return true;
    }
    return false;
  }

  public Map<String, Integer> getAll() {
    return new HashMap<>(resources);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (Map.Entry<String, Integer> e : resources.entrySet()) {
      sb.append(e.getKey()).append(": ").append(e.getValue()).append("\n");
    }
    return sb.toString();
  }
}