package edu.upc.atdputils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

import edu.upc.freelingutils.FreelingUtils;

public class AtdpUtils {
	public static final String[] START_VERBS = new String[] { "start", "begin" };
	public static final String[] END_VERBS = new String[] { "end", "finish" };
	public static final String[] PROCESS_OBJECTS = new String[] { "process", "the process", "workflow", "instance",
			"case" };

	public static String getTokenFromNode(String node) {
		String word = node.replace(FreelingUtils.separator, " ");
		String[] words = word.split("\\s");
		return words[1];
	}

	public static String readFile(String fileName) throws IOException {
		String text = "";
		text = new String(Files.readAllBytes(Paths.get(fileName)));
		return text;
	}

	public static ArrayList<String> readPatternFile(String fileName) throws IOException {
		File file = new File(fileName);
		FileReader fileReader = new FileReader(file);
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		String line;
		ArrayList<String> list = new ArrayList<String>();
		while ((line = bufferedReader.readLine()) != null) {
			if (!line.startsWith("#") && !line.isEmpty())
				list.add(line);
		}
		bufferedReader.close();
		fileReader.close();
		return list;
	}

	public static ArrayList<String[]> readJudgeAnnotationFile(String fileName) throws IOException {
		File file = new File(fileName);
		FileReader fileReader = new FileReader(file);
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		String line;
		ArrayList<String[]> list = new ArrayList<String[]>();
		while ((line = bufferedReader.readLine()) != null) {
			if (!line.isEmpty()) {
				String[] columns = line.split("\\t");
				String[] columns2 = columns[1].split("\\s");
				if (columns2[0].equals("Condition") || columns2[0].equals("Action")) {
					String id = columns[0];
					String actionCondition = columns2[0];
					String begin = columns2[1];
					String end = columns2[2];
					String text = columns[2];
					String words[] = { id, actionCondition, begin, end, text };
					list.add(words);
				}
			}
		}
		bufferedReader.close();
		fileReader.close();
		return list;
	}

	public static String getFileNameWithoutExtension(File file) {
		String fileName = "";
		try {
			if (file != null && file.exists()) {
				String name = file.getName();
				fileName = name.replaceFirst("[.][^.]+$", "");
			}
		} catch (Exception e) {
			e.printStackTrace();
			fileName = "";
		}
		return fileName;

	}

	public static boolean isProcessObject(String object) {
		return Arrays.asList(PROCESS_OBJECTS).contains(object.toLowerCase());
	}

	public static boolean isStartVerb(String verb) {
		return Arrays.asList(START_VERBS).contains(verb.toLowerCase());
	}

	public static boolean isEndVerb(String verb) {
		return Arrays.asList(END_VERBS).contains(verb.toLowerCase());
	}
}
