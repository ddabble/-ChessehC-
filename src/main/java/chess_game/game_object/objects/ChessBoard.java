package chess_game.game_object.objects;

import chess_game.game_object.GameObjectManager;
import chess_game.game_object.GameObject_interface;
import org.joml.Vector3f;

public class ChessBoard implements GameObject_interface
{
	private Vector3f position;

	public static final float SCALE = 8;
	public static final float WIDTH = 1.0f * SCALE;
	public static final float HEIGHT = 0.05f * SCALE;
	public static final float TILE_WIDTH = WIDTH / 8;

	public ChessBoard(Vector3f position)
	{
		this.position = position;
	}

	@Override
	public Vector3f getColor()
	{
		return null;
	}

	@Override
	public Vector3f getPosition()
	{
		return new Vector3f(position);
	}

	@Override
	public void physicsUpdate(GameObjectManager gameObjectManager)
	{

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
