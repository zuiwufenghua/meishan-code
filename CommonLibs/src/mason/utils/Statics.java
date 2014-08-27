package mason.utils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class Statics
{
	private static final double EPS = 0.00000001;

	public static <T> void increment(Map<T, Integer> s, T key)
	{
		if (s.containsKey(key))
		{
			int i = s.get(key);
			s.put(key, i + 1);
		}
		else
			s.put(key, 1);
	}

	public static String[] mergeArray(String[] a, String[] b)
	{
		String[] c = new String[a.length + b.length];
		System.arraycopy(a, 0, c, 0, a.length);
		System.arraycopy(b, 0, c, a.length, b.length);
		return c;
	}
	
	private static final long SEED = 13;
	
	public static <T> void shuffle(List<T> r)
	{
		Random rnd = new Random();
		rnd.setSeed(SEED);
		Collections.shuffle(r, rnd);
	}
	
	public static double harmonicMean(double d1, double d2)
	{
		return d1 > 0.0 && d2 > 0.0 ? 2.0 / (1.0 / d1 + 1.0 / d2) : 0.0;
	}
	
	public static String strReplace(String str, String s, String t)
	{
		int idx = str.indexOf(s);
		if (idx == -1) return str;
		else return str.substring(0, idx) + t + str.substring(idx + s.length());
	}
	
	public static boolean doubleEquals(double a, double b)
	{
		if (Math.abs(a - b) < EPS) return true;
		else return false;
	}
	
	public static String trimSpecial(String s)
	{
		while (true) {
			int iLen = s.length();
			if (iLen == 0) break;
			char c;
			if ((c = s.charAt(iLen - 1)) == ' ' || c == '\r' || c == '\n')
				s = s.substring(0, iLen - 1);
			else if ((c = s.charAt(0)) == ' ' || c == '\r' || c == '\n' || c == 0xfeff)
				s = s.substring(1, iLen);
			else break;
		}
		return s;
	}
	
    public static <T> T[] fillArray(T[] a, T v)
    {
        for (int i = 0; i < a.length; ++i)
            a[i] = v;
        return a;
    }

    public static <T> boolean arrayEquals(T[] a, T[] b)
    {
        if (a.length != b.length) return false;
        for (int i = 0; i < a.length; ++i)
            if (!a[i].equals(b[i])) return false;
        return true;
    }

    public static <K,V> boolean dictEquals(Map<K, V> d1, Map<K, V> d2)
    {
        if (d1.size() != d2.size()) return false;
        for (K k : d1.keySet())
            if (!d2.containsKey(k) || !d2.get(k).equals(d1.get(k)))
                return false;
        return true;
    }

    public static <T> boolean setEquals(Set<T> k1, Set<T> k2)
    {
        if (k1.size() != k2.size()) return false;
        for (T k : k1)
            if (!k2.contains(k))
                return false;
        return true;
    }

	public static boolean arrayEquals(double[] a, double[] b) {
        if (a.length != b.length) return false;
        for (int i = 0; i < a.length; ++i)
            if (a[i] != b[i]) return false;
        return true;
	}
	
	public static boolean arrayEquals(int[] a, int[] b) {
        if (a.length != b.length) return false;
        for (int i = 0; i < a.length; ++i)
            if (a[i] != b[i]) return false;
        return true;
	}
	
}
