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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Swing-based implementation of the game view.
 */
public class SwingView implements GameView {
    private Labyrinth labyrinth;
    private Player player;
    private JFrame frame;
    private JTextArea outputArea;
    private JTextField inputField;
    private JPanel mapPanel;
    private JLabel statusLabel;
    private BlockingQueue<String> inputQueue;
    
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
        
        SwingUtilities.invokeLater(this::createAndShowGUI);
    }
    
    /**
     * Creates and displays the GUI.
     */
    private void createAndShowGUI() {
        // Create the main frame
        frame = new JFrame("Magical Labyrinth: Escape from the Dungeon");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 700);
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
        frame.add(mapContainer, BorderLayout.EAST);
        
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
        // Update status label
        statusLabel.setText(player.getName() + " | HP: " + player.getHealth() + "/" + player.getMaxHealth() + 
                           " | Level: " + player.getLevel() + " | Position: " + player.getPosition());
        
        // Repaint map
        mapPanel.repaint();
    }
    
    /**
     * Draws the map of the labyrinth with enhanced graphics.
     * 
     * @param g The graphics context
     */
    private void drawMap(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        Position playerPos = player.getPosition();
        int playerX = playerPos.getX();
        int playerY = playerPos.getY();
        
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
        
        // Draw player as animated character
        int playerDrawX = (playerX - startX) * cellSize;
        int playerDrawY = (playerY - startY) * cellSize;
        drawPlayer(g2d, playerDrawX, playerDrawY, cellSize);
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
     * Draws the player character
     */
    private void drawPlayer(Graphics2D g, int x, int y, int size) {
        // Draw character with shading
        int padding = size / 6;
        
        // Character body - blue circle with gradient
        GradientPaint gradient = new GradientPaint(
            x + padding, y + padding, new Color(30, 100, 255),
            x + size - padding, y + size - padding, new Color(20, 70, 200)
        );
        g.setPaint(gradient);
        g.fillOval(x + padding, y + padding, size - padding*2, size - padding*2);
        
        // Add highlight
        g.setColor(new Color(255, 255, 255, 120));
        g.fillOval(x + padding + 2, y + padding + 2, (size - padding*2) / 3, (size - padding*2) / 3);
        
        // Add border
        g.setColor(new Color(10, 40, 120));
        g.setStroke(new BasicStroke(2));
        g.drawOval(x + padding, y + padding, size - padding*2, size - padding*2);
        
        // Direction indicator (simple triangle in front of player)
        int centerX = x + size/2;
        int centerY = y + size/2;
        int direction = 0; // 0=N, 1=E, 2=S, 3=W
        
        g.setColor(new Color(255, 255, 100));
        int[] xPoints = new int[3];
        int[] yPoints = new int[3];
        
        if (direction == 0) { // North
            xPoints[0] = centerX; xPoints[1] = centerX - 4; xPoints[2] = centerX + 4;
            yPoints[0] = y + padding - 2; yPoints[1] = y + padding + 6; yPoints[2] = y + padding + 6;
        } else if (direction == 1) { // East
            xPoints[0] = x + size - padding; xPoints[1] = x + size - padding - 6; xPoints[2] = x + size - padding - 6;
            yPoints[0] = centerY; yPoints[1] = centerY - 4; yPoints[2] = centerY + 4;
        } else if (direction == 2) { // South
            xPoints[0] = centerX; xPoints[1] = centerX - 4; xPoints[2] = centerX + 4;
            yPoints[0] = y + size - padding; yPoints[1] = y + size - padding - 6; yPoints[2] = y + size - padding - 6;
        } else { // West
            xPoints[0] = x + padding; xPoints[1] = x + padding + 6; xPoints[2] = x + padding + 6;
            yPoints[0] = centerY; yPoints[1] = centerY - 4; yPoints[2] = centerY + 4;
        }
        
        g.fillPolygon(xPoints, yPoints, 3);
    }
    
    @Override
    public void displayMessage(String message) {
        if (SwingUtilities.isEventDispatchThread()) {
            outputArea.append(message + "\n");
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        } else {
            SwingUtilities.invokeLater(() -> {
                outputArea.append(message + "\n");
                outputArea.setCaretPosition(outputArea.getDocument().getLength());
            });
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
}
