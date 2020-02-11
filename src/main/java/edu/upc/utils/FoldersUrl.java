package edu.upc.utils;

public enum FoldersUrl {
	INPUT_TEXTS_FOLDER("input/texts/"), 
	OUTPU_ATDP_TFOLDER("output/atdp/"),
	TREE_FOLDER("output/tree/"),
	JUDGEANNOTATION_FOLDER("input/judgeannotations/"),
	FREELING_FOLDER("freeling/");

	private String description;

	private FoldersUrl(String description) {
		this.description = description;
	}

	@Override
	public String toString() {
		return description;
	}
}
