package chess_game.window;

import chess_game.event.EventHandler;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Window
{
	public static final int INITIAL_WINDOW_WIDTH = 500;
	public static final int INITIAL_WINDOW_HEIGHT = 500;

	private static long window;

	private static int width = INITIAL_WINDOW_WIDTH;
	private static int height = INITIAL_WINDOW_HEIGHT;

	public static long getGLFWwindow()
	{
		return window;
	}

	public static int getWidth()
	{
		return width;
	}

	public static int getHeight()
	{
		return height;
	}

	public static float windowToPixelCoord_x(float windowCoord_x)
	{
		return (windowCoord_x + 1) / 2 * width;
	}

	public static float windowToPixelCoord_y(float windowCoord_y)
	{
		return (windowCoord_y + 1) / 2 * height;
	}

	public static float pixelToWindowCoord_x(float pixelCoord_x)
	{
		return (pixelCoord_x / width) * 2 - 1;
	}

	public static float pixelToWindowCoord_y(float pixelCoord_y)
	{
		return (pixelCoord_y / height) * 2 - 1;
	}

	public static void init()
	{
		GLFWErrorCallback.createPrint(System.err).set();

		if (!glfwInit())
			throw new IllegalStateException("Unable to initialize GLFW");

		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
		glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

		glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
		glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);

		// Anti-aliasing
//		glfwWindowHint(GLFW_SAMPLES, 4);

		window = glfwCreateWindow(INITIAL_WINDOW_WIDTH, INITIAL_WINDOW_HEIGHT, "!ChessehC!", NULL, NULL);
		if (window == NULL)
			throw new RuntimeException("Failed to create the GLFW window");

		// Get the thread stack and push a new frame
		try (MemoryStack stack = stackPush())
		{
			IntBuffer pWidth = stack.mallocInt(1); // int*
			IntBuffer pHeight = stack.mallocInt(1); // int*

			// Get the window size passed to glfwCreateWindow
			glfwGetWindowSize(window, pWidth, pHeight);

			// Get the resolution of the primary monitor
			GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

			// Center the window
			glfwSetWindowPos(
					window,
					(vidmode.width() - pWidth.get(0)) / 2,
					(vidmode.height() - pHeight.get(0)) / 2
			);
		} // the stack frame is popped automatically

		// Make the OpenGL context current
		glfwMakeContextCurrent(window);
		// Enable v-sync
		glfwSwapInterval(1);

		// Make the window visible
		glfwShowWindow(window);

		// This line is critical for LWJGL's interoperation with GLFW's
		// OpenGL context, or any context that is managed externally.
		// LWJGL detects the context that is current in the current thread,
		// creates the GLCapabilities instance and makes the OpenGL
		// bindings available for use.
		GL.createCapabilities();

		EventHandler.registerCallbacks(window);
	}

	public static void terminate()
	{
		glfwFreeCallbacks(window);
		glfwDestroyWindow(window);

		glfwTerminate();
		glfwSetErrorCallback(null).free();
	}

	public static void framebufferSizeCallback(int newWidth, int newHeight)
	{
		width = newWidth;
		height = newHeight;
	}
}
