package edu.upc.utils;

public enum ActionType {
	EVENT("EVENT"), 
	CONDITION("CONDITION"),
	ACTION("ACTION"), 
	ACTIVITY("ACTIVITY");

	private final String description;

	ActionType(String description) {
		this.description = description;
	}

	@Override
	public String toString() {
		return description;
	}

}
