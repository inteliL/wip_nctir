package wip.ntcir.util;

import java.util.ArrayList;

import javax.security.auth.kerberos.KerberosKey;
import javax.swing.plaf.basic.BasicComboPopup.InvocationKeyHandler;

import wip.ntcir.type.AnalyzedAnswerChoice;
import wip.ntcir.type.AnswerChoice;
import wip.ntcir.type.Data;
import wip.ntcir.type.Gaps;
import wip.ntcir.type.Instruction;
import wip.ntcir.type.ListItem;
import wip.ntcir.type.QData;
import wip.ntcir.type.Question;
import wip.ntcir.type.QuestionAnswerSet;
import wip.ntcir.type.Refs;
import wip.ntcir.type.SetInstruction;
import wip.ntcir.type.TestDocument;
import wip.ntcir.type.Underlined;

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
}
