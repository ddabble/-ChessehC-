package chess_game;

import chess_game.game_objects.GameObjectManager;
import chess_game.util.Util;
import chess_game.window.Window;
import org.lwjgl.Version;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

// TODO: FIX ALL OF THIS MESSY SPAGHETTI CODE JEEZ

public class Game
{
	private GameObjectManager gameObjectManager;

	void run()
	{
		System.out.println("Hello LWJGL " + Version.getVersion() + "!");

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
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

			gameObjectManager.frameUpdate();

			glfwSwapBuffers(Window.getGLFWwindow());

			glfwPollEvents();
		}
	}

	public class Physics extends Thread
	{
		public static final int PHYSICS_UPDATES_PER_SECOND = 50;
		public static final double PHYSICS_UPDATE_INTERVAL = 1.0 / PHYSICS_UPDATES_PER_SECOND;

		boolean keepRunning = true;

		double lastPhysicsTime;
		boolean hasSlept = false;

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
