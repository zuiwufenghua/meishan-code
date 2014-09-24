package is2.parser;

import is2.data.Parse;


final public  class Open {

	public float p;
	short s, e, label;
	byte dir;

	Closed left;
	Closed right;
	boolean bfaked = false;
	
	public Open() {
		this.p=(-1.0F / 0.0F);
		s=e=label=-1;
		bfaked = true;
	}

	public Open(short s, short t, short dir, short label,Closed left, Closed right, float p) {
		bfaked = false;
		this.s = s;
		this.e = t;
		this.label = label;
		this.dir = (byte)dir;
		this.left =left;
		this.right=right;
		this.p=p;
	}


	void create(Parse parse) {
		if (dir == 0) {
			parse.heads[s] = e;
			if (label != -1) parse.labels[s] = label;
		} else {
			parse.heads[e] = s;
			if (label != -1) parse.labels[e] = label;
		}
		if (left != null) left.create(parse);
		if (right != null) right.create(parse);
	}
	
	public int compareTo(Open other)
	{
		if(p < other.p || bfaked)
		    return -1;
		if(p > other.p || other.bfaked)
		    return 1;
		return 0;
	}
	
	public String toString()
	{
		if(left != null && right != null)
		{
			return String.format("s=%d,e=%d,dir=%d,label=%d,score=%f(r=%d,lm=%d,rm=%d)", s, e,dir,label,p, left.e,left.m, right.m);
		}
		else if(left == null && right != null)
		{
			return String.format("s=%d,e=%d,dir=%d,label=%d,score=%f(r=%d,lm=%d,rm=%d)", s, e,dir,label,p, right.b-1,-1, right.m);
		}
		else if(left != null && right == null)
		{
			return String.format("s=%d,e=%d,dir=%d,label=%d,score=%f(r=%d,lm=%d,rm=%d)", s, e,dir,label,p, left.e,left.m, -1);
		}
		else
		{
			return String.format("s=%d,e=%d,dir=%d,label=%d,score=%f(r=%d,lm=%d,rm=%d)", s, e,dir,label,p, -1,-1, -1);
		}
	}
	
}
