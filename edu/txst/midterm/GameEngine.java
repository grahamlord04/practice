package edu.txst.midterm;

/**
 * Manages the core logic for the Sokoban game.
 * <p>
 * This class keeps track of the game board, the player's position,
 * box movement, win detection, and detection of stuck game states.
 * </p>
 */
public class GameEngine {
	private Board board;
	private int playerRow;
	private int playerCol;
	private int reachedGoals = 0;

	/** Represents an empty floor tile. */
	private static final int FLOOR = 0;

	/** Represents a wall tile that blocks movement. */
	private static final int WALL = 1;

	/** Represents a movable box tile. */
	private static final int BOX = 2;

	/** Represents a goal tile. */
	private static final int GOAL = 3;

	/** Represents the player tile. */
	private static final int PLAYER = 4;

	/** Represents a box that has been moved onto a goal tile. */
	private static final int BOX_ON_GOAL = 5;

	/**
	 * Creates a new game engine using the given board.
	 * Initializes the player's starting position.
	 *
	 * @param board the game board for the current level
	 */
	public GameEngine(Board board) {
		this.board = board;
		findPlayer();
	}

	/**
	 * Determines whether the game has been won.
	 *
	 * @return true if the required number of goals has been reached;
	 *         false otherwise
	 */
	public boolean isGameOver() {
		if (reachedGoals == 2) {
			return true;
		}
		return false;
	}

	/**
	 * Determines whether the game is stuck.
	 * <p>
	 * A game is considered stuck when there is at least one normal box
	 * remaining, but none of the boxes can be pushed in any direction.
	 * </p>
	 *
	 * @return true if no remaining box can be moved; false otherwise
	 */
	public boolean isGameStuck() {
		boolean foundBox = false;

		for (int r = 0; r < 5; r++) {
			for (int c = 0; c < 10; c++) {
				if (board.getCell(r, c) == BOX) {
					foundBox = true;

					// If this box can be pushed in any direction,
					// then the game is NOT stuck
					if (canPushBox(r, c, -1, 0) || // up
						canPushBox(r, c, 1, 0) ||  // down
						canPushBox(r, c, 0, -1) || // left
						canPushBox(r, c, 0, 1)) {  // right
						return false;
					}
				}
			}
		}

		// If there are no normal boxes left, it's not a stuck game;
		// that should be handled by isGameOver()
		if (!foundBox) {
			return false;
		}

		return true;
	}

	/**
	 * Checks whether a box can be pushed in a given direction.
	 *
	 * @param boxRow the row of the box
	 * @param boxCol the column of the box
	 * @param dRow the row direction to push the box
	 * @param dCol the column direction to push the box
	 * @return true if the box can be pushed in the given direction;
	 *         false otherwise
	 */
	private boolean canPushBox(int boxRow, int boxCol, int dRow, int dCol) {
		int playerSideRow = boxRow - dRow;
		int playerSideCol = boxCol - dCol;

		int destinationRow = boxRow + dRow;
		int destinationCol = boxCol + dCol;

		return isPlayerSpace(playerSideRow, playerSideCol)
				&& isBoxDestination(destinationRow, destinationCol);
	}

	/**
	 * Checks whether the given cell is a valid place for the player to stand.
	 *
	 * @param row the row to check
	 * @param col the column to check
	 * @return true if the cell is floor or player; false otherwise
	 */
	private boolean isPlayerSpace(int row, int col) {
		int cell = board.getCell(row, col);
		return cell == FLOOR || cell == PLAYER;
	}

	/**
	 * Checks whether the given cell is a valid destination for a box.
	 *
	 * @param row the row to check
	 * @param col the column to check
	 * @return true if the cell is floor or goal; false otherwise
	 */
	private boolean isBoxDestination(int row, int col) {
		int cell = board.getCell(row, col);
		return cell == FLOOR || cell == GOAL;
	}

	/**
	 * Finds the player's starting position on the board
	 * and stores its row and column.
	 */
	private void findPlayer() {
		for (int r = 0; r < 5; r++) {
			for (int c = 0; c < 10; c++) {
				if (board.getCell(r, c) == PLAYER) {
					playerRow = r;
					playerCol = c;
					return;
				}
			}
		}
	}

	/**
	 * Attempts to move the player in the specified direction.
	 * <p>
	 * If the target space contains a movable box, this method will also
	 * attempt to push the box. If the movement is blocked by a wall,
	 * goal, box on goal, or invalid destination, the player does not move.
	 * </p>
	 *
	 * @param dRow change in row (-1, 0, or 1)
	 * @param dCol change in column (-1, 0, or 1)
	 */
	public void movePlayer(int dRow, int dCol) {
		int targetRow = playerRow + dRow;
		int targetCol = playerCol + dCol;
		int targetCell = board.getCell(targetRow, targetCol);

		// 1. Check for Walls or Out of Bounds or Goal
		if (targetCell == WALL || targetCell == -1 || targetCell == GOAL || targetCell == BOX_ON_GOAL) {
			return; // Movement blocked
		}

		// 2. Check for Boxes (Normal Box or Box on Goal)
		if (targetCell == BOX || targetCell == BOX_ON_GOAL) {
			int nextRow = targetRow + dRow;
			int nextCol = targetCol + dCol;
			int nextCell = board.getCell(nextRow, nextCol);

			// Can only push if the space behind the box is Floor or Goal
			if (nextCell == FLOOR || nextCell == GOAL) {

				// Move the box
				int newBoxType = (nextCell == GOAL) ? BOX_ON_GOAL : BOX;
				board.setCell(nextRow, nextCol, newBoxType);
				if (nextCell == GOAL)
					reachedGoals++;
				// Clear the box's old position (it becomes a floor or remains a goal)
				int oldBoxPosType = (targetCell == BOX_ON_GOAL) ? GOAL : FLOOR;
				board.setCell(targetRow, targetCol, oldBoxPosType);

			} else {
				return; // Box is blocked
			}
		}

		// 3. Move the Player
		// Current position becomes Floor (or Goal if player was standing on one)
		// Note: For simplicity, this engine assumes player replaces the cell.
		// If you want "Player on Goal", you'd add a 6th constant.
		board.setCell(playerRow, playerCol, FLOOR);

		playerRow = targetRow;
		playerCol = targetCol;
		board.setCell(playerRow, playerCol, PLAYER);

		SokobanGUI.stepCounter++;
	}
}