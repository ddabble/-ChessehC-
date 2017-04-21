package game_observer;

import chess_game.game_object.GameObjectManager;
import chess_game.game_object.GameObject_interface;
import chess_game.game_object.Player;
import game_observer.graphics.GraphicsObjectManager;
import game_observer.graphics.GraphicsObject_interface;
import game_observer.graphics.objects.ChessBoard_graphics;
import game_observer.graphics.objects.ChessPiece_graphics;
import chess_game.game_object.objects.ChessPiece;
import chess_game.game_object.objects.GUImenu;
import chess_game.window.Window;
import org.joml.Vector3f;

import java.util.ArrayList;

import static org.lwjgl.glfw.GLFW.*;

public class GameObserver implements GridListener
{
	private GenericGridGame<GameObject_interface> game;

	private GraphicsObject_interface[][] pieceGrid;

	private GraphicsObjectManager graphicsObjectManager;

	public GameObserver(GenericGridGame<GameObject_interface> game)
	{
		this.game = game;

		Window.init();
	}

	public void start(GameObjectManager gameObjectManager)
	{
		game.addGridListener(this);

		createGraphicsObjects(gameObjectManager);

		while (!glfwWindowShouldClose(Window.getGLFWwindow()))
		{
			graphicsObjectManager.graphicsUpdate(gameObjectManager);

			glfwSwapBuffers(Window.getGLFWwindow());

			glfwPollEvents();
		}

		game.removeGridListener(this);
	}

	private void createGraphicsObjects(GameObjectManager gameObjectManager)
	{
		int columnCount = game.getColumnCount(), rowCount = game.getRowCount();
		pieceGrid = new GraphicsObject_interface[rowCount][columnCount];

		ArrayList<GraphicsObject_interface> graphicsObjects = new ArrayList<>();
		graphicsObjects.add(new ChessBoard_graphics(gameObjectManager.getChessBoard()));

		for (int row = 0; row < rowCount; row++)
		{
			for (int col = 0; col < columnCount; col++)
			{
				GameObject_interface gameObject = game.getCell(col, row);
				if (gameObject != null)
				{
					GraphicsObject_interface graphicsObject;

					if (gameObject instanceof Player)
						graphicsObject = new ChessPiece_graphics(((Player)gameObject).getModel(), gameObject.getColor());
					else
						graphicsObject = new ChessPiece_graphics((ChessPiece)gameObject, gameObject.getColor());

					pieceGrid[row][col] = graphicsObject;
					graphicsObjects.add(graphicsObject);
				}
			}
		}

		graphicsObjectManager = new GraphicsObjectManager(gameObjectManager.getPlayer1().getCamera(), gameObjectManager.getPlayer2().getCamera(), graphicsObjects);
	}

	public void terminate()
	{
		Window.terminate();
	}

	@Override
	public void pieceMoved(int originRow, int originCol, int destinationRow, int destinationCol, Vector3f destination_graphics)
	{
		((ChessPiece_graphics)pieceGrid[originRow][originCol]).startMoveAnimation(destination_graphics);

		pieceGrid[destinationRow][destinationCol] = pieceGrid[originRow][originCol];
		pieceGrid[originRow][originCol] = null;
	}

	@Override
	public void pieceAttacked(int col, int row, boolean isDead)
	{
		ChessPiece_graphics piece = (ChessPiece_graphics)pieceGrid[row][col];

		piece.onAttack();

		if (isDead)
			graphicsObjectManager.removeGraphicsObject(piece);
	}

	public GUImenu.Action_enum getGuiAction(int button, int action, GameObjectManager gameObjectManager)
	{
		return graphicsObjectManager.getGuiAction(button, action, gameObjectManager);
	}

	public void onChangedGameState(GameObjectManager gameObjectManager)
	{
		graphicsObjectManager.onChangedGameState(gameObjectManager);
	}
}
