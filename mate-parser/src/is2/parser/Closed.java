package is2.parser;

import is2.data.Parse;


final public class Closed  {

	public float p;
	short b,e,m;
	byte dir;
	
	Closed d;
	Open u;
	
	boolean bfaked = false;
	
	public Closed() {
		this.p=(-1.0F / 0.0F);
		b=e=m=-1;
		bfaked = true;
	}

	public Closed(short s, short t, int m, int dir,Open u, Closed d, float score) {
		bfaked = false;
		this.b = s;
		this.e = t;
		this.m = (short)m;
		this.dir = (byte)dir;
		this.u=u;
		this.d =d;
		p=score;
	}


	public void create(Parse parse) {
		if (u != null) u.create(parse);
		if (d != null) d.create(parse);
	}
	
	public int compareTo(Closed other)
	{
		if(p < other.p || bfaked)
		    return -1;
		if(p > other.p || other.bfaked)
		    return 1;
		return 0;
	}
	
	public String toString()
	{
		if(d != null && u != null)
		{
			return String.format("s=%d,e=%d,dir=%d,m=%d,score=%f(oe=%d,ol=%d,cm=%d)", b, e,dir,m, p, (dir==1)?u.e:u.s,u.label,d.m);
		}
		else if(d == null && u != null)
		{
			return String.format("s=%d,e=%d,dir=%d,m=%d,score=%f(oe=%d,ol=%d,cm=%d)", b, e,dir,m, p, (dir==1)?u.e:u.s,u.label,-1);
		}
		else if(d != null && u == null)
		{
			return String.format("s=%d,e=%d,dir=%d,m=%d,score=%f(oe=%d,ol=%d,cm=%d)", b, e,dir,m, p, (dir==1)?d.b:d.e,-1,d.m);
		}
		else
		{
			return String.format("s=%d,e=%d,dir=%d,m=%d,score=%f(oe=%d,ol=%d,cm=%d)", b, e,dir,m, p, -1,-1,-1);
		}
	}
}


