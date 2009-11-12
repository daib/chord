/*
 * Copyright (c) 2006-07, The Trustees of Stanford University.  All
 * rights reserved.
 * Licensed under the terms of the GNU GPL; see COPYING for details.
 */
package chord.program.insts;

import chord.program.Var;

/**
 * A no-op statement.
 * 
 * @author Mayur Naik (mhn@cs.stanford.edu)
 */
public class SkipInst extends Inst {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6677492543552083310L;
	public SkipInst(int lineNum) {
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
		return super.toString() + ": " + "skip";
	}
}
