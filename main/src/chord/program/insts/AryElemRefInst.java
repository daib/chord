/*
 * Copyright (c) 2006-07, The Trustees of Stanford University.  All
 * rights reserved.
 * Licensed under the terms of the GNU GPL; see COPYING for details.
 */
package chord.program.insts;

import chord.program.Field;
import chord.program.Var;
import chord.util.Assertions;

/**
 * A statement of one of the following forms:
 * <br>
 * <tt>v = b[*]</tt> <br>
 * <tt>b[*] = v</tt> <br>
 * <tt>* = b[*]</tt> <br>
 * <tt>b[*] = *</tt> <br>
 * where <tt>v</tt> and <tt>b</tt> are local variables, and <tt>v</tt>
 * is of reference type.
 * 
 * @author Mayur Naik (mhn@cs.stanford.edu)
 */
public class AryElemRefInst extends HeapInst {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3364797973275857141L;
	/**
	 * The local variable denoting the array indexed by this statement.
	 */
	private Var base;
	public AryElemRefInst(Var var, Var base, boolean isWr, int lineNum) {
		super(var, isWr, lineNum);
		Assertions.Assert(base != null);
		this.base = base;
	}
	public Var getBase() {
		return base;
	}
	public void setBase(Var var) {
		this.base = var;
	}
	public Field getField() {
		return null;
	}
	public Var[] getUses() {
		return (var == null || !isWr) ? new Var[] { base } :
			new Var[] { base, var };
	}
	public Var[] getVars() {
		return (var == null) ? new Var[] { base } :
			new Var[] { base, var };
	}
	public void validate() {

	}
	public void accept(InstVisitor v) {
		v.visit(this);
	}
	public String toString() {
		return location() + ": " +
			(isWr ? (base + "[*]" + " := " + var)
				  : (var + " := " + base + "[*]"));
	}
}
