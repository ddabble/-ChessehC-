package chess_game.game_object;

import chess_game.event.EventHandler;
import chess_game.event.types.FramebufferSizeHook_interface;
import chess_game.util.RelativeDirection_enum;
import chess_game.util.graphics.AnimatedVector;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public class ThirdPersonCamera implements FramebufferSizeHook_interface
{
	private Vector3f target;
	private Vector3f relativeCameraPosition;
	private final Vector3fc up = new Vector3f(0, 1, 0).toImmutable();

	public Matrix4f viewMatrix;
	public Matrix4f projectionMatrix;

	private AnimatedVector.Linear moveAnimation;
	private AnimatedVector.Rotation rotationAnimation;

	public ThirdPersonCamera(Vector3f target, Vector3f relativeCameraPosition, int windowWidth, int windowHeight)
	{
		this.target = target;
		this.relativeCameraPosition = relativeCameraPosition;

		viewMatrix = new Matrix4f();
		updateViewMatrix();

		updateProjectionMatrix(windowWidth, windowHeight);

		moveAnimation = new AnimatedVector().new Linear();
		rotationAnimation = new AnimatedVector().new Rotation();

		EventHandler.FramebufferSize.addHook(this);
	}

	public Vector3f getRelativePosition()
	{
		return new Vector3f(relativeCameraPosition);
	}

	public Vector3f getActualPosition()
	{
		return target.add(relativeCameraPosition, new Vector3f());
	}

	public Vector3f getTarget()
	{
		return new Vector3f(target);
	}

	public void move(Vector3f distance)
	{
		moveAnimation = new AnimatedVector().new Linear(target, target.add(distance, new Vector3f()), 0.1f);
		moveAnimation.start();
	}

	public void rotateAround(RelativeDirection_enum direction)
	{
		double angle;
		if (direction == RelativeDirection_enum.LEFT)
			angle = -Math.PI / 2;
		else if (direction == RelativeDirection_enum.RIGHT)
			angle = Math.PI / 2;
		else
			return;

		rotationAnimation = new AnimatedVector().new Rotation(target, relativeCameraPosition, (float)angle, new Vector3f(0, -1, 0), 0.1f);
		rotationAnimation.start();
	}

	public void graphicsUpdate()
	{
		boolean updated = false;

		Vector3f nextPosition = moveAnimation.getNextFrame();
		if (nextPosition != null)
		{
			target = nextPosition;
			updated = true;
		}

		Vector3f nextRotation = rotationAnimation.getNextFrame();
		if (nextRotation != null)
		{
			relativeCameraPosition = nextRotation;
			updated = true;
		}

		if (updated)
			updateViewMatrix();
	}

	private void updateViewMatrix()
	{
		viewMatrix = new Matrix4f().lookAt(getActualPosition(), target, up);
	}

	private void updateProjectionMatrix(float windowWidth, float windowHeight)
	{
		projectionMatrix = new Matrix4f().mul(new Matrix4f().perspective((float)Math.PI / 4, windowWidth / windowHeight, 0.1f, 100.0f));
	}

	@Override
	public void framebufferSizeCallback(int lastWidth, int lastHeight, int newWidth, int newHeight)
	{
		updateProjectionMatrix(newWidth / 2.0f, newHeight);
	}
}
