package chess_game;

import chess_game.game_object.GameObjectManager;
import chess_game.game_object.GameObject_interface;
import chess_game.util.Util;
import game_observer.GenericGridGame;
import game_observer.GridListener;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.IOException;

import static org.lwjgl.glfw.GLFW.*;

// TODO: FIX ALL OF THIS MESSY SPAGHETTI CODE JEEZ

// TODO: Add swords,
//       a proper intro with the camera starting with a 2D-view from directly above the board, then panning down to the current FPS-view,
//       LAN instead of/in addition to split-screen,
//       mouse-controlled camera

public class Game implements GenericGridGame<GameObject_interface>
{
	private GameObjectManager gameObjectManager;

	private Physics physicsThread;

	public Game()
	{
		physicsThread = new Physics();
	}

	GameObjectManager getGameObjectManager()
	{
		return gameObjectManager;
	}

	void run()
	{
		gameObjectManager = new GameObjectManager();

		physicsThread.start();
	}

	void terminate()
	{
		physicsThread.keepRunning = false;
	}

	@Override
	public boolean canUndo()
	{
		throw new NotImplementedException();
	}

	@Override
	public void undo()
	{
		throw new NotImplementedException();
	}

	@Override
	public boolean canRedo()
	{
		throw new NotImplementedException();
	}

	@Override
	public void redo()
	{
		throw new NotImplementedException();
	}

	@Override
	public void load(String fileName) throws IOException
	{
		throw new NotImplementedException();
	}

	@Override
	public void save(String fileName) throws IOException
	{
		throw new NotImplementedException();
	}

	@Override
	public int getColumnCount()
	{
		return gameObjectManager.getColumnCount();
	}

	@Override
	public int getRowCount()
	{
		return gameObjectManager.getRowCount();
	}

	@Override
	public GameObject_interface getCell(int col, int row)
	{
		return gameObjectManager.getPiece(col, row);
	}

	@Override
	public void addGridListener(GridListener listener)
	{
		gameObjectManager.addGridListener(listener);
	}

	@Override
	public void removeGridListener(GridListener listener)
	{
		gameObjectManager.removeGridListener(listener);
	}

	public class Physics extends Thread
	{
		public static final int PHYSICS_UPDATES_PER_SECOND = 50;
		public static final double PHYSICS_UPDATE_INTERVAL = 1.0 / PHYSICS_UPDATES_PER_SECOND;

		boolean keepRunning = true;

		double lastPhysicsTime;
		boolean hasSlept = false;

		public Physics()
		{
			super("physics");
		}

		@Override
		public void run()
		{
			lastPhysicsTime = glfwGetTime() - PHYSICS_UPDATE_INTERVAL;
			while (keepRunning)
			{
				double currentTime = glfwGetTime();
				if (currentTime - lastPhysicsTime >= PHYSICS_UPDATE_INTERVAL)
				{
					physicsUpdate();
					lastPhysicsTime += PHYSICS_UPDATE_INTERVAL;
					hasSlept = false;
				} else if (!hasSlept)
				{
					double durationUntilNextPhysicsTime = (lastPhysicsTime + PHYSICS_UPDATE_INTERVAL - currentTime) * 0.90; // Leaves 10 % of the actual duration as a time buffer, just in case
					Util.sleep((int)durationUntilNextPhysicsTime * 1000);
					hasSlept = true;
				}
			}
		}

		private void physicsUpdate()
		{
			gameObjectManager.physicsUpdate();
		}
	}
}
