package edu.upc.atdputils;

public enum FoldersUrl {
	TEXTS_INPUT_FOLDER(System.getProperty("user.home") + "/doc2bpmnutils/input/texts/"), 
	OUTPUT_ATDP_TFOLDER(System.getProperty("user.home") + "/doc2bpmnutils/output/atdp/"),
	JUDGEANNOTATION_FOLDER(System.getProperty("user.home") + "/doc2bpmnutils/input/judgeannotations/"),
	CSV_INPUT_FOLDER(System.getProperty("user.home") + "/doc2bpmnutils/input/csv/"),
	CSV_OUTPUT_FOLDER(System.getProperty("user.home") + "/doc2bpmnutils/output/csv/"),;

	private String description;

	private FoldersUrl(String description) {
		this.description = description;
	}

	@Override
	public String toString() {
		return description;
	}
}
