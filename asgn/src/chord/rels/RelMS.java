/*
 * Copyright (c) 2006-07, The Trustees of Stanford University.  All
 * rights reserved.
 * Licensed under the terms of the GNU GPL; see COPYING for details.
 */
package chord.rels;

import chord.program.Method;
import chord.project.Chord;
import chord.project.ProgramRel;
import chord.doms.DomM;
import chord.doms.DomS;

/**
 * Relation containing each tuple (m,s) such that method m has
 * signature s.
 *
 * @author Mayur Naik (mhn@cs.stanford.edu)
 */
@Chord(
	name = "MS",
	sign = "M0,S0:S0_M0"
)
public class RelMS extends ProgramRel {
	public void fill() {
		DomM domM = (DomM) doms[0];
		DomS domS = (DomS) doms[1];
		int numM = domM.size();
		for (int mIdx = 0; mIdx < numM; mIdx++) {
			Method mVal = domM.get(mIdx);
			String sVal = mVal.getSign();
			int sIdx = domS.get(sVal);
			add(mIdx, sIdx);
		}
	}
}
