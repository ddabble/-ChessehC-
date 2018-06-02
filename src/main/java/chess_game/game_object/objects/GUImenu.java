package chess_game.game_object.objects;

import chess_game.game_object.GameObjectManager;
import chess_game.game_object.ThirdPersonCamera;
import chess_game.game_object.graphics.GraphicsObject_interface;
import chess_game.util.graphics.GLSLshaders;
import org.joml.Vector3f;
import org.lwjgl.stb.STBImage;

import java.nio.ByteBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL42.*;

public class GUImenu implements GraphicsObject_interface
{
	private final int NUM_BUTTONS = 5;
	private GUIbutton.NewGame newGame;
	private GUIbutton.SaveGame saveGame;
	private GUIbutton.LoadGame loadGame;
	private GUIbutton.MainMenu mainMenu;
	private GUIbutton.Resume resume;

	private int program;
	private int vertexArrayObject;
	private int vertexBufferObject;

	private int background_uniformIndex;
	private int backgroundColor_uniformIndex;

	private int vignette_textureObject;

	public GUImenu()
	{
		initButtonGraphics();

		newGame = new GUIbutton.NewGame(0.15f, program);
		loadGame = new GUIbutton.LoadGame(-0.15f, program);

		resume = new GUIbutton.Resume(0.3f, program);
		saveGame = new GUIbutton.SaveGame(0.0f, program);
		mainMenu = new GUIbutton.MainMenu(-0.3f, program);
	}

	private void initButtonGraphics()
	{
		program = GLSLshaders.loadShaders("src/main/java/chess_game/shaders/button.glsl");
		glUseProgram(program);

		background_uniformIndex = glGetUniformLocation(program, "background");
		backgroundColor_uniformIndex = glGetUniformLocation(program, "backgroundColor");

		vertexArrayObject = glGenVertexArrays();
		glBindVertexArray(vertexArrayObject);

		vertexBufferObject = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, vertexBufferObject);
		// 4 bytes * 4 coords + tex coords per vertex * 4 vertices * number of buttons
		glBufferData(GL_ARRAY_BUFFER, 4 * 4 * 4 * NUM_BUTTONS, GL_STATIC_DRAW);

		glVertexAttribPointer(0, 2, GL_FLOAT, false, 4 * 4, 0);
		glEnableVertexAttribArray(0);
		glVertexAttribPointer(1, 2, GL_FLOAT, false, 4 * 4, 2 * 4);
		glEnableVertexAttribArray(1);

		int[] width = new int[1];
		int[] height = new int[1];
		int[] components = new int[1];
		ByteBuffer imageData = STBImage.stbi_load("src/main/resources/Button_vignette.png", width, height, components, 0);

		vignette_textureObject = glGenTextures();
		glBindTexture(GL_TEXTURE_2D, vignette_textureObject);

		glTexStorage2D(GL_TEXTURE_2D, 1, GL_RGBA8, width[0], height[0]);
		glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, width[0], height[0], GL_RGBA, GL_UNSIGNED_BYTE, imageData);
		STBImage.stbi_image_free(imageData);

		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
	}

	public Action_enum onMouseButtonAction(int button, int action, int cursorXpos, int cursorYpos, GameObjectManager gameObjectManager)
	{
		if (button != GLFW_MOUSE_BUTTON_LEFT || action != GLFW_RELEASE)
			return null;

		Action_enum gui_action = null;

		switch (gameObjectManager.getGameState())
		{
			case MAIN_MENU:
				if (newGame.isPositionOnButton(cursorXpos, cursorYpos))
					gui_action = Action_enum.NEW_GAME;

				if (loadGame.isPositionOnButton(cursorXpos, cursorYpos))
				{
					if (gui_action == null)
						gui_action = Action_enum.LOAD_GAME;
					else
						System.err.println(Action_enum.LOAD_GAME.name() + " was pressed at the same time as " + gui_action.name() + " was pressed!");
				}

				break;

			case PLAYING:
				break;

			case PAUSED:

				if (resume.isPositionOnButton(cursorXpos, cursorYpos))
					gui_action = Action_enum.RESUME;

				if (saveGame.isPositionOnButton(cursorXpos, cursorYpos))
				{
					if (gui_action == null)
						gui_action = Action_enum.SAVE_GAME;
					else
						System.err.println(Action_enum.SAVE_GAME.name() + " was pressed at the same time as " + gui_action.name() + " was pressed!");
				}

				if (mainMenu.isPositionOnButton(cursorXpos, cursorYpos))
				{
					if (gui_action == null)
						gui_action = Action_enum.MAIN_MENU;
					else
						System.err.println(Action_enum.MAIN_MENU.name() + " was pressed at the same time as " + gui_action.name() + " was pressed!");
				}

				break;
		}

		return gui_action;
	}

	@Override
	public void graphicsUpdate(ThirdPersonCamera camera, GameObjectManager gameObjectManager)
	{
//		glClear(GL_DEPTH_BUFFER_BIT);

		glDisable(GL_DEPTH_TEST);

		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

		glUseProgram(program);
		glBindVertexArray(vertexArrayObject);

		Vector3f color = getButtonBackgroundColor();
		glUniform3f(backgroundColor_uniformIndex, color.x, color.y, color.z);

		switch (gameObjectManager.getGameState())
		{
			case MAIN_MENU:
				newGame.graphics.graphicsUpdate(background_uniformIndex, vignette_textureObject);
				loadGame.graphics.graphicsUpdate(background_uniformIndex, vignette_textureObject);
				break;

			case PLAYING:
				break;

			case PAUSED:
				resume.graphics.graphicsUpdate(background_uniformIndex, vignette_textureObject);
				saveGame.graphics.graphicsUpdate(background_uniformIndex, vignette_textureObject);
				mainMenu.graphics.graphicsUpdate(background_uniformIndex, vignette_textureObject);
				break;
		}

		glDisable(GL_BLEND);

		glEnable(GL_DEPTH_TEST);
	}

	private Vector3f getButtonBackgroundColor()
	{
		final float L = 0.5f;
		final float a = 0.5f;

		final float P = 100;
		final float k = 2 * (float)Math.PI / P;

		final float F = P / 3;

		double currentTime = glfwGetTime() * 5;

		double r = L + a * Math.sin(k * (currentTime - 0 * F));
		double g = L + a * Math.sin(k * (currentTime - 1 * F));
		double b = L + a * Math.sin(k * (currentTime - 2 * F));

		return new Vector3f((float)r, (float)g, (float)b);
	}

	public enum Action_enum
	{
		NEW_GAME, SAVE_GAME, LOAD_GAME, MAIN_MENU, RESUME
	}
}
