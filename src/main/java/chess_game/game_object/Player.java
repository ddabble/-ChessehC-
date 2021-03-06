package chess_game.game_object;

import chess_game.event.EventHandler;
import chess_game.game_object.graphics.GraphicsObject_interface;
import chess_game.game_object.graphics.objects.ChessPiece_graphics;
import chess_game.game_object.objects.ChessPiece;
import chess_game.util.Direction_enum;
import chess_game.util.RelativeDirection_enum;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector3i;

import static org.lwjgl.glfw.GLFW.*;

public class Player implements GameObject_interface
{
	protected final ThirdPersonCamera camera;

	protected final ChessPiece model;

	protected int hitPoints;

	protected static final double UPDATE_COOLDOWN = 0.2;
	protected double lastMoveUpdate = 0;
	protected double lastRotationUpdate = 0;
	protected double lastAttackUpdate = 0;

	protected int FORWARD_KEY = GLFW_KEY_W;
	protected int BACKWARD_KEY = GLFW_KEY_S;
	protected int LEFT_KEY = GLFW_KEY_A;
	protected int RIGHT_KEY = GLFW_KEY_D;

	protected int TURN_LEFT_KEY = GLFW_KEY_F;
	protected int TURN_RIGHT_KEY = GLFW_KEY_G;

	protected int ATTACK_KEY = GLFW_KEY_SPACE;

	public Player(Vector3f position, Vector3fc color, int windowWidth, int windowHeight)
	{
		this(position, color, Direction_enum.NORTH, windowWidth, windowHeight);
	}

	public Player(Vector3f position, Vector3fc color, Direction_enum startLookDirection, int windowWidth, int windowHeight)
	{
		model = new ChessPiece(position, color, false);

		camera = new ThirdPersonCamera(new Vector3f(position).add(0, 1, 0),
				startLookDirection.getVector().mul(3).add(0, 2, 0),
				windowWidth, windowHeight);

		hitPoints = 10;
	}

	public ThirdPersonCamera getCamera()
	{
		return camera;
	}

	@Override
	public Vector3f getPosition()
	{
		return model.getPosition();
	}

	@Override
	public Vector3fc getColor()
	{
		return model.getColor();
	}

	@Override
	public GraphicsObject_interface getGraphics()
	{
		return model.getGraphics();
	}

	@Override
	public void physicsUpdate(GameObjectManager gameObjectManager)
	{
		// TODO: cache the player's next move and move there as soon as the last move is finished (or..?)

		boolean[] keys = EventHandler.Key.keys;

		double currentTime = glfwGetTime();
		if (currentTime - lastRotationUpdate >= UPDATE_COOLDOWN)
		{
			RelativeDirection_enum rotation = null;
			if (keys[TURN_LEFT_KEY])
			{
				rotation = RelativeDirection_enum.LEFT;
			}
			if (keys[TURN_RIGHT_KEY])
			{
				rotation = RelativeDirection_enum.RIGHT;
			}

			if (rotation != null)
			{
				camera.rotateAround(rotation);
				lastRotationUpdate = currentTime;
			}
		}

		if (currentTime - lastAttackUpdate >= UPDATE_COOLDOWN)
		{
			if (keys[ATTACK_KEY])
			{
				Vector3f look = camera.getRelativePosition().negate();
				if (gameObjectManager.attack(this, Direction_enum.getDirection(look)))
					lastAttackUpdate = currentTime;
			}
		}

		if (currentTime - lastMoveUpdate >= UPDATE_COOLDOWN)
		{
			boolean updated = false;

			Vector3f look = camera.getRelativePosition().negate();
			look = RelativeDirection_enum.getNormalizedDominantDirection(look);

			Vector3f moveDistance = new Vector3f();
			if (keys[FORWARD_KEY])
			{
				moveDistance.add(RelativeDirection_enum.rotate(look, RelativeDirection_enum.FORWARD));
				updated = true;
			}
			if (keys[BACKWARD_KEY])
			{
				moveDistance.add(RelativeDirection_enum.rotate(look, RelativeDirection_enum.BACKWARD));
				updated = true;
			}
			if (keys[LEFT_KEY])
			{
				moveDistance.add(RelativeDirection_enum.rotate(look, RelativeDirection_enum.LEFT));
				updated = true;
			}
			if (keys[RIGHT_KEY])
			{
				moveDistance.add(RelativeDirection_enum.rotate(look, RelativeDirection_enum.RIGHT));
				updated = true;
			}

			if (updated)
			{
				if (gameObjectManager.canMove(this, new Vector3i((int)moveDistance.x, 0, (int)moveDistance.z)))
				{
					gameObjectManager.move(this, new Vector3i((int)moveDistance.x, 0, (int)moveDistance.z));
					model.moveTo(model.getPosition().add(moveDistance));
					camera.move(moveDistance);
					lastMoveUpdate = currentTime;
				}
			}
		}
	}

	@Override
	public boolean isMoving()
	{
		return model.isMoving();
	}

	@Override
	public boolean onAttack()
	{
		if (((ChessPiece_graphics)getGraphics()).isInvulnerable())
			return false;

		((ChessPiece_graphics)getGraphics()).onAttack();
		hitPoints--;
		return true;
	}

	@Override
	public boolean isDead()
	{
		return hitPoints <= 0;
	}
}
