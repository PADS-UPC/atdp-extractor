package edu.upc.entities;

public class Mention {
	String id;
	String words;

	public Mention() {
		super();
	}

	public Mention(String id, String words) {
		super();
		this.id = id;
		this.words = words;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getWords() {
		return words;
	}

	public void setWords(String words) {
		this.words = words;
	}

}
