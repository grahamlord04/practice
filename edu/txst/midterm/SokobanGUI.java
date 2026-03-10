package edu.txst.midterm;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;

/**
 * Provides the graphical user interface for the Sokoban game.
 * <p>
 * This class creates the game window, handles menu actions,
 * processes keyboard input, and displays the board on screen.
 * </p>
 */
public class SokobanGUI extends JFrame {
	private Board originalBoard;
	private Board currentBoard;
	private GameEngine engine;
	private GamePanel gamePanel;
	private JMenuItem resetItem;

	/** Stores the number of player moves made in the current level. */
	public static int stepCounter = 0;

	/**
	 * Creates the Sokoban game window and initializes
	 * the menu, game panel, and keyboard controls.
	 */
	public SokobanGUI() {
		setTitle("16-Bit Sokoban");
		setSize(640, 480); // Adjusted for 10x5 grid with scaling
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);

		initMenu();

		gamePanel = new GamePanel();
		add(gamePanel);

		// Handle Keyboard Input
		addKeyListener(new KeyAdapter() {
			/**
			 * Responds to key presses and moves the player
			 * in the corresponding direction.
			 *
			 * @param e the keyboard event triggered by the user
			 */
			@Override
			public void keyPressed(KeyEvent e) {
				if (engine == null)
					return;

				switch (e.getKeyCode()) {
					case KeyEvent.VK_UP -> engine.movePlayer(-1, 0);
					case KeyEvent.VK_DOWN -> engine.movePlayer(1, 0);
					case KeyEvent.VK_LEFT -> engine.movePlayer(0, -1);
					case KeyEvent.VK_RIGHT -> engine.movePlayer(0, 1);
				}
				gamePanel.repaint();

				// Check for victory
				if (engine.isGameOver()) {
					JOptionPane.showMessageDialog(SokobanGUI.this,
							"Congratulations! You solved the puzzle in " + stepCounter + " steps!",
							"Level Complete", JOptionPane.INFORMATION_MESSAGE);

					// Optional: Disable engine to prevent movement after win
					engine = null;
					resetItem.setEnabled(false);
				} else if (engine.isGameStuck()) {
					JOptionPane.showMessageDialog(SokobanGUI.this,
							"Level has no solution because boxes cannot be moved anymore.",
							"Level cannot be solved.", JOptionPane.INFORMATION_MESSAGE);

					// Optional: Disable engine to prevent movement after win
					engine = null;
				}
			}
		});
	}

	/**
	 * Initializes the menu bar and game menu.
	 * <p>
	 * The menu includes options to open a level file
	 * and reset the current game.
	 * </p>
	 */
	private void initMenu() {
		JMenuBar menuBar = new JMenuBar();
		JMenu gameMenu = new JMenu("Game");

		JMenuItem openItem = new JMenuItem("Open");
		resetItem = new JMenuItem("Reset");
		resetItem.setEnabled(false); // Disabled by default

		openItem.addActionListener(e -> openFile());
		resetItem.addActionListener(e -> resetGame());

		gameMenu.add(openItem);
		gameMenu.add(resetItem);
		menuBar.add(gameMenu);
		setJMenuBar(menuBar);
	}

	/**
	 * Opens a file chooser so the user can select a CSV level file.
	 * <p>
	 * After a level is selected, the board is loaded, copied,
	 * and assigned to the game engine.
	 * </p>
	 */
	private void openFile() {
		JFileChooser fileChooser = new JFileChooser(System.getProperty("user.dir"));
		int result = fileChooser.showOpenDialog(this);

		if (result == JFileChooser.APPROVE_OPTION) {
			File selectedFile = fileChooser.getSelectedFile();
			CSVBoardLoader loader = new CSVBoardLoader();

			// Load and Store
			originalBoard = loader.load(selectedFile.getAbsolutePath());
			currentBoard = originalBoard.clone();
			engine = new GameEngine(currentBoard);

			resetItem.setEnabled(true);
			gamePanel.setBoard(currentBoard);
			gamePanel.repaint();
		}
	}

	/**
	 * Resets the current level to its original state.
	 * <p>
	 * This restores the board from the saved original copy
	 * and creates a new game engine for the reset level.
	 * </p>
	 */
	private void resetGame() {
		if (originalBoard != null) {
			currentBoard = originalBoard.clone();
			engine = new GameEngine(currentBoard);
			gamePanel.setBoard(currentBoard);
			gamePanel.repaint();
		}
	}

	/**
	 * Custom panel used to draw the Sokoban board.
	 */
	private class GamePanel extends JPanel {
		private Board board;

		/** Size in pixels of each tile drawn on the board. */
		private final int TILE_SIZE = 64; // Scale up for visibility

		/**
		 * Sets the board to be displayed by the panel.
		 *
		 * @param board the board to render
		 */
		public void setBoard(Board board) {
			this.board = board;
		}

		/**
		 * Paints the game board and all tiles on the panel.
		 *
		 * @param g the graphics object used for drawing
		 */
		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			if (board == null)
				return;

			for (int r = 0; r < 5; r++) {
				for (int c = 0; c < 10; c++) {
					int cell = board.getCell(r, c);
					drawTile(g, cell, c * TILE_SIZE, r * TILE_SIZE);
				}
			}
		}

		/**
		 * Draws a single tile of the board using a placeholder color.
		 *
		 * @param g the graphics object used for drawing
		 * @param type the tile type to draw
		 * @param x the x-coordinate of the tile
		 * @param y the y-coordinate of the tile
		 */
		private void drawTile(Graphics g, int type, int x, int y) {
			// Placeholder colors until you link the sprite loading logic
			switch (type) {
				case 0 -> g.setColor(Color.LIGHT_GRAY); // Floor
				case 1 -> g.setColor(Color.DARK_GRAY); // Wall
				case 2 -> g.setColor(Color.ORANGE); // Box
				case 3 -> g.setColor(Color.magenta); // Goal
				case 4 -> g.setColor(Color.BLUE); // Player
				case 5 -> g.setColor(Color.RED); // Box on Goal
				default -> g.setColor(Color.BLACK);
			}
			g.fillRect(x, y, TILE_SIZE, TILE_SIZE);
			g.setColor(Color.WHITE);
			g.drawRect(x, y, TILE_SIZE, TILE_SIZE); // Grid lines
		}
	}

	/**
	 * Starts the Sokoban application.
	 *
	 * @param args command-line arguments passed to the program
	 */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> new SokobanGUI().setVisible(true));
	}
}