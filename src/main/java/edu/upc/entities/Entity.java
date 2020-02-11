package edu.upc.entities;

import java.util.LinkedHashMap;

public class Entity {
	String id;
	String lemma;
	LinkedHashMap<String, Mention> mentions;

	public Entity() {
		mentions = new LinkedHashMap<String, Mention>();
	}

	public Entity(String id, String lemma, LinkedHashMap<String, Mention> mentions) {
		super();
		this.id = id;
		this.lemma = lemma;
		this.mentions = mentions;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getLemma() {
		return lemma;
	}

	public void setLemma(String lemma) {
		this.lemma = lemma;
	}

	public LinkedHashMap<String, Mention> getMentions() {
		return mentions;
	}

	public void setMentions(LinkedHashMap<String, Mention> mentions) {
		this.mentions = mentions;
	}

}
