package wip.ntcir.type;

import java.util.ArrayList;

public class Assertion {
	String text;
	boolean isAffirmative;
	ArrayList<Double> assertScoreList;
	
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public boolean isAffirmative() {
		return isAffirmative;
	}
	public void setIsAffirmative(boolean isAffirmative) {
		this.isAffirmative = isAffirmative;
	}
	public ArrayList<Double> getAssertScoreList() {
		return assertScoreList;
	}
	public void setAssertScoreList(ArrayList<Double> assertScoreList) {
		this.assertScoreList = assertScoreList;
	} 

	
}
