package chess_game.event;

import chess_game.event.types.FramebufferSizeHook_interface;
import chess_game.window.Window;
import org.lwjgl.glfw.GLFWFramebufferSizeCallbackI;
import org.lwjgl.glfw.GLFWKeyCallbackI;

import java.util.ArrayList;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class EventHandler
{
	private EventHandler() {}

	public static void registerCallbacks(long glfwWindow)
	{
		glfwSetKeyCallback(glfwWindow, Key.instance);
		glfwSetFramebufferSizeCallback(glfwWindow, FramebufferSize.instance);
	}

	public static class Key extends EventHandler implements GLFWKeyCallbackI
	{
		public static final Key instance = new Key();

		public static boolean[] keys = new boolean[1024];

		private Key() {}

		@Override
		public void invoke(long window, int key, int scancode, int action, int mods)
		{
			if (action == GLFW_PRESS)
				keys[key] = true;
			else if (action == GLFW_RELEASE)
				keys[key] = false;
		}
	}

	public static class FramebufferSize extends EventHandler implements GLFWFramebufferSizeCallbackI
	{
		public static final FramebufferSize instance = new FramebufferSize();

		private static ArrayList<FramebufferSizeHook_interface> hooks = new ArrayList<>();

		private FramebufferSize() {}

		@Override
		public void invoke(long window, int newWidth, int newHeight)
		{
			glViewport(0, 0, newWidth, newHeight);

			for (FramebufferSizeHook_interface hook : hooks)
				hook.framebufferSizeCallback(Window.getWidth(), Window.getHeight(), newWidth, newHeight);

			Window.framebufferSizeCallback(newWidth, newHeight);
		}

		public static void addHook(FramebufferSizeHook_interface hook)
		{
			hooks.add(hook);
		}

		public static void removeHook(FramebufferSizeHook_interface hook)
		{
			hooks.remove(hook);
		}
	}
}
