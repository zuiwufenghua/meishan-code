package ensemble;

public interface Dependency {
	public int head();
	public int mod();
	public String label();
	public String pos();
	public double score();
	public boolean sameDependency(Dependency other);
}
