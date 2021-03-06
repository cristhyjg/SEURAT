/*	This code belongs to the SEURAT project as written by Dr. Janet Burge
    Copyright (C) 2013  Janet Burge

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>. */

package edu.wpi.cs.jburge.SEURAT.rationaleData;

import java.util.*;
import java.io.*;

/**
 * This is an enumerated type that defines different levels of errors
 * that can be detected in the rationale.
 * @author burgeje
 *
 */
public final class RationaleErrorLevel implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 9116911030973964794L;
	private String id;
	public final int ord;
	private RationaleErrorLevel prev;
	private RationaleErrorLevel next;
	
	private static int upperBound = 0;
	private static RationaleErrorLevel first = null;
	private static RationaleErrorLevel last = null;
	
	private RationaleErrorLevel(String anID) {
		this.id = anID;
		this.ord = upperBound++;
		if (first == null) first = this;
		if (last != null) {
			this.prev = last;
			last.next = this;
		}
		last = this;
	}
	public static Enumeration elements() {
		return new Enumeration() {
			private RationaleErrorLevel curr = first;
			public boolean hasMoreElements() {
				return curr != null;
			}
			public Object nextElement() {
				RationaleErrorLevel c = curr;
				curr = curr.next();
				return c;
			}
		};
	}
	public String toString() {return this.id; }
	public static int size() { return upperBound; }
	public static RationaleErrorLevel first() { return first; }
	public static RationaleErrorLevel last()  { return last;  }
	public RationaleErrorLevel prev()  { return this.prev; }
	public RationaleErrorLevel next()  { return this.next; }
	
	public static RationaleErrorLevel fromString(String rt)
	{
		Enumeration ourEnum = elements();
		while (ourEnum.hasMoreElements())
		{
			RationaleErrorLevel rtE = (RationaleErrorLevel) ourEnum.nextElement();
			if (rt.compareTo(rtE.toString()) == 0)
			{
				return rtE;
			}
		}
		return null;
	}
	/**
	 * No error - all is well
	 */
	public static final RationaleErrorLevel NONE = new 
	RationaleErrorLevel("None");
	/**
	 * This is just an informational message
	 */
	public static final RationaleErrorLevel INFORMATION = new 
	RationaleErrorLevel("Information");
	/**
	 * Warning - something isn't quite right but may not be a 
	 * serious problem. 
	 */
	public static final RationaleErrorLevel WARNING = new 
	RationaleErrorLevel("Warning");
	/**
	 * Error - this is something that needs to be resolved
	 */
	public static final RationaleErrorLevel ERROR = new 
	RationaleErrorLevel("Error");
	
	
}
