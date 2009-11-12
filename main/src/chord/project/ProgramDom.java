/*
 * Copyright (c) 2006-07, The Trustees of Stanford University.  All
 * rights reserved.
 * Licensed under the terms of the GNU GPL; see COPYING for details.
 */
package chord.project;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import chord.project.ProgramDom;
import chord.util.Assertions;
import chord.util.bddbddb.Dom;

/**
 * Generic implementation of a program domain (a specialized kind
 * of Java task).
 * <p>
 * A program domain maps each of N values of some related type in the
 * given Java program (e.g., methods, types, statements of a certain
 * kind, etc.) to a unique integer in the range [0..N-1].  The
 * integers are assigned in the order in which the values are added to
 * the domain.
 *
 * @param	<T>	The type of values in the program domain.
 * 
 * @author Mayur Naik (mhn@cs.stanford.edu)
 */
public class ProgramDom<T> extends Dom<T> implements ITask {
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
		clear();
		fill();
		save();
	}
	public void save() {
		System.out.println("SAVING dom " + name + " size: " + size());
		super.save();
		project.setTrgtDone(this);
	}
	public void fill() {
		throw new RuntimeException("Domain '" + getName() +
			"' must override method fill().");
	}
	/**
	 * Provides the XML attributes string of the specified value.
	 * Subclasses may override this method if necessary.
	 * 
	 * @param	val	A value.
	 * 
	 * @return	The XML attributes string of the specified value.
	 * 			It is the empty string by default.
	 * 
	 * @see	#saveToXMLFile()
	 */
	public String toXMLAttrsString(T val) {
		return "";
	}
	/**
	 * Provides the XML elements string of the specified value.
	 * Subclasses may override this method if necessary.
	 * 
	 * @param	val	A value.
	 * 
	 * @return	The XML elements string of the specified value.
	 * 			It is the empty string by default.
	 * 
	 * @see	#saveToXMLFile()
	 */
	public String toXMLElemsString(T val) {
		return "";
	}
	public void saveToXMLFile() {
		String name = getName();
		String tag = name + "list";
		String xmlFileName = tag + ".xml";
		PrintWriter out;
		try {
			out = new PrintWriter(new FileWriter(new File(xmlFileName)));
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
		out.println("<" + tag + ">");
		for (int i = 0; i < size(); i++) {
			T val = get(i);
			out.println("<" + name + " id=\"" + name + i + "\" " +
				toXMLAttrsString(val) + ">");
			out.println(toXMLElemsString(val));
			out.println("</" + name + ">");
		}
		out.println("</" + tag + ">");
		out.close();
	}
	public String toString() {
		return name;
	}
}
