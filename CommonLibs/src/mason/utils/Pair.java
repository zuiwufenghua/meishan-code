package mason.utils;

public class Pair<K,V>
{
    public K key;
    public V value;
    private boolean bSortedByKey;
    
    public Pair(K key, V val)
    {
        this.key = key;
        this.value = val;
    }
    
    public Pair(K key, V val, boolean bSortedByKey)
    {
        this.key = key;
        this.value = val;
        this.bSortedByKey = bSortedByKey;
    }
    
    
    @Override
    public String toString()
    {
        return "<" + key.toString() + ", " + value.toString() + ">";
    }
    
    @Override
    public boolean equals(Object obj)
    {
    	if (obj == null || !(obj instanceof Pair)) return false;
    	@SuppressWarnings("rawtypes")
		Pair p = (Pair)obj;
    	return key.equals(p.key) && value.equals(p.value); 
    }
    
    @Override
    public int hashCode()
    {
    	return key.hashCode() * 17 + value.hashCode() + 1;
    }
    
    public int compareTo(Object obj)
    {
    	if (obj == null || !(obj instanceof Pair)) return 1;
    	Pair p = (Pair)obj;
    	if(!bSortedByKey)
    	{
	    	if(value instanceof Double || value instanceof Integer || value instanceof Float || value instanceof Long)
	    	{
	    		Double pValue = (Double)value;
	    		return pValue.compareTo((Double)p.value);
	    	}
	    	else
	    	{
	    		return value.toString().compareTo(p.value.toString());
	    	}
    	}
    	else
    	{
	    	if(key instanceof Double || key instanceof Integer || key instanceof Float || key instanceof Long)
	    	{
	    		Double pkey = (Double)key;
	    		return pkey.compareTo((Double)p.key);
	    	}
	    	else
	    	{
	    		return key.toString().compareTo(p.key.toString());
	    	}
    	}
    }
}

