package chord.rels;

import chord.doms.DomM;
import chord.doms.DomZ;
import chord.doms.DomV;
import chord.program.Method;
import chord.program.Var;
import chord.project.Chord;
import chord.project.ProgramRel;

/**
 * Relation containing each tuple (m,z,v) such that local variable
 * v is the zth return variable of method m.
 */
@Chord(
	name = "MmethRet",
	sign = "M0,Z0,V1:M0_V1_Z0"
)
public class RelMmethRet extends ProgramRel {
	public void fill() {
        //throw new RuntimeException("cs265: implement this method");
	
		System.out.println("RelMmethRet method");
		DomM domM = (DomM) doms[0];
		DomZ domZ = (DomZ) doms[1];
		DomV domV = (DomV) doms[2];
		int numM = domM.size();
		for(int mIdx = 0; mIdx < numM; mIdx++) {
			Method mVal = domM.get(mIdx);
			int count = 0;
			if(mVal.hasCFG()) {
				for(Var vVal: mVal.getCFG().getRets()) {
					int vIdx = domV.get(vVal);
					add(mIdx, count, vIdx);
					count++;
				}
			}
		}	
	}
}
