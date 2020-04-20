package edu.upc;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.json.simple.parser.ParseException;

import edu.upc.atdputils.FoldersUrl;
import edu.upc.freelingutils.FreelingUtils;
import edu.upc.handler.AtdpHandler;

public class AtdpExtractor {

	private static File textFolder = new File(FoldersUrl.TEXTS_INPUT_FOLDER.toString());
	private static File atdpFolder = new File(FoldersUrl.OUTPUT_ATDP_TFOLDER.toString());
	private static File treeFolder = new File(edu.upc.freelingutils.FoldersUrl.TREE_FOLDER.toString());

	public static void main(String[] args) throws IOException, ParseException {
		if (args.length == 0) {
			for (File path : textFolder.listFiles()) {
				String nlpTextFilePath = path.toString();
				String justNameOfFile = FreelingUtils.getFileNameWithoutExtension(new File(nlpTextFilePath));
				AtdpHandler annotationHandler = new AtdpHandler(FreelingUtils.readFile(nlpTextFilePath), "all");
				String annotationText = annotationHandler.getAnnotationTextFromActivities();
				System.out.println("----------------------------ATDP-----------------------------");
				System.out.println(annotationText);
				System.out.println("-------------------------------------------------------------");
				Files.write(Paths.get(atdpFolder + "/" + justNameOfFile + ".ann"), annotationText.getBytes());
				System.out.println("> ATDP: " + atdpFolder + "/" + justNameOfFile + ".ann");
				Files.write(Paths.get(treeFolder + "/" + justNameOfFile + ".trx"),
						annotationHandler.getTrees().toString().getBytes());
				System.out.println("> Tree: " + treeFolder + "/" + justNameOfFile + ".trx");
			}
		} else {
			String nlpTextFilePath = args[0];
			String justNameOfFile = FreelingUtils.getFileNameWithoutExtension(new File(nlpTextFilePath));
			AtdpHandler annotationHandler = new AtdpHandler(FreelingUtils.readFile(nlpTextFilePath), "all");
			String annotationText = annotationHandler.getAnnotationTextFromActivities();
			System.out.println("---------------------ATDP-------------------------");
			System.out.println(annotationText);
			System.out.println("--------------------------------------------------");
			Files.write(Paths.get(atdpFolder + "/" + justNameOfFile + ".ann"), annotationText.getBytes());
			System.out.println("> ATDP: " + atdpFolder + "/" + justNameOfFile + ".ann");
			Files.write(Paths.get(treeFolder + "/" + justNameOfFile + ".trx"),
					annotationHandler.getTrees().toString().getBytes());
			System.out.println("> Tree: " + treeFolder + "/" + justNameOfFile + ".trx");
		}

		System.out.println("-> DONE!. Check output folder");
	}

}
