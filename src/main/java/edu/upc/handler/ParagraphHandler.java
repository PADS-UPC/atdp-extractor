package edu.upc.handler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import edu.stanford.nlp.trees.Tree;
import edu.upc.entities.Entity;
import edu.upc.entities.Mention;
import edu.upc.entities.Predicate;
import edu.upc.entities.Token;
import edu.upc.utils.Utils;

public class ParagraphHandler {

	private LinkedHashMap<String, Token> tokens;
	private LinkedHashMap<String, Predicate> predicates;
	private LinkedHashMap<String, Entity> entities;
	private List<Tree> trees;
	private LinkedHashMap<String, Mention> mentionsList;
	private JSONObject jsonResult;
	private String jsonString;

	public ParagraphHandler(String jsonString) throws ParseException, IOException {
		this.tokens = new LinkedHashMap<String, Token>();
		this.predicates = new LinkedHashMap<String, Predicate>();
		this.entities = new LinkedHashMap<String, Entity>();
		this.trees = new ArrayList<Tree>();
		this.mentionsList = new LinkedHashMap<String, Mention>();
		this.jsonResult = new JSONObject();
		this.jsonString = jsonString;
	}

	public void parceparagraphs() throws ParseException, IOException {
		this.jsonResult = (JSONObject) new JSONParser().parse(jsonString);
		JSONObject jsonObject = (JSONObject) new JSONParser().parse(jsonString);
		JSONArray paragraps = (JSONArray) jsonObject.get("paragraphs");
		if (paragraps != null) {
			Iterator<?> paragraphsIterator = paragraps.iterator();
			while (paragraphsIterator.hasNext()) {
				jsonObject = (JSONObject) paragraphsIterator.next();
				parseSentences(jsonObject);
			}
		} else
			System.err.println("Paragaphs Not Found");

		jsonObject = (JSONObject) new JSONParser().parse(jsonString);
		jsonObject = (JSONObject) jsonObject.get("semantic_graph");
		if (jsonObject != null) {
			parseEntities(jsonObject);
		}
	}

	private void parseSentences(JSONObject jsonObject) throws ParseException, IOException {
		JSONArray sentencesArray = (JSONArray) jsonObject.get("sentences");
		if (sentencesArray != null) {
			Iterator<?> sentencesIterator = sentencesArray.iterator();
			while (sentencesIterator.hasNext()) {
				jsonObject = (JSONObject) sentencesIterator.next();
				TokensHandler tokensHandler = new TokensHandler();
				LinkedHashMap<String, Token> tokensTmp = new LinkedHashMap<String, Token>();
				tokensTmp = tokensHandler.parseTokens(jsonObject);
				if (tokensTmp != null)
					tokens.putAll(tokensTmp);

				LinkedHashMap<String, Predicate> predicatesTmp = new LinkedHashMap<String, Predicate>();
				PredicateHandler predicateHandler = new PredicateHandler(tokens);
				predicatesTmp = predicateHandler.parsePredicates(jsonObject);
				if (predicatesTmp != null) {
					for (Entry<String, Token> token : tokens.entrySet()) {
						if(!predicatesTmp.containsKey(token.getKey()))
						if (Utils.isNounActionToAdd(token.getValue().getWn(), token.getValue().getLemma(), true)
								&& !predicatesTmp.containsKey(token.getKey())) {
							Predicate predicate = new Predicate();
							predicate.setId(token.getKey());
							predicate.setHead_token(token.getKey());
							predicate.setSense(token.getValue().getLemma());
							predicate.setWords(token.getValue().getLemma());
							predicatesTmp.put(token.getKey(), predicate);
						}
					}

					predicates.putAll(predicatesTmp);
				}

				DependeciesTreeHandler dependenciesHandler = new DependeciesTreeHandler(tokens, predicatesTmp);
				List<Tree> treesTmp = null;
				treesTmp = dependenciesHandler.parseDependencies(jsonObject);
				trees.addAll(treesTmp);
			}
		} else
			System.err.println("sentences Not Found");

	}

	private void parseEntities(JSONObject jsonObject) {
		JSONArray entityArray = (JSONArray) jsonObject.get("entities");
		if (entityArray != null) {
			Iterator<?> entityIterator = entityArray.iterator();
			while (entityIterator.hasNext()) {
				jsonObject = (JSONObject) entityIterator.next();
				Entity entity = new Entity();
				entity.setId(jsonObject.get("id").toString());
				entity.setLemma(jsonObject.get("lemma").toString());
				entities.put(jsonObject.get("id").toString(), entity);
				parseEntityMention(jsonObject.get("id").toString(), jsonObject);
			}
		} else
			System.err.println("Entities Not Found");
	}

	private void parseEntityMention(String id, JSONObject jsonObject) {
		JSONArray mentionArray = (JSONArray) jsonObject.get("mentions");
		if (mentionArray != null) {
			Iterator<?> mentionIterator = mentionArray.iterator();
			LinkedHashMap<String, Mention> mentions = new LinkedHashMap<String, Mention>();
			while (mentionIterator.hasNext()) {
				jsonObject = (JSONObject) mentionIterator.next();
				mentions.put(jsonObject.get("id").toString(),
						new Mention(jsonObject.get("id").toString(), jsonObject.get("words").toString()));
				mentionsList.put(jsonObject.get("id").toString(),
						new Mention(jsonObject.get("id").toString(), jsonObject.get("words").toString()));
			}
			entities.get(id).setMentions(mentions);
		} else
			System.out.println("mentions Not Found");
	}

	public LinkedHashMap<String, Token> getTokens() {
		return tokens;
	}

	public void setTokens(LinkedHashMap<String, Token> tokens) {
		this.tokens = tokens;
	}

	public LinkedHashMap<String, Entity> getEntities() {
		return entities;
	}

	public void setEntities(LinkedHashMap<String, Entity> entities) {
		this.entities = entities;
	}

	public List<Tree> getTrees() {
		return trees;
	}

	public void setTrees(List<Tree> trees) {
		this.trees = trees;
	}

	public JSONObject getJsonResult() {
		return jsonResult;
	}

	public void setJsonResult(JSONObject jsonResult) {
		this.jsonResult = jsonResult;
	}

	public String getJsonString() {
		return jsonString;
	}

	public void setJsonString(String jsonString) {
		this.jsonString = jsonString;
	}

	public LinkedHashMap<String, Predicate> getPredicates() {
		return predicates;
	}

	public void setPredicates(LinkedHashMap<String, Predicate> predicates) {
		this.predicates = predicates;
	}

	public LinkedHashMap<String, Mention> getMentionsList() {
		return mentionsList;
	}

	public void setMentionsList(LinkedHashMap<String, Mention> mentionsList) {
		this.mentionsList = mentionsList;
	}
}
