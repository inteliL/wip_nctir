package edu.wip.ntcir.type;

import java.util.ArrayList;

public class QuestionAnswerSet extends Annotation{
	Question question;
	ArrayList<AnswerChoice> answerChoiceList;
	ArrayList<AnalyzedAnswerChoice> analyzedAnswerChoiceList;
	
	public Question getQuestion() {
		return question;
	}
	public void setQuestion(Question question) {
		this.question = question;
	}
	public ArrayList<AnswerChoice> getAnswerChoiceList() {
		return answerChoiceList;
	}
	public void setAnswerChoiceList(ArrayList<AnswerChoice> answerChoiceList) {
		this.answerChoiceList = answerChoiceList;
	}
	public ArrayList<AnalyzedAnswerChoice> getAnalyzedAnswerChoiceList() {
		return analyzedAnswerChoiceList;
	}
	public void setAnalyzedAnswerChoiceList(ArrayList<AnalyzedAnswerChoice> analyzedAnswerChoiceList) {
		this.analyzedAnswerChoiceList = analyzedAnswerChoiceList;
	}
	
}
