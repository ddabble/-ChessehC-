package chess_game.game_object.objects;

import chess_game.event.EventHandler;
import chess_game.event.types.FramebufferSizeHook_interface;
import chess_game.game_object.graphics.objects.GUIbutton_graphics;
import chess_game.window.Window;

public abstract class GUIbutton implements FramebufferSizeHook_interface
{
	protected final GUIbutton_graphics graphics;

	// Coordinates relative to window center
	protected int x1;
	protected int y1;
	protected int x2;
	protected int y2;

	/**
	 * @param yPos y window coordinate of the center of the button
	 */
	protected GUIbutton(float yPos, String pathToLabelImage, int program)
	{
		int[] coords = new int[4];
		graphics = new GUIbutton_graphics(this, yPos, pathToLabelImage, program, coords);

		x1 = coords[0];
		y1 = coords[1];
		x2 = coords[2];
		y2 = coords[3];

		EventHandler.FramebufferSize.addHook(this);
	}

	public boolean isPositionOnButton(int cursor_Xpos, int cursor_Ypos)
	{
		cursor_Ypos = Window.getHeight() - cursor_Ypos; // because mouse position has its origin in the upper left corner of the window

		// Makes the cursor position relative to the window center, like the button coordinates
		cursor_Xpos -= Window.getWidth() / 2;
		cursor_Ypos -= Window.getHeight() / 2;

		return cursor_Xpos >= x1 && cursor_Xpos <= x2
				&& cursor_Ypos >= y1 && cursor_Ypos <= y2;
	}

	@Override
	public void framebufferSizeCallback(int lastWidth, int lastHeight, int newWidth, int newHeight)
	{
		graphics.updateVertices(x1, y1, x2, y2);
	}

	public static class NewGame extends GUIbutton
	{
		public NewGame(float yPos, int program)
		{
			super(yPos, "src/main/resources/Button_NewGame.png", program);
		}
	}

	public static class SaveGame extends GUIbutton
	{
		public SaveGame(float yPos, int program)
		{
			super(yPos, "src/main/resources/Button_SaveGame.png", program);
		}
	}

	public static class LoadGame extends GUIbutton
	{
		public LoadGame(float yPos, int program)
		{
			super(yPos, "src/main/resources/Button_LoadGame.png", program);
		}
	}

	public static class MainMenu extends GUIbutton
	{
		public MainMenu(float yPos, int program)
		{
			super(yPos, "src/main/resources/Button_MainMenu.png", program);
		}
	}

	public static class Resume extends GUIbutton
	{
		public Resume(float yPos, int program)
		{
			super(yPos, "src/main/resources/Button_Resume.png", program);
		}
	}
}
