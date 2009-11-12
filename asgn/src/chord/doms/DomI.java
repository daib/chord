/*
 * Copyright (c) 2006-07, The Trustees of Stanford University.  All
 * rights reserved.
 * Licensed under the terms of the GNU GPL; see COPYING for details.
 */
package chord.doms;

import chord.program.Method;
import chord.program.CFG;
import chord.program.insts.Inst;
import chord.program.insts.InvkInst;
import chord.project.Chord;
import chord.project.ProgramDom;

/**
 * Domain of method invocation statements.
 * 
 * @author Mayur Naik (mhn@cs.stanford.edu)
 */
@Chord(
	name = "I",
	consumedNames = { "M" }
)
public class DomI extends ProgramDom<InvkInst> {
	private DomM domM;
	public void fill() {
		domM = (DomM) project.getTrgt("M"); 
		for (Method meth : domM) {
			CFG cfg = meth.getCFG();
			if (cfg == null)
				continue;
			for (Inst inst : cfg.getNodes()) {
				if (inst instanceof InvkInst)
					set((InvkInst) inst);
			}
		}
	}
	public String toXMLAttrsString(InvkInst inst) {
		Method meth = inst.getOutermostCtnrMethod();
		String fileName = meth.getCtnrType().getFileName();
		return "file=\"" + fileName + "\" " +
			"line=\"" + inst.getLineNum() + "\" " +
			"Mid=\"M" + domM.get(meth) + "\"";
	}
}
