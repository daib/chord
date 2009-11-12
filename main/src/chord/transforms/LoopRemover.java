/*
 * Copyright (c) 2006-07, The Trustees of Stanford University.  All
 * rights reserved.
 * Licensed under the terms of the GNU GPL; see COPYING for details.
 */
package chord.transforms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import chord.program.CFG;
import chord.program.IReachingDefsAnalysis;
import chord.program.Method;
import chord.program.Type;
import chord.program.Var;
import chord.program.insts.HeadInst;
import chord.program.insts.InstVisitor;
import chord.program.insts.Inst;
import chord.program.insts.InvkInst;
import chord.program.insts.InvkKind;
import chord.program.insts.AcqLockInst;
import chord.program.insts.RelLockInst;
import chord.program.insts.PhiExpAsgnInst;
import chord.program.insts.ObjVarAsgnInst;
import chord.program.insts.ObjValAsgnInst;
import chord.program.insts.ClsVarAsgnInst;
import chord.program.insts.AryElemRefInst;
import chord.program.insts.InstFldRefInst;
import chord.program.insts.StatFldRefInst;
import chord.program.insts.StrValAsgnInst;
import chord.program.insts.NilValAsgnInst;
import chord.program.insts.SkipInst;
import chord.program.insts.TailInst;
import chord.util.Assertions;
import chord.util.tuple.object.Pair;
import chord.util.ArraySet;

/**
 * Removes all loops from each method in the given class by
 * generating a fresh static method in the same class per loop.
 * Note: Synchronized block removal must be performed and skip
 * statement removal must NOT be performed before this
 * transformation.
 * 
 * @author Mayur Naik (mhn@cs.stanford.edu)
 */
public class LoopRemover {
	private static final Set<Inst> emptyInstSet =
		Collections.emptySet();
	/**
	 * The given class.
	 */
	private Type type;
	/**
	 * Map from each loop header node in the current method to the
	 * set of all nodes in the corresponding loop body.
	 */
	private Map<Inst, Set<Inst>> headToBody;
	/**
	 * The current method.
	 */
	private Method oldMethod;
	/**
	 * Control-flow graph of the current method.
	 */
	private CFG oldCfg;
	/**
	 * Reaching definitions analysis of the current method.
	 */
	private IReachingDefsAnalysis reachingDefs;
	/**
	 * Map from each node in the control-flow graph of each
	 * generated loop-free method (including the outermost one) to
	 * the corresponding node in the control-flow graph of the
	 * current method.
	 */
	private Map<Inst, Inst> visited;
	/**
	 * Current number of generated loop-free methods (excluding
	 * the outermost ones) in the given class.
	 */
	private int numLoopFreeMethods;
	public LoopRemover(Type type) {
		this.type = type;
		ArrayList<Method> oldMethods =
			new ArrayList<Method>(type.getMethods());
		for (Method method : oldMethods) {
			CFG cfg = method.getCFG();
			if (cfg == null)
				continue;
			headToBody = cfg.loopRemovalHelper();
			int numSyncs = headToBody.size();
			if (numSyncs == 0)
				continue;
            try {
                List<Inst> heads = new ArrayList<Inst>(numSyncs);
                for (Inst head : headToBody.keySet())
                    heads.add(head);
				HeadInst head = cfg.getHead();
                for (int i = 0; i < numSyncs; i++) {
                    Inst head1 = heads.get(i);
                    Set<Inst> body1 = headToBody.get(head1);
					Assertions.Assert(!body1.contains(head));
                    Assertions.Assert(body1.contains(head1));
                    for (Inst inst1 : body1) {
                        if (inst1 != head1) {
                            for (Inst pred : cfg.getPreds(inst1)) {
                                Assertions.Assert(body1.contains(pred));
                            }
                        }
                    }
                    for (int j = i + 1; j < numSyncs; j++) {
                        Inst head2 = heads.get(j);
                        Set<Inst> body2 = headToBody.get(head2);
						if (body2.containsAll(body1)) {
							Assertions.Assert(!body1.contains(head2));
							continue;
						}
                        if (body1.containsAll(body2)) {
							Assertions.Assert(!body2.contains(head1));
							continue;
						}
                        for (Inst inst1 : body1) {
                            Assertions.Assert(!body2.contains(inst1));
                        }
                        for (Inst inst2 : body2) {
                            Assertions.Assert(!body1.contains(inst2));
                        }
                    }
                }
            } catch (Exception ex) {
                System.out.println("WARNING: Loop inference failed for method: " +
                    method + "; setting its CFG to null");
                method.setCFG(null);
                continue;
            }
			reachingDefs = cfg.getReachingDefs();
			visited = new HashMap<Inst, Inst>(cfg.numNodes());
			this.oldMethod = method;
			this.oldCfg = cfg;
			try {
				new OuterBuilder();
			} catch (Exception ex) {
				ex.printStackTrace();
				System.exit(1);
			}
		}
	}
	private class OuterBuilder {
		private final Map<Inst, Set<Inst>> newInstToSuccs;
		private TailInst newTail;
		private OuterBuilder() {
			newInstToSuccs = new HashMap<Inst, Set<Inst>>();
			HeadInst oldHead = oldCfg.getHead();
			TailInst oldTail = oldCfg.getTail();
			if (oldTail != null) {
				newInstToSuccs.put(oldTail, emptyInstSet);
				visited.put(oldTail, oldTail);
				newTail = oldTail;
			}
			Set<Inst> headSuccs = new ArraySet<Inst>();
			newInstToSuccs.put(oldHead, headSuccs);
			for (Inst inst : oldCfg.getSuccs(oldHead))
				headSuccs.add(visit(inst));
			CFG cfg = new CFG(oldHead, newTail,
				oldCfg.getArgs(), oldCfg.getTmps(), oldCfg.getRets(),
				oldCfg.getSync(), null, newInstToSuccs);
			for (Var var : oldCfg.getArgs())
				var.setCFG(cfg);
			for (Var var : oldCfg.getTmps())
				var.setCFG(cfg);
			for (Inst inst : cfg.getNodes())
				inst.setCFG(cfg);
			oldMethod.setCFG(cfg);
			cfg.setCtnrMethod(oldMethod);
		}

		private Inst visit(Inst oldInst) {
			Inst newInst = visited.get(oldInst);
			if (newInst != null)
				return newInst;
			Set<Inst> oldSuccs;
			int numSuccs;
			if (headToBody.containsKey(oldInst)) {
				InnerBuilder builder =
					new InnerBuilder(oldInst, oldMethod);
				newInst = createInvkInst(builder);
				Set<Inst> exits = builder.oldExits;
				numSuccs = exits.size();
				oldSuccs = exits;
				if (numSuccs == 0) {
					// infinite loop
					if (newTail == null) {
						newTail = new TailInst(0);
						newInstToSuccs.put(newTail, emptyInstSet);
					}
					visited.put(oldInst, newInst); 
					Set<Inst> newSuccs = new ArraySet<Inst>(1);
					newInstToSuccs.put(newInst, newSuccs);
					newSuccs.add(newTail);
					return newInst;
				}
			} else {
				newInst = oldInst;
				oldSuccs = oldCfg.getSuccs(oldInst);
				numSuccs = oldSuccs.size();
			}
			visited.put(oldInst, newInst);
			Set<Inst> newSuccs = new ArraySet<Inst>(numSuccs);
			newInstToSuccs.put(newInst, newSuccs);
			for (Inst oldSucc : oldSuccs)
				newSuccs.add(visit(oldSucc));
			return newInst;
		}
		private InvkInst createInvkInst(InnerBuilder calleeBuilder) {
			List<Var> oldArgs = calleeBuilder.oldArgs;
			List<Var> oldRets = calleeBuilder.oldRets;
			List<Var> newArgs = new ArrayList<Var>(oldArgs);
			List<Var> newRets = new ArrayList<Var>(oldRets);
			Method rslvMeth = calleeBuilder.newMethod;
			return new InvkInst(InvkKind.INVK_STATIC, rslvMeth,
				newArgs, newRets, -1);
		}
	}
	private class InnerBuilder {
		private final Method newMethod;
		private final Set<Inst> oldExits;
		private final Map<Inst, Set<Inst>> newInstToSuccs;
		private final List<Var> oldArgs;
		private final List<Var> oldRets;
		private final List<Var> newRets;
		private Set<Inst> oldBody;
		private Inst oldHead;
		private TailInst newTail;
		private InvkInst newInvk;
		private final Map<Var, Var> varMap;
		private InnerBuilder(Inst oldHead, Method parent) {
			String name = "loopFreeMethod_" + numLoopFreeMethods;
			numLoopFreeMethods++;
			String sign = name + "(args)";
			int lineNum = oldHead.getLineNum();
			newMethod = new Method(sign, type, parent, lineNum);
			newMethod.setStatic();
			this.oldHead = oldHead;
			oldBody = headToBody.get(oldHead);
            // Build oldArgs and oldRets as vars that must be made
            // args and rets respectively of method generated from
            // body of current loop.
            // A var must be made an arg if it is used by some inst
            // inside the body of the loop and a definition of that
            // var outside the body of the loop reaches that use.
            // A var must be made a ret if it is used by some inst
            // outside the body of the loop and a definition of that
            // var inside the body of the loop reaches that use.
			oldArgs = new ArrayList<Var>();
			oldRets = new ArrayList<Var>();
			for (Inst inst : oldCfg.getNodes()) {
				Set<Pair<Var, Inst>> iDefs =
					reachingDefs.getIncomingDefs(inst);
				if (oldBody.contains(inst)) {
					for (Pair<Var, Inst> tuple : iDefs) {
						if (!oldBody.contains(tuple.val1)) {
							Var var = tuple.val0;
							if (inst.uses(var) && !oldArgs.contains(var))
								oldArgs.add(var);
						}
					}
				} else {
					for (Pair<Var, Inst> tuple : iDefs) {
						if (oldBody.contains(tuple.val1)) {
							Var var = tuple.val0;
							if (inst.uses(var) && !oldRets.contains(var))
								oldRets.add(var);
						}
					}
				}
			}
			varMap = new HashMap<Var, Var>();
			List<Var> newArgs = new ArrayList<Var>(oldArgs.size());
			for (Var oldVar : oldArgs) {
				Var newVar = oldVar.copy();
				varMap.put(oldVar, newVar);
				newArgs.add(newVar);
			}
			List<Var> newTmps = new ArrayList<Var>();
			for (Inst oldInst : oldBody) {
				Var[] defs = oldInst.getDefs();
				for (Var oldVar : defs) {
					Var newVar = varMap.get(oldVar);
					if (newVar == null) {
						newVar = oldVar.copy();
						varMap.put(oldVar, newVar);
						newTmps.add(newVar);
					}
				}
			}
			newRets = new ArrayList<Var>(oldRets.size());
			for (Var oldVar : oldRets) {
				Var newVar = varMap.get(oldVar);
				Assertions.Assert(newVar != null);
				newRets.add(newVar);
			}
			type.addMethod(newMethod);
			newTail = new TailInst(0);
			newInvk = new InvkInst(InvkKind.INVK_STATIC, newMethod,
				new ArrayList<Var>(newArgs),
				new ArrayList<Var>(newRets), -1);
			HeadInst newHead = new HeadInst(lineNum);
			oldExits = new ArraySet<Inst>();
			newInstToSuccs = new HashMap<Inst, Set<Inst>>();
			Set<Inst> invkSuccs = new ArraySet<Inst>(1);
			invkSuccs.add(newTail);
			newInstToSuccs.put(newInvk, invkSuccs);
			Set<Inst> tailSuccs = emptyInstSet;
			newInstToSuccs.put(newTail, tailSuccs);
			Inst fstInst = visit(oldHead);
            Inst tmpInst = newHead;
            for (Var var : newTmps) {
                Set<Inst> succs = new ArraySet<Inst>(1);
                newInstToSuccs.put(tmpInst, succs);
                NilValAsgnInst inst = new NilValAsgnInst(var, -1);
                succs.add(inst);
                tmpInst = inst;
            }
            Set<Inst> succs = new ArraySet<Inst>(1);
			succs.add(fstInst);
            newInstToSuccs.put(tmpInst, succs);
            CFG cfg = new CFG(newHead, newTail, newArgs,
            	newTmps, newRets, null, null, newInstToSuccs);
            for (Var var : newArgs)
            	var.setCFG(cfg);
            for (Var var : newTmps)
            	var.setCFG(cfg);
            for (Inst inst : cfg.getNodes())
            	inst.setCFG(cfg);
			newMethod.setCFG(cfg);
			cfg.setCtnrMethod(newMethod);
		}
		private Inst visit(Inst oldInst) {
			Inst newInst = visited.get(oldInst);
			if (newInst != null) {
				return newInst;
			}
			Set<Inst> oldSuccs;
			if (headToBody.containsKey(oldInst) && oldInst != oldHead) {
				// oldInst is the head of a loop nested
				// inside the currently processed loop.
				InnerBuilder builder =
					new InnerBuilder(oldInst, newMethod);
				newInst = createInvkInst(builder);
				Set<Inst> exits = builder.oldExits;
				oldSuccs = exits;
			} else {
				oldInst.accept(visitor);
				newInst = visitor.newInst;
				oldSuccs = oldCfg.getSuccs(oldInst);
			}
			visited.put(oldInst, newInst);
			Set<Inst> newSuccs = new ArraySet<Inst>(oldSuccs.size());
			newInstToSuccs.put(newInst, newSuccs);
			for (Inst oldSucc : oldSuccs) {
				if (oldSucc.equals(oldHead)) {
					// oldSucc is head of currently processed loop.
					newSuccs.add(newInvk);
				} else if (oldBody.contains(oldSucc)) {
					// oldSucc is some node other than the head in
					// the body of the currently processed loop.
					newSuccs.add(visit(oldSucc));
				} else {
					// oldSucc is outside currently processed loop.
					oldExits.add(oldSucc);
					newSuccs.add(newTail);
				}
			}
			return newInst;
		}
        private MyInstVisitor visitor = new MyInstVisitor();
        private class MyInstVisitor implements InstVisitor {
            private Inst newInst;
            public void visit(HeadInst inst) {
            	throw new RuntimeException();
            }
            public void visit(TailInst inst) {
            	throw new RuntimeException();
            }
            public void visit(SkipInst inst) {
                newInst = new SkipInst(inst.getLineNum());
            }
            public void visit(NilValAsgnInst inst) {
                Var newVar = varMap.get(inst.getVar());
                Assertions.Assert(newVar != null);
                newInst = new NilValAsgnInst(newVar,
                    inst.getLineNum());
            }
            public void visit(StrValAsgnInst inst) {
                Var newVar = varMap.get(inst.getVar());
                Assertions.Assert(newVar != null);
                newInst = new StrValAsgnInst(newVar, inst.getStr(),
                    inst.getLineNum());
            }
            public void visit(ObjVarAsgnInst inst) {
                Var newVar1 = varMap.get(inst.getLvar());
                Assertions.Assert(newVar1 != null);
                Var newVar2 = varMap.get(inst.getRvar());
                Assertions.Assert(newVar2 != null);
                newInst = new ObjVarAsgnInst(newVar1, newVar2,
                    inst.getLineNum());
            }
            public void visit(ObjValAsgnInst inst) {
                Var newVar = varMap.get(inst.getVar());
                Assertions.Assert(newVar != null);
                newInst = new ObjValAsgnInst(newVar, inst.getType(),
                    inst.getLineNum());
            }
            public void visit(PhiExpAsgnInst inst) {
                throw new RuntimeException();
            }
            public void visit(ClsVarAsgnInst inst) {
                Var newVar1 = varMap.get(inst.getVar());
                Assertions.Assert(newVar1 != null);
                Var newVar2 = varMap.get(inst.getBase());
                Assertions.Assert(newVar2 != null);
                newInst = new ClsVarAsgnInst(newVar1, newVar2,
                    inst.getLineNum());
            }
            public void visit(AryElemRefInst inst) {
                Var oldVar1 = inst.getVar();
                Var newVar1;
                if (oldVar1 != null) {
                    newVar1 = varMap.get(oldVar1);
                    Assertions.Assert(newVar1 != null);
                } else
                    newVar1 = null;
                Var newVar2 = varMap.get(inst.getBase());
                Assertions.Assert(newVar2 != null);
                newInst = new AryElemRefInst(newVar1, newVar2,
                    inst.isWr(), inst.getLineNum());
            }
            public void visit(InstFldRefInst inst) {
                Var oldVar1 = inst.getVar();
                Var newVar1;
                if (oldVar1 != null) {
                    newVar1 = varMap.get(oldVar1);
                    Assertions.Assert(newVar1 != null);
                } else
                    newVar1 = null;
                Var newVar2 = varMap.get(inst.getBase());
                Assertions.Assert(newVar2 != null);
                newInst = new InstFldRefInst(newVar1, newVar2,
                    inst.getField(), inst.isWr(), inst.getLineNum());
            }
            public void visit(StatFldRefInst inst) {
                Var oldVar = inst.getVar();
                Var newVar;
                if (oldVar != null) {
                    newVar = varMap.get(oldVar);
                    Assertions.Assert(newVar != null);
                } else
                    newVar = null;
                newInst = new StatFldRefInst(newVar, inst.getField(),
                    inst.isWr(), inst.getLineNum());
            }
            public void visit(InvkInst inst) {
                List<Var> oldArgs = inst.getArgs();
                int numOldArgs = oldArgs.size();
                List<Var> oldRets = inst.getRets();
                int numOldRets = oldRets.size();
                List<Var> newArgs = new ArrayList<Var>(numOldArgs);
                List<Var> newRets = new ArrayList<Var>(numOldRets);
                for (Var oldVar : oldArgs) {
                    Var newVar = varMap.get(oldVar);
                    Assertions.Assert(newVar != null);
                    newArgs.add(newVar);
                }
                for (Var oldVar : oldRets) {
                    Var newVar = varMap.get(oldVar);
                    Assertions.Assert(newVar != null);
                    newRets.add(newVar);
                }
                newInst = new InvkInst(inst.getInvkKind(),
                	inst.getRslvMethod(), newArgs, newRets,
                	inst.getLineNum());
            }
            public void visit(AcqLockInst inst) {
            	throw new RuntimeException("");
            }
            public void visit(RelLockInst inst) {
            	throw new RuntimeException("");
            }
        };
		private InvkInst createInvkInst(InnerBuilder calleeBuilder) {
			List<Var> oldArgs = calleeBuilder.oldArgs;
			List<Var> oldRets = calleeBuilder.oldRets;
			int numOldArgs = oldArgs.size();
			int numOldRets = oldRets.size();
			List<Var> newArgs = new ArrayList<Var>(numOldArgs);
			List<Var> newRets = new ArrayList<Var>(numOldRets);
			for (Var oldVar : oldArgs) {
				Var newVar = varMap.get(oldVar);
				Assertions.Assert(newVar != null);
				newArgs.add(newVar);
			}
			for (Var oldVar : oldRets) {
				Var newVar = varMap.get(oldVar);
				Assertions.Assert(newVar != null);
				newRets.add(newVar);
			}
			return new InvkInst(InvkKind.INVK_STATIC,
				calleeBuilder.newMethod, newArgs, newRets, -1);
		}
	}
}
