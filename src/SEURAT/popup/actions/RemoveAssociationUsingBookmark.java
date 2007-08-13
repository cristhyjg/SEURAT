package SEURAT.popup.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

/**
 * This method does not appear to do anything and is not invoked.
 * See RemoveBookmarkAssociation in the actions package.
 * @author burgeje
 *
 */
public class RemoveAssociationUsingBookmark implements IObjectActionDelegate {
	
	/**
	 * Constructor for Action1.
	 */
	public RemoveAssociationUsingBookmark() {
		super();
	}
	
	/**
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}
	
	/**
	 * Run the action
	 * @param action the action to execute
	 */
	public void run(IAction action) {
		Shell shell = new Shell();
		MessageDialog.openInformation(
				shell,
				"SEURAT Plug-in",
		"Remove Association was executed.");
	}
	
	/**
	 * Called when the selection changes
	 * @param action the action
	 * @param selection the selection
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}
	
}
