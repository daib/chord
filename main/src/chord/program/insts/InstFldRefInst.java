/*
 * Copyright (c) 2006-07, The Trustees of Stanford University.  All
 * rights reserved.
 * Licensed under the terms of the GNU GPL; see COPYING for details.
 */
package chord.program.insts;

import chord.program.Field;
import chord.program.Type;
import chord.program.Var;
import chord.util.Assertions;

/**
 * A statement of one of the following forms:
 * <br>
 * <tt>v = b.f</tt> <br>
 * <tt>b.f = v</tt> <br>
 * <tt>* = b.f</tt> <br>
 * <tt>b.f = *</tt> <br>
 * where <tt>v</tt> and <tt>b</tt> are local variables, <tt>v</tt>
 * is of reference type, and <tt>f</tt> is an instance field.
 * 
 * @author Mayur Naik (mhn@cs.stanford.edu)
 */
public class InstFldRefInst extends HeapInst {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3026028134612967640L;
	/**
	 * The local variable denoting the object whose instance field
	 * is accessed by this statement.
	 */
	private Var base;
	/**
	 * The instance field accessed by this statement.
	 */
	private Field field;
	public InstFldRefInst(Var var, Var base, Field field,
			boolean isWr, int lineNum) {
		super(var, isWr, lineNum);
		Assertions.Assert(base != null);
		Assertions.Assert(field != null);
		this.base = base;
		this.field = field;
	}
	public Var getBase() {
		return base;
	}
	public void setBase(Var var) {
		this.base = var;
	}
	public Field getField() {
		return field;
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
		if (var != null) {
			Type varType = var.getType();
			Type fldType = field.getDeclType();
			if (isWr) {
				Assertions.Assert(varType.isSubtypeOf(fldType));
			} else {
				Assertions.Assert(fldType.isSubtypeOf(varType));
			}
		}
	}
	public void accept(InstVisitor v) {
		v.visit(this);
	}
	public String toString() {
		return location() + ": " +
			(isWr ? (base + "." + field + " := " + var)
				  : (var + " := " + base + "." + field));
	}
}
