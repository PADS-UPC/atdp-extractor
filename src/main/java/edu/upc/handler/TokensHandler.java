package edu.upc.handler;

import java.util.Iterator;
import java.util.LinkedHashMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import edu.upc.entities.Token;

public class TokensHandler {
	private LinkedHashMap<String, Token> tokens;

	public TokensHandler() {
		super();
		tokens = new LinkedHashMap<String, Token>();
	}

	public LinkedHashMap<String, Token> parseTokens(JSONObject jsonObject) {
		JSONArray tokensArray = (JSONArray) jsonObject.get("tokens");
		if (tokensArray != null) {
			Iterator<?> tokensIterator = tokensArray.iterator();
			while (tokensIterator.hasNext()) {
				jsonObject = (JSONObject) tokensIterator.next();
				Token token = new Token();
				token.setId(jsonObject.get("id").toString());
				token.setForm(jsonObject.get("form").toString());
				token.setLemma(jsonObject.get("lemma").toString());
				token.setTag(jsonObject.get("tag").toString());
				token.setCtag(jsonObject.get("ctag").toString());
				if (jsonObject.get("pos") != null)
					token.setPos(jsonObject.get("pos").toString());
				token.setBegin(Integer.parseInt(jsonObject.get("begin").toString()));
				token.setEnd(Integer.parseInt(jsonObject.get("end").toString()));
				if (jsonObject.get("wn") != null)
					token.setWn(jsonObject.get("wn").toString());
				tokens.put(jsonObject.get("id").toString(), token);
			}
			return tokens;
		} else
			System.err.println("tokens Not Found");
		return null;
	}

}
