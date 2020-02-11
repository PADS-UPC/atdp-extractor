package edu.upc.handler;

import java.io.IOException;
import java.util.ArrayList;
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

		ArrayList<String> patternsToIdentifyGateways = new ArrayList<String>();
		patternsToIdentifyGateways = Utils.readPatternFile(FilesUrl.CONDITION_PATTERNS_FILE.toString());
		conditionsPatterExecutor(patternsToIdentifyGateways, ActionType.CONDITION);

		ArrayList<String> patternsToGenerateNewGateways = new ArrayList<String>();
		patternsToGenerateNewGateways = Utils.readPatternFile(FilesUrl.NEW_CONDITION_PATTERNS_FILE.toString());
		generateNewConditions(patternsToGenerateNewGateways);

		ArrayList<String> patternsToIdentifyConditionalEvent = new ArrayList<String>();
		patternsToIdentifyConditionalEvent = Utils.readPatternFile(FilesUrl.EVENT_PATTERNS_FILE.toString());
		genericPatterExecutor(patternsToIdentifyConditionalEvent, ActionType.EVENT);

		ArrayList<String> patternsToIdentifyActivity = new ArrayList<String>();
		patternsToIdentifyActivity = Utils.readPatternFile(FilesUrl.ACTIVITY_PATTERNS_FILE.toString());
		genericPatterExecutor(patternsToIdentifyActivity, ActionType.ACTIVITY);

		ArrayList<String> patternsToRemoveSubActivities = new ArrayList<String>();
		patternsToRemoveSubActivities = Utils.readPatternFile(FilesUrl.REMOVE_ACTIVITY_FILE.toString());
		removeActivities(patternsToRemoveSubActivities);

		changeAllActionsToActivity();

		ArrayList<String> patternsToSequence = new ArrayList<String>();
		patternsToSequence = Utils.readPatternFile(FilesUrl.SEQUENCE_PATTERNS_FILE.toString());
		sequencePatterExecutor(patternsToSequence);

		ArrayList<String> conflictPatterns = new ArrayList<String>();
		conflictPatterns = Utils.readPatternFile(FilesUrl.CONFLICT_PATTERNS_FILE.toString());
		conflictPatterExecutor(conflictPatterns);

	}

	private void changeAllActionsToActivity() throws IOException {
		for (Entry<String, Activity> activity : activitiesList.entrySet()) {
			if (activity.getValue().getRole().equals(ActionType.ACTION)) {
				activitiesList.get(activity.getKey()).setRole(ActionType.ACTIVITY);
			}
		}
		TreesHandler.refreshTree(trees, activitiesList, tokens);
	}

	private void conditionsPatterExecutor(ArrayList<String> patternStrList, ActionType role) throws IOException {
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

	private void generateNewConditions(ArrayList<String> patternStrList) throws IOException {
		for (String patternStr : patternStrList) {
			TregexPattern pattern = TregexPattern.compile(patternStr);
			for (int i = 0; i < trees.size(); i++) {
				TregexMatcher matcher = pattern.matcher(trees.get(i));
				while (matcher.findNextMatchingNode()) {
					Tree tResult = matcher.getNode("result");
					Tree tCondition = matcher.getNode("condition");
					Tree tObject = matcher.getNode("object");
					String token = Utils.getTokenFromNode(tResult.label().value());
					Action action = new Action(token, tokens.get(token).getLemma(), tokens.get(token).getBegin(),
							tokens.get(token).getEnd());
					Activity activity = new Activity(token, action);
					activity.setAgent(null);
					activity.setRole(ActionType.CONDITION);
					if (tCondition != null) {
						String conditionToken = Utils.getTokenFromNode(tCondition.label().value());
						activity.setPattern(tokens.get(conditionToken).getLemma());
					}
					if (tObject != null) {
						String objectToken = Utils.getTokenFromNode(tObject.label().value());
						activity.setPatient(new Patient(objectToken, tokens.get(objectToken).getLemma(),
								tokens.get(objectToken).getLemma(), tokens.get(objectToken).getBegin(),
								tokens.get(objectToken).getEnd()));
					} else {
						activity.setPatient(
								new Patient(token, tokens.get(token).getLemma(), tokens.get(token).getLemma(),
										tokens.get(token).getBegin(), tokens.get(token).getEnd()));
					}
					activitiesList.put(token, activity);
				}
			}
		}
		TreesHandler.refreshTree(trees, activitiesList, tokens);
	}

	public LinkedHashMap<String, Activity> getActivitiesList() {
		return activitiesList;
	}

}
