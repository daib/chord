/*
 * Copyright (c) 2006-07, The Trustees of Stanford University.  All
 * rights reserved.
 * Licensed under the terms of the GNU GPL; see COPYING for details.
 */
package chord.program.builders;

import chord.program.Program;

/**
 * Specification of a program representation builder.
 * 
 * @author Mayur Naik (mhn@cs.stanford.edu)
 */
public interface IProgramBuilder {
	/**
	 * Provides the program built using the given specifications.
	 *
	 * @param	mainClassName	Fully-qualified name of the program's
	 * 			main class.
	 * 			It must be non-null.
	 * @param	classPathName	The program's classpath.
	 * 			It must be non-null.
	 * @param	srcPathName		The program's Java source path.
	 * 			It may be null.
	 * @param	annotIncludeFileName	File listing each
	 * 			<tt>v.newInstance()</tt> call site (approximately,
	 * 			via its containing method) and the type to which
	 * 			<tt>v</tt> may resolve during the program's
	 * 			execution.
	 * 			It may be null.
	 * @param	annotExcludeFileName	File listing each
	 * 			<tt>v.newInstance()</tt> call site (approximately,
	 * 			via its containing method) which must be silently
	 * 			treated as a no-op.
	 * 			It may be null.
	 * @param	ignoreMethodsBySignFileName	File listing the
	 * 			signature of each method whose body must be treated
	 * 			as a no-op.
	 * 			It may be null.
	 * @param	ignoreMethodsBySignFileName	File listing each type
	 * 			such that the body of each method defined in that
	 * 			type must be treated as a no-op.
	 * 			It may be null.
	 * 
	 * @return	The program built using the given specifications.
	 */
	public Program build(
		String mainClassName,
		String classPathName,
		String srcPathName,
		String annotIncludeFileName,
		String annotExcludeFileName,
		String ignoreMethodsBySignFileName,
		String ignoreMethodsByCtnrFileName);
}
