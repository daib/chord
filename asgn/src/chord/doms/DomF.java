/*
 * Copyright (c) 2006-07, The Trustees of Stanford University.  All
 * rights reserved.
 * Licensed under the terms of the GNU GPL; see COPYING for details.
 */
package chord.doms;

import java.util.Set;

import chord.program.Field;
import chord.program.Type;
import chord.program.Program;
import chord.project.Chord;
import chord.project.ProgramDom;

/**
 * Domain of fields.
 * <p>
 * The 0th element in this domain denotes a distinguished hypothetical
 * field denoted <tt>arrayElem</tt> that is regarded as accessed
 * whenever an array element is accessed.
 * 
 * @author Mayur Naik (mhn@cs.stanford.edu)
 */
@Chord(
	name = "F"
)
public class DomF extends ProgramDom<Field> {
	public void fill() {
		Program program = project.getProgram();
		// Reserve index 0 for the distinguished hypothetical field 
		// representing all array elements
		set(null);
		for (Type type : program.getTypes()) {
			for (Field field : type.getFields()) {
				set(field);
			}
		}
	}

	public String toXMLAttrsString(Field field) {
		String sign;
		String file;
		int line;
		if (field == null) {
			sign = "[*]";
			file = "";
			line = 0;
		} else {
			Type type = field.getCtnrType();
			sign = type.getName() + "." + field.getName();
			file = type.getFileName();
			line = field.getLineNum();
		}
        return "sign=\"" + sign +
            "\" file=\"" + file +
            "\" line=\"" + line + "\"";
	}
}
