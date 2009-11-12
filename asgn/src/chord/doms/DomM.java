/*
 * Copyright (c) 2006-07, The Trustees of Stanford University.  All
 * rights reserved.
 * Licensed under the terms of the GNU GPL; see COPYING for details.
 */
package chord.doms;

import chord.program.Program;
import chord.program.Method;
import chord.program.Type;
import chord.project.Chord;
import chord.project.ProgramDom;

/**
 * Domain of methods.
 * <p>
 * The 0th element in this domain is the main method of the program.
 * <p>
 * The 1st element in this domain is the <tt>start()</tt> method
 * of class <tt>java.lang.Thread</tt>, if this method is reachable
 * from the main method of the program.
 * <p>
 * The above two methods are the entry-point methods of the implicitly
 * created main thread and each explicitly created thread,
 * respectively.  Due to Chord's emphasis on concurrency, these
 * methods are referenced frequently by various predefined program
 * analyses expressed in Datalog, and giving them special indices
 * makes it convenient to reference them in those analyses.
 * 
 * @author Mayur Naik (mhn@cs.stanford.edu)
 */
@Chord(
	name = "M"
)
public class DomM extends ProgramDom<Method> {
	public void fill() {
		// Reserve index 0 for the main method of the program.
		// Reserver index 1 for the start() method of java.lang.Thread
		// if it exists.
		Program program = project.getProgram();
		Method mainMethod = program.getMainMethod();
		set(mainMethod);
		Type threadType = program.getType("java.lang.Thread");
		if (threadType != null) {
			Method startMethod = threadType.getMethod("start()");
			if (startMethod != null)
				set(startMethod);
		}
		for (Type type : program.getTypes()) {
			for (Method meth : type.getMethods())
				set(meth);
		}
	}
	public String toXMLAttrsString(Method meth) {
		Type type = meth.getCtnrType();
		String sign = type.getName() + "." +
			meth.getSign().replace("<", "&lt;").replace(">", "&gt;");
		String file = type.getFileName();
		int line = meth.getLineNum();
		return "sign=\"" + sign +
			"\" file=\"" + file +
			"\" line=\"" + line + "\"";
	}
}
