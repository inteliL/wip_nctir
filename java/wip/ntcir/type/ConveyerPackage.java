package wip.ntcir.type;

import java.util.HashMap;

public class ConveyerPackage {
	TestDocument testDocument;
	HashMap<String, Integer> hshAnswers;
	
	public TestDocument getTestDocument() {
		return testDocument;
	}
	public void setTestDocument(TestDocument testDocument) {
		this.testDocument = testDocument;
	}
	public HashMap<String, Integer> getHshAnswers() {
		return hshAnswers;
	}
	public void setHshAnswers(HashMap<String, Integer> hshAnswers) {
		this.hshAnswers = hshAnswers;
	}
	
	
}
