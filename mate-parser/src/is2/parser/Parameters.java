/**
 * 
 */
package is2.parser;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import is2.data.FV;
import is2.data.IFV;
import is2.data.Instances;
import is2.data.Parse;

/**
 * @author Bernd Bohnet, 31.08.2009
 * 
 * 
 */
public abstract class Parameters {

	
	public abstract void print();
	
	public abstract void average(double avVal);
	
	public abstract void restore();
	
	public abstract void update(FV act, FV pred, Instances isd, int instc, Parse d, double upd, double e);
	
	public abstract void write(DataOutputStream dos) throws IOException;
	
	public abstract void read(DataInputStream dis ) throws IOException;
	
	public abstract int size();
	
	public abstract void set_update_num(double total_updates);

	/**
	 * @return
	 */
	public abstract IFV getFV() ;
	
}
