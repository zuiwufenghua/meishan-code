package ir.hit.mason.postagger;

import java.io.*;
import java.util.ArrayList;

import cc.mallet.pipe.Pipe;
import cc.mallet.types.*;


/**
 * Convert a list of strings into a feature sequence
 */

public class POSTaggerFeatureVectorAndLabel extends Pipe {

    public long totalNanos = 0;

	public POSTaggerFeatureVectorAndLabel (Alphabet dataDict, Alphabet targetAlphabet) {
		super (dataDict, targetAlphabet);
	}

	public POSTaggerFeatureVectorAndLabel () {
		super(new Alphabet(), new LabelAlphabet());
	}
	
	public Instance pipe (Instance carrier) {

		long start = System.nanoTime();

		try {
			ArrayList<String> tokens = (ArrayList<String>) carrier.getData();
			assert(tokens.size() > 1);
		    Label label = ((LabelAlphabet)getTargetAlphabet()).lookupLabel(tokens.get(0), true);
		    carrier.setTarget(label);
		    
		    ArrayList<Integer> indices = new ArrayList<Integer>();
		    ArrayList<Double> values = new ArrayList<Double>();
			
			for (int i = 1; i < tokens.size()-1; i++) {
				int index = getDataAlphabet().lookupIndex(tokens.get(i), true);
				indices.add(index);
				values.add(1.0);
			}
			
		    assert(indices.size() == values.size());
		    int[] indicesArr = new int[indices.size()];
		    double[] valuesArr = new double[values.size()];
		    for (int i = 0; i < indicesArr.length; i++) {
		      indicesArr[i] = indices.get(i);
		      valuesArr[i] = values.get(i);
		    }
		    
		    FeatureVector fv = new FeatureVector(getDataAlphabet(), indicesArr, valuesArr);
		    carrier.setData(fv);
			//carrier.setData(featureSequence);
			
			totalNanos += System.nanoTime() - start;
		} catch (ClassCastException cce) {
			System.err.println("Expecting ArrayList<String>, found " + carrier.getData().getClass());
		}

		return carrier;
	}

	static final long serialVersionUID = 1;
}
