/*
 * Copyright (c) 2006-07, The Trustees of Stanford University.  All
 * rights reserved.
 * Licensed under the terms of the GNU GPL; see COPYING for details.
 */
package chord.program.insts;

import chord.program.CFG;
import chord.program.Method;
import chord.program.Type;
import chord.program.Var;

/**
 * A generic simple statement.
 * 
 * @author Mayur Naik (mhn@cs.stanford.edu)
 */
public abstract class Inst implements java.io.Serializable {
	public static final Var[] emptyVarArray = new Var[0];
	/**
	 * Control-flow graph of the method containing this statement.
	 */
	protected CFG cfg;
	/**
     * Line number of this statement in its containing file.
     */
	protected int lineNum;
	public Inst(int lineNum) {
		this.lineNum = lineNum;
	}
	/**
	 * Sets the containing control flow graph of this statement.
	 * 
	 * @param	cfg	The containing control flow graph of this
	 * 			statement.
	 */
	public void setCFG(CFG cfg) {
		this.cfg = cfg;
	}
	/**
	 * Provides the containing control flow graph of this statement.
	 * 
	 * @return	The containing control flow graph of this statement.
	 */
	public CFG getCFG() {
		return cfg;
	}
	/**
	 * Provides the immediate containing method of this statement.
	 *
	 * @return	The immediate containing method of this statement.
	 */ 
	public Method getImmediateCtnrMethod() {
		return cfg.getCtnrMethod();
	}
	/**
	 * Provides the outermost containing method of this statement.
	 *
	 * @return	The outermost containing method of this statement.
	 */ 
	public Method getOutermostCtnrMethod() {
		return cfg.getCtnrMethod().getOutermostCtnrMethod();
	}
	/**
	 * Provides the line number of this statement in its
	 * containing file.
	 * 
	 * @return	The line number of this statement in its
	 * 			containing file.
	 */
	public int getLineNum() {
		return lineNum;
	}
	/**
	 * Provides all local variables of reference type read
	 * by this statement.
	 * 
	 * @return	All local variables of reference type read
	 * 			by this statement.
	 */
	public abstract Var[] getUses();
	/**
	 * Provides all local variables of reference type written
	 * by this statement.
	 * 
	 * @return	All local variables of reference type written
	 * 			by this statement.
	 */
	public abstract Var[] getDefs();
	/**
	 * Provides all local variables of reference type accessed
	 * (read or written) by this statement.
	 * 
	 * @return	All local variables of reference type accessed
	 * 			(read or written) by this statement.
	 */
	public abstract Var[] getVars();
	/**
	 * Accepts the provided simple statement visitor.
	 * 
	 * @param	v	A simple statement visitor.
	 */
	public abstract void accept(InstVisitor v);
	/**
	 * Validates this statement (checks its partial specification).
	 */
	public abstract void validate();
	/**
	 * Provides the name of the file containing this statement.
	 * 
	 * @return	The name of the file containing this statement.
	 */
	public String getFileName() {
		if (cfg == null)
			return null;
		Method method = cfg.getCtnrMethod();
		if (method == null)
			return null;
		Type type = method.getCtnrType();
		if (type == null)
			return null;
		return type.getFileName();
	}
	/**
	 * Provides the location of this statement in format
	 * "&lt;file name&gt;:&lt;line number&gt;".
	 * 
	 * @return	The location of this statement.
	 */
	public String location() {
		return getFileName() + ":" + lineNum;
	}
	/**
	 * Determines whether the specified local variable is written
	 * by this statement.
	 * 
	 * @param	var	A local variable.
	 * 
	 * @return	true iff the specified local variable is written
	 * 			by this statement.
	 */
	public boolean defs(Var var) {
		for (Var var2 : getDefs()) {
			if (var2 == var)
				return true;
		}
		return false;
	}
	/**
	 * Determines whether the specified local variable is read
	 * by this statement.
	 * 
	 * @param	var	A local variable.
	 * 
	 * @return	true iff the specified local variable is read
	 * 			by this statement.
	 */
	public boolean uses(Var var) {
		for (Var var2 : getUses()) {
			if (var2 == var)
				return true;
		}
		return false;
	}
	public String toString() {
		return super.toString() + " " + location();
	}
}
