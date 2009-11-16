package chord.rels;

import chord.doms.DomM;
import chord.program.Method;
import chord.program.Var;
import chord.program.Field;
import chord.program.insts.Inst;
import chord.program.insts.InstFldRefInst;
import chord.program.insts.AryElemRefInst;
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

		for( Method mVal: domM) {
			if(mVal.hasCFG()) {
				for (Inst inst : mVal.getCFG().getNodes()) {
					if (inst instanceof InstFldRefInst) {
						InstFldRefInst fldRef = (InstFldRefInst) inst;
						if(!fldRef.isWr()) {	
							Var v = fldRef.getVar();
							if(v == null)
								continue;
							add(mVal, v, fldRef.getField(), fldRef.getBase());
						}
					} if (inst instanceof AryElemRefInst) {
						AryElemRefInst fldRef = (AryElemRefInst) inst;
						if(!fldRef.isWr()) {	
							Var v = fldRef.getVar();
							if(v == null)
								continue;
							add(mVal, v, fldRef.getField(), fldRef.getBase());
						}
					}

				}
			}
		}	
	}
}
