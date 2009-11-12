/*
 * Copyright (c) 2006-07, The Trustees of Stanford University.  All
 * rights reserved.
 * Licensed under the terms of the GNU GPL; see COPYING for details.
 */
package chord.program;

import java.io.IOException;
import java.io.PrintStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import chord.util.Assertions;

/**
 * A program.
 * <p> 
 * It is a complete program with a distinguished main method
 * at which the program starts execution.
 * 
 * @author Mayur Naik (mhn@cs.stanford.edu)
 */
public class Program implements java.io.Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2813425679430579751L;
	private transient List<Type> types;
	private String mainClassName;
	private String srcPathName;
	private boolean removedSyncs;
	private boolean removedLoops;
	private boolean removedSkips;
	private boolean convertedToSSA;
	public Program(List<Type> types,
			String mainClassName, String srcPathName) {
		this.types = types;
		this.mainClassName = mainClassName;
		this.srcPathName = srcPathName;
	}
	/**
	 * Provides the main method of this program.  It is a static method
	 * with signature <tt>void main(java.lang.String[])</tt>.
	 * 
	 * @return	The main method of the program.
	 */
	public Method getMainMethod() {
		Type mainType = getType(mainClassName);
		return mainType.getMethod("main(java.lang.String[])");
	}
	/**
	 * Provides all types in this program.
	 * 
	 * @return	All types in this program.
	 */
	public List<Type> getTypes() {
		return types;
	}
	/**
	 * Provides the representation of the type having a given name,
	 * if one exists, and null otherwise.
	 * 
	 * @param	name	The fully-qualified name of a type.
	 * 
	 * @return	The representation of the type having the given
	 * 			name, if one exists, and null otherwise.
	 */
	public Type getType(String name) {
		for (Type type : types) {
			if (type.getName().equals(name))
				return type;
		}
		return null;
	}
	public boolean removedSyncs() {
		return removedSyncs;
	}
	public boolean removedLoops() {
		return removedLoops;
	}
	public boolean removedSkips() {
		return removedSkips;
	}
	public boolean convertedToSSA() {
		return convertedToSSA;
	}
	/**
	 * Removes all skip statements from each method in each class in
	 * this program.
	 */
	public void removeSkips() {
		if (!removedSkips) {
			for (Type type : getTypes()) {
				for (Method method : type.getMethods()) {
					method.removeSkips();
				}
			}
			removedSkips = true;
		}
	}
	/**
	 * Removes all loops from each method in each class in this
	 * program by generating a fresh static method in the same
	 * class per loop.
	 * <p>
	 * Note: Synchronized block removal must be performed and skip
	 * statement removal must NOT be performed before this
	 * transformation.
	 */
	public void removeLoops() {
		if (!removedLoops) {
			Assertions.Assert(removedSyncs);
			Assertions.Assert(!removedSkips);
			for (Type type : getTypes())
				type.removeLoops();
			removedLoops = true;
		}
	}
	/**
	 * Removes all synchronized blocks from each method in each
	 * class in this program by generating a fresh static method
	 * in the same class per synchronized block.
	 */
	public void removeSyncs() {
		if (!removedSyncs) {
			Assertions.Assert(!removedLoops);
			for (Type type : getTypes())
				type.removeSyncs();
			removedSyncs = true;
		}
	}
	/**
	 * Converts each method in each class in this program into
	 * static single assignment (SSA) form.
	 */
	public void convertToSSA() {
		if (!convertedToSSA) {
			Assertions.Assert(removedLoops);
			for (Type type : getTypes()) {
				for (Method method : type.getMethods())
					method.convertToSSA();
			}
			convertedToSSA = true;
		}
	}
	private void writeObject(java.io.ObjectOutputStream out)
			throws IOException  {
		out.defaultWriteObject();
		int numTypes = types.size();
		out.writeInt(numTypes);
		for (Type type : types) {
			out.writeObject(type);
		}
		for (Type type : types) {
			List<Type> suptypes = type.getSuptypes();
			out.writeObject(suptypes);
			out.writeObject(type.getElemType());
		}
		for (Type type : types) {
			List<Field> fields = type.getFields();
			out.writeObject(fields);
		}
		for (Type type : types) {
			List<Method> methods = type.getMethods();
			out.writeObject(methods);
		}
		for (Type type : types) {
			for (Method method : type.getMethods()) {
				out.writeObject(method.getCFG());
			}
		}
	}
	private void readObject(java.io.ObjectInputStream in)
			throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		int numTypes = in.readInt();
		types = new ArrayList<Type>(numTypes);
		for (int i = 0; i < numTypes; i++) {
			Type type = (Type) in.readObject();
			types.add(type);
		}
		for (Type type : types) {
			List<Type> suptypes = (List) in.readObject();
			type.setSuptypes(suptypes);
			Type elemType = (Type) in.readObject();
			type.setElemType(elemType);
		}
		for (Type type : types) {
			List<Field> fields = (List) in.readObject();
			type.setFields(fields);
		}
		for (Type type : types) {
			List<Method> methods = (List) in.readObject();
			type.setMethods(methods);
		}
		for (Type type : types) {
			for (Method method : type.getMethods()) {
				method.setCFG((CFG) in.readObject());
			}
		}
	}
	public void print() {
		print(System.out);
	}
	public void print(PrintStream out) {
		out.println("Main class: " + mainClassName);
		for (Type type : getTypes())
			out.println(type.toLongString());
	}
	/**
	 * Validates this program (checks its partial specification).
	 */
	public void validate() {
		for (Type type : getTypes())
			type.validate();
	}
}
