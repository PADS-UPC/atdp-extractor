package edu.upc.entities;

import java.util.ArrayList;

public class Token {
	// From Token (Freeling Json)
	String id; // t1.1
	String tag; // VBZ
	String ctag; // VBZ
	String form; // wants
	String lemma; // want
	String pos; // noun
	Integer begin;
	Integer end;
	// From dependencies
	String function; // ROOT, SBJ
	// From predicate
	String parent; // t1.2
	String wn; // 80000953-n

	ArrayList<String> children = new ArrayList<String>(); // t1.3, t1.4

	public Token(String id, String function, String form, String lemma, String parent, String pos,
			ArrayList<String> children) {
		super();
		this.id = id;
		this.function = function;
		this.form = form;
		this.lemma = lemma;
		this.parent = parent;
		this.pos = pos;
		this.children = children;
		this.begin = 0;
		this.end = 0;
	}

	public Token() {

	}

	public void putChild(String child) {
		this.children.add(child);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getFunction() {
		return function;
	}

	public void setFunction(String function) {
		this.function = function;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public String getCtag() {
		return ctag;
	}

	public void setCtag(String ctag) {
		this.ctag = ctag;
	}

	public String getForm() {
		return form;
	}

	public void setForm(String form) {
		this.form = form;
	}

	public String getLemma() {
		return lemma;
	}

	public void setLemma(String lemma) {
		this.lemma = lemma;
	}

	public String getParent() {
		return parent;
	}

	public void setParent(String parent) {
		this.parent = parent;
	}

	public ArrayList<String> getChildren() {
		return children;
	}

	public void setChildren(ArrayList<String> children) {
		this.children = children;
	}

	public String getPos() {
		return pos;
	}

	public void setPos(String pos) {
		this.pos = pos;
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

	public String getWn() {
		return wn;
	}

	public void setWn(String wn) {
		this.wn = wn;
	}

	@Override
	public String toString() {

		return "Token [id=" + id + ", function=" + function + ", tag=" + tag + ", ctag=" + ctag + ", form=" + form
				+ ", lemma=" + lemma + ", parent=" + parent + ", pos=" + pos + ", children=" + children + ", wn=" + wn
				+ "]\n";
	}
}
