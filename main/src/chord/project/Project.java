/*
 * Copyright (c) 2006-07, The Trustees of Stanford University.  All
 * rights reserved.
 * Licensed under the terms of the GNU GPL; see COPYING for details.
 */
package chord.project;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import chord.util.Timer;
import chord.program.Program;

/**
 * A project.
 * 
 * It encapsulates a Java program to be analyzed along with
 * program analyses to be performed on it.
 *
 * @author Mayur Naik (mhn@cs.stanford.edu)
 */
public class Project {
	private Program program;
	private Map<String, ITask> nameToTaskMap;
	private Map<String, Object> nameToTrgtMap;
	private Map<ITask, Set<Object>> taskToProducedTrgtsMap;
	private Map<ITask, Set<Object>> taskToConsumedTrgtsMap;
	private Map<Object, Set<ITask>> trgtToProducerTasksMap;
	private Map<Object, Set<ITask>> trgtToConsumerTasksMap;
	private Set<ITask> doneTasks = new HashSet<ITask>();
	private Set<Object> doneTrgts = new HashSet<Object>();

	public void setProgram(Program program) {
		this.program = program;
	}
	public Program getProgram() {
		return program;
	}
	public void setNameToTaskMap(Map<String, ITask> map) {
		nameToTaskMap = map;
	}
	public void setNameToTrgtMap(Map<String, Object> map) {
		nameToTrgtMap = map;
	}
	public void setTaskToProducedTrgtsMap(Map<ITask, Set<Object>> map) {
		taskToProducedTrgtsMap = map;
	}
	public void setTaskToConsumedTrgtsMap(Map<ITask, Set<Object>> map) {
		taskToConsumedTrgtsMap = map;
	}
	public void setTrgtToProducerTasksMap(Map<Object, Set<ITask>> map) {
		trgtToProducerTasksMap = map;
	}
	public void setTrgtToConsumerTasksMap(Map<Object, Set<ITask>> map) {
		trgtToConsumerTasksMap = map;
	}
	public Object getTrgt(String name) {
		Object trgt = nameToTrgtMap.get(name);
		if (trgt == null) {
			throw new RuntimeException("Trgt '" + name +
				"' not found.");
		}
		return trgt;
	}
	public ITask getTask(String name) {
		ITask task = nameToTaskMap.get(name);
		if (task == null) {
			throw new RuntimeException("Task '" + name +
				"' not found.");
		}
		return task;
	}
	public void runTask(ITask task) {
		System.out.print("ENTER: " + task);
		if (isTaskDone(task)) {
			System.out.println(" ALREADY DONE.");
			return;
		}
		System.out.println("");
		for (Object trgt : taskToConsumedTrgtsMap.get(task)) {
			if (isTrgtDone(trgt))
				continue;
			Set<ITask> tasks = trgtToProducerTasksMap.get(trgt);
			if (tasks.size() != 1) {
				throw new RuntimeException("Task producing trgt '" +
					trgt + "' consumed by task '" + task +
					"' not found");
			}
			ITask task2 = tasks.iterator().next();
			runTask(task2);
		}
        Timer timer = new Timer(task.getName());
		System.out.println("START: " + task);
        timer.init();
		task.run();
        timer.done();
        System.out.println("LEAVE: " + task +
            " time: " + timer.getExecTimeStr());
		setTaskDone(task);
		for (Object trgt : taskToProducedTrgtsMap.get(task)) {
			setTrgtDone(trgt);
		}
	}
	public ITask runTask(String name) {
		ITask task = getTask(name);
		runTask(task);
		return task;
	}
	public boolean isTrgtDone(Object trgt) {
		return doneTrgts.contains(trgt);
	}
	public boolean isTrgtDone(String name) {
		return isTrgtDone(getTrgt(name));
	}
	public void setTrgtDone(Object trgt) {
		doneTrgts.add(trgt);
	}
	public void setTrgtDone(String name) {
		setTrgtDone(getTrgt(name));
	}
	public void resetTrgtDone(Object trgt) {
		if (doneTrgts.remove(trgt)) {
			for (ITask task : trgtToConsumerTasksMap.get(trgt)) {
				resetTaskDone(task);
			}
		}
	}
	public void resetAll() {
		doneTrgts.clear();
		doneTasks.clear();
	}
	public void resetTrgtDone(String name) {
		resetTrgtDone(getTrgt(name));
	}
	public boolean isTaskDone(ITask task) {
		return doneTasks.contains(task);
	}
	public boolean isTaskDone(String name) {
		return isTaskDone(getTask(name));
	}
	public void setTaskDone(ITask task) {
		doneTasks.add(task);
	}
	public void setTaskDone(String name) {
		setTaskDone(getTask(name));
	}
	public void resetTaskDone(ITask task) {
		if (doneTasks.remove(task)) {
			for (Object trgt : taskToProducedTrgtsMap.get(task)) {
				resetTrgtDone(trgt);
			}
		}
	}
	public void resetTaskDone(String name) {
		resetTaskDone(getTask(name));
	}
}
