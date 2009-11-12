/*
 * Copyright (c) 2006-07, The Trustees of Stanford University.  All
 * rights reserved.
 * Licensed under the terms of the GNU GPL; see COPYING for details.
 */
package chord.rels;

import chord.program.Type;
import chord.program.TypeKind;
import chord.project.Chord;
import chord.project.ProgramRel;
import chord.doms.DomT;

/**
 * Relation containing each (concrete or abstract) class type
 * (as opposed to interface types, primitive types, etc.).
 *
 * @author Mayur Naik (mhn@cs.stanford.edu)
 */
@Chord(
	name = "classT",
	sign = "T0"
)
public class RelClassT extends ProgramRel {
	public void fill() {
		DomT domT = (DomT) doms[0];
        int numT = domT.size();
		for (int tIdx = 0; tIdx < numT; tIdx++) {
            Type tVal = domT.get(tIdx);
            TypeKind kind = tVal.getKind();
            if (kind == TypeKind.CONCRETE_CLASS_TYPE ||
				kind == TypeKind.ABSTRACT_CLASS_TYPE) {
				add(tIdx);
			}
		}
	}
}
