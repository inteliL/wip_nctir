package edu.wip.ntcir.runner;

import java.nio.file.Path;
import java.nio.file.Paths;

import edu.wip.ntcir.preprocessor.HypothesisGenerator;
import edu.wip.ntcir.preprocessor.QuestionSetReader;
import edu.wip.ntcir.type.ConveyerPackage;
import edu.wip.ntcir.type.TestDocument;
import edu.wip.ntcir.util.Util;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws Exception
    {
    	String inputDirPath = Paths.get("/Users/luo123n/Projects/ntcir/data/input", "question-en-training-national_center_test").toString();
    	String goldDirPath = Paths.get("/Users/luo123n/Projects/ntcir/data/input", "answer-training-national_center_test").toString();
    	
    	Path outDirForKB = Paths.get("/Users/luo123n/Projects/ntcir/data/output");
    	
    	QuestionSetReader questionSetReader = new QuestionSetReader();
    	questionSetReader.initialize(inputDirPath, goldDirPath);
    	
    	Util.writeAllQuestionAnswerSetToDir(questionSetReader, outDirForKB);
    	System.exit(0);
    	
    	ConveyerPackage conveyerPackage =  questionSetReader.getNext();
    	
    	HypothesisGenerator hypothesisGenerator = new HypothesisGenerator();
    	hypothesisGenerator.initialize(conveyerPackage);
    	hypothesisGenerator.process();
    	
    	Util.printTestDocument(conveyerPackage.getTestDocument());
    	System.out.println();
        System.out.println( "Done!" );
    }
}
