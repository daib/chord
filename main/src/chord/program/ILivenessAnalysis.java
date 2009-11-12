/*
 * Copyright (c) 2006-07, The Trustees of Stanford University.  All
 * rights reserved.
 * Licensed under the terms of the GNU GPL; see COPYING for details.
 */
package chord.program;

import java.util.Set;

import chord.program.insts.Inst;
import chord.program.Var;

/**
 * Specification of a liveness dataflow analysis.
 * <p>
 * If the statement is a {@link chord.program.insts.HeadInst} then
 * the analysis considers it as a write to each formal argument
 * of the method.
 * <p>
 * If the statement is a {@link chord.program.insts.TailInst} then
 * the analysis considers it as a read of each return variable
 * of the method.
 *
 * @author Mayur Naik (mhn@cs.stanford.edu)
 */
public interface ILivenessAnalysis {
	/**
	 * Provides all local variables of reference type that may
	 * be live at the entry of a given statement.
	 * 
	 * @param	inst	A statement.
	 * 
	 * @return	All local variables of reference type that may
	 * 			be live at the entry of the given statement.
	 */
	public Set<Var> getIncomingLiveVars(Inst inst);
	/**
	 * Provides all local variables of reference type that may
	 * be live at the exit of a given statement.
	 * 
	 * @param	inst	A statement.
	 * 
	 * @return	All local variables of reference type that may
	 * 			be live at the exit of the given statement.
	 */
	public Set<Var> getOutgoingLiveVars(Inst inst);
}
