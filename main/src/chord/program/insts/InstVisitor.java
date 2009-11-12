/*
 * Copyright (c) 2006-07, The Trustees of Stanford University.  All
 * rights reserved.
 * Licensed under the terms of the GNU GPL; see COPYING for details.
 */
package chord.program.insts;


/**
 * A visitor over all possible kinds of simple statements.
 * 
 * @author Mayur Naik (mhn@cs.stanford.edu)
 */
public interface InstVisitor {
    public void visit(SkipInst inst);
    public void visit(NilValAsgnInst inst);
    public void visit(StrValAsgnInst inst);
    public void visit(ClsVarAsgnInst inst);
    public void visit(ObjValAsgnInst inst);
    public void visit(ObjVarAsgnInst inst);
    public void visit(PhiExpAsgnInst inst);
    public void visit(AryElemRefInst inst);
    public void visit(InstFldRefInst inst);
    public void visit(StatFldRefInst inst);
    public void visit(InvkInst inst);
    public void visit(AcqLockInst inst);
    public void visit(RelLockInst inst);
    public void visit(HeadInst inst);
    public void visit(TailInst inst);
}
