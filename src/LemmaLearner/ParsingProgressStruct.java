package LemmaLearner;

import java.io.File;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class ParsingProgressStruct {
	
	
	long absoluteStartTime;
	long totalFileSpaceConsumption;
	long accumulatedFileSpaceConsumption;
	public long parsedTextCounter;
	
	public ParsingProgressStruct(List<File> textFilesInFolder) {
		absoluteStartTime = System.currentTimeMillis();		
		totalFileSpaceConsumption = textFilesInFolder.stream()
													  .map(file -> file.length())
													  .reduce(0L, (subtotal, element) -> subtotal + element);
		accumulatedFileSpaceConsumption = 0;			
		parsedTextCounter = 0;
	}
}
