package edu.upc.entities;

public class PredicateArgument {
	String from;
	String head_token;
	String role;
	String to;
	String words;

	public PredicateArgument() {

	}

	public PredicateArgument(String from, String head_token, String role, String to, String words) {
		super();
		this.from = from;
		this.head_token = head_token;
		this.role = role;
		this.to = to;
		this.words = words;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getHead_token() {
		return head_token;
	}

	public void setHead_token(String head_token) {
		this.head_token = head_token;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public String getWords() {
		return words;
	}

	public void setWords(String words) {
		this.words = words;
	}
}
