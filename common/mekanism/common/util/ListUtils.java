package mekanism.common.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class ListUtils
{
	public static <V> List<V> inverse(List<V> list)
	{
		List<V> toReturn = new ArrayList<V>();

		for(int i = list.size() - 1; i >= 0; i--)
		{
			toReturn.add(list.get(i));
		}

		return toReturn;
	}

	public static <V> List<V> cap(List<V> list, int cap)
	{
		List<V> toReturn = new ArrayList<V>();

		if(list.size() <= cap)
		{
			toReturn = copy(list);
		}
		else {
			int count = 0;

			for(V obj : list)
			{
				count++;

				toReturn.add(obj);

				if(count == cap)
				{
					break;
				}
			}
		}

		return toReturn;
	}

	public static <V> List<V> copy(List<V> list)
	{
		List<V> toReturn = new ArrayList<V>();

		for(V obj : list)
		{
			toReturn.add(obj);
		}

		return toReturn;
	}

	public static <V> List<V> merge(List<V> listOne, List<V> listTwo)
	{
		List<V> newList = new ArrayList<V>();

		for(V obj : listOne)
		{
			newList.add(obj);
		}

		for(V obj : listTwo)
		{
			newList.add(obj);
		}

		return newList;
	}

	public static <V> List<V> capRemains(List<V> list, int cap)
	{
		List<V> toReturn = new ArrayList<V>();

		if(list.size() <= cap)
		{
			return toReturn;
		}
		else {
			List<V> inverse = inverse(list);

			int iterNeeded = list.size() - cap;
			int count = 0;

			for(V obj : list)
			{
				count++;

				toReturn.add(obj);

				if(count == iterNeeded)
				{
					break;
				}
			}

			return toReturn;
		}
	}

	public static <V> ArrayList<List<V>> split(List<V> list, int divide)
	{
		int remain = list.size() % divide;
		int size = (list.size() - remain) / divide;

		ArrayList<List<V>> toReturn = new ArrayList<List<V>>();

		for(int i = 0; i < divide; i++)
		{
			toReturn.add(i, new ArrayList<V>());
		}

		for(List<V> iterSet : toReturn)
		{
			List<V> removed = new ArrayList<V>();

			int toAdd = size;

			if(remain > 0)
			{
				remain--;
				toAdd++;
			}

			for(V obj : list)
			{
				if(toAdd == 0)
				{
					break;
				}

				iterSet.add(obj);
				removed.add(obj);
				toAdd--;
			}

			for(V obj : removed)
			{
				list.remove(obj);
			}
		}

		return toReturn;
	}

	public static <V> V getTop(List<V> list)
	{
		for(V obj : list)
		{
			return obj;
		}

		return null;
	}

	public static <V> List<V> asList(Set<V> set)
	{
		return (List<V>)Arrays.asList(set.toArray());
	}
	
	public static <V> List<V> asList(V... values)
	{
		return (List<V>)Arrays.asList(values);
	}
}
