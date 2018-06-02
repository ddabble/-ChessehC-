package chess_game.game_object;

import chess_game.game_object.graphics.GraphicsObject_interface;
import org.joml.Vector3f;

public interface GameObject_interface
{
	Vector3f getPosition();

	GraphicsObject_interface getGraphics();

	void physicsUpdate(GameObjectManager gameObjectManager);

	boolean isMoving();

	void onAttack();

	boolean isDead();
}
