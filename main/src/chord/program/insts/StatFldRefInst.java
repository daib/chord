/*
 * Copyright (c) 2006-07, The Trustees of Stanford University.  All
 * rights reserved.
 * Licensed under the terms of the GNU GPL; see COPYING for details.
 */
package chord.program.insts;

import chord.program.Field;
import chord.program.Var;
import chord.program.Type;
import chord.util.Assertions;

/**
 * A statement of one of the following forms:
 * <br>
 * <tt>v = f</tt> <br>
 * <tt>f = v</tt> <br>
 * <tt>* = f</tt> <br>
 * <tt>f = *</tt> <br>
 * where <tt>v</tt> is a local variable of reference type
 * and <tt>f</tt> is a static field.
 *
 * @author Mayur Naik (mhn@cs.stanford.edu)
 */
public class StatFldRefInst extends HeapInst {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6864274541521491976L;
	/**
	 * The static field accessed by this statement.
	 */
	private Field field;
	public StatFldRefInst(Var var, Field field, boolean isWr,
			int lineNum) {
		super(var, isWr, lineNum);
		Assertions.Assert(field != null);
		this.field = field;
	}
	public Field getField() {
		return field;
	}
	public Var[] getUses() {
		return (var == null || !isWr) ? Inst.emptyVarArray :
			new Var[] { var };
	}
	public Var[] getVars() {
		return (var == null) ? Inst.emptyVarArray : new Var[] { var };
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
			(isWr ? (field + " := " + var)
				  : (var + " := " + field));
	}
}
