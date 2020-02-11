package edu.upc.handler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import org.json.simple.parser.ParseException;

import edu.stanford.nlp.trees.Tree;
import edu.upc.entities.Activity;
import edu.upc.entities.Agent;
import edu.upc.entities.Entity;
import edu.upc.entities.Patient;
import edu.upc.entities.Predicate;
import edu.upc.entities.Token;

public class ParserHandler {

	private LinkedHashMap<String, Token> tokens;
	private LinkedHashMap<String, Predicate> predicates;
	private LinkedHashMap<String, Entity> entities;
	private List<Tree> trees;
	private LinkedHashMap<String, Agent> actorsList;
	private LinkedHashMap<String, Patient> patientsList;
	private LinkedHashMap<String, Activity> activitiesList;
	private String jsonString;

	public ParserHandler(String jsonString) throws ParseException, IOException {
		this.tokens = new LinkedHashMap<String, Token>();
		this.predicates = new LinkedHashMap<String, Predicate>();
		this.entities = new LinkedHashMap<String, Entity>();
		this.trees = new ArrayList<Tree>();
		this.actorsList = new LinkedHashMap<String, Agent>();
		this.patientsList = new LinkedHashMap<String, Patient>();
		this.activitiesList = new LinkedHashMap<String, Activity>();
		this.jsonString = jsonString;

		parceAll();
	}

	private void parceAll() throws ParseException, IOException {
		ParagraphHandler paragraphHandler = new ParagraphHandler(jsonString);
		paragraphHandler.parceparagraphs();
		this.tokens = paragraphHandler.getTokens();
		this.predicates = paragraphHandler.getPredicates();
		this.entities = paragraphHandler.getEntities();
		this.trees = paragraphHandler.getTrees();

		AgentHandler actorsHandler = new AgentHandler(trees, tokens, predicates, entities);
		actorsList = actorsHandler.getActorsList();

		PatientHandler patientsHandler = new PatientHandler(trees, tokens, predicates);
		patientsList = patientsHandler.getPatientsList();

		ActivitiesHandler activitiesHandler = new ActivitiesHandler(predicates, tokens, actorsList, patientsList);
		activitiesList = activitiesHandler.getActivitiesList();

	}

	public LinkedHashMap<String, Token> getTokens() {
		return tokens;
	}

	public void setTokens(LinkedHashMap<String, Token> tokens) {
		this.tokens = tokens;
	}

	public LinkedHashMap<String, Agent> getActorsList() {
		return actorsList;
	}

	public void setActorsList(LinkedHashMap<String, Agent> actorsList) {
		this.actorsList = actorsList;
	}

	public LinkedHashMap<String, Activity> getActivitiesList() {
		return activitiesList;
	}

	public void setActivitiesList(LinkedHashMap<String, Activity> activitiesList) {
		this.activitiesList = activitiesList;
	}

	public LinkedHashMap<String, Entity> getEntities() {
		return entities;
	}

	public void setEntities(LinkedHashMap<String, Entity> entities) {
		this.entities = entities;
	}

	public List<Tree> getTrees() {
		return trees;
	}

	public void setTrees(List<Tree> trees) {
		this.trees = trees;
	}

	public LinkedHashMap<String, Predicate> getPredicates() {
		return predicates;
	}

	public void setPredicates(LinkedHashMap<String, Predicate> predicates) {
		this.predicates = predicates;
	}

	public LinkedHashMap<String, Patient> getPatientsList() {
		return patientsList;
	}

	public void setPatientsList(LinkedHashMap<String, Patient> patientsList) {
		this.patientsList = patientsList;
	}

}
