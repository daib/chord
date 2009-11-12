/*
 * Copyright (c) 2006-07, The Trustees of Stanford University.  All
 * rights reserved.
 * Licensed under the terms of the GNU GPL; see COPYING for details.
 */
package chord.rels;

import chord.doms.DomT;
import chord.doms.DomV;
import chord.program.Var;
import chord.project.Chord;
import chord.project.ProgramRel;

/**
 * Relation containing each tuple (v,t) such that local variable v
 * of reference type has type t.
 *
 * @author Mayur Naik (mhn@cs.stanford.edu)
 */
@Chord(
	name = "VT",
	sign = "V0,T0:T0_V0"
)
public class RelVT extends ProgramRel {
	public void fill() {
		DomV domV = (DomV) doms[0];
		DomT domT = (DomT) doms[1];
		int numV = domV.size();
		for (int vIdx = 0; vIdx < numV; vIdx++) {
			Var vVal = domV.get(vIdx);
			int tIdx = domT.get(vVal.getType());
			add(vIdx, tIdx);
		}
	}
}
