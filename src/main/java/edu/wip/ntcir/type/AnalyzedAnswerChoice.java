package edu.wip.ntcir.type;

import java.util.ArrayList;

public class AnalyzedAnswerChoice {
	String qId;
	String ansChoiceId;
	String topic;
	String highLevelContext;
	String specificContext;
	ArrayList<Assertion> assertionList;
	String questionType;
	Instruction instruction;
	Data questionContext;
	public String getQId() {
		return qId;
	}
	public void setQId(String qId) {
		this.qId = qId;
	}
	public String getAnsChoiceId() {
		return ansChoiceId;
	}
	public void setAnsChoiceId(String ansChoiceId) {
		this.ansChoiceId = ansChoiceId;
	}
	public String getTopic() {
		return topic;
	}
	public void setTopic(String topic) {
		this.topic = topic;
	}
	public String getHighLevelContext() {
		return highLevelContext;
	}
	public void setHighLevelContext(String highLevelContext) {
		this.highLevelContext = highLevelContext;
	}
	public String getSpecificContext() {
		return specificContext;
	}
	public void setSpecificContext(String specificContext) {
		this.specificContext = specificContext;
	}
	public ArrayList<Assertion> getAssertionList() {
		return assertionList;
	}
	public void setAssertionList(ArrayList<Assertion> assertationList) {
		this.assertionList = assertationList;
	}
	public String getQuestionType() {
		return questionType;
	}
	public void setQuestionType(String questionType) {
		this.questionType = questionType;
	}
	public Instruction getInstruction() {
		return instruction;
	}
	public void setInstruction(Instruction instruction) {
		this.instruction = instruction;
	}
	public Data getQuestionContext() {
		return questionContext;
	}
	public void setQuestionContext(Data questionContext) {
		this.questionContext = questionContext;
	}
}
