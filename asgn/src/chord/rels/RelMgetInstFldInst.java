package chord.rels;

import chord.doms.DomM;
import chord.doms.DomF;
import chord.doms.DomV;
import chord.program.Method;
import chord.program.Var;
import chord.program.Field;
import chord.program.insts.Inst;
import chord.program.insts.InstFldRefInst;
import chord.project.Chord;
import chord.project.ProgramRel;

/**
 * Relation containing each tuple (m,v,f,b) such that method m
 * contains a statement of the form <tt>v = b.f</tt>.
 */
@Chord(
	name = "MgetInstFldInst",
	sign = "M0,V0,F0,V1:F0_M0_V0xV1"
)
public class RelMgetInstFldInst extends ProgramRel {
	public void fill() {
        //throw new RuntimeException("cs265: implement this method");
		DomM domM = (DomM) doms[0];

		int numM = domM.size();
		for( Method mVal: domM) {
			if(mVal.hasCFG()) {
				for (Inst inst : mVal.getCFG().getNodes()) {
					if (inst instanceof InstFldRefInst) {
						InstFldRefInst hVal = (InstFldRefInst) inst;
						if(!hVal.isWr()) {	
							Var v = hVal.getVar();
							if(v == null)
								continue;
							Var b = hVal.getBase();
							Field f = hVal.getField();
							add(mVal, v, f, b);
						}
					}
				}
			}
		}	
	}
}
