/*
 * Copyright (c) 2006-07, The Trustees of Stanford University.  All
 * rights reserved.
 * Licensed under the terms of the GNU GPL; see COPYING for details.
 */
package chord.program;

import chord.util.Assertions;

/**
 * A local variable of reference type.
 * 
 * @author Mayur Naik (mhn@cs.stanford.edu)
 */
public class Var implements java.io.Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3716010886681096593L;
	/**
	 * Name of the variable.
	 */
	private final String name;
	/**
	 * Type of the variable.
	 */
	private final Type type;
	/**
	 * Control-flow graph declaring this variable.
	 */
	private CFG cfg;
	public Var(String name, Type type) {
		Assertions.Assert(name != null);
		Assertions.Assert(type != null);
		this.name = name;
		this.type = type;
	}
	public String getName() {
		return name;
	}
	public Type getType() {
		return type;
	}
	public CFG getCFG() {
		return cfg;
	}
	public Method getMethod() {
		return cfg.getCtnrMethod();
	}
	public void setCFG(CFG cfg) {
		this.cfg = cfg;
	}
	public Var copy() {
		return new Var(name, type);
	}
	public String toString() {
		return name;
	}
}
