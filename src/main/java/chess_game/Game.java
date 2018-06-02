package chess_game;

import chess_game.game_object.GameObjectManager;
import chess_game.util.Util;
import chess_game.window.Window;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.IOException;

import static org.lwjgl.glfw.GLFW.*;

// TODO: FIX ALL OF THIS MESSY SPAGHETTI CODE JEEZ

// TODO: Add swords,
//       a proper intro with the camera starting with a 2D-view from directly above the board, then panning down to the current FPS-view,
//       LAN instead of/in addition to split-screen,
//       mouse-controlled camera

public class Game
{
	private GameObjectManager gameObjectManager;

	void run()
	{
		Window.init();

		gameObjectManager = new GameObjectManager();

		Physics physicsThread = new Physics();
		physicsThread.start();

		loop();

		physicsThread.keepRunning = false;

		Window.terminate();
	}

	private void loop()
	{
		while (!glfwWindowShouldClose(Window.getGLFWwindow()))
		{
			gameObjectManager.frameUpdate();

			glfwSwapBuffers(Window.getGLFWwindow());

			glfwPollEvents();
		}
	}

	public boolean canUndo()
	{
		throw new NotImplementedException();
	}

	public void undo()
	{
		throw new NotImplementedException();
	}

	public boolean canRedo()
	{
		throw new NotImplementedException();
	}

	public void redo()
	{
		throw new NotImplementedException();
	}

	public void load(String fileName) throws IOException
	{
		throw new NotImplementedException();
	}

	public void save(String fileName) throws IOException
	{
		throw new NotImplementedException();
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
