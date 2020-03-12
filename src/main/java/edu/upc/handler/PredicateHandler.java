package edu.upc.handler;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import edu.upc.entities.Predicate;
import edu.upc.entities.PredicateArgument;
import edu.upc.entities.Token;
import edu.upc.utils.FilesUrl;

public class PredicateHandler {
	private LinkedHashMap<String, Predicate> predicates;
	private LinkedHashMap<String, Token> tokens;
	private ArrayList<String> csvTextList;

	public PredicateHandler(LinkedHashMap<String, Token> tokens) throws IOException {
		super();
		predicates = new LinkedHashMap<String, Predicate>();
		csvTextList = new ArrayList<String>();
		this.tokens = tokens;

	}

	public LinkedHashMap<String, Predicate> parsePredicates(JSONObject jsonObject) throws IOException {
		JSONArray predicatesArray = (JSONArray) jsonObject.get("predicates");
		if (predicatesArray != null) {
			Iterator<?> predicatesIterator = predicatesArray.iterator();
			while (predicatesIterator.hasNext()) {
				JSONObject newJsonObject = (JSONObject) predicatesIterator.next();
				Predicate predicate = createNewPredicate(newJsonObject);
				//if (tokens.get(predicate.getHead_token()).getWn() != null)
					if (tokens.get(predicate.getHead_token()).getPos().equals("verb")
							|| tokens.get(predicate.getHead_token()).getPos().equals("noun")) {
						parsePredicateArgumentActors(predicate, newJsonObject);
						parsePredicateArgumentObjects(predicate, newJsonObject);
						if (predicate.getA1PlusArguments().size() > 0)// it has object
							predicates.put(predicate.getHead_token(), predicate);
					}
			}
			sortByRole();
			//addToCsvFile(csvTextList);
			return predicates;
		} else {
			System.err.println("predicates Not Found. There is not activities");
			return null;
		}
	}

	private void sortByRole() {
		for (Entry<String, Predicate> predicate : predicates.entrySet()) {
			Map<String, String> rolesMap = new LinkedHashMap<String, String>();
			if (predicate.getValue().getA1PlusArguments().size() > 1) {
				for (Entry<String, PredicateArgument> argument : predicate.getValue().getA1PlusArguments().entrySet()) {
					rolesMap.put(argument.getKey(), argument.getValue().getRole());
				}

				Set<Map.Entry<String, String>> rolesSet = rolesMap.entrySet();
				List<Map.Entry<String, String>> rolesListEntry = new ArrayList<Map.Entry<String, String>>(rolesSet);
				Collections.sort(rolesListEntry, new Comparator<Map.Entry<String, String>>() {
					@Override
					public int compare(Entry<String, String> es1, Entry<String, String> es2) {
						return es1.getValue().compareTo(es2.getValue());
					}
				});
				LinkedHashMap<String, PredicateArgument> a1PlusArgumentsTemporal = new LinkedHashMap<String, PredicateArgument>();
				for (Map.Entry<String, String> map : rolesListEntry) {
					PredicateArgument predicateArgument = new PredicateArgument();
					predicateArgument = predicate.getValue().getA1PlusArguments().get(map.getKey());
					a1PlusArgumentsTemporal.put(map.getKey(), predicateArgument);
				}
				predicate.getValue().getA1PlusArguments().clear();
				predicate.getValue().setA1PlusArguments(a1PlusArgumentsTemporal);

			}
		}
	}

	private Predicate createNewPredicate(JSONObject jsonObject) {
		Predicate predicate = new Predicate();
		predicate.setId(jsonObject.get("id").toString());
		predicate.setHead_token(jsonObject.get("head_token").toString());
		predicate.setSense(jsonObject.get("sense").toString());
		predicate.setWords(jsonObject.get("words").toString());
		return predicate;
	}

	private void parsePredicateArgumentActors(Predicate predicate, JSONObject jsonObject) {
		JSONArray argumentArray = (JSONArray) jsonObject.get("arguments");
		if (argumentArray != null) {
			Iterator<?> argumentIterator = argumentArray.iterator();
			while (argumentIterator.hasNext()) {
				jsonObject = (JSONObject) argumentIterator.next();
				PredicateArgument argument = createNewArgument(jsonObject);
				if (argument.getRole().equals("A0")) {
					predicate.setArgumentA0(argument);
				}
			}
		}
	}

	private void parsePredicateArgumentObjects(Predicate predicate, JSONObject jsonObject) {
		JSONArray argumentArray = (JSONArray) jsonObject.get("arguments");
		if (argumentArray != null) {
			Iterator<?> argumentIterator = argumentArray.iterator();
			while (argumentIterator.hasNext()) {
				jsonObject = (JSONObject) argumentIterator.next();
				PredicateArgument argument = createNewArgument(jsonObject);
				if (isArgumentRoleGreaterThanA0(argument)) {
					if (!predicate.getHead_token().equals(argument.getHead_token())) // It's not refer to itself
					{
						predicate.getA1PlusArguments().put(argument.getHead_token(), argument);
						String csvText = predicate.getHead_token() + "\t"
								+ tokens.get(predicate.getHead_token()).getPos() + "\t" + predicate.getWords() + "\t"
								+ argument.getRole() + "\t" + tokens.get(argument.getHead_token()).getPos() + "\t"
								+ tokens.get(argument.getHead_token()).getLemma() + "\t" + argument.getHead_token()
								+ "\t" + argument.getWords();
						csvTextList.add(csvText);
					}
				}
			}
		}
	}

	private void addToCsvFile(ArrayList<String> textList) throws IOException {
		String filePath = FilesUrl.ACTIONS_CSV_FILE.toString();
		FileWriter csvWriter = new FileWriter(filePath, true);
		for (int i = 0; i < textList.size(); i++) {
			csvWriter.append(textList.get(i));
			csvWriter.append("\n");
		}
		csvWriter.flush();
		csvWriter.close();
	}

	private PredicateArgument createNewArgument(JSONObject jsonObject) {
		PredicateArgument newArgument = new PredicateArgument();
		newArgument.setFrom(jsonObject.get("from").toString());
		newArgument.setTo(jsonObject.get("to").toString());
		newArgument.setHead_token(jsonObject.get("head_token").toString());
		newArgument.setRole(jsonObject.get("role").toString());
		newArgument.setWords(jsonObject.get("words").toString());
		return newArgument;
	}

	private boolean isArgumentRoleGreaterThanA0(PredicateArgument argument) {
		String regex = "A[1-9][0-9]?";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(argument.getRole());
		if (matcher.matches())
			return true;
		return false;
	}

}
