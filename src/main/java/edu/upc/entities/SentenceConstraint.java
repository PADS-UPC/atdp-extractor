package edu.upc.entities;

import java.util.ArrayList;

public class SentenceConstraint {
	private Integer id;
	private String sentenceText;
	boolean isNegative;
	private ArrayList<Constraint> constrains;

	public SentenceConstraint() {
		this.id = 0;
		this.sentenceText = null;
		this.isNegative = false;
		this.constrains = new ArrayList<Constraint>();
	}

	public SentenceConstraint(Integer id, String sentenceText, boolean isNegative) {
		super();
		this.id = id;
		this.sentenceText = sentenceText;
		this.isNegative = isNegative;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getSentenceText() {
		return sentenceText;
	}

	public void setSentenceText(String sentenceText) {
		this.sentenceText = sentenceText;
	}

	public boolean isNegative() {
		return isNegative;
	}

	public void setNegative(boolean isNegative) {
		this.isNegative = isNegative;
	}

	public ArrayList<Constraint> getConstrains() {
		return constrains;
	}

	public void setConstrains(ArrayList<Constraint> constrains) {
		this.constrains = constrains;
	}

	@Override
	public String toString() {
		return "isNegative= " + isNegative+ ", constrains: " + constrains;
	}

}
