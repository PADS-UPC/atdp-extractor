package edu.upc.utils;

public enum FilesUrl {
	EVENT_PATTERNS_FILE("input/patterns/event.txt"),
	CONDITION_PATTERNS_FILE("input/patterns/condition.txt"),
	CONDITION_SIMPLE_PATTERNS_FILE("input/patterns/conditionsimple.txt"),
	REMOVE_ACTIVITY_FILE("input/patterns/removeactivity.txt"),
	ACTIVITY_PATTERNS_FILE("input/patterns/activity.txt"),
	SEQUENCE_PATTERNS_FILE("input/patterns/sequence.txt"),
	CONFLICT_PATTERNS_FILE("input/patterns/conflict.txt"),
	FREELING_CACHE_FILE("freeling/cache/freeling-cache"),
	FREEELING_NOUN_CONFIG_FILE("freeling/config/pred-nom.dat"),
	ACTIONS_CSV_FILE("output/temporal/actions.csv"),
	TEMPORAL_TREE("output/tree/temporal.trx"),
	REMOVE_UNDER_CONDITION_PATTERNS_FILE("input/patterns/removeactionsunderconditions.txt"),
	VERBS_TO_REMOVE_PATTERNS_FILE("input/patterns/verbstoremove.txt"),
	APPLY_PATTERNS_FILE("input/patterns/applypatterns.txt");

	private String description;

	private FilesUrl(String description) {
		this.description = description;
	}

	@Override
	public String toString() {
		return description;
	}
}
