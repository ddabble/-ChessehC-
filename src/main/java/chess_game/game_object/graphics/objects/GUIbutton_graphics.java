package chess_game.game_object.graphics.objects;

import chess_game.game_object.objects.GUIbutton;
import chess_game.window.Window;
import org.lwjgl.stb.STBImage;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL42.*;

public class GUIbutton_graphics
{
	private final GUIbutton THIS;

	private static int nextFreeIndex = 0;
	private final int index;

	private int textureObject;

	/**
	 * @param yPos y window coordinate of the center of the button
	 */
	public GUIbutton_graphics(GUIbutton button, float yPos, String pathToLabelImage, int program, int[] out_coords)
	{
		THIS = button;

		int[] width = new int[1];
		int[] height = new int[1];
		int[] components = new int[1];
		ByteBuffer imageData = STBImage.stbi_load(pathToLabelImage, width, height, components, 0);

		textureObject = glGenTextures();
		glBindTexture(GL_TEXTURE_2D, textureObject);

		glTexStorage2D(GL_TEXTURE_2D, 1, GL_RGBA8, width[0], height[0]);
		glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, width[0], height[0], GL_RGBA, GL_UNSIGNED_BYTE, imageData);
		STBImage.stbi_image_free(imageData);

		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);

		int x1 = -width[0] / 2;
		int x2 = width[0] / 2;
		float windowCenter_y = Window.getHeight() / 2.0f;
		int y1 = (int)(Window.windowToPixelCoord_y(yPos) - height[0] / 2.0f - windowCenter_y);
		int y2 = y1 + height[0];

		out_coords[0] = x1; out_coords[1] = y1; out_coords[2] = x2; out_coords[3] = y2;

		index = nextFreeIndex++;
		updateVertices(x1, y1, x2, y2);
	}

	/**
	 * Coordinates relative to window center.
	 */
	public void updateVertices(int x1, int y1, int x2, int y2)
	{
		float[] newVertexData = calculateVertexData(x1, y1, x2, y2);

		// 4 bytes * 4 coords + tex coords per vertex * 4 vertices
		glBufferSubData(GL_ARRAY_BUFFER, index * 4 * 4 * 4, newVertexData);
	}

	private static float[] calculateVertexData(int x1, int y1, int x2, int y2)
	{
		float windowCenter_x = Window.getWidth() / 2.0f;
		float windowCenter_y = Window.getHeight() / 2.0f;

		float x1_ = Window.pixelToWindowCoord_x(windowCenter_x + x1),
				x2_ = Window.pixelToWindowCoord_x(windowCenter_x + x2),
				y1_ = Window.pixelToWindowCoord_y(windowCenter_y + y1),
				y2_ = Window.pixelToWindowCoord_y(windowCenter_y + y2);
		return new float[]
				{
						// Non-standard pairing between vertex and texture coordinates due to
						// STBImage.stbi_load() returning pixels horizontally from top left to bottom right
						x1_, y1_, 0, 1,
						x2_, y1_, 1, 1,
						x2_, y2_, 1, 0,
						x1_, y2_, 0, 0
				};
	}

	public void graphicsUpdate(int background_uniformIndex, int vignette_textureObject)
	{
		glBindTexture(GL_TEXTURE_2D, textureObject);

		// Draw background
		glUniform1i(background_uniformIndex, 1);
		glDrawArrays(GL_TRIANGLE_FAN, index * 4, 4);

		// Draw text
		glUniform1i(background_uniformIndex, 0);
		glDrawArrays(GL_TRIANGLE_FAN, index * 4, 4);

		// Draw vignette
		glBindTexture(GL_TEXTURE_2D, vignette_textureObject);
		glUniform1i(background_uniformIndex, 0);
		glDrawArrays(GL_TRIANGLE_FAN, index * 4, 4);
	}
}
