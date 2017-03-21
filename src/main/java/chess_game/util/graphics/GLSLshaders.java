package chess_game.util.graphics;

import chess_game.util.Util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL32.*;
import static org.lwjgl.opengl.GL40.*;

public class GLSLshaders
{
	public static int loadShaders(String filePath)
	{
		ArrayList<Shader> shaders = parseShaders(new File(filePath));

		int program = glCreateProgram();

		for (Shader shader : shaders)
		{
			int shaderObject = glCreateShader(shader.type);

			glShaderSource(shaderObject, shader.source);
			glCompileShader(shaderObject);

			int compiled = glGetShaderi(shaderObject, GL_COMPILE_STATUS);
			if (compiled == 0)
				throw new GLSLshaderException(glGetShaderInfoLog(shaderObject));

			glAttachShader(program, shaderObject);
		}

		glLinkProgram(program);

		int linked = glGetProgrami(program, GL_LINK_STATUS);
		if (linked == 0)
			throw new GLSLshaderException(glGetProgramInfoLog(program));

		return program;
	}

	private static ArrayList<Shader> parseShaders(File shaderFile)
	{
		ArrayList<Shader> shaders = new ArrayList<>(5); // 5 is the number of different shader types supported

		String source;
		try
		{
			source = Util.readFile_string(shaderFile);
		} catch (IOException e)
		{
			throw new RuntimeException(e);
		}

		int shaderTypeIndex = source.indexOf("/***");
		if (shaderTypeIndex == -1)
			throw new GLSLshaderParseException("Could not find any shader type declarations."
					+ "\n\tat " + Util.getLinkToCharInFile(shaderFile, source, 0));
		do
		{
			int endIndex = source.indexOf("*/", shaderTypeIndex + "/***".length());
			if (endIndex == -1)
				throw new GLSLshaderParseException("Missing comment end token */ for the shader type declaration."
						+ "\n\tat " + Util.getLinkToCharInFile(shaderFile, source, shaderTypeIndex));

			String shaderTypeDeclaration = source.substring(shaderTypeIndex + "/***".length(), endIndex);

			Shader shader = parseShaderTypeDeclaration(shaderTypeDeclaration);
			if (shader == null)
				throw new GLSLshaderParseException("Could not find a valid shader type in the shader type declaration."
						+ "\n\tat " + Util.getLinkToCharInFile(shaderFile, source, shaderTypeIndex));

			int nextShaderTypeIndex = source.indexOf("/***", shaderTypeIndex + "/***".length());
			if (nextShaderTypeIndex != -1)
				shader.source = source.substring(endIndex + "*/".length(), nextShaderTypeIndex);
			else
				shader.source = source.substring(endIndex + "*/".length());

			shaders.add(shader);

			shaderTypeIndex = nextShaderTypeIndex;
		} while (shaderTypeIndex != -1);

		return shaders;
	}

	private static Shader parseShaderTypeDeclaration(String shaderTypeDeclaration)
	{
		Shader shader = null;

		String[] tokens = shaderTypeDeclaration.split("[^a-zA-Z]");
loop:
		for (int i = 0; i < tokens.length; i++)
		{
			if (tokens[i].length() < 4)
				continue;

			switch (tokens[i].substring(0, 4).toLowerCase())
			{
				case "vert":
					shader = new Shader(GL_VERTEX_SHADER);
					break loop;

				case "frag":
					shader = new Shader(GL_FRAGMENT_SHADER);
					break loop;

				case "geom":
					shader = new Shader(GL_GEOMETRY_SHADER);
					break loop;

				case "tess":
					String doubleToken = tokens[i] + ((i + 1 < tokens.length) ? tokens[i + 1] : "");
					doubleToken = doubleToken.toLowerCase();

					if (doubleToken.contains("cont"))
					{
						shader = new Shader(GL_TESS_CONTROL_SHADER);
						break loop;
					} else if (doubleToken.contains("eval"))
					{
						shader = new Shader(GL_TESS_EVALUATION_SHADER);
						break loop;
					}
			}
		}

		return shader;
	}

	private static class Shader
	{
		int type;
		String source;

		Shader(int type)
		{
			this.type = type;
		}
	}

	private static class GLSLshaderException extends RuntimeException
	{
		public GLSLshaderException(String s)
		{
			super(s);
		}
	}

	private static class GLSLshaderParseException extends RuntimeException
	{
		public GLSLshaderParseException(String s)
		{
			super(s);
		}
	}
}
