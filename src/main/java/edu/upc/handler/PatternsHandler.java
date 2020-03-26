package edu.upc.handler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexPattern;
import edu.upc.entities.Action;
import edu.upc.entities.Activity;
import edu.upc.entities.Token;
import edu.upc.utils.ActionType;
import edu.upc.utils.FilesUrl;
import edu.upc.utils.Utils;

public class PatternsHandler {
	private LinkedHashMap<String, Activity> activitiesList;
	private LinkedHashMap<String, Token> tokens;
	private List<Tree> trees;
	private String beginToken = "";
	private String endToken = "";
	private ArrayList<String> subtreeLimits = new ArrayList<String>() {
		private static final long serialVersionUID = 1L;
		{
			add(".");
			add(",");
			add("or");
			add("either");
		}
	};

	public PatternsHandler(LinkedHashMap<String, Token> tokens, List<Tree> trees,
			LinkedHashMap<String, Activity> activitiesList) throws IOException {
		super();
		this.tokens = tokens;
		this.trees = trees;
		this.activitiesList = activitiesList;
		TreesHandler.refreshTree(trees, activitiesList, tokens);
		applyAllPatterns();
	}

	private void applyAllPatterns() throws IOException {

		ArrayList<String> patterns = new ArrayList<String>();

		patterns = Utils.readPatternFile(FilesUrl.CONDITION_PATTERNS_FILE.toString());
		extractConditions(patterns, ActionType.CONDITION);

		patterns = Utils.readPatternFile(FilesUrl.CONDITION_SIMPLE_PATTERNS_FILE.toString());
		extractConditionsSimple(patterns, ActionType.CONDITION);

		patterns = Utils.readPatternFile(FilesUrl.REMOVE_UNDER_CONDITION_PATTERNS_FILE.toString());
		if (Utils.isToApplyPattern())
			patternExecutor(patterns, null);

		patterns = Utils.readPatternFile(FilesUrl.EVENT_PATTERNS_FILE.toString());
		if (Utils.isToApplyPattern())
			patternExecutor(patterns, ActionType.EVENT);

		patterns = Utils.readPatternFile(FilesUrl.WEAK_VERBS_PATTERNS_FILE.toString());
		removeWeakVerbs(patterns);

		patterns = Utils.readPatternFile(FilesUrl.ACTIVITY_PATTERNS_FILE.toString());
		if (Utils.isToApplyPattern())
			patternExecutor(patterns, ActionType.ACTIVITY);

		patterns = Utils.readPatternFile(FilesUrl.REMOVE_ACTIVITY_FILE.toString());
		if (Utils.isToApplyPattern())
			patternExecutor(patterns, ActionType.ACTIVITY);

		changeAllActionsToActivity();

		patterns = Utils.readPatternFile(FilesUrl.SEQUENCE_PATTERNS_FILE.toString());
		sequencePatterExecutor(patterns);

		patterns = Utils.readPatternFile(FilesUrl.CONFLICT_PATTERNS_FILE.toString());
		conflictPatterExecutor(patterns);

	}

	private void patternExecutor(ArrayList<String> patternStrList, ActionType role) throws IOException {
		if (role != null)
			System.out.println("--> Identify " + role.toString().toUpperCase() + " and update ActivitiesList");
		for (String patternStr : patternStrList) {
			TregexPattern pattern = TregexPattern.compile(patternStr);
			for (int i = 0; i < trees.size(); i++) {
				TregexMatcher matcher = pattern.matcher(trees.get(i));
				while (matcher.findNextMatchingNode()) {
					Tree tResult = matcher.getNode("result");
					Tree tToRemove = matcher.getNode("toRemove");
					Tree tCondition = matcher.getNode("condition");
					if (tResult != null) {
						String tokenResult = Utils.getTokenFromNode(tResult.label().value());
						if (activitiesList.containsKey(tokenResult)
								&& activitiesList.get(tokenResult).getRole() == ActionType.ACTION) {
							if (role != null)
								activitiesList.get(tokenResult).setRole(role);
						}
					}
					if (tToRemove != null) {
						String tokenTremove = Utils.getTokenFromNode(tToRemove.label().value());
						if (activitiesList.containsKey(tokenTremove)) {
							activitiesList.remove(tokenTremove);
						}
					}
					if (tResult != null && tCondition != null) {
						String tokenResult = Utils.getTokenFromNode(tResult.label().value());
						String conditionToken = Utils.getTokenFromNode(tCondition.label().value());
						activitiesList.get(tokenResult).setPattern(tokens.get(conditionToken).getLemma());
					}
				}
			}
			TreesHandler.refreshTree(trees, activitiesList, tokens);
		}

	}

	private void removeWeakVerbs(ArrayList<String> verbsList) throws IOException {
		System.out.println("--> Removing VERBS that are not actions and update ActivitiesList");
		ArrayList<String> patternsList = new ArrayList<String>();
		for (String verb : verbsList) {
			patternsList.add("/" + Utils.separator + verb + Utils.separator + ".*" + Utils.separator + "ACTION:.*"
					+ Utils.separator + "/=toRemove");
		}
		patternExecutor(patternsList, null);
	}

	private void changeAllActionsToActivity() throws IOException {
		for (Entry<String, Activity> activity : activitiesList.entrySet()) {
			if (activity.getValue().getRole().equals(ActionType.ACTION)) {
				activitiesList.get(activity.getKey()).setRole(ActionType.ACTIVITY);
			}
		}
		TreesHandler.refreshTree(trees, activitiesList, tokens);
	}

	private void extractConditionsSimple(ArrayList<String> patternStrList, ActionType role) throws IOException {
		System.out.println("--> Identify simple " + role.toString().toUpperCase() + " and update ActivitiesList");
		String resultToken = "";
		for (String patternStr : patternStrList) {
			TregexPattern pattern = TregexPattern.compile(patternStr);
			for (int i = 0; i < trees.size(); i++) {
				TregexMatcher matcher = pattern.matcher(trees.get(i));
				while (matcher.findNextMatchingNode()) {
					Tree tResult = matcher.getNode("result");
					if (tResult == null)
						return;
					resultToken = Utils.getTokenFromNode(tResult.label().value());
					Integer begin = tokens.get(resultToken).getBegin();
					Integer end = tokens.get(resultToken).getEnd();
					String text = tokens.get(resultToken).getLemma();
					Action action = new Action(resultToken, text, begin, end);
					Activity activity = new Activity(resultToken, null, action, null);
					activity.setRole(role);
					activitiesList.put(resultToken, activity);
				}
				TreesHandler.refreshTree(trees, activitiesList, tokens);
			}
		}
	}

	private void extractConditions(ArrayList<String> patternStrList, ActionType role) throws IOException {
		System.out.println("--> Identify " + role.toString().toUpperCase() + " and update ActivitiesList");
		String resultToken = "";
		for (String patternStr : patternStrList) {
			TregexPattern pattern = TregexPattern.compile(patternStr);
			for (int i = 0; i < trees.size(); i++) {
				TregexMatcher matcher = pattern.matcher(trees.get(i));
				while (matcher.findNextMatchingNode()) {
					Tree tResult = matcher.getNode("result");
					Tree tCondition = matcher.getNode("condition");
					if (tResult == null)
						return;
					resultToken = Utils.getTokenFromNode(tResult.label().value());
					ArrayList<String> range = getActionInformation(resultToken, tResult,
							tokens.get(Utils.getTokenFromNode(tCondition.label().value())).getLemma());
					Integer begin = Integer.parseInt(range.get(0));
					Integer end = Integer.parseInt(range.get(1));
					String text = range.get(2);

					if (activitiesList.containsKey(resultToken)) {
						activitiesList.get(resultToken).setRole(role);
						activitiesList.get(resultToken).getAction().setBegin(begin);
						activitiesList.get(resultToken).getAction().setEnd(end);
						activitiesList.get(resultToken).getAction().setWord(text);
						activitiesList.get(resultToken).setPatient(null);
					} else {
						Action action = new Action(resultToken, text, begin, end);
						Activity activity = new Activity(resultToken, null, action, null);
						activity.setRole(role);
						activitiesList.put(resultToken, activity);
						// find Agent
						String patternStr2 = "/" + Utils.separator + "*:.*" + Utils.separator + "/=result >> /"
								+ Utils.separator + resultToken + Utils.separator + "/";
						TregexPattern pattern2 = TregexPattern.compile(patternStr2);
						TregexMatcher matcher2 = pattern2.matcher(trees.get(i));
						while (matcher2.findNextMatchingNode()) {
							Tree tResult2 = matcher2.getNode("result");
							String resultToken2 = Utils.getTokenFromNode(tResult2.label().value());
							if (activitiesList.containsKey(resultToken2)) {
								activitiesList.get(resultToken).getAction()
										.setMainVerbLemma(tokens.get(resultToken2).getLemma());
								if (activitiesList.get(resultToken2).getAgent() != null)
									activitiesList.get(resultToken)
											.setAgent(activitiesList.get(resultToken2).getAgent());
							}
							if (Utils.isToApplyPattern())
								activitiesList.remove(resultToken2);
						}
					}
					if (tCondition != null && activitiesList.containsKey(resultToken)) {
						String conditionToken = Utils.getTokenFromNode(tCondition.label().value());
						activitiesList.get(resultToken).setPattern(tokens.get(conditionToken).getLemma());
					}
					TreesHandler.refreshTree(trees, activitiesList, tokens);
				}
			}
		}
	}

	private ArrayList<String> getActionInformation(String tokenToStart, Tree tree, String conditionLemma) {
		String text = "";
		beginToken = tokenToStart;
		endToken = tokenToStart;
		getFirstOrLastNode(tree, conditionLemma);
		String[] begin = beginToken.split("\\.");
		String[] end = endToken.split("\\.");
		for (int i = Integer.parseInt(begin[1]); i <= Integer.parseInt(end[1]); i++) {
			text += tokens.get(begin[0] + "." + i).getForm() + " ";
		}
		ArrayList<String> results = new ArrayList<String>();
		results.add(tokens.get(beginToken).getBegin().toString());
		results.add(tokens.get(endToken).getEnd().toString());
		results.add(text);
		return results;
	}

	private void getFirstOrLastNode(Tree treeResult, String conditionLemma) {
		String resultToken = Utils.getTokenFromNode(treeResult.value());
		Integer value = Integer.parseInt(resultToken.split("\\.")[1]);
		if (value < Integer.parseInt(beginToken.split("\\.")[1]))
			beginToken = resultToken;
		if (value > Integer.parseInt(endToken.split("\\.")[1]))
			endToken = resultToken;
		Iterator<?> treeIterator = treeResult.getChildrenAsList().iterator();
		while (treeIterator.hasNext()) {
			Tree tResult = (Tree) treeIterator.next();
			if (!conditionLemma.equals("if")) {
				if (subtreeLimits.contains(tokens.get(Utils.getTokenFromNode(tResult.value())).getLemma()))
					return;
			}
			getFirstOrLastNode(tResult, conditionLemma);
		}
	}

	private void sequencePatterExecutor(ArrayList<String> patternStrList) throws IOException {
		System.out.println("--> Identify SEQUENCE and update ActivitiesList");
		String destinationToken = "";
		String originToken = "";
		for (String patternStr : patternStrList) {
			TregexPattern pattern = TregexPattern.compile(patternStr);
			for (int i = 0; i < trees.size(); i++) {
				TregexMatcher matcher = pattern.matcher(trees.get(i));
				while (matcher.findNextMatchingNode()) {
					Tree tDestination = matcher.getNode("destination");
					Tree tOrigin = matcher.getNode("origin");
					if (tDestination == null || tOrigin == null)
						return;
					originToken = Utils.getTokenFromNode(tOrigin.label().value());
					destinationToken = Utils.getTokenFromNode(tDestination.label().value());
					destinationToken = findDestination(destinationToken);
					// Add only if it doesn't has the same origin and the same destination
					if (!activitiesList.get(originToken).getSequenceDestinations().contains(destinationToken)
							&& !activitiesList.get(destinationToken).getSequenceOrigins().contains(originToken)) {
						activitiesList.get(originToken).getSequenceDestinations().add(destinationToken);
						activitiesList.get(destinationToken).getSequenceOrigins().add(originToken);
					} else {
						System.out.println("found");
					}
				}
			}
		}
	}

	private void conflictPatterExecutor(ArrayList<String> patternStrList) throws IOException {
		System.out.println("--> Identify CONFLICT and update ActivitiesList");
		String originToken = "";
		String destinationToken = "";
		for (String patternStr : patternStrList) {
			TregexPattern pattern = TregexPattern.compile(patternStr);
			for (int i = 0; i < trees.size(); i++) {
				TregexMatcher matcher = pattern.matcher(trees.get(i));
				while (matcher.findNextMatchingNode()) {
					Tree tConflictOrigin = matcher.getNode("origin");
					Tree tConflictDestination = matcher.getNode("destination");
					if (tConflictDestination == null || tConflictOrigin == null)
						return;
					originToken = Utils.getTokenFromNode(tConflictOrigin.label().value());
					destinationToken = Utils.getTokenFromNode(tConflictDestination.label().value());
					activitiesList.get(originToken).getConflictDestinations().add(destinationToken);
					activitiesList.get(destinationToken).getConflictOrigins().add(originToken);
				}
			}
		}
	}

	private String findDestination(String destinationToken) {
		if (activitiesList.get(destinationToken).getSequenceOrigins().size() > 0) {
			String destinationTokenTmp = activitiesList.get(destinationToken).getSequenceOrigins().get(0);
			if (activitiesList.get(destinationTokenTmp).getRole().equals(ActionType.CONDITION)) {
				destinationToken = destinationTokenTmp;
			}
		}
		return destinationToken;
	}

	public LinkedHashMap<String, Activity> getActivitiesList() {
		return activitiesList;
	}

}
