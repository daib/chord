/*
 * Copyright (c) 2006-07, The Trustees of Stanford University.  All
 * rights reserved.
 * Licensed under the terms of the GNU GPL; see COPYING for details.
 */
package chord.rels;

import java.util.Collection;

import chord.doms.DomT;
import chord.program.Type;
import chord.program.Program;
import chord.project.Chord;
import chord.project.ProgramRel;

/**
 * Relation containing each tuple (s,t) such that type s is a
 * subtype of type t.
 *
 * @author Mayur Naik (mhn@cs.stanford.edu)
 */
@Chord(
	name = "sub",
	sign = "T1,T0:T0_T1"
)
public class RelSub extends ProgramRel {
	public void fill() {
		DomT domT = (DomT) doms[0];
		Program program = project.getProgram();
		Collection<Type> types = program.getTypes();
		for (Type type1 : program.getTypes()) {
			int t1Idx = domT.get(type1);
			for (Type type2 : types) {
				if (type1.isSubtypeOf(type2)) {
					int t2Idx = domT.get(type2);
					add(t1Idx, t2Idx);
				}
			}
		}
	}
}
