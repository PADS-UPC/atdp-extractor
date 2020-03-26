package edu.upc.entities;

import java.util.ArrayList;

public class Sentence {

	private String id;
	private String text;
	private Integer begin;
	private Integer end;
	private ArrayList<Token> tokens;
	private ArrayList<Activity> activities;

	public Sentence() {
		super();
		this.tokens = new ArrayList<Token>();
		this.setActivities(new ArrayList<Activity>());
	}

	public Sentence(String id, ArrayList<Token> tokens) {
		super();
		this.id = id;
		this.tokens = tokens;
		this.setActivities(new ArrayList<Activity>());
	}

	public Sentence(String id, String text, Integer begin, Integer end, ArrayList<Token> tokens) {
		super();
		this.id = id;
		this.text = text;
		this.begin = begin;
		this.end = end;
		this.tokens = tokens;
		this.setActivities(new ArrayList<Activity>());
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public ArrayList<Token> getTokens() {
		return tokens;
	}

	public void setTokens(ArrayList<Token> tokens) {
		this.tokens = tokens;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public Integer getEnd() {
		return end;
	}

	public void setEnd(Integer end) {
		this.end = end;
	}

	public Integer getBegin() {
		return begin;
	}

	public void setBegin(Integer begin) {
		this.begin = begin;
	}

	public ArrayList<Activity> getActivities() {
		return activities;
	}

	public void setActivities(ArrayList<Activity> activities) {
		this.activities = activities;
	}

}
