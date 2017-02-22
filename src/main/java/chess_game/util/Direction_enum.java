package chess_game.util;

import org.joml.Vector3f;

public enum Direction_enum
{
	NORTH(new Vector3f(0, 0, 1)),
	SOUTH(new Vector3f(0, 0, -1)),
	WEST(new Vector3f(-1, 0, 0)),
	EAST(new Vector3f(1, 0, 0));

	private final Vector3f direction;

	Direction_enum(Vector3f direction)
	{
		this.direction = direction;
	}

	public Vector3f getVector()
	{
		return direction;
	}

	public static Direction_enum getDirection(Vector3f vector)
	{
		Vector3f normalizedDominantDirection = RelativeDirection_enum.getNormalizedDominantDirection(vector);
		if (normalizedDominantDirection.z > 0)
			return NORTH;
		else if (normalizedDominantDirection.z < 0)
			return SOUTH;
		else if (normalizedDominantDirection.x < 0)
			return WEST;
		else if (normalizedDominantDirection.x > 0)
			return EAST;

		// Should never be reached
		return null;
	}
}
