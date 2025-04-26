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
     * Draws a room cell based on its type
     */
    private void drawRoom(Graphics2D g, int x, int y, int size, RoomType type) {
        Color baseColor;
        Color borderColor;
        String symbol = "";
        
        // Set colors and symbol based on room type
        switch (type) {
            case REGULAR:
                baseColor = new Color(200, 200, 220);
                borderColor = new Color(150, 150, 170);
                break;
            case PUZZLE:
                baseColor = new Color(100, 150, 255);
                borderColor = new Color(50, 100, 200);
                symbol = "P";
                break;
            case TREASURE:
                baseColor = new Color(255, 215, 0);
                borderColor = new Color(205, 165, 0);
                symbol = "T";
                break;
            case MONSTER:
                baseColor = new Color(255, 80, 80);
                borderColor = new Color(180, 40, 40);
                symbol = "M";
                break;
            case TRAP:
                baseColor = new Color(255, 150, 0);
                borderColor = new Color(200, 100, 0);
                symbol = "!";
                break;
            case EXIT:
                baseColor = new Color(100, 255, 100);
                borderColor = new Color(0, 180, 0);
                symbol = "E";
                break;
            default:
                baseColor = Color.GRAY;
                borderColor = Color.DARK_GRAY;
                break;
        }
        
        // Draw room base with gradient
        GradientPaint gradient = new GradientPaint(
            x, y, baseColor,
            x + size, y + size, baseColor.darker()
        );
        g.setPaint(gradient);
        g.fillRect(x, y, size, size);
        
        // Add highlight at top-left
        g.setColor(new Color(255, 255, 255, 50));
        g.fillRect(x, y, size/2, size/2);
        
        // Draw symbol if any
        if (!symbol.isEmpty()) {
            g.setFont(new Font("Dialog", Font.BOLD, 16));
            g.setColor(borderColor.darker());
            g.drawString(symbol, x + size/2 - 5, y + size/2 + 6);
        }
        
        // Add border
        g.setColor(borderColor);
        g.drawRect(x, y, size-1, size-1);
    }
    
    /**
     * Draws the player character with enhanced graphics and animation
     */
    private void drawPlayer(Graphics2D g, int x, int y, int size) {
        // Save the current graphics state
        Stroke oldStroke = g.getStroke();
        Paint oldPaint = g.getPaint();
        
        // Calculate animation factor for breathing effect (0.0 to 1.0)
        double breathFactor = Math.sin(System.currentTimeMillis() / 500.0) * 0.05 + 0.95;
        
        // Movement animation factor
        double moveFactor = isAnimating ? Math.sin(animationProgress * Math.PI) * 0.1 + 0.9 : 1.0;
        
        // Adjusted padding for animation
        int basePadding = size / 6;
        int padding = (int)(basePadding * breathFactor);
        
        // Character body size (affected by animation)
        int bodySize = (int)((size - padding * 2) * moveFactor);
        
        // Center point
        int centerX = x + size / 2;
        int centerY = y + size / 2;
        
        // Draw shadow beneath character
        g.setColor(new Color(0, 0, 0, 70));
        g.fillOval(centerX - bodySize/2 + 2, centerY - bodySize/2 + 4, bodySize, bodySize / 2);
        
        // Determine direction based on movement
        int direction = 0; // Default: North
        if (isAnimating) {
            int dx = currentPlayerPos.getX() - previousPlayerPos.getX();
            int dy = currentPlayerPos.getY() - previousPlayerPos.getY();
            
            if (dx > 0) direction = 1; // East
            else if (dx < 0) direction = 3; // West
            else if (dy > 0) direction = 2; // South
            else direction = 0; // North
        }
        
        // Character body - blue hero with gradient and glow effect
        RadialGradientPaint gradient = new RadialGradientPaint(
            centerX, centerY, bodySize/2,
            new float[] {0.0f, 0.7f, 1.0f},
            new Color[] {
                new Color(100, 180, 255),
                new Color(30, 100, 255),
                new Color(20, 70, 200)
            }
        );
        g.setPaint(gradient);
        
        // Draw glowing aura
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
        g.fillOval(centerX - bodySize/2 - 3, centerY - bodySize/2 - 3, bodySize + 6, bodySize + 6);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        
        // Draw main body
        g.fillOval(centerX - bodySize/2, centerY - bodySize/2, bodySize, bodySize);
        
        // Add highlight reflection
        g.setColor(new Color(255, 255, 255, 120));
        int highlightSize = bodySize / 3;
        g.fillOval(
            centerX - bodySize/2 + 4,
            centerY - bodySize/2 + 4,
            highlightSize,
            highlightSize
        );
        
        // Draw animated pulse ring if moving
        if (isAnimating) {
            g.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, 
                                      new float[]{3, 3}, (float)(System.currentTimeMillis() / 100.0) % 6));
            g.setColor(new Color(100, 200, 255, (int)(100 * (1.0 - animationProgress))));
            g.drawOval(centerX - bodySize/2 - 5, centerY - bodySize/2 - 5, bodySize + 10, bodySize + 10);
        }
        
        // Add metallic border
        g.setStroke(new BasicStroke(2));
        g.setPaint(new GradientPaint(
            centerX - bodySize/2, centerY - bodySize/2, new Color(150, 180, 255),
            centerX + bodySize/2, centerY + bodySize/2, new Color(10, 40, 120)
        ));
        g.drawOval(centerX - bodySize/2, centerY - bodySize/2, bodySize, bodySize);
        
        // Direction indicator (shaped according to movement direction)
        int arrowSize = bodySize / 3;
        g.setColor(new Color(255, 240, 100));
        
        // Draw different direction indicators based on facing direction
        switch(direction) {
            case 0: // North
                // Draw up-pointing chevron
                int[] xNorth = {centerX, centerX - arrowSize/2, centerX + arrowSize/2};
                int[] yNorth = {centerY - bodySize/2 - 2, centerY - bodySize/2 + arrowSize/2, centerY - bodySize/2 + arrowSize/2};
                g.fillPolygon(xNorth, yNorth, 3);
                break;
                
            case 1: // East
                // Draw right-pointing chevron
                int[] xEast = {centerX + bodySize/2 + 2, centerX + bodySize/2 - arrowSize/2, centerX + bodySize/2 - arrowSize/2};
                int[] yEast = {centerY, centerY - arrowSize/2, centerY + arrowSize/2};
                g.fillPolygon(xEast, yEast, 3);
                break;
                
            case 2: // South
                // Draw down-pointing chevron
                int[] xSouth = {centerX, centerX - arrowSize/2, centerX + arrowSize/2};
                int[] ySouth = {centerY + bodySize/2 + 2, centerY + bodySize/2 - arrowSize/2, centerY + bodySize/2 - arrowSize/2};
                g.fillPolygon(xSouth, ySouth, 3);
                break;
                
            case 3: // West
                // Draw left-pointing chevron
                int[] xWest = {centerX - bodySize/2 - 2, centerX - bodySize/2 + arrowSize/2, centerX - bodySize/2 + arrowSize/2};
                int[] yWest = {centerY, centerY - arrowSize/2, centerY + arrowSize/2};
                g.fillPolygon(xWest, yWest, 3);
                break;
        }
        
        // Add small eyes to character (giving it a face)
        int eyeOffset = bodySize / 8;
        int eyeSize = bodySize / 10;
        
        // Adjust eye position based on direction
        int leftEyeX = centerX - eyeOffset;
        int rightEyeX = centerX + eyeOffset;
        int eyeY = centerY - eyeOffset/2;
        
        // Draw eyes
        g.setColor(new Color(220, 240, 255));
        g.fillOval(leftEyeX - eyeSize/2, eyeY - eyeSize/2, eyeSize, eyeSize);
        g.fillOval(rightEyeX - eyeSize/2, eyeY - eyeSize/2, eyeSize, eyeSize);
        
        // Restore original graphics state
        g.setStroke(oldStroke);
        g.setPaint(oldPaint);
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
