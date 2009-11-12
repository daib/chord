/*
 * Copyright (c) 2006-07, The Trustees of Stanford University.  All
 * rights reserved.
 * Licensed under the terms of the GNU GPL; see COPYING for details.
 */
package chord.project;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.Iterator;
import java.io.FilenameFilter;
import java.net.MalformedURLException;

import org.scannotation.AnnotationDB;

import chord.util.ArraySet;
import chord.util.ArrayUtils;
import chord.util.Assertions;
import chord.util.ClassUtils;
import chord.util.StringUtils;
import chord.util.bddbddb.RelSign;
import chord.util.tuple.object.Pair;

/**
 * Builder of a project.
 *
 * @author Mayur Naik (mhn@cs.stanford.edu)
 */
public class ProjectBuilder {
	private Project project;
	private String javaAnalysisPathName;
	private String dlogAnalysisPathName;
	private boolean hasNoErrors = true;
	
	private Map<String, ITask> nameToTaskMap;
	private Map<String, Object> nameToTrgtMap;
	private Map<String, Set<TrgtInfo>> nameToTrgtsDebugMap;
	private Map<ITask, Set<String>> taskToConsumedNamesMap;
	private Map<ITask, Set<String>> taskToProducedNamesMap;
	/**
	 * Constructor.
	 * 
	 * @param	javaAnalysisPathName	A path providing the locations
	 * 			of all Java-based program analyses to be included in
	 * 			this project.
	 * @param	dlogAnalysisPathName	A path providing the locations
	 * 			of all Dlog-based program analyses to be included in
	 * 			this project.
	 */
	public ProjectBuilder(String javaAnalysisPathName, String dlogAnalysisPathName) {
		this.dlogAnalysisPathName = dlogAnalysisPathName;
		this.javaAnalysisPathName = javaAnalysisPathName;
	}
	/**
	 * Builds the project from the paths provided to method
	 * {@link #ProjectBuilder(String, String)} specifying the locations
	 * of all Java-based and Dlog-based program analyses.
	 * 
	 * @return	The project, if it was built successfully, and null
	 * 			otherwise.
	 */
	public Project build() {
		this.project = new Project();

		nameToTaskMap = new HashMap<String, ITask>();
		nameToTrgtsDebugMap = new HashMap<String, Set<TrgtInfo>>();
		taskToConsumedNamesMap =
			new HashMap<ITask, Set<String>>();
		taskToProducedNamesMap =
			new HashMap<ITask, Set<String>>();
		
		buildDlogAnalysisMap();
		buildJavaAnalysisMap();

		nameToTrgtMap = new HashMap<String, Object>();
		List<Pair<ProgramRel, RelSign>> todo =
			new ArrayList<Pair<ProgramRel, RelSign>>();
		for (Map.Entry<String, Set<TrgtInfo>> e :
				nameToTrgtsDebugMap.entrySet()) {
			String name = e.getKey();
			Set<TrgtInfo> infos = e.getValue();
			Iterator<TrgtInfo> it = infos.iterator();
			TrgtInfo fstInfo = it.next();
			Class resType = fstInfo.type;
			String resTypeLoc = fstInfo.location;
			String[] resDomNames;
			String resDomOrder;
			if (fstInfo.sign != null) {
				resDomNames = fstInfo.sign.val0;
				resDomOrder = fstInfo.sign.val1;
			} else {
				resDomNames = null;
				resDomOrder = null;
			}
			String resDomNamesLoc = fstInfo.location;
			String resDomOrderLoc = fstInfo.location;
			boolean corrupt = false;
			while (it.hasNext()) {
				TrgtInfo info = it.next();
				Class curType = info.type;
				if (curType != null) {
					if (resType == null) {
						resType = curType;
						resTypeLoc = info.location;
					} else if (ClassUtils.isSubclass(curType, resType)) {
						resType = curType;
						resTypeLoc = info.location;
					} else if (!ClassUtils.isSubclass(resType, curType)) {
						inconsistentTypes(name,
							resType.toString(), curType.toString(),
							resTypeLoc, info.location);
						corrupt = true;
						break;
					}
				}
				RelSign curSign = info.sign;
				if (curSign != null) {
					String[] curDomNames = curSign.val0;
					if (resDomNames == null) {
						resDomNames = curDomNames;
						resDomNamesLoc = info.location;
					} else if (!Arrays.equals(resDomNames, curDomNames)) {
						inconsistentDomNames(name,
							ArrayUtils.toString(resDomNames),
							ArrayUtils.toString(curDomNames),
							resDomNamesLoc, info.location);
						corrupt = true;
						break;
					}
					String curDomOrder = curSign.val1;
					if (curDomOrder != null) {
						if (resDomOrder == null) {
							resDomOrder = curDomOrder;
							resDomOrderLoc = info.location;
						} else if (!resDomOrder.equals(curDomOrder)) {
							inconsistentDomOrders(name,
								resDomOrder, curDomOrder,
								resDomOrderLoc, info.location);
						}
					}
				}
			}
			if (corrupt)
				continue;
			if (resType == null) {
				unknownType(name);
				continue;
			}
			RelSign sign = null;
			if (ClassUtils.isSubclass(resType, ProgramRel.class)) {
				if (resDomNames == null) {
					unknownSign(name);
					continue;
				}
				if (resDomOrder == null) {
					unknownOrder(name);
					continue;
				}
				sign = new RelSign(resDomNames, resDomOrder);
			}
			Object trgt = nameToTaskMap.get(name);
			if (trgt == null) {
				trgt = instantiate(resType.getName(), resType);
				if (trgt instanceof ITask) {
					ITask analysis = (ITask) trgt;
					analysis.setName(name);
					analysis.setProject(project);
				}
			}
			nameToTrgtMap.put(name, trgt);
			if (sign != null) {
				ProgramRel rel = (ProgramRel) trgt;
				todo.add(new Pair<ProgramRel, RelSign>(rel, sign));
			}
		}

		if (!hasNoErrors)
			return null;

		for (Pair<ProgramRel, RelSign> tuple : todo) {
			ProgramRel rel = tuple.val0;
			RelSign sign = tuple.val1;
			String[] domNames = sign.getDomNames();
			ProgramDom[] doms = new ProgramDom[domNames.length];
			boolean hasErrors = false;
			for (int i = 0; i < domNames.length; i++) {
				String domName = StringUtils.trimNumSuffix(domNames[i]);
				Object trgt = nameToTrgtMap.get(domName);
				if (trgt == null) {
					undefinedDom(domName, rel.getName());
					continue;
				}
				if (!(trgt instanceof ProgramDom)) {
					illtypedDom(domName, rel.getName(),
						trgt.getClass().getName());
					continue;
				}
				doms[i] = (ProgramDom) trgt;
			}
			rel.setSign(sign);
			rel.setDoms(doms);
		}

		if (!hasNoErrors)
			return null;

		Map<Object, Set<ITask>> trgtToConsumerTasksMap =
			new HashMap<Object, Set<ITask>>();
		Map<Object, Set<ITask>> trgtToProducerTasksMap =
			new HashMap<Object, Set<ITask>>();
		for (Object trgt : nameToTrgtMap.values()) {
			Set<ITask> consumerTasks = new HashSet<ITask>();
			trgtToConsumerTasksMap.put(trgt, consumerTasks);
			Set<ITask> producerTasks = new HashSet<ITask>();
			trgtToProducerTasksMap.put(trgt, producerTasks);
		}
		Map<ITask, Set<Object>> taskToConsumedTrgtsMap =
			new HashMap<ITask, Set<Object>>();
		Map<ITask, Set<Object>> taskToProducedTrgtsMap =
			new HashMap<ITask, Set<Object>>();
		for (ITask task : nameToTaskMap.values()) {
			Set<String> consumedNames =
				taskToConsumedNamesMap.get(task);
			Set<Object> consumedTrgts =
				new HashSet<Object>(consumedNames.size());
			for (String name : consumedNames) {
				Object trgt = nameToTrgtMap.get(name);
				Assertions.Assert(trgt != null, name);
				consumedTrgts.add(trgt);
				Set<ITask> consumerTasks =
					trgtToConsumerTasksMap.get(trgt);
				consumerTasks.add(task);
			}
			taskToConsumedTrgtsMap.put(task, consumedTrgts);
			Set<String> producedNames =
				taskToProducedNamesMap.get(task);
			Set<Object> producedTrgts =
				new HashSet<Object>(producedNames.size());
			for (String name : producedNames) {
				Object trgt = nameToTrgtMap.get(name);
				Assertions.Assert(trgt != null, name);
				producedTrgts.add(trgt);
				Set<ITask> producerTasks =
					trgtToProducerTasksMap.get(trgt);
				producerTasks.add(task);
			}
			taskToProducedTrgtsMap.put(task, producedTrgts);
		}
		for (String name : nameToTrgtMap.keySet()) {
			Object trgt = nameToTrgtMap.get(name);
			Set<ITask> producerTasks =
				trgtToProducerTasksMap.get(trgt);
			int size = producerTasks.size();
			if (size == 0) {
				Set<ITask> consumerTasks =
					trgtToConsumerTasksMap.get(trgt);
				List<String> consumerTaskNames =
					new ArraySet<String>(consumerTasks.size());
				for (ITask task : consumerTasks) {
					consumerTaskNames.add(getSourceName(task));
				}
				undefinedTarget(name, consumerTaskNames);
			} else if (size > 1) {
				List<String> producerTaskNames =
					new ArraySet<String>(producerTasks.size());
				for (ITask task : producerTasks) {
					producerTaskNames.add(getSourceName(task));
				}
				redefinedTarget(name, producerTaskNames);
			}
		}
		project.setNameToTaskMap(nameToTaskMap);
		project.setNameToTrgtMap(nameToTrgtMap);
		project.setTaskToConsumedTrgtsMap(taskToConsumedTrgtsMap);
		project.setTaskToProducedTrgtsMap(taskToProducedTrgtsMap);
		project.setTrgtToConsumerTasksMap(trgtToConsumerTasksMap);
		project.setTrgtToProducerTasksMap(trgtToProducerTasksMap);
		return project;
	}
	
	private void createTrgt(String name, Class type, String location) {
		TrgtInfo info = new TrgtInfo(type, location, null);
		createTrgt(name, info);
	}
	
	private void createTrgt(String name, Class type, String location,
			RelSign relSign) {
		for (String name2 : relSign.getDomKinds()) {
			createTrgt(name2, ProgramDom.class, location); 
		}
		TrgtInfo info = new TrgtInfo(type, location, relSign);
		createTrgt(name, info);
	}

	private void createTrgt(String name, TrgtInfo info) {
		Set<TrgtInfo> infos = nameToTrgtsDebugMap.get(name);
		if (infos == null) {
			infos = new HashSet<TrgtInfo>();
			nameToTrgtsDebugMap.put(name, infos);
		}
		infos.add(info);
	}

	private class TrgtInfo {
		public Class type;
		public final String location;
		public RelSign sign;
		public TrgtInfo(Class type, String location, RelSign sign) {
			this.type = type;
			this.location = location;
			this.sign = sign;
		}
	};

	private void buildDlogAnalysisMap() {
		String[] fileNames =
			dlogAnalysisPathName.split(File.pathSeparator);
		for (String fileName : fileNames) {
			File file = new File(fileName);
			if (!file.exists()) {
				nonexistentPathElem(fileName, "chord.dlog.analysis.path");
				continue;
			}
			processDlogAnalysis(file);
		}
	}

	private void buildJavaAnalysisMap() {
		ArrayList<URL> list = new ArrayList<URL>();
		String[] fileNames =
			javaAnalysisPathName.split(File.pathSeparator);
		for (String fileName : fileNames) {
			File file = new File(fileName);
			if (!file.exists()) {
				nonexistentPathElem(fileName, "chord.java.analysis.path");
				continue;
			}
			try {
               list.add(file.toURL());
            } catch (MalformedURLException ex) {
            	malformedPathElem(fileName, "chord.java.analysis.path",
            		ex.getMessage());
				continue;
           }
        }
		URL[] urls = new URL[list.size()];
		list.toArray(urls);
		AnnotationDB db = new AnnotationDB();
		try {
			db.scanArchives(urls);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
		Map<String, Set<String>> index = db.getAnnotationIndex();
		if (index == null)
			return;
		Set<String> classNames = index.get(Chord.class.getName());
		if (classNames == null)
			return;
		for (String className : classNames) {
			processJavaAnalysis(className);
		}
	}

    private final FilenameFilter filter = new FilenameFilter() {
		public boolean accept(File dir, String name) {
			if (name.startsWith("."))
				return false;
			return true;
		}
	};

	private void processDlogAnalysis(File file) {
		if (file.isDirectory()) {
			File[] subFiles = file.listFiles(filter);
			for (File subFile : subFiles)
				processDlogAnalysis(subFile);
			return;
		}
		String fileName = file.getAbsolutePath();
		if (!fileName.endsWith(".dlog") &&
				!fileName.endsWith(".datalog"))
			return;
		DlogTask task = new DlogTask();
		boolean ret = task.parse(fileName);
		if (!ret) {
			ignoreDlogAnalysis(fileName);
			return;
		}
		String name = task.getDlogName();
		if (name == null) {
			anonDlogAnalysis(fileName);
			name = fileName;
		}
		ITask task2 = nameToTaskMap.get(name);
		if (task2 != null) {
			redefinedDlogTask(fileName, name, getSourceName(task2));
			return;
		}
		for (String domName : task.getDomNames()) {
			createTrgt(domName, ProgramDom.class, fileName);
		}
		Map<String, RelSign> consumedRelsMap =
			task.getConsumedRels();
		for (Map.Entry<String, RelSign> e : consumedRelsMap.entrySet()) {
			String relName = e.getKey();
			RelSign relSign = e.getValue();
			createTrgt(relName, ProgramRel.class, fileName, relSign);
		}
		Map<String, RelSign> producedRelsMap =
			task.getProducedRels();
		for (Map.Entry<String, RelSign> e : producedRelsMap.entrySet()) {
			String relName = e.getKey();
			RelSign relSign = e.getValue();
			createTrgt(relName, ProgramRel.class, fileName, relSign);
		}
		taskToConsumedNamesMap.put(task,
			consumedRelsMap.keySet());
		taskToProducedNamesMap.put(task,
			producedRelsMap.keySet());
		task.setName(name);
		task.setProject(project);
		nameToTaskMap.put(name, task);
	}
	
	private void processJavaAnalysis(String className) {
		Class type;
		try {
			type = Class.forName(className);
		} catch (ClassNotFoundException ex) {
			throw new RuntimeException(ex);
		}
		ChordAnnotParser info = new ChordAnnotParser(type);
		boolean ret = info.parse();
		if (!ret) {
			ignoreJavaAnalysis(className);
			return;
		}
		String name = info.getName();
		if (name.equals("")) {
			anonJavaAnalysis(className);
			name = className;
		}
		ITask task = nameToTaskMap.get(name);
		if (task != null) {
			redefinedJavaTask(className, name, getSourceName(task));
			return;
		}
		try {
			task = instantiate(className, ITask.class);
		} catch (RuntimeException ex) {
			nonInstantiableJavaAnalysis(className, ex.getMessage());
			return;
		}
		Map<String, Class  > nameToTypeMap = info.getNameToTypeMap();
		Map<String, RelSign> nameToSignMap = info.getNameToSignMap();
		for (Map.Entry<String, Class> e : nameToTypeMap.entrySet()) {
			String name2 = e.getKey();
			Class type2 = e.getValue();
			RelSign sign2 = nameToSignMap.get(name2);
			if (sign2 != null)
				createTrgt(name2, type2, className, sign2);
			else
				createTrgt(name2, type2, className);
		}
		for (Map.Entry<String, RelSign> e :
				nameToSignMap.entrySet()) {
			String name2 = e.getKey();
			if (nameToTypeMap.containsKey(name2))
				continue;
			RelSign sign2 = e.getValue();
			createTrgt(name2, ProgramRel.class, className, sign2);
		}
		Set<String> consumedNames = info.getConsumedNames();
		Set<String> producedNames = info.getProducedNames();
		taskToConsumedNamesMap.put(task, consumedNames);
		taskToProducedNamesMap.put(task, producedNames);
		for (String consumedName : consumedNames) {
			if (!nameToTypeMap.containsKey(consumedName) &&
				!nameToSignMap.containsKey(consumedName)) {
				createTrgt(consumedName, null, className);
			}
		}
		for (String producedName : producedNames) {
			if (!nameToTypeMap.containsKey(producedName) &&
				!nameToSignMap.containsKey(producedName)) {
				createTrgt(producedName, null, className);
			}
		}
		task.setName(name);
		task.setProject(project);
		nameToTaskMap.put(name, task);
	}

	// create an instance of the class named className and cast
	// it to the class named clazz
	private <T> T instantiate(String className, Class<T> clazz) {
		try {
			Object obj = Class.forName(className).newInstance();
			return clazz.cast(obj);
		} catch (InstantiationException e) {
			throw new RuntimeException(
				"Class '" + className + "' cannot be instantiated.");
		} catch (IllegalAccessException e) {
			throw new RuntimeException(
				"Class '" + className + "' cannot be accessed.");
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(
				"Class '" + className + "' not found.");
		} catch (ClassCastException e) {
			throw new RuntimeException(
				"Class '" + className + "' must be a subclass of " +
				clazz.getName() + ".");
		}
	}
	
	private String getSourceName(ITask analysis) {
		Class clazz = analysis.getClass();
		if (clazz == DlogTask.class)
			return ((DlogTask) analysis).getFileName();
		return clazz.getName();
	}
	
	private void anonJavaAnalysis(String name) {
		System.err.println("WARNING: Java task '" + name +
			"' is not named via a @Chord(name=\"...\") annotation; " +
			"using its classname itself as its name.");
	}
	
	private void anonDlogAnalysis(String name) {
		System.err.println("WARNING: Dlog task '" + name +
			"' is not named via a # name=... line; " +
			"using its filename itself as its name.");
	}

	private void ignoreDlogAnalysis(String name) {
		System.err.println("ERROR: Ignoring Dlog task '" + name +
			"': Errors were reported while parsing it (see above).");
		hasNoErrors = false;
	}
	
	private void ignoreJavaAnalysis(String name) {
		System.err.println("ERROR: Ignoring Java task '" + name +
			"': Its @Chord annotation had errors (see above).");
		hasNoErrors = false;
	}
	
	private void undefinedDom(String domName, String relName) {
		System.err.println("ERROR: '" + domName +
			"' declared as a domain of relation '" + relName +
			"' not declared as a produced name of any task.");
		hasNoErrors = false;
	}
	
	private void illtypedDom(String domName, String relName,
			String type) {
		System.err.println("ERROR: '" + domName +
			"' declared as a domain of relation '" + relName +
			"' has type '" + type + "' which is not a subclass of '" +
			ProgramDom.class.getName() + "'.");
		hasNoErrors = false;
	}
	
	private void undefinedTarget(String name,
			List<String> consumerTaskNames) {
		System.err.println("WARNING: '" + name +
			"' not declared as produced name of any task; " +
			"declared as consumed name of following tasks:");
		for (String taskName : consumerTaskNames) {
			System.err.println("\t'" + taskName + "'");
		}
	}
	
	private void redefinedTarget(String name,
			List<String> producerTaskNames) {
		System.err.println("WARNING: '" + name +
			"' declared as produced name of multiple tasks:");
		for (String taskName : producerTaskNames) {
			System.err.println("\t'" + taskName + "'");
		}
	}
	
	private void inconsistentDomNames(String relName, String names1,
			String names2, String loc1, String loc2) {
		System.err.println("ERROR: Relation '" + relName +
			"' declared with different domain names '" + names1 +
			"' and '" + names2 + "' at '" + loc1 + "' and '" + loc2 +
			"' respectively");
		hasNoErrors = false;
	}
	
	private void inconsistentDomOrders(String relName, String order1,
			String order2, String loc1, String loc2) {
		System.err.println("WARNING: Relation '" + relName +
			"' declared with different domain orders '" + order1 +
			"' and '" + order2 + "' at '" + loc1 + "' and '" + loc2 +
			"' respectively");
	}
	
	private void inconsistentTypes(String name, String type1,
			String type2, String loc1, String loc2) {
		System.err.println("ERROR: '" + name +
			"' declared with inconsistent types '" + type1 +
			"' and '" + type2 + "' at '" + loc1 + "' and '" + loc2 +
			"' respectively");
		hasNoErrors = false;
	}
	
	private void unknownSign(String name) {
		System.err.println("ERROR: sign of relation '" + name +
			"' unknown");
		hasNoErrors = false;
	}
	
	private void unknownOrder(String name) {
		System.err.println("ERROR: order of relation '" + name +
			"' unknown");
		hasNoErrors = false;
	}
	
	private void unknownType(String name) {
		System.err.println("ERROR: type of target '" + name +
			"' unknown");
		hasNoErrors = false;
	}
	
	private void redefinedJavaTask(String newTaskName, String name,
			String oldTaskName) {
		System.err.println("ERROR: Ignoring Java task '" +
			newTaskName +
			"': its @Chord(name=\"...\") annotation uses name '" +
			name + "' that is also used for another task '" +
			oldTaskName + "'");
		hasNoErrors = false;
	}
	private void redefinedDlogTask(String newTaskName, String name,
			String oldTaskName) {
		System.err.println("ERROR: Ignoring Dlog task '" +
			newTaskName +
			"': its # name=\"...\" line uses name '" +
			name + "' that is also used for another task '" +
			oldTaskName + "'");
		hasNoErrors = false;
	}
	
	private void malformedPathElem(String elem, String path,
			String msg) {
		System.err.println("WARNING: Ignoring malformed entry '" +
			elem + "' in " + path + ": " + msg);
	}
	
	private void nonexistentPathElem(String elem, String path) {
		System.err.println("WARNING: Ignoring non-existent entry '" +
			elem + "' in " + path);
	}
	
	private void nonInstantiableJavaAnalysis(String name, String msg) {
		System.err.println("ERROR: Ignoring Java analysis task '" +
			name + "': " + msg);
		hasNoErrors = false;
	}
}
