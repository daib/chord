package chord.rels;

import chord.doms.DomM;
import chord.doms.DomH;
import chord.doms.DomV;
import chord.program.Method;
import chord.program.Var;
import chord.program.insts.Inst;
import chord.program.insts.ObjValAsgnInst;
import chord.project.Chord;
import chord.project.ProgramRel;

/**
 * Relation containing each tuple (m,v,h) such that method m contains
 * object allocation statement h which assigns to local variable v.
 */
@Chord(
	name = "MobjValAsgnInst",
	sign = "M0,V0,H0:M0_V0_H0"
)
public class RelMobjValAsgnInst extends ProgramRel {
	public void fill() {
		//throw new RuntimeException("cs265: implement this method");
		DomM domM = (DomM) doms[0];
		DomV domV = (DomV) doms[1];
		DomH domH = (DomH) doms[2];
		int numM = domM.size();
		for(int mIdx = 0; mIdx < numM; mIdx++) {
			Method mVal = domM.get(mIdx);
			if(mVal.hasCFG()) {
				for (Inst inst : mVal.getCFG().getNodes()) {
					if (inst instanceof ObjValAsgnInst) {
						ObjValAsgnInst hVal = (ObjValAsgnInst) inst;
						Var var = hVal.getVar();
						int vIdx = domV.get(var);
						int hIdx = domH.get(hVal);
						add(mIdx, vIdx, hIdx);
					}
				}
			}
		}	

	}
}
