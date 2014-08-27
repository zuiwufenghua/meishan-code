package mason.dep;



public class DepErrors {
	public String m_type;
	public String[] m_contents;
	public double m_times;
	
	public DepErrors(String strContent, double times)
	{
		String[] data_units = strContent.split("#=#");
		int content_length = data_units.length;
		m_type = data_units[0];
		m_contents = new String[content_length];
		m_contents[0] = String.format("%d", data_units.length-1);
		for(int i = 1; i < content_length; i++)m_contents[i] = data_units[i];
		m_times = times;
	}
	
	public String print()
	{
		String printContent = m_type;
		if(m_contents != null)
		{
			for(int i = 0; i < m_contents.length; i++)
			{
				printContent = printContent + "\t" + m_contents[i];
			}
		}
		return String.format("%s\t%f", printContent, m_times);
	}
	
	
}


