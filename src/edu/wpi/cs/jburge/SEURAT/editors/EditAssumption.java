
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

package edu.wpi.cs.jburge.SEURAT.editors;

import java.io.Serializable;

import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.events.*;

import edu.wpi.cs.jburge.SEURAT.rationaleData.*;

/**
 * Assumption editor - assumptions are things that we believe to be true about the context the
 * software will be run in but that may change.
 * @author burgeje
 *
 */
public class EditAssumption extends NewRationaleElementGUI implements Serializable {
	
	private static final long serialVersionUID = 7684998496991891462L;
	
	private Shell shell;
	
	private Assumption ourAssump;
	
	private Text nameField;
	
	private Text descArea;
//	private boolean newItem;
	
	private Button addButton;
	private Button cancelButton;
	
	private Button enableButton;
	
	// private MessageBox errorBox; 		//dialog for the error message
	
	
	
	public EditAssumption(Display display, Assumption editAssump, boolean newItem)
	{
		super();
		shell = new Shell(display, SWT.DIALOG_TRIM | SWT.PRIMARY_MODAL);
		shell.setText("Assumption Information");
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		gridLayout.marginHeight = 5;
		gridLayout.makeColumnsEqualWidth = true;
		shell.setLayout(gridLayout);
		
		ourAssump = editAssump;
		
		
		new Label(shell, SWT.NONE).setText("Name:");
		
		nameField =  new Text(shell, SWT.SINGLE | SWT.BORDER);
		nameField.setText(ourAssump.getName());
		GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING);
		gridData.horizontalSpan = 2;
		DisplayUtilities.setTextDimensions(nameField, gridData, 150);
		nameField.setLayoutData(gridData);
		
		new Label(shell, SWT.NONE).setText("Description:");
		
		descArea = new Text(shell, SWT.MULTI | SWT.WRAP | SWT.BORDER | SWT.V_SCROLL);
		descArea.setText(ourAssump.getDescription());
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		DisplayUtilities.setTextDimensions(descArea, gridData,100, 2);
		gridData.horizontalSpan = 2;
		descArea.setLayoutData(gridData);
		
		
		
		enableButton = new Button(shell, SWT.CHECK);
		enableButton.setText("Enabled");
		enableButton.setSelection(ourAssump.getEnabled());
		
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		enableButton.setLayoutData(gridData);
		new Label(shell, SWT.NONE).setText(" ");
		new Label(shell, SWT.NONE).setText(" ");
		
		addButton = new Button(shell, SWT.PUSH); 
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING);
		addButton.setLayoutData(gridData);
		if (newItem)
		{
			addButton.setText("Add");
			addButton.addSelectionListener(new SelectionAdapter() {
				
				public void widgetSelected(SelectionEvent event) 
				{
					//int typeIndex;
					//int statusIndex;
					canceled = false;
					if (!nameField.getText().trim().equals(""))
					{
						ConsistencyChecker checker = new ConsistencyChecker(ourAssump.getID(), nameField.getText(), "Assumptions");
						
						if(ourAssump.getName() == nameField.getText() || checker.check())
						{
							ourAssump.setName(nameField.getText());
							ourAssump.setDescription(descArea.getText());
							ourAssump.setEnabled(enableButton.getSelection());
							
//							comment before this made no sense...
							ourAssump.toDatabase();
							shell.close();
							shell.dispose();
						}
						
					}
					else
					{
						String l_message = "";
						l_message += "The assumption name you have specified is either already"
							+ " in use or empty. Please make sure that you have specified"
							+ " an assumption name and the assumption name does not already exist"
							+ " in the database.";
						MessageBox mbox = new MessageBox(shell, SWT.ICON_ERROR);
						mbox.setMessage(l_message);
						mbox.setText("Assumption Name Is Invalid");
						mbox.open();
					}
				}
			});
			
		}
		else
		{
			addButton.setText("Save");
			addButton.addSelectionListener(new SelectionAdapter() {
				
				public void widgetSelected(SelectionEvent event) 
				{
					//int typeIndex;
					canceled = false;
					//int statusIndex;
					
					ConsistencyChecker checker = new ConsistencyChecker(ourAssump.getID(), nameField.getText(), "Assumptions");
					
					if(ourAssump.getName() == nameField.getText() || checker.check())
					{
						ourAssump.setName(nameField.getText());
						ourAssump.setDescription(descArea.getText());
						ourAssump.setEnabled(enableButton.getSelection());
						//since this is a save, not an add, the type and parent are ignored
						ourAssump.toDatabase();
						
						//			RationaleDB db = RationaleDB.getHandle();
						//			db.addAssumption(ourAssump);
						
						shell.close();
						shell.dispose();
					}
					
				}
			});
		}
		
		
		
		cancelButton = new Button(shell, SWT.PUSH); 
		cancelButton.setText("Cancel");
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING);
		cancelButton.setLayoutData(gridData);
		cancelButton.addSelectionListener(new SelectionAdapter() {
			
			public void widgetSelected(SelectionEvent event) 
			{
				canceled = true;
				shell.close();
				shell.dispose();
			}
		});
		
		shell.pack();
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) display.sleep();
		}
	}
	
	
	
	public RationaleElement getItem()
	{
		return ourAssump;
	}
	
	
}






