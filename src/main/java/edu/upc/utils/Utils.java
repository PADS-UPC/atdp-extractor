package edu.upc.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import edu.upc.entities.Token;

public class Utils {
	public static final String separator = "¦";
	public static final String[] START_VERBS = new String[] { "start", "begin" };
	public static final String[] END_VERBS = new String[] { "end", "finish" };
	public static final String[] PROCESS_OBJECTS = new String[] { "process", "the process", "workflow", "instance",
			"case" };

	public static boolean isToApplyPattern() throws IOException {
		String applyPatterns = readPatternFile(FilesUrl.APPLY_PATTERNS_FILE.toString()).get(0);
		if (applyPatterns.equals("all"))
			return true;
		return false;
	}

	public static boolean isNounAction(String wn, String lemma) throws IOException {
		return isNounActionToAdd(wn, lemma, false);
	}

	public static boolean isNounActionToAdd(String wn, String lemma, Boolean toAdd) throws IOException {
		ArrayList<String[]> nounActivityList = null;
		if (toAdd)
			nounActivityList = readNounActivityFile(FilesUrl.FREEELING_NOUN_TO_ADD_CONFIG_FILE.toString());
		else
			nounActivityList = readNounActivityFile(FilesUrl.FREEELING_NOUN_CONFIG_FILE.toString());
		if (wn != null && lemma != null)
			for (int i = 0; i < nounActivityList.size(); i++) {
				if (wn.equals(nounActivityList.get(i)[0]) && nounActivityList.get(i)[1].contains(lemma)) {
					return true;
				}
			}
		return false;
	}

	public static String getTokenFromNode(String node) {
		String word = node.replace(separator, " ");
		String[] words = word.split("\\s");
		return words[1];
	}

	public static String removeObject(String text) {
		String textTmp = text.replaceAll(":t[1-9][0-9]?.*|:null" + separator, "");
		if (!text.equals(textTmp)) {
			Integer pos = text.lastIndexOf(separator);
			textTmp = text.substring(0, pos);
			pos = textTmp.lastIndexOf(separator);
			text = text.substring(0, pos + 1);
		}
		return text;
	}

	public static boolean isActivity(String node) {
		String node2 = Utils.removeObject(node);
		if (!node2.equals(node))
			return true;
		return false;
	}

	public static String joinStringsListWithOrSeparator(ArrayList<String> textsArrayList) {
		String str = "";
		for (int i = 0; i < textsArrayList.size(); i++) {
			str += textsArrayList.get(i);
			if (i != textsArrayList.size() - 1)
				str += "|";
		}
		return str;
	}

	public static String makeNode(Object token, LinkedHashMap<String, Token> tokens) {
		String word = separator + tokens.get(token).getId();
		word += separator + tokens.get(token).getPos();
		if (tokens.get(token).getLemma().equals("("))
			word += separator + "Fpa";
		else if (tokens.get(token).getLemma().equals(")"))
			word += separator + "Fpt";
		else
			word += separator + tokens.get(token).getLemma();
		word += separator + tokens.get(token).getCtag();
		word += separator;
		return word;
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

	public static ArrayList<String[]> readNounActivityFile(String fileName) throws IOException {
		File file = new File(fileName);
		FileReader fileReader = new FileReader(file);
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		String line;
		ArrayList<String[]> list = new ArrayList<String[]>();
		while ((line = bufferedReader.readLine()) != null) {
			if (!line.contains("#") && !line.isEmpty()) {
				String[] columns = line.split("\\s");
				String id = columns[0];
				String text = line.substring(id.length() + 1);
				String words[] = { id, text };
				list.add(words);
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
