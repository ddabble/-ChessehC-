package chess_game.game_object.graphics;

import chess_game.game_object.GameObjectManager;
import chess_game.game_object.ThirdPersonCamera;

public interface GraphicsObject_interface
{
	void graphicsUpdate(ThirdPersonCamera camera, GameObjectManager gameObjectManager);
}
