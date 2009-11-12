/*
 * Copyright (c) 2006-07, The Trustees of Stanford University.  All
 * rights reserved.
 * Licensed under the terms of the GNU GPL; see COPYING for details.
 */
package chord.program;

/**
 * A field.
 * <p>
 * It may be of primitive or reference type.
 * 
 * @author Mayur Naik (mhn@cs.stanford.edu)
 */
public class Field implements java.io.Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 587666770881233591L;
	/**
	 * Name of the field.
	 */
	private String name;
	/**
	 * Type of the field.
	 */
	private Type declType;
	/**
	 * Type of the field's declaring class.
	 */
	private Type ctnrType;
	/**
	 * Modifiers of the field.
	 * Valid ones are: static, final, volatile, public, private, protected
	 */
	private int modifiers;
	/**
	 * The first line number of this field's declaration in its
	 * declaring file.
	 */
	private int lineNum;
	public Field(String name, Type declType, Type ctnrType, int lineNum) {
		this.name = name;
		this.declType = declType;
		this.ctnrType = ctnrType;
		this.lineNum = lineNum;
	}
	public String getName() {
		return name;
	}
	public Type getDeclType() {
		return declType;
	}
	public Type getCtnrType() {
		return ctnrType;
	}
	public int getLineNum() {
		return lineNum;	
	}
	public void setFinal() {
		modifiers |= Modifiers.FINAL;
	}
	public boolean isFinal() {
		return (modifiers & Modifiers.FINAL) != 0;
	}
	public void setStatic() {
		modifiers |= Modifiers.STATIC;
	}
	public boolean isStatic() {
		return (modifiers & Modifiers.STATIC) != 0;
	}
	public void setVolatile() {
		modifiers |= Modifiers.VOLATILE;
	}
	public boolean isVolatile() {
		return (modifiers & Modifiers.VOLATILE) != 0;
	}
	public void setPrivate() {
		modifiers |= Modifiers.PRIVATE;
	}
	public boolean isPrivate() {
		return (modifiers & Modifiers.PRIVATE) != 0;
	}
	public void setProtected() {
		modifiers |= Modifiers.PROTECTED;
	}
	public boolean isProtected() {
		return (modifiers & Modifiers.PROTECTED) != 0;
	}
	public void setPublic() {
		modifiers |= Modifiers.PUBLIC;
	}
	public boolean isPublic() {
		return (modifiers & Modifiers.PUBLIC) != 0;
	}
	public String toLongString() {
		return toString() + "\nmodifiers: " +
			Modifiers.toString(modifiers);
	}
	public String toString() {
		return "<" + ctnrType.getName() + ": " +
			declType.getName() + " " + name + ">";
	}
}
	
