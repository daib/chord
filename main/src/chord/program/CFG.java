/*
 * Copyright (c) 2006-07, The Trustees of Stanford University.  All
 * rights reserved.
 * Licensed under the terms of the GNU GPL; see COPYING for details.
 */
package chord.program;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import chord.program.insts.AcqLockInst;
import chord.program.insts.AryElemRefInst;
import chord.program.insts.ClsVarAsgnInst;
import chord.program.insts.HeadInst;
import chord.program.insts.Inst;
import chord.program.insts.InstFldRefInst;
import chord.program.insts.InstVisitor;
import chord.program.insts.InvkInst;
import chord.program.insts.NilValAsgnInst;
import chord.program.insts.ObjValAsgnInst;
import chord.program.insts.ObjVarAsgnInst;
import chord.program.insts.PhiExpAsgnInst;
import chord.program.insts.RelLockInst;
import chord.program.insts.SkipInst;
import chord.program.insts.StatFldRefInst;
import chord.program.insts.StrValAsgnInst;
import chord.program.insts.TailInst;
import chord.util.ArraySet;
import chord.util.Assertions;
import chord.util.IndexMap;
import chord.util.graph.MutableGraph;
import chord.util.tuple.object.Pair;
import chord.util.CollectionUtils;

/**
 * The control-flow graph of a method.
 * <p>
 * It has a unique entry node and a unique exit node.
 * The entry and exit nodes are distinct.
 * Every node in the control-flow graph is reachable
 * from the entry node.
 * 
 * @author Mayur Naik (mhn@cs.stanford.edu)
 */
public class CFG extends MutableGraph<Inst> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -105935582669657714L;
	/**
	 * The containing method's formal arguments of reference type
	 * in order.
	 */
	private List<Var> args;
	/**
	 * The containing method's local variables of reference type,
	 * excluding formal arguments.
	 */
	private List<Var> tmps;
	/**
	 * The containing method's return variables of reference type
	 * in order.
	 */
	private List<Var> rets;
	/**
	 * Formal argument on which the containing method of this
	 * control-flow graph is synchronized.
	 * It is null if the method is not synchronized.
	 */
	private Var sync;
	/**
	 * Unique entry node of this control-flow graph.
	 * It is non-null.
	 * It is the only node in the control-flow graph without any
	 * immediate predecessors.
	 */
	private HeadInst head;
	/**
	 * Unique exit node of this control-flow graph.
	 * It is null if the method does not return, e.g., it has an
	 * infinite loop.
	 * It is the only node in the control-flow graph without any
	 * immediate successors.
	 */
	private TailInst tail;
	/**
	 * Containing method of this control-flow graph.
	 */
	private Method ctnrMethod;
	/**
	 * Constructor.
	 * 
	 * @param	head	The unique entry node of this
	 * 			control-flow graph.
	 * @param	tail	The unique exit node of this
	 * 			control-flow graph.
	 * @param	args	The containing method's formal arguments
	 * 			of reference type in order.
	 * @param 	tmps	The containing method's local variables
	 * 			of reference type, excluding formal arguments.
	 * @param	rets	The containing method's return variables
	 * 			of reference type in order.
	 * @param	sync	Formal argument on which the containing
	 * 			method of this control-flow graph is synchronized.
	 *			It is null if the method is not synchronized.
	 * @param	instToPredsMap	A map from each node in this
	 * 			control-flow graph to the set of its immediate
	 * 			predecessors.
	 * @param	instToSuccsMap	A map from each node in this
	 * 			control-flow graph to the set of its immediate
	 * 			successors.
	 */
	public CFG(HeadInst head, TailInst tail,
		List<Var> args, List<Var> tmps, List<Var> rets, Var sync,
		Map<Inst, Set<Inst>> instToPredsMap,
		Map<Inst, Set<Inst>> instToSuccsMap) {
		super(Collections.singleton((Inst) head),
			instToPredsMap, instToSuccsMap);
		this.head = head;
		this.tail = tail;
		this.args = args;
		this.tmps = tmps;
		this.rets = rets;
		this.sync = sync;
	}
	/**
	 * Sets the containing method of this control-flow graph.
	 * 
	 * @param	ctnrMethod	The containing method of this
	 * 			control-flow graph.
	 */
	public void setCtnrMethod(Method ctnrMethod) { 
		this.ctnrMethod = ctnrMethod;
	}
	/**
	 * Provides the containing method of this control-flow graph.
	 * 
	 * @return	The containing method of this control-flow graph.
	 */
	public Method getCtnrMethod() {
		return ctnrMethod;
	}
	/**
	 * Removes all skip statements from this control-flow graph.
	 */
	public void removeSkips() {
		Set<Inst> skips = new ArraySet<Inst>();
		for (Inst inst : getNodes()) {
			if (inst instanceof SkipInst)
				skips.add(inst);
		}
		for (Inst inst : skips)
			bypassNode(inst);
	}
	/**
	 * Provides the unique entry node of this control-flow graph.
	 *
	 * @return	The unique entry node of this control-flow graph.
	 */
	public HeadInst getHead() {
		return head;
	}
	/**
	 * Provides the unique exit node of this control-flow graph.
	 *
	 * @return	The unique exit node of this control-flow graph.
	 */
	public TailInst getTail() {
		return tail;
	}
	/**
	 * Provides the containing method's formal arguments of
	 * reference type in order.
	 *
	 * @return	The containing method's formal arguments of
	 * 			reference type in order.
	 */
	public List<Var> getArgs() {
		return args;
	}
	/**
	 * Provides the containing method's local variables of
	 * reference type, excluding formal arguments.
	 *
	 * @return	The containing method's local variables of
	 *			reference type, excluding formal arguments.
	 *	 */
	public List<Var> getTmps() {
		return tmps;
	}
	/**
	 * Provides the containing method's return variables of
	 * reference type in order.
	 *
	 * @return	The containing method's return variables of
	 * 			reference type in order.
	 */
	public List<Var> getRets() {
		return rets;
	}
	/**
	 * Provides the formal argument on which the containing method
	 * is synchronized.
	 * It is null if the method is not synchronized.
	 * 
	 * @return	The formal argument on which the containing method
	 *			is synchronized.
	 */
	public Var getSync() {
		return sync;
	}
	/**
	 * Validates this control-flow graph (checks its partial
	 * specification).
	 */
	public void validate() {
		// Check that head inst is non-null and exists in the cfg.
		Assertions.Assert(head != null);
		Assertions.Assert(hasNode(head));
		// Check that tail inst, if non-null, exists in the cfg.
		if (tail != null)
			Assertions.Assert(hasNode(tail));
		// Check that args, tmps, and rets are non-null and do not
		// contain null.
		Assertions.Assert(args != null);
		Assertions.Assert(tmps != null);
		Assertions.Assert(rets != null);
		Assertions.Assert(!args.contains(null));
		Assertions.Assert(!tmps.contains(null));
		Assertions.Assert(!rets.contains(null));
		// Check that arg vars and tmp vars are disjoint.
		for (Var var : args) {
			Assertions.Assert(!tmps.contains(var));
		}
		// Check that each arg var's and each tmp var's cfg is
		// set to this cfg.
		for (Var var : args) {
			Assertions.Assert(var.getCFG() == this);
		}
		for (Var var : tmps) {
			Assertions.Assert(var.getCFG() == this);
		}
		// Check that args, tmps, and rets do not contain duplicates.
		Assertions.Assert(!CollectionUtils.hasDuplicates(args));
		Assertions.Assert(!CollectionUtils.hasDuplicates(tmps));
		Assertions.Assert(!CollectionUtils.hasDuplicates(rets));
		// Check that each ret var is a local var (i.e. is contained
		// in arg vars or tmp vars).
		for (Var var : rets) {
			Assertions.Assert(args.contains(var) ||
				tmps.contains(var));
		}
		// Check that the var on which this method is synchronized
		// is an arg var.
		if (sync != null)
			Assertions.Assert(args.contains(sync));
		for (Inst inst : getNodes()) {
			inst.validate();
			// Check that each var accessed in each instruction is
			// contained in args or tmps.
			for (Var var : inst.getUses()) {
				Assertions.Assert(args.contains(var) ||
					tmps.contains(var));
			}
			for (Var var : inst.getDefs()) {
				Assertions.Assert(args.contains(var) ||
					tmps.contains(var));
			}
			// Check that only head inst has 0 preds.
			Set<Inst> preds = getPreds(inst);
			if (preds.isEmpty()) {
				Assertions.Assert(inst == head, head.getImmediateCtnrMethod() +
					": Stmt [" + inst + "] is not head and has 0 preds");
			} else {
				Assertions.Assert(inst != head, head.getImmediateCtnrMethod() +
					": Stmt [" + inst + "] is head and has > 0 preds");
			}
			// Check that only tail inst has 0 succs.
			Set<Inst> succs = getSuccs(inst);
			if (succs.isEmpty()) {
				Assertions.Assert(inst == tail, head.getImmediateCtnrMethod() +
					": Stmt [" + inst + "] is not tail and has 0 succs");
			} else {
				Assertions.Assert(inst != tail, head.getImmediateCtnrMethod() +
					": Stmt [" + inst + "] is tail and has > 0 succs");
			}
		}
		// Check that every node in the cfg is reachable from head.
		Assertions.Assert(isConnected());
		// Check that no uninitialized variables are used.
		List<Var> origArgs = args;
		args = new ArrayList<Var>(args.size() + tmps.size());
		args.addAll(origArgs);
		args.addAll(tmps);
		IReachingDefsAnalysis analysis = getReachingDefs();
		for (Inst inst : getNodes()) {
			Set<Pair<Var, Inst>> incDefs =
				analysis.getIncomingDefs(inst);
			if (inst instanceof PhiExpAsgnInst)
				continue;
			for (Var var : inst.getUses()) {
				if (origArgs.contains(var))
					continue;
				Pair<Var, Inst> pair = new Pair<Var, Inst>(var, head);
				Assertions.Assert(!incDefs.contains(pair),
					"Uninitialized var '" + var + "' used at inst '" + inst +
					"' in method '" + head.getImmediateCtnrMethod() + "'.");
			}
		}
		args = origArgs;
	}
	/**
	 * Performs reaching definitions dataflow analysis on this
	 * control-flow graph.
	 * 
	 * @return	Result of reaching definitions dataflow analysis
	 * 			performed on this control-flow graph.
	 */
    public IReachingDefsAnalysis getReachingDefs() {
    	final Map<Inst, Set<Pair<Var, Inst>>> incDefsMap =
    		new HashMap<Inst, Set<Pair<Var, Inst>>>();
    	final Map<Inst, Set<Pair<Var, Inst>>> outDefsMap =
    		new HashMap<Inst, Set<Pair<Var, Inst>>>();
    	List<Inst> insts = getNodesInRPO();
    	for (Inst inst : insts) {
    		incDefsMap.put(inst, new HashSet<Pair<Var, Inst>>());
    		outDefsMap.put(inst, new HashSet<Pair<Var, Inst>>());
    	}
    	boolean changed = true;
    	while (changed) {
    		changed = false;
    		for (Inst inst : insts) {
    			Set<Pair<Var, Inst>> incDefs = incDefsMap.get(inst);
    			for (Inst pred : getPreds(inst)) {
    				Set<Pair<Var, Inst>> outDefs =
    					outDefsMap.get(pred);
    				changed |= incDefs.addAll(outDefs);
    			}
    			Set<Pair<Var, Inst>> outDefs = outDefsMap.get(inst);
    			Var[] defVars = inst.getDefs();
    			for (Var var : defVars) {
    				Pair<Var, Inst> genDef =
    					new Pair<Var, Inst>(var, inst);
    				changed |= outDefs.add(genDef);
    			}
				for (Pair<Var, Inst> incDef : incDefs) {
					Var var = incDef.val0;
					if (!inst.defs(var))
						changed |= outDefs.add(incDef);
				}
    		}
    	}
    	return new IReachingDefsAnalysis() {
			public Set<Pair<Var, Inst>> getIncomingDefs(Inst inst) {
				return incDefsMap.get(inst);
			}
			public Set<Pair<Var, Inst>> getOutgoingDefs(Inst inst) {
				return outDefsMap.get(inst);
			}
    	};
    }
	/**
	 * Performs liveness dataflow analysis on this control-flow graph.
	 * 
	 * @return	Result of liveness dataflow analysis performed on this
	 * 			control-flow graph.
	 */
    public ILivenessAnalysis getLiveVars() {
    	final Map<Inst, Set<Var>> incLiveVarsMap =
    		new HashMap<Inst, Set<Var>>();
    	final Map<Inst, Set<Var>> outLiveVarsMap =
    		new HashMap<Inst, Set<Var>>();
    	List<Inst> insts = getNodesInRPO();
    	for (Inst inst : insts) {
    		incLiveVarsMap.put(inst, new HashSet<Var>());
    		outLiveVarsMap.put(inst, new HashSet<Var>());
    	}
    	boolean changed = true;
    	while (changed) {
    		changed = false;
    		for (int i = insts.size() - 1; i >= 0; i--) {
    			Inst inst = insts.get(i);
    			Set<Var> outLiveVars = outLiveVarsMap.get(inst);
    			for (Inst succ : getSuccs(inst)) {
    				Set<Var> incLiveVars = incLiveVarsMap.get(succ);
    				changed |= outLiveVars.addAll(incLiveVars);
    			}
    			Set<Var> incLiveVars = incLiveVarsMap.get(inst);
    			List<Var> useVars = Arrays.asList(inst.getUses());
    			changed |= incLiveVars.addAll(useVars);
    			List<Var> defVars = Arrays.asList(inst.getDefs());
    			for (Var var : outLiveVars) {
    				if (!defVars.contains(var))
    					changed |= incLiveVars.add(var);
    			}
    		}
    	}
    	return new ILivenessAnalysis() {
			public Set<Var> getIncomingLiveVars(Inst inst) {
				return incLiveVarsMap.get(inst);
			}
			public Set<Var> getOutgoingLiveVars(Inst inst) {
				return outLiveVarsMap.get(inst);
			}
    	};
    }
	/**
	 * Computes all loops in this control-flow graph.
	 * <p>
	 * It does so in 3 standard steps: <br>
	 * <ul>
	 * <li> Infer back-edges.
	 * <li> Infer loop bodies, one per back-edge.
	 * <li> Merge loop bodies having the same loop header.
	 * </ul>
	 * 
	 * @return	Map from each loop header node in this control-flow
	 * 			graph to the set of all nodes in the body of that
	 * 			loop (including the loop header node).
	 */
    public Map<Inst, Set<Inst>> loopRemovalHelper() {
    	Set<Pair<Inst, Inst>> backEdges = getBackEdges();
    	Map<Inst, Set<Inst>> headToBody =
    		new HashMap<Inst, Set<Inst>>();
		for (Pair<Inst, Inst> p : backEdges) {
			Inst tail = p.val0;
			Inst head = p.val1;
			// tail->head is a back edge
			Set<Inst> body = headToBody.get(head);
			if (body == null) {
				body = new HashSet<Inst>();
				headToBody.put(head, body);
				body.add(head);
			}
			Stack<Inst> working = new Stack<Inst>();
			working.push(tail);
			while (!working.isEmpty()) {
				Inst v = working.pop();
				if (!body.contains(v)) {
					body.add(v);
					for (Inst u : getPreds(v))
						working.push(u);
				}
			}
		}
		return headToBody;
	}
	private static List<AcqLockInst> emptyAcqLockInstList =
		Collections.emptyList();
	/**
	 * Computes all synchronized blocks in this control-flow graph.
	 * 
	 * @return	Map from each monitorenter node in this
	 * 			control-flow graph to the set of all nodes in the
	 * 			body of the corresponding synchronized block
	 * 			(including the monitorenter node itself and each
	 * 			monitorexit node corresponding to it).
	 */
	public Map<Inst, Set<Inst>> syncRemovalHelper() {
		Map<Inst, Set<Inst>> headToBody =
			new HashMap<Inst, Set<Inst>>();
		Set<Inst> visited = new HashSet<Inst>(numNodes());
		Stack<Pair<Inst, List<AcqLockInst>>> worklist =
			new Stack<Pair<Inst, List<AcqLockInst>>>();
		worklist.push(new Pair<Inst, List<AcqLockInst>>(
			head, emptyAcqLockInstList));
		while (!worklist.isEmpty()) {
			Pair<Inst, List<AcqLockInst>> pair =
				worklist.pop();
			Inst inst = pair.val0;
			List<AcqLockInst> iLocks = pair.val1;
			for (AcqLockInst lockInst : iLocks) {
				Set<Inst> body = headToBody.get(lockInst);
				body.add(inst);
			}
			List<AcqLockInst> oLocks;
			if (inst instanceof AcqLockInst) {
				Set<Inst> body = new HashSet<Inst>();
				body.add(inst);
				headToBody.put(inst, body);
				int n = iLocks.size();
				oLocks = new ArrayList<AcqLockInst>(n + 1);
				oLocks.addAll(iLocks);
				oLocks.add((AcqLockInst) inst);
			} else if (inst instanceof RelLockInst) {
				int n = iLocks.size();
				Assertions.Assert(n > 0);
				if (n == 1) {
					oLocks = emptyAcqLockInstList;
				} else {
					oLocks = new ArrayList<AcqLockInst>(n - 1);
					for (int i = 0; i < n - 1; i++)
						oLocks.add(iLocks.get(i));
				}
			} else {
				oLocks = iLocks;
			}
			for (Inst succ : getSuccs(inst)) {
				if (!visited.contains(succ)) {
					visited.add(succ);
					Pair<Inst, List<AcqLockInst>> pair2 =
						new Pair<Inst, List<AcqLockInst>>(succ, oLocks);
					worklist.push(pair2);
				}
			}
		}
		return headToBody;
	}
	/**
	 * Converts this control-flow graph into Static Single Assignment
	 * (SSA) form.
	 */
	public void convertToSSA() {
		int numVars = args.size() + tmps.size();
		// assign a unique 0-based index to each local var
		IndexMap<Var> varToIdxMap = new IndexMap<Var>(numVars);
		// map each local var's index to its current 'version'
		// treat each arg as an assignment
	    int[] varToNumVrsns = new int[numVars];
	    Map<Pair<Inst, Var>, Var> ssaMap =
	    	new HashMap<Pair<Inst, Var>, Var>();
	    ILivenessAnalysis liveVarsAnalysis = getLiveVars();
	    Set<Var> liveVars = liveVarsAnalysis.getOutgoingLiveVars(head);
		for (int idx = 0; idx < args.size(); idx++) {
			Var var = args.get(idx);
			varToIdxMap.set(var);
			varToNumVrsns[idx]++;
			if (liveVars.contains(var))
				ssaMap.put(new Pair<Inst, Var>(head, var), var);
		}
		for (Var var : tmps)
			varToIdxMap.set(var);

		List<Inst> insts = getNodesInRPO();

		InstUseVarReplacer instUseVarReplacer = new InstUseVarReplacer();
		InstDefVarReplacer instDefVarReplacer = new InstDefVarReplacer();
		for (int i = 1; i < insts.size(); i++) {
			Inst inst = insts.get(i);
			List<Var> useVars = Arrays.asList(inst.getUses());
			List<Var> defVars = Arrays.asList(inst.getDefs());
			liveVars = liveVarsAnalysis.getIncomingLiveVars(inst);
			Set<Inst> preds = getPreds(inst);
			List<Inst> phiInsts = new ArrayList<Inst>();
			for (int idx = 0; idx < numVars; idx++) {
				Var var = varToIdxMap.get(idx);
				Var newVar = null;
				if (liveVars.contains(var)) {
					Set<Var> vars = new HashSet<Var>();
					for (Inst inst2 : preds) {
						Var var2 = ssaMap.get(new Pair<Inst, Var>(inst2, var));
						Assertions.Assert(var2 != null,
							"inst: " + inst + " var: " + var + " inst2: " + inst2);
						vars.add(var2);
					}
					int numPredVars = vars.size();
					if (numPredVars > 1) {
						newVar = getNewVar(var, varToNumVrsns[idx]++);
						PhiExpAsgnInst phiInst = new PhiExpAsgnInst(newVar, vars,
							inst.getLineNum());
						phiInst.setCFG(this);
						phiInsts.add(phiInst);
					} else if (numPredVars == 1)
						newVar = vars.iterator().next();
					else 
						newVar = var;
					if (newVar != var && useVars.contains(var)) {
						instUseVarReplacer.useVar = var;
						instUseVarReplacer.newVar = newVar;
						inst.accept(instUseVarReplacer);
					}
				}
				if (defVars.contains(var)) {
					Var newVar2 = getNewVar(var, varToNumVrsns[idx]++);
					if (newVar2 != var) {
						instDefVarReplacer.defVar = var;
						instDefVarReplacer.newVar = newVar2;
						inst.accept(instDefVarReplacer);
					}
					ssaMap.put(new Pair<Inst, Var>(inst, var), newVar2);
				} else if (newVar != null) {
					ssaMap.put(new Pair<Inst, Var>(inst, var), newVar);
				}
			}
			int numPhiInsts = phiInsts.size();
			if (numPhiInsts > 0) {
				Set<Inst> predsCopy = new HashSet<Inst>(preds);
				Inst firstPhiInst = phiInsts.get(0);
				insertNode(firstPhiInst);
				for (Inst pred : predsCopy) {
					removeEdge(pred, inst);
					insertEdge(pred, firstPhiInst);
				}
				Inst currPhiInst = firstPhiInst;
				for (int j = 1; j < numPhiInsts; j++) {
					Inst tmpPhiInst = currPhiInst;
					currPhiInst = phiInsts.get(j);
					insertNode(currPhiInst);
					insertEdge(tmpPhiInst, currPhiInst);
				}
				insertEdge(currPhiInst, inst);
			}
		}
	}
	private Var getNewVar(Var var, int vrsn) {
		Var newVar;
		if (vrsn > 0) {
			newVar = new Var(var.getName() + "_" + vrsn, var.getType());
			newVar.setCFG(this);
			tmps.add(newVar);
		} else
			newVar = var;
		return newVar;
	}
	private class InstUseVarReplacer implements InstVisitor {
		private Var useVar;
		private Var newVar;
		public void visit(SkipInst inst) { }
		public void visit(NilValAsgnInst inst) { }
		public void visit(StrValAsgnInst inst) { }
		public void visit(ClsVarAsgnInst inst) {
			Assertions.Assert(inst.getBase() == useVar);
			inst.setBase(newVar);
		}
		public void visit(ObjValAsgnInst inst) { }
		public void visit(ObjVarAsgnInst inst) {
			Assertions.Assert(inst.getRvar() == useVar);
			inst.setRvar(newVar);
		}
		public void visit(AryElemRefInst inst) {
			Assertions.Assert(inst.getBase() == useVar ||
				(inst.isWr() && inst.getVar() == useVar));
			if (inst.getBase() == useVar)
				inst.setBase(newVar);
			if (inst.isWr() && inst.getVar() == useVar)
				inst.setVar(newVar);
		}
		public void visit(InstFldRefInst inst) {
			Assertions.Assert(inst.getBase() == useVar ||
				(inst.isWr() && inst.getVar() == useVar));
			if (inst.getBase() == useVar)
				inst.setBase(newVar);
			if (inst.isWr() && inst.getVar() == useVar)
				inst.setVar(newVar);
		}
		public void visit(StatFldRefInst inst) {
			Assertions.Assert(inst.getVar() == useVar);
			inst.setVar(newVar);
		}
		public void visit(InvkInst inst) {
			List<Var> invkArgs = inst.getArgs();
			Assertions.Assert(invkArgs.contains(useVar));
			for (int pos = 0; pos < invkArgs.size(); pos++) {
				Var var = invkArgs.get(pos);
				if (var == useVar) {
					invkArgs.remove(pos);
					invkArgs.add(pos, newVar);
				}
			}
		}
		public void visit(AcqLockInst inst) {
			throw new RuntimeException();
		}
		public void visit(RelLockInst inst) {
			throw new RuntimeException();
		}
		public void visit(PhiExpAsgnInst inst) {
			throw new RuntimeException();
		}
		public void visit(HeadInst inst) {
			throw new RuntimeException();
		}
		public void visit(TailInst inst) {
			int pos = rets.indexOf(useVar);
			Assertions.Assert(pos != -1);
			rets.remove(pos);
			rets.add(pos, newVar);
		}
	};
	private class InstDefVarReplacer implements InstVisitor {
		private Var defVar;
		private Var newVar;
		public void visit(SkipInst inst) { }
		public void visit(NilValAsgnInst inst) {
			Assertions.Assert(inst.getVar() == defVar);
			inst.setVar(newVar);
		}
		public void visit(StrValAsgnInst inst) {
			Assertions.Assert(inst.getVar() == defVar);
			inst.setVar(newVar);
		}
		public void visit(ClsVarAsgnInst inst) {
			Assertions.Assert(inst.getVar() == defVar);
			inst.setVar(newVar);
		}
		public void visit(ObjValAsgnInst inst) {
			Assertions.Assert(inst.getVar() == defVar);
			inst.setVar(newVar);
		}
		public void visit(ObjVarAsgnInst inst) {
			Assertions.Assert(inst.getLvar() == defVar);
			inst.setLvar(newVar);
		}
		public void visit(AryElemRefInst inst) {
			Assertions.Assert(inst.getVar() == defVar);
			inst.setVar(newVar);
		}
		public void visit(InstFldRefInst inst) {
			Assertions.Assert(inst.getVar() == defVar);
			inst.setVar(newVar);
		}
		public void visit(StatFldRefInst inst) {
			Assertions.Assert(inst.getVar() == defVar);
			inst.setVar(newVar);
		}
		public void visit(InvkInst inst) {
			List<Var> invkRets = inst.getRets();
			Assertions.Assert(invkRets.contains(defVar));
			for (int pos = 0; pos < invkRets.size(); pos++) {
				Var var = invkRets.get(pos);
				if (var == defVar) {
					invkRets.remove(pos);
					invkRets.add(pos, newVar);
				}
			}
		}
		public void visit(AcqLockInst inst) {
			throw new RuntimeException();
		}
		public void visit(RelLockInst inst) {
			throw new RuntimeException();
		}
		public void visit(PhiExpAsgnInst inst) {
			throw new RuntimeException();
		}
		public void visit(HeadInst inst) {
			throw new RuntimeException();
		}
		public void visit(TailInst inst) {
			throw new RuntimeException();
		}
	};
	public String toString() {
		String s = "args: " + args + " tmps: " + tmps +
			" rets: " + rets + " sync: " + sync; 
		List<Inst> insts = getNodesInRPO();
		int numInsts = insts.size();
		for (int i = 0; i < numInsts; i++) {
			Inst inst = insts.get(i);
   		 	s += "\n" + i + ": " + inst + " -> {";
			Iterator<Inst> it = getSuccs(inst).iterator();
			if (it.hasNext()) {
				while (true) {
					Inst succ = it.next();
					s += insts.indexOf(succ);
					if (!it.hasNext())
						break;
					s += ",";
				}
			}
			s += "}";
		}
		return s;
	}
}
