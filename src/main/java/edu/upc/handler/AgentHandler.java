package edu.upc.handler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexPattern;
import edu.upc.entities.Agent;
import edu.upc.entities.Entity;
import edu.upc.entities.Mention;
import edu.upc.entities.Predicate;
import edu.upc.entities.Token;
import edu.upc.utils.Utils;

public class AgentHandler {
	private LinkedHashMap<String, Agent> actorsList;
	private List<Tree> trees;
	private LinkedHashMap<String, Token> tokens;
	private LinkedHashMap<String, Predicate> predicates;
	private ArrayList<String> posToMakeShortActor;
	private ArrayList<String> prepositionsToMakeShortActors;
	private LinkedHashMap<String, Entity> entities;
	private ArrayList<String> words;
	private Boolean prepositionFound;

	public AgentHandler(List<Tree> trees, LinkedHashMap<String, Token> tokens,
			LinkedHashMap<String, Predicate> predicates, LinkedHashMap<String, Entity> entities) {
		super();
		this.trees = trees;
		this.tokens = tokens;
		this.predicates = predicates;
		this.actorsList = new LinkedHashMap<String, Agent>();
		this.entities = entities;

		posToMakeShortActor = new ArrayList<String>();
		posToMakeShortActor.add("noun");
		posToMakeShortActor.add("adjective");
		prepositionsToMakeShortActors = new ArrayList<String>();
		prepositionsToMakeShortActors.add("of");
		prepositionsToMakeShortActors.add("by");
		prepositionsToMakeShortActors.add("from");

		fillActorsFromPredicate();
		removeEqualActorInOtherMentions();

		actorsList = delimiteActorsNameLength();
		actorsList = removeRepeatedActors();
	}

	private LinkedHashMap<String, Agent> removeRepeatedActors() {
		LinkedHashMap<String, Agent> actorsListTmp = new LinkedHashMap<String, Agent>();
		Collection<Agent> list = actorsList.values();
		ArrayList<Agent> list2 = new ArrayList<Agent>();
		list.forEach(actor -> list2.add(actor));
		for (int i = 0; i < list2.size(); i++) {
			Agent actor1 = list2.get(i);
			if (actor1.getShortText() != null && !actor1.getShortText().equals("")) {
				actorsListTmp.put(actor1.getId(), actor1);
				for (int j = i + 1; j < list2.size(); j++) {
					Agent actor2 = list2.get(j);
					if (actor1.getShortText().equals(actor2.getShortText())) {
						list2.get(j).setShortText("");
					}
				}
			}
		}
		return actorsListTmp;
	}

	private void fillActorsFromPredicate() {
		for (Entry<String, Predicate> predicate : predicates.entrySet()) {
			if (predicate.getValue().getArgumentA0() != null && !predicate.getValue().getHead_token()
					.equals(predicate.getValue().getArgumentA0().getHead_token())) {
				String head_token = predicate.getValue().getArgumentA0().getHead_token();
				String words = predicate.getValue().getArgumentA0().getWords().toLowerCase();
				String fromToken = predicate.getValue().getArgumentA0().getFrom();
				String toToken = predicate.getValue().getArgumentA0().getTo();
				Agent agent = new Agent(head_token, words, tokens.get(fromToken).getBegin(),
						tokens.get(toToken).getEnd());
				actorsList.put(head_token, agent);
			}
		}
	}

	private LinkedHashMap<String, Agent> delimiteActorsNameLength() {
		System.out.println("--> Apply patterns to get Agents name");
		for (Entry<String, Agent> actor : actorsList.entrySet()) {
			words = new ArrayList<String>();
			prepositionFound = false;
			String text = "";
			String actorToken = actor.getKey();
			if (prepositionsToMakeShortActors.contains(tokens.get(actorToken).getLemma())) {
				String stringWithOrBarFromList = Utils.joinStringsListWithOrSeparator(posToMakeShortActor);
				TregexPattern pattern = TregexPattern.compile("/" + Utils.separator + stringWithOrBarFromList
						+ Utils.separator + "/=nounResult > /" + Utils.separator + actorToken + Utils.separator + "/");
				for (int i = 0; i < trees.size(); i++) {
					TregexMatcher matcher = pattern.matcher(trees.get(i));
					while (matcher.findNextMatchingNode()) {
						Tree tNounResult = matcher.getNode("nounResult");
						actorToken = Utils.getTokenFromNode(tNounResult.label().value());
						text += findSubjectInSubtreesAndMakeActorName(actorToken);
						i = trees.size();
					}
				}
			} else if (tokens.get(actorToken).getPos().equals("noun")) {
				text = findSubjectInSubtreesAndMakeActorName(actorToken);
			}
			if (!text.trim().isEmpty()) {
				actor.getValue().setShortText(text.trim());
				actor.getValue().setBegin(tokens.get(words.get(0)).getBegin());
				actor.getValue().setEnd(tokens.get(words.get(words.size() - 1)).getEnd());
			}
		}
		return actorsList;
	}

	private String findSubjectInSubtreesAndMakeActorName(String actorToken) {
		String text = "";
		if (posToMakeShortActor.contains(tokens.get(actorToken).getPos())
				|| prepositionsToMakeShortActors.contains(tokens.get(actorToken).getLemma())) {
			Boolean found = false;
			ArrayList<String> list = new ArrayList<String>();
			posToMakeShortActor.forEach(action -> list.add(action));
			prepositionsToMakeShortActors.forEach(action -> list.add(action));
			String stringWithOrBarFromList = Utils.joinStringsListWithOrSeparator(list);
			TregexPattern pattern = TregexPattern.compile("/" + Utils.separator + stringWithOrBarFromList
					+ Utils.separator + "/=nounResult > /" + Utils.separator + actorToken + Utils.separator + "/");
			for (int i = 0; i < trees.size(); i++) {
				TregexMatcher matcher = pattern.matcher(trees.get(i));
				while (matcher.findNextMatchingNode()) {
					Tree tNounResult = matcher.getNode("nounResult");
					String actorToken2 = Utils.getTokenFromNode(tNounResult.label().value());
					if (prepositionsToMakeShortActors.contains(tokens.get(actorToken2).getLemma())) {
						text += tokens.get(actorToken).getLemma() + " " + tokens.get(actorToken2).getLemma() + " ";
						prepositionFound = true;
						words.add(actorToken);
						words.add(actorToken2);
						text += findSubjectInSubtreesAndMakeActorName(actorToken2);
					} else {
						text += findSubjectInSubtreesAndMakeActorName(actorToken2);
					}
					i = trees.size();
					found = true;
				}
				if (found && !prepositionFound
						&& !prepositionsToMakeShortActors.contains(tokens.get(actorToken).getLemma())) {
					text += tokens.get(actorToken).getLemma() + " ";
					words.add(actorToken);
				}
			}
			if (!found && !prepositionsToMakeShortActors.contains(tokens.get(actorToken).getLemma())) {
				text += tokens.get(actorToken).getLemma() + " ";
				words.add(actorToken);
			}
		}
		return text;
	}

	private void removeEqualActorInOtherMentions() {
		LinkedHashMap<String, Agent> actorsTmp = new LinkedHashMap<String, Agent>();
		for (Entry<String, Agent> actor : actorsList.entrySet()) {
			if (actor.getValue().getCompleteText() != null) {
				actorsTmp.put(actor.getKey(), new Agent(actor.getKey(), actor.getValue().getCompleteText(),
						actor.getValue().getBegin(), actor.getValue().getEnd()));
			}
			LinkedHashMap<String, Mention> mentions = new LinkedHashMap<String, Mention>();
			for (Entry<String, Entity> entity : entities.entrySet()) {
				if (entity.getValue().getMentions().containsKey(actor.getKey())) {
					for (Entry<String, Mention> mention : entity.getValue().getMentions().entrySet()) {
						if (actorsList.containsKey(mention.getKey())) {
							actorsList.get(mention.getKey()).setCompleteText(null);
							actorsList.get(mention.getKey()).setShortText(null);
						}
					}
					mentions = entity.getValue().getMentions();
					break;
				}
			}
			if (actorsTmp.get(actor.getKey()) != null)
				actorsTmp.get(actor.getKey()).setMentions(mentions);
		}
		actorsList = actorsTmp;
	}

	public LinkedHashMap<String, Agent> getActorsList() {
		return actorsList;
	}

	public void setActorsList(LinkedHashMap<String, Agent> actorsList) {
		this.actorsList = actorsList;
	}

}
