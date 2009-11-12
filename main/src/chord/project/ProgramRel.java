/*
 * Copyright (c) 2006-07, The Trustees of Stanford University.  All
 * rights reserved.
 * Licensed under the terms of the GNU GPL; see COPYING for details.
 */
package chord.project;

import chord.util.Assertions;
import chord.util.bddbddb.Rel;

/**
 * Generic implementation of a program relation (a specialized kind
 * of Java task).
 * <p>
 * A program relation is a relation over one or more program domains.
 *
 * @author Mayur Naik (mhn@cs.stanford.edu)
 */
public class ProgramRel extends Rel implements ITask {
	protected Project project;
	public void setProject(Project project) {
		Assertions.Assert(project != null);
		Assertions.Assert(this.project == null);
		this.project = project;
	}
	public Project getProject() {
		return project;
	}
	public void run() {
		zero();
		fill();
		save();
	}
	public void save() {
		System.out.println("SAVING rel " + name + " size: " + size());
		super.save();
		project.setTrgtDone(this);
	}
	public void fill() {
		throw new RuntimeException("Relation '" + name +
			"' must override method fill().");
	}
	public String toString() {
		return name;
	}
}
