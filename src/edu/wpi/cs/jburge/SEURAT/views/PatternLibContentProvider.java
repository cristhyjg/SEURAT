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

package edu.wpi.cs.jburge.SEURAT.views;

import java.util.Enumeration;

import java.util.Iterator;
import java.util.Vector;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;

import edu.wpi.cs.jburge.SEURAT.rationaleData.Decision;
import edu.wpi.cs.jburge.SEURAT.rationaleData.OntEntry;
import edu.wpi.cs.jburge.SEURAT.rationaleData.Pattern;
import edu.wpi.cs.jburge.SEURAT.rationaleData.PatternDecision;
import edu.wpi.cs.jburge.SEURAT.rationaleData.RationaleDB;
import edu.wpi.cs.jburge.SEURAT.rationaleData.RationaleElement;
import edu.wpi.cs.jburge.SEURAT.rationaleData.RationaleElementType;
import edu.wpi.cs.jburge.SEURAT.rationaleData.Requirement;

/**
 * This provides the content for the Pattern Library tree view.
 *
 */
public class PatternLibContentProvider implements IStructuredContentProvider, IDeltaListener,
ITreeContentProvider{
	
	protected TreeParent invisibleRoot;
	
	protected TreeViewer viewer;

	/**
	 * 
	 * Initializes the roots of the invisibleRoot object.
	 * @return
	 */
	public TreeParent initialize() {
		
		RationaleTreeMap map = RationaleTreeMap.getHandle();
		map.clearMap();
		
//		System.out.println("Start to initialize the pattern tree");
		TreeParent root = new TreeParent("Pattern Library - " + RationaleDB.getDbName(), RationaleElementType.RATIONALE);
				
		invisibleRoot = new TreeParent("", RationaleElementType.RATIONALE);
		invisibleRoot.addChild(root);		
		
		TreeParent architop = new TreeParent("Architectural Patterns", RationaleElementType.RATIONALE);
		root.addChild(architop);
		addArchitecturePatterns(architop, null, null);
		
		TreeParent designtop = new TreeParent("Design Patterns", RationaleElementType.RATIONALE);
		root.addChild(designtop);
		addDesignPatterns(designtop, null, null);
		
		TreeParent idiomtop = new TreeParent("Idioms", RationaleElementType.RATIONALE);
		root.addChild(idiomtop);
		addIdioms(idiomtop, null, null);

		return invisibleRoot;
	}
	
	/**
	 * Given parent and childName and type, create the child and att it to parent.
	 * Remember also to att parent to child.
	 * @param parent
	 * @param childName
	 */
	public void addElement(TreeParent parent, String childName, RationaleElementType type){
		TreeParent child = new TreeParent(childName, type);
		child.setParent(parent);
		parent.addChild(child);
	}
	
	/**
	 * First, remove all children of the child, then remove the element from the tree (from the parent of the child).
	 * @param child
	 */
	public void removeElement(TreeParent child){
		TreeParent parent = child.getParent();
		
		child.removeChildren();
		parent.removeChild(child, false);
	}
	
	/**
	 * In reverse DFS traversal order, remove all children of a pattern
	 * Base Case 1: Node is null, return
	 * Base Case 2: Node is a leaf, remove the node from the parent
	 * Recursion: For each node not a leaf, recursively call this function, and
	 * after all functions removes all descendants of this node, remove the node from its parent.
	 * @param child
	 */
	public void removePattern(TreeParent node){
		if (node == null) return;
		TreeObject[] children = node.getChildren();
		if (children.length == 0){
			node.getParent().removeChild(node, false);
			return;
		}
		
		for (int i = 0; i < children.length; i++){
			TreeObject child = children[i];
			if (child instanceof TreeParent){
				removePattern((TreeParent) child);
			}
			else{
				node.removeChild(child, false);
			}
		}
		
		node.getParent().removeChild(node, false);
	}
	
	
	/**
	 * Given parent and element to be created, add a new element to the tree's content.
	 * @param parent
	 * @param element
	 * @return
	 */
	public TreeParent addPattern(TreeParent parent, Pattern pattern) {
		
		TreeParent child = new TreeParent(pattern.getName(), RationaleElementType.PATTERN);
		parent.addChild(child);
		
		TreeParent ontTop = new TreeParent("Affected Quality Attributes", RationaleElementType.RATIONALE);
		child.addChild(ontTop);
		ontTop.setParent(child);
		TreeParent posiTop = new TreeParent("Positive", RationaleElementType.RATIONALE);
		TreeParent negaTop = new TreeParent("Negative", RationaleElementType.RATIONALE);
		ontTop.addChild(posiTop);
		ontTop.addChild(negaTop);
		negaTop.setParent(ontTop);
		posiTop.setParent(ontTop);

		TreeParent decTop = new TreeParent("Decisions", RationaleElementType.RATIONALE);
		decTop.setParent(child);
		child.addChild(decTop);
		
		return child;
	}
	
	/**
	 * Given the pattern and the ontology entry, create a new tree node, and a path
	 * linking the two of them.
	 * @param parent The pattern parent of this onotlogy entry
	 * @param entry The actual ontology entry to be added to the tree.
	 * @return The tree node that was just added to the tree.
	 */
	public TreeParent addOntology(TreeParent parent, OntEntry entry){
		Pattern pattern = new Pattern();
		pattern.fromDatabase(parent.getName());
		if (pattern.getID() < 0){
			System.err.println("Invalid pattern parent while adding ontology entry");
			return null;
		}
		//Determine whether this entry is positive or negative...
		Vector<OntEntry> posEntry = pattern.getPosiOnts();
		boolean isPositive = false;
		for (int i = 0; i < posEntry.size(); i++){
			if (posEntry.get(i).equals(entry)){
				isPositive = true;
			}
		}
		
		TreeParent ontParent = null;
		TreeObject patternChildren[] = parent.getChildren();
		for (int i = 0; i < patternChildren.length; i++){
			if (patternChildren[i].getName().equals("Affected Quality Attributes") && 
					patternChildren[i].getType().equals(RationaleElementType.RATIONALE)){
				ontParent = (TreeParent) patternChildren[i];
				break;
			}
		}
		
		//Now, get the corresponding root depending on whether the entry is positive or negative.
		TreeParent entryRoot = null;
		TreeObject ontChildren[] = ontParent.getChildren();
		for (int i = 0; i < ontChildren.length; i++){
			if (ontChildren[i].getName().equals("Positive") && isPositive){
				entryRoot = (TreeParent) ontChildren[i];
				break;
			}
			
			if (ontChildren[i].getName().equals("Negative") && !(isPositive) ){
				entryRoot = (TreeParent) ontChildren[i];
				break;
			}
		}
		
		if (entryRoot == null){
			System.err.println("Entry root is null while adding ontentry");
			return null;
		}
		
		//Finally, we add the entry to entryRoot
		TreeParent child = new TreeParent(entry.getName(), RationaleElementType.ONTENTRY);
		child.setParent(entryRoot);
		entryRoot.addChild(child);
		
		return child;
 	}
	
	/**
	 * Given pattern and element to be created, add a new element to the tree's content
	 * @param parent The pattern of this decision.
	 * @param decision The decision to be added
	 * @return The tree node of the newly created pattern decision.
	 */
	public TreeParent addDecision(TreeParent parent, PatternDecision decision){
		TreeParent decisionParent = null;
		for (int i = 0; i < parent.getChildren().length; i++){
			if (parent.getChildren()[i].getName().equals("Decisions") && 
					parent.getChildren()[i].getType().equals(RationaleElementType.RATIONALE)){
				decisionParent = (TreeParent) parent.getChildren()[i];
				break;
			}
		}
		
		if (decisionParent == null){
			System.err.println("Decision Parent is not determined from Pattern");
			return null;
		}
		
		TreeParent child = new TreeParent(decision.getName(), RationaleElementType.PATTERNDECISION);
		child.setParent(decisionParent);
		decisionParent.addChild(child);
		return child;
	}
	
	/**
	 * Add a new pattern element, create its ontology, positive, negative argument roots, as well as its
	 * decision root inside the parent object.
	 * @param parent The parent node to add the pattern
	 * @param parentName
	 * @param parentType
	 */
	private void addArchitecturePatterns(TreeParent parent, String parentName, RationaleElementType parentType){
		//System.out.println("Found something");
		
		RationaleDB d = RationaleDB.getHandle();
		Vector<TreeParent> patternList = d.getArchitecturePatterns();
		Enumeration<TreeParent> patterns =patternList.elements();
		while (patterns.hasMoreElements())
		{
			TreeParent child = (TreeParent) patterns.nextElement();
			String childName = child.getName();
			parent.addChild(child);
			child.setParent(parent);
			
			//Pattern childPattern = (Pattern)patterns.nextElement();
			//String OntName = patterns.nextElement().posiOnt.getName();
			
			TreeParent ontTop = new TreeParent("Affected Quality Attributes", RationaleElementType.RATIONALE);
			child.addChild(ontTop);
			ontTop.setParent(child);
			TreeParent posiTop = new TreeParent("Positive", RationaleElementType.RATIONALE);
			TreeParent negaTop = new TreeParent("Negative", RationaleElementType.RATIONALE);
			ontTop.addChild(posiTop);
			posiTop.setParent(ontTop);
			ontTop.addChild(negaTop);
			negaTop.setParent(ontTop);
			
			addOntEntries(posiTop, childName, true);
			addOntEntries(negaTop, childName, false);
			
			TreeParent decTop = new TreeParent("Decisions", RationaleElementType.RATIONALE);
			child.addChild(decTop);
			decTop.setParent(child);
			addSubDecisions(decTop, childName, parentName);
		}	
	}
	
	/**
	 * This method adds ontology entries to the pattern library
	 * @param parent
	 * @param parentName
	 * @param isPositive
	 */
	private void addOntEntries(TreeParent parent, String parentName, boolean isPositive)
	{
		RationaleDB d = RationaleDB.getHandle();
		Vector ontList = d.getPatternOntologies(parentName, isPositive);
		Enumeration onts = ontList.elements();
		while (onts.hasMoreElements())
		{
			String childName = onts.nextElement().toString();
			TreeParent child = new TreeParent(childName,
					RationaleElementType.ONTENTRY);
			parent.addChild(child);
			//addOntology(child, childName);
		}
	}
	
	/**
	 * This method adds sub-decisions to the pattern library.
	 * @param parent
	 * @param parentName
	 * @param grandParentName
	 */
	private void addSubDecisions(TreeParent parent, String parentName, String grandParentName){
		RationaleDB d = RationaleDB.getHandle();
		Vector subDecisions = d.getPatternDecisions(parentName, grandParentName);
		Enumeration subs = subDecisions.elements();
		while (subs.hasMoreElements())
		{
			String childName = subs.nextElement().toString();
			TreeParent child = new TreeParent(childName,
					RationaleElementType.PATTERNDECISION);
			parent.addChild(child);
			addCandidatePatterns(child, childName);//addOntology(child, childName);
			//addDecisions(child, childName, RationaleElementType.DECISION);
		}
	}
	
	/**
	 * This method helps to add candidate patterns to pattern library
	 * @param parent
	 * @param parentName
	 */
	private void addCandidatePatterns(TreeParent parent, String parentName){
		RationaleDB d = RationaleDB.getHandle();
		PatternDecision parentPatternDecision = new PatternDecision();
		parentPatternDecision.fromDatabase(parentName);
		Vector candidatePatterns = d.getCandidatePatterns(parentName);
		Enumeration candidates = candidatePatterns.elements();
		while (candidates.hasMoreElements()){
			String childName = ((Pattern)candidates.nextElement()).getName();
			TreeParent child = new TreeParent(childName, RationaleElementType.PATTERN);
			parent.addChild(child);
		}
	}
	
	/**
	 * @deprecated Not used
	 * @param parent
	 * @param parentName
	 * @param parentType
	 */
	private void addDecisions(TreeParent parent, String parentName, RationaleElementType parentType)
	{
		RationaleDB d = RationaleDB.getHandle();
		Vector reqList = d.getDecisions(parentName, parentType);
		Enumeration decs = reqList.elements();
		while (decs.hasMoreElements())
		{
//			String childName = (String) decs.nextElement();
//			TreeParent child = new TreeParent(childName, 
//			RationaleElementType.DECISION);
			TreeParent child = (TreeParent) decs.nextElement();
			String childName = child.getName(); 
			parent.addChild(child);
			//add alternatives...
			//addAlternatives(child, childName, RationaleElementType.DECISION);
			//add questions...
			//addQuestions(child, childName, RationaleElementType.DECISION);
			//add sub-decisions
			addDecisions(child, childName, RationaleElementType.DECISION);
			//add sub-decisions....
			//addConstraints(child, childName, RationaleElementType.DECISION);
			//add candidate patterns
			//addPatterns(child, childName);
			
		}			
	}
	
	/**
	 * This method adds design patterns to the pattern library.
	 * @param parent
	 * @param parentName
	 * @param parentType
	 */
	private void addDesignPatterns(TreeParent parent, String parentName, RationaleElementType parentType){
		
		RationaleDB d = RationaleDB.getHandle();
		Vector patternList = d.getDesignPatterns();
		Enumeration patterns =patternList.elements();
		while (patterns.hasMoreElements())
		{
			TreeParent child = (TreeParent) patterns.nextElement();
			String childName = child.getName();
			parent.addChild(child);
			
			TreeParent ontTop = new TreeParent("Affected Quality Attributes", RationaleElementType.RATIONALE);
			child.addChild(ontTop);
			TreeParent posiTop = new TreeParent("Positive", RationaleElementType.RATIONALE);
			TreeParent negaTop = new TreeParent("Negative", RationaleElementType.RATIONALE);
			ontTop.addChild(posiTop);
			ontTop.addChild(negaTop);
			
			addOntEntries(posiTop, childName, true);
			addOntEntries(negaTop, childName, false);
			
			TreeParent decTop = new TreeParent("Decisions", RationaleElementType.RATIONALE);
			child.addChild(decTop);
			addSubDecisions(decTop, childName, parentName);
		}		
	}
	
	/**
	 * This method adds idiom patterns to pattern library.
	 * @param parent
	 * @param parentName
	 * @param parentType
	 */
	private void addIdioms(TreeParent parent, String parentName, RationaleElementType parentType){
		
		RationaleDB d = RationaleDB.getHandle();
		Vector patternList = d.getIdioms();
		Enumeration patterns =patternList.elements();
		while (patterns.hasMoreElements())
		{
			TreeParent child = (TreeParent) patterns.nextElement();
			String childName = child.getName();
			parent.addChild(child);
			
			TreeParent ontTop = new TreeParent("Affected Quality Attributes", RationaleElementType.RATIONALE);
			child.addChild(ontTop);
			TreeParent posiTop = new TreeParent("Positive", RationaleElementType.RATIONALE);
			TreeParent negaTop = new TreeParent("Negative", RationaleElementType.RATIONALE);
			ontTop.addChild(posiTop);
			ontTop.addChild(negaTop);
			
			addOntEntries(posiTop, childName, true);
			addOntEntries(negaTop, childName, false);
			
			TreeParent decTop = new TreeParent("Decisions", RationaleElementType.RATIONALE);
			child.addChild(decTop);
			addSubDecisions(decTop, childName, parentName);
		}		
	} 

	public Object[] getElements(Object parent) {
		if (parent.equals(ResourcesPlugin.getWorkspace())) {
			if (invisibleRoot==null)
			{
				System.out.println("root is null?");
				initialize();
			} 
			return getChildren(invisibleRoot);
		}
		return getChildren(parent);
	}

	public void dispose() {
		// TODO Auto-generated method stub
		
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.viewer = (TreeViewer)viewer;
		System.out.println("viewer ok");
		if (!(oldInput instanceof TreeParent))
		{
			System.out.println("not a TreeParent???");
			if (oldInput instanceof TreeObject)
			{
				System.out.println("it is a tree object");
			}
			return;
		}
		if(oldInput != null) {
			removeListenerFrom((TreeParent)oldInput);
		}
		if(newInput != null) {
			addListenerTo((TreeParent)newInput);
		}
		
	}
	
	protected void addListenerTo(TreeParent element) {
		element.addListener(this);
		for (Iterator iterator = element.getIterator(); iterator.hasNext();) {
			TreeParent aElement = (TreeParent) iterator.next();
			addListenerTo(aElement);
		}
	}
	
	protected void removeListenerFrom(TreeParent element) {
		element.removeListener(this);
		for (Iterator iterator = element.getIterator(); iterator.hasNext();) {
			TreeParent nextEl = (TreeParent) iterator.next();
			removeListenerFrom(nextEl);
		}
	}

	public void add(DeltaEvent event) {
		Object ourObj = ((TreeParent)event.receiver()).getParent();
		viewer.refresh(ourObj, false);
		
	}

	public void remove(DeltaEvent event) {
		add(event);
		
	}

	public Object[] getChildren(Object parent) {
		if (parent instanceof TreeParent) {
			return ((TreeParent)parent).getChildren();
		}
		return new Object[0];
	}

	public Object getParent(Object child) {
		if (child instanceof TreeObject) {
			return ((TreeObject)child).getParent();
		}
		return null;
	}

	public boolean hasChildren(Object parent) {
		if (parent instanceof TreeParent)
			return ((TreeParent)parent).hasChildren();
		return false;
	}


	
}
