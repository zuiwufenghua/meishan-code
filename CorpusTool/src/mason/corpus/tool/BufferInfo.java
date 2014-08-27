package mason.corpus.tool;

import java.util.*;

public class BufferInfo {
	List<Integer> fileIds;
	List<Integer> bufferStartIds;
	List<Integer> bufferEndIds;
		
	public BufferInfo()
	{
		fileIds = new ArrayList<Integer>();
		bufferStartIds = new ArrayList<Integer>();
		bufferEndIds = new ArrayList<Integer>();
	}

}
