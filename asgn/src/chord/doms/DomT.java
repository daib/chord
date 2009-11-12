/*
 * Copyright (c) 2006-07, The Trustees of Stanford University.  All
 * rights reserved.
 * Licensed under the terms of the GNU GPL; see COPYING for details.
 */
package chord.doms;

import chord.program.Program;
import chord.program.Type;
import chord.project.Chord;
import chord.project.ProgramDom;

/**
 * Domain of types.
 * 
 * @author Mayur Naik (mhn@cs.stanford.edu)
 */
@Chord(
	name = "T"
)
public class DomT extends ProgramDom<Type> {
	public void fill() {
		Program program = project.getProgram();
		for (Type type : program.getTypes()) {
			set(type);
		}
	}
    public String toXMLAttrsString(Type type) {
        String name = type.getName();
        String file = type.getFileName();
        int line = 0; // type.getLineNum();
        return "name=\"" + name +
            "\" file=\"" + file +
            "\" line=\"" + line + "\"";
    }
}
