package com.colony;

import jade.core.Runtime;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;
import com.colony.model.ColonyMap;

public class Main {
    public static final ColonyMap colonyMap = new ColonyMap();

    public static void main(String[] args) {
        // Initialize the JADE runtime and container
        Runtime rt = Runtime.instance();
        rt.setCloseVM(true); // Terminate JVM when no more agents
        
        Profile p = new ProfileImpl(null, 1200, null);
        AgentContainer mainContainer = rt.createMainContainer(p);
        
        try {
            // Create and start the Manager agent first
            AgentController manager = mainContainer.createNewAgent("manager", com.colony.agent.ManagerAgent.class.getName(), null);
            manager.start();
            
            // Create and start the Analyst agent
            AgentController analyst = mainContainer.createNewAgent("analyst", com.colony.agent.AnalystAgent.class.getName(), null);
            analyst.start();
            
            // Create and start the GUI agent
            AgentController gui = mainContainer.createNewAgent("gui", com.colony.gui.GuiAgent.class.getName(), null);
            gui.start();
            
            // ─── Cria os NPCs (cada um = 1 anão) ───
            AgentController[] workers = {
                mainContainer.createNewAgent("Urist", com.colony.agent.WorkerAgent.class.getName(), new Object[]{"builder"}),
                mainContainer.createNewAgent("Doren", com.colony.agent.WorkerAgent.class.getName(), new Object[]{"miner"}),
                mainContainer.createNewAgent("Logem", com.colony.agent.WorkerAgent.class.getName(), new Object[]{"woodcutter"}),
                mainContainer.createNewAgent("Kikrost", com.colony.agent.WorkerAgent.class.getName(), new Object[]{"carpenter"}),
                mainContainer.createNewAgent("Stinthad", com.colony.agent.WorkerAgent.class.getName(), new Object[]{"smith"}),
                mainContainer.createNewAgent("Meng", com.colony.agent.WorkerAgent.class.getName(), new Object[]{"mason"}),
                mainContainer.createNewAgent("Zasit", com.colony.agent.WorkerAgent.class.getName(), new Object[]{"marbleworker"}),
            };
            for (AgentController w : workers) w.start();
            
            System.out.println("Todos os agentes iniciados com sucesso!");
            
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
    }
}
