package wip.ntcir.type;

import java.util.ArrayList;

public class Data extends Annotation{
	String id;
	String text;
	ArrayList<Underlined> underlinedList;
	ArrayList<Gaps> gapList;
	
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
	public ArrayList<Underlined> getUnderlinedList() {
		return underlinedList;
	}
	public void setUnderlinedList(ArrayList<Underlined> underlinedList) {
		this.underlinedList = underlinedList;
	}
	public ArrayList<Gaps> getGapList() {
		return gapList;
	}
	public void setGapList(ArrayList<Gaps> gapList) {
		this.gapList = gapList;
	}
	
}