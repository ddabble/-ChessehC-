package chess_game.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Util
{
	public static void sleep(long millis)
	{
		if (millis < 0)
			return;

		try
		{
			Thread.sleep(millis);
		} catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}

	public static float[] listToFloatArray(List<Float> list)
	{
		float[] array = new float[list.size()];

		for (int i = 0; i < list.size(); i++)
			array[i] = list.get(i);

		return array;
	}

	public static int[] listToIntArray(List<Integer> list)
	{
		int[] array = new int[list.size()];

		for (int i = 0; i < list.size(); i++)
			array[i] = list.get(i);

		return array;
	}

	public static ArrayList<String> readFile_list(File file) throws IOException
	{
		ArrayList<String> fileContents = new ArrayList<>();
		try (BufferedReader reader = new BufferedReader(new FileReader(file)))
		{
			String line;
			while ((line = reader.readLine()) != null)
				fileContents.add(line);
		} catch (FileNotFoundException e)
		{
			throw new FileNotFoundException("File not found: " + file.toString());
		} catch (IOException e)
		{
			throw new IOException("Unable to read file: " + file.toString());
		}

		return fileContents;
	}

	public static String readFile_string(File file) throws IOException
	{
		StringBuilder fileContents = new StringBuilder();
		try (BufferedReader reader = new BufferedReader(new FileReader(file)))
		{
			String line;
			while ((line = reader.readLine()) != null)
				fileContents.append(line).append("\n");
		} catch (FileNotFoundException e)
		{
			throw new FileNotFoundException("File not found: " + file.toString());
		} catch (IOException e)
		{
			throw new IOException("Unable to read file: " + file.toString());
		}

		return fileContents.toString();
	}

	public static String getLinkToLineInFile(File file, int lineNumber)
	{
		return file.getPath() + "(" + file.getName() + ":" + lineNumber + ")";
	}

	public static String getLinkToCharInFile(File file, String fileContents, int charIndex)
	{
		int lineNumber = 1 + countOccurences(fileContents.substring(0, charIndex), "\n");
		return getLinkToLineInFile(file, lineNumber);
	}

	public static int countOccurences(String src, String findString)
	{
		int count = 0;
		int findStringLength = findString.length();

		for (int lastIndex = src.indexOf(findString); lastIndex != -1; )
		{
			count++;
			lastIndex = src.indexOf(findString, lastIndex + findStringLength);
		}

		return count;
	}
}
