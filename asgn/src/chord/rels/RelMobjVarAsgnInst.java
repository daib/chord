package chord.rels;

import chord.doms.DomM;
import chord.doms.DomV;
import chord.program.Method;
import chord.program.Var;
import chord.program.insts.Inst;
import chord.program.insts.ObjVarAsgnInst;
import chord.project.Chord;
import chord.project.ProgramRel;

/**
 * Relation containing each tuple (m,v1,v2) such that method m
 * contains a copy-assignment statement of the form <tt>v1 = v2</tt> 
 * or a phi-assignment statement of the form 
 * <tt>v1 = phi(...,v2,...)</tt>.
 */
@Chord(
	name = "MobjVarAsgnInst",
	sign = "M0,V0,V1:M0_V0xV1"
)
public class RelMobjVarAsgnInst extends ProgramRel {
	public void fill() {
        //throw new RuntimeException("cs265: implement this method");
		DomM domM = (DomM) doms[0];
		DomV domV = (DomV) doms[1];
		int numM = domM.size();
		for(int mIdx = 0; mIdx < numM; mIdx++) {
			Method mVal = domM.get(mIdx);
			if(mVal.hasCFG()) {
				for (Inst inst : mVal.getCFG().getNodes()) {
					if (inst instanceof ObjVarAsgnInst) {
						ObjVarAsgnInst hVar = (ObjVarAsgnInst) inst;
						Var lvar = hVar.getLvar();
						Var rvar = hVar.getRvar();
						int lvIdx = domV.get(lvar);
						int rvIdx = domV.get(rvar);
						add(mIdx, lvIdx, rvIdx);
					}
				}
			}
		}	
	}
}
