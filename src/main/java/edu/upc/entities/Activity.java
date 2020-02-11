package edu.upc.entities;

import java.util.ArrayList;

import edu.upc.utils.ActionType;

public class Activity {

	private String id;
	private Agent agent;
	private Patient patient;
	private Action action;
	private ActionType role;
	private String pattern;
	private ArrayList<String> sequenceOrigins;
	private ArrayList<String> sequenceDestinations;
	private ArrayList<String> conflictOrigins;
	private ArrayList<String> conflictDestinations;

	public Activity(String id, Action action) {
		super();
		this.id = id;
		this.role = null;
		this.agent = null;
		this.patient = null;
		this.action = action;
		this.pattern = null;
		this.sequenceOrigins = new ArrayList<String>();
		this.sequenceDestinations = new ArrayList<String>();
		this.conflictOrigins = new ArrayList<String>();
		this.conflictDestinations = new ArrayList<String>();
	}

	public Activity(String id, Agent agent, Action action, Patient patient) {
		super();
		this.id = id;
		this.role = ActionType.ACTION;
		this.agent = agent;
		this.patient = patient;
		this.action = action;
		this.pattern = null;
		this.sequenceOrigins = new ArrayList<String>();
		this.sequenceDestinations = new ArrayList<String>();
		this.conflictOrigins = new ArrayList<String>();
		this.conflictDestinations = new ArrayList<String>();
	}

	@Override
	public String toString() {
		return "Activity [id=" + id + ", Agent=" + agent + ", Action=" + action + ", Patient=" + patient + ", pattern="
				+ pattern + ", Role=" + role + ", SequenceOrigin=" + sequenceOrigins + ", SequenceDestination="
				+ sequenceDestinations + ", ConflicOrigin=" + conflictOrigins + ",ConflictDestination="
				+ conflictDestinations + "]";
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public ActionType getRole() {
		return role;
	}

	public void setRole(ActionType role) {
		this.role = role;
	}

	public Agent getAgent() {
		return agent;
	}

	public void setAgent(Agent agent) {
		this.agent = agent;
	}

	public Patient getPatient() {
		return patient;
	}

	public void setPatient(Patient patient) {
		this.patient = patient;
	}

	public Action getAction() {
		return action;
	}

	public void setAction(Action action) {
		this.action = action;
	}

	public String getPattern() {
		return pattern;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	public ArrayList<String> getSequenceOrigins() {
		return sequenceOrigins;
	}

	public void setSequenceOrigins(ArrayList<String> sequenceOrigins) {
		this.sequenceOrigins = sequenceOrigins;
	}

	public ArrayList<String> getSequenceDestinations() {
		return sequenceDestinations;
	}

	public void setSequenceDestinations(ArrayList<String> sequenceDestinations) {
		this.sequenceDestinations = sequenceDestinations;
	}

	public ArrayList<String> getConflictOrigins() {
		return conflictOrigins;
	}

	public void setConflictOrigins(ArrayList<String> conflictOrigins) {
		this.conflictOrigins = conflictOrigins;
	}

	public ArrayList<String> getConflictDestinations() {
		return conflictDestinations;
	}

	public void setConflictDestinations(ArrayList<String> conflictDestinations) {
		this.conflictDestinations = conflictDestinations;
	}

}
