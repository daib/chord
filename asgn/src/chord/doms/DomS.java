/*
 * Copyright (c) 2006-07, The Trustees of Stanford University.  All
 * rights reserved.
 * Licensed under the terms of the GNU GPL; see COPYING for details.
 */
package chord.doms;

import chord.program.Method;
import chord.program.Program;
import chord.program.Type;
import chord.project.Chord;
import chord.project.ProgramDom;

/**
 * Domain of method signatures.
 * <p>
 * A method signature is a string of the form <tt>m(t1,...,tn)</tt> where
 * <tt>m</tt> is the name of the method and <tt>t1</tt>, ..., <tt>tn</tt>
 * are the fully-qualified names of the types of its arguments in order,
 * including the <tt>this</tt> argument in the case of instance methods.
 * <p>
 * Examples are <tt>main(java.util.String[])</tt> and
 * <tt>equals(java.util.String,java.lang.Object)</tt>.
 *
 * @author Mayur Naik (mhn@cs.stanford.edu)
 */
@Chord(
	name = "S"
)
public class DomS extends ProgramDom<String> {
	public void fill() {
		Program program = project.getProgram();
		for (Type type : program.getTypes()) {
			for (Method meth : type.getMethods()) {
				set(meth.getSign());
			}
		}
	}
}
