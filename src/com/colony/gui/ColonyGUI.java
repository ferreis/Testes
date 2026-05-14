package com.colony.gui;

import com.colony.model.*;
import com.colony.Main;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.*;
import java.util.List;

public class ColonyGUI {
    private final JFrame frame;
    private final DefaultTableModel workerModel;
    private final DefaultTableModel taskModel;
    private final JTextArea logArea;
    private final JTextArea resourceArea;
    private final JLabel statusLabel;
    private final MapPanel mapPanel;
    private final ColonyMap colonyMap;
    private final Map<String, String> workerDetails;
    private final JTable workerTable;

    public ColonyGUI() {
        colonyMap = Main.colonyMap;
        workerDetails = new HashMap<>();

        frame = new JFrame("Gerenciador de Colônia - Inspirado em Dwarf Fortress");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(960, 720);
        frame.setMinimumSize(new Dimension(800, 600));
        frame.setLocationRelativeTo(null);

        // ── Top status bar ──
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(new Color(50, 50, 60));

        statusLabel = new JLabel(" Colônia inicializando... ");
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setFont(new Font("Monospaced", Font.BOLD, 13));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        topBar.add(statusLabel, BorderLayout.WEST);

        JLabel versionLabel = new JLabel("v0.0.6");
        versionLabel.setForeground(new Color(150, 180, 220));
        versionLabel.setFont(new Font("Monospaced", Font.BOLD, 12));
        versionLabel.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 14));
        topBar.add(versionLabel, BorderLayout.EAST);

        frame.add(topBar, BorderLayout.NORTH);

        // ── Tabbed pane ──
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("SansSerif", Font.PLAIN, 13));

        // ---- MAP TAB ----
        mapPanel = new MapPanel(colonyMap);
        JScrollPane mapScroll = new JScrollPane(mapPanel);
        mapScroll.setBorder(BorderFactory.createTitledBorder("Mapa da Colônia (scroll/zoom)"));
        tabs.addTab("Mapa", mapScroll);

        // ---- WORKERS TAB ----
        workerModel = new DefaultTableModel(new String[]{"Trabalhador", "Tipo", "Habilidade", "Nível", "Rank", "Status", "Energia"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        workerTable = new JTable(workerModel);
        workerTable.setFillsViewportHeight(true);
        workerTable.setRowHeight(22);
        // Tooltip nas linhas
        workerTable.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent e) {
                int row = workerTable.rowAtPoint(e.getPoint());
                if (row >= 0) {
                    String name = (String) workerModel.getValueAt(row, 0);
                    String tip = workerDetails.getOrDefault(name, name);
                    workerTable.setToolTipText(tip);
                }
            }
        });
        JScrollPane workerScroll = new JScrollPane(workerTable);
        workerScroll.setBorder(BorderFactory.createTitledBorder("Trabalhadores"));
        tabs.addTab("Trabalhadores", workerScroll);

        // ---- TASKS TAB ----
        taskModel = new DefaultTableModel(new String[]{"ID Tarefa", "Tipo", "Status"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable taskTable = new JTable(taskModel);
        taskTable.setFillsViewportHeight(true);
        JScrollPane taskScroll = new JScrollPane(taskTable);
        taskScroll.setBorder(BorderFactory.createTitledBorder("Tarefas"));
        tabs.addTab("Tarefas", taskScroll);

        // ---- RESOURCES TAB ----
        resourceArea = new JTextArea();
        resourceArea.setEditable(false);
        resourceArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        resourceArea.setBackground(new Color(245, 240, 230));
        ColonyResources res = new ColonyResources();
        updateResourceDisplay(res);
        JScrollPane resScroll = new JScrollPane(resourceArea);
        resScroll.setBorder(BorderFactory.createTitledBorder("Recursos"));
        tabs.addTab("Recursos", resScroll);

        // ---- LOG TAB ----
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        logArea.setBackground(new Color(15, 15, 20));
        logArea.setForeground(new Color(200, 230, 200));
        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setBorder(BorderFactory.createTitledBorder("Registro de Eventos"));
        tabs.addTab("Eventos", logScroll);

        frame.add(tabs, BorderLayout.CENTER);

        // ── Bottom stats bar ──
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 3));
        bottom.setBackground(new Color(50, 50, 60));
        JLabel lbl = new JLabel(" Trabalhadores: 0  |  Tarefas: 0  |  Mapa: "
                + ColonyMap.WIDTH + "x" + ColonyMap.HEIGHT + " | v0.0.6");
        lbl.setForeground(Color.LIGHT_GRAY);
        lbl.setFont(new Font("Monospaced", Font.PLAIN, 11));
        bottom.add(lbl);
        frame.add(bottom, BorderLayout.SOUTH);

        frame.setVisible(true);
    }

    // ── Public update methods (thread-safe) ──

    public void updateNpcPosition(String npcId, int x, int y) {
        SwingUtilities.invokeLater(() -> mapPanel.setNpcPosition(npcId, x, y));
    }

    public MapPanel getMapPanel() { return mapPanel; }

    public void updateWorker(String name, String skill, String level,
                               String rank, String status, String energia) {
        SwingUtilities.invokeLater(() -> {
            String skillPt = traduzirSkill(skill);
            for (int i = 0; i < workerModel.getRowCount(); i++) {
                if (workerModel.getValueAt(i, 0).equals(name)) {
                    workerModel.setValueAt(skillPt, i, 2);
                    workerModel.setValueAt(level, i, 3);
                    workerModel.setValueAt(rank, i, 4);
                    workerModel.setValueAt(status, i, 5);
                    workerModel.setValueAt(energia, i, 6);
                    return;
                }
            }
            String type = traduzirTipo(skill);
            workerModel.addRow(new Object[]{name, type, skillPt, level, rank, status, energia});
            statusLabel.setText(" Trabalhadores: " + workerModel.getRowCount()
                    + "  |  Tarefas: " + taskModel.getRowCount()
                    + "  |  Último: " + name + " " + status);
        });
    }

    private String traduzirTipo(String skill) {
        return switch (skill.toLowerCase()) {
            case "construction", "builder" -> "Construtor";
            case "carpenter", "bowyer" -> "Carpinteiro";
            case "mason", "stonecutter" -> "Pedreiro";
            case "stone carver", "stone crafter" -> "Marmorista";
            case "mining", "miner" -> "Minerador";
            case "woodcutting", "wood cutter" -> "Lenhador";
            case "smithing", "blacksmith", "armorsmith", "weaponsmith" -> "Ferreiro";
            case "farming", "planter", "brewer", "cook" -> "Fazendeiro";
            case "diagnostician", "surgeon", "bone doctor" -> "Médico";
            case "fighter", "axeman", "swordsman" -> "Guerreiro";
            case "mechanic", "siege engineer" -> "Engenheiro";
            case "wood crafter", "bone carver", "potter" -> "Artesão";
            default -> "Geral";
        };
    }

    public void updateTask(String taskId, String taskType, String status) {
        SwingUtilities.invokeLater(() -> {
            String tipoPt = traduzirTaskType(taskType);
            for (int i = 0; i < taskModel.getRowCount(); i++) {
                if (taskModel.getValueAt(i, 0).equals(taskId)) {
                    taskModel.setValueAt(status, i, 2);
                    return;
                }
            }
            taskModel.addRow(new Object[]{taskId, tipoPt, status});
        });
    }

    private String traduzirSkill(String skill) {
        SkillType skillType = SkillType.fromKey(skill);
        if (skillType != null) {
            return skillType.getDisplayName();
        }

        return switch (skill.toLowerCase()) {
            case "construction" -> "construção";
            case "mining" -> "mineração";
            case "woodcutting" -> "corte de madeira";
            case "smithing" -> "ferraria";
            case "farming" -> "agricultura";
            default -> "geral";
        };
    }

    private String traduzirTaskType(String type) {
        return switch (type.toLowerCase()) {
            case "build", "construct" -> "construção";
            case "mine", "mining" -> "mineração";
            case "woodcut", "woodcutting" -> "corte de madeira";
            case "smith", "smithing", "forge" -> "forjaria";
            case "farm", "farming" -> "agricultura";
            case "carpentry" -> "carpintaria";
            case "masonry" -> "alvenaria";
            default -> type;
        };
    }

    public void updateWorkerDetails(String name, int health, int energy,
                                     String type, java.util.Map<String, Integer> skills) {
        SwingUtilities.invokeLater(() -> {
            StringBuilder sb = new StringBuilder();
            sb.append("<html><b>").append(name).append("</b><hr>");
            String corVida = health > 50 ? "#4a4" : health > 25 ? "#aa4" : "#a44";
            String corEn = energy > 50 ? "#44a" : energy > 25 ? "#aa4" : "#a44";
            sb.append("❤ <span style='color:").append(corVida).append("'>HP ").append(health).append("</span>");
            sb.append(" &nbsp; ⚡ <span style='color:").append(corEn).append("'>EN ").append(energy).append("</span>");
            sb.append(" &nbsp; 🏷 ").append(type).append("<br>");
            sb.append("<table>");
            for (java.util.Map.Entry<String, Integer> e : skills.entrySet()) {
                String rank = SkillType.rankForLevel(e.getValue());
                sb.append("<tr><td>").append(e.getKey()).append("</td>")
                  .append("<td> lv").append(e.getValue()).append("</td>")
                  .append("<td><i>").append(rank).append("</i></td></tr>");
            }
            sb.append("</table></html>");
            workerDetails.put(name, sb.toString());
        });
    }

    public void updateResources(ColonyResources res) {
        SwingUtilities.invokeLater(() -> updateResourceDisplay(res));
    }

    private void updateResourceDisplay(ColonyResources res) {
        StringBuilder sb = new StringBuilder("  RECURSOS DA COLÔNIA\n");
        sb.append("  ────────────────────\n");
        for (Map.Entry<String, Integer> e : res.getAll().entrySet()) {
            String bar = "█".repeat(Math.min(e.getValue() / 2, 20));
            sb.append(String.format("  %-10s %3d  %s\n", e.getKey() + ":", e.getValue(), bar));
        }
        resourceArea.setText(sb.toString());
    }

    public void addLog(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append("[" + java.time.LocalTime.now().format(
                    java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")) + "] " + message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    public void setStatus(String text) {
        SwingUtilities.invokeLater(() -> statusLabel.setText(" " + text));
    }
}
