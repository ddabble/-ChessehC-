package chess_game.game_object.objects;

import chess_game.game_object.GameObjectManager;
import chess_game.game_object.GameObject_interface;
import chess_game.game_object.graphics.GraphicsObject_interface;
import chess_game.game_object.graphics.objects.ChessPiece_graphics;
import chess_game.util.Direction_enum;
import chess_game.util.RelativeDirection_enum;
import org.joml.Math;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector3i;

import java.util.Random;

import static org.lwjgl.glfw.GLFW.*;

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

	public ChessPiece(Vector3f position, Vector3fc color, boolean AI)
	{
		this.position = position;

		graphics = new ChessPiece_graphics(this, color);

		this.AI = AI;
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
	public Vector3fc getColor()
	{
		return graphics.getPieceColor();
	}

	@Override
	public GraphicsObject_interface getGraphics()
	{
		return graphics;
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
		{
			if (!gameObjectManager.assignTarget(this))
			{
				target = null;
				return;
			}
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

			if (!tryMoving(moveDistance, currentTime, gameObjectManager))
			{
				if (!stuck)
				{
					lastMoveUpdate = currentTime + UPDATE_COOLDOWN; // Double the update cooldown when stuck
					stuck = true;
					return;
				}

				if (!tryMovingInRandomDirection(moveDistance, currentTime, gameObjectManager))
					lastMoveUpdate = currentTime + UPDATE_COOLDOWN; // Double the update cooldown when stuck
			}
		}
	}

	private boolean tryMoving(Vector3f distance, double currentTime, GameObjectManager gameObjectManager)
	{
		if (gameObjectManager.canMove(this, new Vector3i((int)distance.x, 0, (int)distance.z)))
		{
			if (stuck)
				stuck = false;

			gameObjectManager.move(this, new Vector3i((int)distance.x, 0, (int)distance.z));
			move(distance);
			lastMoveUpdate = currentTime;
			lastAttackUpdate = currentTime;
			return true;
		} else
			return false;
	}

	private boolean tryMovingInRandomDirection(Vector3f distance, double currentTime, GameObjectManager gameObjectManager)
	{
		RelativeDirection_enum randomDirection = RelativeDirection_enum.FORWARD;
		switch (new Random().nextInt(3))
		{
			case 0:
				randomDirection = RelativeDirection_enum.LEFT;
				break;

			case 1:
				randomDirection = RelativeDirection_enum.RIGHT;
				break;

			case 2:
				randomDirection = RelativeDirection_enum.BACKWARD;
				break;
		}
		distance = RelativeDirection_enum.rotate(distance, randomDirection);
		return tryMoving(distance, currentTime, gameObjectManager);
	}

	@Override
	public boolean isMoving()
	{
		// FIXME: Physics shouldn't be dependent on graphics
		return graphics.isMoving();
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
	public boolean onAttack()
	{
		if (graphics.isInvulnerable())
			return false;

		graphics.onAttack();
		hitPoints--;
		return true;
	}

	@Override
	public boolean isDead()
	{
		return hitPoints <= 0;
	}
}
