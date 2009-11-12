/*
 * Copyright (c) 2006-07, The Trustees of Stanford University.  All
 * rights reserved.
 * Licensed under the terms of the GNU GPL; see COPYING for details.
 */
package chord.program.insts;

import chord.program.Var;
import chord.util.Assertions;

/**
 * A statement of the form <tt>v = b.getClass()</tt> where
 * <tt>v</tt> and <tt>b</tt> are local variables.
 * 
 * @author Mayur Naik (mhn@cs.stanford.edu)
 */
public final class ClsVarAsgnInst extends Inst {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6516968803951018981L;
	/**
	 * The local variable on the l.h.s. of this statement.
	 */
	private Var var;
	/**
	 * The local variable whose <tt>getClass()</tt> method is called
	 * on the r.h.s. of this statement.
	 */
	private Var base;
	public ClsVarAsgnInst(Var var, Var base, int lineNum) {
		super(lineNum);
		Assertions.Assert(var != null);
		this.var = var;
		Assertions.Assert(base != null);
		this.base = base;
	}
	public Var getVar() {
		return var;
	}
	public Var getBase() {
		return base;
	}
	public void setVar(Var var) {
		this.var = var;
	}
	public void setBase(Var var) {
		this.base = var;
	}
	public Var[] getUses() {
		return new Var[] { base };
	}
	public Var[] getDefs() {
		return new Var[] { var };
	}
	public Var[] getVars() {
		return new Var[] { base, var };
	}
	public void validate() {
		/*
		Type clsType = program.getType("java.lang.Class");
		Assertions.Assert(clsType != null);
		Type varType = var.getType();
		Assertions.Assert(varType.isSubtypeOf(clsType));
		*/
	}
	public void accept(InstVisitor v) {
		v.visit(this);
	}
	public String toString() {
		return location() + ": " + var + " = " + base + ".getClass()";
	}
}
