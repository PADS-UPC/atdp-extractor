package edu.upc.handler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import edu.stanford.nlp.trees.Tree;
import edu.upc.entities.Activity;
import edu.upc.entities.Token;
import edu.upc.utils.FilesUrl;
import edu.upc.utils.Utils;

public class TreesHandler {
	private LinkedHashMap<String, Token> tokens;
	private LinkedHashMap<String, Activity> activitiesList;

	public TreesHandler(LinkedHashMap<String, Activity> activitiesList, LinkedHashMap<String, Token> tokens) {
		super();
		this.activitiesList = activitiesList;
		this.tokens = tokens;
	}

	public void addObjectsToTreeNodes(List<Tree> trees) {
		for (Tree treeOfOneSentence : trees) {
			for (Entry<String, Activity> activity : activitiesList.entrySet()) {
				addRoleArgumentToTreeNode(activity.getValue(), treeOfOneSentence);
			}
		}
	}

	private void addRoleArgumentToTreeNode(Activity activity, Tree tree) {
		String node = tree.value();
		String newNode = Utils.makeNode(activity.getAction().getId(), tokens);
		if (node.equals(newNode)) {
			if (activity.getPatient() != null) {
				newNode = tree.value() + activity.getRole() + ":" + activity.getPatient().getId() + Utils.separator;
				tree.setValue(newNode);
			}
		}
		Iterator<?> treeIterator = tree.getChildrenAsList().iterator();
		while (treeIterator.hasNext()) {
			Tree tree2 = (Tree) treeIterator.next();
			addRoleArgumentToTreeNode(activity, tree2);
		}
	}

	public void removeObjectsToTreeNodes(List<Tree> trees) {
		for (Tree treeOfOneSentence : trees) {
			removeRoleArgumentToTreeNode(treeOfOneSentence);
		}
	}

	private void removeRoleArgumentToTreeNode(Tree tree) {
		String node = tree.value();
		if (Utils.isActivity(node)) {
			String simpleNode = Utils.removeObject(node);
			tree.setValue(simpleNode);
		}
		Iterator<?> treeIterator = tree.getChildrenAsList().iterator();
		while (treeIterator.hasNext()) {
			Tree tree2 = (Tree) treeIterator.next();
			removeRoleArgumentToTreeNode(tree2);
		}
	}

	public static void refreshTree(List<Tree> trees, LinkedHashMap<String, Activity> activitiesList,
			LinkedHashMap<String, Token> tokens) throws IOException {
		TreesHandler treesHandler = new TreesHandler(activitiesList, tokens);
		treesHandler.removeObjectsToTreeNodes(trees);
		treesHandler.addObjectsToTreeNodes(trees);
		//Files.write(Paths.get(FilesUrl.TEMPORAL_TREE.toString()), trees.toString().getBytes());

	}
}
