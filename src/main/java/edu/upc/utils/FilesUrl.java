package edu.upc.utils;

public enum FilesUrl {
	EVENT_PATTERNS_FILE("input/patterns/event.txt"),
	CONDITION_PATTERNS_FILE("input/patterns/condition.txt"),
	NEW_CONDITION_PATTERNS_FILE("input/patterns/newcondition.txt"), 
	REMOVE_ACTIVITY_FILE("input/patterns/removeactivity.txt"),
	ACTIVITY_PATTERNS_FILE("input/patterns/activity.txt"),
	SEQUENCE_PATTERNS_FILE("input/patterns/sequence.txt"),
	CONFLICT_PATTERNS_FILE("input/patterns/conflict.txt"),
	FREELING_CACHE_FILE("freeling/cache/freeling-cache"),
	FREEELING_NOUN_CONFIG_FILE("freeling/config/pred-nom.dat"),
	ACTIONS_CSV_FILE("output/temporal/actions.csv"),
	TEMPORAL_TREE("output/tree/temporal.trx");

	private String description;

	private FilesUrl(String description) {
		this.description = description;
	}

	@Override
	public String toString() {
		return description;
	}
}
