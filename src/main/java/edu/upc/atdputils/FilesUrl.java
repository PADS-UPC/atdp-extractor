package edu.upc.atdputils;

import edu.upc.freelingutils.FoldersUrl;

public enum FilesUrl {
	ACTIONS_CSV_FILE(FoldersUrl.OUTPUT_FOLDER+"temporal/actions.csv"),
	MANDATORY_PATTERNS_FILE(FoldersUrl.INPUT_FOLDER+"patterns/mandatory.txt"),
	TOREVERSE_PATTERNS_FILE(FoldersUrl.INPUT_FOLDER+"patterns/toreverse.txt");

	private String description;

	private FilesUrl(String description) {
		this.description = description;
	}

	@Override
	public String toString() {
		return description;
	}
}
