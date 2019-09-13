package chess_game.util.graphics;

import chess_game.util.Util;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class ObjFile
{
	public float[] vertexData;
	public int[] vertexIndices;
	/**
	 * Also contains the last vertex index.
	 */
	public int[] vertexGroupIndices;

	private ObjFile(ArrayList<Parser.FullVertex> fullVertices, ArrayList<Integer> fullVertexIndices, ArrayList<Integer> vertexGroupIndices)
	{
		this.vertexData = new float[fullVertices.size() * Parser.FullVertex.VERTEX_DATA_LENGTH];
		this.vertexIndices = new int[fullVertexIndices.size()];
		this.vertexGroupIndices = new int[vertexGroupIndices.size()];

		for (int o = 0; o < fullVertices.size(); o++)
		{
			float[] vertexData = fullVertices.get(o).vertexData;
			for (int i = 0; i < vertexData.length; i++)
				this.vertexData[o * Parser.FullVertex.VERTEX_DATA_LENGTH + i] = vertexData[i];
		}

		for (int i = 0; i < fullVertexIndices.size(); i++)
			this.vertexIndices[i] = fullVertexIndices.get(i);

		for (int i = 0; i < vertexGroupIndices.size(); i++)
			this.vertexGroupIndices[i] = vertexGroupIndices.get(i);
	}

	public static class Parser
	{
		public static ObjFile parse(String pathToObjFile) throws IOException
		{
			/*
			...
			f 2/2/1 1/1/1 4/4/1
			o SketchUp.001_ID11             <- End of the last vertex group and start of the next one [index 2]

			v -0.500000 0.050000 -0.500000  <- Vertex                                       [index 5]
			v -0.500000 0.000000 0.500000
			v -0.500000 0.050000 0.500000
			v -0.500000 0.000000 -0.500000
			vt -1.6404 0.1640               <- Texture coordinate                           [index 5]
			vt 1.6404 0.0000
			vt 1.6404 0.1640
			vt -1.6404 0.0000
			vn -1.0000 0.0000 0.0000        <- Vertex (face) normal                         [index 2]
			s off

			f 5/5/2 6/6/2 7/7/2             <- Face, composited of three references to full vertices
			f 6/6/2 5/5/2 8/8/2                This is a reference to a full vertex:        5/5/2
			                                   and this is the full vertex it references:   -0.500000 0.050000 -0.500000 / -1.6404 0.1640 / -1.0000 0.0000 0.0000
			                                   Full vertex indices:
			                                     [index 7]   [index 8]   [index 9]
			                                   f   5/5/2       6/6/2       7/7/2
			                                   f   6/6/2       5/5/2       8/8/2
			                                     [index 10]  [index 11]  [index 12]

			o SketchUp_ID3                  <- End of the last vertex group and start of the next one [index 3]
			v 0.500000 0.000000 -0.500000
			...
			 */

			ArrayList<String> fileContents = Util.readFile_list(new File(pathToObjFile));

			ArrayList<Vector3f> vertices = new ArrayList<>();
			ArrayList<Vector2f> textureCoords = new ArrayList<>();
			ArrayList<Vector3f> vertexNormals = new ArrayList<>();

			ArrayList<VertexReference> fullVertexReferences = new ArrayList<>();
			ArrayList<Integer> vertexGroupIndices = new ArrayList<>();

			for (String line : fileContents)
			{
				String[] words = line.split(" ");
				switch (words[0])
				{
					case "v":
						vertices.add(new Vector3f(
								Float.parseFloat(words[1]),
								Float.parseFloat(words[2]),
								Float.parseFloat(words[3])));
						break;

					case "vt":
						textureCoords.add(new Vector2f(
								Float.parseFloat(words[1]),
								Float.parseFloat(words[2])));
						break;

					case "vn":
						vertexNormals.add(new Vector3f(
								Float.parseFloat(words[1]),
								Float.parseFloat(words[2]),
								Float.parseFloat(words[3])));
						break;

					case "f":
						for (int i = 1; i < words.length; i++)
							fullVertexReferences.add(new VertexReference(words[i].split("/")));

						break;

					case "o":
						vertexGroupIndices.add(fullVertexReferences.size());
						break;

					case "s":
					default:
						break;
				}
			}
			vertexGroupIndices.add(fullVertexReferences.size());

			ArrayList<FullVertex> fullVertices = new ArrayList<>();
			ArrayList<Integer> fullVertexIndices = new ArrayList<>();
			int fullVertexIndex = 0;

			for (VertexReference fullVertexReference : fullVertexReferences)
			{
				FullVertex vertex = new FullVertex(fullVertexReference, vertices, textureCoords, vertexNormals);

				int similarVertexIndex = fullVertices.indexOf(vertex);
				if (similarVertexIndex == -1)
				{
					fullVertices.add(vertex);

					fullVertexIndices.add(fullVertexIndex);
					fullVertexIndex++;
				} else
					fullVertexIndices.add(similarVertexIndex);
			}

			return new ObjFile(fullVertices, fullVertexIndices, vertexGroupIndices);
		}

		private static class VertexReference
		{
			private int[] indices;

			VertexReference(String[] indices)
			{
				int vertexIndex = Integer.parseInt(indices[0]) - 1;
				int vertexNormalIndex = Integer.parseInt(indices[2]) - 1;

				if (indices[1].length() != 0)
				{
					int textureCoordIndex = Integer.parseInt(indices[1]) - 1;
					this.indices = new int[] { vertexIndex, textureCoordIndex, vertexNormalIndex };
				} else
					this.indices = new int[] { vertexIndex, vertexNormalIndex };
			}

			int getVertexIndex()
			{
				return indices[0];
			}

			int getTextureCoordIndex()
			{
				return indices[1];
			}

			int getVertexNormalIndex()
			{
				return indices[indices.length - 1];
			}

			boolean containsTextureCoord()
			{
				return indices.length == 3;
			}
		}

		private static class FullVertex
		{
			static final int VERTEX_DATA_LENGTH = 8;

			final float[] vertexData = new float[VERTEX_DATA_LENGTH];
			private boolean containsTextureCoords;

			FullVertex(VertexReference vertexReference, ArrayList<Vector3f> vertices, ArrayList<Vector2f> textureCoords, ArrayList<Vector3f> vertexNormals)
			{
				Vector3f vertex = vertices.get(vertexReference.getVertexIndex());
				Vector3f vertexNormal = vertexNormals.get(vertexReference.getVertexNormalIndex());

				vertexData[0] = vertex.x;
				vertexData[1] = vertex.y;
				vertexData[2] = vertex.z;

				if (containsTextureCoords = vertexReference.containsTextureCoord())
				{
					Vector2f textureCoord = textureCoords.get(vertexReference.getTextureCoordIndex());

					vertexData[3] = textureCoord.x;
					vertexData[4] = textureCoord.y;
				}

				vertexData[5] = vertexNormal.x;
				vertexData[6] = vertexNormal.y;
				vertexData[7] = vertexNormal.z;
			}

			@Override
			public boolean equals(Object other)
			{
				if (other.getClass() != FullVertex.class)
					return false;

				// This implies that they're either different vertices, or that they're the same vertex but do not have the same normal.
				if (this.containsTextureCoords != ((FullVertex)other).containsTextureCoords)
					return false;

				int i = 0;
				for (; i < vertexData.length - 3; i++)
				{
					if (this.vertexData[i] != ((FullVertex)other).vertexData[i])
						return false;
				}

				for (; i < vertexData.length; i++)
				{
					if (Math.abs(this.vertexData[i] - ((FullVertex)other).vertexData[i]) >= 0.01f)
						return false;
				}

				return true;
			}
		}
	}
}

