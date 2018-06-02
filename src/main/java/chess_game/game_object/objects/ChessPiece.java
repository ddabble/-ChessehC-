package chess_game.game_object.objects;

import chess_game.game_object.GameObjectManager;
import chess_game.game_object.GameObject_interface;
import chess_game.util.Direction_enum;
import chess_game.util.RelativeDirection_enum;
import org.joml.Math;
import org.joml.Vector3f;
import org.joml.Vector3i;

import static org.lwjgl.glfw.GLFW.*;

public class ChessPiece implements GameObject_interface
{
	private Vector3f color;

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
		this.color = color;

		this.AI = AI;
	}

	@Override
	public Vector3f getColor()
	{
		return color;
	}

	public void setTarget(GameObject_interface target)
	{
		if (AI)
			this.target = target;
	}

	@Override
	public void physicsUpdate(GameObjectManager gameObjectManager)
	{
		if (!AI || target == null)
			return;

		if (target.isDead())
			gameObjectManager.assignTarget(this);

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

	@Override
	public boolean isMoving()
	{
		// FIXME: Physics shouldn't be dependent on graphics
		return graphics.isMoving();
	}

	@Override
	public void onAttack()
	{
		hitPoints--;
	}

	@Override
	public boolean isDead()
	{
		return hitPoints <= 0;
	}
}
