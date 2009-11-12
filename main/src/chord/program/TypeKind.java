/*
 * Copyright (c) 2006-07, The Trustees of Stanford University.  All
 * rights reserved.
 * Licensed under the terms of the GNU GPL; see COPYING for details.
 */
package chord.program;

/**
 * The kind of a type.
 * 
 * @author Mayur Naik (mhn@cs.stanford.edu)
 */
public enum TypeKind {
	/**
	 * An interface type.
	 */
	INTERFACE_TYPE,
	/**
	 * An abstract class type.
	 */
	ABSTRACT_CLASS_TYPE,
	/**
	 * A concrete class type.
	 */
	CONCRETE_CLASS_TYPE,
	/**
	 * An array type.
	 */
	ARRAY_TYPE,
	/**
	 * A primitive type or the void type.
	 */
	PRIMITIVE_TYPE,
	/**
	 * The hypothetical null type (subtype of all types).
	 */
	NULL_TYPE
}
