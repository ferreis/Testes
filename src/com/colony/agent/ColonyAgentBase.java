package com.colony.agent;

import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class ColonyAgentBase extends Agent {
  private final Map<String, AID> serviceCache = new ConcurrentHashMap<>();

  protected void registerService(String serviceType) {
    try {
      DFAgentDescription dfd = new DFAgentDescription();
      dfd.setName(getAID());

      ServiceDescription sd = new ServiceDescription();
      sd.setType(serviceType);
      sd.setName(getLocalName() + "-" + serviceType);
      dfd.addServices(sd);

      DFService.register(this, dfd);
    } catch (FIPAException e) {
      System.err.println(getLocalName() + ": falha ao registrar serviço " + serviceType + " no DF: " + e.getMessage());
    }
  }

  protected AID resolveService(String serviceType, String fallbackLocalName) {
    AID cached = serviceCache.get(serviceType);
    if (cached != null) {
      return cached;
    }

    try {
      DFAgentDescription template = new DFAgentDescription();
      ServiceDescription sd = new ServiceDescription();
      sd.setType(serviceType);
      template.addServices(sd);

      DFAgentDescription[] result = DFService.search(this, template);
      for (DFAgentDescription entry : result) {
        if (entry.getName() == null) {
          continue;
        }
        if (!entry.getName().equals(getAID())) {
          serviceCache.put(serviceType, entry.getName());
          return entry.getName();
        }
      }
    } catch (FIPAException ignored) {
      // Fallback para nome local fixo quando DF não estiver disponível.
    }

    if (fallbackLocalName == null || fallbackLocalName.isBlank()) {
      return null;
    }
    return new AID(fallbackLocalName, AID.ISLOCALNAME);
  }

  protected void clearServiceCache(String serviceType) {
    serviceCache.remove(serviceType);
  }

  @Override
  protected void takeDown() {
    try {
      DFService.deregister(this);
    } catch (Exception ignored) {
    }
  }
}