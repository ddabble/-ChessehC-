package chess_game.util;

import org.joml.Vector3f;
import org.joml.Vector3fc;

public enum Direction_enum
{
	NORTH(new Vector3f(0, 0, 1).toImmutable()),
	SOUTH(new Vector3f(0, 0, -1).toImmutable()),
	WEST(new Vector3f(-1, 0, 0).toImmutable()),
	EAST(new Vector3f(1, 0, 0).toImmutable());

	private final Vector3fc direction;

	Direction_enum(Vector3fc direction)
	{
		this.direction = direction;
	}

	public Vector3f getVector()
	{
		return new Vector3f(direction);
	}

	public static Direction_enum getDirection(Vector3fc vector)
	{
		Vector3fc normalizedDominantDirection = RelativeDirection_enum.getNormalizedDominantDirection(vector);
		if (normalizedDominantDirection.z() > 0)
			return NORTH;
		else if (normalizedDominantDirection.z() < 0)
			return SOUTH;
		else if (normalizedDominantDirection.x() < 0)
			return WEST;
		else if (normalizedDominantDirection.x() > 0)
			return EAST;

		// Should never be reached
		return null;
	}
}
