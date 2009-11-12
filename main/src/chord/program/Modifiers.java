/*
 * Copyright (c) 2006-07, The Trustees of Stanford University.  All
 * rights reserved.
 * Licensed under the terms of the GNU GPL; see COPYING for details.
 */
package chord.program;

/**
 * Modifiers of classes, fields, and methods.
 * 
 * @author Mayur Naik (mhn@cs.stanford.edu)
 */
public class Modifiers {
	public static final int ABSTRACT	= 0x0001;
	public static final int INTERFACE	= 0x0002;
	public static final int NATIVE		= 0x0004;
	public static final int PRIVATE		= 0x0008;
	public static final int PROTECTED	= 0x0010;
	public static final int PUBLIC		= 0x0020;
	public static final int STATIC		= 0x0040;
	public static final int FINAL		= 0x0080;
	public static final int VOLATILE	= 0x0100;
	public static String toString(int modifiers) {
		String s = "";
		if ((modifiers & ABSTRACT) != 0)
			s += "abstract ";
		if ((modifiers & INTERFACE) != 0)
			s += "interface ";
		if ((modifiers & NATIVE) != 0)
			s += "native ";
		if ((modifiers & PRIVATE) != 0)
			s += "private ";
		if ((modifiers & PROTECTED) != 0)
			s += "protected ";
		if ((modifiers & PUBLIC) != 0)
			s += "public ";
		if ((modifiers & STATIC) != 0)
			s += "static ";
		if ((modifiers & FINAL) != 0)
			s += "final ";
		if ((modifiers & VOLATILE) != 0)
			s += "volatile ";
		return s;
	}
};
