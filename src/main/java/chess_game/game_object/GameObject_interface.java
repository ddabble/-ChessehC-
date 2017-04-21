package chess_game.game_object;

import org.joml.Vector3f;

public interface GameObject_interface
{
	Vector3f getPosition();

	Vector3f getColor();

	void physicsUpdate(GameObjectManager gameObjectManager);

	void onAttack();

	boolean isDead();
}
