package chess_game.game_object.objects;

import chess_game.game_object.GameObjectManager;
import chess_game.game_object.GameObject_interface;
import chess_game.game_object.graphics.GraphicsObject_interface;
import chess_game.game_object.graphics.objects.ChessBoard_graphics;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public class ChessBoard implements GameObject_interface
{
	private final ChessBoard_graphics graphics;

	private Vector3f position;

	public static final float SCALE = 8;
	public static final float WIDTH = 1.0f * SCALE;
	public static final float HEIGHT = 0.05f * SCALE;
	public static final float TILE_WIDTH = WIDTH / 8;

	public ChessBoard(Vector3f position)
	{
		this.position = position;

		graphics = new ChessBoard_graphics(this);
	}

	@Override
	public Vector3f getPosition()
	{
		return new Vector3f(position);
	}

	@Override
	public Vector3fc getColor()
	{
		// TODO:
		throw new RuntimeException();
	}

	@Override
	public GraphicsObject_interface getGraphics()
	{
		return graphics;
	}

	@Override
	public void physicsUpdate(GameObjectManager gameObjectManager)
	{

	}

	@Override
	public boolean isMoving()
	{
		// TODO:
		throw new RuntimeException();
	}

	@Override
	public void onAttack()
	{
		// TODO:
		throw new RuntimeException();
	}

	@Override
	public boolean isDead()
	{
		// TODO:
		throw new RuntimeException();
	}
}
