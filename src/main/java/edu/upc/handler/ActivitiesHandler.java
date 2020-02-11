package edu.upc.handler;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import edu.upc.entities.Action;
import edu.upc.entities.Activity;
import edu.upc.entities.Agent;
import edu.upc.entities.Patient;
import edu.upc.entities.Predicate;
import edu.upc.entities.PredicateArgument;
import edu.upc.entities.Token;
import edu.upc.utils.Utils;

public class ActivitiesHandler {
	private LinkedHashMap<String, Activity> activitiesList;
	private LinkedHashMap<String, Predicate> predicates;
	private LinkedHashMap<String, Token> tokens;
	private LinkedHashMap<String, Agent> agentsList;
	private LinkedHashMap<String, Patient> patientsList;

	public ActivitiesHandler(LinkedHashMap<String, Predicate> predicates, LinkedHashMap<String, Token> tokens,
			LinkedHashMap<String, Agent> agentsList, LinkedHashMap<String, Patient> patientsList) throws IOException {
		super();
		this.activitiesList = new LinkedHashMap<String, Activity>();
		this.tokens = tokens;
		this.predicates = predicates;
		this.agentsList = agentsList;
		this.patientsList = patientsList;

		fillActivitiesListFromPredicates();
	}

	private LinkedHashMap<String, Activity> fillActivitiesListFromPredicates() throws IOException {
		for (Entry<String, Predicate> predicate : predicates.entrySet()) {
			Boolean isNounAction = false;
			if (tokens.get(predicate.getValue().getHead_token()).getPos().equals("noun"))
				isNounAction = Utils.isNounAction(tokens.get(predicate.getValue().getHead_token()).getWn(),
						tokens.get(predicate.getValue().getHead_token()).getLemma());
			if (tokens.get(predicate.getValue().getHead_token()).getPos().equals("verb") || isNounAction) {
				Patient patient = null;
				Agent agent = null;
				Action action = new Action(predicate.getKey(), predicate.getValue().getWords(),
						tokens.get(predicate.getKey()).getBegin(), tokens.get(predicate.getKey()).getEnd());
				if (predicate.getValue().getArgumentA0() != null) {
					for (Entry<String, Agent> agentTmp : agentsList.entrySet()) {
						if (agentTmp.getValue().getMentions()
								.get(predicate.getValue().getArgumentA0().getHead_token()) != null) {
							agent = agentTmp.getValue();
							break;
						} else if (agentTmp.getKey().equals(predicate.getValue().getArgumentA0().getHead_token())) {
							agent = agentTmp.getValue();
							break;
						}
					}
				}
				if (predicate.getValue().getA1PlusArguments().size() > 0) {
					for (Entry<String, Patient> patientTmp : patientsList.entrySet()) {
						if (predicate.getValue().getA1PlusArguments().containsKey(patientTmp.getKey())) {
							patient = patientTmp.getValue();
							break;
						}
					}
				}
				Boolean toAdd = true;
				// Remove actions where the actor is itself
				if (predicate.getValue().getArgumentA0() != null)
					if (predicate.getValue().getArgumentA0().getHead_token()
							.equals(predicate.getValue().getHead_token()))
						toAdd = false;
				// Remove actions where the object is itself
				if (toAdd)
					for (Entry<String, PredicateArgument> argument : predicate.getValue().getA1PlusArguments()
							.entrySet())
						if (argument.getKey().equals(predicate.getValue().getHead_token()))
							toAdd = false;
				// Remove action(noun) if this is object of another predicate
				if (toAdd && isNounAction) {
					if (predicate.getValue().getA1PlusArguments().size() > 0)
						for (Entry<String, Predicate> predicateTmp : predicates.entrySet()) {
							for (Entry<String, PredicateArgument> argument : predicateTmp.getValue()
									.getA1PlusArguments().entrySet()) {
								if (argument.getKey().equals(predicate.getValue().getHead_token())) {
									toAdd = false;
									break;
								}
							}
						}
				}
				if (patient != null && toAdd) {
					Activity activity = new Activity(predicate.getKey(), agent, action, patient);
					activitiesList.put(predicate.getKey(), activity);
				}
			}
		}
		return activitiesList;
	}

	public LinkedHashMap<String, Activity> getActivitiesList() {
		return activitiesList;
	}

	public void setActivitiesList(LinkedHashMap<String, Activity> activitiesList) {
		this.activitiesList = activitiesList;
	}

}
