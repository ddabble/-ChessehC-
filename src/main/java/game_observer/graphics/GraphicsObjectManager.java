package game_observer.graphics;

import chess_game.event.EventHandler;
import chess_game.event.types.FramebufferSizeHook_interface;
import chess_game.game_object.GameObjectManager;
import chess_game.game_object.ThirdPersonCamera;
import chess_game.game_object.objects.GUImenu;
import chess_game.util.graphics.GLSLshaders;
import chess_game.window.Window;

import java.util.ArrayList;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL32.*;

public class GraphicsObjectManager implements FramebufferSizeHook_interface
{
	private ArrayList<GraphicsObject_interface> objects;

	private ThirdPersonCamera camera1;
	private ThirdPersonCamera camera2;

	private int GFXprogram;
	private int GFXvertexArrayObject;
	private int GFXvertexBufferObject;
	private int GFXframebufferObject;
	private int GFXtextureObject;
	private int GFXdepthRenderbufferObject;
	private int textureSize_uniformIndex;
	private int blur_uniformIndex;

	private GUImenu gui_menu;

	public GraphicsObjectManager(ThirdPersonCamera camera1, ThirdPersonCamera camera2, ArrayList<GraphicsObject_interface> graphicsObjects)
	{
		this.camera1 = camera1;
		this.camera2 = camera2;

		objects = graphicsObjects;

		createGFXframebuffer();
		createGFXframebufferQuad();

		glClearColor(0.3f, 0.3f, 0.3f, 1.0f);
		glEnable(GL_DEPTH_TEST);
		glDepthFunc(GL_LESS);

		EventHandler.FramebufferSize.addHook(this);

		gui_menu = new GUImenu();
	}

	public void removeGraphicsObject(GraphicsObject_interface object)
	{
		objects.remove(object);
	}

	private void createGFXframebuffer()
	{
		GFXframebufferObject = glGenFramebuffers();
		glBindFramebuffer(GL_FRAMEBUFFER, GFXframebufferObject);

		GFXtextureObject = glGenTextures();
		glBindTexture(GL_TEXTURE_2D, GFXtextureObject);

		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, Window.getWidth(), Window.getHeight(), 0, GL_RGB, GL_UNSIGNED_BYTE, 0);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);

		GFXdepthRenderbufferObject = glGenRenderbuffers();
		glBindRenderbuffer(GL_RENDERBUFFER, GFXdepthRenderbufferObject);
		glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT, Window.getWidth(), Window.getHeight());
		glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, GFXdepthRenderbufferObject);

		glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GFXtextureObject, 0);

		int[] drawBuffers = new int[] { GL_COLOR_ATTACHMENT0 };
		glDrawBuffers(drawBuffers);

		if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE)
			System.err.println("Error during GFX-framebuffer creation.");
	}

	private void createGFXframebufferQuad()
	{
		GFXvertexArrayObject = glGenVertexArrays();
		glBindVertexArray(GFXvertexArrayObject);

		float[] vertexData = new float[]
				{
						-1.0f, -1.0f, 0, 0,
						1.0f, -1.0f, 1, 0,
						1.0f, 1.0f, 1, 1,
						-1.0f, 1.0f, 0, 1
				};

		GFXvertexBufferObject = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, GFXvertexBufferObject);
		glBufferData(GL_ARRAY_BUFFER, vertexData, GL_STATIC_DRAW);

		glVertexAttribPointer(0, 2, GL_FLOAT, false, 4 * 4, 0);
		glEnableVertexAttribArray(0);
		glVertexAttribPointer(1, 2, GL_FLOAT, false, 4 * 4, 2 * 4);
		glEnableVertexAttribArray(1);

		GFXprogram = GLSLshaders.loadShaders("src/main/java/game_observer/graphics/shaders/GFX.glsl");
		glUseProgram(GFXprogram);

		textureSize_uniformIndex = glGetUniformLocation(GFXprogram, "textureSize");
		glUniform2i(textureSize_uniformIndex, Window.getWidth(), Window.getHeight());

		blur_uniformIndex = glGetUniformLocation(GFXprogram, "blur");
		glUniform1ui(blur_uniformIndex, 1);
	}

	public void graphicsUpdate(GameObjectManager gameObjectManager)
	{
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		glBindFramebuffer(GL_FRAMEBUFFER, GFXframebufferObject);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

		// Render left side of window
		glViewport(0, 0, Window.getWidth() / 2, Window.getHeight());
		renderWithCamera(camera1, gameObjectManager);

		// Render right side of window
		glViewport(Window.getWidth() / 2, 0, Window.getWidth() / 2, Window.getHeight());
		renderWithCamera(camera2, gameObjectManager);

		renderGFXframebuffer();

		gui_menu.graphicsUpdate(null, gameObjectManager);
	}

	private void renderWithCamera(ThirdPersonCamera camera, GameObjectManager gameObjectManager)
	{
		camera.graphicsUpdate();

		// TODO: temporary fix for ConcurrentModificationException (not the culprit)
		for (int i = 0; i < objects.size(); i++)
			objects.get(i).graphicsUpdate(camera, gameObjectManager);
	}

	private boolean windowResized = false;
	private GameObjectManager.GameState_enum changedGameState = null;

	private void renderGFXframebuffer()
	{
		glBindFramebuffer(GL_FRAMEBUFFER, 0);
		glViewport(0, 0, Window.getWidth(), Window.getHeight());
		glUseProgram(GFXprogram);

		if (windowResized)
		{
			updateFramebufferSize();
			windowResized = false;
		}

		if (changedGameState != null)
		{
			switch (changedGameState)
			{
				case PLAYING:
					glUniform1ui(blur_uniformIndex, 0);
					break;

				case MAIN_MENU:
				case PAUSED:
					glUniform1ui(blur_uniformIndex, 1);
					break;
			}

			changedGameState = null;
		}

		glBindVertexArray(GFXvertexArrayObject);
		glBindTexture(GL_TEXTURE_2D, GFXtextureObject);

		glDrawArrays(GL_TRIANGLE_FAN, 0, 4);
	}

	public GUImenu.Action_enum getGuiAction(int button, int action, GameObjectManager gameObjectManager)
	{
		double[] x = new double[1];
		double[] y = new double[1];
		glfwGetCursorPos(Window.getGLFWwindow(), x, y);

		return gui_menu.onMouseButtonAction(button, action, (int)Math.floor(x[0]), (int)Math.floor(y[0]), gameObjectManager);
	}

	public void onChangedGameState(GameObjectManager gameObjectManager)
	{
		changedGameState = gameObjectManager.getGameState();
	}

	@Override
	public void framebufferSizeCallback(int lastWidth, int lastHeight, int newWidth, int newHeight)
	{
		windowResized = true;
	}

	private void updateFramebufferSize()
	{
		int newWidth = Window.getWidth();
		int newHeight = Window.getHeight();

		glBindFramebuffer(GL_FRAMEBUFFER, 0);
		glViewport(0, 0, newWidth, newHeight);

		glBindFramebuffer(GL_FRAMEBUFFER, GFXframebufferObject);
		glBindTexture(GL_TEXTURE_2D, GFXtextureObject);

		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, newWidth, newHeight, 0, GL_RGB, GL_UNSIGNED_BYTE, 0);

		glBindRenderbuffer(GL_RENDERBUFFER, GFXdepthRenderbufferObject);
		glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT, newWidth, newHeight);
		glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, GFXdepthRenderbufferObject);

		glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GFXtextureObject, 0);

		if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE)
			System.err.println("Error during GFX-framebuffer update.");

		glUniform2i(textureSize_uniformIndex, Window.getWidth(), Window.getHeight());
	}
}
