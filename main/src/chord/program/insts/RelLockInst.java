/*
 * Copyright (c) 2006-07, The Trustees of Stanford University.  All
 * rights reserved.
 * Licensed under the terms of the GNU GPL; see COPYING for details.
 */
package chord.program.insts;

import chord.program.Var;

/**
 * A statement of the form <tt>monitorexit</tt> which releases the
 * lock acquired by the most recently executed <tt>monitorenter</tt>
 * statement.
 * 
 * @author Mayur Naik (mhn@cs.stanford.edu)
 */
public class RelLockInst extends Inst {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5011959492721066926L;
	public RelLockInst(int lineNum) {
		super(lineNum);
	}
	public Var[] getUses() {
		return Inst.emptyVarArray;
	}
	public Var[] getDefs() {
		return Inst.emptyVarArray;
	}
	public Var[] getVars() {
		return Inst.emptyVarArray;
	}
	public void validate() {
		
	}
	public void accept(InstVisitor v) {
		v.visit(this);
	}
	public String toString() {
		return location() + ": " + "monitorexit";
	}
}
