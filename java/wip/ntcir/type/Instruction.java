package wip.ntcir.type;

import java.util.ArrayList;

public class Instruction extends Annotation{
	String text;
	ArrayList<Refs> refList;
	
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public ArrayList<Refs> getRefList() {
		return refList;
	}
	public void setRefList(ArrayList<Refs> refList) {
		this.refList = refList;
	}

}
