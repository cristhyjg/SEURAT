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

package SEURAT.editors;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.*;

import SEURAT.events.RationaleUpdateEvent;

import edu.wpi.cs.jburge.SEURAT.editors.ConsistencyChecker;
import edu.wpi.cs.jburge.SEURAT.editors.DisplayUtilities;
import edu.wpi.cs.jburge.SEURAT.editors.ReasonGUI;
import edu.wpi.cs.jburge.SEURAT.rationaleData.Alternative;
import edu.wpi.cs.jburge.SEURAT.rationaleData.AlternativeStatus;
import edu.wpi.cs.jburge.SEURAT.rationaleData.Argument;
import edu.wpi.cs.jburge.SEURAT.rationaleData.Pattern;
import edu.wpi.cs.jburge.SEURAT.rationaleData.PatternDecision;
import edu.wpi.cs.jburge.SEURAT.rationaleData.DecisionStatus;
import edu.wpi.cs.jburge.SEURAT.rationaleData.DecisionType;
import edu.wpi.cs.jburge.SEURAT.rationaleData.Designer;
import edu.wpi.cs.jburge.SEURAT.rationaleData.History;
import edu.wpi.cs.jburge.SEURAT.rationaleData.Phase;
import edu.wpi.cs.jburge.SEURAT.rationaleData.RationaleDB;
import edu.wpi.cs.jburge.SEURAT.rationaleData.RationaleElement;
import edu.wpi.cs.jburge.SEURAT.rationaleData.RationaleElementType;
import edu.wpi.cs.jburge.SEURAT.views.PatternLibrary;
import edu.wpi.cs.jburge.SEURAT.views.RationaleExplorer;
import edu.wpi.cs.jburge.SEURAT.views.TreeParent;

/**
 * Displays the editor for a decision in pattern library.
 * @author qiaoy, wangw2
 *
 */
public class PatternDecisionEditor extends RationaleEditorBase {
	public static RationaleEditorInput createInput(RationaleExplorer explorer, TreeParent tree,
			RationaleElement parent, RationaleElement target, boolean new1) {
		return new PatternDecisionEditor.Input(explorer, tree, parent, target, new1);
	}
	
	public static RationaleEditorInput createInput(PatternLibrary patternLib, TreeParent tree,
			RationaleElement parent, RationaleElement target, boolean new1) {
		return new PatternDecisionEditor.Input(patternLib, tree, parent, target, new1);
	}
	
	/**
	 * This class provides caching features used when updating
	 * properties of a decision remotely.
	 */
	private class DataCache
	{
		/**
		 * Last known good name
		 */
		String name;
		/**
		 * Last known good description
		 */
		String description;
		/**
		 * Last known good type
		 */
		int type;
		/**
		 * Last known good stauts
		 */
		int status;
		/**
		 * Last known good phase
		 */
		int phase;
		/**
		 *  last known good designer
		 */
		int designer;
	}
	
	/**
	 * The name of the decision
	 */  
	private Text nameField;
	
	/**
	 * A description of the decision
	 */
	private Text descArea;

	
	/**
	 * The decision type
	 */  
	private Combo typeBox;
	/**
	 * The decision status
	 */
	private Combo statusBox;
	/**
	 * The development phase where the decision is made
	 */
	private Combo phaseBox;


	/**
	 * The designer selection combobox
	 */
	private Combo designerBox;	
	/**
	 * The designer display label
	 */
	private Label designerLabel;	
	/**
	 * Composite used for switching between the designer combobox
	 * and the designer label
	 */
	private Composite designerComposite;
	
	
	/**
	 * Member variable storing last known good
	 * values of editable fields.
	 */
	private DataCache dataCache = new DataCache();
	
	private Pattern ourPattern;
	
	/* (non-Javadoc)
	 * @see SEURAT.editors.RationaleEditorBase#editorType()
	 */
	public Class editorType() {
		return PatternDecision.class;
	}
	
	/* (non-Javadoc)
	 * @see SEURAT.editors.RationaleEditorBase#getRationaleElement()
	 */
	public RationaleElement getRationaleElement() {
		return getDecision();
	}

	/**
	 * @return the decision associated with the editor
	 */
	public PatternDecision getDecision() {
		return (PatternDecision)getEditorData().getAdapter(PatternDecision.class);
	}
	
	/**
	 * Remove all designer controls from the
	 * editor interface.
	 */
	public void unloadDesignerControls()
	{
		if( designerLabel != null )
		{
			designerLabel.dispose();
			designerLabel = null;
		}
		if( designerBox != null )
		{
			designerBox.dispose();
			designerBox = null;
		}		
	}
	
	/**
	 * Add the designer selection combobox to the editor
	 */
	public void loadDesignerComboBox()
	{
		designerBox = new Combo(designerComposite, SWT.NONE);
		designerBox.select(0);	
		GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		DisplayUtilities.setComboDimensions(designerBox, gridData, 50);
		gridData.horizontalSpan = 5;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;
		designerBox.setLayoutData(gridData);
		designerBox.addModifyListener(getNeedsSaveListener());
		
		RationaleDB db = RationaleDB.getHandle();
		Vector ourDesigners = db.getNameList(RationaleElementType.DESIGNER);
		if (ourDesigners != null)
		{
			Enumeration desEnum = ourDesigners.elements();
			while (desEnum.hasMoreElements())
			{
				String des = (String) desEnum.nextElement();
				if (des.compareTo("Designer-Profiles") != 0)
				{
					designerBox.add( des );					
				}
			}
		}
	}
	
	/**
	 * Add the designer label to the editor
	 */
	public void loadDesignerLabel()
	{
		designerLabel = new Label(designerComposite, SWT.NONE);
		GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING);
		gridData.horizontalSpan = 5;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;
		designerLabel.setLayoutData(gridData);
		
		if (getDecision().getDesigner() != null)
		{
			designerLabel.setText(getDecision().getDesigner().getName());
		}
		else
		{
			designerLabel.setText("No Designer Is Associated With This Argument");		
		}
		
		designerComposite.layout();
	}
	
	/**
	 * Update the editor controls in response to a change to the editor.
	 * Also is called when a new child of the decision is created.
	 * 
	 * @param pElement the decision which generated the event
	 * @param pEvent a description of what changed in the description
	 */
	public void onUpdate(PatternDecision pElement, RationaleUpdateEvent pEvent)
	{
		try
		{
			if( pEvent.getElement() != pElement &&
				pEvent.getCreated() &&
				pEvent.getElement() instanceof Alternative )
			{
				
			}
			else
			if( pEvent.getElement().equals(getDecision()) )
			{
				if( pEvent.getDestroyed() )
				{
					closeEditor();
				}
				else
				if( pEvent.getModified() )
				{
					refreshForm(pEvent);
				}
			}			
		}
		catch( Exception eError )
		{
			System.out.println("Exception in PatternDecisionEditor: onUpdate");
		}
	}
	
	
	
	/* (non-Javadoc)
	 * @see SEURAT.editors.RationaleEditorBase#updateFormCache()
	 */
	@Override
	protected void updateFormCache() {
		if( nameField != null )
			dataCache.name = nameField.getText();
		
		if( descArea != null )
			dataCache.description = descArea.getText();
		
		if( designerBox != null )
			dataCache.designer = designerBox.getSelectionIndex();
		else
			dataCache.designer = 0;
		
		if( typeBox != null )
			dataCache.type = typeBox.getSelectionIndex();
		
		if( statusBox != null )
			dataCache.status = statusBox.getSelectionIndex();
		
		if( phaseBox != null )
			dataCache.phase = phaseBox.getSelectionIndex();
		
	}

	/* (non-Javadoc)
	 * @see SEURAT.editors.RationaleEditorBase#onRefreshForm(SEURAT.events.RationaleUpdateEvent)
	 */
	@Override
	protected void onRefreshForm(RationaleUpdateEvent pEvent) {
		boolean l_dirty = isDirty();
		Enumeration iterator;
		int index;
		
		// Something Has Changed, Reload This Element From The DB
		getDecision().fromDatabase(getDecision().getID());
		
		if( nameField.getText().equals(dataCache.name) )
		{
			nameField.setText(getDecision().getName());
			dataCache.name = nameField.getText();
		}
		else
			l_dirty = true;
		
		if( descArea.getText().equals(dataCache.description) )
		{
			descArea.setText(getDecision().getDescription());
			dataCache.description = descArea.getText();
		}
		else
			l_dirty = true;
		
		// Currently Designer Can't Change
		// After The Rationale Has Been Created
		// So Designer Update Issues
		unloadDesignerControls();
		loadDesignerLabel();
		dataCache.designer = 0;		
		
		if( typeBox.getSelectionIndex() == dataCache.type )
		{
			typeBox.removeAll();
			for (index = 0, iterator = DecisionType.elements() ;
				iterator.hasMoreElements() ;
				index++ )
			{
				DecisionType rtype = (DecisionType) iterator.nextElement();
				typeBox.add( rtype.toString());
				if (rtype.toString().compareTo(getDecision().getType().toString()) == 0)
				{
					typeBox.select(index);
				}
			}
			dataCache.type = typeBox.getSelectionIndex();
		}
		else
			l_dirty = true;
		
		if( statusBox.getSelectionIndex() == dataCache.status )
		{
			statusBox.removeAll();
			for( index = 0, iterator = DecisionStatus.elements() ;
					iterator.hasMoreElements() ;
					index++ )
			{
				DecisionStatus stype = (DecisionStatus) iterator.nextElement();
				statusBox.add( stype.toString() );
				if (stype.toString().compareTo(getDecision().getStatus().toString()) == 0)
				{
					statusBox.select(index);
				}
			}
			dataCache.status = statusBox.getSelectionIndex();
		}
		else
			l_dirty = true;
		
		if( phaseBox.getSelectionIndex() == dataCache.phase )
		{
			phaseBox.removeAll();
			for (index = 0, iterator = Phase.elements() ;
					iterator.hasMoreElements() ;
					index++)
			{
				Phase ptype = (Phase) iterator.nextElement();
				phaseBox.add( ptype.toString() );
				if (ptype.toString().equals(getDecision().getPhase().toString()))
				{
					phaseBox.select(index);
				}
				index++;
			}
			dataCache.phase = phaseBox.getSelectionIndex();
		}
		else
			l_dirty = true;
		
		setDirty(l_dirty);
	}
	
	/* (non-Javadoc)
	 * @see SEURAT.editors.RationaleEditorBase#setupForm(org.eclipse.swt.widgets.Composite)
	 */
	public void setupForm(Composite parent) {	
		String patternName = this.getTreeParent().getName();
		ourPattern = new Pattern();
		ourPattern.fromDatabase(patternName);
		//Finally, I GOT IT! Now we can use this to save to the relationship (YQ)
		
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 6;
		gridLayout.marginHeight = 5;
		gridLayout.makeColumnsEqualWidth = true;
		parent.setLayout(gridLayout);
		
		if (isCreating())
		{
			getDecision().setType(DecisionType.SINGLECHOICE);
			getDecision().setStatus(DecisionStatus.UNRESOLVED);
			getDecision().setPhase(Phase.DESIGN);
		}
		
		new Label(parent, SWT.NONE).setText("Name:");
		
		nameField =  new Text(parent, SWT.SINGLE | SWT.BORDER);
		nameField.setText(getDecision().getName());
		GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING);
		DisplayUtilities.setTextDimensions(nameField, gridData, 50);
		gridData.horizontalSpan = 5;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;
		
		nameField.addModifyListener(getNeedsSaveListener());
		nameField.setLayoutData(gridData);
		
		designerComposite = new Composite(parent, SWT.NONE);
		gridLayout = new GridLayout();
		gridLayout.numColumns = 6;
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		gridLayout.makeColumnsEqualWidth = true;
		designerComposite.setLayout(gridLayout);
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gridData.horizontalSpan = 6;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;		
		designerComposite.setLayoutData(gridData);

		new Label(designerComposite, SWT.NONE).setText("Designer:");		
		if (isCreating())
		{
			loadDesignerComboBox();
		}
		else
		{
			loadDesignerLabel();
		}
		
		new Label(parent, SWT.NONE).setText("Description:");
		
		descArea = new Text(parent, SWT.MULTI | SWT.WRAP | SWT.BORDER | SWT.V_SCROLL);
		descArea.setText(getDecision().getDescription());
		descArea.addModifyListener(getNeedsSaveListener());
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		DisplayUtilities.setTextDimensions(descArea, gridData, 50, 5);
		gridData.horizontalSpan = 5;
		gridData.heightHint = descArea.getLineHeight() * 5;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;
		descArea.setLayoutData(gridData);
		
		new Label(parent, SWT.NONE).setText("Type:");
		
		typeBox = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
		Enumeration typeEnum = DecisionType.elements();
//		System.out.println("got enum");
		int i = 0;
		DecisionType rtype;
		while (typeEnum.hasMoreElements())
		{
			rtype = (DecisionType) typeEnum.nextElement();
//			System.out.println("got next element");
			typeBox.add( rtype.toString());
			if (rtype.toString().compareTo(getDecision().getType().toString()) == 0)
			{
//				System.out.println(ourDec.getType().toString());
				typeBox.select(i);
//				System.out.println(i);
			}
			i++;
		}
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;
		typeBox.setLayoutData(gridData);
		typeBox.addModifyListener(getNeedsSaveListener());
		
		new Label(parent, SWT.NONE).setText("Status:");
		
		statusBox = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
		Enumeration statEnum = DecisionStatus.elements();
		int j=0;
		DecisionStatus stype;
		while (statEnum.hasMoreElements())
		{
			stype = (DecisionStatus) statEnum.nextElement();
			statusBox.add( stype.toString() );
			if (stype.toString().compareTo(getDecision().getStatus().toString()) == 0)
			{
//				System.out.println(ourDec.getStatus().toString());
				statusBox.select(j);
//				System.out.println(j);
			}
			j++;
		}
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;
		statusBox.setLayoutData(gridData);
		statusBox.addModifyListener(getNeedsSaveListener());
		
		new Label(parent, SWT.NONE).setText("DevelopmentPhase:");
		
		phaseBox = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
		Enumeration phaseEnum = Phase.elements();
		int k=0;
		Phase ptype;
		while (phaseEnum.hasMoreElements())
		{
			ptype = (Phase) phaseEnum.nextElement();
			phaseBox.add( ptype.toString() );
			if (ptype.toString().equals(getDecision().getPhase().toString()))
			{
//				System.out.println(ourDec.getPhase().toString());
				phaseBox.select(k);
			}
			k++;
		}
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;
		phaseBox.setLayoutData(gridData);
		phaseBox.addModifyListener(getNeedsSaveListener());

		// Register Event Notification
		try
		{
			RationaleDB.getHandle().Notifier().Subscribe(getDecision(), this, "onUpdate");
		}
		catch( Exception e )
		{
			System.out.println("Decision Editor: Decision Update Not Available!");
		}
		
		updateFormCache();
	}
	
	/* (non-Javadoc)
	 * @see SEURAT.editors.RationaleEditorBase#saveData()
	 */
	public boolean saveData() {
		ConsistencyChecker checker = new ConsistencyChecker(getDecision().getID(), nameField.getText(), "PatternDecisions");
		
		if(phaseBox.getSelectionIndex() != -1 && !nameField.getText().trim().equals("") &&
				(getDecision().getName() == nameField.getText() || checker.check())) {
			if (isCreating()) {
				getDecision().setParent(getParentElement());
				if ((designerBox.getItemCount() <= 0) || designerBox.getSelectionIndex() >= 0) {
					getDecision().setName(nameField.getText());
					getDecision().setDescription(descArea.getText());
					getDecision().setType(DecisionType.fromString(typeBox.getItem(typeBox.getSelectionIndex())));
					getDecision().setStatus( DecisionStatus.fromString(statusBox.getItem(statusBox.getSelectionIndex())));
					//				ourDec.setArtifact( artifactField.getText());
					getDecision().setPhase(Phase.fromString(phaseBox.getItem(phaseBox.getSelectionIndex())));
					getDecision().updateHistory(new History(getDecision().getStatus().toString(), "Initial Entry"));
					getDecision().setAlts(true);
					getDecision().setParentPattern(ourPattern.getID());
					getDecision().setParentID(ourPattern.getID());
					// TODO Wang uses PARENT instead of relationship set pattern_decision to determine which pattern is it.
					//This might not be a good way to implement it and we might need to change it some time.
					if (designerBox.getItemCount() > 0) {
						String designerName = designerBox
						.getItem(designerBox.getSelectionIndex());
						Designer ourDes = new Designer();
						ourDes.fromDatabase(designerName);
						getDecision().setDesigner(ourDes);
					}
					getDecision().setID(getDecision().toDatabase(ourPattern.getID(), ourPattern.getElementType()));
					
					System.out.println("our ID = " + getDecision().getID());
					return true;
				} else {
					String l_message = "";
					l_message += "Please specify the designer name.";
					MessageBox mbox = new MessageBox(getSite().getShell(), SWT.ICON_ERROR);
					mbox.setMessage(l_message);
					mbox.setText("Designer Name Is Invalid");
					mbox.open();
				}
			} else {
				getDecision().setName(nameField.getText());
				getDecision().setDescription(descArea.getText());
				getDecision().setType(DecisionType.fromString(typeBox.getItem(typeBox.getSelectionIndex())));
				DecisionStatus newStat = DecisionStatus.fromString(statusBox.getItem(statusBox.getSelectionIndex()));
				getDecision().setAlts(true);
				getDecision().setPhase(Phase.fromString(phaseBox.getItem(phaseBox.getSelectionIndex())));
				if (!newStat.toString().equals(getDecision().getStatus().toString())) {
						ReasonGUI rg = new ReasonGUI();
						//				rg.show();
						String newReason = rg.getReason();
						getDecision().setStatus(newStat);
						//				System.out.println(newStat.toString() + ourDec.getStatus().toString());
						History newHist = new History(newStat.toString(), newReason);
						getDecision().updateHistory(newHist);
						//				ourDec.toDatabase(ourDec.getParent(), RationaleElementType.fromString(ourDec.getPtype()));
						//				newHist.toDatabase(ourDec.getID(), RationaleElementType.Decision);
				}
				// since this is a save, not an add, the type and parent are ignored
				getDecision().setID(getDecision().toDatabase(getDecision().getParent(), getDecision().getPtype()));
				return true;
			}
		}	
		else
		{
			String l_message = "";
			l_message += "The pattern decision name you have specified is either already"
				+ " in use or empty. Or the development phase is empty. Please make sure that you have specified both"
				+ " a pattern decision name and a development phase, and the pattner decision name does not already exist"
				+ " in the database.";
			MessageBox mbox = new MessageBox(getSite().getShell(), SWT.ICON_ERROR);
			mbox.setMessage(l_message);
			mbox.setText("Pattern Decision Name Is Invalid");
			mbox.open();
		}
		return false;
	}
	
	/**
	 * Wraps a decision into a logical file
	 */
	public static class Input extends RationaleEditorInput {
		/**
		 * @param explorer RationaleExplorer
		 * @param tree the element in the RationaleExplorer tree for the decision
		 * @param parent the parent of the decision in the RationaleExplorer tree
		 * @param target the decision to associate with the logical file
		 * @param new1 true if the decision is being created, false if it already exists
		 */
		public Input(RationaleExplorer explorer, TreeParent tree,
				RationaleElement parent, RationaleElement target, boolean new1) {
			super(explorer, tree, parent, target, new1);
		}
		
		public Input(PatternLibrary patternLib, TreeParent tree,
				RationaleElement parent, RationaleElement target, boolean new1) {
			super(patternLib, tree, parent, target, new1);
		}
		/**
		 * @return the decision being wrapped by the logical file
		 */
		public PatternDecision getData() { return (PatternDecision)getAdapter(PatternDecision.class); }
		
		/* (non-Javadoc)
		 * @see SEURAT.editors.RationaleEditorInput#getName()
		 */
		@Override
		public String getName() {
			return isCreating() ? "New Pattern Decision Editor" :
				"Pattern Decision: " + getData().getName();
		}

		/* (non-Javadoc)
		 * @see SEURAT.editors.RationaleEditorInput#targetType(java.lang.Class)
		 */
		@Override
		public boolean targetType(Class type) {
			return type == PatternDecision.class;
		}		
	}
}

