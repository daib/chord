/*
 * Copyright (c) 2006-07, The Trustees of Stanford University.  All
 * rights reserved.
 * Licensed under the terms of the GNU GPL; see COPYING for details.
 */
package chord.program.insts;

import java.util.List;

import chord.program.CFG;
import chord.program.Method;
import chord.program.Var;
import chord.util.Assertions;

/**
 * A method invocation statement of the form <tt>r1,...,rn = m(a1,...,ak)</tt>
 * where <tt>r1</tt>,...,<tt>rn</tt>, <tt>a1</tt>,...,<tt>ak</tt> are local
 * variables of reference type.
 * 
 * @author Mayur Naik (mhn@cs.stanford.edu)
 */
public class InvkInst extends Inst {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3278874757896541656L;
	/**
	 * The kind of this call statement.
	 */
	private InvkKind kind;
	/**
	 * The resolved method of this call statement.
	 */
	private Method rslvMethod;
	/**
	 * List of actual arguments of reference type.
	 */
	private List<Var> args;
	/**
	 * List of return variables of reference type.
	 */
	private List<Var> rets;
	public InvkInst(InvkKind kind, Method rslvMethod, List<Var> args,
			List<Var> rets, int lineNum) {
		super(lineNum);
		Assertions.Assert(kind != null);
		this.kind = kind;
		Assertions.Assert(rslvMethod != null);
		this.rslvMethod = rslvMethod;
		Assertions.Assert(args != null);
		Assertions.Assert(!args.contains(null));
		this.args = args;
		Assertions.Assert(rets != null);
		Assertions.Assert(!rets.contains(null));
		this.rets = rets;
	}
	public List<Var> getArgs() {
		return args;
	}
	public int numArgs() {
		return args.size();
	}
	public List<Var> getRets() {
		return rets;
	}
	public int numRets() {
		return rets.size();
	}
	public Method getRslvMethod() {
		return rslvMethod;
	}
	public InvkKind getInvkKind() {
		return kind;
	}
	public boolean isSynthesized() {
		return rslvMethod.isSynthesized();
	}
	public Var[] getUses() {
		if (args.isEmpty())
			return Inst.emptyVarArray;
		Var[] uses = new Var[args.size()];
		args.toArray(uses);
		return uses;
	}
	public Var[] getDefs() {
		if (rets.isEmpty())
			return Inst.emptyVarArray;
		Var[] defs = new Var[rets.size()];
		rets.toArray(defs);
		return defs;
	}
	public Var[] getVars() {
		Var[] vars = new Var[args.size() + rets.size()];
		int i = 0;
		for (Var var : args) {
			vars[i] = var;
			i++;
		}
		for (Var var : rets) {
			vars[i] = var;
			i++;
		}
		return vars;
	}
	public void validate() {
		CFG cfg = rslvMethod.getCFG();
		if (cfg != null) {
			Assertions.Assert(cfg.getArgs().size() == args.size());
			Assertions.Assert(cfg.getRets().size() >= rets.size());
		}
	}
	public void accept(InstVisitor v) {
		v.visit(this);
	}
	public String toString() {
		return location() + ": " + kind + " " + rslvMethod +
			" args: " + args + " rets: " + rets;
	}
}
