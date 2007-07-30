/*
 * Requirements class
 */
package edu.wpi.cs.jburge.SEURAT.rationaleData;

import instrumentation.DataLog;

import java.util.*;
import java.io.*;

import java.sql.Connection; 
import java.sql.SQLException; 
import java.sql.Statement;
import java.sql.ResultSet;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import edu.wpi.cs.jburge.SEURAT.editors.EditRequirement;
import edu.wpi.cs.jburge.SEURAT.inference.RequirementInferences;
import org.w3c.dom.*;

/**
 * Defines the structure of a requirement.
 * @author burgeje
 *
 */
public class Requirement extends RationaleElement implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -8527639694978991643L;
	// class variables

	// instance variables
	/**
	 * Type of requirement: functional or non-functional (FR or NFR)
	 */
	ReqType m_type;
	/**
	 * Identifies the parent element
	 */
	int m_parent;
	/**
	 * The type of the parent. This should be an element type but there
	 * were issues with conversions.
	 */
	String m_ptype;
	/**
	 * String giving the artifact. This could be a requirement number. Right now
	 * this would need to be manually entered but eventually we would want to tie this into
	 * a requirements tool such as Requisite Pro (or whatever it is being
	 * replaced with...)
	 */
	String m_artifact;
	/**
	 * The status of the requirement (addressed, violated, satisfied, etc.)
	 */
	ReqStatus m_status;		
	
	/**
	 * List of the names of arguments for this requirement
	 */
	Vector<String> m_argumentsAgainst;
	/**
	 * List of the names of arguments against this alternative
	 */
    Vector<String> m_argumentsFor;
    /**
     * List of all the arguments relating to this requirement
     */
    Vector<Argument> m_arguments;
    
    /**
     * List of sub-requirements
     */
	Vector<Requirement> m_requirements;

	/**
	 * Constructor. Sets the initial status to undecided and creates ampty
	 * vectors for the arguments for and against, and other arguments (dependencies),
	 * and for requirements (sub-requirements?)
	 *
	 */
	public Requirement()
	{
		super();
		m_status = ReqStatus.UNDECIDED;
		m_argumentsAgainst = new Vector<String>();
		m_argumentsFor = new Vector<String>();
		m_arguments = new Vector<Argument>();
		m_requirements = new Vector<Requirement>();
	} 

	/*
	public Requirement(String name)
	{
		this.name = name;
	}
	*/
	
	public Requirement(String newDescription, String newArtifact, ReqType newType)
	{
		description = newDescription;
		m_artifact = newArtifact;
		m_type = newType;
	}
	
	public RationaleElementType getElementType()
	{
		return RationaleElementType.REQUIREMENT;
	}
	
	public void fromDatabase(int reqID)
	{
		
		RationaleDB db = RationaleDB.getHandle();
		Connection conn = db.getConnection();

		this.id = reqID;
		
		Statement stmt = null; 
		ResultSet rs = null; 
//		boolean error = false;
		try {
			
			stmt = conn.createStatement();
			String findQuery;
			findQuery = "SELECT name FROM " + 
			"requirements where id = " +
			new Integer(reqID).toString();
//***		    System.out.println(findQuery);
			rs = stmt.executeQuery(findQuery);
			 
			if (rs.next())
			{
		   		name = RationaleDB.decode(rs.getString("name"));
		   		rs.close();
		   		this.fromDatabase(name);
			}
			
		} catch (SQLException ex) {
	   // handle any errors 
	   System.out.println("SQLException: " + ex.getMessage()); 
	   System.out.println("SQLState: " + ex.getSQLState()); 
	   System.out.println("VendorError: " + ex.getErrorCode()); 
	   }
	   finally { 
		   // it is a good idea to release
		   // resources in a finally{} block 
		   // in reverse-order of their creation 
		   // if they are no-longer needed 

		   if (rs != null) { 
			   try {
				   rs.close(); 
			   } catch (SQLException sqlEx) { // ignore 
			   } 

			   rs = null; 
		   }
    
		   if (stmt != null) { 
			   try { 
				   stmt.close(); 
			   } catch (SQLException sqlEx) { // ignore
				   } 

			   stmt = null; 
		   }
		   }
	
	}
	public int getParent()
	{
		return m_parent;
	}
	
	public String getPtype()
	{
		return m_ptype;
	}
	public Vector getArgumentsFor()
	{
		return m_argumentsFor;
	}
	
	public Vector getArgumentsAgainst()
	{
		return m_argumentsAgainst;
	}
	
	public ReqType getType()
	{
		return m_type;
	}
	
	public void setType(ReqType newtype)
	{
		m_type = newtype;
	}
	
	public ReqStatus getStatus()
	{
		return m_status;
	}
	
	public void setStatus(ReqStatus newstatus)
	{
		m_status = newstatus;
	}
	
	public String getArtifact()
	{
		return m_artifact;
	}
	
	public void setArtifact(String newArtifact)
	{
		m_artifact = newArtifact;
	}
	
	public void addArgumentFor(String arg)
	{
		m_argumentsFor.addElement(arg);
	}
	
	
	public void addArgumentAgainst(String arg)
	{
		m_argumentsAgainst.addElement(arg);
	}
/* do I need this???	
	public Vector getList(RationaleElementType type)
	{
		if (type.equals(RationaleElementType.REQUIREMENT))
		{
			return m_requirements;
		}
		else if (type.equals(RationaleElementType.ARGUMENT))
		{
			return m_arguments;
		}
		else
		{
			return null;
		}
	}
*/	
	
	public void addRequirement(Requirement newReq)
	{
		m_requirements.addElement(newReq);
	}
	public void deleteArgument(Argument newArg)
	{
		m_arguments.remove(newArg);
	}
	
	public void addArgument(Argument newArg)
	{
		m_arguments.addElement(newArg);
	}

	public void fromDatabase(String rName)
	{
		RationaleDB db = RationaleDB.getHandle();
		Connection conn = db.getConnection();
		
		this.name = rName; //don't forget our name!
		rName = RationaleDB.escape(rName);
		
		Statement stmt = null;
		ResultSet rs = null;
		
		try {
			 stmt = conn.createStatement(); 
			 String findQuery = "SELECT * FROM requirements where name='" +
				rName + "'";
//***			 System.out.println(findQuery);
			 rs = stmt.executeQuery(findQuery); 

			if (rs.next())
			{
				this.id = rs.getInt("id");
				this.description = RationaleDB.decode(rs.getString("description"));
				this.m_type = (ReqType) ReqType.fromString(rs.getString("type"));
				this.m_status = (ReqStatus) ReqStatus.fromString(rs.getString("status"));
				this.m_artifact = rs.getString("artifact");
				this.m_parent = rs.getInt("parent");
				this.m_ptype = rs.getString("ptype");
				this.enabled = (rs.getString("enabled").compareTo("True") == 0);
				rs.close();
			}
			
			//Now, we need to get the lists of arguments for and against
			//first For
			String findFor = "SELECT name FROM Arguments where " +
			"ptype = 'Requirement' and " +
			"parent = " + 
			new Integer(this.id).toString() + " and " +
			"(type = 'SUPPORTS' or " +
			"type = 'ADDRESSES' or " +
			"type = 'SATISFIES' or " +
			"type = 'PRE-SUPPOSED-BY')";
//***			System.out.println(findFor);
			rs = stmt.executeQuery(findFor); 

			while (rs.next())
			{
				m_argumentsFor.addElement(RationaleDB.decode(rs.getString("name")));
			}
			rs.close();
			
			//Now, the arguments against
			String findAgainst = "SELECT name FROM Arguments where " +
			"ptype = 'Requirement' and " +
			"parent = " + 
			new Integer(this.id).toString() + " and " +
			"(type = 'DENIES' or " +
			"type = 'VIOLATES' or " +
			"type = 'OPPOSED-BY')";
//***			System.out.println(findAgainst);
			rs = stmt.executeQuery(findAgainst); 

			while (rs.next())
			{
				m_argumentsAgainst.addElement(RationaleDB.decode(rs.getString("name")));
			}
			rs.close();
			
			//find history
			//no, not last - need history too
			String findQuery5 = "SELECT * from HISTORY where ptype = 'Requirement' and " +
			  "parent = " + Integer.toString(id);
//***			  System.out.println(findQuery5);
			rs = stmt.executeQuery(findQuery5);
			while (rs.next())
			{
				History nextH = new History();
				nextH.setStatus(rs.getString("status"));
				nextH.setReason(RationaleDB.decode(rs.getString("reason")));
				nextH.dateStamp = rs.getTimestamp("date");
//				nextH.dateStamp = rs.getDate("date");
				history.add(nextH);
			}
			
		} catch (SQLException ex) {
	   // handle any errors 
	   System.out.println("SQLException: " + ex.getMessage()); 
	   System.out.println("SQLState: " + ex.getSQLState()); 
	   System.out.println("VendorError: " + ex.getErrorCode()); 
	   }
   	   
	   finally { 
		   // it is a good idea to release
		   // resources in a finally{} block 
		   // in reverse-order of their creation 
		   // if they are no-longer needed 

		   if (rs != null) { 
			   try {
				   rs.close(); 
			   } catch (SQLException sqlEx) { // ignore 
			   } 

			   rs = null; 
		   }
    
		   if (stmt != null) { 
			   try { 
				   stmt.close(); 
			   } catch (SQLException sqlEx) { // ignore
				   } 

			   stmt = null; 
		   } 
		   }
		
	}
	public int toDatabase(int parentID, RationaleElementType ptype)
	{
		RationaleDB db = RationaleDB.getHandle();
		Connection conn = db.getConnection();
		
		int ourid = this.id;
		
		//find out if this requirement is already in the database
		Statement stmt = null; 
		ResultSet rs = null; 

		String enabledStr;
		if (enabled)
			enabledStr = "True";
		else
			enabledStr = "False";
			
		try {
			stmt = conn.createStatement(); 
			/*
			String findQuery = "SELECT id FROM requirements where name='" +
			   this.name + "'";
			 System.out.println(findQuery);
			rs = stmt.executeQuery(findQuery); 

		   if (rs.next())
		   { */
		   if (this.id >= 0)
		   {
				String updateParent = "UPDATE Requirements "+
				   "SET name = '" +
					RationaleDB.escape(this.name) + "', " +				   "description = '" +					RationaleDB.escape(this.description) + "', " +
					"type = '" +
					this.m_type.toString() + "', " +				   "status = '" +
					this.m_status.toString() + "', " +				   "enabled = '" +
				   enabledStr + "' " +
					" WHERE " +
			   		"id = " + this.id + " " ;

//			   System.out.println(updateParent);
				stmt.execute(updateParent);
			}
//				return ourid;
		else 
		{
		
			//now, we have determined that the requirement is new
			String parentSt;
			String parentTSt;
			System.out.println("parent ID is "+parentID);
			if ((parentID < 0) || (ptype == null))
			{
				parentSt = "NULL";
				parentTSt = "None";
			}
			else
			{
				parentSt = new Integer(parentID).toString();
				parentTSt = ptype.toString();
			}
			String newReqSt = "INSERT INTO Requirements "+
			   "(name, description, type, status, ptype, parent, enabled) " +
			   "VALUES ('" +
			   RationaleDB.escape(this.name) + "', '" +
			   RationaleDB.escape(this.description) + "', '" +
			   this.m_type.toString() + "', '" +
			   this.m_status.toString() + "', '" +
			   parentTSt + "', " +
			   parentSt + ", '" +
			   enabledStr + "')";
//			   System.out.println(newReqSt);
			stmt.execute(newReqSt); 
			
		}
		
			//now, we need to get our ID
			String findQuery2 = "SELECT id FROM requirements where name='" +
			   this.name + "'";
			rs = stmt.executeQuery(findQuery2); 

		   if (rs.next())
		   {
			   ourid = rs.getInt("id");
			   rs.close();
		   }
		   else
		   {
		   	ourid = 0;
		   }
		   this.id = ourid;
		   
		   /* This should be done elsewhere???   no, this should be fixed...
		   //in either case, we want to update any sub-requirements in case
		   //they are new!

		   
		   Enumeration args = m_arguments.elements();
		   while (args.hasMoreElements())
		   {
			   Argument arg = (Argument) args.nextElement();
			   arg.toDatabase(ourid, RationaleElementType.REQUIREMENT);
		   }
		
		   //now, any sub-requirements
		   Enumeration reqs = m_requirements.elements();
		   while (reqs.hasMoreElements())
		   {
		   	   System.out.println("adding Sub-requirements");
		   	   System.out.println(ourid);
			   Requirement req = (Requirement) reqs.nextElement();
			   req.toDatabase(ourid, RationaleElementType.REQUIREMENT);
		   }

			*/			
		   //finally, the history
			
		   Enumeration hist = history.elements();
		   while (hist.hasMoreElements())
		   {
//		   	   System.out.println("printing history");
			   History his = (History) hist.nextElement();
			   his.toDatabase(ourid, RationaleElementType.REQUIREMENT);
//			   System.out.println("printed history");
		   }
		


		} catch (SQLException ex) {
	   // handle any errors 
	   System.out.println("SQLException: " + ex.getMessage()); 
	   System.out.println("SQLState: " + ex.getSQLState()); 
	   System.out.println("VendorError: " + ex.getErrorCode()); 
   	   }
   	   
	   finally { 
		   // it is a good idea to release
		   // resources in a finally{} block 
		   // in reverse-order of their creation 
		   // if they are no-longer needed 

		   if (rs != null) { 
			   try {
				   rs.close(); 
			   } catch (SQLException sqlEx) { // ignore 
			   } 

			   rs = null; 
		   }
    
		   if (stmt != null) { 
			   try { 
				   stmt.close(); 
			   } catch (SQLException sqlEx) { // ignore
				   } 

			   stmt = null; 
		   } 
		   }
		   
		return ourid;	
 
	}	
	
	
	
	// test for XML input (new method to test whether an requirement is already there)
	public int toDatabaseXML(int parentID, RationaleElementType ptype)
	{
		RationaleDB db = RationaleDB.getHandle();
		Connection conn = db.getConnection();
		
		int ourid = this.id;
		
		//find out if this requirement is already in the database
		Statement stmt = null; 
		ResultSet rs = null; 

		String enabledStr;
		if (enabled)
			enabledStr = "True";
		else
			enabledStr = "False";
			
		try {
			stmt = conn.createStatement(); 
			String findQuery = "SELECT id, parent FROM requirements where name='" +
			this.name + "'";
			System.out.println(findQuery);
			rs = stmt.executeQuery(findQuery); 			 

			if (rs.next())
			{
				System.out.println("already there");
				ourid = rs.getInt("id");
				this.id = ourid;
				String updateParent = "UPDATE Requirements "+
				"SET name = '" +
				RationaleDB.escape(this.name) + "', " +
				"description = '" +
				RationaleDB.escape(this.description) + "', " +
				"type = '" +
				this.m_type.toString() + "', " +
				"status = '" +
				this.m_status.toString() + "', " +
				"enabled = '" +
				enabledStr + "' " +
				" WHERE " +
				"id = " + ourid + " " ;

//				System.out.println(updateParent);
				stmt.execute(updateParent);
			}
//				return ourid;
			else 
			{

				//now, we have determined that the requirement is new
				String parentSt;
				String parentTSt;
				System.out.println("parent ID is "+parentID + "new version test");
				if ((parentID < 0) || (ptype == null))
				{
					parentSt = "NULL";
					parentTSt = "None";
				}
				else
				{
					parentSt = new Integer(parentID).toString();
					parentTSt = ptype.toString();
				}
				String newReqSt = "INSERT INTO Requirements "+
				"(name, description, type, status, ptype, parent, enabled) " +
				"VALUES ('" +
				RationaleDB.escape(this.name) + "', '" +
				RationaleDB.escape(this.description) + "', '" +
				this.m_type.toString() + "', '" +
				this.m_status.toString() + "', '" +
				parentTSt + "', " +
				parentSt + ", '" +
				enabledStr + "')";
//				System.out.println(newReqSt);
				stmt.execute(newReqSt); 

			}
		
			//now, we need to get our ID
			String findQuery2 = "SELECT id FROM requirements where name='" +
			this.name + "'";
			rs = stmt.executeQuery(findQuery2); 

			if (rs.next())
			{
				ourid = rs.getInt("id");
				rs.close();
			}
			else
			{
				ourid = 0;
			}
			this.id = ourid;
			
			// arguments under the requirements
			Enumeration args = m_arguments.elements();
			while (args.hasMoreElements())
			{
				Argument arg = (Argument) args.nextElement();
				arg.toDatabaseXML(ourid, RationaleElementType.REQUIREMENT);
			}

			//now, any sub-requirements
			Enumeration reqs = m_requirements.elements();
			while (reqs.hasMoreElements())
			{
				System.out.println("adding Sub-requirements");
				System.out.println(ourid);
				Requirement req = (Requirement) reqs.nextElement();
				req.toDatabaseXML(ourid, RationaleElementType.REQUIREMENT);
			}
			
			Enumeration hist = history.elements();
			while (hist.hasMoreElements())
			{
//				System.out.println("printing history");
				History his = (History) hist.nextElement();
				his.toDatabaseXML(ourid, RationaleElementType.REQUIREMENT);
//				System.out.println("printed history");
			}
		} catch (SQLException ex) {
			// handle any errors 
			System.out.println("SQLException: " + ex.getMessage()); 
			System.out.println("SQLState: " + ex.getSQLState()); 
			System.out.println("VendorError: " + ex.getErrorCode()); 
		}

		finally { 
			// it is a good idea to release
			// resources in a finally{} block 
			// in reverse-order of their creation 
			// if they are no-longer needed 

			if (rs != null) { 
				try {
					rs.close(); 
				} catch (SQLException sqlEx) { // ignore 
				} 

				rs = null; 
			}

			if (stmt != null) { 
				try { 
					stmt.close(); 
				} catch (SQLException sqlEx) { // ignore
				} 

				stmt = null; 
			} 
		}

		return ourid;	

	}	
	/*
	public boolean display()
	{
		Frame lf = new Frame();
		RequirementGUI ar = new RequirementGUI(lf, this, false);
		ar.show();
		return ar.getCanceled();
	} */
	
	public boolean display(Display disp)
	{
		EditRequirement ar = new EditRequirement(disp, this, false);
		String msg = "Edited requirement " + this.getName() + " " + ar.getCanceled();
		DataLog d = DataLog.getHandle();
		d.writeData(msg);
//		System.out.println("this after = " + this.getStatus().toString());
//		System.out.println(ar.getCanceled());
		return ar.getCanceled(); //can I do this?
		
	}

	public boolean create(Display disp, RationaleElement parent)
	{
//		System.out.println("create requirement");
		if (parent != null)
		{
			this.m_parent = parent.getID();
			this.m_ptype = parent.getElementType().toString();
		}
		else
		{
			this.m_parent = 0;
		}
		EditRequirement ar = new EditRequirement(disp, this, true);
		return ar.getCanceled(); //can I do this?
	}
	/*	
	public boolean create(RationaleElement parent)
	{
		System.out.println("create requirement");
		if (parent != null)
		{
			this.m_parent = parent.getID();
			this.m_ptype = parent.getElementType().toString();
		}
		else
		{
			this.m_parent = 0;
		}
		Frame lf = new Frame();
		RequirementGUI ar = new RequirementGUI(lf,  this, true);
		ar.show();
		return ar.getCanceled();
	} */
	
	public boolean delete()
	{
		//need to have a way to inform if delete did not happen
		//can't delete if there are dependencies...
		if ((this.m_argumentsAgainst.size() > 0) ||
			(this.m_argumentsFor.size() > 0))
			{
				MessageDialog.openError(new Shell(),	"Delete Error",	"Can't delete when there are sub-elements.");
				return true;
			}
		RationaleDB db = RationaleDB.getHandle();
		
		int argCount = db.countArgReferences(this);
		if (argCount > 0)
		{
			MessageDialog.openError(new Shell(),	"Delete Error",	"Can't delete when there are referring arguments.");
		}
		
		//are there any dependencies on this item?
		if (db.dependentAlternatives(this))
		{
			MessageDialog.openError(new Shell(),	"Delete Error",	"Can't delete when there are dependencies.");
			return true;
		}
		db.deleteRationaleElement(this);
		return false;
		
	}
	public Vector<RationaleStatus> updateStatus()
	{
		RequirementInferences inf = new RequirementInferences();
		Vector<RationaleStatus> newStat = inf.updateRequirement( this);
		return newStat;

	}
	
	public Vector<RationaleStatus> updateOnDelete()
	{
		//if no dependencies, no one to update
		//wrong - need to update our own status...
//		return new Vector();
		return updateStatus();
	}
	
	public void fromXML(Element reqNode)
	{
		RationaleDB db = RationaleDB.getHandle();
		
		//add idref ***from the XML***
		String idref = reqNode.getAttribute("id");
	
		//get our name
		name = reqNode.getAttribute("name");
		
		//get our type
		m_type = ReqType.fromString(reqNode.getAttribute("reqtype"));
		
		//get our status
		m_status = ReqStatus.fromString(reqNode.getAttribute("status"));
		
		//get our artifact
		m_artifact = reqNode.getAttribute("artifact");

		//and last....
		db.addRef(idref, this);	//important to use the ref from the XML file!
		
		Node descN = reqNode.getFirstChild();
		//get the description
		//the text is actually the child of the element, odd...
		Node descT = descN.getFirstChild();
		if (descT instanceof Text) 
		{
		  Text text = (Text) descT;
		  String data = text.getData();
		  setDescription(data);
		}
		
		//now, we loop until all children are done.
		Element nextChild = (Element) reqNode.getFirstChild();
		String nextName;
		
		while (nextChild != null)
		{
			nextName = nextChild.getNodeName();
			//here we check the type, then process
			if (nextName.compareTo("DR:argument") == 0)
			{
				Argument nextArg = new Argument();
				db.addArgument(nextArg);
				addArgument(nextArg);
				nextArg.fromXML(nextChild);
			}
			else if (nextName.compareTo("DR:decision") == 0)
			{
				System.out.println("decision under requirement?");
			} 
			else if (nextName.compareTo("DR:history") == 0)
			{
				History hist = new History();
				// why is this commented out???
//				updateHistory(hist); 
				historyFromXML(nextChild);
			}
			else if (nextName.compareTo("DR:requirement") == 0)
			{
				Requirement subR = new Requirement();
				addRequirement(subR);	
				db.addRequirement(subR);
				subR.fromXML(nextChild);
			}
			else if (nextName.compareTo("reqref") == 0)
			{
				Node childRef = nextChild.getFirstChild(); //now, get the text
				//decode the reference
				Text refText = (Text) childRef;
				String stRef = refText.getData();
				addRequirement((Requirement)db.getRef(stRef));
			}
			else if (nextName.compareTo("decref") == 0)
			{
				Node childRef = nextChild.getFirstChild(); //now, get the text
				//decode the reference
				Text refText = (Text) childRef;
				String stRef = refText.getData();
	//			addDecision((Decision)db.getRef(stRef));

			} 
			
			nextChild = (Element) nextChild.getNextSibling();
		}
	
		
	}
	

			
}
	
