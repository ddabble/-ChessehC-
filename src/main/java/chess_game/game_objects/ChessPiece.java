package chess_game.game_objects;

import chess_game.game_objects.graphics.GraphicsObject_interface;
import chess_game.game_objects.graphics.objects.ChessPiece_graphics;
import chess_game.util.Direction_enum;
import chess_game.util.RelativeDirection_enum;
import org.joml.Math;
import org.joml.Vector3f;
import org.joml.Vector3i;

import static org.lwjgl.glfw.GLFW.glfwGetTime;

public class ChessPiece implements GameObject_interface
{
	private final ChessPiece_graphics graphics;

	private Vector3f position;

	private int hitPoints = 3;

	private boolean AI;
	private GameObject_interface target;

	private static final double UPDATE_COOLDOWN = 0.5;
	private double lastMoveUpdate = 0;
	private boolean stuck = false;
	private double lastAttackUpdate = 0;

	public ChessPiece(Vector3f position, Vector3f color, boolean AI)
	{
		this.position = position;

		graphics = new ChessPiece_graphics(this, color);

		this.AI = AI;
	}

	public void setTarget(GameObject_interface target)
	{
		if (AI)
			this.target = target;
	}

	@Override
	public GraphicsObject_interface getGraphics()
	{
		return graphics;
	}

	@Override
	public void physicsUpdate(GameObjectManager gameObjectManager)
	{
		if (!AI)
			return;

		if (target == null)
		{
			if (lastMoveUpdate > 0 || lastAttackUpdate > 0)
				gameObjectManager.assignTarget(this);
			else
				return;
		}

		double currentTime = glfwGetTime();
		if (currentTime - lastAttackUpdate >= UPDATE_COOLDOWN)
		{
			Vector3f targetDistance = target.getPosition().sub(this.position, new Vector3f());
			if (Math.abs(targetDistance.x) + Math.abs(targetDistance.z) < 1.01f)
			{
				if (gameObjectManager.attack(this, Direction_enum.getDirection(targetDistance)))
				{
					lastAttackUpdate = currentTime;
					lastMoveUpdate = currentTime;
					return;
				}
			}
		}

		if (currentTime - lastMoveUpdate >= UPDATE_COOLDOWN)
		{
			Vector3f targetDistance = target.getPosition().sub(this.position, new Vector3f());
			Vector3f moveDistance = RelativeDirection_enum.getNormalizedDominantDirection(targetDistance);

			if (gameObjectManager.canMove(this, new Vector3i((int)moveDistance.x, 0, (int)moveDistance.z)))
			{
				if (stuck)
				{
					lastMoveUpdate = currentTime;
					stuck = false;
					return;
				}

				gameObjectManager.move(this, new Vector3i((int)moveDistance.x, 0, (int)moveDistance.z));
				move(moveDistance);
				lastMoveUpdate = currentTime;
				lastAttackUpdate = currentTime;
			} else
				stuck = true;
		}
	}

	@Override
	public Vector3f getPosition()
	{
		return new Vector3f(position);
	}

	public void setPosition(Vector3f position)
	{
		this.position = position;
	}

	public void moveTo(Vector3f newPosition)
	{
		graphics.startMoveAnimation(newPosition);
	}

	public void move(Vector3f distance)
	{
		moveTo(position.add(distance, new Vector3f()));
	}

	@Override
	public boolean isDead()
	{
		graphics.onAttack();

		return --hitPoints <= 0;
	}
}
