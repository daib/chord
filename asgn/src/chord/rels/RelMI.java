package chord.rels;

import chord.doms.DomM;
import chord.program.Method;
import chord.program.insts.Inst;
import chord.program.insts.InvkInst;
import chord.project.Chord;
import chord.project.ProgramRel;

/**
 * Relation containing each tuple (m,i) such that method m contains
 * method invocation statement i.
 */
@Chord(
	name = "MI",
	sign = "M0,I0:I0xM0"
)
public class RelMI extends ProgramRel {
	public void fill() {
		//throw new RuntimeException("cs265: implement this method");
        DomM domM = (DomM) doms[0];
		for(Method mVal:domM) {
			if(mVal.hasCFG()) {
				for (Inst inst : mVal.getCFG().getNodes()) {
					if (inst instanceof InvkInst) {
						//InvkInst hVal = (InvkInst) inst;
						add(mVal, inst);
					}
				}
			}
		}
	}
}
