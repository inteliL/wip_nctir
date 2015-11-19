package edu.wip.ntcir.type;

import java.util.ArrayList;

public class TestDocument extends Annotation{
	String id;
	SetInstruction instruction;
	ArrayList<QuestionAnswerSet> QAList;
	
	// info
	String uri;
	int offsetInSource;		// number of document
	int documentSize;
	boolean isLastSegment;
	String documentText;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public SetInstruction getInstruction() {
		return instruction;
	}
	public void setInstruction(SetInstruction instruction) {
		this.instruction = instruction;
	}
	public ArrayList<QuestionAnswerSet> getQAList() {
		return QAList;
	}
	public void setQAList(ArrayList<QuestionAnswerSet> qAList) {
		QAList = qAList;
	}
	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}
	public int getOffsetInSource() {
		return offsetInSource;
	}
	public void setOffsetInSource(int offsetInSource) {
		this.offsetInSource = offsetInSource;
	}
	public int getDocumentSize() {
		return documentSize;
	}
	public void setDocumentSize(int documentSize) {
		this.documentSize = documentSize;
	}
	public boolean isLastSegment() {
		return isLastSegment;
	}
	public void setIsLastSegment(boolean isLastSegment) {
		this.isLastSegment = isLastSegment;
	}
	public String getDocumentText() {
		return documentText;
	}
	public void setDocumentText(String documentText) {
		this.documentText = documentText;
	}
	
	
}
