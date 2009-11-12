/*
 * Copyright (c) 2006-07, The Trustees of Stanford University.  All
 * rights reserved.
 * Licensed under the terms of the GNU GPL; see COPYING for details.
 */
package chord.util.bddbddb;

import chord.util.ProcessExecutor;
import chord.util.Timer;

/**
 * Interface to bddbddb's BDD-based Datalog solver.
 * 
 * @author Mayur Naik (mhn@cs.stanford.edu)
 */
public class Solver {
	public final static String bddbddbMaxHeapSize =
		System.getProperty("bddbddb.max.heap.size", "1024m");
	public final static String bddbddbClsPathName =
		System.getProperty("bddbddb.class.path");
	public final static String bddbddbLibPathName =
		System.getProperty("bddbddb.library.path");
	/**
	 * Runs bddbddb's BDD-based Datalog solver on the specified
	 * Datalog program.
	 * <p>
	 * The maximum amount of memory available to the solver at
	 * run-time can be specified by the user via system property
	 * <tt>bddbddb.max.heap.size</tt> (default is 1024m).
	 * 
	 * @param	fileName	A file containing a Datalog program.
	 */
	public static void run(String fileName) {
		Timer timer = new Timer(fileName);
		timer.init();
		System.out.println("ENTER: Solving " + fileName);
		String cmd =
			"java -Xmx" + bddbddbMaxHeapSize +
			" -cp " + bddbddbClsPathName +
			" -Dnoisy=no" +
			" -Djava.library.path=" + bddbddbLibPathName +
			" -Dbasedir=." +
			" net.sf.bddbddb.Solver " + fileName;
		int ret = ProcessExecutor.execute(cmd);
		if (ret != 0) {
			throw new RuntimeException("Command '" + cmd +
				"' terminated with non-zero value '" +
				ret + "'");
		}
		System.out.println("LEAVE: Solving " + fileName);
		timer.done();
		System.out.println("TIME: " + timer.getExecTimeStr());
	}
}
