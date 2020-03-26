package edu.upc.handler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import edu.stanford.nlp.trees.Tree;
import edu.upc.entities.Activity;
import edu.upc.entities.Token;
import edu.upc.handler.PatternsHandler;
import edu.upc.utils.ActionType;

public class AtdpHandler {
	private LinkedHashMap<String, Token> tokens;
	private LinkedHashMap<String, Activity> activitiesList;
	private ArrayList<String[]> actionAnnotationList = new ArrayList<String[]>();
	private ArrayList<String[]> conditionAnnotationList = new ArrayList<String[]>();
	private ArrayList<String[]> eventAnnotationList = new ArrayList<String[]>();
	private ArrayList<String[]> agentAnnotationList = new ArrayList<String[]>();
	private ArrayList<String[]> patientAnnotationList = new ArrayList<String[]>();

	public AtdpHandler(LinkedHashMap<String, Token> tokens, LinkedHashMap<String, Activity> activitiesList,
			List<Tree> trees) throws IOException {
		this.tokens = tokens;
		PatternsHandler patternHandler = new PatternsHandler(tokens, trees, activitiesList);
		this.activitiesList = patternHandler.getActivitiesList();
		Integer count = 0;

		generateConditionAnnotationList(count, activitiesList);

		count = conditionAnnotationList.size();
		generateActionAnnotationList(count, activitiesList);

		count = actionAnnotationList.size() + conditionAnnotationList.size();
		count = generateAgentAnnotationList(count, activitiesList);

		generatePatientAnnotationList(count, activitiesList);
	}

	public String getAnnotationTextFromActivities() throws IOException {
		String text = "";
		Integer count = 0;

		text += makeText(conditionAnnotationList);
		text += makeText(actionAnnotationList);
		text += makeText(agentAnnotationList);
		text += makeText(patientAnnotationList);

		count = 0;
		String[] array = makeAgentRelationText(count, agentAnnotationList);
		text += array[0];
		count += Integer.parseInt(array[1]);

		array = makePatientRelationText(count, patientAnnotationList);
		text += array[0];

		count += Integer.parseInt(array[1]);
		array = makeSequenceRelationText(count);
		text += array[0];

		count += Integer.parseInt(array[1]);
		array = makeConflictRelationText(count);
		text += array[0];

		for (int i = 0; i < eventAnnotationList.size(); i++) {
			text += eventAnnotationList.get(i)[0] + "\t";
			text += eventAnnotationList.get(i)[1] + " ";
			text += eventAnnotationList.get(i)[2] + " ";
			text += "\n";
		}

		return text;
	}

	private String[] makeSequenceRelationText(Integer count) {
		String text = "";
		for (Entry<String, Activity> activity : activitiesList.entrySet()) {
			if (activity.getValue().getSequenceDestinations().size() > 0) {
				String verbActivity = tokens.get(activity.getKey()).getForm().toLowerCase();
				Integer beginActivity = Integer.parseInt(tokens.get(activity.getKey()).getBegin().toString());
				Integer endActivity = Integer.parseInt(tokens.get(activity.getKey()).getEnd().toString());
				String arg1 = "";
				Boolean foundArg1 = false;
				for (int i = 0; i < conditionAnnotationList.size(); i++) {
					if (beginActivity >= Integer.parseInt(conditionAnnotationList.get(i)[2])
							&& endActivity <= Integer.parseInt(conditionAnnotationList.get(i)[3])
							&& conditionAnnotationList.get(i)[4].contains(verbActivity)) {
						arg1 += "Sequence ";
						arg1 += "Arg1:" + conditionAnnotationList.get(i)[0] + " ";
						foundArg1 = true;
						break;
					}
				}
				if (!foundArg1) {
					for (int i = 0; i < actionAnnotationList.size(); i++) {
						if (beginActivity >= Integer.parseInt(actionAnnotationList.get(i)[2])
								&& endActivity <= Integer.parseInt(actionAnnotationList.get(i)[3])
								&& actionAnnotationList.get(i)[4].contains(verbActivity)) {
							arg1 += "Sequence ";
							arg1 += "Arg1:" + actionAnnotationList.get(i)[0] + " ";
							foundArg1 = true;
							break;
						}
					}
				}
				if (foundArg1) {
					Boolean foundArg2 = false;
					for (int i = 0; i < activity.getValue().getSequenceDestinations().size(); i++) {
						String tokenArg2 = activity.getValue().getSequenceDestinations().get(i);
						for (int j = 0; j < actionAnnotationList.size(); j++) {
							String name = tokens.get(tokenArg2).getForm().toLowerCase();
							String begin = tokens.get(tokenArg2).getBegin().toString();
							String end = tokens.get(tokenArg2).getEnd().toString();
							if (actionAnnotationList.get(j)[2].equals(begin)
									&& actionAnnotationList.get(j)[3].equals(end)
									&& actionAnnotationList.get(j)[4].equals(name)) {
								text += "R" + count + "\t";
								text += arg1 + "Arg2:" + actionAnnotationList.get(j)[0];
								text += "\n";
								count++;
								foundArg2 = true;
								break;
							}
						}
					}
					if (!foundArg2) {
						for (int i = 0; i < activity.getValue().getSequenceDestinations().size(); i++) {
							String tokenArg2 = activity.getValue().getSequenceDestinations().get(i);
							String name = tokens.get(tokenArg2).getForm().toLowerCase();
							Integer begin = Integer.parseInt(tokens.get(tokenArg2).getBegin().toString());
							Integer end = Integer.parseInt(tokens.get(tokenArg2).getEnd().toString());
							for (int j = 0; j < conditionAnnotationList.size(); j++) {
								if (begin >= Integer.parseInt(conditionAnnotationList.get(j)[2])
										&& end <= Integer.parseInt(conditionAnnotationList.get(j)[3])
										&& conditionAnnotationList.get(j)[4].contains(name)) {
									text += "R" + count + "\t";
									text += arg1 + "Arg2:" + conditionAnnotationList.get(j)[0];
									text += "\n";
									count++;
									foundArg1 = true;
									break;
								}
							}
						}
					}
				}
			}
		}
		String[] array = { text, count.toString() };
		return array;
	}

	private String[] makeConflictRelationText(Integer count) {
		String text = "";
		for (Entry<String, Activity> activity : activitiesList.entrySet()) {
			if (activity.getValue().getConflictDestinations().size() > 0) {
				String verbActivity = tokens.get(activity.getKey()).getForm().toLowerCase();
				Integer beginActivity = Integer.parseInt(tokens.get(activity.getKey()).getBegin().toString());
				Integer endActivity = Integer.parseInt(tokens.get(activity.getKey()).getEnd().toString());
				String arg1 = "";
				Boolean foundArg1 = false;
				for (int i = 0; i < conditionAnnotationList.size(); i++) {
					if (beginActivity >= Integer.parseInt(conditionAnnotationList.get(i)[2])
							&& endActivity <= Integer.parseInt(conditionAnnotationList.get(i)[3])
							&& conditionAnnotationList.get(i)[4].contains(verbActivity)) {
						arg1 += "Conflict ";
						arg1 += "Arg1:" + conditionAnnotationList.get(i)[0] + " ";
						foundArg1 = true;
						break;
					}
				}
				if (!foundArg1) {
					for (int i = 0; i < actionAnnotationList.size(); i++) {
						if (beginActivity >= Integer.parseInt(actionAnnotationList.get(i)[2])
								&& endActivity <= Integer.parseInt(actionAnnotationList.get(i)[3])
								&& actionAnnotationList.get(i)[4].contains(verbActivity)) {
							arg1 += "Conflict ";
							arg1 += "Arg1:" + actionAnnotationList.get(i)[0] + " ";
							foundArg1 = true;
							break;
						}
					}
				}
				if (foundArg1) {
					Boolean foundArg2 = false;
					for (int i = 0; i < activity.getValue().getConflictDestinations().size(); i++) {
						String tokenArg2 = activity.getValue().getConflictDestinations().get(i);
						for (int j = 0; j < actionAnnotationList.size(); j++) {
							String name = tokens.get(tokenArg2).getForm().toLowerCase();
							String begin = tokens.get(tokenArg2).getBegin().toString();
							String end = tokens.get(tokenArg2).getEnd().toString();
							if (actionAnnotationList.get(j)[2].equals(begin)
									&& actionAnnotationList.get(j)[3].equals(end)
									&& actionAnnotationList.get(j)[4].equals(name)) {
								text += "R" + count + "\t";
								text += arg1 + "Arg2:" + actionAnnotationList.get(j)[0];
								text += "\n";
								count++;
								foundArg2 = true;
								break;
							}
						}
					}
					if (!foundArg2) {
						for (int i = 0; i < activity.getValue().getConflictDestinations().size(); i++) {
							String tokenArg2 = activity.getValue().getConflictDestinations().get(i);
							String name = tokens.get(tokenArg2).getForm().toLowerCase();
							Integer begin = Integer.parseInt(tokens.get(tokenArg2).getBegin().toString());
							Integer end = Integer.parseInt(tokens.get(tokenArg2).getEnd().toString());
							for (int j = 0; j < conditionAnnotationList.size(); j++) {
								if (begin >= Integer.parseInt(conditionAnnotationList.get(j)[2])
										&& end <= Integer.parseInt(conditionAnnotationList.get(j)[3])
										&& conditionAnnotationList.get(j)[4].contains(name)) {
									text += "R" + count + "\t";
									text += arg1 + "Arg2:" + conditionAnnotationList.get(j)[0];
									text += "\n";
									count++;
									foundArg1 = true;
									break;
								}
							}
						}
					}
				}
			}
		}
		String[] array = { text, count.toString() };
		return array;
	}

	private String[] makeAgentRelationText(Integer count, ArrayList<String[]> annotationList) {
		String text = "";
		for (Entry<String, Activity> activity : activitiesList.entrySet()) {
			if (activity.getValue().getAgent() != null && !activity.getValue().getRole().equals(ActionType.CONDITION)) {
				for (int i = 0; i < actionAnnotationList.size(); i++) {
					String verbActivity = tokens.get(activity.getKey()).getForm().toLowerCase();
					String beginActivity = tokens.get(activity.getKey()).getBegin().toString();
					String endActivity = tokens.get(activity.getKey()).getEnd().toString();
					if (actionAnnotationList.get(i)[2].equals(beginActivity)
							&& actionAnnotationList.get(i)[3].equals(endActivity)
							&& actionAnnotationList.get(i)[4].equals(verbActivity)) {
						text += "R" + count + "\t";
						text += "Agent ";
						text += "Arg1:" + actionAnnotationList.get(i)[0] + " ";
						for (int j = 0; j < annotationList.size(); j++) {
							String name = activity.getValue().getAgent().getShortText();
							String begin = activity.getValue().getAgent().getBegin().toString();
							String end = activity.getValue().getAgent().getEnd().toString();
							endActivity = tokens.get(activity.getKey()).getEnd().toString();
							if (annotationList.get(j)[2].equals(begin) && annotationList.get(j)[3].equals(end)
									&& annotationList.get(j)[4].equals(name)) {
								text += "Arg2:" + annotationList.get(j)[0];
								break;
							}
						}
						text += "\n";
						count++;
					}
				}
			}
		}
		String[] array = { text, count.toString() };
		return array;
	}

	private String[] makePatientRelationText(Integer count, ArrayList<String[]> annotationList) {
		String text = "";
		for (Entry<String, Activity> activity : activitiesList.entrySet()) {
			if (activity.getValue().getPatient() != null
					&& !activity.getValue().getRole().equals(ActionType.CONDITION)) {
				for (int i = 0; i < actionAnnotationList.size(); i++) {
					String verbActivity = tokens.get(activity.getKey()).getForm().toLowerCase();
					String beginActivity = tokens.get(activity.getKey()).getBegin().toString();
					String endActivity = tokens.get(activity.getKey()).getEnd().toString();
					if (actionAnnotationList.get(i)[2].equals(beginActivity)
							&& actionAnnotationList.get(i)[3].equals(endActivity)
							&& actionAnnotationList.get(i)[4].equals(verbActivity)) {
						text += "R" + count + "\t";
						text += "Patient ";
						text += "Arg1:" + actionAnnotationList.get(i)[0] + " ";
						for (int j = 0; j < annotationList.size(); j++) {
							String name = activity.getValue().getPatient().getShortText();
							String begin = activity.getValue().getPatient().getBegin().toString();
							String end = activity.getValue().getPatient().getEnd().toString();
							endActivity = tokens.get(activity.getKey()).getEnd().toString();
							if (annotationList.get(j)[2].equals(begin) && annotationList.get(j)[3].equals(end)
									&& annotationList.get(j)[4].equals(name)) {
								text += "Arg2:" + annotationList.get(j)[0];
								break;
							}
						}
						text += "\n";
						count++;
					}
				}
			}
		}
		String[] array = { text, count.toString() };
		return array;
	}

	private String makeText(ArrayList<String[]> annotationList) {
		String text = "";
		for (int i = 0; i < annotationList.size(); i++) {
			text += annotationList.get(i)[0] + "\t";
			text += annotationList.get(i)[1] + " ";
			text += annotationList.get(i)[2] + " ";
			text += annotationList.get(i)[3] + "\t";
			text += annotationList.get(i)[4];
			text += "\n";
		}
		return text;
	}

	private Integer generateAgentAnnotationList(Integer count, LinkedHashMap<String, Activity> activitiesList) {
		for (Entry<String, Activity> activity : activitiesList.entrySet()) {
			if (activity.getValue().getAgent() != null) {
				String words[] = { "T" + count, "Entity", activity.getValue().getAgent().getBegin().toString(),
						activity.getValue().getAgent().getEnd().toString(),
						activity.getValue().getAgent().getShortText() };
				Boolean found = false;
				for (int i = 0; i < agentAnnotationList.size(); i++) {
					if (agentAnnotationList.get(i)[1].equals(words[1]) && agentAnnotationList.get(i)[2].equals(words[2])
							&& agentAnnotationList.get(i)[3].equals(words[3])
							&& agentAnnotationList.get(i)[4].equals(words[4])) {
						found = true;

					}
				}
				if (!found)
					agentAnnotationList.add(words);
				count++;
			}
		}
		return count;
	}

	private Integer generatePatientAnnotationList(Integer count, LinkedHashMap<String, Activity> activitiesList) {
		for (Entry<String, Activity> activity : activitiesList.entrySet()) {
			if (activity.getValue().getPatient() != null) {
				String words[] = { "T" + count, "Entity", activity.getValue().getPatient().getBegin().toString(),
						activity.getValue().getPatient().getEnd().toString(),
						activity.getValue().getPatient().getShortText() };
				patientAnnotationList.add(words);
				count++;
			}
		}
		return count;
	}

	private void generateActionAnnotationList(Integer count, LinkedHashMap<String, Activity> activitiesList) {
		Integer aCount = 0;
		for (Entry<String, Activity> activity : activitiesList.entrySet()) {
			if (activity.getValue().getRole().equals(ActionType.ACTIVITY)) {
				String text = tokens.get(activity.getKey()).getForm().toLowerCase();
				String words[] = { "T" + count, "Action", activity.getValue().getAction().getBegin().toString(),
						activity.getValue().getAction().getEnd().toString(), text};
						//activity.getValue().getPatient().getBegin().toString(),
						//activity.getValue().getPatient().getEnd().toString() };
				actionAnnotationList.add(words);
				count++;
			} else if (activity.getValue().getRole().equals(ActionType.EVENT)) {
				String text = tokens.get(activity.getKey()).getForm().toLowerCase();
				String words[] = { "T" + count, "Action", activity.getValue().getAction().getBegin().toString(),
						activity.getValue().getAction().getEnd().toString(), text};
						//activity.getValue().getPatient().getBegin().toString(),
						//activity.getValue().getPatient().getEnd().toString() };
				actionAnnotationList.add(words);
				count++;

				String wordsEvent[] = { "A" + aCount, "Event", words[0] };
				eventAnnotationList.add(wordsEvent);
				aCount++;
			}

		}

	}

	private void generateConditionAnnotationList(Integer count, LinkedHashMap<String, Activity> activitiesList) {
		for (Entry<String, Activity> activity : activitiesList.entrySet()) {
			if (activity.getValue().getRole().equals(ActionType.CONDITION)) {
				Integer begin = 0;
				Integer end = 0;
				String text = activity.getValue().getAction().getWord();
				begin = activity.getValue().getAction().getBegin();
				end = activity.getValue().getAction().getEnd();
				String words[] = { "T" + count, "Condition", begin.toString(), end.toString(), text };
				conditionAnnotationList.add(words);
				activity.getValue().setPatient(null);
				count++;
			}
		}

	}

	public ArrayList<String[]> getActionAnnotationList() {
		return actionAnnotationList;
	}

	public void setActionAnnotationList(ArrayList<String[]> actionAnnotationList) {
		this.actionAnnotationList = actionAnnotationList;
	}

	public ArrayList<String[]> getConditionAnnotationList() {
		return conditionAnnotationList;
	}

	public void setConditionAnnotationList(ArrayList<String[]> conditionAnnotationList) {
		this.conditionAnnotationList = conditionAnnotationList;
	}

	public ArrayList<String[]> getEventAnnotationList() {
		return eventAnnotationList;
	}

	public void setEventAnnotationList(ArrayList<String[]> eventAnnotationList) {
		this.eventAnnotationList = eventAnnotationList;
	}

	public ArrayList<String[]> getAgentAnnotationList() {
		return agentAnnotationList;
	}

	public void setAgentAnnotationList(ArrayList<String[]> agentAnnotationList) {
		this.agentAnnotationList = agentAnnotationList;
	}

	public ArrayList<String[]> getPatientAnnotationList() {
		return patientAnnotationList;
	}

	public void setPatientAnnotationList(ArrayList<String[]> patientAnnotationList) {
		this.patientAnnotationList = patientAnnotationList;
	}

}
