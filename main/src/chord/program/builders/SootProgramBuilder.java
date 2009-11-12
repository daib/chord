/*
 * Copyright (c) 2006-07, The Trustees of Stanford University.  All
 * rights reserved.
 * Licensed under the terms of the GNU GPL; see COPYING for details.
 */
package chord.program.builders;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import chord.util.ArraySet;
import chord.program.CFG;
import chord.program.Program;
import chord.program.Field;
import chord.program.Method;
import chord.program.Type;
import chord.program.TypeKind;
import chord.program.Var;
import chord.program.insts.AcqLockInst;
import chord.program.insts.AryElemRefInst;
import chord.program.insts.ClsVarAsgnInst;
import chord.program.insts.HeadInst;
import chord.program.insts.Inst;
import chord.program.insts.InstFldRefInst;
import chord.program.insts.InvkInst;
import chord.program.insts.InvkKind;
import chord.program.insts.NilValAsgnInst;
import chord.program.insts.ObjValAsgnInst;
import chord.program.insts.ObjVarAsgnInst;
import chord.program.insts.RelLockInst;
import chord.program.insts.SkipInst;
import chord.program.insts.StatFldRefInst;
import chord.program.insts.StrValAsgnInst;
import chord.program.insts.TailInst;
import chord.util.Assertions;

import soot.jimple.toolkits.typing.fast.BottomType;
import soot.NullType;
import soot.ArrayType;
import soot.BooleanType;
import soot.ByteType;
import soot.CharType;
import soot.DoubleType;
import soot.FloatType;
import soot.G;
import soot.IntType;
import soot.Local;
import soot.LongType;
import soot.PackManager;
import soot.PrimType;
import soot.RefLikeType;
import soot.RefType;
import soot.Scene;
import soot.ShortType;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.VoidType;
import soot.jimple.AnyNewExpr;
import soot.jimple.ArrayRef;
import soot.jimple.AssignStmt;
import soot.jimple.CastExpr;
import soot.jimple.ClassConstant;
import soot.jimple.EnterMonitorStmt;
import soot.jimple.ExitMonitorStmt;
import soot.jimple.FieldRef;
import soot.jimple.IdentityStmt;
import soot.jimple.IfStmt;
import soot.jimple.GotoStmt;
import soot.jimple.InterfaceInvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.NullConstant;
import soot.jimple.StringConstant;
import soot.jimple.TableSwitchStmt;
import soot.jimple.LookupSwitchStmt;
import soot.jimple.InstanceFieldRef;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.ReturnStmt;
import soot.jimple.ReturnVoidStmt;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.ThrowStmt;
import soot.jimple.VirtualInvokeExpr;
import soot.options.Options;
import soot.tagkit.Host;
import soot.tagkit.SourceFileTag;
import soot.tagkit.LineNumberTag;
import soot.tagkit.SourceLineNumberTag;
import soot.tagkit.SourceLnPosTag;
import soot.toolkits.graph.BriefUnitGraph;
import soot.util.Chain;

/**
 * A program representation builder based on Soot.
 * <p>
 * Each class initializer method of the form:
 * <pre>
 * class T {
 *     static void <clinit>() {
 *         ...
 *     }
 * }
 * </pre>
 * is transformed into:
 * <pre>
 * class T {
 *     static void <clinit>() {
 *         java.lang.Class v;
 *         v = new java.lang.Class;
 *         T.class = v;
 *         ...
 *     }
 * }
 * </pre>
 * <p>
 * Each static synchronized method of the form:
 * <pre>
 * class T {
 *     static synchronized void foo() {
 *         ...
 *     }
 * }
 * </pre>
 * is transformed into:
 * <pre>
 * class T {
 *     static void foo() {
 *         java.lang.Class v;
 *         v = T.class;
 *         synchronized (v) {
 *             ...
 *         }
 *     }
 * }
 * </pre>
 * <p>
 * Method void start() in class java.lang.Thread:
 * <pre>
 * class java.lang.Thread {
 *     public native void start();
 * }
 * </pre>
 * is transformed into:
 * <pre>
 * class java.lang.Thread {
 *     public void start() {
 *         this.run();
 *     }
 * }
 * </pre>
 *
 * @author Mayur Naik (mhn@cs.stanford.edu)
 */
public class SootProgramBuilder implements IProgramBuilder {
	public Program build(String mainClassName,
			String classPathName, String srcPathName, 
			String annotIncludeFileName,
			String annotExcludeFileName,
			String ignoreMethodsBySignFileName,
			String ignoreMethodsByCtnrFileName) {
		try {
			signToTypeAnnotMap = new HashMap<String, String>();
			if (annotIncludeFileName != null) {
				BufferedReader reader = new BufferedReader(
					new FileReader(annotIncludeFileName));
				String line;
				while ((line = reader.readLine()) != null) {
					String[] words = line.split(" ");
					Assertions.Assert(words.length == 2);
					String sign = words[0];
					String type = words[1];
					Assertions.Assert(!signToTypeAnnotMap.containsKey(sign));
					signToTypeAnnotMap.put(sign, type);
				} 
				reader.close();
			}
			excludedAnnotSigns = new HashSet<String>();
			if (annotExcludeFileName != null) {
				BufferedReader reader = new BufferedReader(
					new FileReader(annotExcludeFileName));
				String line;
				while ((line = reader.readLine()) != null) {
					excludedAnnotSigns.add(line);
				}
				reader.close();
			}
			if (ignoreMethodsBySignFileName != null) {
				ignoreMethodsBySign = new HashSet<String>();
				BufferedReader reader = new BufferedReader(
					new FileReader(ignoreMethodsBySignFileName));
				String line;
				while ((line = reader.readLine()) != null) {
					ignoreMethodsBySign.add(line);
				} 
				reader.close();
			} else
				ignoreMethodsBySign = Collections.emptySet();
			if (ignoreMethodsByCtnrFileName != null) {
				ignoreMethodsByCtnr = new HashSet<String>();
				BufferedReader reader = new BufferedReader(
					new FileReader(ignoreMethodsByCtnrFileName));
				String line;
				while ((line = reader.readLine()) != null) {
					ignoreMethodsByCtnr.add(line);
				} 
				reader.close();
			} else
				ignoreMethodsByCtnr = Collections.emptySet();
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}

		G.reset();
		List<String> args = new ArrayList<String>();
		// args.add("-verbose");
		args.add("-app");
		args.add("-include-all");
		args.add("-keep-line-number");
		args.add("-d");
		args.add(".");
		// do not output any files (e.g. .jimple)
		args.add("-f");
		args.add("n");

		args.add("-w");

		// use cha for call graph construction
		args.add("-p");
		args.add("cg.cha");
		args.add("on");

		// keep original local variable names
/*
		args.add("-p");
		args.add("jb");
		args.add("use-original-names:true");
*/

		args.add("-cp");
		args.add(classPathName);
        args.add("-main-class");
		args.add(mainClassName);
		for (String type : signToTypeAnnotMap.values())
			args.add(type);
		args.add(mainClassName);

		String[] argsArray = new String[args.size()];
		args.toArray(argsArray);

        System.out.println("Soot args:");
        for (String s : argsArray)
            System.out.println(s);

		Options.v().parse(argsArray);
		Scene.v().loadNecessaryClasses();
		PackManager.v().runPacks();

		Scene.v().getOrMakeFastHierarchy();

    	oldToNewTMap = new HashMap<soot.Type, Type>();
		types = new ArrayList<Type>();
    	oldToNewMMap = new HashMap<SootMethod, Method>();
		oldToNewFMap = new HashMap<SootField, Field>();
		oldToNewVMap = new HashMap<Local, Var>();
		
		initTypes();
		typeOfJavaLangObj = getType("java.lang.Object");
    	typeOfJavaLangCls = getType("java.lang.Class");
		Type mainType = getType(mainClassName);
		if (mainType == null) {
			Assertions.Assert(false,
				"Class '" + mainClassName + "' not found.");
		}
		fillSuptypes();
		fillFields();
		fillMethods();
		for (Map.Entry<SootMethod, Method> e :
				oldToNewMMap.entrySet()) {
        	oldM = e.getKey();
        	newM = e.getValue();
        	processMeth();
		}
		String sign = "main(java.lang.String[])";
		Method method = mainType.getMethod(sign);
		if (method == null) {
			Assertions.Assert(false,
				"Cannot find method with signature '" + sign +
				"' in class '" + mainClassName + "'.");
		}
		return new Program(types, mainClassName, srcPathName);
	}

	private static final List<Type> emptyTypeList =
		Collections.emptyList();
	private static final List<Method> emptyMethodList =
		Collections.emptyList();
	private static final Set<Inst> emptyInstSet =
		Collections.emptySet();
	private static final List<Var> emptyVarList =
		Collections.emptyList();

	private Map<String, String> signToTypeAnnotMap;
	private Set<String> excludedAnnotSigns;
	private Set<String> ignoreMethodsBySign;
	private Set<String> ignoreMethodsByCtnr;

	/**
	 * List of each type in its representation in Chord.
	 */
	private List<Type> types;
	/**
	 * Map from the representation of each type
	 * in Soot to that in Chord.
	 */
	private Map<soot.Type, Type> oldToNewTMap;
	/**
	 * Map from the representation of each field
     * in Soot to that in Chord.
	 */
	private Map<SootField, Field> oldToNewFMap;
	/**
	 * Map from the representation of each method
     * in Soot to that in Chord.
	 */
	private Map<SootMethod, Method> oldToNewMMap;
	/**
	 * Map from the representation of each local variable
	 * in Soot to that in Chord.
	 */
	private Map<Local, Var> oldToNewVMap;

	private Type typeOfJavaLangObj;
	private Type typeOfJavaLangCls;

	private Type getType(String name) {
		for (Type type : types) {
			if (type.getName().equals(name))
				return type;
		}
		Assertions.Assert(false);
		return null;
	}

	private Type getType(soot.Type oldT) {
		Type newT = oldToNewTMap.get(oldT);
		Assertions.Assert(newT != null);
		return newT;
	}

	private Field getField(SootField oldF) {
		Field newF = oldToNewFMap.get(oldF);
		Assertions.Assert(newF != null);
		return newF;
	}

	private Method getMethod(SootMethod oldM) {
		Method newM = oldToNewMMap.get(oldM);
		Assertions.Assert(newM != null);
		return newM;
	}

	SootMethod oldM;
	Method newM;
	Map<Stmt, Inst> stmtToFstInst;
	Map<Stmt, Inst> stmtToLstInst;
	Map<Inst, Set<Inst>> instToSuccs;
	List<Var> args;
	List<Var> tmps;
	List<Var> rets;
	HeadInst fstInstOfNewHead;
	Inst lstInstOfNewHead;
	Inst fstInstOfNewTail;
	TailInst lstInstOfNewTail;
	BriefUnitGraph oldCFG;
    private boolean retnReached; 
    Set<Stmt> visited;

	private void processMeth() {
		String subSign = oldM.getSubSignature();
		String cName = oldM.getDeclaringClass().getName();
		if (ignoreMethodsBySign.contains(subSign) ||
			ignoreMethodsByCtnr.contains(cName)) {
			System.out.println("IGNORING: " + oldM);
			return;
		}
		if (newM == forkMethod) {
			buildForkMethod();
			return;
		}
		if (newM == pa1Method) {
			buildPAMethod(DO_PA1_SIGN);
			return;
		}
		if (newM == pa2Method) {
			buildPAMethod(DO_PA2_SIGN);
			return;
		}
		if (newM == pa3Method) {
			buildPAMethod(DO_PA3_SIGN);
			return;
		}
		if (newM == pa4Method) {
			buildPAMethod(DO_PA4_SIGN);
			return;
		}
		if (!oldM.hasActiveBody())
			return;

        retnReached = false;
		soot.Body oldBody = oldM.getActiveBody();
		oldCFG = new BriefUnitGraph(oldBody);

/*
		System.out.println("Processing: " + oldM);
		for (Unit unit : oldCFG.getHeads())
			System.out.println("\tHead:" + unit);
		for (Unit unit : oldCFG.getTails())
			System.out.println("\tTail:" + unit);
		for (SootClass ex : oldM.getExceptions())
			System.out.println("\tThrows: " + ex);
		Chain<Trap> traps = oldBody.getTraps();
		for (Trap trap : traps) {
			System.out.println("\tTrap: " + trap);
			System.out.println("\t\tEx: " + trap.getException());
		}
*/
			
		Chain stmts = oldBody.getUnits();
		Stmt root = (Stmt) stmts.getFirst();

		Assertions.Assert(root != null);
		instToSuccs = new HashMap<Inst, Set<Inst>>();

		lstInstOfNewTail = new TailInst(0);
		instToSuccs.put(lstInstOfNewTail, emptyInstSet);
		// fstInstOfNewTail will be computed shortly

		int rootLineNum = getLineNum(oldM);
		Set<Local> vars = new ArraySet<Local>();
		for (Iterator vit = oldBody.getLocals().iterator();
				vit.hasNext();) {
			Local v = (Local) vit.next();
			if (validType(v.getType()))
				vars.add(v);
		}
		args = new ArrayList<Var>();
		tmps = new ArrayList<Var>();
		Var sync;
		if (oldM.isStatic()) {
			sync = null;
			if (oldM.isSynchronized()) {
				Var V = makeVar(typeOfJavaLangCls);
				tmps.add(V);
				Field F = newM.getCtnrType().getField("class");
				Assertions.Assert(F != null);
				fstInstOfNewHead = new HeadInst(rootLineNum);
				Inst inst1 = new StatFldRefInst(V, F, false, rootLineNum);
				lstInstOfNewHead = new AcqLockInst(V, rootLineNum);
				Set<Inst> succs1 = new ArraySet<Inst>(1);
				succs1.add(inst1);
				instToSuccs.put(fstInstOfNewHead, succs1);
				Set<Inst> succs2 = new ArraySet<Inst>(1);
				succs2.add(lstInstOfNewHead);
				instToSuccs.put(inst1, succs2);
				fstInstOfNewTail = new RelLockInst(0);
				Set<Inst> succs3 = new ArraySet<Inst>(1);
				succs3.add(lstInstOfNewTail);
				instToSuccs.put(fstInstOfNewTail, succs3);
			} else if (oldM.getSubSignature().equals("void <clinit>()")) {
				Var V = makeVar(typeOfJavaLangCls);
				tmps.add(V);
				Field F = newM.getCtnrType().getField("class");
				Assertions.Assert(F != null);
				fstInstOfNewHead = new HeadInst(rootLineNum);
				Inst inst1 = new ObjValAsgnInst(V,
					typeOfJavaLangCls, rootLineNum);
				lstInstOfNewHead = new StatFldRefInst(V, F,
					true, rootLineNum);
				Set<Inst> succs1 = new ArraySet<Inst>(1);
				succs1.add(inst1);
				instToSuccs.put(fstInstOfNewHead, succs1);
				Set<Inst> succs2 = new ArraySet<Inst>(1);
				succs2.add(lstInstOfNewHead);
				instToSuccs.put(inst1, succs2);
				fstInstOfNewTail = lstInstOfNewTail;
			} else {
				fstInstOfNewHead = new HeadInst(rootLineNum);
				lstInstOfNewHead = fstInstOfNewHead;
				fstInstOfNewTail = lstInstOfNewTail;
			}
		} else {
			Local v = oldBody.getThisLocal();
			Assertions.Assert(validType(v.getType()));
			vars.remove(v);
			Var V = makeVar(v, newM);
			args.add(V);
			sync = (oldM.isSynchronized()) ? V : null;
			fstInstOfNewHead = new HeadInst(rootLineNum);
			lstInstOfNewHead = fstInstOfNewHead;
			fstInstOfNewTail = lstInstOfNewTail;
		}

		int numArgs = oldM.getParameterCount();
		for (int i = 0; i < numArgs; i++) {
			Local v = oldBody.getParameterLocal(i);
			if (validType(v.getType())) {
				vars.remove(v);
				args.add(makeVar(v, newM));
			}
		}

		for (Local v : vars)
			tmps.add(makeVar(v, newM));

		soot.Type retOldT = oldM.getReturnType();
		if (validType(retOldT)) {
		    rets = new ArrayList<Var>(1);
			Type retNewT = getType(retOldT);
			Var retNewV = makeVar(retNewT);
			tmps.add(retNewV);
			rets.add(retNewV);
		} else
			rets = emptyVarList;

		stmtToFstInst = new HashMap<Stmt, Inst>();
		stmtToLstInst = new HashMap<Stmt, Inst>();
		visited = new HashSet<Stmt>();
		visited.add(root);
        processStmt(root, 0);

		for (Map.Entry<Stmt, Inst> f : stmtToLstInst.entrySet()) {
			Stmt s = f.getKey();
			Inst S = f.getValue();
			List<Unit> nexts = oldCFG.getSuccsOf(s);
			Set<Inst> succs = new ArraySet<Inst>(nexts.size());
			instToSuccs.put(S, succs);
			for (Unit t : nexts) {
				Inst T = stmtToFstInst.get(t);
				Assertions.Assert(T != null);
				succs.add(T);
			}
		}

        if (!retnReached) {
            instToSuccs.remove(fstInstOfNewTail);
            if (lstInstOfNewTail != fstInstOfNewTail)
                instToSuccs.remove(lstInstOfNewTail);
            lstInstOfNewTail = null;
        }

		Inst fstInstOfOldBody = stmtToFstInst.get(root);
		Assertions.Assert(fstInstOfOldBody != null);
		Set<Inst> succs = new ArraySet<Inst>(1);
		succs.add(fstInstOfOldBody);
		instToSuccs.put(lstInstOfNewHead, succs);
		CFG cfg = new CFG(fstInstOfNewHead, lstInstOfNewTail,
			args, tmps, rets, sync, null, instToSuccs);
		for (Var var : args)
			var.setCFG(cfg);
		for (Var var : tmps)
			var.setCFG(cfg);
		for (Inst inst : cfg.getNodes())
			inst.setCFG(cfg);
		newM.setCFG(cfg);
		cfg.setCtnrMethod(newM);
	}

    private void processStmt(Stmt stmt, int numLocks) {
        if (stmt instanceof AssignStmt && !stmt.containsInvokeExpr()) {
            processAsgnStmt((AssignStmt) stmt);
        } else if (stmt instanceof InvokeStmt || stmt instanceof AssignStmt) {
            processInvkStmt(stmt);
        } else if (stmt instanceof IdentityStmt) {
            Inst inst = new SkipInst(getLineNum(stmt));
            stmtToFstInst.put(stmt, inst);
            stmtToLstInst.put(stmt, inst);
        } else if (stmt instanceof IfStmt) {
            Inst inst = new SkipInst(getLineNum(stmt));
            stmtToFstInst.put(stmt, inst);
            stmtToLstInst.put(stmt, inst);
        } else if (stmt instanceof GotoStmt) {
			Inst inst = new SkipInst(getLineNum(stmt));
			stmtToFstInst.put(stmt, inst);
			stmtToLstInst.put(stmt, inst);
        } else if (stmt instanceof ThrowStmt) {
            processThrowStmt(stmt, numLocks);
            retnReached = true;
            return;
        } else if (stmt instanceof ReturnStmt) {
            processRetnStmt(stmt);
            retnReached = true;
            Assertions.Assert(numLocks == 0);
            return;
        } else if (stmt instanceof ReturnVoidStmt) {
            stmtToFstInst.put(stmt, fstInstOfNewTail);
            retnReached = true;
            Assertions.Assert(numLocks == 0);
            return;
        } else if (stmt instanceof ExitMonitorStmt) {
            Inst inst = new RelLockInst(getLineNum(stmt));
            stmtToFstInst.put(stmt, inst);
            stmtToLstInst.put(stmt, inst);
            numLocks--;
        } else if (stmt instanceof EnterMonitorStmt) {
            Value v = ((EnterMonitorStmt) stmt).getOp();
            Assertions.Assert(validValue(v));
            Var V = oldToNewVMap.get(v);
            Inst inst = new AcqLockInst(V, getLineNum(stmt));
			stmtToFstInst.put(stmt, inst);
			stmtToLstInst.put(stmt, inst);
            numLocks++;
		} else {
			Assertions.Assert(stmt instanceof TableSwitchStmt ||
				stmt instanceof LookupSwitchStmt);
			Inst inst = new SkipInst(getLineNum(stmt));
			stmtToFstInst.put(stmt, inst);
			stmtToLstInst.put(stmt, inst);
		}
		for (Iterator it = oldCFG.getSuccsOf(stmt).iterator();
				it.hasNext();) {
			Stmt s = (Stmt) it.next();
			if (visited.add(s))
				processStmt(s, numLocks);
		}
	}
	
	private void processRetnStmt(Stmt stmt) {
		// Transform a ReturnStmt of the form "return e"
		// where e is either of the following:
		// 1. a variable of reference type.
		// 2. a constant of reference type, i.e., either
		// null, a string constant, or a class constant.
		// 3. an expression of primitive type.
		Value oldV = ((ReturnStmt) stmt).getOp();
		Inst lstInst = fstInstOfNewTail;
		Inst fstInst;
		if (validType(oldV.getType())) {
			// handle cases 1 and 2
			int lineNum = getLineNum(stmt);
			Assertions.Assert(rets.size() == 1);
			Var newR = rets.get(0);
			if (oldV instanceof Local) {
				Var newV = oldToNewVMap.get(oldV);
				Assertions.Assert(newV != null);
				fstInst = new ObjVarAsgnInst(newR, newV, lineNum);
			} else {
				fstInst = processValAsgnInst(newR, oldV, lineNum);
			}
			Set<Inst> succs = new ArraySet<Inst>(1);
			succs.add(lstInst);
			instToSuccs.put(fstInst, succs);
		} else {
			// handle case 3
			fstInst = lstInst;
		}
		stmtToFstInst.put(stmt, fstInst);
	}

	private void processThrowStmt(Stmt stmt, int numLocks) {
		Inst fstInst = null;
        Inst prevInst = null;
        int lineNum = getLineNum(stmt);
        for (int i = 0; i < numLocks; i++) {
            Inst inst = new RelLockInst(lineNum);
            if (fstInst == null) {
                fstInst = inst;
            } else {
                Set<Inst> succs = new ArraySet<Inst>(1);
                succs.add(inst);
                instToSuccs.put(prevInst, succs);
            }
            prevInst = inst;
        }
        Inst nextInst;
		if (rets.size() == 1) {
			Var newV = rets.get(0);
            nextInst = new NilValAsgnInst(newV, lineNum);
            Set<Inst> succs = new ArraySet<Inst>(1);
			succs.add(fstInstOfNewTail);
			instToSuccs.put(nextInst, succs);
		} else {
            Assertions.Assert(rets.size() == 0);
            nextInst = fstInstOfNewTail;
		}
        if (fstInst == null)
		    fstInst = nextInst;
        else {
            // we have prevInst != null
            Set<Inst> succs = new ArraySet<Inst>(1);
            succs.add(nextInst);
            instToSuccs.put(prevInst, succs);
        }
		stmtToFstInst.put(stmt, fstInst);
	}
	
	private void processAsgnStmt(AssignStmt stmt) {
		Value oldL = stmt.getLeftOp ();
		Value oldR = stmt.getRightOp();
		boolean asgnIsRef = validType(oldL.getType());
		// Assignment is of the form l := b.f or l = b[i]
		if (oldR instanceof FieldRef || oldR instanceof ArrayRef) {
			int lineNum = getLineNum(stmt);
			Var newL;
			if (asgnIsRef) {
				Assertions.Assert(oldL instanceof Local);
				newL = oldToNewVMap.get(oldL);
				Assertions.Assert(newL != null);
			} else
				newL = null;
			Inst inst = (oldR instanceof FieldRef) ?
				processIorSFldRefInst((FieldRef) oldR,
					newL, false, lineNum) :
				processAryElemRefInst((ArrayRef) oldR,
					newL, false, lineNum);
				stmtToFstInst.put(stmt, inst);
				stmtToLstInst.put(stmt, inst);
				return;
		}
		// Assignment is of the form b.f = r or b[i] = r
		if (oldL instanceof FieldRef || oldL instanceof ArrayRef) {
			int lineNum = getLineNum(stmt);
			Var newL;
			Inst fstInst;
			if (!asgnIsRef) {
				newL = null;
				fstInst = null;
			} else if (oldR instanceof Local) {
				newL = oldToNewVMap.get(oldR);
				Assertions.Assert(newL != null);
				fstInst = null;
			} else {
				soot.Type oldT = oldL.getType();
				Type newT = getType(oldT);
				newL = makeVar(newT);
				tmps.add(newL);
				fstInst = processValAsgnInst(newL, oldR, lineNum);
			}
			Inst lstInst = (oldL instanceof FieldRef) ?
				processIorSFldRefInst((FieldRef) oldL,
					newL, true, lineNum) :
				processAryElemRefInst((ArrayRef) oldL,
					newL, true, lineNum);
				if (fstInst == null)
					fstInst = lstInst;
				else {
					Set<Inst> succs = new ArraySet<Inst>(1);
					succs.add(lstInst);
					instToSuccs.put(fstInst, succs);
				}
				stmtToFstInst.put(stmt, fstInst);
				stmtToLstInst.put(stmt, lstInst);
				return;
		}

		Assertions.Assert(oldL instanceof Local);

		// Assignment is of the form v = new h
		if (oldR instanceof AnyNewExpr) {
			Var newL = oldToNewVMap.get(oldL);
			Assertions.Assert(newL != null);
			soot.Type oldT = oldR.getType();
			Type newT = getType(oldT);
			Inst inst = new ObjValAsgnInst(newL, newT, getLineNum(stmt));
			stmtToFstInst.put(stmt, inst);
			stmtToLstInst.put(stmt, inst);
			return;
		}

		if (!asgnIsRef) {
			Inst inst = new SkipInst(getLineNum(stmt));
			stmtToFstInst.put(stmt, inst);
			stmtToLstInst.put(stmt, inst);
			return;
		}

		// Assignment is of the form l = (T) r and l = r
		if (oldR instanceof CastExpr) {
			oldR = ((CastExpr) oldR).getOp();
		}

		Inst inst;
		Var newL = oldToNewVMap.get(oldL);
		Assertions.Assert(newL != null);
		if (oldR instanceof Local) {
			Var newR = oldToNewVMap.get(oldR);
			Assertions.Assert(newR != null);
			inst = new ObjVarAsgnInst(newL, newR, getLineNum(stmt));
		} else {
			inst = processValAsgnInst(newL, oldR, getLineNum(stmt));
		}
		stmtToFstInst.put(stmt, inst);
		stmtToLstInst.put(stmt, inst);
		return;
	}

	private void processInvkStmt(Stmt stmt) {
		InvokeExpr expr;
		Var newL;
		if (stmt instanceof AssignStmt) {
			// Assignment is of the form l = invoke ...
			AssignStmt asgnStmt = (AssignStmt) stmt;
			Value oldL = asgnStmt.getLeftOp ();
			Value oldR = asgnStmt.getRightOp();
			boolean asgnIsRef = validType(oldL.getType());
			Assertions.Assert(oldR instanceof InvokeExpr);
			if (asgnIsRef) {
				Assertions.Assert(oldL instanceof Local);
				newL = oldToNewVMap.get(oldL);
				Assertions.Assert(newL != null);
			} else
				newL = null;
			expr = (InvokeExpr) oldR;
		} else {
			newL = null;
			expr = ((InvokeStmt) stmt).getInvokeExpr();
		}
		int lineNum = getLineNum(stmt);
		SootMethod oldN = expr.getMethod();
		Assertions.Assert(oldN != null);

		InvkKind invkKind;
		int numParams;
		List<Var> invkArgs;
		String oldNClsName = oldN.getDeclaringClass().getName();
		String oldNSubSign = oldN.getSubSignature();
		if (expr instanceof InstanceInvokeExpr) {
			Value oldV = ((InstanceInvokeExpr) expr).getBase();
			Assertions.Assert(validValue(oldV));
			Var newV = oldToNewVMap.get(oldV);
			Assertions.Assert(newV != null);
			if (oldNClsName.equals("java.lang.Object") &&
					oldNSubSign.equals("java.lang.Class getClass()")) {
				Inst inst = (newL != null) ?
					new ClsVarAsgnInst(newL, newV, lineNum) :
					new SkipInst(lineNum);
				stmtToFstInst.put(stmt, inst);
				stmtToLstInst.put(stmt, inst);
				return;
			}
			if (oldNClsName.equals("java.lang.Class") &&
					oldNSubSign.equals("java.lang.Object newInstance()")) {
				String oldMsubSign = oldM.getSubSignature();
				String key = oldM.getDeclaringClass().getName() +
					"." + oldMsubSign.split(" ")[1];
				if (!excludedAnnotSigns.contains(key)) {
					String type = signToTypeAnnotMap.get(key);
					if (type == null) {
						System.out.println("WARNING: newInstance() method in body of " +
							oldM + " ignored.");
					} else {
                        System.out.println("FOUND: " + key + " " + type);
						SootClass oldC = Scene.v().getSootClass(type);
						soot.Type oldT = oldC.getType();
						SootMethod oldM = oldC.getMethod("void <init>()");
						Assertions.Assert(oldM != null);
    					Type newT = getType(oldT);
						if (newL == null) {
							newL = makeVar(newT);
							tmps.add(newL);
						}
						Inst fstInst = new ObjValAsgnInst(newL, newT, 0);
						Method tgtM = getMethod(oldM);
						List<Var> args = new ArrayList<Var>(1);
						args.add(newL);
						List<Var> rets = emptyVarList;
						Inst lstInst = new InvkInst(InvkKind.INVK_VIRTUAL,
							tgtM, args, rets, 0);
						stmtToFstInst.put(stmt, fstInst);
						stmtToLstInst.put(stmt, lstInst);
						Set<Inst> succs = new ArraySet<Inst>(1);
						succs.add(lstInst);
						instToSuccs.put(fstInst, succs);
						return;
					}
				}
			}
			if (expr instanceof VirtualInvokeExpr)
				invkKind = InvkKind.INVK_VIRTUAL;
			else if (expr instanceof SpecialInvokeExpr)
				invkKind = InvkKind.INVK_SPECIAL;
			else {
				Assertions.Assert(expr instanceof InterfaceInvokeExpr);
				invkKind = InvkKind.INVK_INTERFACE;
			}
			numParams = expr.getArgCount();
			invkArgs = new ArrayList<Var>(numParams + 1);
			invkArgs.add(newV);
		} else {
			Assertions.Assert(expr instanceof StaticInvokeExpr);
			SootClass oldClass = null;
/* XXX
 			if (oldNClsName.equals(JAVA_SEC_AC_CLS)) {
				if (oldNSubSign.equals(DO_PA1_SIGN)) {
					oldClass = Scene.v().getSootClass(JAVA_SEC_PA_CLS);
					Assertions.Assert(oldClass != null);
				} else if (oldNSubSign.equals(DO_PA2_SIGN)) {
					oldClass = Scene.v().getSootClass(JAVA_SEC_PEA_CLS);
					Assertions.Assert(oldClass != null);
				} else if (oldNSubSign.equals(DO_PA3_SIGN)) {
					oldClass = Scene.v().getSootClass(JAVA_SEC_PA_CLS);
					Assertions.Assert(oldClass != null);
				} else if (oldNSubSign.equals(DO_PA4_SIGN)) {
					oldClass = Scene.v().getSootClass(JAVA_SEC_PEA_CLS);
					Assertions.Assert(oldClass != null);
				}
			}
*/
			numParams = expr.getArgCount();
			if (oldClass != null) {
				invkKind = InvkKind.INVK_INTERFACE;
				oldN = oldClass.getMethod("java.lang.Object run()");
				Assertions.Assert(oldN != null);
				Assertions.Assert(numParams == 1 || numParams == 2);
				numParams = 1;
				invkArgs = new ArrayList<Var>(1);
			} else {
				invkKind = InvkKind.INVK_STATIC;
				invkArgs = (numParams == 0) ? emptyVarList :
					new ArrayList<Var>(numParams);
			}
		}

		Inst fstInst = null;
		Set<Inst> succs = null;
		for (int i = 0; i < numParams; i++) {
			Value oldV = expr.getArg(i);
			soot.Type oldT = oldV.getType();
			if (!validType(oldT))
				continue;
			if (oldV instanceof Local) {
				Var newV = oldToNewVMap.get(oldV);
				Assertions.Assert(newV != null);
				invkArgs.add(newV);
				continue;
			}
			Type newT = getType(oldT);
			Var newV = makeVar(newT);
			tmps.add(newV);
			Inst inst = processValAsgnInst(newV, oldV, lineNum);
			if (fstInst == null)
				fstInst = inst;
			else
				succs.add(inst);
			succs = new ArraySet<Inst>(1);
			instToSuccs.put(inst, succs);
			invkArgs.add(newV);	
		}

		List<Var> invkRets;
		if (newL != null) {
			invkRets = new ArrayList<Var>(1);
			invkRets.add(newL);
		} else 
			invkRets = emptyVarList;
		Inst lstInst = null;
		Method newN = getMethod(oldN);
		lstInst = new InvkInst(invkKind, newN, invkArgs, invkRets, lineNum);
		if (fstInst != null)
			succs.add(lstInst);
		else
			fstInst = lstInst;
		stmtToFstInst.put(stmt, fstInst);
		stmtToLstInst.put(stmt, lstInst);
		return;
	}
	
	private Inst processValAsgnInst(Var L, Value r, int lineNum) {
		if (r instanceof NullConstant) {
			return new NilValAsgnInst(L, lineNum);
		} 
		if (r instanceof StringConstant) {
			String S = ((StringConstant) r).value;
			return new StrValAsgnInst(L, S, lineNum);
		}
		Assertions.Assert(r instanceof ClassConstant);
		// TODO: remove foll. 6 lines and uncomment below
		// two commented lines after Soot bugfix
		String c = ((ClassConstant) r).value;
		if (c.contains("[") || c.contains(";"))
			c = typeName(((ClassConstant) r).value);
		else
			c = c.replace('/', '.');
		RefType t = Scene.v().getRefType(c);
		//String c = typeName(((ClassConstant) r).value);
		//RefType t = Scene.v().getRefType(c);
		Assertions.Assert(t != null);
		Type newT = getType(t);
		Field F = newT.getField("class");
		Assertions.Assert(F != null);
		return new StatFldRefInst(L, F, false, lineNum);
	}

	private Inst processAryElemRefInst(ArrayRef aref, Var V,
			boolean isWr, int lineNum) {
		Value oldB = aref.getBase();
		Var newB = oldToNewVMap.get(oldB);
		Assertions.Assert(newB != null);
		return new AryElemRefInst(V, newB, isWr, lineNum);
	}

	private Inst processIorSFldRefInst(FieldRef fref, Var newV,
			boolean isWr, int lineNum) {
		SootField oldF = fref.getField();
		Field newF = getField(oldF);
		if (fref instanceof InstanceFieldRef) {
			Value oldB = ((InstanceFieldRef) fref).getBase();
			Var newB = oldToNewVMap.get(oldB);
			Assertions.Assert(newB != null);
			return new InstFldRefInst(newV, newB, newF, isWr, lineNum);
		}
		Assertions.Assert(!oldF.getName().equals("class"));
		return new StatFldRefInst(newV, newF, isWr, lineNum);
	}

	private int count = 0;
	private Var makeVar(Type t) {
		Var var = new Var("dummy_" + t + "_" + count, t);
		count++;
		return var;
	}

	private Var makeVar(Local oldV, Method newM) {
		soot.Type oldT = oldV.getType();
		Type newT = getType(oldT);
		Var newV = new Var(oldV.getName(), newT);
		oldToNewVMap.put(oldV, newV);
		return newV;
	}

	private static boolean validValue(Value v) {
		return v instanceof Local && validType(v.getType());
	}
	private static boolean validType(soot.Type t) {
		return t instanceof RefLikeType || t instanceof BottomType;
	}
	
	/**
	 * Converts the bytecode name of a type
	 * (e.g., "[Ljava/lang/Object;") to its ordinary name
	 * (e.g., "java.lang.Object[]").
	 * @param bcName	Bytecode name of the type.
	 * @return			ordinary name of the type.
	 */
	private static String typeName(String bcName) {
		boolean isArray = false;
		int numDims = 0;
		soot.Type baseType;
		// Handle array case
		while(bcName.startsWith("[")) {
			isArray = true;
			numDims++;
			bcName = bcName.substring(1);
		}
		// Determine base type
		if (bcName.equals("B")) {
			baseType = ByteType.v();
		} else if (bcName.equals("C")) {
			baseType = CharType.v();
		} else if (bcName.equals("D")) {
			baseType = DoubleType.v();
		} else if (bcName.equals("F")) {
			baseType = FloatType.v();
		} else if (bcName.equals("I")) {
			baseType = IntType.v();
		} else if (bcName.equals("J")) {
			baseType = LongType.v();
		} else if (bcName.equals("V")) {
			baseType = VoidType.v();
		} else if (bcName.startsWith("L")) {
			if(!bcName.endsWith(";")) {
				throw new RuntimeException(
					"Class reference does not end with ;");
			}
			String className =
				bcName.substring(1, bcName.length() - 1);
			baseType = RefType.v(className.replace('/', '.'));
		} else if (bcName.equals("S")) {
			baseType = ShortType.v();
		} else if (bcName.equals("Z")) {
			baseType = BooleanType.v();
		} else
			throw new RuntimeException("Unknown type: " + bcName);
		return isArray ? "java.lang.Object" :
			baseType.toString();
	}
	
	private static int getLineNum(Host h) {
		if (h.hasTag("LineNumberTag")) {
			return ((LineNumberTag)
				h.getTag("LineNumberTag")).getLineNumber();
		}
		if (h.hasTag("SourceLineNumberTag")) {
			return ((SourceLineNumberTag)
				h.getTag("SourceLineNumberTag")).getLineNumber();
		}
		if (h.hasTag("SourceLnPosTag")) {
			return ((SourceLnPosTag)
				h.getTag("SourceLnPosTag")).startLn();
		}
		return 0;
	}

	private static String getFileName(SootClass c) {
		if (!c.hasTag("SourceFileTag"))
			return "unknown.java";
		String s = ((SourceFileTag) c.getTag("SourceFileTag")).
			getSourceFile();
        String pckgName = c.getPackageName();
		return pckgName.equals("") ? s :
			pckgName.replace('.', '/') + "/" + s;
	}

	// creates a chord type corresponding to each soot type
	// and populates oldToNewTMap and types.
	// the chord type's suptypes, fields, and methods are
	// not initialized
	private void initTypes() {
    	Iterator it = Scene.v().getTypeNumberer().iterator();
		while (it.hasNext()) {
			soot.Type oldT = (soot.Type) it.next();
			if (oldToNewTMap.containsKey(oldT))
				continue;
			String name = oldT.toString();
			TypeKind kind;
 			String fileName;
			if (oldT instanceof RefType) {
				SootClass oldC = ((RefType) oldT).getSootClass();
				if (oldC.isInterface())
					kind = TypeKind.INTERFACE_TYPE;
				else if (oldC.isAbstract())
					kind = TypeKind.ABSTRACT_CLASS_TYPE;
				else
					kind = TypeKind.CONCRETE_CLASS_TYPE;
				fileName = getFileName(oldC);
			} else if (oldT instanceof ArrayType) {
				kind = TypeKind.ARRAY_TYPE;
				fileName = null;
			} else if (oldT instanceof PrimType ||
					oldT instanceof VoidType) {
				kind = TypeKind.PRIMITIVE_TYPE;
				fileName = null;
			} else if (oldT == NullType.v() || oldT == BottomType.v()) {
				kind = TypeKind.NULL_TYPE;
				fileName = null;
			} else
				continue;
			Type newT = new Type(name, kind, fileName);
			oldToNewTMap.put(oldT, newT);
			types.add(newT);
		}
	}

	// initializes the suptypes of each chord type
	private void fillSuptypes() {
		List<Type> arraySuptypesList = new ArrayList<Type>(3);
		{
			SootClass oldC1 = Scene.v().getSootClass(
				"java.lang.Cloneable");
			SootClass oldC2 = Scene.v().getSootClass(
				"java.io.Serializable");
			RefType oldT1 = oldC1.getType();
			RefType oldT2 = oldC2.getType();
			Type newT1 = getType(oldT1);
			Type newT2 = getType(oldT2);
			arraySuptypesList.add(typeOfJavaLangObj);
			arraySuptypesList.add(newT1);
			arraySuptypesList.add(newT2);
		}
		SootClass classOfJavaLangObj =
			Scene.v().getSootClass("java.lang.Object");
		for (Map.Entry<soot.Type, Type> e :
				oldToNewTMap.entrySet()) {
			soot.Type oldT = e.getKey();
			Type newT = e.getValue();
			List<Type> newTs;
			if (oldT instanceof RefType) {
				SootClass oldC = ((RefType) oldT).getSootClass();
				if (oldC == classOfJavaLangObj)
					newTs = emptyTypeList;
				else {
					Chain<SootClass> oldImpCs =
						oldC.getInterfaces();
					int numSupCs = oldImpCs.size() + 1;
					newTs = new ArrayList<Type>(numSupCs);
					SootClass oldSupC = oldC.getSuperclass();
					RefType oldSupT = oldSupC.getType();
					Type newSupT = getType(oldSupT);
					newTs.add(newSupT);
					for (SootClass oldImpC : oldImpCs) {
						RefType oldImpT = oldImpC.getType();
						Type newImpT = getType(oldImpT);
						newTs.add(newImpT);
					}
				}		
			} else if (oldT instanceof ArrayType) {
				newTs = arraySuptypesList;
				soot.Type oldElemT = ((ArrayType) oldT).getElementType();
				Type newElemT = getType(oldElemT);
				newT.setElemType(newElemT);
			} else
				newTs = emptyTypeList;
			newT.setSuptypes(newTs);
		}
	}

	// initializes the fields of each chord type
	// also populates oldToNewFMap
	private void fillFields() {
		for (Map.Entry<soot.Type, Type> e : oldToNewTMap.entrySet()) {
			soot.Type oldT = e.getKey();
			Type newT = e.getValue();
			List<Field> newFs;
			if (oldT instanceof RefType) {
				SootClass oldC = ((RefType) oldT).getSootClass();
				Chain<SootField> oldFs = oldC.getFields();
				newFs = new ArrayList<Field>(oldFs.size() + 1);
				for (SootField oldF : oldFs) {
					String name = oldF.getName();
					soot.Type oldFldT = oldF.getType();
					Type newFldT = getType(oldFldT);
					int lineNum = getLineNum(oldF);
					Field newF = new Field(name, newFldT, newT, lineNum);
					setModifiers(oldF, newF);
					oldToNewFMap.put(oldF, newF);
					newFs.add(newF);
				}
			} else
				newFs = new ArrayList<Field>(1);
			Field newClsF = new Field("class", typeOfJavaLangCls, newT, 0);
			newClsF.setFinal();
			newClsF.setStatic();
			newClsF.setPublic();
			newFs.add(newClsF);
			newT.setFields(newFs);
		}
	}

	// initializes the methods of each chord type
	// also populates oldToNewMMap
	private void fillMethods() {
		for (Map.Entry<soot.Type, Type> e : oldToNewTMap.entrySet()) {
			soot.Type oldT = e.getKey();
			Type newT = e.getValue();
			List<Method> newMs;
			if (oldT instanceof RefType) {
				SootClass oldC = ((RefType) oldT).getSootClass();
				List<SootMethod> oldMs = oldC.getMethods();
				boolean genClinit = !oldC.declaresMethodByName("<clinit>");
				int numMs = oldMs.size() + (genClinit ? 1 : 0);
				newMs = new ArrayList<Method>(numMs);
				for (SootMethod oldM : oldMs) {
					String sign = oldM.getSubSignature();
					sign = sign.substring(sign.indexOf(' ') + 1);
					int methLineNum = getLineNum(oldM);
					Method newM = new Method(sign, newT, null, methLineNum);
					setModifiers(oldM, newM);
					String cName = oldC.getName();
					String mName = oldM.getName();
					String mSign = oldM.getSubSignature();
					if (cName.equals("java.lang.Thread") &&
							mSign.equals("void start()")) {
						forkMethod = newM;
					}
					 else if (cName.equals(JAVA_SEC_AC_CLS) &&
							mName.equals("doPrivileged")) {
						if (mSign.equals(DO_PA1_SIGN))
							pa1Method = newM;
						else if (mSign.equals(DO_PA2_SIGN))
							pa2Method = newM;
						else if (mSign.equals(DO_PA3_SIGN))
							pa3Method = newM;
						else if (mSign.equals(DO_PA4_SIGN))
							pa4Method = newM;
						else
							Assertions.Assert(false);
					}
					oldToNewMMap.put(oldM, newM);
					newMs.add(newM);
				}
        		if (genClinit) {
        			Method newM = buildClinitMethod(newT);
					newMs.add(newM);
				}
			} else
				newMs = emptyMethodList;
			newT.setMethods(newMs);
		}
	}

	private Method buildClinitMethod(Type newT) {
		Method newM = new Method("<clinit>()", newT, null, 0);
		newM.setStatic();
		List<Var> args = emptyVarList;
		List<Var> rets = emptyVarList;
		List<Var> tmps = new ArrayList<Var>(1);
		Var V = makeVar(typeOfJavaLangCls);
		tmps.add(V);
		Field F = newM.getCtnrType().getField("class");
		Assertions.Assert(F != null);
		HeadInst head = new HeadInst(0);
		Inst inst1 = new ObjValAsgnInst(V, typeOfJavaLangCls, 0);
		Inst inst2 = new StatFldRefInst(V, F, true, 0);
		TailInst tail = new TailInst(0);
		Map<Inst, Set<Inst>> instToSuccs =
			new HashMap<Inst, Set<Inst>>(4);
		Set<Inst> succs1 = new ArraySet<Inst>(1);
		succs1.add(inst1);
		instToSuccs.put(head, succs1);
		Set<Inst> succs2 = new ArraySet<Inst>(1);
		succs2.add(inst2);
		instToSuccs.put(inst1, succs2);
		Set<Inst> succs3 = new ArraySet<Inst>(1);
		succs3.add(tail);
		instToSuccs.put(inst2, succs3);
		instToSuccs.put(tail, emptyInstSet);
		CFG cfg = new CFG(head, tail, args, tmps, rets, null,
			null, instToSuccs);
		for (Var var : args)
			var.setCFG(cfg);
		for (Var var : tmps)
			var.setCFG(cfg);
		for (Inst inst : cfg.getNodes())
			inst.setCFG(cfg);
		newM.setCFG(cfg);
		cfg.setCtnrMethod(newM);
		return newM;
	}

	private Method forkMethod;
	private final static String JAVA_SEC_PA_CLS  = "java.security.PrivilegedAction";
	private final static String JAVA_SEC_PEA_CLS = "java.security.PrivilegedExceptionAction";
	private final static String JAVA_SEC_ACC_CLS = "java.security.AccessControlContext";
	private final static String JAVA_SEC_AC_CLS  = "java.security.AccessController";
	private final static String DO_PA1_SIGN =
		"java.lang.Object doPrivileged(" + JAVA_SEC_PA_CLS + ")";
	private final static String DO_PA2_SIGN =
		"java.lang.Object doPrivileged(" + JAVA_SEC_PEA_CLS + ")";
	private final static String DO_PA3_SIGN =
		"java.lang.Object doPrivileged(" + JAVA_SEC_PA_CLS + "," + JAVA_SEC_ACC_CLS + ")";
	private final static String DO_PA4_SIGN =
		"java.lang.Object doPrivileged(" + JAVA_SEC_PEA_CLS + "," + JAVA_SEC_ACC_CLS + ")";
	private Method pa1Method;
	private Method pa2Method;
	private Method pa3Method;
	private Method pa4Method;

	private void buildForkMethod() {
		Type newT = newM.getCtnrType();
		Var v = makeVar(newT);
		List<Var> args = new ArrayList<Var>(1);
		args.add(v);
		List<Var> rets = emptyVarList;
		List<Var> tmps = emptyVarList;
		Method newN = newT.getMethod("run()");
		Assertions.Assert(newN != null);
		HeadInst head = new HeadInst(0);
		Inst invk = new InvkInst(InvkKind.INVK_VIRTUAL, newN, args, rets, 0);
		TailInst tail = new TailInst(0);
		Set<Inst> headSuccs = new ArraySet<Inst>(1);
		headSuccs.add(invk);
		Set<Inst> invkSuccs = new ArraySet<Inst>(1);
		invkSuccs.add(tail);
		Map<Inst, Set<Inst>> instToSuccs =
			new HashMap<Inst, Set<Inst>>(3);
		instToSuccs.put(head, headSuccs);
		instToSuccs.put(invk, invkSuccs);
		instToSuccs.put(tail, emptyInstSet);
		CFG cfg = new CFG(head, tail, args, tmps, rets, null,
			null, instToSuccs);
		for (Var var : args)
			var.setCFG(cfg);
		for (Var var : tmps)
			var.setCFG(cfg);
		for (Inst inst : cfg.getNodes())
			inst.setCFG(cfg);
		newM.setCFG(cfg);
		cfg.setCtnrMethod(newM);
	}

	private void buildPAMethod(String sign) {
		List<Var> methArgs = new ArrayList<Var>(2);
		SootClass oldClass = null;
		Var va = null;
		if (sign.equals(DO_PA1_SIGN)) {
			oldClass = Scene.v().getSootClass(JAVA_SEC_PA_CLS);
      		soot.Type oldType = oldClass.getType();
			Type newType = getType(oldType);
			va = makeVar(newType);
			methArgs.add(va);
		} else if (sign.equals(DO_PA2_SIGN)) {
			oldClass = Scene.v().getSootClass(JAVA_SEC_PEA_CLS);
      		soot.Type oldType = oldClass.getType();
			Type newType = getType(oldType);
			va = makeVar(newType);
			methArgs.add(va);
		} else if (sign.equals(DO_PA3_SIGN)) {
			oldClass = Scene.v().getSootClass(JAVA_SEC_PA_CLS);
      			soot.Type oldType = oldClass.getType();
			Type newType = getType(oldType);
			va = makeVar(newType);
			methArgs.add(va);
			SootClass oldClass2 = Scene.v().getSootClass(JAVA_SEC_ACC_CLS);
      			soot.Type oldType2 = oldClass2.getType();
			Type newType2 = getType(oldType2);
			Var v2 = makeVar(newType2);
			methArgs.add(v2);
		} else if (sign.equals(DO_PA4_SIGN)) {
			oldClass = Scene.v().getSootClass(JAVA_SEC_PEA_CLS);
      			soot.Type oldType = oldClass.getType();
			Type newType = getType(oldType);
			va = makeVar(newType);
			methArgs.add(va);
			SootClass oldClass2 = Scene.v().getSootClass(JAVA_SEC_ACC_CLS);
      			soot.Type oldType2 = oldClass2.getType();
			Type newType2 = getType(oldType2);
			Var v2 = makeVar(newType2);
			methArgs.add(v2);
		} else
			Assertions.Assert(false);
		Var vr = makeVar(typeOfJavaLangObj);
		List<Var> methTmps = new ArrayList<Var>(1);
		methTmps.add(vr);
		List<Var> methRets = new ArrayList<Var>(1);
		methRets.add(vr);
		SootMethod oldN = oldClass.getMethod("java.lang.Object run()");
		Assertions.Assert(oldN != null);
		Method newN = getMethod(oldN);
		HeadInst head = new HeadInst(0);
		List<Var> invkArgs = new ArrayList<Var>(1);
		invkArgs.add(va);
		List<Var> invkRets = new ArrayList<Var>(1);
		invkRets.add(vr);
		Inst invk = new InvkInst(InvkKind.INVK_INTERFACE, newN,
			invkArgs, invkRets, 0);
		TailInst tail = new TailInst(0);
		Set<Inst> headSuccs = new ArraySet<Inst>(1);
		headSuccs.add(invk);
		Set<Inst> invkSuccs = new ArraySet<Inst>(1);
		invkSuccs.add(tail);
		Map<Inst, Set<Inst>> instToSuccs =
			new HashMap<Inst, Set<Inst>>(3);
		instToSuccs.put(head, headSuccs);
		instToSuccs.put(invk, invkSuccs);
		instToSuccs.put(tail, emptyInstSet);
		CFG cfg = new CFG(head, tail, methArgs, methTmps, methRets, null,
			null, instToSuccs);
		for (Var var : methArgs)
			var.setCFG(cfg);
		for (Var var : methTmps)
			var.setCFG(cfg);
		for (Inst inst : cfg.getNodes())
			inst.setCFG(cfg);
		newM.setCFG(cfg);
		cfg.setCtnrMethod(newM);
	}

	private void setModifiers(SootField oldF, Field newF) {
		if (oldF.isStatic())
			newF.setStatic();
		if (oldF.isFinal())
			newF.setFinal();
		if (oldF.isPrivate())
			newF.setPrivate();
		if (oldF.isProtected())
			newF.setProtected();
		if (oldF.isPublic())
			newF.setPublic();
	}

	private void setModifiers(SootMethod oldM, Method newM) {
		if (oldM.isAbstract())
			newM.setAbstract();
		if (oldM.isNative())
			newM.setNative();
		if (oldM.isPrivate())
			newM.setPrivate();
		if (oldM.isProtected())
			newM.setProtected();
		if (oldM.isPublic())
			newM.setPublic();
		if (oldM.isStatic())
			newM.setStatic();
	}
}
