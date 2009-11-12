package chord.rels;

import chord.doms.DomM;
import chord.program.Method;
import chord.program.Var;
import chord.program.Field;
import chord.program.insts.Inst;
import chord.program.insts.StatFldRefInst;
import chord.project.Chord;
import chord.project.ProgramRel;

/**
 * Relation containing each tuple (m,v,f) such that method m contains
 * a statement of the form <tt>v = f</tt>.
 */
@Chord(
	name = "MgetStatFldInst",
	sign = "M0,V0,F0:F0_M0_V0"
)
public class RelMgetStatFldInst extends ProgramRel {
	public void fill() {
        //throw new RuntimeException("cs265: implement this method");
		DomM domM = (DomM) doms[0];
		for(Method mVal:domM) {
			if(mVal.hasCFG()) {
				for (Inst inst : mVal.getCFG().getNodes()) {
					if (inst instanceof StatFldRefInst) {
						StatFldRefInst hVal = (StatFldRefInst) inst;
						if(!hVal.isWr()) {	
							Var v = hVal.getVar();
							if(v == null)
								continue;
							Field f = hVal.getField();
							add(mVal, v, f);
						}
					}
				}
			}
		}	
	}
}
