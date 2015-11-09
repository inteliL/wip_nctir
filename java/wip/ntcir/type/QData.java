package wip.ntcir.type;

import java.util.ArrayList;

public class QData extends Annotation{
	String id;
	String text;
	ArrayList<Refs> refs;
	ArrayList<Gaps> gaps;
	ArrayList<ListItem> listItems;
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
	public ArrayList<Refs> getRefs() {
		return refs;
	}
	public void setRefs(ArrayList<Refs> refs) {
		this.refs = refs;
	}
	public ArrayList<Gaps> getGaps() {
		return gaps;
	}
	public void setGaps(ArrayList<Gaps> gaps) {
		this.gaps = gaps;
	}
	public ArrayList<ListItem> getListItems() {
		return listItems;
	}
	public void setListItems(ArrayList<ListItem> listItems) {
		this.listItems = listItems;
	}
	
}
