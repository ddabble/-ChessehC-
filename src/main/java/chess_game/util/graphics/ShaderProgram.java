package chess_game.util.graphics;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.Map;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;

public class ShaderProgram {

	protected static FloatBuffer buf16Pool;

	/**
	 * Makes the "default shader" (0) the active program. In GL 3.1+ core profile,
	 * you may run into glErrors if you try rendering with the default shader.
	 */
	public static void unbind() {
		glUseProgram(0);
	}

	public final int program;
	public final int vertex;
	public final int fragment;
	protected String log;

	public ShaderProgram(String vertexSource, String fragmentSource) {
		this(vertexSource, fragmentSource, null);
	}

	/**
	 * Creates a new shader from vertex and fragment source, and with the given
	 * map of <Integer, String> attrib locations
	 * @param vertexShader the vertex shader source string
	 * @param fragmentShader the fragment shader source string
	 * @param attributes a map of attrib locations for GLSL 120
	 * @throws RuntimeException if the program could not be compiled and linked
	 */
	public ShaderProgram(String vertexShader, String fragmentShader, Map<Integer, String> attributes) {
		//compile the String source
		vertex = compileShader(vertexShader, GL_VERTEX_SHADER);
		fragment = compileShader(fragmentShader, GL_FRAGMENT_SHADER);

		//create the program
		program = glCreateProgram();

		//attach the shaders
		glAttachShader(program, vertex);
		glAttachShader(program, fragment);

		//bind the attrib locations for GLSL 120
		if (attributes != null)
			for (Map.Entry<Integer, String> e : attributes.entrySet())
				glBindAttribLocation(program, e.getKey(), e.getValue());

		//link our program
		glLinkProgram(program);

		//grab our info log
		String infoLog = glGetProgramInfoLog(program, glGetProgrami(program, GL_INFO_LOG_LENGTH));

		//if some log exists, append it
		if (infoLog!=null && infoLog.trim().length()!=0)
			log += infoLog;

		//if the link failed, throw some sort of exception
		if (glGetProgrami(program, GL_LINK_STATUS) == GL_FALSE)
			throw new RuntimeException(
					"Failure in linking program. Error log:\n" + infoLog);

		//detach and delete the shaders which are no longer needed
		glDetachShader(program, vertex);
		glDetachShader(program, fragment);
		glDeleteShader(vertex);
		glDeleteShader(fragment);
	}

	/** Compile the shader source as the given type and return the shader object ID. */
	protected int compileShader(String source, int type) {
		//create a shader object
		int shader = glCreateShader(type);
		//pass the source string
		glShaderSource(shader, source);
		//compile the source
		glCompileShader(shader);

		//if info/warnings are found, append it to our shader log
		String infoLog = glGetShaderInfoLog(shader,
				glGetShaderi(shader, GL_INFO_LOG_LENGTH));
		if (infoLog!=null && infoLog.trim().length()!=0)
			log += getName(type) +": "+infoLog + "\n";

		//if the compiling was unsuccessful, throw an exception
		if (glGetShaderi(shader, GL_COMPILE_STATUS) == GL_FALSE)
			throw new RuntimeException("Failure in compiling " + getName(type)
					+ ". Error log:\n" + infoLog);

		return shader;
	}

	protected String getName(int shaderType) {
		if (shaderType == GL_VERTEX_SHADER)
			return "GL_VERTEX_SHADER";
		if (shaderType == GL_FRAGMENT_SHADER)
			return "GL_FRAGMENT_SHADER";
		else
			return "shader";
	}

	/**
	 * Make this shader the active program.
	 */
	public void use() {
		glUseProgram(program);
	}

	/**
	 * Destroy this shader program.
	 */
	public void destroy() {
		glDeleteProgram(program);
	}

	public static String parseSourceFile(String sourceFilePath)
	{
		File sourceFile = new File(sourceFilePath);
		StringBuilder buffer = new StringBuilder();

		try (BufferedReader reader = new BufferedReader(new FileReader(sourceFile)))
		{
			String line;
			while ((line = reader.readLine()) != null)
			{
				buffer.append(line).append("\n");
			}
		} catch (FileNotFoundException e)
		{
			System.err.println("File not found: " + sourceFile.toString());
			e.printStackTrace();
		} catch (IOException e)
		{
			System.err.println("Unable to read file: " + sourceFile.toString());
			e.printStackTrace();
		}

		return buffer.toString();
	}
}
