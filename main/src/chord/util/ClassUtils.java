/*
 * Copyright (c) 2006-07, The Trustees of Stanford University.  All
 * rights reserved.
 * Licensed under the terms of the GNU GPL; see COPYING for details.
 */
package chord.util;

/**
 * Class related utilities.
 *  
 * @author Mayur Naik (mhn@cs.stanford.edu)
 */
public class ClassUtils {
	/**
	 * Determines whether a given class is a subclass of another.
	 * 
	 * @param	a	A class.
	 * @param	b	A class.
	 * @return	true iff class <tt>a</tt> is a subclass of class
	 * 			<tt>b</tt>.
	 */
	public static boolean isSubclass(Class a, Class b) {
		try {
			a.asSubclass(b);
		} catch (ClassCastException ex) {
			return false;
		}
		return true;
	}
}
