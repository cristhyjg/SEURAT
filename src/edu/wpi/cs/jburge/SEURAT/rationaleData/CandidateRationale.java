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

import instrumentation.DataLog;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import edu.wpi.cs.jburge.SEURAT.editors.ConsistencyChecker;
import edu.wpi.cs.jburge.SEURAT.editors.EditCandidateRationale;
import edu.wpi.cs.jburge.SEURAT.editors.EditDecision;

public class CandidateRationale extends RationaleElement implements Serializable {

	/**
	 * Serial number - required by Java for serialization
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * What type of element is the candidate?
	 */
	private RationaleElementType type;
	
	/**
	 * The unique ID of our parent. Requirements and Decisions are always top level but
	 * alternatives and arguments will have parents.
	 */
	int parent;
	/**
	 * We have a list of children
	 */
	private Vector<CandidateRationale> children;
	
	/**
	 * The source document (web page, etc.) where the item came from
	 */
	private String source;
	
	/**
	 * A qualifier giving more detail on the status of the element. Currently
	 * valid only for alternatives (which can be "Adopted") and arguments (which can be 
	 * for or against alterantives - "Supports" and "Denies"
	 */
	private String qualifier;
	
	/**
	 * Our constructor - we need to know what type we are!
	 */
	public CandidateRationale(RationaleElementType ourType)
	{
		type = ourType;
		source = "";
		children = new Vector<CandidateRationale>();
	}
	
	/**
	 * Get the type
	 */
	public RationaleElementType getType()
	{
		return type;
	}
	
	/**
	 * Set our type
	 */
	public void setType(RationaleElementType ourType)
	{
		type = ourType;
	}
	
	/**
	 * Get our parent
	 */
	public int getParent()
	{
		return parent;
	}
	
	/**
	 * Set our parent
	 */
	public void setParent(int newParent)
	{
		parent = newParent;
	}
	/**
	 * Get our child elements
	 * @return
	 */
	public Vector<CandidateRationale> getChildren()
	{
		return children;
	}
	
	/**
	 * Add a new sub-element
	 *
	 */
	public void addChild(CandidateRationale ele)
	{
		children.add(ele);
	}
	
	/**
	 * Create the appropriate rationale element from our candidate and its children
	 * In order for the IDs to be correct, we need to save each element to the database
	 * before we create its children.
	 */
	public RationaleElement createRationale(int parent, RationaleElementType ptype)
	{
		RationaleElement newRat;

		boolean exists = RationaleDB.elementExists(name, type);
		
		String nameID = "";
		
		if (exists)
		{
			nameID = Integer.toString(this.getID());
		}
		if (type == RationaleElementType.ARGUMENT)
		{
			Argument arg = new Argument();
			arg.setName(name + nameID);
			arg.setDescription(description);
			arg.setCategory(ArgCategory.NONE);
			if (qualifier != null)
			{
				arg.setType(ArgType.fromString(qualifier));
			}
			else
			{
				arg.setType(ArgType.NONE);
			}		
			arg.setImportance(Importance.DEFAULT);
			arg.setPlausibility(Plausibility.HIGH);
			arg.setAmount(10);	
			arg.setParent(parent);
			arg.setPtype(ptype);
			//Now, we need to step through all our children...
			Iterator candidateKids = this.children.iterator();
			while (candidateKids.hasNext())
			{
				CandidateRationale kid;
				kid = (CandidateRationale) candidateKids.next();
				if (kid.getType() == RationaleElementType.ASSUMPTION)
				{
				Assumption newAssump = (Assumption) kid.createRationale(id, RationaleElementType.ALTERNATIVE);
				arg.setAssumption(newAssump);
				}
			}	
			int id = arg.toDatabase(parent, ptype);
			arg.setID(id);
			newRat = arg;
		}
		else if (type == RationaleElementType.ASSUMPTION)
		{
			Assumption assump = new Assumption();
			assump.setName(name + nameID);
			assump.setDescription(description);
			assump.setImportance(Importance.MODERATE);
			int id = assump.toDatabase();
			assump.setID(id);
			newRat = assump;
		}
		else if (type == RationaleElementType.QUESTION)
		{
			Question quest = new Question();
			quest.setName(name + nameID);
			quest.setDescription(description);
			quest.setStatus(QuestionStatus.UNANSWERED);
			quest.setParent(parent);
			quest.setPtype(ptype);
			int id = quest.toDatabase(parent, ptype);
			quest.setID(id);
			newRat = quest;
		}
		else if (type == RationaleElementType.ALTERNATIVE)
		{
			Alternative alt = new Alternative();
			alt.setName(name + nameID);
			alt.setDescription(description);
			if (qualifier != null)
			{
				alt.setStatus(AlternativeStatus.fromString(qualifier));
			}
			else
			{
				alt.setStatus(AlternativeStatus.ATISSUE);
			} 
			alt.setParent(parent);
			alt.setPtype(ptype);
			int id = alt.toDatabase(parent, ptype);
			alt.setID(id);
			//Now, we need to step through all our children...
			Iterator candidateKids = this.children.iterator();
			while (candidateKids.hasNext())
			{
				CandidateRationale kid;
				kid = (CandidateRationale) candidateKids.next();
				if (kid.getType() == RationaleElementType.ARGUMENT)
				{
				Argument newArg = (Argument) kid.createRationale(id, RationaleElementType.ALTERNATIVE);
				alt.addArgument(newArg);
				}
				else if (kid.getType() == RationaleElementType.QUESTION)
				{
					Question newQuest = (Question) kid.createRationale(id, RationaleElementType.ALTERNATIVE);
					alt.addQuestion(newQuest);
				}
			}	
			//Unlike the others, this can nest three deep and we risk not having the alternative relationships written
			//to the DB unless we do it here.
			alt.toDatabase(parent, ptype);
			newRat = alt;
		}
		else if (type == RationaleElementType.REQUIREMENT)
		{
			Requirement req = new Requirement();
			req.setName(name + nameID);
			req.setDescription(description);
			req.setType(ReqType.FR);
			req.setStatus(ReqStatus.UNDECIDED);
			req.setImportance(Importance.ESSENTIAL);
			req.setParent(parent);
			req.setPtype(ptype);
			int id = req.toDatabase(parent, ptype);
			req.setID(id);
			//Now, we need to step through all our children...
			Iterator candidateKids = this.children.iterator();
			while (candidateKids.hasNext())
			{
				CandidateRationale kid;
				kid = (CandidateRationale) candidateKids.next();
				Argument newArg = (Argument) kid.createRationale(id, RationaleElementType.REQUIREMENT);
				req.addArgument(newArg);
			}	
			newRat = req;			
		}
		else if (type == RationaleElementType.DECISION)
		{
			Decision dec = new Decision();
			dec.setName(name + nameID);
			dec.setDescription(description);
			dec.setType(DecisionType.SINGLECHOICE);
			dec.setStatus(DecisionStatus.UNRESOLVED);
			dec.setPhase(Phase.DESIGN);
			dec.setParent(parent);
			dec.setPtype(ptype);
			
			int id = dec.toDatabase(parent, ptype);
			dec.setID(id);
			//Now, we need to step through all our children...
			Iterator candidateKids = this.children.iterator();
			while (candidateKids.hasNext())
			{
				CandidateRationale kid;
				kid = (CandidateRationale) candidateKids.next();
				if (kid.getType() == RationaleElementType.ALTERNATIVE)
				{
				Alternative newAlt = (Alternative) kid.createRationale(id, RationaleElementType.DECISION);
				dec.addAlternative(newAlt);
				}
				else if (kid.getType() == RationaleElementType.QUESTION)
				{
					Question newQuest = (Question) kid.createRationale(id, RationaleElementType.DECISION);
					dec.addQuestion(newQuest);
				}
			}	
			newRat = dec;				
		}
		else
		{
			return null;
		}

		saveSource(newRat);
		
		return newRat;
	}
	
	/**
	 * Save the source information for a new rationale element. 
	 */
	public void saveSource(RationaleElement newRat)
	{
		RationaleDB db = RationaleDB.getHandle();
		Connection conn = db.getConnection();
		String insertQuery = null;
		
		Statement stmt = null; 
		ResultSet rs = null; 
		try
		{
			stmt = conn.createStatement();
			insertQuery = "INSERT INTO Sources " +
			"(source, parent, ptype) " +
			"VALUES ('" +
			RationaleDBUtil.escape(source) + "', " +
			new Integer(newRat.id).toString() + ", '" + 
			this.getType().toString() + "')";
		
			stmt.execute(insertQuery); 
			
		} catch (SQLException ex) {
			RationaleDB.reportError(ex, "CandidateRationale.fromDatabase(String)", "SQL Error");
			System.out.println(insertQuery);
		}
		
		finally { 
			RationaleDB.releaseResources(stmt, rs);
		}					
	}
	
	public void fromDatabase(int idp)
	{
		RationaleDB db = RationaleDB.getHandle();
		Connection conn = db.getConnection();
		this.id = idp;
		Statement stmt = null;
		ResultSet rs = null;
		
		try {
			stmt = conn.createStatement(); 
			String findQuery = "SELECT * FROM candidates where id=" +
			new Integer(idp).toString();
			rs = stmt.executeQuery(findQuery); 
			
			if (rs.next())
			{
				String ourName = rs.getString("name");
				this.fromDatabase(ourName);
			}
			rs.close();
			
		} catch (SQLException ex) {
			RationaleDB.reportError(ex, "CandidateRationale.fromDatabase(String)", "SQL Error");
		}
		
		finally { 
			RationaleDB.releaseResources(stmt, rs);
		}			
	}

	public void fromDatabase(String rName)
	{
		RationaleDB db = RationaleDB.getHandle();
		Connection conn = db.getConnection();
		
		this.name = rName; //don't forget our name!
		rName = RationaleDBUtil.escape(rName);
		
		Statement stmt = null;
		ResultSet rs = null;
		
		try {
			stmt = conn.createStatement(); 
			String findQuery = "SELECT * FROM candidates where name='" +
			rName + "'";
//			***			 System.out.println(findQuery);
			rs = stmt.executeQuery(findQuery); 
			
			if (rs.next())
			{
				this.id = rs.getInt("id");
				this.description = RationaleDBUtil.decode(rs.getString("description"));
				this.parent = rs.getInt("parent");
				//in some cases we might not actually have the right type, so get it again
				this.type = RationaleElementType.fromString(rs.getString("type"));
				this.source = RationaleDBUtil.decode(rs.getString("source"));
				this.qualifier = rs.getString("qualifier"); //might be null
				rs.close();
			}
			
			//Now, we need to get any child elements
			String findChildren = "SELECT name, type FROM candidates where " +
			"parent = " + new Integer(this.id).toString();
//			***			System.out.println(findFor);
			rs = stmt.executeQuery(findChildren); 
			
			while (rs.next())
			{
				String nextName = RationaleDBUtil.decode(rs.getString("name"));
				RationaleElementType ctype = RationaleElementType.fromString(rs.getString("type"));
				CandidateRationale child = new CandidateRationale(ctype);
				child.fromDatabase(nextName);
				children.add(child);
			}
			rs.close();
			
		} catch (SQLException ex) {
			RationaleDB.reportError(ex, "CandidateRationale.fromDatabase(String)", "SQL Error");
		}
		
		finally { 
			RationaleDB.releaseResources(stmt, rs);
		}
		
	}
	
	/**
	 * Save our candidate to the database.
	 * @param parent - the parent of the candidate
	 * @param newEle - indicates if we just created this candidate
	 * @return the unique ID
	 */
	public int toDatabase(int parent, boolean newEle)
	{
		RationaleDB db = RationaleDB.getHandle();
		Connection conn = db.getConnection();
		String updateQuery = "";
		int ourid = 0;
		boolean duplicate = false;
		this.parent = parent;
		
		//We save it under a fake name to make sure we don't get duplicate names
		//When importing from external documents, we don't want to force the user to
		//uniqueness, we'll impose it on this end.
		String tempName = RationaleDBUtil.escape(this.name) + "xyzzy";
		
		Statement stmt = null; 
		ResultSet rs = null; 
		try
		{
			stmt = conn.createStatement();
		
		if (newEle)
		{
			//need to make sure that we aren't duplicating the name
			updateQuery = "SELECT id FROM candidates where name='" +
			RationaleDBUtil.escape(this.name) + "'";
			rs = stmt.executeQuery(updateQuery); 
			
			if (rs.next())
			{
				duplicate = true;
				rs.close();
			}
			
			if (qualifier == null)
			{
				updateQuery = "INSERT INTO Candidates "+
				"(name, description, type, parent, source) " +
				"VALUES ('" +
				tempName + "', '" +
				RationaleDBUtil.escape(this.description) + "', '" +
				this.type.toString() + "', " +
				new Integer(this.parent).toString() + ", '" +
				RationaleDBUtil.escape(this.source) + "')";				
			}
			else
			{

				String strqualifier = getOurQualifier();
				
				updateQuery = "INSERT INTO Candidates "+
				"(name, description, type, parent, source, qualifier) " +
				"VALUES ('" +
				tempName + "', '" +
				RationaleDBUtil.escape(this.description) + "', '" +
				this.type.toString() + "', " +
				new Integer(this.parent).toString() + ", '" +
				RationaleDBUtil.escape(this.source) + "', '" +
				strqualifier + "')";
				
			}

		
			stmt.execute(updateQuery); 
			
			//get our ID
			updateQuery = "SELECT id FROM Candidates where name='" +
			tempName + "'";
			rs = stmt.executeQuery(updateQuery); 
			
			if (rs.next())
			{
				ourid = rs.getInt("id");
				rs.close();
				//now, update with the real name
				if (duplicate)
				{
					tempName = RationaleDBUtil.escape(this.name) + new Integer(ourid).toString();
				}
				else
				{
					tempName = RationaleDBUtil.escape(this.name);
				}
				updateQuery = "UPDATE candidates D " +
				"SET D.name = '" + tempName + "'" +
				" WHERE D.id = " + ourid + "";
				stmt.execute(updateQuery);
			}
			else
			{
				ourid = 0;
				System.out.println("query returned nothing: " + updateQuery);
			}
			this.id = ourid;
		} //if new
		//otherwise, we are updating it
		else
		{
			if (qualifier == null)
			{
				updateQuery = "UPDATE candidates D " +
				"SET D.parent = " + new Integer(parent).toString() +
				", D.description = '" + RationaleDBUtil.escape(description) +
				"', D.type = '" + type.toString() +
				"', D.name = '" + RationaleDBUtil.escape(name) +
				"', D.source = '" + RationaleDBUtil.escape(source) +
				"' WHERE " +
				"D.id = " + this.id + " " ;
				stmt.execute(updateQuery);				
			}
			else
			{
				String strqualifier = getOurQualifier();
				updateQuery = "UPDATE candidates D " +
				"SET D.parent = " + new Integer(parent).toString() +
				", D.description = '" + RationaleDBUtil.escape(description) +
				"', D.type = '" + type.toString() +
				"', D.name = '" + RationaleDBUtil.escape(name) +
				"', D.source = '" + RationaleDBUtil.escape(source) +
				"', D.qualifier = '" + strqualifier +
				"' WHERE " +
				"D.id = " + this.id + " " ;		
				stmt.execute(updateQuery);
			}
			
			}
		
		//Now, we need to step through all our children...
		Iterator candidateKids = this.children.iterator();
		while (candidateKids.hasNext())
		{
		CandidateRationale kid;
		kid = (CandidateRationale) candidateKids.next();
			kid.toDatabase(this.id, newEle);
		}
		

			
		} catch (SQLException ex) {
			// handle any errors 
			RationaleDB.reportError(ex,"Error in Candidate.toDatabase", updateQuery);
			
		}
		
		finally { 
			RationaleDB.releaseResources(stmt, rs);
		}
		
		return this.id;	
		
	}	
	
	private String getOurQualifier()
	{
		String strqualifier;
		if (this.type == RationaleElementType.ALTERNATIVE)
		{
			if (this.qualifier.compareTo(AlternativeStatus.ADOPTED.toString()) == 0)
			{
				strqualifier = this.qualifier;
			}
			//it should ONLY be equal to adopted or equal to null but, just in case...
			else
			{
				strqualifier = AlternativeStatus.ATISSUE.toString();
			}
		}
		else if (this.type == RationaleElementType.ARGUMENT)
		{
			if ((this.qualifier.compareTo(ArgType.DENIES.toString()) == 0) ||
			    (this.qualifier.compareTo(ArgType.SUPPORTS.toString()) == 0))
				{
				strqualifier = qualifier;
				}
			else
			{
				strqualifier = ArgType.NONE.toString();
			}
		}
		else
		{
			strqualifier = ArgType.NONE.toString(); //shouldn't happen!
		}
		return strqualifier;
	}
	/**
	 * Reads in a candidate rationale element stored in XML.
	 * @param candN - the XML element.
	 */
	public void fromXML(Element candN, String source) {
		
		this.fromXML = true;
		this.source = source;
		
		//get our name
		String tname = candN.getAttribute("name");
		if (tname.length() > 250)
		{
			name = tname.substring(0, 250);
			System.out.println("Truncated name");
		}
		else
		{
			name = tname;
		}
			
		Node descN = candN.getFirstChild();
		//get the description
		//the text is actually the child of the element, odd...
		Node descT = descN.getFirstChild();
		if (descT instanceof Text) {
			Text text = (Text) descT;
			String data = text.getData();
			if (data.length() > 250)
			{
				setDescription(data.substring(0, 250));
			}
			else
			{
			setDescription(data);
			}
		}
		Element nextChild = (Element) descN.getNextSibling();
		while (nextChild != null)
		{
    		String nextName;
	    	nextName = nextChild.getNodeName();
	    	if (nextName.compareTo("DR:requirement") == 0)
	    	{
				CandidateRationale nextReq = new CandidateRationale(RationaleElementType.REQUIREMENT);
				nextReq.fromXML(nextChild, source);
				this.addChild(nextReq);
	    	}
	    	else if ((nextName.compareTo("DR:decision") == 0) ||
	    			 (nextName.compareTo("DR:decisionproblem") == 0))
	    	{
				CandidateRationale nextReq = new CandidateRationale(RationaleElementType.DECISION);
				nextReq.fromXML(nextChild, source);
				this.addChild(nextReq);
	    	}
	    	else if (nextName.compareTo("DR:alternative") == 0)
	    	{
				CandidateRationale nextReq = new CandidateRationale(RationaleElementType.ALTERNATIVE);
				//get its qualifier
				nextReq.setQualifier(nextChild.getAttribute("status"));
				nextReq.fromXML(nextChild, source);
				this.addChild(nextReq);
	    	}
	    	else if (nextName.compareTo("DR:argument") == 0)
	    	{
				CandidateRationale nextReq = new CandidateRationale(RationaleElementType.ARGUMENT);
				//get its qualifier
				nextReq.setQualifier(nextChild.getAttribute("argtype"));
				nextReq.fromXML(nextChild, source);
				this.addChild(nextReq);
	    	}
	    	else if (nextName.compareTo("DR:question") == 0)
	    	{
	    		CandidateRationale nextReq = new CandidateRationale(RationaleElementType.QUESTION);
				nextReq.fromXML(nextChild, source);
				this.addChild(nextReq);	    		
	    	}
	    	else if (nextName.compareTo("DR:assumption") == 0)
	    	{
	    		CandidateRationale nextReq = new CandidateRationale(RationaleElementType.ASSUMPTION);
				nextReq.fromXML(nextChild, source);
				this.addChild(nextReq);	    		
	    	}
	    		
	    	nextChild = (Element) nextChild.getNextSibling();
		}
		
	}
	
	/**
	 * Deletes a candidate rationale element from SEURAT and the database. This only works
	 * if there aren't any child elements
	 */
	public boolean delete()
	{
		//need to have a way to inform if delete did not happen
		//can't delete if there are dependencies...
		if (this.children.size() > 0) 
		{
			MessageDialog.openError(new Shell(),	"Delete Error",	"Can't delete when there are sub-elements.");
			return true;
		}
		RationaleDB db = RationaleDB.getHandle();
		
		db.deleteCandidateRationaleElement(this);
		return false;
		
	}
	
	/**
	 * Deletes a candidate rationale element from the database and all its children. This
	 * is called when the element is adopted as rationale
	 */
	public void deleteAll()
	{
		//iterate through children
		Iterator kids = this.children.iterator();
		while (kids.hasNext())
		{
			CandidateRationale child = (CandidateRationale) kids.next();
			child.deleteAll();
		}
		RationaleDB db = RationaleDB.getHandle();
		db.deleteCandidateRationaleElement(this);

	}
	
	/**
	 * Display our decision by bringing up the decision editor.
	 * @param disp - points back to our display
	 * @return true if the user cancels the editor
	 */
	public boolean display(Display disp)
	{
		EditCandidateRationale ar = new EditCandidateRationale(disp, this, false);
//		System.out.println("this after = " + this.getStatus().toString());
//		System.out.println(ar.getCanceled());
		String msg = "Edited candidate " + this.getName() + " " + ar.getCanceled();
		DataLog d = DataLog.getHandle();
		d.writeData(msg);
		return ar.getCanceled(); //can I do this?
		
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getQualifier() {
		return qualifier;
	}

	public void setQualifier(String qualifier) {
		this.qualifier = qualifier;
	}

}
