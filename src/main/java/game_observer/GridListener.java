package game_observer;

import chess_game.game_object.GameObject_interface;
import org.joml.Vector3f;
import org.joml.Vector3i;

public interface GridListener/*<T>*/
{
//	/**
//	 * Reports that a (sub-)rectangle of the grid of gridGame has been modified.
//	 * The position and dimensions of the rectangle is provided as four arguments.
//	 * @param gridGame the game that has been modified
//	 * @param col the x-coordinate of the rectangle that has changed
//	 * @param row the y-coordinate of the rectangle that has changed
//	 * @param width the width of the rectangle that has changed
//	 * @param height the height of the rectangle that has changed
//	 */
//	public void gridChanged(GenericGridGame<T> gridGame, int col, int row, int width, int height);

	/**
	 * Should check with {@link chess_game.game_object.GameObjectManager#canMove(GameObject_interface, Vector3i)} first.
	 */
	void pieceMoved(int originRow, int originCol, int destinationRow, int destinationCol, Vector3f destination_graphics);

	void pieceAttacked(int col, int row, boolean isDead);
}
