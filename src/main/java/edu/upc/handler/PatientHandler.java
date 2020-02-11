package edu.upc.handler;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexPattern;
import edu.upc.entities.Patient;
import edu.upc.entities.Predicate;
import edu.upc.entities.PredicateArgument;
import edu.upc.entities.Token;
import edu.upc.utils.Utils;

public class PatientHandler {
	private LinkedHashMap<String, Patient> patientsList;
	private List<Tree> trees;
	private LinkedHashMap<String, Token> tokens;
	private LinkedHashMap<String, Predicate> predicates;
	private ArrayList<String> posToMakeShortPatient;
	private ArrayList<String> words;
	private Boolean prepositionFound;

	public PatientHandler(List<Tree> trees, LinkedHashMap<String, Token> tokens,
			LinkedHashMap<String, Predicate> predicates) {
		super();
		this.trees = trees;
		this.tokens = tokens;
		this.predicates = predicates;
		this.patientsList = new LinkedHashMap<String, Patient>();

		posToMakeShortPatient = new ArrayList<String>();
		posToMakeShortPatient.add("noun");
		posToMakeShortPatient.add("adjective");
		posToMakeShortPatient.add("pronoun");
		posToMakeShortPatient.add("preposition");
		posToMakeShortPatient.add("number");
		fillPatientsFromPredicate();
		delimitePatientsNameLength();
		removePatientsWithNullText();
	}

	private void removePatientsWithNullText() {
		LinkedHashMap<String, Patient> patientsListTmp = new LinkedHashMap<String, Patient>();
		for (Entry<String, Patient> patient : patientsList.entrySet()) {
			if (patient.getValue().getShortText() != null) {
				patientsListTmp.put(patient.getKey(), patient.getValue());
			}
		}
		patientsList = new LinkedHashMap<String, Patient>();
		patientsList.putAll(patientsListTmp);
	}

	private void fillPatientsFromPredicate() {
		Integer count = 1;
		for (Entry<String, Predicate> predicate : predicates.entrySet()) {
			if (predicate.getValue().getA1PlusArguments().size() > 0) {
				Patient patient = null;
				String completeText = "";
				Integer begin = 0;
				Integer end = 0;
				boolean found = false;
				String patientId = null;
				for (Entry<String, PredicateArgument> argument : predicate.getValue().getA1PlusArguments().entrySet()) {
					completeText += argument.getValue().getWords().toLowerCase() + " ";
					if (!found) {
						patientId = argument.getValue().getHead_token();
						begin = tokens.get(argument.getValue().getFrom()).getBegin();
						end = tokens.get(argument.getValue().getTo()).getEnd();
						found = true;
					} else {
						if (end <= tokens.get(argument.getValue().getTo()).getEnd())
							end = tokens.get(argument.getValue().getTo()).getEnd();
					}
				}
				patient = new Patient(patientId, completeText, completeText, begin, end);
				patientsList.put(patientId, patient);
				count++;
			}
		}
	}

	private void delimitePatientsNameLength() {
		System.out.println("--> Apply patterns to get Patients Name");
		Integer count = 1;
		for (Entry<String, Patient> patient : patientsList.entrySet()) {
			words = new ArrayList<String>();
			prepositionFound = false;
			String text = "";
			String patientToken = patient.getKey();
			if (posToMakeShortPatient.contains(tokens.get(patientToken).getPos()))
				text = findSubjectInSubtreesAndMakePatientName(patientToken); // TODO send exactly tree
			else {
				patient.getValue().setShortText(null);
				patient.getValue().setBegin(null);
				patient.getValue().setEnd(null);
			}
			if (!text.trim().isEmpty()) {
				patient.getValue().setShortText(text.trim());
				patient.getValue().setBegin(tokens.get(words.get(0)).getBegin());
				patient.getValue().setEnd(tokens.get(words.get(words.size() - 1)).getEnd());
			} else {
				patient.getValue().setShortText(null);
				patient.getValue().setBegin(null);
				patient.getValue().setEnd(null);
			}
			count++;
		}
	}

	private String findSubjectInSubtreesAndMakePatientName(String patientToken) {
		String text = "";
		if (posToMakeShortPatient.contains(tokens.get(patientToken).getPos())) {
			Boolean found = false;
			ArrayList<String> list = new ArrayList<String>();
			posToMakeShortPatient.forEach(action -> list.add(action));
			String stringWithOrBarFromList = Utils.joinStringsListWithOrSeparator(list);
			TregexPattern pattern = TregexPattern.compile("/" + Utils.separator + stringWithOrBarFromList
					+ Utils.separator + "/=nounResult > /" + Utils.separator + patientToken + Utils.separator + "/");
			for (int i = 0; i < trees.size(); i++) {
				TregexMatcher matcher = pattern.matcher(trees.get(i));
				while (matcher.findNextMatchingNode()) {
					Tree tNounResult = matcher.getNode("nounResult");
					String patientToken2 = Utils.getTokenFromNode(tNounResult.label().value());
					if (tokens.get(patientToken2).getPos().equals("preposition")) {
						text += tokens.get(patientToken).getLemma() + " " + tokens.get(patientToken2).getLemma() + " ";
						prepositionFound = true;
						words.add(patientToken);
						words.add(patientToken2);
						text += findSubjectInSubtreesAndMakePatientName(patientToken2);
					} else {
						text += findSubjectInSubtreesAndMakePatientName(patientToken2);
					}
					i = trees.size();
					found = true;
				}
				if (found && !prepositionFound && !tokens.get(patientToken).getPos().equals("preposition")) {
					text += tokens.get(patientToken).getLemma() + " ";
					words.add(patientToken);
				}
			}
			if (!found && !tokens.get(patientToken).getPos().equals("preposition")) {
				text += tokens.get(patientToken).getLemma() + " ";
				words.add(patientToken);
			}
		}
		return text;
	}

	public LinkedHashMap<String, Patient> getPatientsList() {
		return patientsList;
	}

	public void setPatientsList(LinkedHashMap<String, Patient> patientsList) {
		this.patientsList = patientsList;
	}

}
