package game.view;

import game.model.Labyrinth;
import game.model.Player;
import game.model.Position;
import game.model.rooms.Room;
import game.model.rooms.RoomType;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.AlphaComposite;
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
    private double animationProgress = 1.0; // 0.0 to 1.0
    private boolean isAnimating = false;
    
    // Minimap properties
    private int minimapScale = 5; // Each room is 5x5 pixels on minimap
    
    /**
     * Creates a new Swing view.
     * 
     * @param labyrinth The labyrinth model
     * @param player    The player model
     */
    public SwingView(Labyrinth labyrinth, Player player) {
        this.labyrinth = labyrinth;
        this.player = player;
        this.inputQueue = new LinkedBlockingQueue<>();
        this.currentPlayerPos = player.getPosition();
        this.previousPlayerPos = new Position(currentPlayerPos);
        
        // Setup animation timer
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
    
    /**
     * Creates and displays the GUI.
     */
    private void createAndShowGUI() {
        // Create the main frame
        frame = new JFrame("Magical Labyrinth: Escape from the Dungeon");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 750);  // Slightly larger to accommodate minimap
        frame.setLayout(new BorderLayout(10, 10));
        frame.getContentPane().setBackground(new Color(40, 40, 60)); // Dark blue background
        
        // Create the output text area with styled look
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setLineWrap(true);
        outputArea.setWrapStyleWord(true);
        outputArea.setFont(new Font("Monospaced", Font.BOLD, 14));
        outputArea.setBackground(new Color(30, 30, 50)); // Dark blue-ish background
        outputArea.setForeground(new Color(200, 200, 255)); // Light blue text
        outputArea.setMargin(new Insets(10, 10, 10, 10));
        JScrollPane scrollPane = new JScrollPane(outputArea);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(10, 10, 10, 10),
            BorderFactory.createLineBorder(new Color(100, 100, 200), 2)
        ));
        frame.add(scrollPane, BorderLayout.CENTER);
        
        // Create status panel with styled look
        statusLabel = new JLabel("HP: 0/0 | Level: 0");
        statusLabel.setFont(new Font("Dialog", Font.BOLD, 16));
        statusLabel.setForeground(new Color(255, 220, 150)); // Gold text
        statusLabel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(10, 10, 10, 10),
            BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(100, 100, 200))
        ));
        statusLabel.setOpaque(true);
        statusLabel.setBackground(new Color(60, 60, 80)); // Slightly lighter than main background
        frame.add(statusLabel, BorderLayout.NORTH);
        
        // Create map panel with title and border
        JPanel mapContainer = new JPanel(new BorderLayout());
        mapContainer.setBackground(new Color(40, 40, 60));
        mapContainer.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(10, 10, 10, 10),
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(100, 100, 200), 2),
                "Dungeon Map",
                TitledBorder.CENTER,
                TitledBorder.TOP,
                new Font("Dialog", Font.BOLD, 14),
                new Color(200, 200, 255)
            )
        ));
        
        mapPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Use antialiasing for smoother graphics
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
        
        // Add a legend panel below the map
        JPanel legendPanel = new JPanel(new GridLayout(3, 3, 5, 5));
        legendPanel.setBackground(new Color(40, 40, 60));
        legendPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        // Add legend items
        addLegendItem(legendPanel, Color.WHITE, "Regular");
        addLegendItem(legendPanel, Color.BLUE, "Puzzle");
        addLegendItem(legendPanel, Color.YELLOW, "Treasure");
        addLegendItem(legendPanel, Color.RED, "Monster");
        addLegendItem(legendPanel, Color.ORANGE, "Trap");
        addLegendItem(legendPanel, Color.GREEN, "Exit");
        addLegendItem(legendPanel, Color.LIGHT_GRAY, "Unexplored");
        addLegendItem(legendPanel, Color.BLACK, "Player");
        
        mapContainer.add(legendPanel, BorderLayout.SOUTH);
        
        // Create minimap panel to display the entire explored area
        JPanel minimapContainer = new JPanel(new BorderLayout());
        minimapContainer.setBackground(new Color(40, 40, 60));
        minimapContainer.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(5, 5, 5, 5),
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(100, 100, 200), 2),
                "World Map",
                TitledBorder.CENTER,
                TitledBorder.TOP,
                new Font("Dialog", Font.BOLD, 12),
                new Color(200, 200, 255)
            )
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
        
        // Create a minimap legend panel with compact color squares
        JPanel minimapLegend = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 2));
        minimapLegend.setBackground(new Color(30, 30, 45));
        
        // Add compact color indicators for the minimap
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
        
        // Add both map and minimap to a container
        JPanel mapAndMinimapContainer = new JPanel(new BorderLayout());
        mapAndMinimapContainer.setBackground(new Color(40, 40, 60));
        mapAndMinimapContainer.add(mapContainer, BorderLayout.CENTER);
        mapAndMinimapContainer.add(minimapContainer, BorderLayout.SOUTH);
        
        frame.add(mapAndMinimapContainer, BorderLayout.EAST);
        
        // Create styled input panel
        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
        inputPanel.setBackground(new Color(60, 60, 80));
        inputPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(2, 0, 0, 0, new Color(100, 100, 200)),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        // Styled input field
        inputField = new JTextField();
        inputField.setFont(new Font("Monospaced", Font.PLAIN, 14));
        inputField.setBackground(new Color(30, 30, 50));
        inputField.setForeground(new Color(200, 200, 255));
        inputField.setCaretColor(Color.WHITE);
        inputField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(100, 100, 200), 1),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        
        inputField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String input = inputField.getText();
                inputQueue.add(input);
                inputField.setText("");
            }
        });
        
        // Add key listener for arrow key navigation
        inputField.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                // Not used
            }
            
            @Override
            public void keyPressed(KeyEvent e) {
                int keyCode = e.getKeyCode();
                if (keyCode == KeyEvent.VK_UP) {
                    inputQueue.add("north");
                } else if (keyCode == KeyEvent.VK_DOWN) {
                    inputQueue.add("south");
                } else if (keyCode == KeyEvent.VK_LEFT) {
                    inputQueue.add("west");
                } else if (keyCode == KeyEvent.VK_RIGHT) {
                    inputQueue.add("east");
                }
            }
            
            @Override
            public void keyReleased(KeyEvent e) {
                // Not used
            }
        });
        
        // Create styled label for command
        JLabel cmdLabel = new JLabel("Command: ");
        cmdLabel.setFont(new Font("Dialog", Font.BOLD, 14));
        cmdLabel.setForeground(new Color(180, 180, 220));
        
        // Add navigation buttons panel
        JPanel buttonPanel = new JPanel(new BorderLayout(5, 5));
        buttonPanel.setBackground(new Color(60, 60, 80));
        
        // Button grid for movement (up, down, left, right)
        JPanel dirButtons = new JPanel(new GridLayout(3, 3));
        dirButtons.setBackground(new Color(60, 60, 80));
        
        // Create navigation buttons with improved styling
        JButton[] buttons = new JButton[9];
        for (int i = 0; i < 9; i++) {
            buttons[i] = new JButton();
            buttons[i].setFocusable(false);
            buttons[i].setBackground(new Color(70, 70, 90));
            buttons[i].setForeground(new Color(200, 200, 255));
            buttons[i].setBorder(BorderFactory.createLineBorder(new Color(100, 100, 150), 1));
            dirButtons.add(buttons[i]);
        }
        
        // Up button
        buttons[1].setText("↑");
        buttons[1].setFont(new Font("Dialog", Font.BOLD, 16));
        buttons[1].addActionListener(e -> inputQueue.add("north"));
        
        // Left button
        buttons[3].setText("←");
        buttons[3].setFont(new Font("Dialog", Font.BOLD, 16));
        buttons[3].addActionListener(e -> inputQueue.add("west"));
        
        // Center button (can be used for wait/rest)
        buttons[4].setText("·");
        buttons[4].setFont(new Font("Dialog", Font.BOLD, 16));
        buttons[4].addActionListener(e -> inputQueue.add("wait"));
        
        // Right button
        buttons[5].setText("→");
        buttons[5].setFont(new Font("Dialog", Font.BOLD, 16));
        buttons[5].addActionListener(e -> inputQueue.add("east"));
        
        // Down button
        buttons[7].setText("↓");
        buttons[7].setFont(new Font("Dialog", Font.BOLD, 16));
        buttons[7].addActionListener(e -> inputQueue.add("south"));
        
        // Common command buttons
        JPanel commonButtons = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        commonButtons.setBackground(new Color(60, 60, 80));
        
        // Create buttons for common commands
        String[] cmdNames = {"Look", "Help", "Inventory", "Status"};
        for (String cmd : cmdNames) {
            JButton button = new JButton(cmd);
            button.setBackground(new Color(40, 60, 100));
            button.setForeground(new Color(200, 200, 255));
            button.setFocusable(false);
            button.setFont(new Font("Dialog", Font.BOLD, 12));
            button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(80, 100, 150), 1),
                BorderFactory.createEmptyBorder(2, 5, 2, 5)
            ));
            
            String cmdLower = cmd.toLowerCase();
            button.addActionListener(e -> inputQueue.add(cmdLower));
            commonButtons.add(button);
        }
        
        // Add components to panel
        buttonPanel.add(dirButtons, BorderLayout.CENTER);
        buttonPanel.add(commonButtons, BorderLayout.SOUTH);
        
        inputPanel.add(cmdLabel, BorderLayout.WEST);
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(buttonPanel, BorderLayout.EAST);
        frame.add(inputPanel, BorderLayout.SOUTH);
        
        // Show the frame
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
    
    /**
     * Updates the UI components.
     */
    private void updateUI() {
        // Check if player position has changed
        Position newPos = player.getPosition();
        if (!newPos.equals(currentPlayerPos) && !isAnimating) {
            // Start animation
            previousPlayerPos = new Position(currentPlayerPos);
            currentPlayerPos = new Position(newPos);
            animationProgress = 0.0;
            isAnimating = true;
        }
        
        // Update status label with health bar visualization
        String healthBar = createHealthBar(player.getHealth(), player.getMaxHealth());
        statusLabel.setText(player.getName() + " | " + healthBar + " " + 
                           player.getHealth() + "/" + player.getMaxHealth() + 
                           " | Level: " + player.getLevel() + " | Loc: " + player.getPosition());
        
        // Repaint map and minimap
        mapPanel.repaint();
        if (minimapPanel != null) {
            minimapPanel.repaint();
        }
    }
    
    /**
     * Creates a visual health bar using Unicode block characters
     * 
     * @param health Current health
     * @param maxHealth Maximum health
     * @return String representation of health bar
     */
    private String createHealthBar(int health, int maxHealth) {
        final int barLength = 10; // Number of segments in health bar
        int filledSegments = Math.round((float)health / maxHealth * barLength);
        
        // Ensure at least one segment if health > 0
        if (health > 0 && filledSegments == 0) {
            filledSegments = 1;
        }
        
        StringBuilder bar = new StringBuilder("HP: [");
        
        // Add filled segments
        for (int i = 0; i < filledSegments; i++) {
            bar.append("█");
        }
        
        // Add empty segments
        for (int i = filledSegments; i < barLength; i++) {
            bar.append("░");
        }
        
        bar.append("]");
        return bar.toString();
    }
    
    /**
     * Draws the map of the labyrinth with enhanced graphics.
     * 
     * @param g The graphics context
     */
    private void drawMap(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        
        // Get player positions for animation
        int playerX, playerY;
        if (isAnimating) {
            // Calculate interpolated position between previous and current
            playerX = (int) (previousPlayerPos.getX() * (1.0 - animationProgress) + 
                             currentPlayerPos.getX() * animationProgress);
            playerY = (int) (previousPlayerPos.getY() * (1.0 - animationProgress) + 
                             currentPlayerPos.getY() * animationProgress);
        } else {
            playerX = currentPlayerPos.getX();
            playerY = currentPlayerPos.getY();
        }
        
        int cellSize = 30; // Larger cells for better visuals
        int mapWidth = mapPanel.getWidth() / cellSize;
        int mapHeight = mapPanel.getHeight() / cellSize;
        
        // Calculate the range to display
        int startX = Math.max(0, playerX - mapWidth / 2);
        int startY = Math.max(0, playerY - mapHeight / 2);
        int endX = Math.min(labyrinth.getWidth() - 1, startX + mapWidth - 1);
        int endY = Math.min(labyrinth.getHeight() - 1, startY + mapHeight - 1);
        
        // Adjust start if needed to fill the view
        if (endX - startX + 1 < mapWidth) {
            startX = Math.max(0, endX - mapWidth + 1);
        }
        if (endY - startY + 1 < mapHeight) {
            startY = Math.max(0, endY - mapHeight + 1);
        }
        
        // Draw a nice background grid pattern
        g2d.setColor(new Color(15, 15, 25));
        g2d.fillRect(0, 0, mapPanel.getWidth(), mapPanel.getHeight());
        
        // Draw subtle grid lines
        g2d.setColor(new Color(40, 40, 60));
        for (int i = 0; i <= mapWidth; i++) {
            g2d.drawLine(i * cellSize, 0, i * cellSize, mapHeight * cellSize);
        }
        for (int i = 0; i <= mapHeight; i++) {
            g2d.drawLine(0, i * cellSize, mapWidth * cellSize, i * cellSize);
        }
        
        // Draw the cells with enhanced graphics
        for (int y = startY; y <= endY; y++) {
            for (int x = startX; x <= endX; x++) {
                int drawX = (x - startX) * cellSize;
                int drawY = (y - startY) * cellSize;
                
                Room room = labyrinth.getRoomAt(new Position(x, y));
                
                if (room == null) {
                    // Wall - with texture effect
                    drawWall(g2d, drawX, drawY, cellSize);
                } else {
                    if (!room.isVisited()) {
                        // Unexplored - fog effect
                        drawUnexplored(g2d, drawX, drawY, cellSize);
                    } else {
                        // Explored - draw based on room type with enhanced graphics
                        drawRoom(g2d, drawX, drawY, cellSize, room.getType());
                    }
                }
            }
        }
        
        // Calculate smooth player drawing position
        double drawPlayerX, drawPlayerY;
        
        if (isAnimating) {
            // Calculate interpolated drawing position
            drawPlayerX = (previousPlayerPos.getX() - startX) * cellSize * (1.0 - animationProgress) +
                          (currentPlayerPos.getX() - startX) * cellSize * animationProgress;
            drawPlayerY = (previousPlayerPos.getY() - startY) * cellSize * (1.0 - animationProgress) +
                          (currentPlayerPos.getY() - startY) * cellSize * animationProgress;
        } else {
            drawPlayerX = (currentPlayerPos.getX() - startX) * cellSize;
            drawPlayerY = (currentPlayerPos.getY() - startY) * cellSize;
        }
        
        // Draw player with animation
        drawPlayer(g2d, (int)drawPlayerX, (int)drawPlayerY, cellSize);
    }
    
    /**
     * Draws a wall cell with texture effect
     */
    private void drawWall(Graphics2D g, int x, int y, int size) {
        // Dark stone wall
        GradientPaint gradient = new GradientPaint(
            x, y, new Color(60, 60, 70),
            x + size, y + size, new Color(40, 40, 50)
        );
        g.setPaint(gradient);
        g.fillRect(x, y, size, size);
        
        // Add stone texture effect
        g.setColor(new Color(30, 30, 40, 100));
        for (int i = 0; i < 5; i++) {
            int rx = x + (int)(Math.random() * size);
            int ry = y + (int)(Math.random() * size);
            int rs = 3 + (int)(Math.random() * 5);
            g.fillOval(rx, ry, rs, rs);
        }
        
        // Add border
        g.setColor(new Color(20, 20, 30));
        g.drawRect(x, y, size-1, size-1);
    }
    
    /**
     * Draws an unexplored cell with fog effect
     */
    private void drawUnexplored(Graphics2D g, int x, int y, int size) {
        // Misty gray for unexplored
        g.setColor(new Color(120, 120, 140));
        g.fillRect(x, y, size, size);
        
        // Add fog effect
        g.setColor(new Color(140, 140, 160, 150));
        for (int i = 0; i < 8; i++) {
            int rx = x + (int)(Math.random() * size);
            int ry = y + (int)(Math.random() * size);
            int rs = 5 + (int)(Math.random() * 8);
            g.fillOval(rx, ry, rs, rs);
        }
        
        // Question mark for unexplored
        g.setFont(new Font("Dialog", Font.BOLD, 16));
        g.setColor(new Color(60, 60, 80));
        g.drawString("?", x + size/2 - 5, y + size/2 + 6);
        
        // Add border
        g.setColor(new Color(100, 100, 120));
        g.drawRect(x, y, size-1, size-1);
    }
    
    /**
     * Draws a room cell based on its type with enhanced 2D game-like graphics
     */
    private void drawRoom(Graphics2D g, int x, int y, int size, RoomType type) {
        // Animation time for effects
        long currentTime = System.currentTimeMillis();
        float animFactor = (float)Math.sin(currentTime / 600.0) * 0.5f + 0.5f;
        
        // Base floor color and decorations based on room type
        Color floorBaseColor;
        Color borderColor;
        Color decorationColor;
        
        // Set colors and decoration details based on room type
        switch (type) {
            case REGULAR:
                // Stone floor with subtle texture
                floorBaseColor = new Color(180, 180, 200);
                borderColor = new Color(150, 150, 170);
                decorationColor = new Color(150, 150, 170);
                break;
            case PUZZLE:
                // Magical blue floor with runes
                floorBaseColor = new Color(100, 150, 255);
                borderColor = new Color(50, 100, 200);
                decorationColor = new Color(220, 220, 255);
                break;
            case TREASURE:
                // Golden floor with glittering effect
                floorBaseColor = new Color(255, 215, 0);
                borderColor = new Color(205, 165, 0);
                decorationColor = new Color(255, 255, 200);
                break;
            case MONSTER:
                // Blood-stained floor
                floorBaseColor = new Color(200, 180, 180);
                borderColor = new Color(180, 40, 40);
                decorationColor = new Color(255, 80, 80);
                break;
            case TRAP:
                // Cracked floor with danger marks
                floorBaseColor = new Color(220, 200, 180);
                borderColor = new Color(200, 100, 0);
                decorationColor = new Color(255, 150, 0);
                break;
            case EXIT:
                // Glowing portal floor
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
        
        // Draw base floor
        g.setColor(floorBaseColor);
        g.fillRect(x, y, size, size);
        
        // Add floor tile pattern
        drawFloorPattern(g, x, y, size, floorBaseColor.darker());
        
        // Draw room-specific decoration based on type
        switch (type) {
            case REGULAR:
                // Simple stone pattern
                drawStoneFloor(g, x, y, size);
                break;
            case PUZZLE:
                // Magic runes that glow
                drawMagicRunes(g, x, y, size, animFactor);
                break;
            case TREASURE:
                // Gold and gems
                drawTreasure(g, x, y, size, animFactor);
                break;
            case MONSTER:
                // Blood spatters and claw marks
                drawBloodMarks(g, x, y, size);
                break;
            case TRAP:
                // Danger markings and cracks
                drawTrapMarkings(g, x, y, size, animFactor);
                break;
            case EXIT:
                // Magic portal
                drawExitPortal(g, x, y, size, animFactor);
                break;
        }
        
        // Add lighting effect - corner highlights
        drawRoomLighting(g, x, y, size);
        
        // Add border with 3D effect
        draw3DBorder(g, x, y, size, borderColor);
    }
    
    /**
     * Draws a basic floor tile pattern
     */
    private void drawFloorPattern(Graphics2D g, int x, int y, int size, Color lineColor) {
        g.setColor(new Color(lineColor.getRed(), lineColor.getGreen(), lineColor.getBlue(), 40));
        
        // Draw tile grid (4x4 tiles per room)
        int tileSize = size / 4;
        for (int i = 0; i <= 4; i++) {
            // Horizontal lines
            g.drawLine(x, y + i * tileSize, x + size, y + i * tileSize);
            // Vertical lines
            g.drawLine(x + i * tileSize, y, x + i * tileSize, y + size);
        }
    }
    
    /**
     * Draws stone floor texture
     */
    private void drawStoneFloor(Graphics2D g, int x, int y, int size) {
        g.setColor(new Color(100, 100, 120, 80));
        
        // Add random stone-like texture
        Random random = new Random(x * 31 + y * 17); // Consistent random pattern
        for (int i = 0; i < 6; i++) {
            int stoneX = x + random.nextInt(size);
            int stoneY = y + random.nextInt(size);
            int stoneSize = 3 + random.nextInt(5);
            g.fillRect(stoneX, stoneY, stoneSize, stoneSize);
        }
    }
    
    /**
     * Draws magical runes for puzzle rooms
     */
    private void drawMagicRunes(Graphics2D g, int x, int y, int size, float animFactor) {
        int runeSize = size / 8;
        
        // Save old composite
        Composite oldComposite = g.getComposite();
        
        // Draw magical circle
        g.setColor(new Color(150, 200, 255));
        g.drawOval(x + size/4, y + size/4, size/2, size/2);
        
        // Draw glowing runes
        g.setFont(new Font("Serif", Font.BOLD, runeSize));
        g.setColor(new Color(220, 220, 255));
        
        // Pulsing glow effect
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f + 0.5f * animFactor));
        
        // Draw various magical symbols
        char[] runes = {'Ψ', '⦿', '⧗', '⧍', '♅', '☤', '⚝'};
        Random random = new Random(x * 17 + y * 31); // Consistent random pattern
        
        for (int i = 0; i < 4; i++) {
            int runeX = x + size/4 + random.nextInt(size/2);
            int runeY = y + size/4 + random.nextInt(size/2);
            char rune = runes[random.nextInt(runes.length)];
            g.drawString(String.valueOf(rune), runeX, runeY);
        }
        
        // Restore original composite
        g.setComposite(oldComposite);
    }
    
    /**
     * Draws treasure decorations
     */
    private void drawTreasure(Graphics2D g, int x, int y, int size, float animFactor) {
        // Draw treasure chest
        int chestWidth = size / 2;
        int chestHeight = size / 3;
        int chestX = x + (size - chestWidth) / 2;
        int chestY = y + (size - chestHeight) / 2;
        
        // Chest body
        g.setColor(new Color(139, 69, 19)); // Brown
        g.fillRect(chestX, chestY, chestWidth, chestHeight);
        
        // Chest lid
        g.setColor(new Color(160, 82, 45)); // Sienna
        g.fillRect(chestX, chestY - chestHeight/4, chestWidth, chestHeight/4);
        
        // Metal bands
        g.setColor(new Color(218, 165, 32)); // Golden rod
        g.fillRect(chestX, chestY + chestHeight/3, chestWidth, chestHeight/6);
        
        // Lock
        g.setColor(new Color(255, 215, 0)); // Gold
        g.fillRect(chestX + chestWidth/2 - 3, chestY, 6, 6);
        
        // Glinting effect on gold
        float glintAlpha = 0.3f + 0.7f * animFactor;
        g.setColor(new Color(255, 255, 180, (int)(glintAlpha * 255)));
        
        // Draw scattered coins/gems
        Random random = new Random(x * 7 + y * 13); // Consistent random pattern
        for (int i = 0; i < 5; i++) {
            int gemX = x + random.nextInt(size);
            int gemY = y + random.nextInt(size);
            int gemSize = 3 + random.nextInt(3);
            
            if (random.nextBoolean()) {
                // Gold coin
                g.setColor(new Color(255, 223, 0, (int)(glintAlpha * 255)));
                g.fillOval(gemX, gemY, gemSize, gemSize);
            } else {
                // Gem
                g.setColor(new Color(random.nextInt(100), 100 + random.nextInt(155), 200 + random.nextInt(55), 
                                     (int)(glintAlpha * 255)));
                g.fillRect(gemX, gemY, gemSize, gemSize);
            }
        }
    }
    
    /**
     * Draws blood marks and claw scratches for monster rooms
     */
    private void drawBloodMarks(Graphics2D g, int x, int y, int size) {
        g.setColor(new Color(180, 0, 0, 120)); // Dark red, semi-transparent
        
        // Random blood spatters
        Random random = new Random(x * 11 + y * 23); // Consistent random pattern
        for (int i = 0; i < 8; i++) {
            int spatterX = x + random.nextInt(size);
            int spatterY = y + random.nextInt(size);
            int spatterSize = 2 + random.nextInt(6);
            
            g.fillOval(spatterX, spatterY, spatterSize, spatterSize);
            
            // Sometimes add drip
            if (random.nextBoolean()) {
                int dripLength = 3 + random.nextInt(8);
                g.fillRect(spatterX + spatterSize/2, spatterY + spatterSize, 1, dripLength);
            }
        }
        
        // Add claw marks
        g.setStroke(new BasicStroke(1.5f));
        for (int i = 0; i < 2; i++) {
            int clawX = x + random.nextInt(size - size/3);
            int clawY = y + random.nextInt(size - size/3);
            
            // Draw three parallel lines (claw marks)
            for (int j = 0; j < 3; j++) {
                int startX = clawX + j*4;
                int startY = clawY;
                int endX = startX + random.nextInt(10) - 5;
                int endY = startY + 10 + random.nextInt(10);
                
                g.drawLine(startX, startY, endX, endY);
            }
        }
        
        // Reset stroke
        g.setStroke(new BasicStroke(1.0f));
    }
    
    /**
     * Draws trap markings and danger signs
     */
    private void drawTrapMarkings(Graphics2D g, int x, int y, int size, float animFactor) {
        // Save stroke
        Stroke oldStroke = g.getStroke();
        
        // Draw cracks in the floor
        g.setColor(new Color(100, 80, 60));
        g.setStroke(new BasicStroke(1.5f));
        
        Random random = new Random(x * 41 + y * 47); // Consistent random pattern
        
        // Draw several cracks
        for (int i = 0; i < 3; i++) {
            int startX = x + random.nextInt(size);
            int startY = y + random.nextInt(size);
            
            // Create a jagged line (crack)
            int lastX = startX;
            int lastY = startY;
            
            for (int j = 0; j < 3; j++) {
                int nextX = lastX + random.nextInt(size/4) - size/8;
                int nextY = lastY + random.nextInt(size/4) - size/8;
                
                // Keep within bounds
                nextX = Math.max(x, Math.min(x + size, nextX));
                nextY = Math.max(y, Math.min(y + size, nextY));
                
                g.drawLine(lastX, lastY, nextX, nextY);
                
                lastX = nextX;
                lastY = nextY;
            }
        }
        
        // Draw danger symbol (warning triangle)
        int triangleSize = size / 3;
        int triangleX = x + (size - triangleSize) / 2;
        int triangleY = y + (size - triangleSize) / 2;
        
        // Create triangle points
        int[] xPoints = {
            triangleX + triangleSize/2,
            triangleX + triangleSize,
            triangleX
        };
        
        int[] yPoints = {
            triangleY,
            triangleY + triangleSize,
            triangleY + triangleSize
        };
        
        // Pulsing warning symbol
        float pulseAlpha = 0.4f + 0.6f * animFactor;
        g.setColor(new Color(255, 150, 0, (int)(pulseAlpha * 255)));
        g.fillPolygon(xPoints, yPoints, 3);
        
        g.setColor(new Color(0, 0, 0));
        g.setStroke(new BasicStroke(2.0f));
        g.drawPolygon(xPoints, yPoints, 3);
        
        // Exclamation mark
        g.setColor(new Color(0, 0, 0));
        g.setStroke(new BasicStroke(2.0f));
        int centerX = triangleX + triangleSize/2;
        int centerY = triangleY + triangleSize/2;
        
        g.drawLine(centerX, centerY - triangleSize/4, centerX, centerY + triangleSize/8);
        g.fillOval(centerX - 1, centerY + triangleSize/4, 3, 3);
        
        // Restore stroke
        g.setStroke(oldStroke);
    }
    
    /**
     * Draws magical exit portal
     */
    private void drawExitPortal(Graphics2D g, int x, int y, int size, float animFactor) {
        // Save composite
        Composite oldComposite = g.getComposite();
        
        int portalSize = (int)(size * (0.6f + 0.1f * animFactor));
        int portalX = x + (size - portalSize) / 2;
        int portalY = y + (size - portalSize) / 2;
        
        // Portal outer ring
        g.setColor(new Color(100, 255, 100));
        g.fillOval(portalX, portalY, portalSize, portalSize);
        
        // Portal inner
        int innerSize = (int)(portalSize * 0.8f);
        int innerX = x + (size - innerSize) / 2;
        int innerY = y + (size - innerSize) / 2;
        
        // Create radial gradient for portal
        RadialGradientPaint gradient = new RadialGradientPaint(
            x + size/2, y + size/2, innerSize/2,
            new float[] {0.0f, 0.5f, 1.0f},
            new Color[] {
                new Color(255, 255, 255),
                new Color(150, 255, 150),
                new Color(0, 180, 0)
            }
        );
        g.setPaint(gradient);
        g.fillOval(innerX, innerY, innerSize, innerSize);
        
        // Add swirl effect
        g.setColor(new Color(255, 255, 255, 150));
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
        
        // Rotating swirl
        double angle = System.currentTimeMillis() / 1000.0;
        for (int i = 0; i < 4; i++) {
            double currentAngle = angle + i * Math.PI / 2;
            int swirlX = (int)(x + size/2 + Math.cos(currentAngle) * innerSize/4);
            int swirlY = (int)(y + size/2 + Math.sin(currentAngle) * innerSize/4);
            
            int swirlSize = innerSize / 8;
            g.fillOval(swirlX - swirlSize/2, swirlY - swirlSize/2, swirlSize, swirlSize);
        }
        
        // Add directional arrows pointing inward (exit)
        g.setColor(new Color(255, 255, 255, 100));
        g.setStroke(new BasicStroke(2.0f));
        
        int arrowLength = size / 8;
        // Top arrow
        drawArrow(g, x + size/2, y + size/5, x + size/2, y + size/2, 6);
        // Right arrow
        drawArrow(g, x + size*4/5, y + size/2, x + size/2, y + size/2, 6);
        // Bottom arrow
        drawArrow(g, x + size/2, y + size*4/5, x + size/2, y + size/2, 6);
        // Left arrow
        drawArrow(g, x + size/5, y + size/2, x + size/2, y + size/2, 6);
        
        // Restore composite
        g.setComposite(oldComposite);
    }
    
    /**
     * Draws an arrow from (x1,y1) to (x2,y2) with arrowhead size
     */
    private void drawArrow(Graphics2D g, int x1, int y1, int x2, int y2, int arrowSize) {
        // Draw main line
        g.drawLine(x1, y1, x2, y2);
        
        // Calculate arrow angle
        double angle = Math.atan2(y2 - y1, x2 - x1);
        
        // Create arrowhead
        int[] xPoints = new int[3];
        int[] yPoints = new int[3];
        
        // Tip point
        xPoints[0] = x2;
        yPoints[0] = y2;
        
        // Left point
        xPoints[1] = (int)(x2 - arrowSize * Math.cos(angle - Math.PI/6));
        yPoints[1] = (int)(y2 - arrowSize * Math.sin(angle - Math.PI/6));
        
        // Right point
        xPoints[2] = (int)(x2 - arrowSize * Math.cos(angle + Math.PI/6));
        yPoints[2] = (int)(y2 - arrowSize * Math.sin(angle + Math.PI/6));
        
        g.fillPolygon(xPoints, yPoints, 3);
    }
    
    /**
     * Adds 3D border effect to a room
     */
    private void draw3DBorder(Graphics2D g, int x, int y, int size, Color borderColor) {
        // Top and left - lighter
        g.setColor(borderColor.brighter());
        g.drawLine(x, y, x + size - 1, y); // Top
        g.drawLine(x, y, x, y + size - 1); // Left
        
        // Bottom and right - darker
        g.setColor(borderColor.darker().darker());
        g.drawLine(x, y + size - 1, x + size - 1, y + size - 1); // Bottom
        g.drawLine(x + size - 1, y, x + size - 1, y + size - 1); // Right
    }
    
    /**
     * Adds ambient lighting effect to a room
     */
    private void drawRoomLighting(Graphics2D g, int x, int y, int size) {
        // Ambient corner lighting
        int gradientSize = size / 3;
        
        // Top-left corner highlight
        GradientPaint cornerHighlight = new GradientPaint(
            x, y, new Color(255, 255, 255, 70),
            x + gradientSize, y + gradientSize, new Color(255, 255, 255, 0)
        );
        g.setPaint(cornerHighlight);
        g.fillRect(x, y, gradientSize, gradientSize);
        
        // Bottom-right corner shadow
        GradientPaint cornerShadow = new GradientPaint(
            x + size - gradientSize, y + size - gradientSize, new Color(0, 0, 0, 0),
            x + size, y + size, new Color(0, 0, 0, 70)
        );
        g.setPaint(cornerShadow);
        g.fillRect(x + size - gradientSize, y + size - gradientSize, gradientSize, gradientSize);
    }
    
    /**
     * Draws the player character with enhanced graphics and animation
     */
    private void drawPlayer(Graphics2D g, int x, int y, int size) {
        // Save the current graphics state
        Stroke oldStroke = g.getStroke();
        Paint oldPaint = g.getPaint();
        Composite oldComposite = g.getComposite();
        
        // Calculate animation factors
        double breathFactor = Math.sin(System.currentTimeMillis() / 500.0) * 0.05 + 0.95;
        double bobFactor = Math.sin(System.currentTimeMillis() / 300.0) * 0.1;
        
        // Movement animation factors
        double stepFactor = isAnimating ? Math.sin(animationProgress * Math.PI * 2) * 0.1 : 0;
        double moveFactor = isAnimating ? Math.sin(animationProgress * Math.PI) * 0.1 + 0.9 : 1.0;
        
        // Character size setup
        int characterSize = (int)(size * 0.7 * moveFactor);
        
        // Center point, with slight bobbing animation
        int centerX = x + size / 2;
        int centerY = y + size / 2 + (int)(bobFactor * size/10);
        
        // Determine facing direction based on movement
        int facingDirection = 0; // Default: North
        if (isAnimating) {
            int dx = currentPlayerPos.getX() - previousPlayerPos.getX();
            int dy = currentPlayerPos.getY() - previousPlayerPos.getY();
            
            if (dx > 0) facingDirection = 1; // East
            else if (dx < 0) facingDirection = 3; // West
            else if (dy > 0) facingDirection = 2; // South
            else facingDirection = 0; // North
        }
        
        // Draw shadow beneath character
        g.setColor(new Color(0, 0, 0, 70));
        g.fillOval(centerX - characterSize/2 + 2, 
                  centerY + characterSize/4, 
                  characterSize, 
                  characterSize/4);
        
        // Character base for different directions
        switch (facingDirection) {
            case 0: // North - hero facing away
                drawHeroFromBehind(g, centerX, centerY, characterSize, stepFactor);
                break;
            case 1: // East - hero facing right
                drawHeroSideView(g, centerX, centerY, characterSize, stepFactor, true);
                break;
            case 2: // South - hero facing forward
                drawHeroFrontView(g, centerX, centerY, characterSize, stepFactor);
                break;
            case 3: // West - hero facing left
                drawHeroSideView(g, centerX, centerY, characterSize, stepFactor, false);
                break;
        }
        
        // Draw magical aura/effects around the hero
        drawHeroAura(g, centerX, centerY, characterSize, breathFactor);
        
        // Restore the graphics state
        g.setPaint(oldPaint);
        g.setStroke(oldStroke);
        g.setComposite(oldComposite);
    }
    
    /**
     * Draws the hero from behind (facing north)
     */
    private void drawHeroFromBehind(Graphics2D g, int centerX, int centerY, int size, double stepFactor) {
        // Base body measurements
        int headSize = size / 3;
        int bodyWidth = (int)(size * 0.6);
        int bodyHeight = (int)(size * 0.5);
        int legWidth = size / 6;
        int legHeight = size / 3;
        int armWidth = size / 7;
        int armLength = (int)(size * 0.3);
        
        // Body positioning
        int headY = centerY - size/2 + headSize/2;
        int bodyY = headY + headSize - 2;
        int legY = bodyY + bodyHeight - 2;
        
        // Draw cape/cloak
        g.setColor(new Color(60, 0, 120)); // Deep purple
        int[] capeX = {
            centerX - bodyWidth/2 - 4,
            centerX + bodyWidth/2 + 4,
            centerX + bodyWidth/2 + 2,
            centerX - bodyWidth/2 - 2
        };
        int[] capeY = {
            headY - 2,
            headY - 2,
            bodyY + bodyHeight + 5,
            bodyY + bodyHeight + 5
        };
        g.fillPolygon(capeX, capeY, 4);
        
        // Draw legs with walking animation
        g.setColor(new Color(60, 60, 100)); // Dark blue pants
        // Left leg
        g.fillRect(centerX - bodyWidth/3 - legWidth/2 - (int)(stepFactor * 2), 
                  legY, 
                  legWidth, 
                  legHeight);
        // Right leg
        g.fillRect(centerX + bodyWidth/3 - legWidth/2 + (int)(stepFactor * 2), 
                  legY, 
                  legWidth, 
                  legHeight);
        
        // Draw body
        g.setColor(new Color(0, 80, 160)); // Blue tunic
        g.fillRect(centerX - bodyWidth/2, 
                  bodyY, 
                  bodyWidth, 
                  bodyHeight);
        
        // Draw arms
        g.setColor(new Color(0, 80, 160)); // Blue sleeves matching tunic
        // Left arm
        g.fillRect(centerX - bodyWidth/2 - armWidth/2,
                  bodyY + bodyHeight/6,
                  armWidth,
                  armLength);
        // Right arm
        g.fillRect(centerX + bodyWidth/2 - armWidth/2,
                  bodyY + bodyHeight/6,
                  armWidth,
                  armLength);
        
        // Draw head (back view)
        g.setColor(new Color(225, 180, 120)); // Skin tone
        g.fillOval(centerX - headSize/2,
                  headY - headSize/2,
                  headSize,
                  headSize);
        
        // Draw hair
        g.setColor(new Color(80, 50, 20)); // Brown hair
        g.fillArc(centerX - headSize/2,
                 headY - headSize/2,
                 headSize,
                 headSize,
                 0, 180);
        
        // Draw sword hilt above shoulder
        g.setColor(new Color(180, 150, 80)); // Gold hilt
        g.fillRect(centerX + headSize/2 - 2,
                  headY - 2,
                  6,
                  8);
    }
    
    /**
     * Draws the hero from the front (facing south)
     */
    private void drawHeroFrontView(Graphics2D g, int centerX, int centerY, int size, double stepFactor) {
        // Base body measurements
        int headSize = size / 3;
        int bodyWidth = (int)(size * 0.6);
        int bodyHeight = (int)(size * 0.5);
        int legWidth = size / 6;
        int legHeight = size / 3;
        int armWidth = size / 7;
        int armLength = (int)(size * 0.3);
        
        // Body positioning
        int headY = centerY - size/2 + headSize/2;
        int bodyY = headY + headSize - 2;
        int legY = bodyY + bodyHeight - 2;
        
        // Draw legs with walking animation
        g.setColor(new Color(60, 60, 100)); // Dark blue pants
        // Left leg
        g.fillRect(centerX - bodyWidth/3 - legWidth/2 - (int)(stepFactor * 2), 
                  legY, 
                  legWidth, 
                  legHeight);
        // Right leg
        g.fillRect(centerX + bodyWidth/3 - legWidth/2 + (int)(stepFactor * 2), 
                  legY, 
                  legWidth, 
                  legHeight);
        
        // Draw body/tunic
        g.setColor(new Color(0, 80, 160)); // Blue tunic
        g.fillRect(centerX - bodyWidth/2, 
                  bodyY, 
                  bodyWidth, 
                  bodyHeight);
        
        // Draw belt
        g.setColor(new Color(120, 80, 40)); // Brown belt
        g.fillRect(centerX - bodyWidth/2, 
                  bodyY + bodyHeight*2/3, 
                  bodyWidth, 
                  4);
        
        // Draw buckle
        g.setColor(new Color(200, 180, 60)); // Gold buckle
        g.fillRect(centerX - 4, 
                  bodyY + bodyHeight*2/3 - 1, 
                  8, 
                  6);
                  
        // Draw arms
        g.setColor(new Color(0, 80, 160)); // Blue sleeves
        // Left arm with slight swinging
        g.fillRect(centerX - bodyWidth/2 - (int)(stepFactor * 3),
                  bodyY + 5,
                  armWidth,
                  armLength);
        // Right arm with opposite swing
        g.fillRect(centerX + bodyWidth/2 - armWidth + (int)(stepFactor * 3),
                  bodyY + 5,
                  armWidth,
                  armLength);
        
        // Draw hands
        g.setColor(new Color(225, 180, 120)); // Skin tone
        // Left hand
        g.fillOval(centerX - bodyWidth/2 - (int)(stepFactor * 3) - 1,
                  bodyY + 5 + armLength - 6,
                  armWidth + 2,
                  8);
        // Right hand
        g.fillOval(centerX + bodyWidth/2 - armWidth + (int)(stepFactor * 3) - 1,
                  bodyY + 5 + armLength - 6,
                  armWidth + 2,
                  8);
                  
        // Draw head (front view)
        g.setColor(new Color(225, 180, 120)); // Skin tone
        g.fillOval(centerX - headSize/2,
                  headY - headSize/2,
                  headSize,
                  headSize);
        
        // Draw hair
        g.setColor(new Color(80, 50, 20)); // Brown hair
        g.fillArc(centerX - headSize/2,
                 headY - headSize/2,
                 headSize,
                 headSize + 4,
                 180, 180);
        
        // Draw eyes
        g.setColor(new Color(40, 40, 40)); // Dark eyes
        // Left eye
        g.fillOval(centerX - headSize/5 - 1,
                  headY - 2,
                  4,
                  4);
        // Right eye
        g.fillOval(centerX + headSize/5 - 3,
                  headY - 2,
                  4,
                  4);
        
        // Draw mouth
        g.drawArc(centerX - headSize/6,
                 headY + headSize/6,
                 headSize/3,
                 headSize/6,
                 0, 180);
    }
    
    /**
     * Draws the hero from the side (left or right view)
     */
    private void drawHeroSideView(Graphics2D g, int centerX, int centerY, int size, double stepFactor, boolean facingRight) {
        // Flip coordinates if facing left
        int dir = facingRight ? 1 : -1;
        
        // Base body measurements
        int headSize = size / 3;
        int bodyWidth = (int)(size * 0.4);
        int bodyHeight = (int)(size * 0.5);
        int legWidth = size / 6;
        int legHeight = size / 3;
        int armWidth = size / 7;
        int armLength = (int)(size * 0.3);
        
        // Body positioning
        int headY = centerY - size/2 + headSize/2;
        int bodyY = headY + headSize - 2;
        int legY = bodyY + bodyHeight - 2;
        
        // Draw cape/cloak
        g.setColor(new Color(60, 0, 120)); // Deep purple
        int[] capeX = {
            centerX,
            centerX,
            centerX - dir * bodyWidth/2,
            centerX - dir * (bodyWidth/2 + 5)
        };
        int[] capeY = {
            headY - 2,
            bodyY + bodyHeight + 5,
            bodyY + bodyHeight + 5,
            headY + headSize/2
        };
        g.fillPolygon(capeX, capeY, 4);
        
        // Draw back leg
        g.setColor(new Color(60, 60, 100)); // Dark blue pants
        // Back leg (slightly offset for perspective)
        g.fillRect(centerX - dir * (legWidth/4) - dir * (int)(stepFactor * 4), 
                  legY, 
                  legWidth, 
                  legHeight);
        
        // Draw body/tunic
        g.setColor(new Color(0, 80, 160)); // Blue tunic
        g.fillRect(centerX - dir * bodyWidth/2, 
                  bodyY, 
                  bodyWidth, 
                  bodyHeight);
        
        // Draw belt
        g.setColor(new Color(120, 80, 40)); // Brown belt
        g.fillRect(centerX - dir * bodyWidth/2, 
                  bodyY + bodyHeight*2/3, 
                  bodyWidth, 
                  4);
        
        // Draw front leg
        g.setColor(new Color(60, 60, 100)); // Dark blue pants
        // Front leg (with walking animation)
        g.fillRect(centerX + dir * (legWidth/4) + dir * (int)(stepFactor * 4), 
                  legY, 
                  legWidth, 
                  legHeight);
        
        // Draw arm with walking motion
        g.setColor(new Color(0, 80, 160)); // Blue sleeve
        
        // Calculate arm position with swinging animation
        int armY = bodyY + 5;
        int armSwing = (int)(stepFactor * 6);
        
        // Draw arm
        g.fillRect(centerX + dir * (bodyWidth/2 - armWidth) + dir * armSwing,
                  armY,
                  armWidth,
                  armLength);
        
        // Draw hand
        g.setColor(new Color(225, 180, 120)); // Skin tone
        g.fillOval(centerX + dir * (bodyWidth/2 - armWidth) + dir * armSwing - 1,
                  armY + armLength - 6,
                  armWidth + 2,
                  8);
                  
        // Draw head (side view)
        g.setColor(new Color(225, 180, 120)); // Skin tone
        g.fillOval(centerX - headSize/2,
                  headY - headSize/2,
                  headSize,
                  headSize);
        
        // Draw hair
        g.setColor(new Color(80, 50, 20)); // Brown hair
        g.fillArc(centerX - headSize/2,
                 headY - headSize/2,
                 headSize,
                 headSize,
                 facingRight ? 270 : 90, 180);
        
        // Draw eye (only one visible from side)
        g.setColor(new Color(40, 40, 40)); // Dark eye
        g.fillOval(centerX + dir * headSize/6,
                  headY - 2,
                  3,
                  4);
        
        // Draw nose
        g.drawLine(centerX + dir * headSize/4,
                  headY + 2,
                  centerX + dir * headSize/3,
                  headY + 4);
        
        // Draw partial mouth
        g.drawArc(centerX + dir * (headSize/8),
                 headY + headSize/6,
                 headSize/6,
                 headSize/8,
                 facingRight ? 0 : 180, 
                 180);
    }
    
    /**
     * Draws aura effects around the hero
     */
    private void drawHeroAura(Graphics2D g, int centerX, int centerY, int size, double breathFactor) {
        // Save old composite
        Composite oldComposite = g.getComposite();
        
        // Magical aura around character
        RadialGradientPaint auraGradient = new RadialGradientPaint(
            centerX, centerY, size/2 + 10,
            new float[] {0.0f, 0.7f, 1.0f},
            new Color[] {
                new Color(200, 220, 255, 0),
                new Color(100, 180, 255, 30),
                new Color(50, 100, 255, 50)
            }
        );
        g.setPaint(auraGradient);
        
        // Pulsing ethereal glow
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));
        int auraSize = (int)(size * (1.2 + breathFactor * 0.1));
        g.fillOval(centerX - auraSize/2, 
                  centerY - auraSize/2, 
                  auraSize, 
                  auraSize);
        
        // Random sparkles
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
        Random random = new Random(System.currentTimeMillis() / 500); // Change pattern every half second
        
        // Draw small sparkles around the hero
        for (int i = 0; i < 6; i++) {
            double angle = Math.PI * 2 * random.nextDouble();
            double distance = size/2 * (0.8 + 0.4 * random.nextDouble());
            
            int sparkleX = (int)(centerX + Math.cos(angle) * distance);
            int sparkleY = (int)(centerY + Math.sin(angle) * distance);
            int sparkleSize = 1 + random.nextInt(3);
            
            // Sparkle color
            g.setColor(new Color(
                200 + random.nextInt(55),
                200 + random.nextInt(55),
                255,
                150 + random.nextInt(105)
            ));
            
            g.fillOval(sparkleX, sparkleY, sparkleSize, sparkleSize);
        }
        
        // Restore original composite
        g.setComposite(oldComposite);
    }
    
    @Override
    public void displayMessage(String message) {
        // Check for special message types to apply formatting
        String styledMessage = message;
        
        // Apply special styling for different message types
        if (message.contains("MONSTER") || message.contains("attacked") || message.contains("damage")) {
            // Combat messages in bold red
            styledMessage = "❗ " + message + " ❗";
        } else if (message.contains("TREASURE") || message.contains("gold") || message.contains("found")) {
            // Treasure messages with stars
            styledMessage = "✨ " + message + " ✨";
        } else if (message.contains("PUZZLE") || message.contains("riddle")) {
            // Puzzle messages with question marks
            styledMessage = "❓ " + message + " ❓";
        } else if (message.contains("TRAP") || message.contains("triggered")) {
            // Trap messages with warning symbol
            styledMessage = "⚠️ " + message + " ⚠️";
        } else if (message.contains("EXIT") || message.contains("escape")) {
            // Exit messages with door symbol
            styledMessage = "🚪 " + message + " 🚪";
        } else if (message.contains("healed") || message.contains("level up")) {
            // Healing/level up messages with plus symbol
            styledMessage = "➕ " + message + " ➕";
        }
        
        // Flash effect for important messages
        final String finalMessage = styledMessage + "\n";
        
        if (SwingUtilities.isEventDispatchThread()) {
            addMessageWithEffect(finalMessage);
        } else {
            SwingUtilities.invokeLater(() -> addMessageWithEffect(finalMessage));
        }
    }
    
    /**
     * Adds a message to the output area with visual effects for important messages
     */
    private void addMessageWithEffect(String message) {
        // Check if this is an important message that should trigger visual effect
        boolean isImportant = message.contains("❗") || message.contains("✨") || 
                              message.contains("❓") || message.contains("⚠️") ||
                              message.contains("🚪") || message.contains("➕");
        
        // Add the message to the output area
        outputArea.append(message);
        outputArea.setCaretPosition(outputArea.getDocument().getLength());
        
        // Create a visual flash effect for important messages
        if (isImportant && mapPanel != null) {
            Color originalColor = outputArea.getBackground();
            
            // Determine flash color based on message type
            Color flashColor;
            if (message.contains("❗")) {
                flashColor = new Color(255, 100, 100); // Red for combat
            } else if (message.contains("✨")) {
                flashColor = new Color(255, 215, 0);   // Gold for treasure
            } else if (message.contains("❓")) {
                flashColor = new Color(100, 150, 255); // Blue for puzzles
            } else if (message.contains("⚠️")) {
                flashColor = new Color(255, 150, 0);   // Orange for traps
            } else if (message.contains("🚪")) {
                flashColor = new Color(100, 255, 100); // Green for exit
            } else {
                flashColor = new Color(200, 200, 255); // Default light blue
            }
            
            // Create flash effect timer
            Timer flashTimer = new Timer(100, null);
            final int[] flashCount = {0};
            
            flashTimer.addActionListener(e -> {
                if (flashCount[0] % 2 == 0) {
                    // Flash on
                    outputArea.setBackground(flashColor);
                } else {
                    // Flash off
                    outputArea.setBackground(originalColor);
                }
                
                flashCount[0]++;
                if (flashCount[0] >= 6) { // 3 flashes (on-off cycles)
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
            return inputQueue.take(); // This will block until input is available
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "";
        }
    }
    
    /**
     * Draws the minimap showing the entire explored dungeon
     */
    private void drawMinimap(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        
        // Get player position
        Position playerPos = player.getPosition();
        
        // Calculate the minimap scale for the entire labyrinth
        int panelWidth = minimapPanel.getWidth();
        int panelHeight = minimapPanel.getHeight();
        
        // Draw the background
        g2d.setColor(new Color(10, 10, 20));
        g2d.fillRect(0, 0, panelWidth, panelHeight);
        
        // Calculate scaling to fit the entire labyrinth
        int labWidth = labyrinth.getWidth();
        int labHeight = labyrinth.getHeight();
        
        int cellWidth = Math.max(1, panelWidth / (labWidth + 2));
        int cellHeight = Math.max(1, panelHeight / (labHeight + 2));
        int cellSize = Math.min(cellWidth, cellHeight);
        
        // Center the minimap in the panel
        int offsetX = (panelWidth - (labWidth * cellSize)) / 2;
        int offsetY = (panelHeight - (labHeight * cellSize)) / 2;
        
        // Draw subtle grid
        g2d.setColor(new Color(30, 30, 40));
        for (int x = 0; x <= labWidth; x++) {
            g2d.drawLine(
                offsetX + x * cellSize, 
                offsetY, 
                offsetX + x * cellSize, 
                offsetY + labHeight * cellSize
            );
        }
        
        for (int y = 0; y <= labHeight; y++) {
            g2d.drawLine(
                offsetX, 
                offsetY + y * cellSize, 
                offsetX + labWidth * cellSize, 
                offsetY + y * cellSize
            );
        }
        
        // Draw border around the minimap
        g2d.setColor(new Color(70, 70, 90));
        g2d.drawRect(
            offsetX - 1, 
            offsetY - 1, 
            labWidth * cellSize + 1, 
            labHeight * cellSize + 1
        );
        
        // Draw all the rooms in the labyrinth
        for (int y = 0; y < labHeight; y++) {
            for (int x = 0; x < labWidth; x++) {
                Position pos = new Position(x, y);
                Room room = labyrinth.getRoomAt(pos);
                
                int drawX = offsetX + x * cellSize;
                int drawY = offsetY + y * cellSize;
                
                if (room == null) {
                    // Draw wall (black)
                    g2d.setColor(new Color(20, 20, 30));
                    g2d.fillRect(drawX, drawY, cellSize, cellSize);
                } else if (room.isVisited()) {
                    // Draw explored room with its type color
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
                    
                    // Draw border
                    g2d.setColor(roomColor.darker());
                    g2d.drawRect(drawX, drawY, cellSize - 1, cellSize - 1);
                } else {
                    // Draw unexplored but known room (gray)
                    g2d.setColor(new Color(100, 100, 120));
                    g2d.fillRect(drawX, drawY, cellSize, cellSize);
                }
            }
        }
        
        // Draw the viewport rectangle (visible area in the main map)
        int cellSizeMain = 30; // Same as in drawMap method
        int mapWidth = mapPanel.getWidth() / cellSizeMain;
        int mapHeight = mapPanel.getHeight() / cellSizeMain;
        
        int startX = Math.max(0, playerPos.getX() - mapWidth / 2);
        int startY = Math.max(0, playerPos.getY() - mapHeight / 2);
        int endX = Math.min(labyrinth.getWidth() - 1, startX + mapWidth - 1);
        int endY = Math.min(labyrinth.getHeight() - 1, startY + mapHeight - 1);
        
        // Adjust for partial view
        if (endX - startX + 1 < mapWidth) {
            startX = Math.max(0, endX - mapWidth + 1);
        }
        if (endY - startY + 1 < mapHeight) {
            startY = Math.max(0, endY - mapHeight + 1);
        }
        
        // Draw viewport rectangle
        g2d.setColor(new Color(255, 255, 255, 70));
        g2d.drawRect(
            offsetX + startX * cellSize,
            offsetY + startY * cellSize,
            (endX - startX + 1) * cellSize,
            (endY - startY + 1) * cellSize
        );
        
        // Draw player position on minimap
        int playerDrawX = offsetX + playerPos.getX() * cellSize;
        int playerDrawY = offsetY + playerPos.getY() * cellSize;
        
        // Draw player marker (pulsing circle)
        double pulseSize = 0.8 + Math.sin(System.currentTimeMillis() / 200.0) * 0.2;
        int markerSize = (int)(cellSize * pulseSize);
        int markerX = playerDrawX + (cellSize - markerSize) / 2;
        int markerY = playerDrawY + (cellSize - markerSize) / 2;
        
        // Draw outer glow
        g2d.setColor(new Color(50, 200, 255, 150));
        g2d.fillOval(markerX - 1, markerY - 1, markerSize + 2, markerSize + 2);
        
        // Draw player marker
        g2d.setColor(new Color(0, 100, 255));
        g2d.fillOval(markerX, markerY, markerSize, markerSize);
        
        // Draw highlight
        g2d.setColor(new Color(150, 220, 255));
        g2d.fillOval(markerX + markerSize/4, markerY + markerSize/4, markerSize/4, markerSize/4);
    }
    
    /**
     * Adds a color item to the legend panel
     * 
     * @param panel The legend panel to add to
     * @param color The color to display
     * @param label The text label for the color
     */
    private void addLegendItem(JPanel panel, Color color, String label) {
        JPanel item = new JPanel(new BorderLayout(5, 0));
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
    
    /**
     * Creates a compact legend item for the minimap
     * 
     * @param color The color to display
     * @param label The text label for the color
     * @return A panel containing the legend item
     */
    private JPanel createCompactLegendItem(Color color, String label) {
        JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        item.setBackground(new Color(30, 30, 45));
        
        // Color square
        JPanel colorBox = new JPanel();
        colorBox.setBackground(color);
        colorBox.setPreferredSize(new Dimension(8, 8));
        colorBox.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1));
        
        // Text label
        JLabel textLabel = new JLabel(label);
        textLabel.setForeground(new Color(180, 180, 220));
        textLabel.setFont(new Font("Dialog", Font.PLAIN, 9));
        
        item.add(colorBox);
        item.add(textLabel);
        
        return item;
    }
}
