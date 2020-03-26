package edu.upc;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;

import org.json.simple.parser.ParseException;

import com.opencsv.CSVReader;

import edu.upc.entities.Action;
import edu.upc.entities.Activity;
import edu.upc.entities.Constraint;
import edu.upc.entities.SentenceConstraint;
import edu.upc.handler.ConstraintHandler;
import edu.upc.handler.ParserHandler;
import edu.upc.handler.PatternsHandler;
import edu.upc.utils.ActionType;
import edu.upc.utils.ConstraintType;
import edu.upc.utils.FoldersUrl;
import edu.upc.utils.Utils;

public class ConstraintExtractor {

	private static ParserHandler parser;
	static private DecimalFormat decimalFormal = new DecimalFormat("#.###");
	static private String justNameOfFile;

	public static void main(String[] args) throws ParseException, IOException {
		FileWriter csvWriter = new FileWriter(FoldersUrl.CSV_OUTPUT_FOLDER + "totalResult.csv");
		csvWriter.append("Source");
		csvWriter.append("\tGold");
		csvWriter.append("\tPredicted");
		csvWriter.append("\tCoincidence");
		csvWriter.append("\tPrecision");
		csvWriter.append("\tRecall");
		csvWriter.append("\tF-Score");
		csvWriter.append("\n");
		if (args.length == 0) {
			File[] filePaths = new File(FoldersUrl.CSV_INPUT_FOLDER.toString()).listFiles();
			Arrays.sort(filePaths);

			for (File path : filePaths) {
				String[] result = process(path);
				justNameOfFile = Utils.getFileNameWithoutExtension(path);
				csvWriter.append(justNameOfFile);
				csvWriter.append("\t" + result[0]);
				csvWriter.append("\t" + result[1]);
				csvWriter.append("\t" + result[2]);
				csvWriter.append("\t" + result[3]);
				csvWriter.append("\t" + result[4]);
				csvWriter.append("\t" + result[5]);
				csvWriter.append("\n");
			}
			csvWriter.flush();
			csvWriter.close();
		} else {
			String[] result = process(new File(args[0]));
			justNameOfFile = Utils.getFileNameWithoutExtension(new File(args[0]));
			csvWriter.append(justNameOfFile);
			csvWriter.append("\t" + result[0]);
			csvWriter.append("\t" + result[1]);
			csvWriter.append("\t" + result[2]);
			csvWriter.append("\t" + result[3]);
			csvWriter.append("\t" + result[4]);
			csvWriter.append("\t" + result[5]);
			csvWriter.append("\n");
			csvWriter.flush();
			csvWriter.close();
		}
		System.out.println("----> DONE!, output/csv/");
	}

	public static String[] process(File file) throws ParseException, IOException {
		justNameOfFile = Utils.getFileNameWithoutExtension(new File(file.toString()));
		File csvFile = file;// new File(fileUrl);
		ArrayList<SentenceConstraint> sentenceConstrainsAnnotated = new ArrayList<SentenceConstraint>();
		// extract fragments from csv file
		if (csvFile.exists()) {
			CSVReader reader = new CSVReader(new FileReader(csvFile.getAbsolutePath()), ';');
			reader.readNext(); // skip table header
			String[] nextLine;

			while ((nextLine = reader.readNext()) != null && nextLine.length > 1 && !nextLine[0].isEmpty()) {
				SentenceConstraint sentenceConstrain = new SentenceConstraint();
				Integer textId = Integer.parseInt(nextLine[4]);
				String text = nextLine[5];
				int constraintsNumber = Integer.parseInt(nextLine[6]);

				boolean isNegative = nextLine[7].equalsIgnoreCase("true");
				for (int i = 0; i < constraintsNumber; i++) {
					Constraint constrain = new Constraint();
					ConstraintType type = ConstraintType.getType(nextLine[8 + i * 3]);
					Activity activityA = new Activity(textId.toString(), new Action("1", nextLine[9 + i * 3], 0, 0));
					if (nextLine[10 + i * 3] == null || nextLine[10 + i * 3].isEmpty()) {
						constrain = new Constraint(type, activityA);
					} else {
						Activity ActivityB = new Activity(textId.toString(),
								new Action("1", nextLine[10 + i * 3], 0, 0));
						constrain = new Constraint(type, activityA, ActivityB);
					}
					if (isNegative)
						sentenceConstrain.setNegative(true);
					else
						sentenceConstrain.setNegative(false);

					sentenceConstrain.getConstrains().add(constrain);
				}
				sentenceConstrain.setId(textId);
				sentenceConstrain.setSentenceText(text);
				sentenceConstrainsAnnotated.add(sentenceConstrain);
			}
			reader.close();
		}
		ArrayList<SentenceConstraint> sentenceConstrainsGenerated = new ArrayList<SentenceConstraint>();
		for (int i = 0; i < sentenceConstrainsAnnotated.size(); i++) {
			parseText(sentenceConstrainsAnnotated.get(i).getSentenceText());
			PatternsHandler patternHandler = new PatternsHandler(parser.getTokens(), parser.getTrees(),
					parser.getActivitiesList());
			SentenceConstraint sentenceConstrainExtracted = new SentenceConstraint();
			ConstraintHandler constraint = new ConstraintHandler(patternHandler.getActivitiesList(),
					parser.getTrees().get(0), parser.getTokens());
			sentenceConstrainExtracted = constraint.generate();
			if (sentenceConstrainExtracted != null) {
				System.out.println("--> Constrains #: " + sentenceConstrainExtracted.getConstrains().size() + ", "
						+ sentenceConstrainExtracted);
				sentenceConstrainExtracted.setSentenceText(sentenceConstrainsAnnotated.get(i).getSentenceText());
			}
			sentenceConstrainsGenerated.add(sentenceConstrainExtracted);
		}

		// to compare
		FileWriter csvWriter = new FileWriter(FoldersUrl.CSV_OUTPUT_FOLDER + justNameOfFile + ".csv");
		csvWriter.append("Id");
		csvWriter.append("\tText");
		csvWriter.append("\tConstraints");
		csvWriter.append("\tNegative");
		csvWriter.append("\tConstraints extracted");
		csvWriter.append("\tGold");
		csvWriter.append("\tPredicted");
		csvWriter.append("\tCoincidence");
		csvWriter.append("\tPrecision");
		csvWriter.append("\tRecall");
		csvWriter.append("\tF-Score");
		csvWriter.append("\n");

		Integer totalGold = 0;
		Integer totalPredicted = 0;
		Integer totalCoincidence = 0;

		for (int i = 0; i < sentenceConstrainsAnnotated.size(); i++) {
			Integer gold = 0; 
			Integer predicted = 0; 
			Integer coincidence = 0;
			double precision = 0.0;
			double recall = 0.0;
			double fscore = 0.0;

			SentenceConstraint sentenceAnnotated = sentenceConstrainsAnnotated.get(i);
			for (int j = 0; j < sentenceAnnotated.getConstrains().size(); j++) {
				Constraint constrain = sentenceAnnotated.getConstrains().get(j);
				if (constrain.getConstrainType() != null)
					gold++;
				if (constrain.getActivityA() != null)
					gold++;
				if (constrain.getActivityB() != null)
					gold++;
			}
			if (sentenceConstrainsAnnotated.get(i).isNegative())
				gold++;
			if (sentenceConstrainsGenerated.get(i) != null) {
				SentenceConstraint scExtracted = sentenceConstrainsGenerated.get(i);
				for (int j = 0; j < scExtracted.getConstrains().size(); j++) {
					Constraint constrain = scExtracted.getConstrains().get(j);
					if (constrain.getConstrainType() != null)
						predicted++;
					if (constrain.getActivityA() != null)
						predicted++;
					if (constrain.getActivityB() != null)
						predicted++;
				}
				if (scExtracted.isNegative()) {
					predicted++;
				}
				int posExtracted = 0;
				// compare
				for (int posAnnotated = 0; posAnnotated < sentenceAnnotated.getConstrains().size(); posAnnotated++) {
					Constraint constrainAnnotated = sentenceAnnotated.getConstrains().get(posAnnotated);
					Constraint constrainExtracted = null;
					if (scExtracted.getConstrains().size() > posExtracted) {
						constrainExtracted = scExtracted.getConstrains().get(posExtracted);
						posExtracted++;
					}
					if (constrainExtracted != null) {
						if (constrainAnnotated.getConstrainType() == constrainExtracted.getConstrainType())
							coincidence++;

						if (constrainExtracted.getActivityA() != null && constrainAnnotated.getActivityA() != null) {

							if (equals(constrainAnnotated.getActivityA(), constrainExtracted.getActivityA()))
								coincidence++;
						}

						if (constrainExtracted.getActivityB() != null && constrainAnnotated.getActivityB() != null) {
							if (equals(constrainAnnotated.getActivityB(), constrainExtracted.getActivityB()))
								coincidence++;
						}
					}
				}
				if (sentenceAnnotated.isNegative() && scExtracted.isNegative()) {
					coincidence++;
				}
				if (coincidence > 0) {
					precision = coincidence * 1.00 / predicted;
				}

				recall = coincidence * 1.00 / gold;
				fscore = 2 * ((precision * recall) / (precision + recall));

				totalGold += gold;
				totalPredicted += predicted;
				totalCoincidence += coincidence;

				// Print
				System.out.println("--> Sentence #:" + i + ", Constrains #: "
						+ sentenceConstrainsGenerated.get(i).getConstrains().size() + ", "
						+ sentenceConstrainsGenerated.get(i) + ", precision: " + precision + ", recall: " + recall
						+ ", fscore: " + fscore);
				csvWriter.append(i + "");
				csvWriter.append("\t" + sentenceConstrainsGenerated.get(i).getSentenceText());
				csvWriter.append("\t" + sentenceConstrainsGenerated.get(i).getConstrains().size());
				csvWriter.append("\t" + sentenceConstrainsGenerated.get(i).isNegative());
				csvWriter.append("\t" + sentenceConstrainsGenerated.get(i).getConstrains());
				csvWriter.append("\t" + gold);
				csvWriter.append("\t" + predicted);
				csvWriter.append("\t" + coincidence);
				csvWriter.append("\t" + precision);
				csvWriter.append("\t" + recall);
				csvWriter.append("\t" + fscore);
				csvWriter.append("\n");
			} else {
				totalGold += gold;
				System.out.println("--> Sentence #:" + i + ",Constrains #: 0");
				csvWriter.append(i + "");
				csvWriter.append("\t" + sentenceConstrainsAnnotated.get(i).getSentenceText());
				csvWriter.append("\t0");
				csvWriter.append("\t");
				csvWriter.append("\t");
				csvWriter.append("\t" + gold);
				csvWriter.append("\t" + predicted);
				csvWriter.append("\t" + coincidence);
				csvWriter.append("\t" + decimalFormal.format(precision));
				csvWriter.append("\t" + decimalFormal.format(recall));
				csvWriter.append("\t" + decimalFormal.format(fscore));
				csvWriter.append("\n");
			}
		}
		csvWriter.append("");
		csvWriter.append("\t\t\t");
		csvWriter.append("\tTotal");
		double totalPrecision = totalCoincidence * 1.0 / totalPredicted;
		double totalRecall = totalCoincidence * 1.0 / totalGold;
		double totalFscore = 2 * ((totalPrecision * totalRecall) / (totalPrecision + totalRecall));
		csvWriter.append("\t" + totalGold);
		csvWriter.append("\t" + totalPredicted);
		csvWriter.append("\t" + totalCoincidence);
		csvWriter.append("\t" + decimalFormal.format(totalPrecision));
		csvWriter.append("\t" + decimalFormal.format(totalRecall));
		csvWriter.append("\t" + decimalFormal.format(totalFscore));
		csvWriter.append("\n");
		csvWriter.flush();
		csvWriter.close();
		System.out.println("Gold=" + totalGold + " , Predicted=" + totalPredicted + " , Coincidence=" + totalCoincidence
				+ ", Precision=" + decimalFormal.format(totalPrecision) + ", recall="
				+ decimalFormal.format(totalRecall) + ", F-Score=" + decimalFormal.format(totalFscore));
		String[] result = { totalGold.toString(), totalPredicted.toString(), totalCoincidence.toString(),
				decimalFormal.format(totalPrecision), decimalFormal.format(totalRecall),
				decimalFormal.format(totalFscore) };
		return result;
	}

	private static boolean equals(Activity activityAnnotated, Activity activityExtracted) {
		String textActivityExtracted = "";
		textActivityExtracted = activityExtracted.getAction().getWord();
		if (activityExtracted.getPatient() != null)
			textActivityExtracted += " " + activityExtracted.getPatient().getCompleteText();

		String arrayAnnotated[] = activityAnnotated.getAction().getWord().toLowerCase().split(" ", 2);
		String arrayExtracted[] = textActivityExtracted.toLowerCase().split(" ", 2);
		if (activityAnnotated.getAction().getWord().toLowerCase().contains(arrayExtracted[0]))
			return true;
		else if (textActivityExtracted.toLowerCase().contains(arrayAnnotated[0]))
			return true;
		if (activityExtracted.getRole() == ActionType.CONDITION
				&& activityExtracted.getAction().getMainVerbLemma() != null) {
			textActivityExtracted = activityExtracted.getAction().getMainVerbLemma();
			if (activityAnnotated.getAction().getWord().toLowerCase().contains(textActivityExtracted))
				return true;
		}
		return false;
	}

	private static void parseText(String text) throws ParseException, IOException {
		System.out.println("------------------- TEXT -------------------------");
		System.out.println(text);
		System.out.println("--------------------------------------------------");
		System.out.println("Parcing...");
		FreelingConnection freelingConection = new FreelingConnection();
		String freelingJsonString = freelingConection.getJsonString(text);
		parser = new ParserHandler(freelingJsonString);
	}
}
