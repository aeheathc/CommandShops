package com.aehdev.commandshops;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;

// TODO: Auto-generated Javadoc
/**
 * The Class SortedProperties.
 */
public class SortedProperties extends Properties
{

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/* (non-Javadoc)
	 * @see java.util.Hashtable#keys() */
	@SuppressWarnings({"rawtypes", "unchecked"})
	public Enumeration keys()
	{
		Enumeration keysEnum = super.keys();
		Vector<String> keyList = new Vector<String>();
		while(keysEnum.hasMoreElements())
		{
			keyList.add((String)keysEnum.nextElement());
		}
		Collections.sort(keyList);
		return keyList.elements();
	}
}
