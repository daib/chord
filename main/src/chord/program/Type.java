/*
 * Copyright (c) 2006-07, The Trustees of Stanford University.  All
 * rights reserved.
 * Licensed under the terms of the GNU GPL; see COPYING for details.
 */
package chord.program;

import java.util.List;

import chord.transforms.LoopRemover;
import chord.transforms.SyncRemover;
import chord.util.Assertions;

/**
 * A type.
 * 
 * @author Mayur Naik (mhn@cs.stanford.edu)
 */
public class Type implements java.io.Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3831395170109129202L;
	/**
	 * Name of the type (e.g., <tt>java.lang.Object</tt>).
	 */
	private String name;
	/**
	 * Kind of the type.
	 */
	private TypeKind kind;
	/**
	 * Name of the file in which the type is declared.
	 * It is null if the type is not a class.
	 */
	private String fileName;
	/**
	 * List of all supertypes of this type.
	 * <p>
	 * if this type is of kind <tt>PRIM_TYPE</tt> or <tt>NULL_TYPE</tt> or
	 * if this type is <tt>java.lang.Object</tt> then this list is empty.
	 * Otherwise, this list is <tt>[C,I1,...,In]</tt> where <tt>C</tt> is
	 * this type's superclass and <tt>I1,...,In</tt> are this type's
	 * implemented interfaces.  There are two special cases:
	 * <ul>
	 * <li> If this type is of kind <tt>INTERFACE_TYPE</tt> then this list
	 * is <tt>[java.lang.Object,I1,...,In]</tt>.</li>
	 * <li> If this type is of kind <tt>ARRAY_TYPE</tt> then this list is
	 * <tt>[java.lang.Object,java.lang.Cloneable,java.io.Serializable].</tt></li>
	 * </ul>
	 */
	private transient List<Type> suptypes;
	/**
	 * Element type if this type is of kind <tt>ARRAY_TYPE</tt> and
	 * null otherwise.
	 */
	private transient Type elemType;
	/**
	 * List of all fields if this type is of kind <tt>ABSTRACT_CLASS_TYPE</tt>
	 * or <tt>CONCRETE_CLASS_TYPE</tt> and an empty list otherwise.
	 */
	private transient List<Field> fields;
	/**
	 * List of all methods if this type is of kind <tt>ABSTRACT_CLASS_TYPE</tt>
	 * or <tt>CONCRETE_CLASS_TYPE</tt> and an empty list otherwise.
	 */
	private transient List<Method> methods;

	public Type(String name, TypeKind kind, String fileName) {
		Assertions.Assert(name != null);
		Assertions.Assert(kind != null);
		this.name = name;
		this.kind = kind;
		this.fileName = fileName;
	}
	public String getName() {
		return name;
	}
	public TypeKind getKind() {
		return kind;
	}
	public String getFileName() {
		return fileName;
	}
	public void setElemType(Type type) {
		this.elemType = type;
	}
	public Type getElemType() {
		return elemType;
	}
	public void setSuptypes(List<Type> suptypes) {
		this.suptypes = suptypes;
	}
	public List<Type> getSuptypes() {
		return suptypes;
	}
	public void setFields(List<Field> fields) {
		this.fields = fields;
	}
	public List<Field> getFields() {
		return fields;
	}
	public void setMethods(List<Method> methods) {
		this.methods = methods;
	}
	public List<Method> getMethods() {
		return methods;
	}
	public void addField(Field field) {
		Assertions.Assert(field != null);
		fields.add(field);
	}
	public Field getField(String name) {
		Assertions.Assert(name != null);
		for (Field field : fields) {
			if (field.getName().equals(name))
				return field;
		}
		return null;
	}
	public void addMethod(Method method) {
		Assertions.Assert(method != null);
		String sign = method.getSign();
		for (Method m : methods) {
			if (m.getSign().equals(sign)) {
				String msg = "Method having sign '" + sign +
					"' already exists in type '" +  name + "'.";
				throw new RuntimeException(msg);
			}
		}
		methods.add(method);
	}
	public Method getMethod(String sign) {
		Assertions.Assert(sign != null);
		for (Method method : methods) {
			if (method.getSign().equals(sign))
				return method;
		}
		return null;
	}
	public boolean isSubtypeOf(Type that) {
        if (that == this)
            return true;
        if (this.kind == TypeKind.NULL_TYPE)
            return true;
		if (that.kind == TypeKind.NULL_TYPE)
			return false;
        if (suptypes.contains(that))
            return true;
        for (Type t : suptypes) {
            if (t.isSubtypeOf(that))
                return true;
        }
		if (this.kind == TypeKind.ARRAY_TYPE &&
			that.kind == TypeKind.ARRAY_TYPE)
			return this.elemType.isSubtypeOf(that.elemType); 
        return false;
    }
	public boolean isRefType() {
		return kind == TypeKind.CONCRETE_CLASS_TYPE ||
			kind == TypeKind.INTERFACE_TYPE ||
			kind == TypeKind.ABSTRACT_CLASS_TYPE ||
			kind == TypeKind.ARRAY_TYPE;
	}
	public void removeLoops() {
		new LoopRemover(this);
	}
	public void removeSyncs() {
		new SyncRemover(this);
	}
	public void validate() {
		Assertions.Assert(suptypes != null);
		Assertions.Assert(fields != null);
		Assertions.Assert(methods != null);
		int numSuptypes = suptypes.size();
		if (kind == TypeKind.CONCRETE_CLASS_TYPE ||
				kind == TypeKind.ABSTRACT_CLASS_TYPE ||
				kind == TypeKind.INTERFACE_TYPE) {
			if (name.equals("java.lang.Object"))
				Assertions.Assert(numSuptypes == 0);
			else
				Assertions.Assert(numSuptypes > 0);
		}
		if (kind == TypeKind.ARRAY_TYPE) {
			Assertions.Assert(elemType != null);
			Assertions.Assert(numSuptypes == 3);
		} else
			Assertions.Assert(elemType == null);
		if (kind == TypeKind.PRIMITIVE_TYPE ||
				kind == TypeKind.NULL_TYPE)
			Assertions.Assert(numSuptypes == 0);
		for (Method method : methods)
			method.validate();
	}
	public String toLongString() {
		String s = "name: " + name + "\nfileName: " + fileName +
			"\nkind: " + kind + "\nsuptypes:";
		for (Type suptype : suptypes)
			s += "\n" + suptype;
		s += "\nfields:";
		for (Field field : fields)
			s += "\n" + field.toLongString();
		s += "\nmethods:";
		for (Method meth : methods)
			s += "\n" + meth.toLongString();
		return s;
	}
	public String toString() {
		return name;
	}
}
