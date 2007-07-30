/*
 * Created on Feb 1, 2004
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package edu.wpi.cs.jburge.SEURAT.inference;

import java.sql.*;
import java.util.*;

import edu.wpi.cs.jburge.SEURAT.rationaleData.*;

/**
 * Performs any necessary inferences when a claim is deleted or modified.
 * @author jburge
 */
public class ClaimInferences {

	/**
	 * Empty constructor
	 */
	public ClaimInferences() {
	}
	
	/**
	 * Update the claim and return any status changes
	 * @param ourClaim - the claim that has been modified
	 * @return a vector of status updates that need to be displayed
	 */
	public Vector<RationaleStatus> updateClaim(Claim ourClaim)
	{
		Vector<RationaleStatus> newStatus = new Vector<RationaleStatus>();
		//a claim has changed - so what inferences must be re-computed?
		//first, find what arguments are affected and evaluate them
		
		Vector<String> argNames = new Vector<String>();
		
		RationaleDB db = RationaleDB.getHandle();
		Connection conn = db.getConnection();
		
		Statement stmt = null; 
		ResultSet rs = null; 
	//	boolean error = false;
		try {
			 stmt = conn.createStatement();
			 String findQuery; 
				 findQuery = "SELECT name  FROM " +
				 "arguments where argtype = 'claim' and " +
				 "claim = " + ourClaim.getID();
//***			 System.out.println(findQuery);
			 rs = stmt.executeQuery(findQuery);
			 
			 while (rs.next())
			 {
				argNames.add(RationaleDB.decode(rs.getString("name")));
			 }
			 
			 Enumeration args = argNames.elements();
			 while (args.hasMoreElements())
			 {
				Argument arg = new Argument();
				arg.fromDatabase((String) args.nextElement());
			 	
				ArgumentInferences inf = new ArgumentInferences();
				newStatus.addAll(inf.updateArgument(arg, true));
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
		   
		UpdateManager manager = UpdateManager.getHandle();
		manager.addUpdate(ourClaim.getID(), ourClaim.getName(), RationaleElementType.CLAIM);

		 return newStatus;
	
	} 	

}
