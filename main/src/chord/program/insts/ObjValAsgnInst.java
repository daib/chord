/*
 * Copyright (c) 2006-07, The Trustees of Stanford University.  All
 * rights reserved.
 * Licensed under the terms of the GNU GPL; see COPYING for details.
 */
package chord.program.insts;

import chord.program.Type;
import chord.program.TypeKind;
import chord.program.Var;
import chord.util.Assertions;

/**
 * An object allocation statement of the form <tt>v = new t</tt> where
 * <tt>v</tt> is a local variable and <tt>t</tt> is the type of the
 * object allocated.
 * 
 * @author Mayur Naik (mhn@cs.stanford.edu)
 */
public class ObjValAsgnInst extends Inst {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4726838584087498707L;
	/**
	 * The local variable on the l.h.s. of this statement.
	 */
	private Var var;
	/**
	 * The type on the r.h.s. of this statement.
	 */
	private Type type;
	public ObjValAsgnInst(Var var, Type type, int lineNum) {
		super(lineNum);
		Assertions.Assert(var != null);
		Assertions.Assert(type != null);
		TypeKind kind = type.getKind();
		Assertions.Assert(kind == TypeKind.CONCRETE_CLASS_TYPE ||
			kind == TypeKind.ARRAY_TYPE);
		this.var = var;
		this.type = type;
	}
	public Var getVar() {
		return var;
	}
	public void setVar(Var var) {
		this.var = var;
	}
	public Type getType() {
		return type;
	}
	public Var[] getUses() {
		return Inst.emptyVarArray;
	}
	public Var[] getDefs() {
		return new Var[] { var };
	}
	public Var[] getVars() {
		return new Var[] { var };
	}
	public void validate() {
		Assertions.Assert(type.isSubtypeOf(var.getType()));
	}
	public void accept(InstVisitor v) {
		v.visit(this);
	}
	public String toString() {
		return location() + ": " + var + " = new " + type;
	}
}
