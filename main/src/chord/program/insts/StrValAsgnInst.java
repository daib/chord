/*
 * Copyright (c) 2006-07, The Trustees of Stanford University.  All
 * rights reserved.
 * Licensed under the terms of the GNU GPL; see COPYING for details.
 */
package chord.program.insts;

import chord.program.Var;
import chord.util.Assertions;

/**
 * A statement of the form <tt>v = "..."</tt> where <tt>v</tt> is
 * a local variable.
 * 
 * @author Mayur Naik (mhn@cs.stanford.edu)
 */
public class StrValAsgnInst extends Inst {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8362103474496940694L;
	/**
	 * The local variable on the l.h.s. of this statement.
	 */
	private Var var;
	/**
	 * The string constant on the r.h.s. of this statement.
	 */
	private String str;
	public StrValAsgnInst(Var var, String str, int lineNum) {
		super(lineNum);
		Assertions.Assert(var != null);
		Assertions.Assert(str != null);
		this.var = var;
		this.str = str;
	}
	public Var getVar() {
		return var;
	}
	public String getStr() {
		return str;
	}
	public void setVar(Var var) {
		this.var = var;
	}
	public Var[] getUses() {
		return Inst.emptyVarArray;
	}
	public Var[] getDefs() {
		return new Var[] { var };
	}
	public Var[] getVars() {
		return getDefs();
	}
	public void validate() {
		
	}
	public void accept(InstVisitor v) {
		v.visit(this);
	}
	public String toString() {
		return location() + ": " + var + " = " + str;
	}
}
