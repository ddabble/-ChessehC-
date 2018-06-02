package chess_game.game_object;

import chess_game.event.EventHandler;
import chess_game.event.types.MouseButtonHook_interface;
import chess_game.game_object.objects.ChessBoard;
import chess_game.game_object.objects.ChessPiece;
import chess_game.game_object.objects.GUImenu;
import chess_game.util.Direction_enum;
import chess_game.window.Window;
import game_observer.GameObserver;
import game_observer.GridListener;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.util.ArrayList;
import java.util.Random;

import static chess_game.game_object.GameObjectManager.ChessPiece_enum.*;
import static org.lwjgl.glfw.GLFW.*;

public class GameObjectManager implements MouseButtonHook_interface
{
	private ArrayList<GridListener> gridListeners;

	private ChessBoard chessBoard;
	private ArrayList<GameObject_interface> pieces;

	private final GameObject_interface[][] pieceGrid = new GameObject_interface[8][8];

	private Player player1;
	private Player player2;

	private boolean AIstarted = false;

	private GameState_enum gameState = GameState_enum.MAIN_MENU;

	public GameObjectManager()
	{
		gridListeners = new ArrayList<>();

		chessBoard = new ChessBoard(new Vector3f(0, 0, 0));

		pieces = new ArrayList<>();

		addPieces();

		EventHandler.MouseButton.addHook(this);
	}

	public GameState_enum getGameState()
	{
		return gameState;
	}

	public int getRowCount()
	{
		return pieceGrid.length;
	}

	public int getColumnCount()
	{
		return pieceGrid[0].length;
	}

	public GameObject_interface getPiece(int col, int row)
	{
		return pieceGrid[row][col];
	}

	public ChessBoard getChessBoard()
	{
		return chessBoard;
	}

	public Player getPlayer1()
	{
		return player1;
	}

	public Player getPlayer2()
	{
		return player2;
	}

	public void addGridListener(GridListener listener)
	{
		gridListeners.add(listener);
	}

	public void removeGridListener(GridListener listener)
	{
		gridListeners.remove(listener);
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
						piece = new ChessPiece(new Vector3f(xCoord, floorHeight, zCoord), color, true);
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

		return destination.x >= 0 && destination.x <= 7
				&& destination.z >= 0 && destination.z <= 7
				&& !isPositionOccupied(destination);
	}

	/**
	 * Should check with {@link GameObjectManager#canMove(GameObject_interface, Vector3i)} first.
	 */
	public void move(GameObject_interface piece, Vector3i distance)
	{
		Vector3f piecePosition = worldPositionToChessBoardPosition(piece.getPosition());

		Vector3f destination = piecePosition.add(distance.x, distance.y, distance.z, new Vector3f());

		int originCol = (int)piecePosition.x, originRow = (int)piecePosition.z;
		int destinationCol = (int)destination.x, destinationRow = (int)destination.z;

		pieceGrid[originRow][originCol] = null;
		pieceGrid[destinationRow][destinationCol] = piece;

		gridListeners.forEach((GridListener listener) ->
				listener.pieceMoved(originRow, originCol, destinationRow, destinationCol,
						piece.getPosition().add(distance.x, distance.y, distance.z, new Vector3f())));
	}

	public boolean attack(GameObject_interface piece, Direction_enum direction)
	{
		// TODO: caused by some pieceGrid-positions being the same, somehow
		if (direction == null)
			return false;
		else if (piece.isMoving())
			return false;

		Vector3f piecePosition = worldPositionToChessBoardPosition(piece.getPosition());

		Vector3f attackedPiecePosition = piecePosition.add(direction.getVector(), new Vector3f());
		if (attackedPiecePosition.x < 0 || attackedPiecePosition.x > 7 || attackedPiecePosition.z < 0 || attackedPiecePosition.z > 7
				|| !isPositionOccupied(attackedPiecePosition))
			return false;

		int col = (int)attackedPiecePosition.x, row = (int)attackedPiecePosition.z;
		GameObject_interface attackedPiece = pieceGrid[row][col];
		attackedPiece.onAttack();
		boolean isDead = attackedPiece.isDead();
		if (isDead)
		{
			pieceGrid[row][col] = null;
			pieces.remove(attackedPiece);

			if (!(piece instanceof Player))
				assignTarget(piece);
		}

		gridListeners.forEach((GridListener listener) ->
				listener.pieceAttacked(col, row, isDead));

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

	private boolean hasReleasedPauseButton = true;

	public void physicsUpdate()
	{
		if (!AIstarted)
		{
			if (EventHandler.Key.keys[GLFW_KEY_ENTER])
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
		for (int i = 0; i < pieces.size(); i++)
			pieces.get(i).physicsUpdate(this);

		chessBoard.physicsUpdate(this);
	}

	@Override
	public void mouseButtonCallback(int button, int action)
	{
		GUImenu.Action_enum gui_action = getGameObserver().getGuiAction(button, action, this);
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

	private GameObserver getGameObserver()
	{
		for (GridListener listener : gridListeners)
		{
			if (listener instanceof GameObserver)
				return (GameObserver)listener;
		}

		throw new IllegalStateException("No GameObserver..?");
	}

	private void changeGameState(GameState_enum newGameState)
	{
		gameState = newGameState;

		getGameObserver().onChangedGameState(this);
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
