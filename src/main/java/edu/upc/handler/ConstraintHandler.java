package edu.upc.handler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexPattern;
import edu.upc.atdputils.AtdpUtils;
import edu.upc.atdputils.ConstraintType;
import edu.upc.atdputils.FilesUrl;
import edu.upc.entities.Activity;
import edu.upc.entities.Constraint;
import edu.upc.entities.SentenceConstraint;
import edu.upc.entities.Token;
import edu.upc.freelingutils.ActionType;
import edu.upc.freelingutils.FreelingUtils;

public class ConstraintHandler {
	private LinkedHashMap<String, Activity> activitiesList;
	private Tree tree;
	private LinkedHashMap<String, Token> tokens;

	public ConstraintHandler(LinkedHashMap<String, Activity> activitiesList, Tree tree,
			LinkedHashMap<String, Token> tokens) {
		this.activitiesList = activitiesList;
		this.tree = tree;
		this.tokens = tokens;
	}

	public SentenceConstraint generate() throws IOException {
		ArrayList<Activity> activities = new ArrayList<Activity>();
		activities = fillActivitiesFromTree();
		activities = sortActivities(activities);
		activities = followOrder(activities);
		activities = makePairs(activities);
		return makeConstraint(activities);
	}

	private SentenceConstraint makeConstraint(ArrayList<Activity> activitiesArray) throws IOException {
		Activity activityA = null;
		Activity activityB = null;
		ConstraintType type = ConstraintType.PRECEDENCE;
		Constraint constraint = null;
		SentenceConstraint sentenceConstrain = new SentenceConstraint();

		if (activitiesArray.size() > 1) {
			for (int i = 0; i < activitiesArray.size(); i = i + 2) {
				activityA = activitiesArray.get(i);
				activityB = activitiesArray.get(i + 1);
				if (AtdpUtils.isProcessObject(activityA.getAction().getWord())
						|| AtdpUtils.isProcessObject(activityB.getAction().getWord())) {
					return null;
				}
				if (isReverseB(activityB)) {
					Activity activityTmp = activityA;
					activityA = activityB;
					activityB = activityTmp;
				}
				if (AtdpUtils.isStartVerb(activityA.getAction().getWord()) && activityB.getRole() != ActionType.CONDITION) {
					constraint = new Constraint(ConstraintType.INIT, activityB);
				} else if (AtdpUtils.isStartVerb(activityB.getAction().getWord())
						&& activityA.getRole() != ActionType.CONDITION) {
					constraint = new Constraint(ConstraintType.INIT, activityA);
				} else if (AtdpUtils.isEndVerb(activityA.getAction().getWord())
						&& activityB.getRole() != ActionType.CONDITION) {
					constraint = new Constraint(ConstraintType.END, activityB);
				} else if (AtdpUtils.isEndVerb(activityB.getAction().getWord())
						&& activityA.getRole() != ActionType.CONDITION) {
					constraint = new Constraint(ConstraintType.END, activityA);
				} else if (activityA.getRole() == ActionType.CONDITION && activityB.getRole() == ActionType.CONDITION) {
					constraint = new Constraint(ConstraintType.NONCOOCCURRENCE, activityA, activityB);
				} else {
					if (!isMandatory(activityB)) {
						type = ConstraintType.PRECEDENCE;
					} else if (isMandatory(activityA)) {
						type = ConstraintType.SUCCESSION;
					} else {
						type = ConstraintType.RESPONSE;
					}
					constraint = new Constraint(type, activityA, activityB);
				}
				sentenceConstrain.setNegative(isNegativeSentence());
				sentenceConstrain.getConstrains().add(constraint);
			}
		} else {
			if (activitiesArray.size() > 0) {
				if (isThereStart()) {
					type = ConstraintType.INIT;
				} else if (isThereEnd()) {
					type = ConstraintType.END;
				} else {
					if (isNegativeSentence()) {
						type = ConstraintType.ABSENCE;
					} else {
						type = ConstraintType.EXISTENCE;
					}
				}
				sentenceConstrain.setNegative(isNegativeSentence());
				sentenceConstrain.getConstrains().add(new Constraint(type, activitiesArray.get(0)));
			}
		}
		return sentenceConstrain;
	}

	private ArrayList<Activity> fillActivitiesFromTree() {
		ArrayList<Activity> activities = new ArrayList<Activity>();
		ArrayList<String> patternStrList = new ArrayList<String>();
		patternStrList.add("/" + FreelingUtils.separator + "*:.*" + FreelingUtils.separator + "/=result");
		String resultToken = "";
		for (String patternStr : patternStrList) {
			TregexPattern pattern = TregexPattern.compile(patternStr);
			TregexMatcher matcher = pattern.matcher(tree);
			while (matcher.findNextMatchingNode()) {
				Tree tResult = matcher.getNode("result");
				if (tResult != null) {
					resultToken = FreelingUtils.getTokenFromNode(tResult.label().value());
					if (activitiesList.containsKey(resultToken))
						activities.add(activitiesList.get(resultToken));
				}
			}
		}
		return activities;
	}

	private ArrayList<Activity> makePairs(ArrayList<Activity> activities) {
		Activity activityA = null;
		Activity activityB = null;
		ArrayList<Activity> temporalList = new ArrayList<Activity>();
		if (activities.size() > 1) {
			activityA = activities.get(0);
			for (int i = 1; i < activities.size(); i++) {
				activityB = activities.get(i);
				temporalList.add(activityA);
				temporalList.add(activityB);
				if (activityB.getRole() == ActionType.CONDITION) {
					activityA = activities.get(i);
				}
			}
		} else {
			if (activities.size() > 0) {
				activityA = activities.get(0);
				temporalList.add(activityA);
			}
		}
		return temporalList;
	}

	private ArrayList<Activity> sortActivities(ArrayList<Activity> activities) {
		ArrayList<Integer> array = new ArrayList<Integer>();
		for (int i = 0; i < activities.size(); i++) {
			String number = activities.get(i).getId().replace(".", " ").split(" ", 2)[1];
			array.add(Integer.parseInt(number));
		}
		Collections.sort(array);
		ArrayList<Activity> activitiesTmp = new ArrayList<Activity>();
		for (int i = 0; i < array.size(); i++) {
			String token = activities.get(i).getId().replace(".", " ").split(" ", 2)[0] + "." + array.get(i);
			for (int j = 0; j < activities.size(); j++) {
				if (token.equals(activities.get(j).getId()))
					activitiesTmp.add(activities.get(j));
			}
		}
		return activitiesTmp;
	}

	private ArrayList<Activity> followOrder(ArrayList<Activity> activities) throws IOException {
		Activity activityA = null;
		Activity activityB = null;
		for (int i = 1; i < activities.size(); i++) {
			activityA = activities.get(i - 1);
			activityB = activities.get(i);
			if (isConditionToReverseFollow(activityA, activityB)) {
				activities.set(i - 1, activityB);
				activities.set(i, activityA);
			}
		}
		return activities;
	}

	private Boolean isConditionToReverseFollow(Activity activityA, Activity activityB) throws IOException {
		if (activityB.getRole() == ActionType.CONDITION) {
			ArrayList<String> patternStrList = new ArrayList<String>();
			patternStrList.add("/" + FreelingUtils.separator + activityA.getId() + FreelingUtils.separator + "/=destination < (/"
					+ FreelingUtils.separator + "if" + FreelingUtils.separator + "/ < /" + FreelingUtils.separator + activityB.getId()
					+ FreelingUtils.separator + "/=origin)");
			for (String patternStr : patternStrList) {
				TregexPattern pattern = TregexPattern.compile(patternStr);
				TregexMatcher matcher = pattern.matcher(tree);
				while (matcher.findNextMatchingNode()) {
					if (matcher.getNode("origin") != null && matcher.getNode("destination") != null)
						return true;
				}
			}
		}
		return false;
	}

	private boolean isActivityIdFound(Activity activity, FilesUrl fileUrl) throws IOException {
		ArrayList<String> patterns = new ArrayList<String>();
		patterns = FreelingUtils.readPatternFile(fileUrl.toString());
		for (String patternStr : patterns) {
			patternStr = patternStr.replace("[parameter1]", activity.getId());
			TregexPattern pattern = TregexPattern.compile(patternStr);
			TregexMatcher matcher = pattern.matcher(tree);
			while (matcher.findNextMatchingNode()) {
				Tree tResult = matcher.getNode("result");
				Tree tMandatory = matcher.getNode("mandatory");
				if (tResult != null) {
					String tokenResult = FreelingUtils.getTokenFromNode(tResult.value());
					if (tMandatory != null) {
						String tokenMandatory = FreelingUtils.getTokenFromNode(tMandatory.value());
						if (tokens.get(tokenResult).getBegin() > tokens.get(tokenMandatory).getBegin())
							return true;
					} else
						return true;
				}
			}
		}
		return false;
	}

	private boolean isThereEnd() {
		ArrayList<String> patternStrList = new ArrayList<String>();
		String param = "";
		Boolean found = false;
		for (int i = 0; i < Arrays.asList(AtdpUtils.END_VERBS).size(); i++) {
			if (!found)
				found = true;
			else
				param += "|";
			param += FreelingUtils.separator + Arrays.asList(AtdpUtils.END_VERBS).get(i) + FreelingUtils.separator;
		}
		patternStrList.add("/" + param + "/=result");
		for (String patternStr : patternStrList) {
			TregexPattern pattern = TregexPattern.compile(patternStr);
			TregexMatcher matcher = pattern.matcher(tree);
			while (matcher.findNextMatchingNode()) {
				Tree tResult = matcher.getNode("result");
				if (tResult != null) {
					return true;
				}
			}
		}
		return false;
	}

	private Boolean isThereStart() {
		ArrayList<String> patternStrList = new ArrayList<String>();
		String param = "";
		Boolean found = false;
		for (int i = 0; i < Arrays.asList(AtdpUtils.START_VERBS).size(); i++) {
			if (!found)
				found = true;
			else
				param += "|";
			param += FreelingUtils.separator + Arrays.asList(AtdpUtils.START_VERBS).get(i) + FreelingUtils.separator;
		}
		patternStrList.add("/" + param + "/=result");
		for (String patternStr : patternStrList) {
			TregexPattern pattern = TregexPattern.compile(patternStr);
			TregexMatcher matcher = pattern.matcher(tree);
			while (matcher.findNextMatchingNode()) {
				Tree tResult = matcher.getNode("result");
				if (tResult != null) {
					return true;
				}
			}
		}
		return false;
	}

	private Boolean isNegativeSentence() {
		ArrayList<String> patternStrList = new ArrayList<String>();
		patternStrList.add("/" + FreelingUtils.separator + "not" + FreelingUtils.separator + "/=result");
		for (String patternStr : patternStrList) {
			TregexPattern pattern = TregexPattern.compile(patternStr);
			TregexMatcher matcher = pattern.matcher(tree);
			while (matcher.findNextMatchingNode()) {
				Tree tResult = matcher.getNode("result");
				if (tResult != null) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean isReverseB(Activity activity) throws IOException {
		return isActivityIdFound(activity, FilesUrl.TOREVERSE_PATTERNS_FILE);
	}

	private Boolean isMandatory(Activity activity) throws IOException {
		return isActivityIdFound(activity, FilesUrl.MANDATORY_PATTERNS_FILE);
	}

	public LinkedHashMap<String, Token> getTokens() {
		return tokens;
	}
}
