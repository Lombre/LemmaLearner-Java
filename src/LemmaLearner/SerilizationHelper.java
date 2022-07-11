package LemmaLearner;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;


import org.nustaq.serialization.FSTConfiguration;
import org.nustaq.serialization.FSTObjectInput;
import org.nustaq.serialization.FSTObjectOutput;

public class SerilizationHelper {

	private final static FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();
	
	
	public static void save(Object objectToSave, String fileLocation) {
		try {
			FileOutputStream fileOutputStream = new FileOutputStream(fileLocation);
			FSTObjectOutput out = conf.getObjectOutput(fileOutputStream);
		    out.writeObject(objectToSave);
		    out.flush();
		    fileOutputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
			throw new Error("Saving object \"" + objectToSave.toString() + "\" at location \"" + fileLocation + "\" failed.");
		}				
	}
	
	public static Object load(String fileLocation) {
	    FileInputStream fileInputStream;
		try {
			fileInputStream = new FileInputStream(fileLocation);
			FSTObjectInput in = new FSTObjectInput(fileInputStream);
			Object result = in.readObject();
			in.close();
			return result;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	

}
