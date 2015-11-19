package edu.wip.ntcir.type;

import java.util.ArrayList;

public class AnswerChoice extends Annotation{
	String id;
	String text;
	boolean isCorrect;
	boolean isSelected;
	ChoiceNumber choiceNum;
	ArrayList<Refs> refList;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public boolean isCorrect() {
		return isCorrect;
	}
	public void setIsCorrect(boolean isCorrect) {
		this.isCorrect = isCorrect;
	}
	public boolean isSelected() {
		return isSelected;
	}
	public void setIsSelected(boolean isSelected) {
		this.isSelected = isSelected;
	}
	public ChoiceNumber getChoiceNum() {
		return choiceNum;
	}
	public void setChoiceNum(ChoiceNumber choiceNum) {
		this.choiceNum = choiceNum;
	}
	public ArrayList<Refs> getRefList() {
		return refList;
	}
	public void setRefList(ArrayList<Refs> refList) {
		this.refList = refList;
	}

	
	
}

