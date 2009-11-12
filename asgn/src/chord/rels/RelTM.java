/*
 * Copyright (c) 2006-07, The Trustees of Stanford University.  All
 * rights reserved.
 * Licensed under the terms of the GNU GPL; see COPYING for details.
 */
package chord.rels;

import chord.program.Method;
import chord.program.Type;
import chord.project.Chord;
import chord.project.ProgramRel;
import chord.doms.DomM;
import chord.doms.DomT;

/**
 * Relation containing each tuple (t,m) such that method m is
 * declared in type t.
 *
 * @author Mayur Naik (mhn@cs.stanford.edu)
 */
@Chord(
	name = "TM",
	sign = "T0,M0:M0_T0"
)
public class RelTM extends ProgramRel {
	public void fill() {
		DomT domT = (DomT) doms[0];
		DomM domM = (DomM) doms[1];
		int numT = domT.size();
		for (int tIdx = 0; tIdx < numT; tIdx++) {
			Type tVal = domT.get(tIdx);
			for (Method mVal : tVal.getMethods()) {
				int mIdx = domM.get(mVal);
				add(tIdx, mIdx);
			}
		}
	}
}
