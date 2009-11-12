/*
 * Copyright (c) 2006-07, The Trustees of Stanford University.  All
 * rights reserved.
 * Licensed under the terms of the GNU GPL; see COPYING for details.
 */
package chord.project;

import chord.util.Assertions;

/**
 * Generic implementation of a Java task (a program analysis
 * expressed in Java).
 * 
 * @author Mayur Naik (mhn@cs.stanford.edu)
 */
public class JavaTask implements ITask {
    protected String name;
	protected Project project;
    public void setName(String name) {
        Assertions.Assert(name != null);
        Assertions.Assert(this.name == null);
        this.name = name;
    }
    public String getName() {
        return name;
    }
	public void setProject(Project project) {
		Assertions.Assert(project != null);
		Assertions.Assert(this.project == null);
		this.project = project;
	}
	public Project getProject() {
		return project;
	}
	public void run() {
		throw new RuntimeException("Analysis '" + name +
			"' must override method run().");
	}
	public String toString() {
		return name;
	}
}
