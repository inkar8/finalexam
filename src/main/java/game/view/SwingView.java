package game.view;

import game.model.Labyrinth;
import game.model.Player;
import game.model.Position;
import game.model.rooms.Room;
import game.model.rooms.RoomType;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Swing-based implementation of the game view with enhanced graphics.
 */
public class SwingView implements GameView {
    private Labyrinth labyrinth;
    private Player player;
    private JFrame frame;
    private JTextArea outputArea;
    private JTextField inputField;
    private JPanel mapPanel;
    private JPanel minimapPanel;
    private JLabel statusLabel;
    private BlockingQueue<String> inputQueue;

    // Animation properties
    private Timer animationTimer;
    private Position previousPlayerPos;
    private Position currentPlayerPos;
    private double animationProgress = 1.0;
    private boolean isAnimating = false;

    // Minimap properties
    private int minimapScale = 5;

    public SwingView(Labyrinth labyrinth, Player player) {
        this.labyrinth = labyrinth;
        this.player = player;
        this.inputQueue = new LinkedBlockingQueue<>();
        this.currentPlayerPos = player.getPosition();
        this.previousPlayerPos = new Position(currentPlayerPos);

        this.animationTimer = new Timer(16, e -> {
            if (isAnimating) {
                animationProgress += 0.1;
                if (animationProgress >= 1.0) {
                    animationProgress = 1.0;
                    isAnimating = false;
                }
                mapPanel.repaint();
            }
        });
        animationTimer.setRepeats(true);
        animationTimer.start();

        SwingUtilities.invokeLater(this::createAndShowGUI);
    }

    private void createAndShowGUI() {
        frame = new JFrame("Magical Labyrinth: Escape from the Dungeon");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(875, 875);
        frame.setLayout(new BorderLayout(0, 0));
        frame.getContentPane().setBackground(new Color(40, 40, 60));

        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setLineWrap(true);
        outputArea.setWrapStyleWord(true);
        outputArea.setFont(new Font("Monospaced", Font.BOLD, 14));
        outputArea.setBackground(new Color(30, 30, 50));
        outputArea.setForeground(new Color(200, 200, 255));
        outputArea.setMargin(new Insets(5, 5, 5, 5));
        JScrollPane scrollPane = new JScrollPane(outputArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(100, 100, 200), 2));
        frame.add(scrollPane, BorderLayout.EAST);
        scrollPane.setPreferredSize(new Dimension(400, 600));

        statusLabel = new JLabel("HP: 0/0 | Level: 0");
        statusLabel.setFont(new Font("Dialog", Font.BOLD, 16));
        statusLabel.setForeground(new Color(255, 220, 150));
        statusLabel.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(100, 100, 200)));
        statusLabel.setOpaque(true);
        statusLabel.setBackground(new Color(60, 60, 80));
        frame.add(statusLabel, BorderLayout.NORTH);

        JPanel mapContainer = new JPanel(new BorderLayout());
        mapContainer.setBackground(new Color(40, 40, 60));
        mapContainer.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(100, 100, 200), 2),
                "Dungeon Map",
                TitledBorder.CENTER,
                TitledBorder.TOP,
                new Font("Dialog", Font.BOLD, 14),
                new Color(200, 200, 255)
        ));

        mapPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (g instanceof Graphics2D) {
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                }
                setBackground(new Color(20, 20, 40));
                drawMap(g);
            }
        };
        mapPanel.setPreferredSize(new Dimension(300, 300));
        mapContainer.add(mapPanel, BorderLayout.CENTER);

        JPanel legendPanel = new JPanel(new GridLayout(3, 3, 2, 2));
        legendPanel.setBackground(new Color(40, 40, 60));
        addLegendItem(legendPanel, Color.WHITE, "Regular");
        addLegendItem(legendPanel, Color.BLUE, "Puzzle");
        addLegendItem(legendPanel, Color.YELLOW, "Treasure");
        addLegendItem(legendPanel, Color.RED, "Monster");
        addLegendItem(legendPanel, Color.ORANGE, "Trap");
        addLegendItem(legendPanel, Color.GREEN, "Exit");
        addLegendItem(legendPanel, Color.LIGHT_GRAY, "Unexplored");
        addLegendItem(legendPanel, Color.BLACK, "Player");
        mapContainer.add(legendPanel, BorderLayout.SOUTH);

        JPanel minimapContainer = new JPanel(new BorderLayout());
        minimapContainer.setBackground(new Color(40, 40, 60));
        minimapContainer.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(100, 100, 200), 2),
                "World Map",
                TitledBorder.CENTER,
                TitledBorder.TOP,
                new Font("Dialog", Font.BOLD, 12),
                new Color(200, 200, 255)
        ));

        minimapPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (g instanceof Graphics2D) {
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                }
                setBackground(new Color(15, 15, 25));
                drawMinimap(g);
            }
        };
        minimapPanel.setPreferredSize(new Dimension(300, 150));

        JPanel minimapLegend = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 1));
        minimapLegend.setBackground(new Color(30, 30, 45));
        JPanel regularColor = createCompactLegendItem(new Color(200, 200, 220), "Regular");
        JPanel treasureColor = createCompactLegendItem(new Color(255, 215, 0), "Treasure");
        JPanel monsterColor = createCompactLegendItem(new Color(255, 80, 80), "Monster");
        JPanel puzzleColor = createCompactLegendItem(new Color(100, 150, 255), "Puzzle");
        JPanel trapColor = createCompactLegendItem(new Color(255, 150, 0), "Trap");
        JPanel exitColor = createCompactLegendItem(new Color(100, 255, 100), "Exit");
        minimapLegend.add(regularColor);
        minimapLegend.add(treasureColor);
        minimapLegend.add(monsterColor);
        minimapLegend.add(puzzleColor);
        minimapLegend.add(trapColor);
        minimapLegend.add(exitColor);
        minimapContainer.add(minimapPanel, BorderLayout.CENTER);
        minimapContainer.add(minimapLegend, BorderLayout.SOUTH);

        JPanel mapAndMinimapContainer = new JPanel(new BorderLayout());
        mapAndMinimapContainer.setBackground(new Color(40, 40, 60));
        mapAndMinimapContainer.add(mapContainer, BorderLayout.CENTER);
        mapAndMinimapContainer.add(minimapContainer, BorderLayout.SOUTH);
        frame.add(mapAndMinimapContainer, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new BorderLayout(2, 2));
        inputPanel.setBackground(new Color(60, 60, 80));
        inputPanel.setBorder(BorderFactory.createMatteBorder(2, 0, 0, 0, new Color(100, 100, 200)));

        inputField = new JTextField();
        inputField.setFont(new Font("Monospaced", Font.PLAIN, 14));
        inputField.setBackground(new Color(30, 30, 50));
        inputField.setForeground(new Color(200, 200, 255));
        inputField.setCaretColor(Color.WHITE);
        inputField.setBorder(BorderFactory.createLineBorder(new Color(100, 100, 200), 1));

        inputField.addActionListener(e -> {
            String input = inputField.getText();
            inputQueue.add(input);
            inputField.setText("");
        });

        inputField.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {}
            @Override
            public void keyPressed(KeyEvent e) {
                int keyCode = e.getKeyCode();
                if (keyCode == KeyEvent.VK_UP) inputQueue.add("north");
                else if (keyCode == KeyEvent.VK_DOWN) inputQueue.add("south");
                else if (keyCode == KeyEvent.VK_LEFT) inputQueue.add("west");
                else if (keyCode == KeyEvent.VK_RIGHT) inputQueue.add("east");
            }
            @Override
            public void keyReleased(KeyEvent e) {}
        });

        JLabel cmdLabel = new JLabel("Command: ");
        cmdLabel.setFont(new Font("Dialog", Font.BOLD, 14));
        cmdLabel.setForeground(new Color(180, 180, 220));

        JPanel buttonPanel = new JPanel(new BorderLayout(2, 2));
        buttonPanel.setBackground(new Color(60, 60, 80));

        JPanel dirButtons = new JPanel(new GridLayout(3, 3));
        dirButtons.setBackground(new Color(60, 60, 80));

        JButton[] buttons = new JButton[9];
        for (int i = 0; i < 9; i++) {
            buttons[i] = new JButton();
            buttons[i].setFocusable(false);
            buttons[i].setBackground(new Color(70, 70, 90));
            buttons[i].setForeground(new Color(200, 200, 255));
            buttons[i].setBorder(BorderFactory.createLineBorder(new Color(100, 100, 150), 1));
            dirButtons.add(buttons[i]);
        }

        buttons[1].setText("↑");
        buttons[1].setFont(new Font("Dialog", Font.BOLD, 16));
        buttons[1].addActionListener(e -> inputQueue.add("north"));

        buttons[3].setText("←");
        buttons[3].setFont(new Font("Dialog", Font.BOLD, 16));
        buttons[3].addActionListener(e -> inputQueue.add("west"));

        buttons[4].setText("·");
        buttons[4].setFont(new Font("Dialog", Font.BOLD, 16));
        buttons[4].addActionListener(e -> inputQueue.add("wait"));

        buttons[5].setText("→");
        buttons[5].setFont(new Font("Dialog", Font.BOLD, 16));
        buttons[5].addActionListener(e -> inputQueue.add("east"));

        buttons[7].setText("↓");
        buttons[7].setFont(new Font("Dialog", Font.BOLD, 16));
        buttons[7].addActionListener(e -> inputQueue.add("south"));

        JPanel commonButtons = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 0));
        commonButtons.setBackground(new Color(60, 60, 80));

        String[] cmdNames = {"Look", "Help", "Inventory", "Status"};
        for (String cmd : cmdNames) {
            JButton button = new JButton(cmd);
            button.setBackground(new Color(40, 60, 100));
            button.setForeground(new Color(200, 200, 255));
            button.setFocusable(false);
            button.setFont(new Font("Dialog", Font.BOLD, 12));
            button.setBorder(BorderFactory.createLineBorder(new Color(80, 100, 150), 1));
            String cmdLower = cmd.toLowerCase();
            button.addActionListener(e -> inputQueue.add(cmdLower));
            commonButtons.add(button);
        }

        buttonPanel.add(dirButtons, BorderLayout.CENTER);
        buttonPanel.add(commonButtons, BorderLayout.SOUTH);
        inputPanel.add(cmdLabel, BorderLayout.WEST);
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(buttonPanel, BorderLayout.EAST);
        frame.add(inputPanel, BorderLayout.SOUTH);

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        inputField.requestFocusInWindow();
    }

    @Override
    public void update() {
        if (SwingUtilities.isEventDispatchThread()) {
            updateUI();
        } else {
            SwingUtilities.invokeLater(this::updateUI);
        }
    }

    private void updateUI() {
        Position newPos = player.getPosition();
        if (!newPos.equals(currentPlayerPos) && !isAnimating) {
            previousPlayerPos = new Position(currentPlayerPos);
            currentPlayerPos = new Position(newPos);
            animationProgress = 0.0;
            isAnimating = true;
        }

        String healthBar = createHealthBar(player.getHealth(), player.getMaxHealth());
        statusLabel.setText(player.getName() + " | " + healthBar + " " +
                player.getHealth() + "/" + player.getMaxHealth() +
                " | Level: " + player.getLevel() + " | Loc: " + player.getPosition());

        mapPanel.repaint();
        if (minimapPanel != null) {
            minimapPanel.repaint();
        }
    }

    private String createHealthBar(int health, int maxHealth) {
        final int barLength = 10;
        int filledSegments = Math.round((float) health / maxHealth * barLength);
        if (health > 0 && filledSegments == 0) filledSegments = 1;
        StringBuilder bar = new StringBuilder("HP: [");
        for (int i = 0; i < filledSegments; i++) bar.append("█");
        for (int i = filledSegments; i < barLength; i++) bar.append("░");
        bar.append("]");
        return bar.toString();
    }

    private void drawMap(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        int playerX, playerY;
        if (isAnimating) {
            playerX = (int) (previousPlayerPos.getX() * (1.0 - animationProgress) +
                    currentPlayerPos.getX() * animationProgress);
            playerY = (int) (previousPlayerPos.getY() * (1.0 - animationProgress) +
                    currentPlayerPos.getY() * animationProgress);
        } else {
            playerX = currentPlayerPos.getX();
            playerY = currentPlayerPos.getY();
        }

        int cellSizeX = mapPanel.getWidth() / labyrinth.getWidth();
        int cellSizeY = mapPanel.getHeight() / labyrinth.getHeight();
        int cellSize = Math.min(cellSizeX, cellSizeY);
        int mapWidth = mapPanel.getWidth() / cellSize;
        int mapHeight = mapPanel.getHeight() / cellSize;

        int startX = Math.max(0, playerX - mapWidth / 2);
        int startY = Math.max(0, playerY - mapHeight / 2);
        int endX = Math.min(labyrinth.getWidth() - 1, startX + mapWidth - 1);
        int endY = Math.min(labyrinth.getHeight() - 1, startY + mapHeight - 1);

        if (endX - startX + 1 < mapWidth) startX = Math.max(0, endX - mapWidth + 1);
        if (endY - startY + 1 < mapHeight) startY = Math.max(0, endY - mapHeight + 1);

        g2d.setColor(new Color(15, 15, 25));
        g2d.fillRect(0, 0, mapPanel.getWidth(), mapPanel.getHeight());

        g2d.setColor(new Color(40, 40, 60));
        for (int i = 0; i <= mapWidth; i++) {
            g2d.drawLine(i * cellSize, 0, i * cellSize, mapHeight * cellSize);
        }
        for (int i = 0; i <= mapHeight; i++) {
            g2d.drawLine(0, i * cellSize, mapWidth * cellSize, i * cellSize);
        }

        for (int y = startY; y <= endY; y++) {
            for (int x = startX; x <= endX; x++) {
                int drawX = (x - startX) * cellSize;
                int drawY = (y - startY) * cellSize;
                Room room = labyrinth.getRoomAt(new Position(x, y));
                if (room == null) {
                    drawWall(g2d, drawX, drawY, cellSize);
                } else {
                    if (!room.isVisited()) {
                        drawUnexplored(g2d, drawX, drawY, cellSize);
                    } else {
                        drawRoom(g2d, drawX, drawY, cellSize, room.getType());
                    }
                }
            }
        }

        double drawPlayerX, drawPlayerY;
        if (isAnimating) {
            drawPlayerX = (previousPlayerPos.getX() - startX) * cellSize * (1.0 - animationProgress) +
                    (currentPlayerPos.getX() - startX) * cellSize * animationProgress;
            drawPlayerY = (previousPlayerPos.getY() - startY) * cellSize * (1.0 - animationProgress) +
                    (currentPlayerPos.getY() - startY) * cellSize * animationProgress;
        } else {
            drawPlayerX = (currentPlayerPos.getX() - startX) * cellSize;
            drawPlayerY = (currentPlayerPos.getY() - startY) * cellSize;
        }

        drawPlayer(g2d, (int) drawPlayerX, (int) drawPlayerY, cellSize);
    }

    private void drawWall(Graphics2D g, int x, int y, int size) {
        GradientPaint gradient = new GradientPaint(
                x, y, new Color(60, 60, 70),
                x + size, y + size, new Color(40, 40, 50)
        );
        g.setPaint(gradient);
        g.fillRect(x, y, size, size);
        g.setColor(new Color(30, 30, 40, 100));
        for (int i = 0; i < 5; i++) {
            int rx = x + (int) (Math.random() * size);
            int ry = y + (int) (Math.random() * size);
            int rs = 3 + (int) (Math.random() * 5);
            g.fillOval(rx, ry, rs, rs);
        }
        g.setColor(new Color(20, 20, 30));
        g.drawRect(x, y, size - 1, size - 1);
    }

    private void drawUnexplored(Graphics2D g, int x, int y, int size) {
        g.setColor(new Color(120, 120, 140));
        g.fillRect(x, y, size, size);
        g.setColor(new Color(140, 140, 160, 150));
        for (int i = 0; i < 8; i++) {
            int rx = x + (int) (Math.random() * size);
            int ry = y + (int) (Math.random() * size);
            int rs = 5 + (int) (Math.random() * 8);
            g.fillOval(rx, ry, rs, rs);
        }
        g.setFont(new Font("Dialog", Font.BOLD, 16));
        g.setColor(new Color(60, 60, 80));
        g.drawString("?", x + size / 2 - 5, y + size / 2 + 6);
        g.setColor(new Color(100, 100, 120));
        g.drawRect(x, y, size - 1, size - 1);
    }

    private void drawRoom(Graphics2D g, int x, int y, int size, RoomType type) {
        long currentTime = System.currentTimeMillis();
        float animFactor = (float) Math.sin(currentTime / 600.0) * 0.5f + 0.5f;
        Color floorBaseColor, borderColor, decorationColor;
        switch (type) {
            case REGULAR:
                floorBaseColor = new Color(180, 180, 200);
                borderColor = new Color(150, 150, 170);
                decorationColor = new Color(150, 150, 170);
                break;
            case PUZZLE:
                floorBaseColor = new Color(100, 150, 255);
                borderColor = new Color(50, 100, 200);
                decorationColor = new Color(220, 220, 255);
                break;
            case TREASURE:
                floorBaseColor = new Color(255, 215, 0);
                borderColor = new Color(205, 165, 0);
                decorationColor = new Color(255, 255, 200);
                break;
            case MONSTER:
                floorBaseColor = new Color(200, 180, 180);
                borderColor = new Color(180, 40, 40);
                decorationColor = new Color(255, 80, 80);
                break;
            case TRAP:
                floorBaseColor = new Color(220, 200, 180);
                borderColor = new Color(200, 100, 0);
                decorationColor = new Color(255, 150, 0);
                break;
            case EXIT:
                floorBaseColor = new Color(150, 255, 150);
                borderColor = new Color(0, 180, 0);
                decorationColor = new Color(200, 255, 200);
                break;
            default:
                floorBaseColor = Color.GRAY;
                borderColor = Color.DARK_GRAY;
                decorationColor = Color.LIGHT_GRAY;
                break;
        }
        g.setColor(floorBaseColor);
        g.fillRect(x, y, size, size);
        drawFloorPattern(g, x, y, size, floorBaseColor.darker());
        switch (type) {
            case REGULAR:
                drawStoneFloor(g, x, y, size);
                break;
            case PUZZLE:
                drawMagicRunes(g, x, y, size, animFactor);
                break;
            case TREASURE:
                drawTreasure(g, x, y, size, animFactor);
                break;
            case MONSTER:
                drawBloodMarks(g, x, y, size);
                break;
            case TRAP:
                drawTrapMarkings(g, x, y, size, animFactor);
                break;
            case EXIT:
                drawExitPortal(g, x, y, size, animFactor);
                break;
        }
        drawRoomLighting(g, x, y, size);
        draw3DBorder(g, x, y, size, borderColor);
    }

    private void drawFloorPattern(Graphics2D g, int x, int y, int size, Color lineColor) {
        g.setColor(new Color(lineColor.getRed(), lineColor.getGreen(), lineColor.getBlue(), 40));
        int tileSize = size / 4;
        for (int i = 0; i <= 4; i++) {
            g.drawLine(x, y + i * tileSize, x + size, y + i * tileSize);
            g.drawLine(x + i * tileSize, y, x + i * tileSize, y + size);
        }
    }

    private void drawStoneFloor(Graphics2D g, int x, int y, int size) {
        g.setColor(new Color(100, 100, 120, 80));
        Random random = new Random(x * 31 + y * 17);
        for (int i = 0; i < 6; i++) {
            int stoneX = x + random.nextInt(size);
            int stoneY = y + random.nextInt(size);
            int stoneSize = 3 + random.nextInt(5);
            g.fillRect(stoneX, stoneY, stoneSize, stoneSize);
        }
    }

    private void drawMagicRunes(Graphics2D g, int x, int y, int size, float animFactor) {
        int runeSize = size / 8;
        Composite oldComposite = g.getComposite();
        g.setColor(new Color(150, 200, 255));
        g.drawOval(x + size / 4, y + size / 4, size / 2, size / 2);
        g.setFont(new Font("Serif", Font.BOLD, runeSize));
        g.setColor(new Color(220, 220, 255));
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f + 0.5f * animFactor));
        char[] runes = {'Ψ', '⦿', '⧗', '⧍', '♅', '☤', '⚝'};
        Random random = new Random(x * 17 + y * 31);
        for (int i = 0; i < 4; i++) {
            int runeX = x + size / 4 + random.nextInt(size / 2);
            int runeY = y + size / 4 + random.nextInt(size / 2);
            char rune = runes[random.nextInt(runes.length)];
            g.drawString(String.valueOf(rune), runeX, runeY);
        }
        g.setComposite(oldComposite);
    }

    private void drawTreasure(Graphics2D g, int x, int y, int size, float animFactor) {
        int chestWidth = size / 2;
        int chestHeight = size / 3;
        int chestX = x + (size - chestWidth) / 2;
        int chestY = y + (size - chestHeight) / 2;
        g.setColor(new Color(139, 69, 19));
        g.fillRect(chestX, chestY, chestWidth, chestHeight);
        g.setColor(new Color(160, 82, 45));
        g.fillRect(chestX, chestY - chestHeight / 4, chestWidth, chestHeight / 4);
        g.setColor(new Color(218, 165, 32));
        g.fillRect(chestX, chestY + chestHeight / 3, chestWidth, chestHeight / 6);
        g.setColor(new Color(255, 215, 0));
        g.fillRect(chestX + chestWidth / 2 - 3, chestY, 6, 6);
        float glintAlpha = 0.3f + 0.7f * animFactor;
        g.setColor(new Color(255, 255, 180, (int) (glintAlpha * 255)));
        Random random = new Random(x * 7 + y * 13);
        for (int i = 0; i < 5; i++) {
            int gemX = x + random.nextInt(size);
            int gemY = y + random.nextInt(size);
            int gemSize = 3 + random.nextInt(3);
            if (random.nextBoolean()) {
                g.setColor(new Color(255, 223, 0, (int) (glintAlpha * 255)));
                g.fillOval(gemX, gemY, gemSize, gemSize);
            } else {
                g.setColor(new Color(random.nextInt(100), 100 + random.nextInt(155), 200 + random.nextInt(55),
                        (int) (glintAlpha * 255)));
                g.fillRect(gemX, gemY, gemSize, gemSize);
            }
        }
    }

    private void drawBloodMarks(Graphics2D g, int x, int y, int size) {
        g.setColor(new Color(180, 0, 0, 120));
        Random random = new Random(x * 11 + y * 23);
        for (int i = 0; i < 8; i++) {
            int spatterX = x + random.nextInt(size);
            int spatterY = y + random.nextInt(size);
            int spatterSize = 2 + random.nextInt(6);
            g.fillOval(spatterX, spatterY, spatterSize, spatterSize);
            if (random.nextBoolean()) {
                int dripLength = 3 + random.nextInt(8);
                g.fillRect(spatterX + spatterSize / 2, spatterY + spatterSize, 1, dripLength);
            }
        }
        g.setStroke(new BasicStroke(1.5f));
        for (int i = 0; i < 2; i++) {
            int clawX = x + random.nextInt(size - size / 3);
            int clawY = y + random.nextInt(size - size / 3);
            for (int j = 0; j < 3; j++) {
                int startX = clawX + j * 4;
                int startY = clawY;
                int endX = startX + random.nextInt(10) - 5;
                int endY = startY + 10 + random.nextInt(10);
                g.drawLine(startX, startY, endX, endY);
            }
        }
        g.setStroke(new BasicStroke(1.0f));
    }

    private void drawTrapMarkings(Graphics2D g, int x, int y, int size, float animFactor) {
        Stroke oldStroke = g.getStroke();
        g.setColor(new Color(100, 80, 60));
        g.setStroke(new BasicStroke(1.5f));
        Random random = new Random(x * 41 + y * 47);
        for (int i = 0; i < 3; i++) {
            int startX = x + random.nextInt(size);
            int startY = y + random.nextInt(size);
            int lastX = startX;
            int lastY = startY;
            for (int j = 0; j < 3; j++) {
                int nextX = lastX + random.nextInt(size / 4) - size / 8;
                int nextY = lastY + random.nextInt(size / 4) - size / 8;
                nextX = Math.max(x, Math.min(x + size, nextX));
                nextY = Math.max(y, Math.min(y + size, nextY));
                g.drawLine(lastX, lastY, nextX, nextY);
                lastX = nextX;
                lastY = nextY;
            }
        }
        int triangleSize = size / 3;
        int triangleX = x + (size - triangleSize) / 2;
        int triangleY = y + (size - triangleSize) / 2;
        int[] xPoints = {triangleX + triangleSize / 2, triangleX + triangleSize, triangleX};
        int[] yPoints = {triangleY, triangleY + triangleSize, triangleY + triangleSize};
        float pulseAlpha = 0.4f + 0.6f * animFactor;
        g.setColor(new Color(255, 150, 0, (int) (pulseAlpha * 255)));
        g.fillPolygon(xPoints, yPoints, 3);
        g.setColor(new Color(0, 0, 0));
        g.setStroke(new BasicStroke(2.0f));
        g.drawPolygon(xPoints, yPoints, 3);
        g.setColor(new Color(0, 0, 0));
        g.setStroke(new BasicStroke(2.0f));
        int centerX = triangleX + triangleSize / 2;
        int centerY = triangleY + triangleSize / 2;
        g.drawLine(centerX, centerY - triangleSize / 4, centerX, centerY + triangleSize / 8);
        g.fillOval(centerX - 1, centerY + triangleSize / 4, 3, 3);
        g.setStroke(oldStroke);
    }

    private void drawExitPortal(Graphics2D g, int x, int y, int size, float animFactor) {
        Composite oldComposite = g.getComposite();
        int portalSize = (int) (size * (0.6f + 0.1f * animFactor));
        int portalX = x + (size - portalSize) / 2;
        int portalY = y + (size - portalSize) / 2;
        g.setColor(new Color(100, 255, 100));
        g.fillOval(portalX, portalY, portalSize, portalSize);
        int innerSize = (int) (portalSize * 0.8f);
        int innerX = x + (size - innerSize) / 2;
        int innerY = y + (size - innerSize) / 2;
        RadialGradientPaint gradient = new RadialGradientPaint(
                x + size / 2, y + size / 2, innerSize / 2,
                new float[]{0.0f, 0.5f, 1.0f},
                new Color[]{new Color(255, 255, 255), new Color(150, 255, 150), new Color(0, 180, 0)}
        );
        g.setPaint(gradient);
        g.fillOval(innerX, innerY, innerSize, innerSize);
        g.setColor(new Color(255, 255, 255, 150));
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
        double angle = System.currentTimeMillis() / 1000.0;
        for (int i = 0; i < 4; i++) {
            double currentAngle = angle + i * Math.PI / 2;
            int swirlX = (int) (x + size / 2 + Math.cos(currentAngle) * innerSize / 4);
            int swirlY = (int) (y + size / 2 + Math.sin(currentAngle) * innerSize / 4);
            int swirlSize = innerSize / 8;
            g.fillOval(swirlX - swirlSize / 2, swirlY - swirlSize / 2, swirlSize, swirlSize);
        }
        g.setColor(new Color(255, 255, 255, 100));
        g.setStroke(new BasicStroke(2.0f));
        int arrowLength = size / 8;
        drawArrow(g, x + size / 2, y + size / 5, x + size / 2, y + size / 2, 6);
        drawArrow(g, x + size * 4 / 5, y + size / 2, x + size / 2, y + size / 2, 6);
        drawArrow(g, x + size / 2, y + size * 4 / 5, x + size / 2, y + size / 2, 6);
        drawArrow(g, x + size / 5, y + size / 2, x + size / 2, y + size / 2, 6);
        g.setComposite(oldComposite);
    }

    private void drawArrow(Graphics2D g, int x1, int y1, int x2, int y2, int arrowSize) {
        g.drawLine(x1, y1, x2, y2);
        double angle = Math.atan2(y2 - y1, x2 - x1);
        int[] xPoints = new int[3];
        int[] yPoints = new int[3];
        xPoints[0] = x2;
        yPoints[0] = y2;
        xPoints[1] = (int) (x2 - arrowSize * Math.cos(angle - Math.PI / 6));
        yPoints[1] = (int) (y2 - arrowSize * Math.sin(angle - Math.PI / 6));
        xPoints[2] = (int) (x2 - arrowSize * Math.cos(angle + Math.PI / 6));
        yPoints[2] = (int) (y2 - arrowSize * Math.sin(angle + Math.PI / 6));
        g.fillPolygon(xPoints, yPoints, 3);
    }

    private void draw3DBorder(Graphics2D g, int x, int y, int size, Color borderColor) {
        g.setColor(borderColor.brighter());
        g.drawLine(x, y, x + size - 1, y);
        g.drawLine(x, y, x, y + size - 1);
        g.setColor(borderColor.darker().darker());
        g.drawLine(x, y + size - 1, x + size - 1, y + size - 1);
        g.drawLine(x + size - 1, y, x + size - 1, y + size - 1);
    }

    private void drawRoomLighting(Graphics2D g, int x, int y, int size) {
        int gradientSize = size / 3;
        GradientPaint cornerHighlight = new GradientPaint(
                x, y, new Color(255, 255, 255, 70),
                x + gradientSize, y + gradientSize, new Color(255, 255, 255, 0)
        );
        g.setPaint(cornerHighlight);
        g.fillRect(x, y, gradientSize, gradientSize);
        GradientPaint cornerShadow = new GradientPaint(
                x + size - gradientSize, y + size - gradientSize, new Color(0, 0, 0, 0),
                x + size, y + size, new Color(0, 0, 0, 70)
        );
        g.setPaint(cornerShadow);
        g.fillRect(x + size - gradientSize, y + size - gradientSize, gradientSize, gradientSize);
    }

    private void drawPlayer(Graphics2D g, int x, int y, int size) {
        Stroke oldStroke = g.getStroke();
        Paint oldPaint = g.getPaint();
        Composite oldComposite = g.getComposite();
        double breathFactor = Math.sin(System.currentTimeMillis() / 500.0) * 0.05 + 0.95;
        double bobFactor = Math.sin(System.currentTimeMillis() / 300.0) * 0.1;
        double stepFactor = isAnimating ? Math.sin(animationProgress * Math.PI * 2) * 0.1 : 0;
        double moveFactor = isAnimating ? Math.sin(animationProgress * Math.PI) * 0.1 + 0.9 : 1.0;
        int characterSize = (int) (size * 0.7 * moveFactor);
        int centerX = x + size / 2;
        int centerY = y + size / 2 + (int) (bobFactor * size / 10);
        int facingDirection = 0;
        if (isAnimating) {
            int dx = currentPlayerPos.getX() - previousPlayerPos.getX();
            int dy = currentPlayerPos.getY() - previousPlayerPos.getY();
            if (dx > 0) facingDirection = 1;
            else if (dx < 0) facingDirection = 3;
            else if (dy > 0) facingDirection = 2;
            else facingDirection = 0;
        }
        g.setColor(new Color(0, 0, 0, 70));
        g.fillOval(centerX - characterSize / 2 + 2, centerY + characterSize / 4, characterSize, characterSize / 4);
        switch (facingDirection) {
            case 0:
                drawHeroFromBehind(g, centerX, centerY, characterSize, stepFactor);
                break;
            case 1:
                drawHeroSideView(g, centerX, centerY, characterSize, stepFactor, true);
                break;
            case 2:
                drawHeroFrontView(g, centerX, centerY, characterSize, stepFactor);
                break;
            case 3:
                drawHeroSideView(g, centerX, centerY, characterSize, stepFactor, false);
                break;
        }
        drawHeroAura(g, centerX, centerY, characterSize, breathFactor);
        g.setPaint(oldPaint);
        g.setStroke(oldStroke);
        g.setComposite(oldComposite);
    }

    private void drawHeroFromBehind(Graphics2D g, int centerX, int centerY, int size, double stepFactor) {
        int headSize = size / 3;
        int bodyWidth = (int) (size * 0.6);
        int bodyHeight = (int) (size * 0.5);
        int legWidth = size / 6;
        int legHeight = size / 3;
        int armWidth = size / 7;
        int armLength = (int) (size * 0.3);
        int headY = centerY - size / 2 + headSize / 2;
        int bodyY = headY + headSize - 2;
        int legY = bodyY + bodyHeight - 2;
        g.setColor(new Color(60, 0, 120));
        int[] capeX = {
                centerX - bodyWidth / 2 - 4,
                centerX + bodyWidth / 2 + 4,
                centerX + bodyWidth / 2 + 2,
                centerX - bodyWidth / 2 - 2
        };
        int[] capeY = {
                headY - 2,
                headY - 2,
                bodyY + bodyHeight + 5,
                bodyY + bodyHeight + 5
        };
        g.fillPolygon(capeX, capeY, 4);
        g.setColor(new Color(60, 60, 100));
        g.fillRect(centerX - bodyWidth / 3 - legWidth / 2 - (int) (stepFactor * 2), legY, legWidth, legHeight);
        g.fillRect(centerX + bodyWidth / 3 - legWidth / 2 + (int) (stepFactor * 2), legY, legWidth, legHeight);
        g.setColor(new Color(0, 80, 160));
        g.fillRect(centerX - bodyWidth / 2, bodyY, bodyWidth, bodyHeight);
        g.setColor(new Color(0, 80, 160));
        g.fillRect(centerX - bodyWidth / 2 - armWidth / 2, bodyY + bodyHeight / 6, armWidth, armLength);
        g.fillRect(centerX + bodyWidth / 2 - armWidth / 2, bodyY + bodyHeight / 6, armWidth, armLength);
        g.setColor(new Color(225, 180, 120));
        g.fillOval(centerX - headSize / 2, headY - headSize / 2, headSize, headSize);
        g.setColor(new Color(80, 50, 20));
        g.fillArc(centerX - headSize / 2, headY - headSize / 2, headSize, headSize, 0, 180);
        g.setColor(new Color(180, 150, 80));
        g.fillRect(centerX + headSize / 2 - 2, headY - 2, 6, 8);
    }

    private void drawHeroFrontView(Graphics2D g, int centerX, int centerY, int size, double stepFactor) {
        int headSize = size / 3;
        int bodyWidth = (int) (size * 0.6);
        int bodyHeight = (int) (size * 0.5);
        int legWidth = size / 6;
        int legHeight = size / 3;
        int armWidth = size / 7;
        int armLength = (int) (size * 0.3);
        int headY = centerY - size / 2 + headSize / 2;
        int bodyY = headY + headSize - 2;
        int legY = bodyY + bodyHeight - 2;
        g.setColor(new Color(60, 60, 100));
        g.fillRect(centerX - bodyWidth / 3 - legWidth / 2 - (int) (stepFactor * 2), legY, legWidth, legHeight);
        g.fillRect(centerX + bodyWidth / 3 - legWidth / 2 + (int) (stepFactor * 2), legY, legWidth, legHeight);
        g.setColor(new Color(0, 80, 160));
        g.fillRect(centerX - bodyWidth / 2, bodyY, bodyWidth, bodyHeight);
        g.setColor(new Color(120, 80, 40));
        g.fillRect(centerX - bodyWidth / 2, bodyY + bodyHeight * 2 / 3, bodyWidth, 4);
        g.setColor(new Color(200, 180, 60));
        g.fillRect(centerX - 4, bodyY + bodyHeight * 2 / 3 - 1, 8, 6);
        g.setColor(new Color(0, 80, 160));
        g.fillRect(centerX - bodyWidth / 2 - (int) (stepFactor * 3), bodyY + 5, armWidth, armLength);
        g.fillRect(centerX + bodyWidth / 2 - armWidth + (int) (stepFactor * 3), bodyY + 5, armWidth, armLength);
        g.setColor(new Color(225, 180, 120));
        g.fillOval(centerX - bodyWidth / 2 - (int) (stepFactor * 3) - 1, bodyY + 5 + armLength - 6, armWidth + 2, 8);
        g.fillOval(centerX + bodyWidth / 2 - armWidth + (int) (stepFactor * 3) - 1, bodyY + 5 + armLength - 6, armWidth + 2, 8);
        g.setColor(new Color(225, 180, 120));
        g.fillOval(centerX - headSize / 2, headY - headSize / 2, headSize, headSize);
        g.setColor(new Color(80, 50, 20));
        g.fillArc(centerX - headSize / 2, headY - headSize / 2, headSize, headSize + 4, 180, 180);
        g.setColor(new Color(40, 40, 40));
        g.fillOval(centerX - headSize / 5 - 1, headY - 2, 4, 4);
        g.fillOval(centerX + headSize / 5 - 3, headY - 2, 4, 4);
        g.drawArc(centerX - headSize / 6, headY + headSize / 6, headSize / 3, headSize / 6, 0, 180);
    }

    private void drawHeroSideView(Graphics2D g, int centerX, int centerY, int size, double stepFactor, boolean facingRight) {
        int dir = facingRight ? 1 : -1;
        int headSize = size / 3;
        int bodyWidth = (int) (size * 0.4);
        int bodyHeight = (int) (size * 0.5);
        int legWidth = size / 6;
        int legHeight = size / 3;
        int armWidth = size / 7;
        int armLength = (int) (size * 0.3);
        int headY = centerY - size / 2 + headSize / 2;
        int bodyY = headY + headSize - 2;
        int legY = bodyY + bodyHeight - 2;
        g.setColor(new Color(60, 0, 120));
        int[] capeX = {centerX, centerX, centerX - dir * bodyWidth / 2, centerX - dir * (bodyWidth / 2 + 5)};
        int[] capeY = {headY - 2, bodyY + bodyHeight + 5, bodyY + bodyHeight + 5, headY + headSize / 2};
        g.fillPolygon(capeX, capeY, 4);
        g.setColor(new Color(60, 60, 100));
        g.fillRect(centerX - dir * (legWidth / 4) - dir * (int) (stepFactor * 4), legY, legWidth, legHeight);
        g.setColor(new Color(0, 80, 160));
        g.fillRect(centerX - dir * bodyWidth / 2, bodyY, bodyWidth, bodyHeight);
        g.setColor(new Color(120, 80, 40));
        g.fillRect(centerX - dir * bodyWidth / 2, bodyY + bodyHeight * 2 / 3, bodyWidth, 4);
        g.setColor(new Color(60, 60, 100));
        g.fillRect(centerX + dir * (legWidth / 4) + dir * (int) (stepFactor * 4), legY, legWidth, legHeight);
        g.setColor(new Color(0, 80, 160));
        int armY = bodyY + 5;
        int armSwing = (int) (stepFactor * 6);
        g.fillRect(centerX + dir * (bodyWidth / 2 - armWidth) + dir * armSwing, armY, armWidth, armLength);
        g.setColor(new Color(225, 180, 120));
        g.fillOval(centerX + dir * (bodyWidth / 2 - armWidth) + dir * armSwing - 1, armY + armLength - 6, armWidth + 2, 8);
        g.setColor(new Color(225, 180, 120));
        g.fillOval(centerX - headSize / 2, headY - headSize / 2, headSize, headSize);
        g.setColor(new Color(80, 50, 20));
        g.fillArc(centerX - headSize / 2, headY - headSize / 2, headSize, headSize, facingRight ? 270 : 90, 180);
        g.setColor(new Color(40, 40, 40));
        g.fillOval(centerX + dir * headSize / 6, headY - 2, 3, 4);
        g.drawLine(centerX + dir * headSize / 4, headY + 2, centerX + dir * headSize / 3, headY + 4);
        g.drawArc(centerX + dir * (headSize / 8), headY + headSize / 6, headSize / 6, headSize / 8, facingRight ? 0 : 180, 180);
    }

    private void drawHeroAura(Graphics2D g, int centerX, int centerY, int size, double breathFactor) {
        Composite oldComposite = g.getComposite();
        RadialGradientPaint auraGradient = new RadialGradientPaint(
                centerX, centerY, size / 2 + 10,
                new float[]{0.0f, 0.7f, 1.0f},
                new Color[]{new Color(200, 220, 255, 0), new Color(100, 180, 255, 30), new Color(50, 100, 255, 50)}
        );
        g.setPaint(auraGradient);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));
        int auraSize = (int) (size * (1.2 + breathFactor * 0.1));
        g.fillOval(centerX - auraSize / 2, centerY - auraSize / 2, auraSize, auraSize);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
        Random random = new Random(System.currentTimeMillis() / 500);
        for (int i = 0; i < 6; i++) {
            double angle = Math.PI * 2 * random.nextDouble();
            double distance = size / 2 * (0.8 + 0.4 * random.nextDouble());
            int sparkleX = (int) (centerX + Math.cos(angle) * distance);
            int sparkleY = (int) (centerY + Math.sin(angle) * distance);
            int sparkleSize = 1 + random.nextInt(3);
            g.setColor(new Color(200 + random.nextInt(55), 200 + random.nextInt(55), 255, 150 + random.nextInt(105)));
            g.fillOval(sparkleX, sparkleY, sparkleSize, sparkleSize);
        }
        g.setComposite(oldComposite);
    }

    @Override
    public void displayMessage(String message) {
        String styledMessage = message;
        if (message.contains("MONSTER") || message.contains("attacked") || message.contains("damage")) {
            styledMessage = "❗ " + message + " ❗";
        } else if (message.contains("TREASURE") || message.contains("gold") || message.contains("found")) {
            styledMessage = "✨ " + message + " ✨";
        } else if (message.contains("PUZZLE") || message.contains("riddle")) {
            styledMessage = "❓ " + message + " ❓";
        } else if (message.contains("TRAP") || message.contains("triggered")) {
            styledMessage = "⚠️ " + message + " ⚠️";
        } else if (message.contains("EXIT") || message.contains("escape")) {
            styledMessage = "🚪 " + message + " 🚪";
        } else if (message.contains("healed") || message.contains("level up")) {
            styledMessage = "➕ " + message + " ➕";
        }
        final String finalMessage = styledMessage + "\n";
        if (SwingUtilities.isEventDispatchThread()) {
            addMessageWithEffect(finalMessage);
        } else {
            SwingUtilities.invokeLater(() -> addMessageWithEffect(finalMessage));
        }
    }

    private void addMessageWithEffect(String message) {
        boolean isImportant = message.contains("❗") || message.contains("✨") ||
                message.contains("❓") || message.contains("⚠️") ||
                message.contains("🚪") || message.contains("➕");
        outputArea.append(message);
        outputArea.setCaretPosition(outputArea.getDocument().getLength());
        if (isImportant && mapPanel != null) {
            Color originalColor = outputArea.getBackground();
            Color flashColor;
            if (message.contains("❗")) flashColor = new Color(255, 100, 100);
            else if (message.contains("✨")) flashColor = new Color(255, 215, 0);
            else if (message.contains("❓")) flashColor = new Color(100, 150, 255);
            else if (message.contains("⚠️")) flashColor = new Color(255, 150, 0);
            else if (message.contains("🚪")) flashColor = new Color(100, 255, 100);
            else flashColor = new Color(200, 200, 255);
            Timer flashTimer = new Timer(100, null);
            final int[] flashCount = {0};
            flashTimer.addActionListener(e -> {
                if (flashCount[0] % 2 == 0) outputArea.setBackground(flashColor);
                else outputArea.setBackground(originalColor);
                flashCount[0]++;
                if (flashCount[0] >= 6) {
                    flashTimer.stop();
                    outputArea.setBackground(originalColor);
                }
            });
            flashTimer.start();
        }
    }

    @Override
    public String getPlayerInput(String prompt) {
        displayMessage(prompt);
        try {
            return inputQueue.take();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "";
        }
    }

    private void drawMinimap(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        Position playerPos = player.getPosition();
        int panelWidth = minimapPanel.getWidth();
        int panelHeight = minimapPanel.getHeight();
        g2d.setColor(new Color(10, 10, 20));
        g2d.fillRect(0, 0, panelWidth, panelHeight);
        int labWidth = labyrinth.getWidth();
        int labHeight = labyrinth.getHeight();
        int cellWidth = Math.max(1, panelWidth / (labWidth + 2));
        int cellHeight = Math.max(1, panelHeight / (labHeight + 2));
        int cellSize = Math.min(cellWidth, cellHeight);
        int offsetX = (panelWidth - (labWidth * cellSize)) / 2;
        int offsetY = (panelHeight - (labHeight * cellSize)) / 2;
        g2d.setColor(new Color(30, 30, 40));
        for (int x = 0; x <= labWidth; x++) {
            g2d.drawLine(offsetX + x * cellSize, offsetY, offsetX + x * cellSize, offsetY + labHeight * cellSize);
        }
        for (int y = 0; y <= labHeight; y++) {
            g2d.drawLine(offsetX, offsetY + y * cellSize, offsetX + labWidth * cellSize, offsetY + y * cellSize);
        }
        g2d.setColor(new Color(70, 70, 90));
        g2d.drawRect(offsetX - 1, offsetY - 1, labWidth * cellSize + 1, labHeight * cellSize + 1);
        for (int y = 0; y < labHeight; y++) {
            for (int x = 0; x < labWidth; x++) {
                Position pos = new Position(x, y);
                Room room = labyrinth.getRoomAt(pos);
                int drawX = offsetX + x * cellSize;
                int drawY = offsetY + y * cellSize;
                if (room == null) {
                    g2d.setColor(new Color(20, 20, 30));
                    g2d.fillRect(drawX, drawY, cellSize, cellSize);
                } else if (room.isVisited()) {
                    Color roomColor;
                    switch (room.getType()) {
                        case REGULAR:
                            roomColor = new Color(200, 200, 220);
                            break;
                        case PUZZLE:
                            roomColor = new Color(100, 150, 255);
                            break;
                        case TREASURE:
                            roomColor = new Color(255, 215, 0);
                            break;
                        case MONSTER:
                            roomColor = new Color(255, 80, 80);
                            break;
                        case TRAP:
                            roomColor = new Color(255, 150, 0);
                            break;
                        case EXIT:
                            roomColor = new Color(100, 255, 100);
                            break;
                        default:
                            roomColor = Color.GRAY;
                            break;
                    }
                    g2d.setColor(roomColor);
                    g2d.fillRect(drawX, drawY, cellSize, cellSize);
                    g2d.setColor(roomColor.darker());
                    g2d.drawRect(drawX, drawY, cellSize - 1, cellSize - 1);
                } else {
                    g2d.setColor(new Color(100, 100, 120));
                    g2d.fillRect(drawX, drawY, cellSize, cellSize);
                }
            }
        }
        int cellSizeMain = 30;
        int mapWidth = mapPanel.getWidth() / cellSizeMain;
        int mapHeight = mapPanel.getHeight() / cellSizeMain;
        int startX = Math.max(0, playerPos.getX() - mapWidth / 2);
        int startY = Math.max(0, playerPos.getY() - mapHeight / 2);
        int endX = Math.min(labyrinth.getWidth() - 1, startX + mapWidth - 1);
        int endY = Math.min(labyrinth.getHeight() - 1, startY + mapHeight - 1);
        if (endX - startX + 1 < mapWidth) startX = Math.max(0, endX - mapWidth + 1);
        if (endY - startY + 1 < mapHeight) startY = Math.max(0, endY - mapHeight + 1);
        g2d.setColor(new Color(255, 255, 255, 70));
        g2d.drawRect(offsetX + startX * cellSize, offsetY + startY * cellSize,
                (endX - startX + 1) * cellSize, (endY - startY + 1) * cellSize);
        int playerDrawX = offsetX + playerPos.getX() * cellSize;
        int playerDrawY = offsetY + playerPos.getY() * cellSize;
        double pulseSize = 0.8 + Math.sin(System.currentTimeMillis() / 200.0) * 0.2;
        int markerSize = (int) (cellSize * pulseSize);
        int markerX = playerDrawX + (cellSize - markerSize) / 2;
        int markerY = playerDrawY + (cellSize - markerSize) / 2;
        g2d.setColor(new Color(50, 200, 255, 150));
        g2d.fillOval(markerX - 1, markerY - 1, markerSize + 2, markerSize + 2);
        g2d.setColor(new Color(0, 100, 255));
        g2d.fillOval(markerX, markerY, markerSize, markerSize);
        g2d.setColor(new Color(150, 220, 255));
        g2d.fillOval(markerX + markerSize / 4, markerY + markerSize / 4, markerSize / 4, markerSize / 4);
    }

    private void addLegendItem(JPanel panel, Color color, String label) {
        JPanel item = new JPanel(new BorderLayout(2, 0));
        item.setBackground(new Color(40, 40, 60));
        JPanel colorBox = new JPanel();
        colorBox.setBackground(color);
        colorBox.setPreferredSize(new Dimension(15, 15));
        colorBox.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1));
        JLabel textLabel = new JLabel(label);
        textLabel.setForeground(new Color(200, 200, 255));
        textLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
        item.add(colorBox, BorderLayout.WEST);
        item.add(textLabel, BorderLayout.CENTER);
        panel.add(item);
    }

    private JPanel createCompactLegendItem(Color color, String label) {
        JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 1, 0));
        item.setBackground(new Color(30, 30, 45));
        JPanel colorBox = new JPanel();
        colorBox.setBackground(color);
        colorBox.setPreferredSize(new Dimension(8, 8));
        colorBox.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1));
        JLabel textLabel = new JLabel(label);
        textLabel.setForeground(new Color(180, 180, 220));
        textLabel.setFont(new Font("Dialog", Font.PLAIN, 9));
        item.add(colorBox);
        item.add(textLabel);
        return item;
    }
}