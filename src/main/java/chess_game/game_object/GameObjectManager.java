package chess_game.game_object;

import chess_game.event.EventHandler;
import chess_game.event.types.MouseButtonHook_interface;
import chess_game.game_object.graphics.GraphicsObjectManager;
import chess_game.game_object.objects.ChessBoard;
import chess_game.game_object.objects.ChessPiece;
import chess_game.game_object.objects.GUImenu;
import chess_game.util.Direction_enum;
import chess_game.window.Window;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector3i;

import java.util.ArrayList;
import java.util.Random;

import static chess_game.game_object.GameObjectManager.ChessPiece_enum.*;
import static org.lwjgl.glfw.GLFW.*;

public class GameObjectManager implements MouseButtonHook_interface
{
	private static final Vector3fc WHITE = new Vector3f(1, 1, 1).toImmutable();
	private static final Vector3fc BLACK = new Vector3f(0.05f, 0.05f, 0.05f).toImmutable();

	private GraphicsObjectManager graphicsObjectManager;

	private ArrayList<GameObject_interface> objects;
	private ArrayList<GameObject_interface> pieces_white;
	private ArrayList<GameObject_interface> pieces_black;

	private final GameObject_interface[][] pieceGrid = new GameObject_interface[8][8];

	private Player player1;
	private Player player2;

	private boolean AIstarted = false;

	private GameState_enum gameState = GameState_enum.MAIN_MENU;

	public GameObjectManager()
	{
		objects = new ArrayList<>();
		pieces_white = new ArrayList<>();
		pieces_black = new ArrayList<>();
		addPieces();

		objects.add(new ChessBoard(new Vector3f(0, 0, 0)));

		graphicsObjectManager = new GraphicsObjectManager(this, objects, pieces_white, pieces_black);

		EventHandler.MouseButton.addHook(this);
	}

	public GameState_enum getGameState()
	{
		return gameState;
	}

	private void addPieces()
	{
		final float floorHeight = ChessBoard.HEIGHT + 0.001f;
		final float tileWidth = ChessBoard.TILE_WIDTH;

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
		this.pieces_white.add(player1);
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
		this.pieces_black.add(player2);
		pieceGrid[player2Position_z][player2Position_x] = player2;

		for (int z = 0; z < pieces.length; z++)
		{
			for (int x = 0; x < pieces[z].length; x++)
			{
				if (pieces[z][x] == null)
					continue;

				float xCoord = (x - 4) * tileWidth + tileWidth / 2;
				float zCoord = (z - 4) * tileWidth + tileWidth / 2;

				Vector3fc color = (z < pieces.length / 2) ? BLACK : WHITE;

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
						piece = new ChessPiece(new Vector3f(xCoord, floorHeight, zCoord), color, true);
						break;
				}
				if (piece != null)
				{
					if (color == WHITE)
						this.pieces_white.add(piece);
					else
						this.pieces_black.add(piece);
				}
				pieceGrid[z][x] = piece;
			}
		}
	}

	private void assignAITargets()
	{
		for (GameObject_interface piece : pieces_white)
			assignTarget(piece);

		for (GameObject_interface piece : pieces_black)
			assignTarget(piece);
	}

	public boolean assignTarget(GameObject_interface object)
	{
		if (!(object instanceof ChessPiece))
			return false;

		Random rand = new Random();
		ArrayList<GameObject_interface> opponentPieces;
		if (object.getColor() == WHITE)
			opponentPieces = pieces_black;
		else if (object.getColor() == BLACK)
			opponentPieces = pieces_white;
		else
			throw new IllegalStateException("Piece has neither white nor black color!");

		if (opponentPieces.size() == 0)
			return false;

		int targetIndex = rand.nextInt(opponentPieces.size());
		((ChessPiece)object).setTarget(opponentPieces.get(targetIndex));
		return true;
	}

	public boolean canMove(GameObject_interface object, Vector3i distance)
	{
		Vector3f piecePosition = worldPositionToChessBoardPosition(object.getPosition());

		Vector3f destination = piecePosition.add(distance.x, distance.y, distance.z, new Vector3f());

		return destination.x >= 0 && destination.x <= 7
				&& destination.z >= 0 && destination.z <= 7
				&& !isPositionOccupied(destination);
	}

	/**
	 * Should check with {@link GameObjectManager#canMove(GameObject_interface, Vector3i)} first.
	 */
	public void move(GameObject_interface object, Vector3i distance)
	{
		Vector3f piecePosition = worldPositionToChessBoardPosition(object.getPosition());

		Vector3f destination = piecePosition.add(distance.x, distance.y, distance.z, new Vector3f());

		pieceGrid[(int)piecePosition.z][(int)piecePosition.x] = null;
		pieceGrid[(int)destination.z][(int)destination.x] = object;
	}

	public boolean attack(GameObject_interface object, Direction_enum direction)
	{
		// TODO: caused by some pieceGrid-positions being the same, somehow
		if (direction == null)
			return false;
		else if (object.isMoving())
			return false;

		Vector3f piecePosition = worldPositionToChessBoardPosition(object.getPosition());

		Vector3f attackedPiecePosition = piecePosition.add(direction.getVector(), new Vector3f());
		if (attackedPiecePosition.x < 0 || attackedPiecePosition.x > 7 || attackedPiecePosition.z < 0 || attackedPiecePosition.z > 7
				|| !isPositionOccupied(attackedPiecePosition))
			return false;

		int col = (int)attackedPiecePosition.x, row = (int)attackedPiecePosition.z;
		GameObject_interface attackedPiece = pieceGrid[row][col];
		attackedPiece.onAttack();
		if (attackedPiece.isDead())
		{
			pieceGrid[row][col] = null;
			graphicsObjectManager.removeGraphicsObject(attackedPiece.getGraphics());

			if (attackedPiece.getColor() == WHITE)
				pieces_white.remove(attackedPiece);
			else if (attackedPiece.getColor() == BLACK)
				pieces_black.remove(attackedPiece);
			else
				throw new IllegalStateException("Attacked piece has neither white nor black color!");

			assignTarget(object);
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
		graphicsObjectManager.graphicsUpdate(player1.getCamera(), player2.getCamera(), this);
	}

	private boolean hasReleasedPauseButton = true;

	public void physicsUpdate()
	{
		if (!AIstarted)
		{
			if (EventHandler.Key.keys[GLFW_KEY_ENTER] || EventHandler.Key.keys[GLFW_KEY_KP_ENTER])
			{
				assignAITargets();
				AIstarted = true;
			}
		}

		if (!hasReleasedPauseButton && !EventHandler.Key.keys[GLFW_KEY_ESCAPE])
			hasReleasedPauseButton = true;

		switch (gameState)
		{
			case MAIN_MENU:
				return;

			case PLAYING:
				if (hasReleasedPauseButton && EventHandler.Key.keys[GLFW_KEY_ESCAPE])
				{
					changeGameState(GameState_enum.PAUSED);
					hasReleasedPauseButton = false;
					return;
				}
				break;

			case PAUSED:
				if (hasReleasedPauseButton && EventHandler.Key.keys[GLFW_KEY_ESCAPE])
				{
					changeGameState(GameState_enum.PLAYING);
					hasReleasedPauseButton = false;
				} else
					return;

				break;
		}

		// TODO: temporary fix for ConcurrentModificationException
		for (int i = 0; i < pieces_white.size(); i++)
			pieces_white.get(i).physicsUpdate(this);
		for (int i = 0; i < pieces_black.size(); i++)
			pieces_black.get(i).physicsUpdate(this);

		for (int i = 0; i < objects.size(); i++)
			objects.get(i).physicsUpdate(this);
	}

	@Override
	public void mouseButtonCallback(int button, int action)
	{
		GUImenu.Action_enum gui_action = graphicsObjectManager.getGuiAction(button, action, this);
		if (gui_action == null)
			return;

		switch (gui_action)
		{
			case NEW_GAME:
				changeGameState(GameState_enum.PLAYING);
				break;

			case SAVE_GAME:
				// TODO: saveGame()
				break;

			case LOAD_GAME:
				// TODO: loadGame()
				changeGameState(GameState_enum.PLAYING);
				break;

			case MAIN_MENU:
				// TODO: are you sure you want to quit without saving?
				changeGameState(GameState_enum.MAIN_MENU);
				break;

			case RESUME:
				changeGameState(GameState_enum.PLAYING);
				break;
		}
	}

	private void changeGameState(GameState_enum newGameState)
	{
		gameState = newGameState;

		graphicsObjectManager.onChangedGameState(this);
	}

	public enum GameState_enum
	{
		MAIN_MENU, PLAYING, PAUSED
	}

	public enum ChessPiece_enum
	{
		KING, QUEEN, ROOK, BISHOP, KNIGHT, PAWN
	}
}
