/*
 * Copyright (c) 2006-07, The Trustees of Stanford University.  All
 * rights reserved.
 * Licensed under the terms of the GNU GPL; see COPYING for details.
 */
package chord.program.insts;

import chord.program.Var;
import chord.util.Assertions;

/**
 * A statement of the form <tt>v1 = v2</tt> where <tt>v1</tt>
 * and <tt>v2</tt> are local variables of reference type.
 * 
 * @author Mayur Naik (mhn@cs.stanford.edu)
 */
public class ObjVarAsgnInst extends Inst {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1420246656248907264L;
	/**
	 * The local variable on the l.h.s. of this statement.
	 */
	private Var lvar;
	/**
	 * The local variable on the r.h.s. of this statement.
	 */
	private Var rvar;
	public ObjVarAsgnInst(Var lvar, Var rvar, int lineNum) {
		super(lineNum);
		Assertions.Assert(lvar != null);
		this.lvar = lvar;
		Assertions.Assert(rvar != null);
		this.rvar = rvar;
	}
	public Var getLvar() {
		return lvar;
	}
	public Var getRvar() {
		return rvar;
	}
	public void setLvar(Var var) {
		this.lvar = var;
	}
	public void setRvar(Var var) {
		this.rvar = var;
	}
	public Var[] getUses() {
		return new Var[] { rvar };
	}
	public Var[] getDefs() {
		return new Var[] { lvar };
	}
	public Var[] getVars() {
		return new Var[] { lvar, rvar };
	}
	public void validate() {
		
	}
	public void accept(InstVisitor v) {
		v.visit(this);
	}
	public String toString() {
		return location() + ": " + lvar + " = " + rvar;
	}
}

