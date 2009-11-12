/*
 * Copyright (c) 2006-07, The Trustees of Stanford University.  All
 * rights reserved.
 * Licensed under the terms of the GNU GPL; see COPYING for details.
 */
package chord.program;

import java.util.Set;

import chord.program.insts.Inst;
import chord.util.tuple.object.Pair;

/**
 * Specification of a reaching definitions dataflow analysis.
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
public interface IReachingDefsAnalysis {
	/**
	 * Provides all definitions that may reach the entry of a
	 * given statement.
     *
	 * @param	inst	A statement.
	 * 
	 * @return	All definitions that may reach the entry of the
	 * 			given statement.
	 */
	public Set<Pair<Var, Inst>> getIncomingDefs(Inst inst);
	/**
	 * Provides all definitions that may reach the exit of a
	 * given statement.
	 * 
	 * @param	inst	A statement.
	 * 
	 * @return	All definitions that may reach the exit of the
	 * 			given statement.
	 */
	public Set<Pair<Var, Inst>> getOutgoingDefs(Inst inst);
}
