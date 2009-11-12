/*
 * Copyright (c) 2006-07, The Trustees of Stanford University.  All
 * rights reserved.
 * Licensed under the terms of the GNU GPL; see COPYING for details.
 */
package chord.rels;

import chord.doms.DomM;
import chord.program.Method;
import chord.project.Chord;
import chord.project.ProgramRel;

/**
 * Relation containing all private (as opposed to protected or
 * public) methods.
 *
 * @author Mayur Naik (mhn@cs.stanford.edu)
 */
@Chord(
	name = "privateM",
	sign = "M0"
)
public class RelPrivateM extends ProgramRel {
    public void fill() {
    	DomM domM = (DomM) doms[0];
		int numM = domM.size();
		for (int mIdx = 0; mIdx < numM; mIdx++) {
			Method mVal = domM.get(mIdx);
			if (mVal.isPrivate())
				add(mIdx);
		}
	}
}
