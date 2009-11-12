/*
 * Copyright (c) 2006-07, The Trustees of Stanford University.  All
 * rights reserved.
 * Licensed under the terms of the GNU GPL; see COPYING for details.
 */
package chord.program;

import chord.program.insts.Inst;
import chord.util.Assertions;

/**
 * A method.
 * 
 * @author Mayur Naik (mhn@cs.stanford.edu)
 */
public class Method implements java.io.Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1193024892026943850L;
	/**
	 * Signature of every class initializer method.
	 */
	public static final String SIGN_OF_CLINIT = "<clinit>()";
	/**
	 * Signature of this method.
	 */
	private String sign;
	/**
	 * Modifiers of this method.
	 * <p>
	 * Modifiers currently allowed are: static, abstract, native,
	 * private, protected, and public.
	 */
	private int modifiers;
	/**
	 * The type of the class in which this method is declared.
	 */
	private Type ctnrType;
	/**
	 * The "immediate containing method" of this method.
	 */
	private Method ctnrMethod;
	/**
	 * Control-flow graph of this method.
	 */
	private transient CFG cfg;
	/**
	 * The first line number of this method's declaration in its
	 * declaring file.
	 */
	private int lineNum;
	/**
	 * @param	sign		Signature of this method.
	 * @param	ctnrType	The type of the class in which this
	 * 						method is declared.
	 * @param	ctnrMethod	The "immediate containing method" of this
	 * 						method.
	 * @param	lineNum		The first line number of this method's
	 * 						declaration in its containing file.
	 */
	public Method(String sign, Type ctnrType, Method ctnrMethod,
			int lineNum) {
		this.sign = sign;
		this.ctnrType = ctnrType;
		this.ctnrMethod = ctnrMethod;
		this.lineNum = lineNum;
	}
	/**
	 * Provides the name of this method (excluding its argument list
	 * and return type).
	 * 
	 * @return	The name of this method.
	 */
	public String getName() {
		return sign.substring(0, sign.indexOf('('));
	}
	/**
	 * Provides the signature of this method.
	 * <p>
	 * It is of the form <tt>name(arg1_type,...,argN_type)</tt>
	 * where <tt>name</tt> is the name of the method and
	 * <tt>arg1_type</tt>, ..., <tt>argN_type</tt> are the
	 * fully-qualified names of the method's arguments in
	 * order.
	 * <p>
	 * An example signature is <tt>main(java.lang.String[])</tt>.
	 * 
	 * @return	The signature of this method.
	 */
	public String getSign() {
		return sign;
	}
	/**
	 * Provides the name of the Java source file (e.g.,
	 * <tt>java/lang/Object.java</tt>) in which this method is
	 * declared.
	 * 
	 * @return	The name of the Java source file in which this
	 * 			method is declared.
	 */
	public String getFileName() {
		return ctnrType.getFileName();
	}
	/**
	 * Provides the type of the class in which this method is
	 * declared.
	 * 
	 * @return	The type of the class in which this method is
	 *			declared.
	 */
	public Type getCtnrType() {
		return ctnrType;
	}
	/**
	 * Provides the "immediate containing method" of this method.
	 * <p>
	 * It is best explained by means of an example.  Suppose the
	 * original program contains a method <tt>foo</tt>:
	 * <pre>
	 * foo() {
	 *     code1;
	 *     synchronized (...) {
	 *         code2;
	 *         synchronized (...) {
	 *             code3;
	 *         }
	 *     }
	 * }
	 * </pre>
	 * This method is transformed into the following three methods:
	 * <pre>
	 * foo() {
	 *     code1;
	 *     syncFreeMethod1();
	 * }
	 * 
	 * syncFreeMethod1() {
	 *     code2;
	 *     syncFreeMethod2();
	 * }
	 * 
	 * syncFreeMethod2() {
	 *     code3;
	 * }
	 * </pre>
	 * Then:
	 * <ul>
	 * <li>The immediate containing method of <tt>foo</tt> is
	 *     null.</li>
	 * <li>The immediate containing method of
	 *     <tt>syncFreeMethod1</tt> is <tt>foo</tt>.</li>
	 * <li>The immediate containing method of
	 *     <tt>syncFreeMethod2</tt> is <tt>syncFreeMethod1</tt>.</li>
	 * </ul>
	 * 
	 * @return	The "immediate containing method" of this method.
	 */
	public Method getImmediateCtnrMethod() {
		return ctnrMethod;
	}
	/**
	 * Provides the "outermost containing method" of this method.
	 * <p>
	 * It is best explained by means of an example.  Suppose the
	 * original program contains a method <tt>foo</tt>:
	 * <pre>
	 * foo() {
	 *     code1;
	 *     synchronized (...) {
	 *         code2;
	 *         synchronized (...) {
	 *             code3;
	 *         }
	 *     }
	 * }
	 * </pre>
	 * This method is transformed into the following three methods:
	 * <pre>
	 * foo() {
	 *     code1;
	 *     syncFreeMethod1();
	 * }
	 * 
	 * syncFreeMethod1() {
	 *     code2;
	 *     syncFreeMethod2();
	 * }
	 * 
	 * syncFreeMethod2() {
	 *     code3;
	 * }
	 * </pre>
	 * Then, the outermost containing method of each of methods
	 * <tt>foo</tt>, <tt>syncFreeMethod1</tt>, and
	 * <tt>syncFreeMethod2</tt> is <tt>foo</tt> itself.
	 * 
	 * @return	The "outermost containing method" of this method.
	 */
	public Method getOutermostCtnrMethod() {
        Method outermostCtnrMethod = this;
        do {
            Method parentMethod = outermostCtnrMethod.ctnrMethod;
            if (parentMethod == null)
                break;
            outermostCtnrMethod = parentMethod;
        } while (true);
        return outermostCtnrMethod;
	}
	/**
	 * Determines whether this method is declared in the original
	 * program or is synthesized, that is, generated from a loop body
	 * or a synchronized block in the body of a method declared in
	 * the original program.
	 * 
	 * @return	true if this method is synthesized.
	 */
	public boolean isSynthesized() {
		return (ctnrMethod != null);
	}
	/**
	 * Sets the control-flow graph of this method to the specified
	 * control-flow graph.
	 * 
	 * @param	cfg	A control-flow graph.
	 */
	public void setCFG(CFG cfg) {
		this.cfg = cfg;
	}
	/**
	 * Determines whether this method has a control-flow graph.
	 * 
	 * @return	true iff this method has a control-flow graph.
	 */
	public boolean hasCFG() {
		return cfg != null;
	}
	/**
	 * Provides the control-flow graph of this method.
	 * <p>
	 * It is null if the method does not contain any code, e.g., if
	 * it is an abstract or native method.
	 * 
	 * @return	The control-flow graph of this method.
	 */
	public CFG getCFG() {
		return cfg;
	}
	/**
	 * Provides the first line number of this method's declaration
	 * in its containing file.
	 *
	 * @return	The first line number of this method's declaration
	 * 			in its containing file.
	 */
	public int getLineNum() {
		return lineNum;
	}
	/**
	 * Designates this method as an abstract method.
	 */
	public void setAbstract() {
		modifiers |= Modifiers.ABSTRACT;
	}
	/**
	 * Determines if this method is an abstract method.
	 * 
	 * @return	true iff this method is an abstract method.
	 */
	public boolean isAbstract() {
		return (modifiers & Modifiers.ABSTRACT) != 0;
	}
	/**
	 * Designates this method as a native method.
	 */
	public void setNative() {
		modifiers |= Modifiers.NATIVE;
	}
	/**
	 * Determines if this method is a native method.
	 * 
	 * @return	true iff this method is a native method.
	 */
	public boolean isNative() {
		return (modifiers & Modifiers.NATIVE) != 0;
	}
	/**
	 * Designates this method as a static method.
	 */
	public void setStatic() {
		modifiers |= Modifiers.STATIC;
	}
	/**
	 * Determines if this method is a static method.
	 * 
	 * @return	true iff this method is a static method.
	 */
	public boolean isStatic() {
		return (modifiers & Modifiers.STATIC) != 0;
	}
	/**
	 * Designates this method as a private method.
	 */
	public void setPrivate() {
		modifiers |= Modifiers.PRIVATE;
	}
	/**
	 * Determines if this method is a private method.
	 * 
	 * @return	true iff this method is a private method.
	 */
	public boolean isPrivate() {
		return (modifiers & Modifiers.PRIVATE) != 0;
	}
	/**
	 * Designates this method as a protected method.
	 */
	public void setProtected() {
		modifiers |= Modifiers.PROTECTED;
	}
	/**
	 * Determines if this method is a protected method.
	 * 
	 * @return	true iff this method is a protected method.
	 */
	public boolean isProtected() {
		return (modifiers & Modifiers.PROTECTED) != 0;
	}
	/**
	 * Designates this method as a public method.
	 */
	public void setPublic() {
		modifiers |= Modifiers.PUBLIC;
	}
	/**
	 * Determines if this method is a public method.
	 * 
	 * @return	true iff this method is a public method.
	 */
	public boolean isPublic() {
		return (modifiers & Modifiers.PUBLIC) != 0;
	}
	/**
	 * Determines if this method is a constructor, namely, a method
	 * named <tt>&lt;init&gt;</tt>.
	 * 
	 * @return	true iff this method is a constructor.
	 */
	public boolean isInit() {
		return getName().equals("<init>");
	}
	/**
	 * Determines if this method is a class initializer method,
	 * namely, a method with signature <tt>&lt;clinit&gt;()</tt>.
	 * 
	 * @return	true iff this method is a class initializer method.
	 */
	public boolean isClinit() {
		return sign.equals(Method.SIGN_OF_CLINIT);
	}
	/**
	 * Determines if this method is the thread-starting method,
	 * namely, the method with signature <tt>start()</tt> in class
	 * <tt>java.lang.Thread</tt>.
	 * 
	 * @return	true iff this method is the thread-starting method.
	 */
	public boolean isStartMethod() {
		return sign.equals("start()") &&
			ctnrType.getName().equals("java.lang.Thread");
	}
	/**
	 * Removes all skip statements from the body of this method.
	 * <p>
	 * It has no effect if this method does not have a body or if
	 * its body has already been stripped of all skip statements.
	 */
	public void removeSkips() {
		if (cfg != null)
			cfg.removeSkips();
	}
	/**
	 * Converts the body of this method into Static Single Assignment
	 * (SSA) form.
	 * <p>
	 * It has no effect if this method does not have a body or if
	 * its body has already been converted into SSA form.
	 */
	public void convertToSSA() {
		if (cfg != null)
			cfg.convertToSSA();
	}
	/**
	 * Validates this method (checks its partial specification).
	 */
	public void validate() {
		Assertions.Assert(sign != null);
		// Check that sign matches a certain regular expression
		Assertions.Assert(ctnrType != null);
		if (cfg != null) {
			for (Inst inst : cfg.getNodes()) {
				Method method = inst.getImmediateCtnrMethod();
				Assertions.Assert(method == this,
					"Instruction '" + inst +
					"' has immediate containing method '" +
					method + "'; expected '" + this + "'.");
			}
			cfg.validate();
		}
	}
	public String toLongString() {
		return toString() + "\nmodifiers: " +
			Modifiers.toString(modifiers) +
			"\ncfg: " + ((cfg == null) ? "null" : cfg.toString());
	}
	public String toString() {
		return "<" + ctnrType.getName() + ": " + sign + ">";
	}
}
