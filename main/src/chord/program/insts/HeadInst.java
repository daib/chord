/*
 * Copyright (c) 2006-07, The Trustees of Stanford University.  All
 * rights reserved.
 * Licensed under the terms of the GNU GPL; see COPYING for details.
 */
package chord.program.insts;

import java.util.List;

import chord.program.Var;

/**
 * The unique entry statement of a method.
 * 
 * @author Mayur Naik (mhn@cs.stanford.edu)
 */
public class HeadInst extends Inst {
	/**
	 * 
	 */
	private static final long serialVersionUID = 9008778138994631092L;
	public HeadInst(int lineNum) {
		super(lineNum);
	}
	public Var[] getUses() {
		return Inst.emptyVarArray;
	}
	public Var[] getDefs() {
		List<Var> args = cfg.getArgs();
		if (args.isEmpty())
			return Inst.emptyVarArray;
		Var[] defs = new Var[args.size()];
		args.toArray(defs);
		return defs;
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
		return location() + ": head";
	}
}
