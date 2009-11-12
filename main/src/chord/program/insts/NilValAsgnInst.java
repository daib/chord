/*
 * Copyright (c) 2006-07, The Trustees of Stanford University.  All
 * rights reserved.
 * Licensed under the terms of the GNU GPL; see COPYING for details.
 */
package chord.program.insts;

import chord.program.Var;
import chord.util.Assertions;

/**
 * A statement of the form <tt>v = null</tt> where <tt>v</tt>
 * is a local variable.
 * 
 * @author Mayur Naik (mhn@cs.stanford.edu)
 */
public class NilValAsgnInst extends Inst {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6342510667781436660L;
	/**
	 * The local variable on the l.h.s. of this statement.
	 */
	private Var var;
	public NilValAsgnInst(Var var, int lineNum) {
		super(lineNum);
		Assertions.Assert(var != null);
		this.var = var;
	}
	public Var getVar() {
		return var;
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
		return new Var[] { var };
	}
	public void validate() {
		
	}
	public void accept(InstVisitor v) {
		v.visit(this);
	}
	public String toString() {
		return location() + ": " + var + " = null";
	}
}
