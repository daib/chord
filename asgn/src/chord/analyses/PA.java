package chord.analyses;

import java.io.PrintWriter;

import chord.program.Field;
import chord.program.Var;
import chord.program.insts.ObjValAsgnInst;
import chord.program.insts.InvkInst;
import chord.program.Method;
import chord.project.Chord;
import chord.project.JavaTask;
import chord.project.ProgramRel;
import chord.util.FileUtils;
import chord.util.tuple.object.Pair;
import chord.util.tuple.object.Trio;

/**
 * Context insensitive pointer analysis with call-graph construction.
 */
@Chord(
	name = "pa-java",
	consumedNames = { "FH", "VH", "HFH", "IM" }
)
public class PA extends JavaTask {
	public void run() {
        printFH();
        printVH();
        printHFH();
        printIM();
    }
	private void printFH() {
		PrintWriter out = FileUtils.newPrintWriter("FH.txt");
        ProgramRel relFH = (ProgramRel) project.getTrgt("FH");
		relFH.load();
        Iterable<Pair<Field, ObjValAsgnInst>> tuples = relFH.getAry2ValTuples();
		for (Pair<Field, ObjValAsgnInst> tuple : tuples) {
			out.println("F=" + tuple.val0 + " H=" + tuple.val1);
		}
		relFH.close();
		out.close();
	}
	private void printVH() {
		PrintWriter out = FileUtils.newPrintWriter("VH.txt");
        ProgramRel relVH = (ProgramRel) project.getTrgt("VH");
		relVH.load();
        Iterable<Pair<Var, ObjValAsgnInst>> tuples = relVH.getAry2ValTuples();
		for (Pair<Var, ObjValAsgnInst> tuple : tuples) {
			out.println("V=" + tuple.val0 + " H=" + tuple.val1);
		}
		relVH.close();
		out.close();
	}
	private void printHFH() {
		PrintWriter out = FileUtils.newPrintWriter("HFH.txt");
        ProgramRel relHFH = (ProgramRel) project.getTrgt("HFH");
		relHFH.load();
        Iterable<Trio<ObjValAsgnInst, Field, ObjValAsgnInst>> tuples =
			relHFH.getAry3ValTuples();
		for (Trio<ObjValAsgnInst, Field, ObjValAsgnInst> tuple : tuples) {
			out.println("H=" + tuple.val0 + " F=" + tuple.val1 +
				" H=" + tuple.val2);
		}
		relHFH.close();
		out.close();
	}
	private void printIM() {
		PrintWriter out = FileUtils.newPrintWriter("IM.txt");
        ProgramRel relIM = (ProgramRel) project.getTrgt("IM");
		relIM.load();
        Iterable<Pair<InvkInst, Method>> tuples = relIM.getAry2ValTuples();
		for (Pair<InvkInst, Method> tuple : tuples) {
			out.println("I=" + tuple.val0 + " M=" + tuple.val1);
		}
		relIM.close();
		out.close();
	}
}
