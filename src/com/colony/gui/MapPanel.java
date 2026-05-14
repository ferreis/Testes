package com.colony.gui;

import com.colony.model.*;
import com.colony.model.BuildingType;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import javax.imageio.ImageIO;

public class MapPanel extends JPanel {
    private final ColonyMap map;
    private double scale = 6.0;
    private int viewX = ColonyMap.WIDTH / 2, viewY = ColonyMap.HEIGHT / 2;
    private int mouseX, mouseY;
    private String selectedNpc;
    private final Map<String, Color> npcColors;
    private final Map<String, int[]> npcPositions;
    private BufferedImage dwarfSpriteSheet;
    private static final int FRAME_W = 32;
    private static final int FRAME_H = 32;
    private static final int COLS = 12;
    private final Map<String, int[]> npcSpriteFrames;

    public MapPanel(ColonyMap map) {
        this.map = map;
        this.npcColors = new HashMap<>();
        this.npcPositions = new HashMap<>();
        this.npcSpriteFrames = new HashMap<>();
        loadSpriteSheet();
        setBackground(new Color(30, 30, 35));
        setPreferredSize(new Dimension(
            (int)(ColonyMap.WIDTH * scale),
            (int)(ColonyMap.HEIGHT * scale)));

        MouseAdapter ma = new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                requestFocusInWindow();
                int mx = (int)((e.getX() - getWidth()/2) / scale + viewX);
                int my = (int)((e.getY() - getHeight()/2) / scale + viewY);
                if (map.inBounds(mx, my)) {
                    // Check if clicked on NPC
                    selectedNpc = null;
                    for (Map.Entry<String, int[]> npc : npcPositions.entrySet()) {
                        int[] pos = npc.getValue();
                        if (Math.abs(pos[0] - mx) <= 1 && Math.abs(pos[1] - my) <= 1) {
                            selectedNpc = npc.getKey();
                            break;
                        }
                    }
                    if (selectedNpc == null) {
                        showTileInfo(mx, my);
                    }
                }
            }
            public void mouseMoved(MouseEvent e) { mouseX = e.getX(); mouseY = e.getY(); }
            public void mouseWheelMoved(MouseWheelEvent e) {
                double oldScale = scale;
                scale = Math.max(4, Math.min(32, scale - e.getWheelRotation() * 2));
                setPreferredSize(new Dimension(
                    (int)(ColonyMap.WIDTH * scale),
                    (int)(ColonyMap.HEIGHT * scale)));
                revalidate();
                repaint();
            }
        };
        addMouseListener(ma);
        addMouseMotionListener(ma);
        addMouseWheelListener(ma);

        // Scroll with arrow keys
        setFocusable(true);
        addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                int step = 5;
                if (e.isShiftDown()) step = 20;
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_LEFT: case KeyEvent.VK_A: viewX -= step; break;
                    case KeyEvent.VK_RIGHT: case KeyEvent.VK_D: viewX += step; break;
                    case KeyEvent.VK_UP: case KeyEvent.VK_W: viewY -= step; break;
                    case KeyEvent.VK_DOWN: case KeyEvent.VK_S: viewY += step; break;
                    case KeyEvent.VK_MINUS: scale = Math.max(4, scale - 2); break;
                    case KeyEvent.VK_PLUS: case KeyEvent.VK_EQUALS: scale = Math.min(32, scale + 2); break;
                }
                setPreferredSize(new Dimension((int)(ColonyMap.WIDTH * scale), (int)(ColonyMap.HEIGHT * scale)));
                revalidate();
                repaint();
            }
        });
    }

    public void setNpcPosition(String npcId, int x, int y) {
        npcPositions.put(npcId, new int[]{x, y});
        if (!npcColors.containsKey(npcId)) {
            Random r = new Random(npcId.hashCode());
            npcColors.put(npcId, new Color(r.nextInt(200) + 55, r.nextInt(200) + 55, r.nextInt(200) + 55));
        }
        repaint();
    }

    public String getSelectedNpc() { return selectedNpc; }

    private void loadSpriteSheet() {
        // Tenta 1: classpath (mais confiável em JARs/IDE)
        try {
            java.io.InputStream is = getClass().getResourceAsStream("/com/colony/dwarves.png");
            if (is != null) {
                dwarfSpriteSheet = ImageIO.read(is);
                is.close();
            }
        } catch (Exception e) { /* fallback */ }

        // Tenta 2: caminho relativo ao working directory
        if (dwarfSpriteSheet == null) {
            try {
                dwarfSpriteSheet = ImageIO.read(new File("src/com/colony/dwarves.png"));
            } catch (IOException e) { /* fallback */ }
        }

        // Tenta 3: caminho absoluto (último recurso)
        if (dwarfSpriteSheet == null) {
            try {
                dwarfSpriteSheet = ImageIO.read(new File(
                    System.getProperty("user.dir"), "src/com/colony/dwarves.png"));
            } catch (IOException e) { /* fallback */ }
        }

        if (dwarfSpriteSheet != null) {
            System.out.println("[MapPanel] Spritesheet carregado: " 
                + dwarfSpriteSheet.getWidth() + "x" + dwarfSpriteSheet.getHeight());
        } else {
            System.err.println("[MapPanel] AVISO: dwarves.png NÃO carregado. user.dir=" 
                + System.getProperty("user.dir") + ". Usando círculos coloridos.");
        }
    }

    private int[] getSpriteFrame(String npcId) {
        int[] frame = npcSpriteFrames.get(npcId);
        if (frame == null) {
            Random r = new Random(npcId.hashCode());
            int col = r.nextInt(COLS);
            int row = 5 + r.nextInt(14);
            frame = new int[]{col, row};
            npcSpriteFrames.put(npcId, frame);
        }
        return frame;
    }

    public int[] getMouseTile() {
        return new int[]{
            (int)((mouseX - getWidth()/2) / scale + viewX),
            (int)((mouseY - getHeight()/2) / scale + viewY)
        };
    }

    private void showTileInfo(int tx, int ty) {
        TerrainTile t = map.getTile(tx, ty);
        ColonyMap.ColonyBuilding b = map.getBuildingAt(tx, ty);
        StringBuilder sb = new StringBuilder();
        sb.append("<html>Tile: ").append(tx).append(", ").append(ty).append("<br>");
        sb.append("Terreno: ").append(t.getName());
        if (b != null) {
            sb.append("<br>Construção: ").append(b.getType().getName())
              .append(" (").append(b.getProgress()).append("%)");
            if (b.getOwner() != null)
                sb.append("<br>Dono: ").append(b.getOwner());
        }
        JOptionPane.showMessageDialog(this, sb.toString(), "Info do Tile", JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth(), h = getHeight();
        int halfW = w / 2, halfH = h / 2;

        // Calculate visible range
        int startX = (int)(viewX - halfW / scale) - 1;
        int startY = (int)(viewY - halfH / scale) - 1;
        int endX = (int)(viewX + halfW / scale) + 2;
        int endY = (int)(viewY + halfH / scale) + 2;

        startX = Math.max(0, startX);
        startY = Math.max(0, startY);
        endX = Math.min(ColonyMap.WIDTH, endX);
        endY = Math.min(ColonyMap.HEIGHT, endY);

        // ─── Draw terrain tiles ───
        for (int y = startY; y < endY; y++) {
            for (int x = startX; x < endX; x++) {
                TerrainTile tile = map.getTile(x, y);
                int px = halfW + (int)((x - viewX) * scale);
                int py = halfH + (int)((y - viewY) * scale);
                int sz = (int)Math.ceil(scale) + 1;

                Color cor = tileColor(tile);
                g2.setColor(cor);
                g2.fillRect(px, py, sz, sz);

                if (scale >= 10) {
                    g2.setColor(new Color(Math.max(0, cor.getRed()-40),
                        Math.max(0, cor.getGreen()-40), Math.max(0, cor.getBlue()-40)));
                    g2.drawRect(px, py, sz, sz);
                }
            }
        }

        // ─── Draw buildings ───
        for (ColonyMap.ColonyBuilding b : map.getBuildings()) {
            int px = halfW + (int)((b.getX() - viewX) * scale);
            int py = halfH + (int)((b.getY() - viewY) * scale);
            int bw = (int)(b.getType().getWidth() * scale);
            int bh = (int)(b.getType().getHeight() * scale);

            Color cor = buildingColor(b.getType());
            g2.setColor(cor);
            g2.fillRect(px, py, bw, bh);
            g2.setColor(Color.BLACK);
            g2.drawRect(px, py, bw, bh);

            // Ícone/baseado no tipo
            String icon = b.getType().hasRoof() ? "\u2302" : "\u2591";
            g2.setFont(new Font("Monospaced", Font.PLAIN, Math.max(8, (int)(scale * 0.9))));
            g2.setColor(Color.BLACK);
            g2.drawString(icon, px + 2, py + (int)(scale * 0.8));

            // Nome do dono se for casa
            if (b.getOwner() != null && scale >= 10) {
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Monospaced", Font.PLAIN, Math.max(7, (int)(scale * 0.5))));
                g2.drawString(b.getOwner(), px + 2, py + bh - 2);
            }

            // Barra de progresso
            if (b.getProgress() < 100) {
                g2.setColor(Color.GRAY);
                g2.fillRect(px, py + bh + 2, bw, 3);
                g2.setColor(Color.GREEN);
                g2.fillRect(px, py + bh + 2, bw * b.getProgress() / 100, 3);
            }
        }

        // ─── Draw NPCs ───
        for (Map.Entry<String, int[]> npc : npcPositions.entrySet()) {
            int[] pos = npc.getValue();
            int px = halfW + (int)((pos[0] - viewX) * scale);
            int py = halfH + (int)((pos[1] - viewY) * scale);
            int r = Math.max(3, (int)(scale * 0.6));

            if (dwarfSpriteSheet != null) {
                int[] frame = getSpriteFrame(npc.getKey());
                int sx = frame[0] * FRAME_W;
                int sy = frame[1] * FRAME_H;
                int spriteSize = Math.max(4, (int)(scale * 1.2));
                g2.drawImage(dwarfSpriteSheet,
                    px - spriteSize/2, py - spriteSize/2,
                    px + spriteSize/2, py + spriteSize/2,
                    sx, sy, sx + FRAME_W, sy + FRAME_H, null);

                if (npc.getKey().equals(selectedNpc)) {
                    g2.setColor(Color.YELLOW);
                    g2.setStroke(new BasicStroke(2));
                    g2.drawRect(px - spriteSize/2 - 1, py - spriteSize/2 - 1, spriteSize + 2, spriteSize + 2);
                    g2.setStroke(new BasicStroke(1));
                }
            } else {
                Color cor = npcColors.getOrDefault(npc.getKey(), Color.WHITE);
                g2.setColor(cor);
                g2.fillOval(px - r, py - r, r*2, r*2);
                g2.setColor(Color.BLACK);
                g2.drawOval(px - r, py - r, r*2, r*2);

                if (npc.getKey().equals(selectedNpc)) {
                    g2.setColor(Color.YELLOW);
                    g2.drawOval(px - r - 2, py - r - 2, r*2 + 4, r*2 + 4);
                }
            }

            // Name label when zoomed in
            if (scale >= 14) {
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Monospaced", Font.BOLD, Math.max(8, (int)(scale * 0.6))));
                g2.drawString(npc.getKey(), px + r + 2, py + 4);
            }
        }

        // ─── Mouse hover coordinates ───
        int[] tile = getMouseTile();
        if (map.inBounds(tile[0], tile[1])) {
            g2.setColor(new Color(255, 255, 255, 100));
            int px = halfW + (int)((tile[0] - viewX) * scale);
            int py = halfH + (int)((tile[1] - viewY) * scale);
            int sz = (int)Math.ceil(scale) + 1;
            g2.drawRect(px, py, sz, sz);

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Monospaced", Font.PLAIN, 11));
            g2.drawString("(" + tile[0] + "," + tile[1] + ")", 10, getHeight() - 10);
        }

        // Grid lines when zoomed
        if (scale >= 16) {
            g2.setColor(new Color(255, 255, 255, 30));
            for (int y = startY; y <= endY; y++) {
                int py = halfH + (int)((y - viewY) * scale);
                g2.drawLine(0, py, w, py);
            }
            for (int x = startX; x <= endX; x++) {
                int px = halfW + (int)((x - viewX) * scale);
                g2.drawLine(px, 0, px, h);
            }
        }
    }

    private Color buildingColor(BuildingType type) {
        return switch (type.getZone()) {
            case "residencial" -> new Color(180, 130, 80);
            case "industrial" -> new Color(140, 100, 60);
            case "agrícola" -> new Color(120, 180, 70);
            case "serviços" -> new Color(200, 100, 100);
            case "militar" -> new Color(180, 60, 60);
            case "armazenamento" -> new Color(160, 150, 90);
            case "infraestrutura" -> new Color(100, 140, 180);
            case "comércio" -> new Color(200, 180, 60);
            default -> new Color(139, 90, 43);
        };
    }

    private Color tileColor(TerrainTile tile) {
        return switch (tile.getName()) {
            case "grama" -> new Color(80, 170, 60);
            case "terra" -> new Color(150, 120, 70);
            case "pedra" -> new Color(130, 130, 135);
            case "montanha" -> new Color(90, 85, 95);
            case "árvore" -> new Color(40, 130, 30);
            case "água" -> new Color(50, 110, 200);
            case "areia" -> new Color(220, 200, 140);
            case "parede" -> new Color(100, 75, 55);
            case "chão" -> new Color(170, 155, 115);
            default -> Color.GRAY;
        };
    }
}
