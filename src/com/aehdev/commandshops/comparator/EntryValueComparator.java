package com.aehdev.commandshops.comparator;

import java.util.Comparator;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class EntryValueComparator.
 */
@SuppressWarnings("rawtypes")
public class EntryValueComparator implements Comparator
{

	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object) */
	public int compare(Object o1, Object o2)
	{
		return compare((Map.Entry)o1, (Map.Entry)o2);
	}

	/**
	 * Compare.
	 * @param e1
	 * the e1
	 * @param e2
	 * the e2
	 * @return the int
	 */
	@SuppressWarnings("unchecked")
	public int compare(Map.Entry e1, Map.Entry e2)
	{
		int cf = ((Comparable)e1.getValue()).compareTo(e2.getValue());
		if(cf == 0)
		{
			cf = ((Comparable)e1.getKey()).compareTo(e2.getKey());
		}
		return cf;
	}
}
