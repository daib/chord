package chord.rels;

import chord.doms.DomM;
import chord.doms.DomV;
import chord.program.Method;
import chord.program.Var;
import chord.program.insts.Inst;
import chord.program.insts.ObjVarAsgnInst;
import chord.program.insts.PhiExpAsgnInst;
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
		int numM = domM.size();
		for(Method mVal: domM) {
			if(mVal.hasCFG()) {
				for (Inst inst : mVal.getCFG().getNodes()) {
					if (inst instanceof ObjVarAsgnInst) {
						ObjVarAsgnInst varAsgn = (ObjVarAsgnInst) inst;
						add(mVal, varAsgn.getLvar(), varAsgn.getRvar());
					} else if (inst instanceof  PhiExpAsgnInst) {
						PhiExpAsgnInst expAsgn = (PhiExpAsgnInst) inst;
						for(Var rVar:expAsgn.getRvars()) {
							add(mVal, expAsgn.getLvar(), rVar);
						}
					}

				}
			}
		}	
	}
}
