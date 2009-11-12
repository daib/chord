/*
 * Copyright (c) 2006-07, The Trustees of Stanford University.  All
 * rights reserved.
 * Licensed under the terms of the GNU GPL; see COPYING for details.
 */
package chord.program.insts;

import java.util.List;

import chord.program.Var;

/**
 * The unique exit statement of a method.
 * 
 * @author Mayur Naik (mhn@cs.stanford.edu)
 */
public class TailInst extends Inst {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4614378625407620823L;
	public TailInst(int lineNum) {
		super(lineNum);
	}
	public Var[] getUses() {
		List<Var> rets = cfg.getRets();
		if (rets.isEmpty())
			return Inst.emptyVarArray;
		Var[] uses = new Var[rets.size()];
		rets.toArray(uses);
		return uses;
	}
	public Var[] getDefs() {
		return Inst.emptyVarArray;
	}
	public Var[] getVars() {
		return getUses();
	}
	public void validate() {
		
	}
	public void accept(InstVisitor v) {
		v.visit(this);
	}
	public String toString() {
		return location() + ": tail";
	}
}
