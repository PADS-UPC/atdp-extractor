package edu.upc.entities;

import java.util.LinkedHashMap;

public class Predicate {
	String id;
	String head_token;
	String sense;
	String words;
	PredicateArgument argumentA0;
	LinkedHashMap<String, PredicateArgument> a1PlusArguments;

	public Predicate() {
		super();
		argumentA0=null;
		a1PlusArguments=new LinkedHashMap<String, PredicateArgument>();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getHead_token() {
		return head_token;
	}

	public void setHead_token(String head_token) {
		this.head_token = head_token;
	}

	public String getSense() {
		return sense;
	}

	public void setSense(String sense) {
		this.sense = sense;
	}

	public String getWords() {
		return words;
	}

	public void setWords(String words) {
		this.words = words;
	}

	public PredicateArgument getArgumentA0() {
		return argumentA0;
	}

	public void setArgumentA0(PredicateArgument argumentA0) {
		this.argumentA0 = argumentA0;
	}

	public LinkedHashMap<String, PredicateArgument> getA1PlusArguments() {
		return a1PlusArguments;
	}

	public void setA1PlusArguments(LinkedHashMap<String, PredicateArgument> a1PlusArguments) {
		this.a1PlusArguments = a1PlusArguments;
	}
}
