package chord.rels;

import chord.doms.DomM;
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
		for(Method mVal : domM ) {
			if(mVal.hasCFG()) {
				for (Inst inst : mVal.getCFG().getNodes()) {
					if (inst instanceof ObjValAsgnInst) {
						ObjValAsgnInst hVal = (ObjValAsgnInst) inst;
						add(mVal, hVal.getVar(), hVal);
					}
				}
			}
		}	

	}
}
