package chess_game.game_objects;

import org.joml.Vector3f;

import static org.lwjgl.glfw.GLFW.*;

public class Player2 extends Player
{
	public Player2(Vector3f position, Vector3f color, int windowWidth, int windowHeight)
	{
		super(position, color, windowWidth, windowHeight);

		FORWARD_KEY = GLFW_KEY_P;
		BACKWARD_KEY = GLFW_KEY_SEMICOLON;
		LEFT_KEY = GLFW_KEY_L;
		RIGHT_KEY = GLFW_KEY_APOSTROPHE;

		TURN_LEFT_KEY = GLFW_KEY_KP_4;
		TURN_RIGHT_KEY = GLFW_KEY_KP_5;

		ATTACK_KEY = GLFW_KEY_RIGHT_CONTROL;
	}
}
