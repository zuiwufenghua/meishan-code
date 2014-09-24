package is2.parser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import is2.data.DataF;
import is2.data.Parse;
import is2.data.ParseNBest;
import is2.util.DB;


/**
 * @author Bernd Bohnet, 01.09.2009
 * 
 * This methods do the actual work and they build the dependency trees. 
 */
final public class Decoder   {

	public static long timeDecotder;
	public static long timeRearrange;

	/**
	 * Threshold for rearrange edges non-projective
	 */
	public static float NON_PROJECTIVITY_THRESHOLD = 0.3F;

	/**
	 * Build a dependency tree based on the data
	 * @param pos part-of-speech tags
	 * @param x the data
	 * @param projective projective or non-projective
	 * @param edges the edges
	 * @return a parse tree
	 * @throws InterruptedException
	 */
	public static Parse  decode(short[] pos,  DataF x, boolean projective) throws InterruptedException {

		long ts = System.nanoTime();
		
		
		final int n = pos.length;

		final Open O[][][][] = new Open[n][n][2][];
		final Closed C[][][][] = new Closed[n][n][2][];


		int maxThreads =(pos.length>Parser.THREADS)? Parser.THREADS: pos.length;
		//maxThreads=1; // change this change this change this!!!!!
		ParallelDecoder[] pd = new ParallelDecoder[maxThreads];

		
		int threads =  maxThreads;
		for(int i=0;i<threads ;i++)  pd[i]= new ParallelDecoder(pos, x,  O, C, n);

		boolean start =true;
		
		for (short k = 1; k < n; k++) {
		
			int thread=0;

			// provide the threads the data
			for (short s = 0; s < n; s++) {
				short t = (short) (s + k);
				if (t >= n) break;
				
				pd[thread++].add(s,t);
				if (thread>=threads)thread=0;
			}
			
			// start or continue the threads
			if (start)	{
				for(int i=0;i<threads;i++)  pd[i].start();	
				start =false;
			} else for(int i=0;i<threads;i++)  pd[i].waiting=false;				

			
			// wait until last thread has finished this round; next needs the results
			int i=threads-1;
			while(i>=0) {
				ParallelDecoder pdx = pd[i];
				if (ParallelDecoder.sets.size()==0 && pdx.waiting)  i--;					 
				else Thread.yield();				
			}
		}
		
		// end the threads
		for(int i=0;i<threads;i++) {
			pd[i].done=true;
			pd[i].waiting=false;
		}		
	
		for(int i=0;i<threads;i++) {
			pd[i].join(1000);
			if (pd[i].isAlive())DB.println("error thread is still alive ");
		}
		
		
			
		
		float bestSpanScore = (-1.0F / 0.0F);
		Closed bestSpan = null;
		for (int m = 1; m < n; m++)
			if (C[0][n - 1][1][m].p > bestSpanScore) {
				bestSpanScore = C[0][n - 1][1][m].p;
				bestSpan = C[0][n - 1][1][m];
			}

		// build the dependency tree from the chart 
		Parse out= new Parse(pos.length);

		bestSpan.create(out);

		out.heads[0]=-1;
		out.labels[0]=0;

		timeDecotder += (System.nanoTime()-ts);
		 
		ts = System.nanoTime();
		
		if (!projective) rearrange(pos, out.heads, out.labels,x);
		
		timeRearrange += (System.nanoTime()-ts);		

		return out;
	}

	
	/**
	 * This is the parallel non-projective edge re-arranger
	 *  
	 * @param pos part-of-speech tags
	 * @param heads parent child relation 
	 * @param labs edge labels 
	 * @param x the data
	 * @param edges the existing edges defined by part-of-speech tags 
	 * @throws InterruptedException
	 */
	public static void rearrange(short[] pos, short[] heads, short[] labs,  DataF x) throws InterruptedException {

		int threads =(pos.length>Parser.THREADS)? Parser.THREADS: pos.length;

		ParallelRearrange[] rp = new ParallelRearrange[threads];
		
		// wh  what to change, nPar - new parent, nType - new type
		short wh = -1, nPar = -1,nType = -1;
		
		while(true) {
			boolean[][] isChild = new boolean[heads.length][heads.length];
			for(int i = 1, l1=1; i < heads.length; i++,l1=i)  
				while((l1= heads[l1]) != -1) isChild[l1][i] = true;
		
			float max = Float.NEGATIVE_INFINITY;
			float p = Extractor.encode3(pos, heads, labs, x);

			for(int i=0;i<rp.length;i++)  rp[i]=new ParallelRearrange( isChild, pos,x,heads,labs);
			
			for(int ch = 1; ch < heads.length; ch++) {

				for(short pa = 0; pa < heads.length; pa++) {
					if(ch == pa || pa == heads[ch] || isChild[ch][pa]) continue;

					ParallelRearrange.add(p,(short) ch, pa);
				} 
			}  
			
			for(int i=0;i<threads;i++) rp[i].start();					
			for(int i=0;i<threads;i++)  rp[i].join();
			
			for(int i=0;i<rp.length;i++) 
				if(max < rp[i].max  ) {					
						max = rp[i].max;  	wh = rp[i].wh; 
						nPar = rp[i].nPar;  nType = rp[i].nType ;
				}

			if(max <= NON_PROJECTIVITY_THRESHOLD)	break; // bb: changed from 0.0

			heads[wh] = nPar;
			labs[wh] = nType;

		}
	}


	
	public static String getInfo() {

		return "Decoder non-projectivity threshold: "+NON_PROJECTIVITY_THRESHOLD;

	}
	
	
	/**
	 * Build a dependency tree based on the data
	 * @param pos part-of-speech tags
	 * @param x the data
	 * @param projective projective or non-projective
	 * @param edges the edges
	 * @return a parse tree
	 * @throws InterruptedException
	 */
	public static List<ParseNBest>  decodeNest(short[] pos,  DataF x, int nbest, boolean projective) throws InterruptedException {

		long ts = System.nanoTime();
		
		
		final int n = pos.length;

		final Open O[][][][][] = new Open[n][n][2][][];
		final Closed C[][][][][] = new Closed[n][n][2][][];


		int maxThreads =(pos.length>Parser.THREADS)? Parser.THREADS: pos.length;
		//maxThreads=1; // change this change this change this!!!!!
		ParallelDecoderNBest[] pd = new ParallelDecoderNBest[maxThreads];

		
		int threads =  maxThreads;
		for(int i=0;i<threads ;i++)  pd[i]= new ParallelDecoderNBest(pos, x,  O, C, n, nbest);

		boolean start =true;
		
		for (short k = 1; k < n; k++) {
		
			int thread=0;

			// provide the threads the data
			for (short s = 0; s < n; s++) {
				short t = (short) (s + k);
				if (t >= n) break;
				
				pd[thread++].add(s,t);
				if (thread>=threads)thread=0;
			}
			
			// start or continue the threads
			if (start)	{
				for(int i=0;i<threads;i++)  pd[i].start();	
				start =false;
			} else for(int i=0;i<threads;i++)  pd[i].waiting=false;				

			
			// wait until last thread has finished this round; next needs the results
			int i=threads-1;
			while(i>=0) {
				ParallelDecoderNBest pdx = pd[i];
				if (ParallelDecoderNBest.sets.size()==0 && pdx.waiting)  i--;					 
				else Thread.yield();				
			}
		}
		
		// end the threads
		for(int i=0;i<threads;i++) {
			pd[i].done=true;
			pd[i].waiting=false;
		}		
	
		for(int i=0;i<threads;i++) {
			pd[i].join(1000);
			if (pd[i].isAlive())DB.println("error thread is still alive ");
		}
		
		
			
		KBestListClosed kbestresults = new KBestListClosed(nbest);
		for (int m = 1; m < n; m++)
		{
			for(int k = 0; k < nbest; k++)
			{
				kbestresults.add(C[0][n - 1][1][m][k]);
			}
		}
		List<ParseNBest> parses= new ArrayList<ParseNBest>();
		
		
		for(int k = 0; k < nbest; k++)
		{
			Closed cspan = kbestresults.get(k);
			if(cspan.bfaked)break;
			ParseNBest out= new ParseNBest(pos.length);
			cspan.create(out);
			out.heads[0]=-1;
			out.labels[0]=0;	
			out.f1 = cspan.p;
			ts = System.nanoTime();
			timeDecotder += (System.nanoTime()-ts);
			if (!projective) rearrange(pos, out.heads, out.labels,x);			
			timeRearrange += (System.nanoTime()-ts);
			parses.add(out);
		}
		// build the dependency tree from the chart 
		
		//added by zms 
		/*
		PrintWriter outWriter = null;
		try {
			outWriter = new PrintWriter(new OutputStreamWriter(
					new FileOutputStream(new File("temp.log"),true), "UTF-8"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//open
		
		for(int i1 = 0; i1 < n; i1++)
			for(int i2 = i1+1; i2 < n; i2++)
				for(int i3 = 0; i3 < 2; i3++)
				{
					int labelNum = O[i1][i2][i3].length;
					for(int i4 = 0; i4 < labelNum; i4++)
					{
						int tkbest = O[i1][i2][i3][i4].length;
						for(int i5 = 0; i5 < tkbest; i5++)
						{
							if(O[i1][i2][i3][i4][i5] != null && !O[i1][i2][i3][i4][i5].bfaked)
							{
								outWriter.println(O[i1][i2][i3][i4][i5].toString());
							}
						}
					}
				}
		
		for(int i1 = 0; i1 < n; i1++)
			for(int i2 = i1+1; i2 < n; i2++)
				for(int i3 = 0; i3 < 2; i3++)
				{
					int labelNum = C[i1][i2][i3].length;
					for(int i4 = 0; i4 < n; i4++)
					{
						int tkbest = C[i1][i2][i3][i4].length;
						for(int i5 = 0; i5 < tkbest; i5++)
						{
							if(C[i1][i2][i3][i4][i5] != null && !C[i1][i2][i3][i4][i5].bfaked)
							{
								outWriter.println(C[i1][i2][i3][i4][i5].toString());
							}
						}
					}
				}
		
		outWriter.println();
		outWriter.close();
	*/
		return parses;
	}

}
