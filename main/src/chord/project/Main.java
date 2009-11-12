/*
 * Copyright (c) 2006-07, The Trustees of Stanford University.  All
 * rights reserved.
 * Licensed under the terms of the GNU GPL; see COPYING for details.
 */
package chord.project;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;

import chord.program.Method;
import chord.program.Program;
import chord.program.Type;
import chord.program.builders.IProgramBuilder;
import chord.program.builders.SootProgramBuilder;
import chord.util.Assertions;
import chord.util.PropertyUtils;

/**
 * Chord's main class.
 * 
 * @author Mayur Naik (mhn@cs.stanford.edu)
 */
public class Main {
	public static void main(String[] a) {
		PrintStream outStream = null;
		PrintStream errStream = null;
		try {
			String outStreamName = PropertyUtils.getStrProperty("chord.out");
			String errStreamName = PropertyUtils.getStrProperty("chord.err");
			File outFile = (outStreamName != null) ? new File(outStreamName) : null;
			File errFile = (errStreamName != null) ? new File(errStreamName) : null;
			if (outFile != null) {
				outStream = new PrintStream(outFile);
				System.setOut(outStream);
			}
			if (errFile != null) {
				if (outFile != null && errFile.equals(outFile))
					errStream = outStream;
				else
					errStream = new PrintStream(errFile);
				System.setErr(errStream);
			}
			String mainClassName =
				PropertyUtils.getStrProperty("chord.main.class");
			String classPathName =
				PropertyUtils.getStrProperty("chord.class.path");
			String srcPathName =
				PropertyUtils.getStrProperty("chord.src.path");
			String builderClassName =
				PropertyUtils.getStrProperty("chord.builder.class",
					SootProgramBuilder.class.getName());
			String serialFileName =
				PropertyUtils.getStrProperty("chord.serial.file");
			String analysisList =
				PropertyUtils.getStrProperty("chord.analysis.list");
			String javaAnalysisPathName =
				PropertyUtils.getStrProperty("chord.java.analysis.path", "");
			String dlogAnalysisPathName =
				PropertyUtils.getStrProperty("chord.dlog.analysis.path", "");
			String annotIncludeFileName =
				PropertyUtils.getStrProperty("chord.annot.include.file");
			String annotExcludeFileName =
				PropertyUtils.getStrProperty("chord.annot.exclude.file");
			String ignoreMethodsBySignFileName =
				PropertyUtils.getStrProperty("chord.ignore.methods.bysign.file");
			String ignoreMethodsByCtnrFileName =
				PropertyUtils.getStrProperty("chord.ignore.methods.byctnr.file");
			boolean doSyncRemovalTransform =
				PropertyUtils.getBoolProperty("chord.transform.rem.syncs", true);
			boolean doLoopRemovalTransform =
				PropertyUtils.getBoolProperty("chord.transform.rem.loops", true);
			boolean doSkipRemovalTransform =
				PropertyUtils.getBoolProperty("chord.transform.rem.skips", true);
			boolean doSSATransform =
				PropertyUtils.getBoolProperty("chord.transform.ssa", true);
			boolean printProgram =
				PropertyUtils.getBoolProperty("chord.print.program", false);
			String sunBootClassPathName =
				PropertyUtils.getStrProperty("sun.boot.class.path");
			String workDirName = System.getProperty("user.dir");

			System.out.println("System Properties [working directory is '" + workDirName + "']:");
			System.out.println("chord.main.class: " + mainClassName);
			System.out.println("chord.class.path: " + classPathName);
			System.out.println("chord.src.path: " + srcPathName);
			System.out.println("chord.out: " + outStreamName);
			System.out.println("chord.err: " + errStreamName);
			System.out.println("chord.builder.class: " + builderClassName);
			System.out.println("chord.serial.file: " + serialFileName);
			System.out.println("chord.analysis.list: " + analysisList);
			System.out.println("chord.java.analysis.path: " + javaAnalysisPathName);
			System.out.println("chord.dlog.analysis.path: " + dlogAnalysisPathName);
			System.out.println("chord.annot.include.file: " + annotIncludeFileName);
			System.out.println("chord.annot.exclude.file: " + annotExcludeFileName);
			System.out.println("chord.ignore.methods.bysign.file: " + ignoreMethodsBySignFileName);
			System.out.println("chord.ignore.methods.byctnr.file: " + ignoreMethodsByCtnrFileName);
			System.out.println("chord.transform.rem.syncs: " + doSyncRemovalTransform);
			System.out.println("chord.transform.rem.loops: " + doLoopRemovalTransform);
			System.out.println("chord.transform.rem.skips: " + doSkipRemovalTransform);
			System.out.println("chord.transform.ssa: " + doSSATransform);
			System.out.println("chord.print.program: " + printProgram);
			System.out.println("sun.boot.class.path: " + sunBootClassPathName);
			System.out.println("bddbddb.max.heap.size: " + System.getProperty("bddbddb.max.heap.size"));
			System.out.println("bddbddb.class.path: " + System.getProperty("bddbddb.class.path"));
			System.out.println("bddbddb.library.path: " + System.getProperty("bddbddb.library.path"));
			
			if (doLoopRemovalTransform)
				Assertions.Assert(doSyncRemovalTransform);
			if (doSSATransform)
				Assertions.Assert(doLoopRemovalTransform);
			Program program = null;
			boolean loadProgram = false;
			boolean saveProgram = false;
			if (serialFileName != null) {
				if ((new File(serialFileName)).exists())
					loadProgram = true;
				else
					saveProgram = true;
			}
			ProjectBuilder projectBuilder =
				new ProjectBuilder(javaAnalysisPathName, dlogAnalysisPathName);
			Project project = projectBuilder.build();
			if (project == null) {
				System.err.println("Found errors (see above). Exiting ...");
				System.exit(1);
			}
			if (loadProgram) {
				FileInputStream fs = new FileInputStream(serialFileName);
				ObjectInputStream os = new ObjectInputStream(fs);
				program = (Program) os.readObject();
				if (doSyncRemovalTransform)
					Assertions.Assert(program.removedSyncs());
				if (doLoopRemovalTransform)
					Assertions.Assert(program.removedLoops());
				if (doSkipRemovalTransform)
					Assertions.Assert(program.removedSkips());
				if (doSSATransform)
					Assertions.Assert(program.convertedToSSA());
				os.close();
				program.validate();
			} else {
				Assertions.Assert(classPathName != null);
				Assertions.Assert(mainClassName != null);
				classPathName += File.pathSeparator + sunBootClassPathName;
				IProgramBuilder builder = (IProgramBuilder)
					Class.forName(builderClassName).newInstance();
				program = builder.build(mainClassName, classPathName,
					srcPathName, annotIncludeFileName, annotExcludeFileName,
					ignoreMethodsBySignFileName, ignoreMethodsByCtnrFileName);
				if (doSyncRemovalTransform)
					program.removeSyncs();
				if (doLoopRemovalTransform)
					program.removeLoops();
				if (doSkipRemovalTransform)
					program.removeSkips();
				if (doSSATransform)
					program.convertToSSA();
				program.validate();
			}
			project.setProgram(program);
			if (saveProgram) {
				FileOutputStream fs =
					new FileOutputStream(serialFileName);
				ObjectOutputStream os = new ObjectOutputStream(fs);
				os.writeObject(program);
				os.close();
			}
			if (printProgram) {
				program.print();
			}
			if (analysisList != null) {
				String[] analysisAry = analysisList.split(" ,|:|;");
				for (String name : analysisAry)
					project.runTask(name);
			}
		} catch (Throwable ex) {
			ex.printStackTrace();
			if (outStream != null)
				outStream.close();
			if (errStream != null && errStream != outStream)
				errStream.close();
			System.exit(1);
		}
	}
}
