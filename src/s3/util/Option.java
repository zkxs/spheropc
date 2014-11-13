package s3.util;

/**
 * Optionally contains a value
 * @author Michael Ripley (<a href="mailto:michael-ripley@utulsa.edu">michael-ripley@utulsa.edu</a>) Nov 13, 2014
 * @param <T>
 */
public class Option<T>
{	
	private final T item;
	
	public Option(T item)
	{
		this.item = item;
	}
	
	public T get()
	{
		return item;
	}
	
	public boolean isNull()
	{
		return item == null;
	}
	
	@SuppressWarnings("unchecked")
	public final static Option<?> EMPTY = new Option(null);
}
