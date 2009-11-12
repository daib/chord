/*
 * Copyright (c) 2006-07, The Trustees of Stanford University.  All
 * rights reserved.
 * Licensed under the terms of the GNU GPL; see COPYING for details.
 */
package chord.program.insts;

import chord.program.Field;
import chord.program.Var;

/**
 * A statement that accesses (reads or writes) an instance field, a
 * static field, or an array element.
 *
 * @author Mayur Naik (mhn@cs.stanford.edu)
 */
public abstract class HeapInst extends Inst {
	/**
	 * Determines whether this is a get/put statement.
	 * <p>
	 * If <tt>isWr</tt> is true, then this is a putfield/putstatic/astore
	 * statement.
	 * <p>
	 * If <tt>isWr</tt> is false, then this is a getfield/getstatic/aload
	 * statement.
	 */
	protected boolean isWr;
	/**
	 * The local variable on the l.h.s. if this is a get statement, and
	 * on the r.h.s. if this is a put statement.  It may be of primitive
	 * type, in which case <tt>var</tt> is null.
     */
	protected Var var;
	/**
	 * Provides the field accessed by this statement.
	 * 
	 * @return	The field accessed by this statement.
	 */
	public abstract Field getField();
	public HeapInst(Var var, boolean isWr, int lineNum) {
		super(lineNum);
		// var may be null
		this.var = var;
		this.isWr = isWr;
	}
	public boolean isWr() {
		return isWr;
	}
	public Var getVar() {
		return var;
	}
	public void setVar(Var var) {
		this.var = var;
	}
	public Var[] getDefs() {
		return (var == null || isWr) ? Inst.emptyVarArray :
			new Var[] { var };
	}
}
