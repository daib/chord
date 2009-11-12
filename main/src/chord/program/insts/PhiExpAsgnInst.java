/*
 * Copyright (c) 2006-07, The Trustees of Stanford University.  All
 * rights reserved.
 * Licensed under the terms of the GNU GPL; see COPYING for details.
 */
package chord.program.insts;

import java.util.Set;

import chord.program.Var;
import chord.util.Assertions;

/**
 * A statement of the form <tt>v = phi(v1, ..., vn)</tt> where
 * <tt>v</tt>, <tt>v1</tt>, ..., <tt>vn</tt> are local variables
 * of the same reference type.
 * 
 * @author Mayur Naik (mhn@cs.stanford.edu)
 */
public class PhiExpAsgnInst extends Inst {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8215534076205096033L;
	/**
	 * The local variable on the l.h.s. of this statement.
	 */
	private Var lvar;
	/**
	 * The local variables on the r.h.s. of this statement.
	 */
	private Set<Var> rvars;
	public PhiExpAsgnInst(Var lvar, Set<Var> rvars, int lineNum) {
		super(lineNum);
		Assertions.Assert(lvar != null);
		Assertions.Assert(rvars != null);
		Assertions.Assert(rvars.size() >= 2);
		for (Var rvar : rvars) {
			Assertions.Assert(rvar != null);
			Assertions.Assert(rvar.getType() == lvar.getType());
		}
		this.lvar = lvar;
		this.rvars = rvars;
	}
	public Var getLvar() {
		return lvar;
	}
	public Set<Var> getRvars() {
		return rvars;
	}
	public Var[] getDefs() {
		return new Var[] { lvar };
	}
	public Var[] getUses() {
    	Var[] uses = new Var[rvars.size()];
        rvars.toArray(uses);
        return uses;
	}
	public Var[] getVars() {
		throw new RuntimeException("not impl");
	}
	public void validate() {
		
	}
	public void accept(InstVisitor v) {
		v.visit(this);
	}
	public String toString() {
		return location() + ": " + lvar + " = phi " + rvars;
	}
}
