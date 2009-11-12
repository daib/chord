package chord.rels;

import chord.doms.DomM;
import chord.program.Method;
import chord.program.insts.Inst;
import chord.program.insts.InvkInst;
import chord.program.insts.InvkKind;
import chord.project.Chord;
import chord.project.ProgramRel;

/**
 * Relation containing each tuple (i,m) such that m is the resolved
 * method of method invocation statement i of kind
 * <tt>INVK_STATIC</tt>.
 */
@Chord(
	name = "statIM",
	sign = "I0,M0:I0xM0"
)
public class RelStatIM extends ProgramRel {
	public void fill() {
        //throw new RuntimeException("cs265: implement this method");
		DomM domM = (DomM) doms[1];
		for(Method mVal:domM) {
			if(mVal.hasCFG()) {
				for (Inst inst : mVal.getCFG().getNodes()) {
					if (inst instanceof InvkInst) {
						InvkInst invkInst = (InvkInst) inst;
						if(invkInst.getInvkKind() == InvkKind.INVK_STATIC) 
							add(inst, mVal);
					}
				}
			}
		}

	}
}
