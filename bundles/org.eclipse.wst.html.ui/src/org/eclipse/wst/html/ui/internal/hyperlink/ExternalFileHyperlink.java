package org.eclipse.wst.html.ui.internal.hyperlink;

import java.io.File;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.wst.html.ui.internal.Logger;

/**
 * Hyperlink for external files.
 */
class ExternalFileHyperlink implements IHyperlink {
	// copies of this class exist in:
	// org.eclipse.wst.xml.ui.internal.hyperlink
	// org.eclipse.wst.html.ui.internal.hyperlink
	// org.eclipse.jst.jsp.ui.internal.hyperlink
	
	private IRegion fHyperlinkRegion;
	private File fHyperlinkFile;

	public ExternalFileHyperlink(IRegion region, File file) {
		fHyperlinkFile = file;
		fHyperlinkRegion = region;
	}
	
	public IRegion getHyperlinkRegion() {
		return fHyperlinkRegion;
	}

	public String getTypeLabel() {
		return null;
	}

	public String getHyperlinkText() {
		return null;
	}

	public void open() {
		if (fHyperlinkFile != null) {
			IEditorInput input = new ExternalFileEditorInput(fHyperlinkFile);
			IEditorDescriptor descriptor;
			try {
				descriptor = IDE.getEditorDescriptor(input.getName(), true);
				if (descriptor != null) {
					IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
					IDE.openEditor(page, input, descriptor.getId(), true);
				}
			}
			catch (PartInitException e) {
				Logger.log(Logger.WARNING_DEBUG, e.getMessage(), e);
			}
		}
	}
}