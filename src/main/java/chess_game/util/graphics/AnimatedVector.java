package chess_game.util.graphics;

import org.joml.AxisAngle4f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

public class AnimatedVector
{
	protected float duration;
	protected boolean isAnimating = false;

	protected double startTime;

	public Vector3f getNextFrame() { throw new IllegalStateException(); }

	public AnimatedVector() {}

	protected AnimatedVector(float duration)
	{
		this.duration = duration;
	}

	public boolean isAnimating()
	{
		return isAnimating;
	}

	public void start()
	{
		isAnimating = true;
		startTime = GLFW.glfwGetTime();
	}

	public class Linear extends AnimatedVector
	{
		private Vector3f origin;
		private Vector3f destination;

		public Linear() { super(); }

		public Linear(Vector3f start, Vector3f end, float duration)
		{
			super(duration);

			this.origin = start;
			this.destination = end;
		}

		@Override
		public Vector3f getNextFrame()
		{
			if (!isAnimating)
				return null;

			double currentTime = GLFW.glfwGetTime();
			if (currentTime < startTime + duration)
			{
				float progressPercentage = (float)(currentTime - startTime) / duration;

				return origin.add(destination.sub(origin, new Vector3f()).mul(progressPercentage, new Vector3f()), new Vector3f());
			} else if (isAnimating)
			{
				isAnimating = false;

				return new Vector3f(destination);
			}

			return null;
		}
	}

	public class Rotation extends AnimatedVector
	{
		private Vector3f origin;
		private Vector3f orbital;

		private float destinationAngle;
		private Vector3f axis;

		public Rotation() { super(); }

		public Rotation(Vector3f origin, Vector3f orbital, float destinationAngle, Vector3f axis, float duration)
		{
			super(duration);

			this.origin = origin;
			this.orbital = orbital;

			this.destinationAngle = destinationAngle;
			this.axis = axis;
		}

		@Override
		public Vector3f getNextFrame()
		{
			if (!isAnimating)
				return null;

			double currentTime = GLFW.glfwGetTime();
			if (currentTime < startTime + duration)
			{
				float progressPercentage = (float)(currentTime - startTime) / duration;

				float angle = destinationAngle * progressPercentage;
				Quaternionf quat = new Quaternionf(new AxisAngle4f(angle, axis));
				Matrix4f rotation = new Matrix4f().rotateAround(quat, origin.x, origin.y, origin.z);

				return orbital.mulDirection(rotation, new Vector3f());
			} else if (isAnimating)
			{
				isAnimating = false;

				Quaternionf quat = new Quaternionf(new AxisAngle4f(destinationAngle, axis));
				Matrix4f rotation = new Matrix4f().rotateAround(quat, origin.x, origin.y, origin.z);

				return orbital.mulDirection(rotation, new Vector3f());
			}

			return null;
		}
	}
}
