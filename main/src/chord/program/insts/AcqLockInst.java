/*
 * Copyright (c) 2006-07, The Trustees of Stanford University.  All
 * rights reserved.
 * Licensed under the terms of the GNU GPL; see COPYING for details.
 */
package chord.program.insts;

import chord.program.Var;
import chord.util.Assertions;

/**
 * A statement of the form <tt>monitorenter v</tt> where
 * <tt>v</tt> is a local variable.
 * <p>
 * This statement acquires a lock on the object denoted
 * by <tt>v</tt>.
 * 
 * @author Mayur Naik (mhn@cs.stanford.edu)
 */
public class AcqLockInst extends Inst {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5855611845865285253L;
	/**
	 * The local variable referenced by this statement.
     */
	private Var var;
	public AcqLockInst(Var var, int lineNum) {
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
		return new Var[] { var };
	}
	public Var[] getDefs() {
		return Inst.emptyVarArray;
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
		return location() + ": " + "monitorenter " + var;
	}
}
