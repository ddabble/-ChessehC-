package chess_game.game_objects;

import chess_game.event.EventHandler;
import chess_game.game_objects.graphics.GraphicsObjectManager;
import chess_game.util.Direction_enum;
import chess_game.util.RelativeDirection_enum;
import chess_game.window.Window;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.util.ArrayList;
import java.util.Random;

import static chess_game.game_objects.GameObjectManager.ChessPiece_enum.*;
import static org.lwjgl.glfw.GLFW.*;

public class GameObjectManager
{
	private GraphicsObjectManager graphicsObjectManager;

	private ArrayList<GameObject_interface> objects;
	private ArrayList<GameObject_interface> pieces;

	private final GameObject_interface[][] pieceGrid = new GameObject_interface[8][8];

	Player player1;
	Player2 player2;

	private boolean started = false;

	public GameObjectManager()
	{
		objects = new ArrayList<>();
		pieces = new ArrayList<>();

		objects.add(new ChessBoard(new Vector3f(0, 0, 0)));

		addPieces();

		graphicsObjectManager = new GraphicsObjectManager(objects, pieces);
	}

	private void addPieces()
	{
		final float floorHeight = ChessBoard.HEIGHT + 0.001f;
		final float tileWidth = ChessBoard.TILE_WIDTH;

		final Vector3f WHITE = new Vector3f(1, 1, 1);
		final Vector3f BLACK = new Vector3f(0.05f, 0.05f, 0.05f);

		final ChessPiece_enum[][] pieces =
				{
						{ ROOK, KNIGHT, BISHOP, QUEEN, KING, BISHOP, KNIGHT, ROOK },
						{ PAWN, PAWN, PAWN, PAWN, PAWN, PAWN, PAWN, PAWN },
						{ null, null, null, null, null, null, null, null },
						{ null, null, null, null, null, null, null, null },
						{ null, null, null, null, null, null, null, null },
						{ null, null, null, null, null, null, null, null },
						{ PAWN, PAWN, PAWN, PAWN, PAWN, PAWN, PAWN, PAWN },
						{ ROOK, KNIGHT, BISHOP, QUEEN, KING, BISHOP, KNIGHT, ROOK }
				};

		int player1Position_x = 0;
		int player1Position_z = 6;
		pieces[player1Position_z][player1Position_x] = null;

		player1 = new Player(new Vector3f(
				(player1Position_x - 4) * tileWidth + tileWidth / 2,
				floorHeight,
				(player1Position_z - 4) * tileWidth + tileWidth / 2),
				WHITE,
				Window.INITIAL_WINDOW_WIDTH / 2, Window.INITIAL_WINDOW_HEIGHT);
		this.pieces.add(player1);
		pieceGrid[player1Position_z][player1Position_x] = player1;

		int player2Position_x = 7;
		int player2Position_z = 1;
		pieces[player2Position_z][player2Position_x] = null;
		player2 = new Player2(new Vector3f(
				(player2Position_x - 4) * tileWidth + tileWidth / 2,
				floorHeight,
				(player2Position_z - 4) * tileWidth + tileWidth / 2),
				BLACK,
				Window.INITIAL_WINDOW_WIDTH / 2, Window.INITIAL_WINDOW_HEIGHT);
		this.pieces.add(player2);
		pieceGrid[player2Position_z][player2Position_x] = player2;

		for (int z = 0; z < pieces.length; z++)
		{
			for (int x = 0; x < pieces[z].length; x++)
			{
				if (pieces[z][x] == null)
					continue;

				float xCoord = (x - 4) * tileWidth + tileWidth / 2;
				float yCoord = floorHeight;
				float zCoord = (z - 4) * tileWidth + tileWidth / 2;

				Vector3f color = (z < pieces.length / 2) ? BLACK : WHITE;

				ChessPiece piece = null;
				switch (pieces[z][x])
				{
					case KING:
					case QUEEN:
					case ROOK:
					case BISHOP:
					case KNIGHT:
						break;

					case PAWN:
						piece = new ChessPiece(new Vector3f(xCoord, yCoord, zCoord), color, true);
						break;
				}
				if (piece != null)
					this.pieces.add(piece);
				pieceGrid[z][x] = piece;
			}
		}
	}

	private void assignAITargets()
	{
		for (GameObject_interface object : pieces)
		{
			if (object instanceof ChessPiece)
				assignTarget(object);
		}
	}

	public void assignTarget(GameObject_interface object)
	{
		Random rand = new Random();

		if (pieces.size() < 2)
			return;

		int targetIndex = rand.nextInt(pieces.size());
		GameObject_interface target = pieces.get(targetIndex);
		if (target == object)
			target = pieces.get((targetIndex + 1) % pieces.size());

		((ChessPiece)object).setTarget(target);
	}

	public boolean canMove(GameObject_interface piece, Vector3i distance)
	{
		Vector3f piecePosition = worldPositionToChessBoardPosition(piece.getPosition());

		Vector3f destination = piecePosition.add(distance.x, distance.y, distance.z, new Vector3f());

		return destination.x >= 0 && destination.x <= 7 && destination.z >= 0 && destination.z <= 7
				&& !isPositionOccupied(destination);
	}

	/**
	 * Should check with {@link GameObjectManager#canMove(GameObject_interface, Vector3i)} first.
	 */
	public boolean move(GameObject_interface piece, Vector3i distance)
	{
		Vector3f piecePosition = worldPositionToChessBoardPosition(piece.getPosition());

		Vector3f destination = piecePosition.add(distance.x, distance.y, distance.z, new Vector3f());

		pieceGrid[(int)piecePosition.z][(int)piecePosition.x] = null;
		pieceGrid[(int)destination.z][(int)destination.x] = piece;
		return true;
	}

	public boolean attack(GameObject_interface piece, Direction_enum direction)
	{
		// TODO: caused by some pieceGrid' positions being the same, somehow
		if (direction == null)
			return false;

		Vector3f piecePosition = worldPositionToChessBoardPosition(piece.getPosition());

		Vector3f attackedPiecePosition = piecePosition.add(direction.getVector(), new Vector3f());
		if (attackedPiecePosition.x < 0 || attackedPiecePosition.x > 7 || attackedPiecePosition.z < 0 || attackedPiecePosition.z > 7
				|| !isPositionOccupied(attackedPiecePosition))
			return false;

		GameObject_interface attackedPiece = pieceGrid[(int)attackedPiecePosition.z][(int)attackedPiecePosition.x];
		if (attackedPiece.isDead())
		{
			pieceGrid[(int)attackedPiecePosition.z][(int)attackedPiecePosition.x] = null;
			graphicsObjectManager.removeGraphicsObject(attackedPiece.getGraphics());
			pieces.remove(attackedPiece);

			if (!(piece instanceof Player))
				assignTarget(piece);
		}
		return true;
	}

	private boolean isPositionOccupied(Vector3f position)
	{
		GameObject_interface otherPiece = pieceGrid[(int)position.z][(int)position.x];
		if (otherPiece != null)
		{
			Vector3f otherPieceRelativePosition = worldPositionToChessBoardPosition(otherPiece.getPosition()).sub(position, new Vector3f());
			if (Math.abs(otherPieceRelativePosition.x) <= 0.5f || Math.abs(otherPieceRelativePosition.z) <= 0.5f)
				return true;
		}

		return false;
	}

	private static Vector3f worldPositionToChessBoardPosition(Vector3f position)
	{
		final float tileWidth = ChessBoard.TILE_WIDTH;

		return new Vector3f((position.x - tileWidth / 2) * tileWidth + 4, 0, (position.z - tileWidth / 2) * tileWidth + 4);
	}

	public void frameUpdate()
	{
		graphicsObjectManager.graphicsUpdate(player1.getCamera(), RelativeDirection_enum.LEFT);

		graphicsObjectManager.graphicsUpdate(player2.getCamera(), RelativeDirection_enum.RIGHT);
	}

	public void physicsUpdate()
	{
		if (!started)
		{
			if (EventHandler.Key.keys[GLFW_KEY_ENTER])
				assignAITargets();
		}

		// TODO: temporary fix for ConcurrentModificationException
		for (int i = 0; i < pieces.size(); i++)
			pieces.get(i).physicsUpdate(this);

		for (int i = 0; i < objects.size(); i++)
			objects.get(i).physicsUpdate(this);
	}

	enum ChessPiece_enum
	{
		KING, QUEEN, ROOK, BISHOP, KNIGHT, PAWN
	}
}
