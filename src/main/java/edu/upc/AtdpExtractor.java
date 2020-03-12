package edu.upc;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.json.simple.parser.ParseException;

import edu.upc.handler.AtdpHandler;
import edu.upc.handler.ParserHandler;
import edu.upc.utils.FoldersUrl;
import edu.upc.utils.Utils;

public class AtdpExtractor {

	private static ParserHandler parser;

	private static File textFolder = new File(FoldersUrl.INPUT_TEXTS_FOLDER.toString());
	private static File atdpFolder = new File(FoldersUrl.OUTPU_ATDP_TFOLDER.toString());
	private static File treeFolder = new File(FoldersUrl.TREE_FOLDER.toString());

	public static void main(String[] args) throws IOException, ParseException {
		if (args.length != 0) {
			for (File path : textFolder.listFiles()) {
				String nlpTextFilePath = path.toString();
				String justNameOfFile = Utils.getFileNameWithoutExtension(new File(nlpTextFilePath));
				parseText(nlpTextFilePath);
				AtdpHandler annotationHandler = new AtdpHandler(parser.getTokens(),
						parser.getActivitiesList(), parser.getTrees());
				String annotationText = annotationHandler.getAnnotationTextFromActivities();
				System.out.println("----------------------------ATDP-----------------------------");
				System.out.println(annotationText);
				System.out.println("-------------------------------------------------------------");
				Files.write(Paths.get(atdpFolder + "/" + justNameOfFile + ".ann"), annotationText.getBytes());
				System.out.println("> ATDP: " + atdpFolder + "/" + justNameOfFile + ".ann");
				Files.write(Paths.get(treeFolder + "/" + justNameOfFile + ".trx"),
						parser.getTrees().toString().getBytes());
				System.out.println("> Tree: " + treeFolder + "/" + justNameOfFile + ".trx");
			}
		} else {
			String nlpTextFilePath = args[0];
			String justNameOfFile = Utils.getFileNameWithoutExtension(new File(nlpTextFilePath));
			parseText(nlpTextFilePath);
			AtdpHandler atdp = new AtdpHandler(parser.getTokens(), parser.getActivitiesList(),
					parser.getTrees());
			String annotationText = atdp.getAnnotationTextFromActivities();
			System.out.println("---------------------ATDP-------------------------");
			System.out.println(annotationText);
			System.out.println("--------------------------------------------------");
			Files.write(Paths.get(atdpFolder + "/" + justNameOfFile + ".ann"), annotationText.getBytes());
			System.out.println("> ATDP: " + atdpFolder + "/" + justNameOfFile + ".ann");
			Files.write(Paths.get(treeFolder + "/" + justNameOfFile + ".trx"),
					parser.getTrees().toString().getBytes());
			System.out.println("> Tree: " + treeFolder + "/" + justNameOfFile + ".trx");
		}

		System.out.println("-> DONE!. Check output folder");
	}

	private static void parseText(String nlpTextFilePath) throws ParseException, IOException {
		System.out.println("------------------- TEXT -------------------------");
		System.out.println(Utils.readFile(nlpTextFilePath));
		System.out.println("--------------------------------------------------");
		System.out.println("Parcing...");
		FreelingConnection freelingConection = new FreelingConnection();
		String freelingJsonString = freelingConection.getJsonString(Utils.readFile(nlpTextFilePath));
		parser = new ParserHandler(freelingJsonString);

	}

}
