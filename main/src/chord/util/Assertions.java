/*
 * Copyright (c) 2006-07, The Trustees of Stanford University.  All
 * rights reserved.
 * Licensed under the terms of the GNU GPL; see COPYING for details.
 */
package chord.util;

/**
 * Error handling utilities.
 *
 * @author Mayur Naik (mhn@cs.stanford.edu)
 */
public class Assertions {
	public static void Assert(boolean cond) {
		if (!cond) {
			throw new RuntimeException("");
		}
	}
	public static void Assert(boolean cond, String mesg) {
		if (!cond) {
			throw new RuntimeException(mesg);
		}
	}
}
