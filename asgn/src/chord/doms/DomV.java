/*
 * Copyright (c) 2006-07, The Trustees of Stanford University.  All
 * rights reserved.
 * Licensed under the terms of the GNU GPL; see COPYING for details.
 */
package chord.doms;

import chord.program.CFG;
import chord.program.Method;
import chord.program.Var;
import chord.project.Chord;
import chord.project.ProgramDom;

/**
 * Domain of local variables of reference type.
 * <p>
 * Each local variable declared in each block of each method is
 * represented by a unique element in this domain, in particular,
 * local variables that have the same name but are declared in
 * different methods or in different blocks of the same method
 * are represented by different elements in this domain.
 * <p>
 * All local variables of the same method are assigned contiguous
 * indices in this domain, in particular, the set of local
 * variables of a method is the disjoint union of its argument
 * variables and its temporary variables, and the argument
 * variables are assigned contiguous indices in order followed by
 * the temporary variables.
 * 
 * @author Mayur Naik (mhn@cs.stanford.edu)
 */
@Chord(
	name = "V",
	consumedNames = { "M" }
)
public class DomV extends ProgramDom<Var> {
	private DomM domM;
	public void fill() {
		domM = (DomM) project.getTrgt("M");
        for (Method meth : domM) {
            CFG cfg = meth.getCFG();
            if (cfg == null)
            	continue;
            for (Var var : cfg.getArgs())
            	set(var);
            for (Var var : cfg.getTmps())
            	set(var);
		}
	}
    public String toXMLAttrsString(Var var) {
        String name = var.getName();
        String file = var.getMethod().getFileName();
        int line = 0;
        return "name=\"" + name +
            "\" file=\"" + file +
            "\" line=\"" + line + "\"";
    }
}
