package edu.upc.entities;

public class Action {

	private String id;
	private String word;
	private Integer begin;
	private Integer end;

	public Action() {
		super();
	}

	public Action(String id, String word, Integer begin, Integer end) {
		super();
		this.id = id;
		this.word = word;
		this.begin = begin;
		this.end = end;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
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

	public String getWord() {
		return word;
	}

	public void setWord(String word) {
		this.word = word;
	}

	@Override
	public String toString() {
		return "Action: [id:" + this.id + ", Word:" + this.word + "]";
	}
}
