package game_observer.graphics.objects;

import chess_game.game_object.GameObjectManager;
import chess_game.game_object.ThirdPersonCamera;
import chess_game.game_object.objects.ChessBoard;
import chess_game.util.graphics.GLSLshaders;
import chess_game.util.graphics.ObjFile;
import game_observer.graphics.GraphicsObject_interface;
import org.joml.Matrix4f;
import org.lwjgl.stb.STBImage;

import java.io.IOException;
import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL42.*;

public class ChessBoard_graphics implements GraphicsObject_interface
{
	private final ChessBoard THIS;

	private int program;

	private int vertexArrayObject;
	private int vertexBufferObject;
	private int elementBufferObject;
	private int textureObject;

	private int[] vertexGroupIndices;

	private int model_uniformIndex;
	private int view_uniformIndex;
	private int projection_uniformIndex;

	private Matrix4f modelMatrix = new Matrix4f();

	private int useTexture_uniformIndex;

	public ChessBoard_graphics(ChessBoard chessBoard)
	{
		THIS = chessBoard;

		ObjFile objFile;
		try
		{
			objFile = ObjFile.Parser.parse("src/main/resources/Chessboard.obj");
		} catch (IOException e)
		{
			e.printStackTrace();
			throw new RuntimeException();
		}
		vertexGroupIndices = objFile.vertexGroupIndices;

		program = GLSLshaders.loadShaders("src/main/java/game_observer/graphics/shaders/chessBoard.glsl");
		glUseProgram(program);

		vertexArrayObject = glGenVertexArrays();
		glBindVertexArray(vertexArrayObject);

		vertexBufferObject = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, vertexBufferObject);
		glBufferData(GL_ARRAY_BUFFER, objFile.vertexData, GL_STATIC_DRAW);

		glVertexAttribPointer(0, 3, GL_FLOAT, false, 8 * 4, 0);
		glEnableVertexAttribArray(0);
		glVertexAttribPointer(1, 2, GL_FLOAT, false, 8 * 4, 3 * 4);
		glEnableVertexAttribArray(1);
		glVertexAttribPointer(2, 3, GL_FLOAT, false, 8 * 4, 5 * 4);
		glEnableVertexAttribArray(2);

		elementBufferObject = glGenBuffers();
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, elementBufferObject);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, objFile.vertexIndices, GL_STATIC_DRAW);

//		STBImage.stbi_set_flip_vertically_on_load(true);
		int[] width = new int[1];
		int[] height = new int[1];
		int[] components = new int[1];
		ByteBuffer imageData = STBImage.stbi_load("src/main/resources/Checkerboard.png", width, height, components, 1);

		textureObject = glGenTextures();
		glBindTexture(GL_TEXTURE_2D, textureObject);

		glTexStorage2D(GL_TEXTURE_2D, 1, GL_RGBA8, width[0], height[0]);
		glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, width[0], height[0], GL_RED, GL_UNSIGNED_BYTE, imageData);
		STBImage.stbi_image_free(imageData);

		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);

		model_uniformIndex = glGetUniformLocation(program, "model");
		view_uniformIndex = glGetUniformLocation(program, "view");
		projection_uniformIndex = glGetUniformLocation(program, "projection");

//		modelMatrix.translate(0, 0, 0);
//		viewMatrix.lookAt(
//				new Vector3f(0, 3, 3),
//				new Vector3f(0, 0, 0),
//				new Vector3f(0, 1, 0));
////		viewMatrix.translate(0, 0, -3)
////				.rotateAround((float)Math.PI / 4, new Vector3f(1, 0, 0));
//		projectionMatrix.perspective((float)Math.PI / 4, (float)Game.windowWidth / Game.INITIAL_WINDOW_HEIGHT, 0.1f, 100.0f);

		modelMatrix.scale(ChessBoard.SCALE, ChessBoard.SCALE, ChessBoard.SCALE);
		glUniformMatrix4fv(model_uniformIndex, false, modelMatrix.get(new float[16]));
//		glUniformMatrix4fv(view_uniformIndex, false, viewMatrix.get(new float[16]));
//		glUniformMatrix4fv(projection_uniformIndex, false, projectionMatrix.get(new float[16]));

		useTexture_uniformIndex = glGetUniformLocation(program, "useTexture");
	}

	@Override
	public void graphicsUpdate(ThirdPersonCamera camera, GameObjectManager gameObjectManager)
	{
		glUseProgram(program);
		glBindVertexArray(vertexArrayObject);
		glBindTexture(GL_TEXTURE_2D, textureObject);

//		Vector3f axis = new Vector3f(-1, 0, 0);
//		modelMatrix.rotateAround((float)angle, axis.normalize());
//		glUniformMatrix4fv(model_uniformIndex, false, modelMatrix.get(new float[16]));

		glUniformMatrix4fv(model_uniformIndex, false, modelMatrix.get(new float[16]));
		glUniformMatrix4fv(view_uniformIndex, false, camera.viewMatrix.get(new float[16]));
		glUniformMatrix4fv(projection_uniformIndex, false, camera.projectionMatrix.get(new float[16]));

//		glUniform1i(useTexture_uniformIndex, GL_TRUE);
		for (int i = 0; i < vertexGroupIndices.length - 1; i++)
		{
			// Is this needed?
			if (vertexGroupIndices[i] == 0)
				glUniform1i(useTexture_uniformIndex, GL_TRUE);
			else
				glUniform1i(useTexture_uniformIndex, GL_FALSE);

			glDrawElements(GL_TRIANGLES, vertexGroupIndices[i + 1] - vertexGroupIndices[i], GL_UNSIGNED_INT, vertexGroupIndices[i] * 4);
		}
	}
}
