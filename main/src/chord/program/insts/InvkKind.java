/*
 * Copyright (c) 2006-07, The Trustees of Stanford University.  All
 * rights reserved.
 * Licensed under the terms of the GNU GPL; see COPYING for details.
 */
package chord.program.insts;

/**
 * The kind of a method invocation statement.
 * 
 * @author Mayur Naik (mhn@cs.stanford.edu)
 */
public enum InvkKind {
	/**
	 * An invokevirtual statement.
	 */
	INVK_VIRTUAL,
	/**
	 * An invokestatic statement.
	 */
	INVK_STATIC,
	/**
	 * An invokeinterface statement.
	 */
	INVK_INTERFACE,
	/**
	 * An invokespecial statement.
	 */
	INVK_SPECIAL
}
