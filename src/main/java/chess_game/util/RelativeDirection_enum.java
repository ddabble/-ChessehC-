package chess_game.util;

import org.joml.Vector3f;

public enum RelativeDirection_enum
{
	LEFT, RIGHT, FORWARD, BACKWARD;

	public static Vector3f rotate(Vector3f vector, RelativeDirection_enum direction)
	{
		switch (direction)
		{
			case LEFT:
				return new Vector3f(vector.z, vector.y, -vector.x);

			case RIGHT:
				return new Vector3f(-vector.z, vector.y, vector.x);

			case BACKWARD:
				return new Vector3f(-vector.x, vector.y, -vector.z);

			case FORWARD:
			default:
				return vector;
		}
	}

	public static Vector3f getNormalizedDominantDirection(Vector3f vector)
	{
		if (Math.abs(vector.x) > Math.abs(vector.z))
			return new Vector3f(Math.signum(vector.x), 0, 0);
		else
			return new Vector3f(0, 0, Math.signum(vector.z));
	}
}
