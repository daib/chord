package chord.rels;

import chord.doms.DomI;
import chord.doms.DomZ;
import chord.doms.DomV;
import chord.program.insts.InvkInst;
import chord.program.Var;
import chord.project.Chord;
import chord.project.ProgramRel;

/**
 * Relation containing each tuple (i,z,v) such that local variable v
 * is the zth return variable of method invocation statement i.
 */
@Chord(
	name = "IinvkRet",
	sign = "I0,Z0,V0:I0_V0_Z0"
)
public class RelIinvkRet extends ProgramRel {
	public void fill() {
        //throw new RuntimeException("cs265: implement this method");
		DomI domI = (DomI) doms[0];
		DomZ domZ = (DomZ) doms[1];
		DomV domV = (DomV) doms[2];
		int numI = domI.size();
		for(int iIdx = 0; iIdx < numI; iIdx++) {
			InvkInst iVal = domI.get(iIdx);
			int count = 0;
			for(Var vVal: iVal.getRets()) {
				int vIdx = domV.get(vVal);
				add(iIdx, count, vIdx);
				count++;
			}
		}	
	}
}
