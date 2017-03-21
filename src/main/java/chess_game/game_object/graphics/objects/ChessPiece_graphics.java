package chess_game.game_objects.graphics.objects;

import chess_game.game_objects.ChessPiece;
import chess_game.game_objects.ThirdPersonCamera;
import chess_game.game_objects.graphics.GraphicsObject_interface;
import chess_game.util.graphics.AnimatedVector;
import chess_game.util.graphics.ObjFile;
import chess_game.util.graphics.ShaderProgram;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.io.IOException;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class ChessPiece_graphics implements GraphicsObject_interface
{
	private final ChessPiece THIS;

	private ShaderProgram program;

	private int vao;
	private int vbo;
	private int ebo;

	private int numIndices;

	private int model_uniformIndex;
	private int view_uniformIndex;
	private int projection_uniformIndex;

	private Matrix4f modelMatrix = new Matrix4f();

	private int color_uniformIndex;
	private Vector3f pieceColor;
	private final Vector3f attackedColor = new Vector3f(1.0f, 0.0f, 0.0f);
	private Vector3f currentColor;
	private static final float COLOR_CHANGE_DURATION = 0.2f;
	private double colorChangeStartTime = -1;

	public AnimatedVector.Linear moveAnimation;

	public ChessPiece_graphics(ChessPiece chessPiece, Vector3f color)
	{
		THIS = chessPiece;
		pieceColor = new Vector3f(color);
		currentColor = color;
		moveAnimation = new AnimatedVector().new Linear();

		ObjFile objFile;
		try
		{
			objFile = ObjFile.Parser.parse("src/main/resources/Pawn.obj");
		} catch (IOException e)
		{
			e.printStackTrace();
			throw new RuntimeException();
		}
		numIndices = objFile.vertexIndices.length;

		program = new ShaderProgram(ShaderProgram.parseSourceFile("src/main/java/chess_game/shaders/chessPiece.vert"),
				ShaderProgram.parseSourceFile("src/main/java/chess_game/shaders/chessPiece.frag"), null);
		program.use();

		vao = glGenVertexArrays();
		glBindVertexArray(vao);

		vbo = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, vbo);
		glBufferData(GL_ARRAY_BUFFER, objFile.vertexData, GL_STATIC_DRAW);

		glVertexAttribPointer(0, 3, GL_FLOAT, false, 8 * 4, 0);
		glEnableVertexAttribArray(0);
		glVertexAttribPointer(1, 3, GL_FLOAT, false, 8 * 4, 5 * 4);
		glEnableVertexAttribArray(1);

		ebo = glGenBuffers();
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, objFile.vertexIndices, GL_STATIC_DRAW);

		model_uniformIndex = glGetUniformLocation(program.program, "model");
		view_uniformIndex = glGetUniformLocation(program.program, "view");
		projection_uniformIndex = glGetUniformLocation(program.program, "projection");

		updateModelMatrix();

		color_uniformIndex = glGetUniformLocation(program.program, "materialDiffuseColor");
		glUniform3f(color_uniformIndex, color.x, color.y, color.z);
	}

	public void startMoveAnimation(Vector3f newPosition)
	{
		moveAnimation = new AnimatedVector().new Linear(THIS.getPosition(), newPosition, 0.1f);
		moveAnimation.start();
	}

	public void onAttack()
	{
		colorChangeStartTime = glfwGetTime();
		currentColor = new Vector3f(attackedColor);
	}

	public void updateModelMatrix()
	{
		modelMatrix = new Matrix4f().translation(THIS.getPosition());
	}

	@Override
	public void graphicsUpdate(ThirdPersonCamera camera)
	{
		program.use();
		glBindVertexArray(vao);

		Vector3f nextPosition = moveAnimation.getNextFrame();
		if (nextPosition != null)
		{
			THIS.setPosition(nextPosition);
			updateModelMatrix();
		}

		if (colorChangeStartTime > 0)
			changeColor();

		glUniformMatrix4fv(model_uniformIndex, false, modelMatrix.get(new float[16]));
		glUniformMatrix4fv(view_uniformIndex, false, camera.viewMatrix.get(new float[16]));
		glUniformMatrix4fv(projection_uniformIndex, false, camera.projectionMatrix.get(new float[16]));
		glDrawElements(GL_TRIANGLES, numIndices, GL_UNSIGNED_INT, 0);
	}

	private void changeColor()
	{
		double currentTime = glfwGetTime();
		if (currentTime < colorChangeStartTime + COLOR_CHANGE_DURATION)
		{
			float progressPercentage = (float)(currentTime - colorChangeStartTime) / COLOR_CHANGE_DURATION;

			currentColor = attackedColor.smoothStep(pieceColor, progressPercentage, new Vector3f());
		} else
		{
			currentColor = new Vector3f(pieceColor);

			colorChangeStartTime = -1;
		}

		glUniform3f(color_uniformIndex, currentColor.x, currentColor.y, currentColor.z);
	}
}