package wip.ntcir.preprocessor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.commons.io.FileUtils;
import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.google.common.collect.ArrayListMultimap;

import wip.ntcir.type.AnswerChoice;
import wip.ntcir.type.ChoiceNumber;
import wip.ntcir.type.ConveyerPackage;
import wip.ntcir.type.Data;
import wip.ntcir.type.Gaps;
import wip.ntcir.type.Instruction;
import wip.ntcir.type.ListItem;
import wip.ntcir.type.QData;
import wip.ntcir.type.Question;
import wip.ntcir.type.QuestionAnswerSet;
import wip.ntcir.type.Refs;
import wip.ntcir.type.SetInstruction;
import wip.ntcir.type.TestDocument;
import wip.ntcir.type.Underlined;
import wip.ntcir.type.Annotation;


/**
 * Modified on top of the original TestDocXMLReader to add correct DocumentText
 * and text span.
 */

public class QuestionSetReader{

	File testFile[] = null;		// a list of input files
	int nCurrFile = 0;	// index of current file (many files in directory)
	List<Node> documents = new ArrayList<Node>();	// package: org.w3c.dom, stores big question set nodes
	String tempYear = ""; // the year of current exam
	String questionMarker = "Q: ";	// used as mark in generated plain text
	String answerMarker = "A: ";
	HashMap<String, Integer> hshAnswers = new HashMap<String, Integer>();	// key: qustion_id, value: right answer index
	int nCurrDoc = 0;

	// use to build document text
	StringBuffer documentText;
	int annoOffset;

	public void initialize(String inputDirPath, String goldDirPath){
		try {
			File inputDir = new File(inputDirPath);
			File goldStandardDir = new File(goldDirPath);

			if (!goldStandardDir.exists() || !goldStandardDir.isDirectory()) {
				System.err
						.println("Cannot find gold standard file or it is not a directory");
				System.exit(1);
			}
			if (!inputDir.exists() || !inputDir.isDirectory()) {
				System.err
						.println("Cannot find input directory or it is not a directory");
				System.exit(1);
			}

			// parseGlodStandardsDir(goldStandardDir);	// get gold answer

			testFile = inputDir.listFiles(new OnlyNXML("xml"));		// OnlyNXML is an FileFilter
			System.out.println("Total files: " + testFile.length);
			String xmlText = readTestFile();		// read the first file
			
			xmlText = xmlText.replaceAll("①\\~④", "(1)-(4)");
			xmlText = xmlText.replaceAll("①\\~⑤", "(1)-(5)");
			xmlText = xmlText.replaceAll("①", "(1)");
			xmlText = xmlText.replaceAll("②", "(2)");
			xmlText = xmlText.replaceAll("③", "(3)");
			xmlText = xmlText.replaceAll("④", "(4)");
			xmlText = xmlText.replaceAll("⑤", "(5)");
			parseTestDocument(xmlText);	// get big question set nodes, real parse is done when getNext() called
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void parseGlodStandardsDir(File goldDir) throws Exception {
		if (!goldDir.isDirectory()){
			System.err.println("Gold Standard Path is not a Directory!");
			System.exit(1);
		}
		String[] fileList = goldDir.list();
        for (int i = 0; i < fileList.length; i++) {
        	if (fileList[i].startsWith(".")) continue;
        	File tempFile = Paths.get(goldDir.getAbsolutePath(), fileList[i]).toFile();
        	parseGoldStandards(tempFile);
        }
	}

	private void parseGoldStandards(File goldFile) throws Exception {
		String tempYear = goldFile.getName().split("-")[1];
		String qIdPrefix = "Q" + tempYear + "_";
		DOMParser parser = new DOMParser();
		try {
			parser.parse(new InputSource(new FileReader(goldFile)));
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}

		Document doc = parser.getDocument();

		NodeList dataList = doc.getElementsByTagName("data");

		for (int i = 0; i < dataList.getLength(); i++) {
			Element dataEle = (Element) dataList.item(i);
			String questionId = qIdPrefix + dataEle.getElementsByTagName("question_ID")
					.item(0).getTextContent().trim().substring(1);
			String answer = dataEle.getElementsByTagName("answer").item(0)
					.getTextContent().trim();
			int answerId = Integer.parseInt(answer);

			hshAnswers.put(questionId, answerId);
		}
	}
	
	/**
	 * Get next big question set
	 * @param aCAS
	 * @throws IOException
	 * @throws CollectionException
	 */
	public ConveyerPackage getNext(){
		// one question set corresponds to a doc (get next question set)
		documentText = new StringBuffer();	// text for this big question set
		annoOffset = 0;

		// change to next file when this file is done
		if (nCurrFile < testFile.length && !(nCurrDoc < documents.size())) {
			nCurrDoc = 0;
			nCurrFile++;
			documents = null;
			getNext();
		}

		if (documents == null) {		// get a new file
			try {
				String xmlText = readTestFile();
				this.parseTestDocument(xmlText);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		NodeList questionSetNodeList = documents.get(nCurrDoc).getChildNodes();

		// The XML is not well designed, so we need to assume that data and
		// instruction will appear before the questions so that we could map
		// them up
		SetInstruction setInstruction = null;	// instruction for several subsequent quesitons
		Data contextData = null;	// context data for several subsequent questions

		ArrayList<QuestionAnswerSet> qaList = new ArrayList<QuestionAnswerSet>();

		for (int i = 0; i < questionSetNodeList.getLength(); i++) {
			if (questionSetNodeList.item(i).getNodeType() == Node.ELEMENT_NODE) {
				Element questionSetNode = (Element) questionSetNodeList.item(i);
				String nodeName = questionSetNode.getNodeName().trim();
				if (nodeName.startsWith("#")) {
					continue;
				}
				if (nodeName.startsWith("data")) {	// context data
					Element contextDataElement = questionSetNode;
					contextData = extractContextFromData(contextDataElement);
				}
				if (nodeName.equals("question")) {		// single small question
					Element qElement = questionSetNode;
					QuestionAnswerSet qaSet = annoateQuestion(qElement);
					qaList.add(qaSet);
					qaSet.getQuestion().setContextData(contextData);
					qaSet.getQuestion().setSetInstruction(setInstruction);
				}
				if (nodeName.equals("label")) {		// useless
					String docId = questionSetNode.getTextContent().trim();
				}
				if (nodeName.equals("instruction")) {	// set instruction
					String topInstructionText = questionSetNode
							.getTextContent().trim();

					// Create SetInstruction
					setInstruction = new SetInstruction();
					String topic = extrctTopicFromSetInstruction(topInstructionText);
					setInstruction.setTopic(topic);
					setInstruction.setText(topInstructionText);

					String instructionMarker = "[Overall Instruction]\n";

					documentText.append(instructionMarker + topInstructionText);
					annoOffset += instructionMarker.length();

					setInstruction.setBegin(annoOffset);
					setInstruction.setEnd(annoOffset
							+ topInstructionText.length());
					documentText.append("\n\n");

					annoOffset += topInstructionText.length() + 2;
				}
			}
		}

		TestDocument testDoc = new TestDocument();
		testDoc.setId(String.valueOf(nCurrDoc));
		testDoc.setInstruction(setInstruction);		// set instruction 
		testDoc.setQAList(qaList);
		
		File currentFile = testFile[nCurrFile];
		
		testDoc.setUri(currentFile.toURI().toString());
		testDoc.setDocumentSize((int) currentFile.length());
		testDoc.setIsLastSegment(hasNext());
		testDoc.setOffsetInSource(nCurrDoc);
		testDoc.setDocumentText(documentText.toString());
		
		nCurrDoc++;
		
		ConveyerPackage conveyerPackage = new ConveyerPackage();
		conveyerPackage.setTestDocument(testDoc);
		conveyerPackage.setHshAnswers(hshAnswers);
		
		return conveyerPackage;
	}

	private String extrctTopicFromSetInstruction(String instruction) {
		String str = "relate to";
		int startIdx = instruction.indexOf(str);
		int endIdx = instruction.indexOf(",", startIdx);
		String topic = "";
		try {
			topic = instruction.substring(startIdx + str.length(), endIdx)
					.trim();
		} catch (Exception e) {
			System.err.println(e);
		}
		return topic;
	}

	/**
	 * Some basic node content have additional tagging, such as blank and ref,
	 * offsets and formatting handled using this method
	 * 
	 * @return special taggings are returned as a list
	 */
	private ArrayListMultimap<String, Annotation> formatTaggedText(
			Element element) {
		ArrayListMultimap<String, Annotation> annoMap = ArrayListMultimap
				.create();		// one key --- multi value
		NodeList children = element.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);

			if (child.getNodeType() == Node.TEXT_NODE) {
				String text = child.getTextContent().trim();
				// if empty text, ignored
				if (text.equals("")) {
					continue;
				} else {
					// if pure text, append and increment offset
					documentText.append(text + " ");
					annoOffset += text.length() + 1;
				}
			} else if (child.getNodeType() == Node.ELEMENT_NODE) {
				// if tag, handle and grab the text, append and increment offset
				Element childElement = (Element) child;

				// 1. blank (need anwsers to fill)
				if (childElement.getNodeName().equals("blank")) {
					String ii = childElement.getAttribute("id");
					String lbl = childElement.getTextContent().trim();	// label
					if (childElement.getElementsByTagName("label").getLength() > 0) {
						lbl = childElement.getElementsByTagName("label")
								.item(0).getTextContent().trim();
					}

					// Create annotation Gaps
					Gaps gap = new Gaps();
					gap.setId(ii);
					gap.setLabel(lbl);

					// Create some gap representation on documentText
					String gapPlaceHolder = "_____";
					documentText.append(" " + gapPlaceHolder + " ");
					annoOffset += 1;
					gap.setBegin(annoOffset);
					annoOffset += gapPlaceHolder.length();
					gap.setEnd(annoOffset);
					annoOffset += 1;

					annoMap.put("blank", gap);

				} else if (childElement.getNodeName().equals("lText")) {
					// 2. list
					String ii = childElement.getAttribute("id");
					String lbl = childElement.getElementsByTagName("label")
							.item(0).getTextContent().trim();

					// Create annotation ListItem
					ListItem lstItem = new ListItem();
					lstItem.setId(ii);
					lstItem.setLabel(lbl);

					NodeList listContents = childElement.getChildNodes();
					String listTxt = "";
					for (int j = 0; j < listContents.getLength(); j++) {
						Node listContent = listContents.item(j);
						if (listContent.getNodeType() == Node.TEXT_NODE) {
							if (!listContent.getTextContent().trim().equals("")) {
								listTxt = listContent.getTextContent().trim();
							}
						}
					}
					lstItem.setText(listTxt);

					String listPrefix = "    ";
					documentText
							.append(listPrefix + lbl + " " + listTxt + "\n");
					annoOffset += (1 + listPrefix.length() + lbl.length());
					lstItem.setBegin(annoOffset);
					annoOffset += listTxt.length();
					lstItem.setEnd(annoOffset);
					annoOffset += 1;

					annoMap.put("lText", lstItem);
				} else if (childElement.getNodeName().equals("ref")) {
					// 3. ref
					String refLabel = childElement.getTextContent().trim();

					// Create annotation ref
					Refs ref = new Refs();
					String id = childElement.getAttribute("target");
					ref.setId(id);
					ref.setLabel(refLabel);

					documentText.append(refLabel + " ");
					ref.setBegin(annoOffset);
					annoOffset += refLabel.length();
					ref.setEnd(annoOffset);
					annoOffset += 1;

					if (id.startsWith("B")) {
						// target are blanks
						String replacement = childElement.getNextSibling()
								.getTextContent().trim();	// why?
						ref.setText(replacement);
					} else if (id.startsWith("L")) {
						// target are lists
						String suffix = childElement.getNextSibling()	// why?
								.getTextContent().trim();
						ref.setText(suffix);
					} else if (id.startsWith("U")) {
						// target are underlined
					} else if (id.startsWith("D")) {
						// target are pictures
					}
					annoMap.put("ref", ref);
				} else if (childElement.getNodeName().equals("cNum")) {
					// 4. cNum
					ChoiceNumber cNum = new ChoiceNumber();
					annoMap.put("cNum", (Annotation)cNum);		// cNum is useless?

					String cNumText = childElement.getTextContent().trim();

					documentText.append(cNumText + " ");
					cNum.setBegin(annoOffset);
					annoOffset += cNumText.length();
					cNum.setEnd(annoOffset);
					annoOffset += 1; // for the space
				} else if (childElement.getNodeName().equals("label")) {
					// 5 label (not very useful right now)
					String labelText = childElement.getTextContent().trim();
				} else if (childElement.getNodeName().equals("uText")) {
					// 6 underlined
					String ulabelId = childElement.getAttribute("id");
					String uText = "";
					String label = "";

					for (int j = 0; j < childElement.getChildNodes()
							.getLength(); j++) {
						Node uNodeChild = childElement.getChildNodes().item(j);
						if (uNodeChild.getNodeType() == Node.ELEMENT_NODE) {
							if (uNodeChild.getNodeName().equals("label")) {
								label = uNodeChild.getTextContent();
							} else if (uNodeChild.getNodeName().equals("br")) {
								uText += " ";
							}
						} else if (uNodeChild.getNodeType() == Node.TEXT_NODE) {
							uText += uNodeChild.getTextContent();
						}
					}

					uText = uText.trim();

					// Create annotation Underlined
					Underlined context = new Underlined();
					context.setId(ulabelId);
					context.setLabel(label);
					context.setText(uText);
					context.setBegin(annoOffset);
					documentText.append(uText);
					annoOffset += uText.length();
					context.setEnd(annoOffset);
					annoMap.put("uText", context);

					documentText.append(" ");
					annoOffset += 1;
				}
			}
		}

		return annoMap;
	}

	private QuestionAnswerSet annoateQuestion(Element qElement) {
		// parse single small question
		String qid = "Q" + tempYear + "_" + qElement.getAttribute("id").substring(1);
		String questionLabel = qElement.getElementsByTagName("label").item(0)
				.getTextContent().trim();
		String ansType = qElement.getAttribute("answer_type");
		String knowledgeType = qElement.getAttribute("knowledge_type");
		String ansColumn = qElement.getElementsByTagName("ansColumn").item(0)
				.getTextContent().trim();	// answer id

		Instruction instr = new Instruction();

		ArrayList<QData> qDataList = new ArrayList<QData>();
		ArrayList<AnswerChoice> answerChoiceList = new ArrayList<AnswerChoice>();

		for (int i = 0; i < qElement.getChildNodes().getLength(); i++) {
			Node questionChild = qElement.getChildNodes().item(i);

			if (questionChild.getNodeType() == Node.ELEMENT_NODE) {
				String elementName = questionChild.getNodeName();
				if (elementName.equals("instruction")) {
					// Create annotation Instruction, assuming only one for each
					// question
					Element instrEle = (Element) questionChild;
					String instText = instrEle.getTextContent().trim();

					String instructionMarker = questionLabel + ": ";

					documentText.append(instructionMarker);
					annoOffset += instructionMarker.length();

					// before format text
					int instrBegin = annoOffset;
					ArrayListMultimap<String, Annotation> generatedTags = formatTaggedText(
							instrEle);
					// after format text
					int instrEnd = annoOffset;

					ArrayList<Refs> refList = new ArrayList<Refs>();
					for (Annotation instrRefAnno : generatedTags.get("ref")) {
						// for(Object o: list)
						Refs instrRef = (Refs) instrRefAnno;
						refList.add(instrRef);
					}
					instr.setText(instText);
					instr.setRefList(refList);

					instr.setBegin(instrBegin);
					instr.setEnd(instrEnd);

					documentText.append("\n\n");
					annoOffset += 2;
				} else if (elementName.equals("data")) {
					// Create annotation QData
					Element qDataEle = (Element) questionChild;
					String type = qDataEle.getAttribute("type").trim();
					if (type == null || !type.equals("text")) {	// ignore image
						continue;
					}
					String dataId = qDataEle.getAttribute("id");
					String dataText = qDataEle.getTextContent().trim();

					String qDataMarker = "[Question Data] \n";
					documentText.append(qDataMarker);
					annoOffset += qDataMarker.length();

					int qDataBegin = annoOffset;
					ArrayListMultimap<String, Annotation> generatedTags = formatTaggedText(
							qDataEle);
					int qDataEnd = annoOffset;

					// Create annotation QData
					QData qData = new QData();
					qData.setBegin(qDataBegin);
					qData.setEnd(qDataEnd);
					qData.setId(dataId);
					qData.setText(dataText);

					documentText.append("\n\n");
					annoOffset += 2;

					// Add tags into qData (these are just type conversion)
					ArrayList<Gaps> gaps = new ArrayList<Gaps>();

					for (Annotation blankAnno : generatedTags.get("blank")) {
						Gaps instrRef = (Gaps) blankAnno;
						gaps.add(instrRef);
					}

					ArrayList<ListItem> listItems = new ArrayList<ListItem>();

					for (Annotation listAnno : generatedTags.get("lText")) {
						ListItem listItem = (ListItem) listAnno;
						listItems.add(listItem);
					}

					ArrayList<Refs> refs = new ArrayList<Refs>();

					for (Annotation refAnno : generatedTags.get("ref")) {
						Refs ref = (Refs) refAnno;
						refs.add(ref);
					}

					qData.setGaps(gaps);
					qData.setListItems(listItems);
					qData.setRefs(refs);

					qDataList.add(qData);

				} else if (elementName.equals("choices")) {
					Element choicesEle = (Element) questionChild;
					NodeList choiceList = choicesEle
							.getElementsByTagName("choice");
					for (int j = 0; j < choiceList.getLength(); j++) {
						Element choice = (Element) choiceList.item(j);

						// Create annotation AnswerChoice
						String choiceMarker = "    ";

						documentText.append(choiceMarker);

						annoOffset += choiceMarker.length();

						AnswerChoice ansChoice = new AnswerChoice();

						ansChoice.setBegin(annoOffset);
						ArrayListMultimap<String, Annotation> generatedTags = formatTaggedText(
								choice);
						ansChoice.setEnd(annoOffset);

						documentText.append("\n");
						annoOffset += 1;

						Annotation cNum = generatedTags.get("cNum").get(0);

						String ansChoiceId = documentText.substring(
								cNum.getBegin(), cNum.getEnd());

						ansChoice.setId(ansChoiceId);
						ansChoice.setText(choice.getTextContent().trim());//

						ArrayList<Refs> refList = new ArrayList<Refs>();
						int refIdx=0;
						// ref is weird
						for (Annotation refAnno : generatedTags.get("ref")) {
							Refs instrRef = (Refs) refAnno;
							String refText=choice.getElementsByTagName("ref").item(refIdx).getNextSibling().getTextContent();
							instrRef.setText(refText);
							refList.add(instrRef);
							refIdx++;
						}

						ansChoice.setRefList(refList);

						Integer correctChoice = hshAnswers.get(qid);
						if (correctChoice == null) {
							answerChoiceList.add(ansChoice);
							continue;
						}
						if (correctChoice == j + 1) {
							ansChoice.setIsCorrect(true);
						} else {
							ansChoice.setIsCorrect(false);
						}
						answerChoiceList.add(ansChoice);

					}
				}
			}
		}

		// ////////////////////////////////////////////////

		documentText.append("\n");
		annoOffset += 1;

		// Create annotation Question
		// question is associate with meta data not related to document text.
		Question question = new Question();

		question.setId(qid);
		question.setKnowledgeType(knowledgeType);
		question.setQuestionType(ansType);
		question.setInstruction(instr);

		question.setQdataList(qDataList);

		QuestionAnswerSet qaSet = new QuestionAnswerSet();
		qaSet.setQuestion(question);

		qaSet.setAnswerChoiceList(answerChoiceList);
		return qaSet;
	}

	private Data extractContextFromData(Element dataElement) {
		String contextDataMarker = "[Context]\n";

		documentText.append(contextDataMarker);
		annoOffset += contextDataMarker.length();

		// Create annotation Data
		Data dataObj = new Data();
		String dataId = dataElement.getAttribute("id");
		dataObj.setId(dataId);

		dataObj.setBegin(annoOffset);		// belong to annotation
		ArrayListMultimap<String, Annotation> generatedTags = formatTaggedText(
				dataElement);
		dataObj.setEnd(annoOffset);

		documentText.append("\n\n");
		annoOffset += 2;

		ArrayList<Underlined> uTexts = new ArrayList<Underlined>();
		for (Annotation uText : generatedTags.get("uText")) {
			uTexts.add((Underlined) uText);
		}

		ArrayList<Gaps> gaps = new ArrayList<Gaps>();

		for (Annotation blank : generatedTags.get("blank")) {
			gaps.add((Gaps) blank);
		}

		dataObj.setUnderlinedList(uTexts);

		dataObj.setGapList(gaps);

		dataObj.setText(documentText.substring(dataObj.getBegin(),
				dataObj.getEnd()));
		return dataObj;
	}

	/**
	 * Read input file to string
	 * Do some filtering as well (filter two useless lines which may cause exception)
	 * @return	A string of input file content
	 * @throws Exception
	 */
	private String readTestFile() throws Exception {
		// open input file list iterator
		BufferedReader bfr = null;		
		String xmlText = "";
		try {
			// xmlText = FileUtils.readFileToString(testFile[nCurrFile]);
			xmlText = myReadXmlToString(testFile[nCurrFile]);
			tempYear = getYearFromFileName(testFile[nCurrFile].getName());

			System.out
					.println("Read: " + testFile[nCurrFile].getAbsolutePath());

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (bfr != null) {
				bfr.close();
				bfr = null;
			}
		}
		return xmlText;
	}

	/**
	 * Parse a given document, and stores big question set nodes in documents variable
	 * @param xmlText	Input document string (xml format)
	 * @throws Exception
	 */
	private void parseTestDocument(String xmlText) throws Exception {
		// read a document and stores big question sets in documents
		DOMParser parser = new DOMParser();		// read xml
		parser.parse(new InputSource(new StringReader(xmlText)));
		Document document = parser.getDocument();

		NodeList docNodeList = document.getElementsByTagName("question");	// big questions

		for (int i = 0; i < docNodeList.getLength(); i++) {

			Element questionElement = (Element) docNodeList.item(i);
			String isDoc = questionElement.getAttribute("minimal");		// this is a question set or single question

			if (isDoc.equalsIgnoreCase("no")) {	// no means this is a question set
				documents.add(docNodeList.item(i));
			}
		}
	}

	public boolean hasNext() {
		// return nCurrFile < 10;
		// return nCurrFile < testFile.length;
		if (nCurrFile < testFile.length && nCurrDoc < documents.size()) {
			return true;
		}
		return false;
	}
	
	private String getYearFromFileName(String fileName){
		return fileName.trim().split("-")[1];
	}

	private class OnlyNXML implements FilenameFilter {
		String ext;

		public OnlyNXML(String ext) {
			this.ext = "." + ext;
		}

		public boolean accept(File dir, String name) {
			return name.endsWith(ext);
		}
	}
	
	private String myReadXmlToString(File infile){
		StringBuffer resBuffer = new StringBuffer();
		try {
			Scanner scanner = new Scanner(infile);
			while(scanner.hasNextLine()){
				String line = scanner.nextLine().trim();
				if (line.startsWith("<!DOCTYPE") || line.startsWith("<?xml-stylesheet")){
					continue;
				} else {
					resBuffer.append(line + "\n");
				}
				
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return resBuffer.toString();
	}

}

