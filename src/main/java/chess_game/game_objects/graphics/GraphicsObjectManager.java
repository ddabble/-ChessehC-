package chess_game.game_objects.graphics;

import chess_game.game_objects.ThirdPersonCamera;
import chess_game.game_objects.GameObject_interface;
import chess_game.util.RelativeDirection_enum;
import chess_game.window.Window;

import java.util.ArrayList;

import static org.lwjgl.opengl.GL11.*;

public class GraphicsObjectManager
{
	private ArrayList<GraphicsObject_interface> objects;

	public GraphicsObjectManager(ArrayList<GameObject_interface>... gameObjects)
	{
		objects = new ArrayList<>();

		for (ArrayList<GameObject_interface> gameObjectList : gameObjects)
		{
			for (GameObject_interface gameObject : gameObjectList)
			{
				GraphicsObject_interface graphics = gameObject.getGraphics();
				if (graphics != null)
					objects.add(graphics);
			}
		}

		glClearColor(0.3f, 0.3f, 0.3f, 1.0f);
		glEnable(GL_DEPTH_TEST);
		glDepthFunc(GL_LESS);
	}

	public void graphicsUpdate(ThirdPersonCamera camera, RelativeDirection_enum windowSide)
	{
		if (windowSide == RelativeDirection_enum.LEFT)
		{
			glViewport(0, 0, Window.getWidth() / 2, Window.getHeight());
		} else if (windowSide == RelativeDirection_enum.RIGHT)
		{
			glViewport(Window.getWidth() / 2, 0, Window.getWidth() / 2, Window.getHeight());
		}

		camera.graphicsUpdate();

		// TODO: temporary fix for ConcurrentModificationException (not the culprit)
		for (int i = 0; i < objects.size(); i++)
			objects.get(i).graphicsUpdate(camera);
	}

	public void removeGraphicsObject(GraphicsObject_interface object)
	{
		objects.remove(object);
	}
}
