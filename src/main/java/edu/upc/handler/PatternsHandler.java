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
import edu.upc.entities.Patient;
import edu.upc.entities.Token;
import edu.upc.utils.ActionType;
import edu.upc.utils.FilesUrl;
import edu.upc.utils.Utils;

public class PatternsHandler {
	private LinkedHashMap<String, Activity> activitiesList;
	private LinkedHashMap<String, Token> tokens;
	private List<Tree> trees;

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
			removeActionsUnderConditions(patterns);

		patterns = Utils.readPatternFile(FilesUrl.EVENT_PATTERNS_FILE.toString());
		if (Utils.isToApplyPattern())
			genericPatterExecutor(patterns, ActionType.EVENT);

		patterns = Utils.readPatternFile(FilesUrl.VERBS_TO_REMOVE_PATTERNS_FILE.toString());
		removeVerbs(patterns);
		if (Utils.isToApplyPattern())
			removeActionWhoseObjectIsItsParentNode();// fix parser error
		if (Utils.isToApplyPattern())
			removeActionsAfterTO();

		patterns = Utils.readPatternFile(FilesUrl.ACTIVITY_PATTERNS_FILE.toString());
		if (Utils.isToApplyPattern())
			genericPatterExecutor(patterns, ActionType.ACTIVITY);

		if (Utils.isToApplyPattern())
			removeActionsUnderActivity();

		patterns = Utils.readPatternFile(FilesUrl.REMOVE_ACTIVITY_FILE.toString());
		if (Utils.isToApplyPattern())
			removeActivities(patterns);

		changeAllActionsToActivity();

		if (Utils.isToApplyPattern())
			removeActivitiesWithProcessObject();

		patterns = Utils.readPatternFile(FilesUrl.SEQUENCE_PATTERNS_FILE.toString());
		sequencePatterExecutor(patterns);

		patterns = Utils.readPatternFile(FilesUrl.CONFLICT_PATTERNS_FILE.toString());
		conflictPatterExecutor(patterns);

	}

	private void removeActivitiesWithProcessObject() throws IOException {
		ArrayList<String> list = new ArrayList<String>();
		for (Entry<String, Activity> activity : activitiesList.entrySet()) {
			if (activity.getValue().getPatient() != null) {
				String token = activity.getValue().getPatient().getId();
				if (tokens.get(token).getLemma().equals("process")) {
					System.out.println("-> " + activity.getValue().getId() + " - "
							+ tokens.get(activity.getValue().getId()).getLemma() + " - "
							+ activity.getValue().getPatient().getShortText());
					if (!tokens.get(activity.getValue().getId()).getLemma().equals("examine"))
						list.add(activity.getValue().getId());
					else
						System.out.println("found");
				}
			}
		}
		for (int i = 0; i < list.size(); i++) {
			activitiesList.remove(list.get(i));
		}
		TreesHandler.refreshTree(trees, activitiesList, tokens);
	}

	private void removeActionsUnderActivity() throws IOException {
		System.out.println("--> Removing ACTIONS under main ACTIVITY after AND/OR, and update ActivitiesList");
		String patternStr = "/¦ACTION:.*¦/=toRemove >> (/¦ACTIVITY:.*¦/ > /¦and¦|¦or¦/)";
		applyPatternToRemove(patternStr);

	}

	private void removeActionsAfterTO() throws IOException {
		System.out.println("--> Removing ACTIONS under TO, and update ActivitiesList");
		String patternStr = "/¦*:.*¦/=toRemove >> (/¦to¦/ > /¦ACTION:.*¦/=result)";
		applyPatternToRemove(patternStr);
	}

	private void removeActionWhoseObjectIsItsParentNode() throws IOException {
		// Fix parser error
		String patternStr = "/¦*:.*¦/=toRemove > /¦noun¦/=object";
		TregexPattern pattern = TregexPattern.compile(patternStr);
		for (int i = 0; i < trees.size(); i++) {
			TregexMatcher matcher = pattern.matcher(trees.get(i));
			while (matcher.findNextMatchingNode()) {
				Tree tToRemove = matcher.getNode("toRemove");
				String removeToken = Utils.getTokenFromNode(tToRemove.label().value());
				String objectToken = Utils.getTokenFromNode(matcher.getNode("object").label().value());
				if (activitiesList.containsKey(removeToken)) {
					if (!hasChildren(tToRemove, trees.get(i))) {
						Patient patient = new Patient();
						patient = activitiesList.get(removeToken).getPatient();
						if (patient.getId().equals(objectToken))
							activitiesList.remove(Utils.getTokenFromNode(tToRemove.label().value()));
					}
				}
			}
		}
		TreesHandler.refreshTree(trees, activitiesList, tokens);
	}

	private boolean hasChildren(Tree tToRemove, Tree tree) {
		String patternStr = "/¦.*/=result > /¦" + Utils.getTokenFromNode(tToRemove.label().value()) + "¦/";
		TregexPattern pattern = TregexPattern.compile(patternStr);
		TregexMatcher matcher = pattern.matcher(tree);
		while (matcher.findNextMatchingNode()) {
			return true;
		}
		return false;
	}

	private void removeVerbs(ArrayList<String> patterns) throws IOException {
		System.out.println("--> Removing VERBS that are not actions and update ActivitiesList");
		for (String patternStr : patterns) {
			patternStr = "/¦" + patternStr + "¦.*¦ACTION:.*¦/=toRemove";
			applyPatternToRemove(patternStr);
		}
	}

	private void removeActionsUnderConditions(ArrayList<String> patterns) throws IOException {
		System.out.println("--> Removing all actions under CONDITIONS and update ActivitiesList");
		for (String patternStr : patterns) {
			applyPatternToRemove(patternStr);
		}
	}

	private void applyPatternToRemove(String patternStr) throws IOException {
		TregexPattern pattern = TregexPattern.compile(patternStr);
		for (int i = 0; i < trees.size(); i++) {
			TregexMatcher matcher = pattern.matcher(trees.get(i));
			while (matcher.findNextMatchingNode()) {
				Tree tToRemove = matcher.getNode("toRemove");
				activitiesList.remove(Utils.getTokenFromNode(tToRemove.label().value()));
			}
		}
		TreesHandler.refreshTree(trees, activitiesList, tokens);
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
					TreesHandler.refreshTree(trees, activitiesList, tokens);
				}
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
						String patternStr2 = "/¦*:.*¦/=result >> /¦" + resultToken + "¦/";
						TregexPattern pattern2 = TregexPattern.compile(patternStr2);
						TregexMatcher matcher2 = pattern2.matcher(trees.get(i));
						while (matcher2.findNextMatchingNode()) {
							Tree tResult2 = matcher2.getNode("result");
							String resultToken2 = Utils.getTokenFromNode(tResult2.label().value());
							if (activitiesList.containsKey(resultToken2)) {
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

	private ArrayList<String> getActionInformation(String tokenToStart, Tree tree, String conditionLemma) {
		String text = "";
		beginToken = tokenToStart;
		endToken = tokenToStart;
		getFirstLastNode(tree, conditionLemma);
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

	private void getFirstLastNode(Tree tree, String conditionLemma) {
		String resultToken = Utils.getTokenFromNode(tree.value());
		Integer value = Integer.parseInt(resultToken.split("\\.")[1]);
		if (value < Integer.parseInt(beginToken.split("\\.")[1]))
			beginToken = resultToken;
		if (value > Integer.parseInt(endToken.split("\\.")[1]))
			endToken = resultToken;
		Iterator<?> treeIterator = tree.getChildrenAsList().iterator();
		while (treeIterator.hasNext()) {
			Tree tResult = (Tree) treeIterator.next();
			if (!conditionLemma.equals("if")) {
				if (subtreeLimits.contains(tokens.get(Utils.getTokenFromNode(tResult.value())).getLemma()))
					return;
			}
			getFirstLastNode(tResult, conditionLemma);
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

	private void genericPatterExecutor(ArrayList<String> patternStrList, ActionType role) throws IOException {
		System.out.println("--> Identify " + role.toString().toUpperCase() + " and update ActivitiesList");
		String token = "";
		for (String patternStr : patternStrList) {
			TregexPattern pattern = TregexPattern.compile(patternStr);
			for (int i = 0; i < trees.size(); i++) {
				TregexMatcher matcher = pattern.matcher(trees.get(i));
				while (matcher.findNextMatchingNode()) {
					Tree tActivity = matcher.getNode("result");
					if (tActivity == null)
						return;
					Tree tCondition = matcher.getNode("condition");
					Tree tToRemove = matcher.getNode("toRemove");
					token = Utils.getTokenFromNode(tActivity.label().value());
					if (activitiesList.containsKey(token) && activitiesList.get(token).getRole() == ActionType.ACTION) {
						activitiesList.get(token).setRole(role);
						if (tCondition != null) {
							String conditionToken = Utils.getTokenFromNode(tCondition.label().value());
							activitiesList.get(token).setPattern(tokens.get(conditionToken).getLemma());
						}
						if (tToRemove != null) {
							String tokenToRemove = Utils.getTokenFromNode(tToRemove.label().value());
							activitiesList.remove(tokenToRemove);
						}
						TreesHandler.refreshTree(trees, activitiesList, tokens);
						i--;
						break;
					}
				}
			}
		}

	}

	private void removeActivities(ArrayList<String> patternStrList) throws IOException {
		ArrayList<String> listToRemove = new ArrayList<String>();
		for (String patternStr : patternStrList) {
			TregexPattern pattern = TregexPattern.compile(patternStr);
			for (int i = 0; i < trees.size(); i++) {
				TregexMatcher matcher = pattern.matcher(trees.get(i));
				Tree tResult = null;
				while (matcher.findNextMatchingNode()) {
					// if (tActivity == null)
					tResult = matcher.getNode("result");
					Tree tToRemove = matcher.getNode("toRemove");
					if (tResult != null) {
						String tokenResult = Utils.getTokenFromNode(tResult.label().value());
						if (activitiesList.containsKey(tokenResult)
								&& activitiesList.get(tokenResult).getRole() == ActionType.ACTION) {
							activitiesList.get(tokenResult).setRole(ActionType.ACTIVITY);
							if (tToRemove != null) {
								String tokenToRemove = Utils.getTokenFromNode(tToRemove.label().value());
								if (activitiesList.containsKey(tokenToRemove)) {
									listToRemove.add(tokenToRemove);
								}
							}
						}
					} else if (tToRemove != null) {
						String tokenToRemove = Utils.getTokenFromNode(tToRemove.label().value());
						if (activitiesList.containsKey(tokenToRemove)) {
							activitiesList.remove(tokenToRemove);
						}
					}
				}

				for (int j = 0; j < listToRemove.size(); j++) {
					activitiesList.remove(listToRemove.get(j));
				}
			}
		}
		TreesHandler.refreshTree(trees, activitiesList, tokens);
	}

	public LinkedHashMap<String, Activity> getActivitiesList() {
		return activitiesList;
	}

}
