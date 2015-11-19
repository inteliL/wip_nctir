package edu.wip.ntcir.preprocessor;

import java.sql.Ref;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.event.AncestorEvent;

import org.apache.commons.io.filefilter.AndFileFilter;

import edu.wip.ntcir.type.*;


public class HypothesisGenerator {

	TestDocument testDocument;
	ConveyerPackage conveyerPackage;

	public void initialize(ConveyerPackage conveyerPackage) {
		this.testDocument = conveyerPackage.getTestDocument();
		this.conveyerPackage = conveyerPackage;
	}

	public void process() throws Exception {

		ArrayList<QuestionAnswerSet> qaSet = testDocument.getQAList();

		for (int i = 0; i < qaSet.size(); i++) {	

			ArrayList<AnalyzedAnswerChoice> analyzedAnswerChoiceList = new ArrayList<AnalyzedAnswerChoice>();
			QuestionAnswerSet qa = qaSet.get(i);	// ith quesition answer pair

			// Extracting Question and related data
			Question question = qa.getQuestion();
			String questionType = question.getQuestionType();
			Data qContext = question.getContextData();
			
			qa = judgeQuestionType(qa);

			// Extracting answer choice associated with question
			ArrayList<AnswerChoice> answerChoiceList = qa.getAnswerChoiceList();
			for (int j = 0; j < answerChoiceList.size(); j++) {
				String ansId = answerChoiceList.get(j).getId();

				AnalyzedAnswerChoice analyzedAnswerChoice = new AnalyzedAnswerChoice();
				analyzedAnswerChoice.setQId(question.getId());
				analyzedAnswerChoice.setTopic(question.getSetInstruction().getTopic());
				analyzedAnswerChoice.setQuestionType(questionType);
				analyzedAnswerChoice.setAnsChoiceId(ansId);
				analyzedAnswerChoice.setHighLevelContext(qContext.getText());
				// add specific object, not only string
				analyzedAnswerChoice.setQuestionContext(qContext);

				String specificContext = "";
				try {
					specificContext = extractSpecificContext(question);
				} catch (Exception e) {
					// e.printStackTrace();
					System.err.println("Could not extract specific context ");

				}

				analyzedAnswerChoice.setSpecificContext(specificContext);

				ArrayList<Assertion> assertionList = new ArrayList<Assertion>();
				
				// All answers are sentences (to judge true/false)	
				if (question.getQuestionType().equalsIgnoreCase("sentence")) {
					
					ArrayList<Assertion> assertList = null;
					try {
						assertList = generateHypotheisStatement(qa,
								answerChoiceList.get(j));
					} catch (Exception e) {
						e.printStackTrace();
					}

					assertionList.addAll(assertList);
				} else {

					ArrayList<Assertion> assertList = null;
					try {
						assertList = prepareHypotheisStatement(qa,
								answerChoiceList.get(j));
					} catch (Exception e) {
						System.out.println();
						e.printStackTrace();
					}

					assertionList.addAll(assertList);
				}
				
				if (assertionList.size() == 0){
					assertionList.addAll(fillEmptyAssertion(qa, answerChoiceList.get(j)));
				}

				analyzedAnswerChoice.setAssertionList(assertionList);
				analyzedAnswerChoiceList.add(analyzedAnswerChoice);
			}

			qa.setAnalyzedAnswerChoiceList(analyzedAnswerChoiceList);
			qaSet.set(i, qa);
		}
		testDocument.setQAList(qaSet);

		System.out.println("Done preparing hypothesis.\n");
	}

	/**
	 * General-purpose hypothesis generator 
	 * @param qaSet
	 * @param answerChoice
	 * @return
	 * @throws Exception
	 */
	public ArrayList<Assertion> fillEmptyAssertion(QuestionAnswerSet qaSet,
			AnswerChoice answerChoice) throws Exception {

		ArrayList<Assertion> assertionList = new ArrayList<Assertion>();
		String assertionStatement = "";
		Instruction instruction = qaSet.getQuestion().getInstruction();
		ArrayList<Refs> refList = answerChoice.getRefList();
		ArrayList<QData> qDataList = qaSet.getQuestion().getQdataList();
		Data contextData = qaSet.getQuestion().getContextData();
		
		// Extract context data
		String context = "";
		if (contextData.getUnderlinedList() != null) {
			ArrayList<Underlined> underlined = contextData.getUnderlinedList();
			ArrayList<Refs> instrRefList = instruction.getRefList();

			for (int i = 0; i < instrRefList.size(); i++) {
				Refs refs = instrRefList.get(i);

				for (int j = 0; j < underlined.size(); j++) {
					if (refs.getId().equals(underlined.get(j).getId())) {
						context += underlined.get(j).getText() + " ";
					}
				}
			}
		}
		
		// Resolve reference in choice
		if (refList != null && refList.size() > 0) {
			for (int i = 0; i < refList.size(); i++) {
				assertionStatement += refList.get(i).getText() + " ";	
				if (qDataList != null && qDataList.size() > 0) {
					for (int j = 0; j < qDataList.size(); j++) {
						ArrayList<Refs> qDataRefs = qDataList.get(j).getRefs();
						ArrayList<ListItem> qDatalst = qDataList.get(j).getListItems();
						ArrayList<Gaps> qDatagaps = qDataList.get(j).getGaps();

						for (int k = 0; k < qDataRefs.size(); k++) {
							if (qDataRefs.get(k).getId()
									.equals(refList.get(i).getId())) {
								assertionStatement += qDataRefs.get(k)
										.getText() + " ";
							}
						}
						for (int k = 0; k < qDatalst.size(); k++) {
							if (qDatalst.get(k).getId()
									.equals(refList.get(i).getId())) {
								assertionStatement += qDatalst.get(k).getText()
										+ " ";
							}
						}
						for (int k = 0; k < qDatagaps.size(); k++) {
							if (qDatagaps.get(k).getId()
									.equals(refList.get(i).getId())) {
								assertionStatement += qDatagaps.get(k)
										.getText() + " ";
							}
						}
					}
				}
			}
		}

		assertionStatement = assertionStatement.trim();
		Assertion assertion = new Assertion();
		assertionStatement = assertionStatement.replaceAll("^([(][0-9]+[)])",
				"").trim();

		if (assertionStatement.equals("")) {

			assertionStatement = answerChoice.getText();
		}
		
		assertionStatement = context + " " + assertionStatement;

		assertion.setText(assertionStatement);

		assertion.setIsAffirmative(checkQuestionNegativity(qaSet.getQuestion()));
		assertionList.add(assertion);
		return assertionList;
	}

	/**
	 * Generate hypothesis for questions where all answers are sentences (to judge true/false)
	 * @param qaSet
	 * @param answerChoice
	 * @return
	 * @throws Exception
	 */
	public ArrayList<Assertion> generateHypotheisStatement(
			QuestionAnswerSet qaSet, AnswerChoice answerChoice)
					throws Exception {

		Question question = qaSet.getQuestion();

		Instruction instruction = qaSet.getQuestion().getInstruction();
		Data contextData = qaSet.getQuestion().getContextData();

		// if the answers are sentences, there may be many specific contexts
		String context = "";
		if (contextData.getUnderlinedList() != null) {
			ArrayList<Underlined> underlined = contextData.getUnderlinedList();

			ArrayList<Refs> refList = instruction.getRefList();

			for (int i = 0; i < refList.size(); i++) {
				Refs refs = refList.get(i);

				for (int j = 0; j < underlined.size(); j++) {

					if (refs.getId().equals(underlined.get(j).getId())) {

						context += underlined.get(j).getText() + " ";
					}
				}
			}
		}
		ArrayList<Assertion> assertionList = new ArrayList<Assertion>();
		String assertionStatement = answerChoice.getText() + " " + context;
		assertionStatement = assertionStatement.trim();
		Assertion assertion = new Assertion();
		assertionStatement = assertionStatement.replaceAll("^([(][0-9]+[)])",
				"").trim();
		assertion.setText(assertionStatement);
		assertion.setIsAffirmative(checkQuestionNegativity(question));
		assertionList.add(assertion);

		return assertionList;
	}

	/**
	 * Extract specific context (underlined text specified by question instruction) 
	 * @param question
	 * @return
	 */
	public String extractSpecificContext(Question question) {

		String specificContext = "";

		Instruction qInstruction = question.getInstruction();

		ArrayList<Refs> refList = qInstruction.getRefList();

		Data contextData = question.getContextData();
		ArrayList<Underlined> underlined = contextData.getUnderlinedList();

		if (refList.size() > 0) {
			for (int i = 0; i < refList.size(); i++) {

				String refId = refList.get(i).getId();
				for (int j = 0; j < underlined.size(); j++) {

					if (underlined.get(j).getId().equals(refId)
							&& refId.startsWith("U")) {
						specificContext += underlined.get(j).getText() + " ";
						break;
					}
				}
			}

		}
		return specificContext;
	}

	public ArrayList<Assertion> prepareHypotheisStatement(
			QuestionAnswerSet qaSet, AnswerChoice answerChoice)
					throws Exception {

		Question question = qaSet.getQuestion();
		Instruction qInstruction = question.getInstruction();

		ArrayList<QData> qDataList = question.getQdataList();
		String questionType = question.getQuestionType();
		ArrayList<Assertion> assertionList = new ArrayList<Assertion>();
		// for (int i = 0; i < qDataList.size(); i++) {

		if (questionType.equals("symbol-term")) {	// slot filling
			if (qDataList.size() > 0) {	// has additional data
				for (int i = 0; i < qDataList.size(); i++) {
					ArrayList<Gaps> gapsList = new ArrayList<Gaps>();
					if (qDataList.get(i).getGaps() != null) {
						gapsList = qDataList.get(i).getGaps();
					}

					String qDataText = qDataList.get(i).getText();
					for (int l = 0; l < gapsList.size(); l++) {
						String gapId = gapsList.get(l).getId();

						ArrayList<Refs> refList = answerChoice.getRefList();
						for (int m = 0; m < refList.size(); m++) {
							if (gapId.equals(refList.get(m).getId())) {
								String refText = refList.get(m).getText();
								qDataText = qDataText.replace(refList.get(m)
										.getLabel(), refText);
							}
						}

					}

					String assertions[] = qDataText.split("[.]");
					String ansText = answerChoice.getText();

					ansText = ansText.replaceAll("^([(][0-9]+[)])", "").trim();
					for (int l = 0; l < gapsList.size(); l++) {

						ansText = ansText.replace(gapsList.get(l).getLabel(),
								"|");
						// list.add(gapsList.get(l).getLabel());

					}
					String ans[] = ansText.split("[|]");
					ArrayList<String> ansList = new ArrayList<String>();
					for (int l = 0; l < ans.length; l++) {
						if (ans[l].equals("")) {
							continue;
						}
						ansList.add(ans[l]);
					}

					for (int l = 0; l < assertions.length; l++) {

						boolean found = false;
						HashSet<String> hsh = new HashSet<String>();
						// sometimes
						// more than
						// fill in
						// the
						// blanks
						// comes
						// with same
						// ref
						int n = 0;
						for (int m = 0; m < gapsList.size(); m++) {	// find assertions that contain gap
							if (hsh.contains(gapsList.get(m).getLabel())) {
								continue;
							}
							hsh.add(gapsList.get(m).getLabel());
							if (assertions[l].contains(gapsList.get(m)
									.getLabel())
									|| assertions[l].contains(ansList.get(n))) {
								assertions[l] = assertions[l].replace(gapsList
										.get(m).getLabel(), ansList.get(n));
								found = true;
							}
							n++;
						}

						if (!found) {
							continue;
						}

						Assertion assertion = new Assertion();
						assertions[l] = assertions[l].replaceAll(
								"^([(][0-9]+[)])", "").trim();
						assertion.setText(assertions[l]);
						assertion.setIsAffirmative(true);
						assertionList.add(assertion);
					}
					// assertionStatement=qDataText;
				}
			} else {	// no additional data

				Data data = question.getContextData();
				String assertionText = data.getText();
				ArrayList<Refs> qInstrRefList = qInstruction.getRefList();
				// ArrayList<Underlined>
				// underlined=Utils.fromFSListToCollection(data.getUnderlinedList(),Underlined.class);
				ArrayList<Gaps> gaps = data.getGapList();
				Assertion assertion = new Assertion();
				String assertions[] = assertionText.split("[.]");
				ArrayList<Refs> ansRefs = answerChoice.getRefList();

				for (int m = 0; m < ansRefs.size(); m++) {
					if (ansRefs.get(m).getText() == null) {
						continue;
					}
					for (int l = 0; l < assertions.length; l++) {
						if (assertions[l].trim().equals("")) {
							continue;
						}
						String updatedAssertions = "";
						for (int i = 0; i < qInstrRefList.size(); i++) {

							for (int j = 0; j < gaps.size(); j++) {

								// if (qInstrRefList.get(i).getId()
								// .equalsIgnoreCase(gaps.get(j).getId())) {

								// if this assertion has a gap, and it correspond to ansRef[m]
								if ((assertions[l].contains(gaps.get(j)
										.getLabel()) || assertions[l]
												.contains("__"))
										&& gaps.get(j).getId()
										.equals(ansRefs.get(m).getId())) {

									if (qInstrRefList
											.get(i)
											.getId()
											.equalsIgnoreCase(
													ansRefs.get(m).getId())) {
										if (updatedAssertions.equals("")) {
											updatedAssertions = assertions[l];
										}

										updatedAssertions = updatedAssertions
												.replace(
														gaps.get(j).getLabel(),
														ansRefs.get(m)
														.getText())
												.trim();
										updatedAssertions = updatedAssertions
												.replaceFirst(
														"[_]+",
														ansRefs.get(m)
														.getText())
												.trim();

										assertions[l] = updatedAssertions;
										// updatedAssertions=assertions[l];
									}

								}

							}
						}

						if (!updatedAssertions.equals("")
								&& !(updatedAssertions.contains("__") || updatedAssertions
										.contains(ansRefs.get(m).getLabel()))) {
							updatedAssertions = updatedAssertions.replaceAll(
									"^([(][0-9]+[)])", "").trim();
							assertion.setText(updatedAssertions);

							assertion.setIsAffirmative(true);
							assertionList.add(assertion);
							break;
						}
						// }//

					}

				}
			}

		} else if (questionType.equals("(symbol-TF)*2")) {

			for (int i = 0; i < qDataList.size(); i++) {

				ArrayList<Refs> refList = answerChoice.getRefList();
				// String chText = answerChoice.getText();
				ArrayList<ListItem> listItemsList = qDataList.get(i).getListItems();

				for (int k = 0; k < listItemsList.size(); k++) {
					String lId = listItemsList.get(k).getId();
					String lText = listItemsList.get(k).getText();
					for (int l = 0; l < refList.size(); l++) {
						if (refList.get(l).getId().equals(lId)) {
							Assertion assertion = new Assertion();
							lText = lText.replaceAll("^([(][0-9]+[)])", "")
									.trim();
							assertion.setText(lText);
							if (refList.get(l).getText().endsWith("Correct")) {
								assertion.setIsAffirmative(true);
							} else {
								assertion.setIsAffirmative(false);
							}
							assertionList.add(assertion);
						}
					}
				}
			}
		} else if (questionType.equals("factoid-term")) {		// factoid
			String instruction = qaSet.getQuestion().getInstruction().getText();
			String ansChoiceText = answerChoice.getText()
					.replace(answerChoice.getId(), "").trim();
			ArrayList<Refs> refList = qaSet.getQuestion().getInstruction().getRefList();
			if (refList.size() == 0) {

				if (qDataList.size() == 0) {

					String assertions[] = question.getContextData().getText()
							.split("[.]");
					boolean found = false;
					String foundAssert = "";
					for (int a = 0; a < assertions.length; a++) {
						if (assertions[a].trim().equals("")) {
							continue;
						}

						assertions[a] = assertions[a].replaceAll(
								"^([(][0-9]+[)])", "").trim();
						int len = assertions[a].length();
						assertions[a] = assertions[a].replaceAll("[_]+{2,}",
								ansChoiceText).trim();
						if (len != assertions[a].length()) {
							found = true;
							foundAssert = assertions[a];
							break;
						}
					}
					if (found) {
						Assertion assertion = new Assertion();

						assertion.setText(foundAssert);
						assertion.setIsAffirmative(true);
						assertionList.add(assertion);
					}

				} else {

					for (int l = 0; l < qDataList.size(); l++) {
						String qData = qDataList.get(l).getText();
						ArrayList<Refs> gapList = qDataList.get(l).getRefs();

						for (int m = 0; m < gapList.size(); m++) {

							qData = qData.replace(gapList.get(m).getLabel(),
									ansChoiceText);
							qData = qData.replace("[_]+{2,}", ansChoiceText);
						}
						String assertions[] = qData.split("[.]");
						for (int a = 0; a < assertions.length; a++) {
							if (assertions[a].trim().equals("")) {
								continue;
							}
							Assertion assertion = new Assertion();
							assertions[a] = assertions[a].replaceAll(
									"^([(][0-9]+[)])", "").trim();
							assertion.setText(assertions[a]);
							assertion.setIsAffirmative(true);
							assertionList.add(assertion);
						}
					}

				}

			} else {

				for (int k = 0; k < refList.size(); k++) {
					Refs target = refList.get(k);
					String targetId = target.getId();
					String targetLabel = target.getLabel();

					if (targetId.startsWith("U")) {

						String underlined = extractSpecificContext(question);
						underlined = underlined.replaceAll("[(][0-9][)]", "")
								.trim();

						boolean boolVal = checkQuestionNegativity(question);

						String filteredInstruction = filterInstruction(instruction);

						Assertion assertion = new Assertion();
						String assertText = filteredInstruction + " "
								+ underlined + " " + ansChoiceText;
						assertion.setText(assertText);
						assertion.setIsAffirmative(boolVal);
						assertionList.add(assertion);

					} else if (targetId.startsWith("B")) {

						Data contextData = question.getContextData();
						String contextText = contextData.getText();
						contextText = contextText.replaceAll("^ａ", "")
								.replaceFirst("^b", "").replaceFirst("^ｃ", "")
								.trim();

						String choiceText = answerChoice.getText()
								.replaceAll("^([(][0-9]+{1,2}[)])", "").trim();
						String assertionText = contextText.replace(targetLabel,
								choiceText);
						assertionText = assertionText.replaceFirst("[_]+{2,}",
								choiceText);

						String assertions[] = assertionText.split("[.]");
						for (int l = 0; l < assertions.length; l++) {

							boolean found = false;

							if (assertions[l].contains(choiceText)) {
								found = true;
							}

							if (!found) {
								continue;
							}
							Assertion assertion = new Assertion();

							assertion.setText(assertions[l]);
							assertion.setIsAffirmative(true);
							assertionList.add(assertion);
						}

					} else {
						System.err.println(question.getId()
								+ ": Image question not supproted");
						// throw new Exception("Unsupported RefTarget type");
					}
				}
			}
		} else if (questionType.equals("(slot-symbol)*2")) {	// two-slot slot filling
			ArrayList<Refs> refList = qaSet.getQuestion().getInstruction().getRefList();
			String contextData = question.getContextData().getText();

			String assertText = contextData;
			String ansText = answerChoice.getText();
			String ans[] = ansText.split("[-]");
			for (int k = 0; k < refList.size(); k++) {
				Refs target = refList.get(k);
				String targetId = target.getId();
				String targetLabel = target.getLabel();
				ans[k] = ans[k].replaceAll("^([(][0-9]+[)])", "").trim();
				ans[k] = ans[k].replaceAll("[①②③④⑤⑥]", " ").trim();

				assertText = assertText.replace(targetLabel, ans[k]);
				assertText = assertText.replaceFirst("[_]+{2,}", ans[k]);

			}
			String assertions[] = assertText.trim().split("[.]");
			for (int l = 0; l < assertions.length; l++) {
				boolean found = false;
				for (int m = 0; m < ans.length; m++) {
					if (assertions[l].contains(ans[m])) {
						found = true;
					}
				}

				if (!found) {
					continue;
				}
				Assertion assertion = new Assertion();
				assertion.setText(assertions[l]);
				assertion.setIsAffirmative(true);
				assertionList.add(assertion);
			}

		} else if (questionType
				.equals("o(sentence-sentence-sentence-sentence)")) {

		} else {	// default hypothesis generator
			return fillEmptyAssertion(qaSet, answerChoice);
		}

		return assertionList;
	}

	private boolean checkQuestionNegativity(Question question) {
		String qInstrText = question.getInstruction().getText().toLowerCase();

		boolean res = (qInstrText.indexOf("that incorrectly") == -1
				&& qInstrText.indexOf("that was not") == -1
				&& qInstrText.indexOf("incorrect sentence") == -1
				&& qInstrText.indexOf("wrong sentence") == -1
				&& // not seen aded by Leo
				qInstrText.indexOf("is incorrect") == -1
				&& qInstrText.indexOf("is wrong") == -1 && // not seen aded by
				// Leo
				qInstrText.indexOf("incorrectly describes") == -1 && qInstrText
				.indexOf("contains a mistake") == -1);
		return res;

	}

	public String filterInstruction(String instruction) throws Exception {

		instruction = instruction.toLowerCase().replaceAll("[(][0-9][)]", " ")
				.replaceAll("[\\W]+", " ").trim();
		String wordList[] = { "in", "regard", "to", "the", "underlined",
				"portion", "from", "①~④", "below", "choose", "one", "correct",
				"incorrect", "option", "that", "is", "name", "a", "an", };
		HashSet<String> hshFilterSet = new HashSet<String>();
		hshFilterSet.addAll(Arrays.asList(wordList));

		String words[] = instruction.split("[\\W]");
		String instr = "";
		for (int i = 0; i < words.length; i++) {
			words[i] = words[i].trim();
			if (words[i].equals("")) {
				continue;
			}
			if (hshFilterSet.contains(words[i])) {
				continue;
			}
			instr += words[i] + " ";
		}

		return instr;
	}
	
	/**
	 * Judge the question type given a question answer set
	 * Available types are:
	 * 		sentence:	all answers are sentences, to judge true or false of each of them
	 * 		symbol-term: slot filling
	 * 		(symbol-TF)*2: two sentence true/false
	 * 		factoid-term: factoid, answers are terms 
	 * 		(slot-symbol)*2: two-slot slot filing
	 * @param qa Question answer set to judge type
	 * @return the same question answer set
	 */
	private QuestionAnswerSet judgeQuestionType(QuestionAnswerSet qa){
		Question question = qa.getQuestion();
		
		if (twoSentTrueFalse(qa)){
			question.setQuestionType("(symbol-TF)*2");
			return qa;
		} else if (isTwoSlot(qa)){
			question.setQuestionType("(slot-symbol)*2");
			return qa;
		} else if (isSymbolTerm(qa)){
			question.setQuestionType("symbol-term");
			return qa;
		} else if (isFactoidTerm(qa)){
			question.setQuestionType("factoid-term");
			return qa;
		} else if (allAnswersAreSentences(qa)){
			question.setQuestionType("sentence");
			return qa;
		}
		
		return qa;
	}
	
	/* Here are some functions to judge question type */	
	private boolean twoSentTrueFalse(QuestionAnswerSet qa){
		// There must be a two-item list in qdata
		Question question = qa.getQuestion();
		ArrayList<QData> qDataList = question.getQdataList();
		// ArrayList<AnswerChoice> answerChoiceList = qa.getAnswerChoiceList();
		
		for (int i = 0; i < qDataList.size(); i++) {
			ArrayList<ListItem> listItemsList = qDataList.get(i).getListItems();
			if (listItemsList != null && listItemsList.size() == 2){
				return true;
			}
		}
		return false;
	}
	
	private boolean isTwoSlot(QuestionAnswerSet qa){
		// exactly 2 blanks in each answer
		ArrayList<AnswerChoice> answerChoiceList = qa.getAnswerChoiceList();
		for (AnswerChoice answerChoice : answerChoiceList){
			ArrayList<Refs> refList = answerChoice.getRefList();
			int blankCnt = 0;
			for (Refs ref : refList){
				if (ref.getId().startsWith("B")){
					blankCnt += 1;
				}
			}
			if (blankCnt != 2){
				return false;
			}
		}
		return true;
	}
	
	private boolean isSymbolTerm(QuestionAnswerSet qa){
		// blank in answer
		ArrayList<AnswerChoice> answerChoiceList = qa.getAnswerChoiceList();
		for (AnswerChoice answerChoice : answerChoiceList){
			ArrayList<Refs> refList = answerChoice.getRefList();
			for (Refs ref : refList){
				if (ref.getId().startsWith("B")){
					return true;
				}
			}
		}
		return false;
	}
			
	private boolean isFactoidTerm(QuestionAnswerSet qa){
		// the answer length is less or eaqual to 4
		for (AnswerChoice answerChoice : qa.getAnswerChoiceList()){
			String[] answerSplit = answerChoice.getText().split("[ \t\n]");
			int cnt = 0;
			for (String word : answerSplit){
				if (word.trim() != ""){
					cnt += 1;
				}
			}
			if (cnt > 4){
				return false;
			}
		}
		return true;
	}
	
	private boolean allAnswersAreSentences(QuestionAnswerSet qa){	// end of pipeline
		return true;
	}
}
