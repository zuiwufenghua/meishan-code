package ir.hit.mason.postagger;

import java.io.*;
import java.net.URI;
import java.util.*;

import cc.mallet.types.*;
import cc.mallet.classify.*;
import cc.mallet.pipe.*;

public class POSTaggingByME {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub


		
		Pipe pipe = new POSTaggerFeatureVectorAndLabel(); // the pipe that will do the conversion
		InstanceList traindata = readInstances(args[0], pipe, 100, false); // create training data list
		InstanceList devdata = readInstances(args[1], pipe, 20, false); // create dev data list		
		//data.addThruPipe(it); // feeding the file to the pipe and to the data list		
		// Create a classifier trainer, and use it to create a classifier
		ClassifierTrainer maxentTrainer = new MaxEntTrainer ();
		maxentTrainer.setValidationInstances(devdata);
		Classifier classifier = maxentTrainer.train (traindata);
		
		//save classifier
		
		ObjectOutputStream oos =
	            new ObjectOutputStream(new FileOutputStream (args[2]));
	    oos.writeObject (classifier);
	    oos.close();
	    

	    
	    
	    //read classfier
	    Classifier testclassifier;
        ObjectInputStream ois =
            new ObjectInputStream (new FileInputStream (args[2]));
        testclassifier = (Classifier) ois.readObject();
        ois.close();
        
        // decode and evaluate 
        Pipe testpipe = classifier.getInstancePipe();
        
        //pipe.getTargetAlphabet().dump(System.out);
        
        InstanceList testdata = readInstances(args[3], testpipe, -1, true); // create dev data list
	    
        System.out.println ("The testing accuracy is "+ testclassifier.getAccuracy (testdata));
	    
		
		
		

		//System.out.println ("The training accuracy is "+ classifier.getAccuracy (ilists[0]));
		//System.out.println ("The testing accuracy is "+ classifier.getAccuracy (ilists[1]));

	}
	
	public static InstanceList readInstances(String inputFile, Pipe pipe, int maxnum, boolean bTest) throws Exception
	{
		InstanceList data = new InstanceList(pipe);
		BufferedReader in = new BufferedReader(new InputStreamReader(
				new FileInputStream(inputFile), "UTF8"));
		String sLine = null;
		
		int tokenNum = 0;
		int sentNum = 0;
		while ((sLine = in.readLine()) != null) {
			sLine = sLine.trim();
			if(sLine.isEmpty()) continue;
			sentNum++;
			
			String[] allWordPoss = sLine.split("\\s+");
			
			int sentLength = allWordPoss.length;
			for(int idx = 0; idx < sentLength; idx++)
			{
				String prev2word = "#ST-2#";
				String prev1word = "#ST-1#";
				String curword = "#ST0#";
				String next1word = "#ST+1#";
				String next2word = "#ST+2#";
				String curTag = "#TAG0#";
				
				{
					int tagStartIndex = allWordPoss[idx].lastIndexOf("_");
					if(tagStartIndex >= 0)
					{
						curword = allWordPoss[idx].substring(0, tagStartIndex);
						curTag = allWordPoss[idx].substring(tagStartIndex+1);
					}
					else
					{
						curword = allWordPoss[idx];
					}					
				}
				
				if(bTest && pipe.getTargetAlphabet().lookupIndex(curTag, false) == -1)
				{
					continue;
				}
				
				if(idx >= 2)
				{
					int tagStartIndex = allWordPoss[idx-2].lastIndexOf("_");
					if(tagStartIndex >= 0)
					{
						prev2word = allWordPoss[idx-2].substring(0, tagStartIndex);
					}
					else
					{
						prev2word = allWordPoss[idx-2];
					}
				}
				
				if(idx >= 1)
				{
					int tagStartIndex = allWordPoss[idx-1].lastIndexOf("_");
					if(tagStartIndex >= 0)
					{
						prev1word = allWordPoss[idx-1].substring(0, tagStartIndex);
					}
					else
					{
						prev1word = allWordPoss[idx-1];
					}
				}
								
				
				if(idx < sentLength-1)
				{
					int tagStartIndex = allWordPoss[idx+1].lastIndexOf("_");
					if(tagStartIndex >= 0)
					{
						next1word = allWordPoss[idx+1].substring(0, tagStartIndex);
					}
					else
					{
						next1word = allWordPoss[idx+1];
					}
				}
				
				if(idx < sentLength-2)
				{
					int tagStartIndex = allWordPoss[idx+2].lastIndexOf("_");
					if(tagStartIndex >= 0)
					{
						next2word = allWordPoss[idx+2].substring(0, tagStartIndex);
					}
					else
					{
						next2word = allWordPoss[idx+2];
					}
				}
				
				ArrayList<String> labelAndFeatures = new ArrayList<String>();
				labelAndFeatures.add(curTag);
				
				//unigram features
				labelAndFeatures.add("#W0#"+curword);
				labelAndFeatures.add("#W-1#"+prev1word);
				labelAndFeatures.add("#W-2#"+prev2word);
				labelAndFeatures.add("#W1#"+next1word);
				labelAndFeatures.add("#W2#"+next2word);
				
				//bigram features
				labelAndFeatures.add("#W-2W-1#"+prev2word + "#" +prev1word);
				labelAndFeatures.add("#W-1W0#"+prev1word + "#" +curword);
				labelAndFeatures.add("#W0W1#"+curword + "#" +next1word);
				labelAndFeatures.add("#W1W2#"+next1word + "#" +next2word);
				
				//skip word features
				labelAndFeatures.add("#W-2W0#"+prev2word + "#" +curword);
				labelAndFeatures.add("#W-1W1#"+prev1word + "#" +next1word);
				labelAndFeatures.add("#W0W2#"+curword + "#" +next2word);
				
				//exist cap
				if(curword.toLowerCase().equals(curword)) labelAndFeatures.add("#existUpperLetter#");
				
				URI uri = null;
				try { uri = new URI ("array:" + tokenNum++); }
				catch (Exception e) { throw new RuntimeException (e); }
				
				Instance curInst = new Instance (labelAndFeatures, null, uri, null);
				data.addThruPipe(curInst);
			}
			
			if(maxnum > 0 && sentNum >= maxnum) break;
		}
		
		in.close();
		return data;
	}

}
