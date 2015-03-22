package mcp.mobius.waila.api;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

public interface ITaggedList<E, T> extends List<E> {
	public boolean add(E e, T tag);
	public boolean add(E e, Collection<? extends T> taglst);
	public Set<T>  getTags(E e);
	public Set<T>  getTags(int index);
	public void    addTag(E e, T tag);
	public void    addTag(int index, T tag);
	public void    removeTag(E e, T tag);	
	public void    removeTag(int index, T tag);
	public Set<E>  getEntries(T tag);
	public void    removeEntries(T tag);
	public String  getTagsAsString(E e);	
}
