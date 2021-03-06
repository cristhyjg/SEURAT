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

package edu.wpi.cs.jburge.SEURAT.tasks;

import java.util.*;

import edu.wpi.cs.jburge.SEURAT.rationaleData.*;


/**
 * Defines the rationale task to be displayed in the task list
 * @author burgeje
 *
 */
public class RationaleTask {
	
	/**
	 * The ID of our parent
	 */
	private int pid;
	/**
	 * The error level (error, warning, etc.)
	 */
	private RationaleErrorLevel status = RationaleErrorLevel.INFORMATION;
	/**
	 * A text description of the specific error (error message)
	 */
	private String description 	= "";
	/**
	 * The type of element the error applies to
	 */
	private RationaleElementType type = RationaleElementType.RATIONALE;
	/**
	 * The name of the element the error is about
	 */
	private String rationale = "";  
	/**
	 * The date when the error occured
	 */
	private Date dateStamp;
	/**
	 * The type of error
	 */
	private RationaleStatusType stype;
	
	
	public RationaleTask() {
		
		super();
	}
	
	/**
	 * Create a new task
	 * @param parent - the element the error is about
	 * @param tstatus - the error level
	 * @param tdescription - the error message
	 * @param ttype - the type of element it is referring to
	 * @param trationale - the element name
	 * @param date - when it occured
	 * @param sttype - the status type
	 */
	public RationaleTask(int parent, RationaleErrorLevel tstatus, String tdescription, RationaleElementType ttype,
			String trationale, Date date, RationaleStatusType sttype)
	{
		super();
		this.pid = parent;
		setDescription(tdescription);
		setStatus(tstatus);
		setRationaleType(ttype);
		setRationale(trationale);
		setDateStamp(date);
		this.stype = sttype;
	}
	/**
	 * @return true if error, false otherwise
	 */
	public boolean isError() {
		return (status == RationaleErrorLevel.ERROR);
	}
	public boolean isWarning() {
		return (status == RationaleErrorLevel.WARNING);
	}
	public boolean isInformation() {
		return (status == RationaleErrorLevel.INFORMATION);
	}
	
	public RationaleErrorLevel getStatus() {
		return status;
	}
	
	public RationaleStatusType getStatusType() 
	{
		return stype;
	}
	
	
	public String getDescription() {
		return description;
	}
	
	public String getRationale() {
		return rationale;
	} 
	
	public String getRationaleType() {
		return type.toString();
	}
	
	public int getParent() {
		return pid;
	}
	
	public void setStatus(RationaleErrorLevel stat) {
		status = stat;
	}
	
	public void setDescription(String string) {
		description = string;
	}
	
	public void setRationale(String rat) {
		rationale = rat;
	}
	
	public void setRationaleType(RationaleElementType rat) {
		type = rat;
	}
	
	public Date getDateStamp() {
		return dateStamp;
	}
	
	public void setDateStamp(Date dateStamp) {
		this.dateStamp = dateStamp;
	}
	
	
	
}
