package wip.ntcir.type;

import java.util.ArrayList;

public class Question extends Annotation{
	String id;
	Data contextData;	// question set context
	SetInstruction setInstruction;	// question set instruction
	ArrayList<QData> qdataList;
	String questionType;
	String knowledgeType;
	Instruction instruction;	// instruction of this question
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public Data getContextData() {
		return contextData;
	}
	public void setContextData(Data contextData) {
		this.contextData = contextData;
	}
	public SetInstruction getSetInstruction() {
		return setInstruction;
	}
	public void setSetInstruction(SetInstruction setInstruction) {
		this.setInstruction = setInstruction;
	}
	public ArrayList<QData> getQdataList() {
		return qdataList;
	}
	public void setQdataList(ArrayList<QData> qdataList) {
		this.qdataList = qdataList;
	}
	public String getQuestionType() {
		return questionType;
	}
	public void setQuestionType(String questionType) {
		this.questionType = questionType;
	}
	public String getKnowledgeType() {
		return knowledgeType;
	}
	public void setKnowledgeType(String knowledgeType) {
		this.knowledgeType = knowledgeType;
	}
	public Instruction getInstruction() {
		return instruction;
	}
	public void setInstruction(Instruction instruction) {
		this.instruction = instruction;
	}
}
