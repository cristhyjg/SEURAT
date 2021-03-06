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

/*
 * Constraint class
 */

package edu.wpi.cs.jburge.SEURAT.rationaleData;


import instrumentation.DataLog;

import java.util.*;
import java.io.*;

import java.sql.Connection; 
import java.sql.SQLException; 
import java.sql.Statement;
import java.sql.ResultSet;

import org.eclipse.swt.widgets.Display;

import SEURAT.events.RationaleElementUpdateEventGenerator;
import SEURAT.events.RationaleUpdateEvent;


import edu.wpi.cs.jburge.SEURAT.editors.EditConstraint;
import edu.wpi.cs.jburge.SEURAT.editors.SelectOntEntry;
import edu.wpi.cs.jburge.SEURAT.rationaleData.RationaleDBUtil;

/**
 * Defines the contents of a Constraint Rationale Element
 * @author burgeje
 *
 */
public class Constraint extends RationaleElement implements Serializable 
{
	// class variables
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2521402923823030837L;
	
	// instance variables
	/**
	 * The number of children of this constraint
	 */
	int nRefs;
	
	/**
	 * The constraint type. Currently not used and left here for future
	 * expansion.
	 */
	String type; //future expansion
	/**
	 * The amount of the constraint (how much the item is constrained by or to)
	 */
	float amount;
	/**
	 * The units (pounds, minutes, degrees celsius, etc.)
	 */
	String units;
	/**
	 * The component being affected by the constraint
	 */
	DesignProductEntry component;
	/**
	 * The ontology entries that relate to this constraint.
	 */
	Vector<OntEntry> ontEntries;
	
	//still need parents and children since constraints are in a hierarchy
	/**
	 * What level the constraint is in the hierarchy.
	 */
	int level;
	/**
	 * Who our parents are (the constraint can occur in multiple places)
	 */
	Vector<Constraint> parents;
	/**
	 * Who our children are 
	 */
	Vector<Constraint> children;

	private RationaleElementUpdateEventGenerator<Constraint> m_eventGenerator = 
		new RationaleElementUpdateEventGenerator<Constraint>(this);
	/**
	 * Constructor called from the XML parsing code.
	 */
	public Constraint()
	{
		super();
		nRefs = 0;
		children = new Vector<Constraint>();
		parents = new Vector<Constraint>();	
		ontEntries = new Vector<OntEntry>();
		units = new String("");
		type = new String("");
		component = null;
	}
	
	/**
	 * Constructor
	 * @param cName - the constraint name
	 * @param parnt - the constraint parent
	 */
	public Constraint(String cName, Constraint parnt)
	{
		super();
		nRefs = 0;
		children = new Vector<Constraint>();
		parents = new Vector<Constraint>();
		parents.addElement(parnt);
		name = cName;
		ontEntries = new Vector<OntEntry>();
		units = new String("");
	} 
	
	public RationaleElementType getElementType()
	{
		return RationaleElementType.CONSTRAINT;
	}
	
	/**
	 * Add a new child to the constraint.
	 * @param cName -the name of the new child
	 * @return the new child constraint
	 */
	public Constraint addChild(String cName)
	{
		Constraint newEntry = new Constraint(cName, this);
		newEntry.setLevel(level + 1);
		children.addElement(newEntry);
		this.incRefs();
		return newEntry;
	}
	
	public void addChild(Constraint child)
	{
		children.addElement(child);
		this.incRefs();
		child.addParent(this);
	}
	
	public void removeChild(Constraint child)
	{
		children.removeElement(child);
		this.decRefs();
	}
	
	public void addParent(Constraint parnt)
	{
		parents.addElement(parnt);
	}
	
	public void addOntEntry(OntEntry entry)
	{
		ontEntries.addElement(entry);
		entry.incRefs();
	}
	
	public void removeOntEntry(OntEntry entry)
	{
		ontEntries.removeElement(entry);
		entry.decRefs();
	}
	
	public float getAmount()
	{
		return amount;
	}
	
	public void setAmount(float amount)
	{
		this.amount = amount;
	}
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public String getUnits() {
		return units;
	}
	
	public void setUnits(String units) {
		this.units = units;
	}
	
	public int getLevel()
	{
		return level;
	}
	
	public void setLevel(int lev)
	{
		level = lev;
	}
	
	public Vector getChildren()
	{
		return children;
	}
	
	public Vector getParents()
	{
		return parents;
	}
	
	public void incRefs()
	{
		nRefs += 1;
	}
	
	public void decRefs()
	{
		nRefs -=1;
	}
	
	public int getRefs()
	{
		return nRefs;
	}
	
	
	/**
	 * Save the constraint to the database
	 * @param pid - the parent ID (from the constraint hierarchy)
	 * @return the ID
	 */
	public int toDatabase(int pid)
	{
		RationaleDB db = RationaleDB.getHandle();
		Connection conn = db.getConnection();
		
		int ourid = 0;

		// Update Event To Inform Subscribers Of Changes
		// To Rationale
		RationaleUpdateEvent l_updateEvent;
		
		//find out if this requirement is already in the database
		Statement stmt = null; 
		ResultSet rs = null; 
		String findQuery = "";
		
//		System.out.println("Saving to the database");
		
		try {
			stmt = conn.createStatement(); 
			if (this.id >= 0)
			{
				
				//now, update it with the new information
				findQuery = "UPDATE " + RationaleDBUtil.escapeTableName("CONSTRAINTS") + " " +
				"SET name = '" +
				RationaleDBUtil.escape(this.name) + "', " +
				"description = '" +
				RationaleDBUtil.escape(this.description) + "', " +
				"type = '" +
				RationaleDBUtil.escape(this.type) + "', " +
				"units = '" +
				RationaleDBUtil.escape(this.units) + "', " +
				"subsys = " + this.component.getID() + ", " +
				"amount = " + 
				this.amount +
				" WHERE " +
				"id = " + this.id + " " ;
				System.out.println(findQuery);
				stmt.execute(findQuery);
				
				l_updateEvent = m_eventGenerator.MakeUpdated();
			}
			else 
			{
				
				//now, we have determined that the ontolgy entry is new
				
				findQuery = "INSERT INTO " + RationaleDBUtil.escapeTableName("CONSTRAINTS") + " " +
				"(name, description, type, units, subsys, amount) " +
				"VALUES ('" +
				RationaleDBUtil.escape(this.name) + "', '" +
				RationaleDBUtil.escape(this.description) + "', '" +
				RationaleDBUtil.escape(this.type) + "', '" +
				RationaleDBUtil.escape(this.units) + "', " +
				this.component.getID() + ", " +
				this.amount + ")"; 
				
				System.out.println(findQuery);
				stmt.execute(findQuery); 
				
				l_updateEvent = m_eventGenerator.MakeCreated();				
			}
			//now, we need to get our ID
			findQuery = "SELECT id FROM " + 
				RationaleDBUtil.escapeTableName("CONSTRAINTS") + " " + 
				" where name='" + this.name + "'";
			rs = stmt.executeQuery(findQuery); 
//			***			System.out.println(findQuery);
			
			if (rs.next())
			{
				ourid = rs.getInt("id");
				rs.close();
			}
			else
			{
				ourid = -1;
			}
			
			this.id = ourid;
			
			//need to update our relationships with the OntEntries
			Enumeration ontkids = this.ontEntries.elements();
			while (ontkids.hasMoreElements())
			{
				OntEntry kid = (OntEntry) ontkids.nextElement();
				//if the parent ID is not zero, then update the parent-child relationship
				
				findQuery = "SELECT * from OntConRel WHERE " +
				"ontEntry = " + new Integer(kid.getID()).toString() +
				" and constr = " + new Integer(ourid).toString();
				System.out.println(findQuery);
				rs = stmt.executeQuery(findQuery);
				if (rs.next())
				{
					rs.close();
				}
				else
				{
					findQuery = "INSERT INTO OntConRel (ontEntry, constr) " +
					"VALUES (" +
					new Integer(kid.getID()).toString() + ", " +
					new Integer(ourid).toString() + ")";
					stmt.execute(findQuery);
				}
				kid.toDatabase(ourid);
			} //checking parent
			
			
			//if the parent ID is not zero, then update the parent-child relationship
			if (pid > 0)
			{
				findQuery = "SELECT * from ConstraintRelationships WHERE " +
				"parent = " + new Integer(pid).toString() +
				" and child = " + new Integer(ourid).toString();
				rs = stmt.executeQuery(findQuery);
				if (rs.next())
				{
					rs.close();
				}
				else
				{
					findQuery = "INSERT INTO ConstraintRelationships (parent, child) " +
					"VALUES (" +
					new Integer(pid).toString() + ", " +
					new Integer(ourid).toString() + ")";
//					***					System.out.println(insertRel);
					stmt.execute(findQuery);
				}
			} //checking parent
			
			//now, decode our children
			Enumeration kids = children.elements();
			while (kids.hasMoreElements())
			{
				Constraint kid = (Constraint) kids.nextElement();
				kid.toDatabase(ourid);
			}
			
			m_eventGenerator.Broadcast(l_updateEvent);
		} catch (SQLException ex) {
			// handle any errors 
			RationaleDB.reportError(ex, "Writing Constraint to DB", findQuery);
		}
		
		finally { 
			RationaleDB.releaseResources(stmt, rs);
		}
		
		return ourid;	
		
	}	
	
	/**
	 * Read in a constraint from the database
	 * @param id - the unique id
	 */
	public void fromDatabase(int id)
	{
		
		RationaleDB db = RationaleDB.getHandle();
		Connection conn = db.getConnection();
		String findQuery = "";
		this.id = id;
		
		Statement stmt = null; 
		ResultSet rs = null; 
		try {
			stmt = conn.createStatement();
			
			findQuery = "SELECT *  FROM " +
			RationaleDBUtil.escapeTableName("CONSTRAINTS") + " " +
			"where id = " +
			new Integer(id).toString();
//			***			System.out.println(findQuery);
			rs = stmt.executeQuery(findQuery);
			
			if (rs.next())
			{
				name = RationaleDBUtil.decode(rs.getString("name"));
				rs.close();
				this.fromDatabase(name);
			}
			
		} catch (SQLException ex) {
			// handle any errors 
			RationaleDB.reportError(ex, "writing Constraint to DB", findQuery);
		}
		finally { 
			RationaleDB.releaseResources(stmt, rs);
		}
		
	}		
	
	/**
	 * Read in the constraint from the database, given its name
	 * @param name - the constraint name
	 */
	public void fromDatabase(String name)
	{
		
		RationaleDB db = RationaleDB.getHandle();
		Connection conn = db.getConnection();
		
		//if this is coming from a textual claim descriptor we will want to 
		//strip out the "IS" and "NOT" from the name.
		
		
		
//		***		System.out.println("ont name = " + name);
		
		this.name = name;
		name = RationaleDBUtil.escape(name);
		
		String findQuery = "";
		Statement stmt = null; 
		ResultSet rs = null; 
		try {
			stmt = conn.createStatement();
			findQuery = "SELECT *  FROM " +
			RationaleDBUtil.escapeTableName("CONSTRAINTS") + 
			" where name = '" + name + "'";
//			***			System.out.println(findQuery);
			rs = stmt.executeQuery(findQuery);
			
			if (rs.next())
			{
				id = rs.getInt("id");
				description = RationaleDBUtil.decode(rs.getString("description"));
				amount = rs.getFloat("amount");
				String tmp = rs.getString("units");
				if (tmp != null)
				{
					units = RationaleDBUtil.decode(tmp);
				}
				
				component = new DesignProductEntry();
				component.fromDatabase(rs.getInt("subsys"));
				
				//recursively get our ontology entries too
				findQuery = "SELECT * from OntConRel WHERE " +
				"conStr = " + new Integer(id).toString();
				
				rs = stmt.executeQuery(findQuery);
				if (rs != null)
				{
					while (rs.next())
					{
						int ontID = rs.getInt("ontEntry");
						OntEntry ontology = new OntEntry();
						ontology.fromDatabase(ontID);
						this.addOntEntry(ontology);
					}
					rs.close();
				}
				
				rs.close();	
			}
			
		} catch (SQLException ex) {
			// handle any errors 
			RationaleDB.reportError(ex, "write Constraint to DB 2", findQuery);
		}
		finally { 
			RationaleDB.releaseResources(stmt, rs);
			
		}
		
	}	
	
	/*	public boolean display()
	 {
	 Frame lf = new Frame();
	 ConstraintGUI ar = new ConstraintGUI(lf, this, null, false);
	 ar.show();
	 return ar.getCanceled();
	 } */
	
	/**
	 * Display the constraint in the editor
	 * @param disp - points back to the display
	 * @return true if the user cancels
	 */
	public boolean display(Display disp)
	{
		EditConstraint ar = new EditConstraint(disp, this, null, false);
		String msg = "Edited Constraint " + this.getName() + " " + ar.getCanceled();
		DataLog d = DataLog.getHandle();
		d.writeData(msg);
		return ar.getCanceled(); //can I do this?
		
	}
	
	/**
	 * Create a new constraint by bringing up the editor
	 * @param disp - points to the display
	 * @param parent - the parent constraint
	 * @return true if the user cancels
	 */
	public boolean create(Display disp, RationaleElement parent)
	{
		System.out.println("create Constraint");
		System.out.println("id = " + parent.getID());
		this.addParent((Constraint) parent);
		
		EditConstraint ar = new EditConstraint(disp, this, (Constraint) parent, true);
		System.out.println("name in create = " + this.getName());
		return ar.getCanceled(); //can I do this?
	}
	
	/**
	 * Used to associate an ontology entry with the constraint. This brings
	 * up the selection display so the user can choose the entry
	 * @param disp - points back to the display
	 */
	public RationaleElement associateElement(Display disp)
	{
		OntEntry newOnt = null;
		SelectOntEntry ar = new SelectOntEntry(disp, true);
		newOnt = ar.getSelOntEntry();
		if (newOnt != null)
		{
			addOntEntry(newOnt);
			this.toDatabase(0);
		}
		return newOnt;
	}
	
	/**
	 * Currently doesn't do anything.
	 */
	public Vector<RationaleStatus> updateStatus()
	{
		//need to replace with real content!
		return null;
	}
	
	public DesignProductEntry getComponent() {
		return component;
	}
	
	public void setComponent(DesignProductEntry component) {
		this.component = component;
	}
	
	public Vector getOntEntries() {
		return ontEntries;
	}
	
}
