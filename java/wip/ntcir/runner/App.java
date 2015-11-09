package wip.ntcir.runner;

import java.nio.file.Paths;

import wip.ntcir.preprocessor.HypothesisGenerator;
import wip.ntcir.preprocessor.QuestionSetReader;
import wip.ntcir.type.ConveyerPackage;
import wip.ntcir.type.TestDocument;
import wip.ntcir.util.Util;

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
    			
    	QuestionSetReader questionSetReader = new QuestionSetReader();
    	questionSetReader.initialize(inputDirPath, goldDirPath);
    	ConveyerPackage conveyerPackage =  questionSetReader.getNext();
    	
    	HypothesisGenerator hypothesisGenerator = new HypothesisGenerator();
    	hypothesisGenerator.initialize(conveyerPackage);
    	hypothesisGenerator.process();
    	
    	Util.printTestDocument(conveyerPackage.getTestDocument());
    	System.out.println();
        System.out.println( "Done!" );
    }
}
