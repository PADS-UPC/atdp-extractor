package edu.upc.entities;

public class Patient {

	private String id;
	private String completeText;
	private String shortText;
	private Integer begin;
	private Integer end;

	public Patient() {
		super();
	}

	public Patient(String id, String completeText, String shortText, Integer begin, Integer end) {
		super();
		this.id = id;
		this.completeText = completeText;
		this.shortText = shortText;
		this.begin = begin;
		this.end = end;
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

	public Integer getBegin() {
		return begin;
	}

	public void setBegin(Integer begin) {
		this.begin = begin;
	}

	public Integer getEnd() {
		return end;
	}

	public void setEnd(Integer end) {
		this.end = end;
	}

	@Override
	public String toString() {
		return "Patient: [id:" + this.id + ", ShortText:" + this.shortText + ", CompleteText: " + this.completeText
				+ "]";
	}
}
