package edu.upc.utils;

public enum FilesUrl {
	EVENT_PATTERNS_FILE("input/patterns/event.txt"),
	CONDITION_PATTERNS_FILE("input/patterns/condition.txt"),
	CONDITION_SIMPLE_PATTERNS_FILE("input/patterns/conditionsimple.txt"),
	REMOVE_ACTIVITY_FILE("input/patterns/removeactivity.txt"),
	ACTIVITY_PATTERNS_FILE("input/patterns/activity.txt"),
	SEQUENCE_PATTERNS_FILE("input/patterns/sequence.txt"),
	CONFLICT_PATTERNS_FILE("input/patterns/conflict.txt"),
	FREELING_CACHE_FILE(FoldersUrl.FREELING_FOLDER+"cache/freeling-cache"),
	FREEELING_NOUN_CONFIG_FILE(FoldersUrl.FREELING_FOLDER+"config/pred-nom.dat"),
	FREEELING_NOUN_TO_ADD_CONFIG_FILE(FoldersUrl.FREELING_FOLDER+"config/pred-nom-add.dat"),
	ACTIONS_CSV_FILE("output/temporal/actions.csv"),
	TEMPORAL_TREE("output/tree/temporal.trx"),
	REMOVE_UNDER_CONDITION_PATTERNS_FILE("input/patterns/removeactionsunderconditions.txt"),
	WEAK_VERBS_PATTERNS_FILE("input/patterns/weakverbs.txt"),
	APPLY_PATTERNS_FILE("input/patterns/applypatterns.txt"),
	MANDATORY_PATTERNS_FILE("input/patterns/mandatory.txt"),
	TOREVERSE_PATTERNS_FILE("input/patterns/toreverse.txt");

	private String description;

	private FilesUrl(String description) {
		this.description = description;
	}

	@Override
	public String toString() {
		return description;
	}
}
