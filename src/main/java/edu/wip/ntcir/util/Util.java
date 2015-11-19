package edu.wip.ntcir.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import javax.security.auth.kerberos.KerberosKey;
import javax.swing.plaf.basic.BasicComboPopup.InvocationKeyHandler;
import javax.xml.crypto.NodeSetData;
import javax.xml.soap.Node;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import edu.wip.ntcir.preprocessor.HypothesisGenerator;
import edu.wip.ntcir.preprocessor.QuestionSetReader;
import edu.wip.ntcir.type.AnalyzedAnswerChoice;
import edu.wip.ntcir.type.AnswerChoice;
import edu.wip.ntcir.type.ConveyerPackage;
import edu.wip.ntcir.type.Data;
import edu.wip.ntcir.type.Gaps;
import edu.wip.ntcir.type.Instruction;
import edu.wip.ntcir.type.ListItem;
import edu.wip.ntcir.type.QData;
import edu.wip.ntcir.type.Question;
import edu.wip.ntcir.type.QuestionAnswerSet;
import edu.wip.ntcir.type.Refs;
import edu.wip.ntcir.type.SetInstruction;
import edu.wip.ntcir.type.TestDocument;
import edu.wip.ntcir.type.Underlined;

public class Util {
	
	public static void printTestDocument(TestDocument testDocument){
		ArrayList<QuestionAnswerSet> qaList = testDocument.getQAList();
		for (int i = 0; i < qaList.size(); i++){
			printQuestionAnswerSet(qaList.get(i));
			System.out.println("==========================================");
			System.out.println();
		}
	}
	
	public static void printQuestionAnswerSet(QuestionAnswerSet qa){
		Question question = qa.getQuestion();
		SetInstruction setInstr = question.getSetInstruction();
		Instruction qInstruction = question.getInstruction();
		String questionType = question.getQuestionType();
		Data qContext = question.getContextData();
		ArrayList<Underlined> underlined = qContext.getUnderlinedList();
		ArrayList<Gaps> gaps = qContext.getGapList();
		ArrayList<Refs> refList = qInstruction.getRefList();
		ArrayList<AnswerChoice> answerChoiceList = qa.getAnswerChoiceList();
		ArrayList<AnalyzedAnswerChoice> analyzedAnswerChoices = qa.getAnalyzedAnswerChoiceList();
		
		System.out.println("Question: " + question.getId());
		System.out.println("SetInstruction: " + setInstr.getText());
		System.out.println("Instruction: " + qInstruction.getText());
		System.out.println("Topic: " + setInstr.getTopic());
		System.out.println("QuestionType: " + question.getQuestionType());
		System.out.println();
		System.out.println("Context: " + qContext.getText());
		System.out.println();
		
		ArrayList<QData> qDataList = question.getQdataList();
		for (int j = 0; j < qDataList.size(); j++) {
			QData qData = qDataList.get(j);
			ArrayList<Gaps> gapList = new ArrayList<Gaps>();
			if (qData.getGaps() != null) {
				gapList = qData.getGaps();
			}

			ArrayList<ListItem> listItems = new ArrayList<ListItem>();
			if (qData.getListItems() != null) {
				listItems = qData.getListItems();
			}

			System.out.println("QuestionData: " + qData.getText());
			for (int k = 0; k < gapList.size(); k++) {
				System.out.println("Blank: " + gapList.get(k).getId()
						+ "\t" + gapList.get(k).getLabel() + "\t"
						+ gapList.get(k).getText());
			}
			for (int k = 0; k < listItems.size(); k++) {
				System.out.println("List: " + listItems.get(k).getId()
						+ "\t" + listItems.get(k).getLabel() + "\t"
						+ listItems.get(k).getText());
			}
		}
		System.out.println();
		
		for (int j = 0; j < underlined.size(); j++) {
			System.out.println("Underline " + underlined.get(j).getId() + " " + underlined.get(j).getLabel() + ": "
					+ underlined.get(j).getText());
		}
		for (int j = 0; j < gaps.size(); j++) {
			System.out.println("Gaps " + gaps.get(j).getId() + " " + gaps.get(j).getLabel() + ": "
		+ gaps.get(j).getText());
		}
		for (int j = 0; j < refList.size(); j++) {
			Refs ref = refList.get(j);
			System.out.println("Ref: " + ref.getId() + "\t"
					+ ref.getLabel());
		}


		
		System.out.println();
		for (int j = 0; j < answerChoiceList.size(); j++) {
			ArrayList<Refs> rfList = answerChoiceList.get(j).getRefList();
			System.out.println(answerChoiceList.get(j).getText());

			for (int k = 0; k < rfList.size(); k++) {
				System.out.println("Ref: " + rfList.get(k).getId() + "\t"
						+ rfList.get(k).getLabel() + "\t"
						+ rfList.get(k).getText());
			}
			
			System.out.println("Specific Context: " + analyzedAnswerChoices.get(j).getSpecificContext());
			
			for (int k = 0; k < analyzedAnswerChoices.get(j).getAssertionList().size(); k++){
				System.out.println("Assertion: " + analyzedAnswerChoices.get(j).getAssertionList().get(k).getText());
			}
			System.out.println();
		}
	}
	
	public static void writeAllQuestionAnswerSetToDir(QuestionSetReader questionSetReader, Path outdir) throws Exception {
		int cnt = 0;
		while (questionSetReader.hasNext()){
			cnt += 1;
//			if (cnt > 1){
//				break;
//			}
			ConveyerPackage conveyerPackage =  questionSetReader.getNext();
			System.out.println("Processing: " + conveyerPackage.getTestDocument().getId());
		
	    	HypothesisGenerator hypothesisGenerator = new HypothesisGenerator();
	    	hypothesisGenerator.initialize(conveyerPackage);
	    	hypothesisGenerator.process();
	    	
	    	for (QuestionAnswerSet qa : conveyerPackage.getTestDocument().getQAList()){
	    		writeQuestionAnswerSetToDir(qa, outdir);
	    	}
		}
	}
	
	/**
	 * Write test document to directory.
	 * One file for each test document
	 * @param outdir
	 * @throws IOException 
	 */
	public static void writeQuestionAnswerSetToDir(QuestionAnswerSet qa, Path outOuterDir) throws IOException{
		Path outdir = Paths.get(outOuterDir.toAbsolutePath().toString(), qa.getQuestion().getId());
		if (!Files.isDirectory(outdir)){
			Files.createDirectory(outdir);
		}
				
		Question question = qa.getQuestion();
		SetInstruction setInstr = question.getSetInstruction();
		Instruction qInstruction = question.getInstruction();
		String questionType = question.getQuestionType();
		Data qContext = question.getContextData();
		ArrayList<Underlined> underlined = qContext.getUnderlinedList();
		ArrayList<Gaps> gaps = qContext.getGapList();
		ArrayList<Refs> refList = qInstruction.getRefList();
		ArrayList<AnswerChoice> answerChoiceList = qa.getAnswerChoiceList();
		ArrayList<AnalyzedAnswerChoice> analyzedAnswerChoices = qa.getAnalyzedAnswerChoiceList();
		
		// set instruction
		writeStringToFile(setInstr.getText(), Paths.get(outdir.toString(), "set_instruction"));
		
		// question instruction
		writeStringToFile(qInstruction.getText(), Paths.get(outdir.toString(), "question_instruction"));
		
		// question context
		writeStringToFile(qInstruction.getText(), Paths.get(outdir.toString(), "question_context"));
		
		for (int j = 0; j < answerChoiceList.size(); j++) {
			Path tempOutdir = Paths.get(outdir.toAbsolutePath().toString(), "answer_" + (j+1));
			if (!Files.isDirectory(tempOutdir)){
				Files.createDirectory(tempOutdir);
			}
			
			// answer text
			String answerText = answerChoiceList.get(j).getText().replaceAll("\\([0-9]+\\)", "");
			// System.out.println(answerText);
			writeStringToFile(answerText, Paths.get(tempOutdir.toString(), "answer_text"));

			// specific context
			writeStringToFile(analyzedAnswerChoices.get(j).getSpecificContext(), Paths.get(tempOutdir.toString(), "specific_context"));

			// assertion
			StringBuffer assertionBuffer = new StringBuffer();
			for (int k = 0; k < analyzedAnswerChoices.get(j).getAssertionList().size(); k++){
				assertionBuffer.append(analyzedAnswerChoices.get(j).getAssertionList().get(k).getText() + "\n");
			}
			writeStringToFile(assertionBuffer.toString(), Paths.get(tempOutdir.toString(), "assertion"));
		}
	}
	
	private static void writeStringToFile(String str, Path outpath) throws FileNotFoundException{
		PrintWriter writer = new PrintWriter(outpath.toFile());
		writer.write(str + "\n");
		writer.close();
	}
	
	/**
	 * Deal with elements with only one extra layer of nodes.
	 * @param ele	Input element
	 * @return	Space separated string content of the element
	 */
	public static String getTextFromElement(Element ele) {
		NodeList nodeList = ele.getChildNodes();
		StringBuffer resBuffer = new StringBuffer();
		for (int i = 0; i < nodeList.getLength(); i++){
			resBuffer.append(nodeList.item(i).getTextContent() + " ");
		}
		return resBuffer.toString().trim();
	}
}
