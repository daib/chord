/*
 * Copyright (c) 2006-07, The Trustees of Stanford University.  All
 * rights reserved.
 * Licensed under the terms of the GNU GPL; see COPYING for details.
 */
package chord.doms;

import chord.program.CFG;
import chord.program.Method;
import chord.program.Program;
import chord.program.Type;
import chord.program.insts.InvkInst;
import chord.program.insts.Inst;
import chord.project.Chord;
import chord.project.ProgramDom;

/**
 * Domain of argument and return variable positions of methods
 * and method invocation statements.
 * <p>
 * Let N be the largest number of arguments or return variables
 * of any method or method invocation statement.  Then, this
 * domain contains elements 0, 1, ..., N-1 in order.
 * 
 * @author Mayur Naik (mhn@cs.stanford.edu)
 */
@Chord(
	name = "Z"
)
public class DomZ extends ProgramDom<Integer> {
	public void fill() {
		Program program = project.getProgram();
		int max = 0;
		for (Type type : program.getTypes()) {
			for (Method meth : type.getMethods()) {
				CFG cfg = meth.getCFG();
				if (cfg == null)
					continue;
				int numArgs = cfg.getArgs().size();
				if (numArgs > max)
					max = numArgs;
				int numRets = cfg.getRets().size();
				if (numRets > max)
					max = numRets;
				for (Inst inst : cfg.getNodes()) {
					if (inst instanceof InvkInst) {
						InvkInst invk = (InvkInst) inst;
						numArgs = invk.numArgs();
						if (numArgs > max)
							max = numArgs;
						if (numRets > max)
							max = numRets;
					}
				}
			}
		}
		for (int i = 0; i < max; i++)
			set(new Integer(i));
	}
}
