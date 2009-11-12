/*
 * Copyright (c) 2006-07, The Trustees of Stanford University.  All
 * rights reserved.
 * Licensed under the terms of the GNU GPL; see COPYING for details.
 */
package chord.rels;

import chord.doms.DomH;
import chord.doms.DomT;
import chord.program.insts.ObjValAsgnInst;
import chord.project.Chord;
import chord.project.ProgramRel;

/**
 * Relation containing each tuple (h,t) such that object allocation
 * statement h allocates objects of type t.
 *
 * @author Mayur Naik (mhn@cs.stanford.edu)
 */
@Chord(
	name = "HT",
	sign = "H0,T1:T1_H0"
)
public class RelHT extends ProgramRel {
	public void fill() {
		DomH domH = (DomH) doms[0];
		DomT domT = (DomT) doms[1];
		int numH = domH.size();
		for (int hIdx = 0; hIdx < numH; hIdx++) {
			ObjValAsgnInst hVal = domH.get(hIdx);
			int tIdx = domT.get(hVal.getType());
			add(hIdx, tIdx);
		}
	}
}
