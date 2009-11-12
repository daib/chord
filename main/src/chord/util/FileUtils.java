/*
 * Copyright (c) 2006-07, The Trustees of Stanford University.  All
 * rights reserved.
 * Licensed under the terms of the GNU GPL; see COPYING for details.
 */
package chord.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;

/**
 * File related utilities.
 *  
 * @author Mayur Naik (mhn@cs.stanford.edu)
 */
public class FileUtils {
	private static String chordHomeDirName =
		PropertyUtils.getStrProperty("chord.home.dir");
	public static String getFileNameOfResource(String resourceName) {
		if (chordHomeDirName == null) {
			throw new RuntimeException("System property chord.home must " +
				"specify the location of Chord's root directory.");
		}
		File file = new File(chordHomeDirName, resourceName);
		if (!file.exists()) {
			throw new RuntimeException(
				"File named '" + resourceName +
				"' does not exist under Chord's root directory '" +
				chordHomeDirName + "'.");
		}
		return file.getAbsolutePath();
	}

	public static void copy(String fromFileName, String toFileName) {
		try {
			FileInputStream fis = new FileInputStream(fromFileName);
			FileOutputStream fos = new FileOutputStream(toFileName);
			byte[] buf = new byte[1024];
			int i = 0;
			while((i = fis.read(buf)) != -1) {
				fos.write(buf, 0, i);
			}
			fis.close();
			fos.close();
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
	public static boolean mkdir(String dirName) {
		return mkdir(new File(dirName));
	}
	public static boolean mkdir(String parentName, String childName) {
		return mkdir(new File(parentName, childName));
	}
	public static boolean mkdir(File file) {
		if (file.exists()) {
			assertIsDir(file);
			return false;
		}
		if (file.mkdirs())
			return true;
		throw new RuntimeException("Failed to create directory '" +
			file + "'"); 
	}
	public static void assertExists(File file) {
		if (!file.exists()) {
			throw new RuntimeException(
				"File '" + file + "' does not exist.");
		}
	}
	public static boolean checkExists(File file) {
		if (!file.exists()) {
			System.err.println("WARNING: " +
				"File '" + file + "' does not exist.");
			return false;
		}
		return true;
	}
	public static void assertIsDir(File file) {
		if (!file.isDirectory()) {
			throw new RuntimeException(
				"File '" + file + "' is not a directory.");
		}
	}
	public static boolean checkIsDir(File file) {
		if (!file.isDirectory()) {
			System.err.println("WARNING: " +
				"File '" + file + "' is not a directory.");
			return false;
		}
		return true;
	}
	public static void assertWritable(File file) {
		if (!file.canWrite()) {
			throw new RuntimeException(
				"File '" + file + "' is not writable.");
		}
	}
	public static boolean checkWritable(File file) {
		if (!file.canWrite()) {
			System.err.println("WARNING: " +
				"File '" + file + "' is not writable.");
			return false;
		}
		return true;
	}
	public static void assertReadable(File file) {
		if (!file.canRead()) {
			throw new RuntimeException(
				"File '" + file + "' is not readable.");
		}
	}
	public static boolean checkReadable(File file) {
		if (!file.canRead()) {
			System.err.println("WARNING: " +
				"File '" + file + "' is not readable.");
			return false;
		}
		return true;
	}
	public static void readWarning(String fileName) {
		System.err.println( "WARNING: " +
			"I/O error while reading file '" + fileName + "'");
	}
	public static void openWarning(String fileName) {
		System.err.println("WARNING: " +
			"I/O error while opening file '" + fileName + "'");
	}
	public static void closeWarning(String fileName) {
		System.err.println("WARNING: " +
			"I/O error while closing file '" + fileName + "'");
	}
	public static PrintWriter newPrintWriter(String fileName) {
		PrintWriter out;
		try {
			out = new PrintWriter(new File(fileName));
		} catch (FileNotFoundException ex) {
			throw new RuntimeException(ex);
		}
		return out;
	}
	public static PrintWriter newPrintWriter(File parentName,
			String childName) {
		PrintWriter out;
		try {
			out = new PrintWriter(new File(parentName, childName));
		} catch (FileNotFoundException ex) {
			throw new RuntimeException(ex);
		}
		return out;
	}
	public static PrintWriter newPrintWriter(String parentName,
			String childName) {
		PrintWriter out;
		try {
			out = new PrintWriter(new File(parentName, childName));
		} catch (FileNotFoundException ex) {
			throw new RuntimeException(ex);
		}
		return out;
	}
}
