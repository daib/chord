/*
 * Copyright (c) 2006-07, The Trustees of Stanford University.  All
 * rights reserved.
 * Licensed under the terms of the GNU GPL; see COPYING for details.
 */
package chord.rels;

import java.util.Collection;
import java.util.List;

import chord.doms.DomT;
import chord.program.Type;
import chord.program.TypeKind;
import chord.program.Program;
import chord.project.Chord;
import chord.project.ProgramRel;

/**
 * Relation containing each tuple (s,t) such that class type s
 * implements interface type t.
 *
 * @author Mayur Naik (mhn@cs.stanford.edu)
 */
@Chord(
	name = "implsTT",
	sign = "T1,T0:T0_T1"
)
public class RelImplsTT extends ProgramRel {
	public void fill() {
		DomT domT = (DomT) doms[0];
		Program program = project.getProgram();
		Collection<Type> types = program.getTypes();
		for (Type type : types) {
			TypeKind kind = type.getKind();
			if (kind == TypeKind.CONCRETE_CLASS_TYPE ||
					kind == TypeKind.ABSTRACT_CLASS_TYPE) {
				int t1Idx = domT.get(type);
				List<Type> suptypes = type.getSuptypes();
				for (int i = 1; i < suptypes.size(); i++) {
					Type suptype = suptypes.get(i);
					int t2Idx = domT.get(suptype);
					add(t1Idx, t2Idx);
				}
			}
		}
	}
}
