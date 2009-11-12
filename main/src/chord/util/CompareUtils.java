/*
 * Copyright (c) 2006-07, The Trustees of Stanford University.  All
 * rights reserved.
 * Licensed under the terms of the GNU GPL; see COPYING for details.
 */
package chord.util;

/**
 * Object comparison utilities.
 * 
 * @author Mayur Naik (mhn@cs.stanford.edu)
 */
public class CompareUtils {
	public static boolean areEqual(Object a, Object b) {
		return a == null ? b == null : a.equals(b);
	}
}
