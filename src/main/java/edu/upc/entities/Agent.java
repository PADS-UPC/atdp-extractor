package edu.upc.entities;

import java.util.LinkedHashMap;

public class Agent {
	private String id;
	private String completeText;
	private String shortText;
	private LinkedHashMap<String, Mention> mentions;
	private Integer begin;
	private Integer end;

	public Agent() {
		super();
		this.id = null;
		this.completeText = null;
		this.shortText = null;
		this.mentions = new LinkedHashMap<String, Mention>();
	}

	public Agent(String id, String completeText, Integer begin, Integer end) {
		super();
		this.id = id;
		this.completeText = completeText;
		this.begin = begin;
		this.end = end;
		this.shortText = null;
		this.mentions = new LinkedHashMap<String, Mention>();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCompleteText() {
		return completeText;
	}

	public void setCompleteText(String completeText) {
		this.completeText = completeText;
	}

	public String getShortText() {
		return shortText;
	}

	public void setShortText(String shortText) {
		this.shortText = shortText;
	}

	public LinkedHashMap<String, Mention> getMentions() {
		return mentions;
	}

	public void setMentions(LinkedHashMap<String, Mention> mentions) {
		this.mentions = mentions;
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

	@Override
	public String toString() {
		return "Agent: [id:" + this.id + ", ShortText:" + this.shortText + ", CompleteText: " + this.completeText + "]";
	}

}
