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
 * Relation containing each tuple (m,f,v) such that method m contains
 * a statement of the form <tt>f = v</tt>.
 */
@Chord(
	name = "MputStatFldInst",
	sign = "M0,F0,V0:F0_M0_V0"
)
public class RelMputStatFldInst extends ProgramRel {
	public void fill() {
        //throw new RuntimeException("cs265: implement this method");
		DomM domM = (DomM) doms[0];
		for(Method mVal:domM) {
			if(mVal.hasCFG()) {
				for (Inst inst : mVal.getCFG().getNodes()) {
					if (inst instanceof StatFldRefInst) {
						StatFldRefInst fldRef = (StatFldRefInst) inst;
						if(fldRef.isWr()) {	
							Var v = fldRef.getVar();
							if(v == null)
								continue;
							add(mVal, fldRef.getField(), v);
						}
					}
				}
			}
		}	
	}
}
