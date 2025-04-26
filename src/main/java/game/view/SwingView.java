package game.view;

import game.model.Labyrinth;
import game.model.Player;
import game.model.Position;
import game.model.rooms.Room;

import javax.swing.*;
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
        frame = new JFrame("Magical Labyrinth");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLayout(new BorderLayout());
        
        // Create the output text area
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setLineWrap(true);
        outputArea.setWrapStyleWord(true);
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(outputArea);
        frame.add(scrollPane, BorderLayout.CENTER);
        
        // Create status panel
        statusLabel = new JLabel("HP: 0/0 | Level: 0");
        frame.add(statusLabel, BorderLayout.NORTH);
        
        // Create map panel
        mapPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawMap(g);
            }
        };
        mapPanel.setPreferredSize(new Dimension(200, 200));
        frame.add(mapPanel, BorderLayout.EAST);
        
        // Create input panel
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputField = new JTextField();
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
        
        inputPanel.add(new JLabel("Command: "), BorderLayout.WEST);
        inputPanel.add(inputField, BorderLayout.CENTER);
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
     * Draws the map of the labyrinth.
     * 
     * @param g The graphics context
     */
    private void drawMap(Graphics g) {
        Position playerPos = player.getPosition();
        int playerX = playerPos.getX();
        int playerY = playerPos.getY();
        
        int cellSize = 20;
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
        
        // Draw the cells
        for (int y = startY; y <= endY; y++) {
            for (int x = startX; x <= endX; x++) {
                int drawX = (x - startX) * cellSize;
                int drawY = (y - startY) * cellSize;
                
                Room room = labyrinth.getRoomAt(new Position(x, y));
                
                if (room == null) {
                    // Wall
                    g.setColor(Color.DARK_GRAY);
                    g.fillRect(drawX, drawY, cellSize, cellSize);
                } else {
                    if (!room.isVisited()) {
                        // Unexplored
                        g.setColor(Color.LIGHT_GRAY);
                        g.fillRect(drawX, drawY, cellSize, cellSize);
                    } else {
                        // Explored - color based on room type
                        switch (room.getType()) {
                            case REGULAR:
                                g.setColor(Color.WHITE);
                                break;
                            case PUZZLE:
                                g.setColor(Color.BLUE);
                                break;
                            case TREASURE:
                                g.setColor(Color.YELLOW);
                                break;
                            case MONSTER:
                                g.setColor(Color.RED);
                                break;
                            case TRAP:
                                g.setColor(Color.ORANGE);
                                break;
                            case EXIT:
                                g.setColor(Color.GREEN);
                                break;
                        }
                        g.fillRect(drawX, drawY, cellSize, cellSize);
                    }
                }
                
                // Draw cell border
                g.setColor(Color.BLACK);
                g.drawRect(drawX, drawY, cellSize, cellSize);
            }
        }
        
        // Draw player
        int playerDrawX = (playerX - startX) * cellSize;
        int playerDrawY = (playerY - startY) * cellSize;
        g.setColor(Color.BLACK);
        g.fillOval(playerDrawX + 5, playerDrawY + 5, cellSize - 10, cellSize - 10);
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
}
