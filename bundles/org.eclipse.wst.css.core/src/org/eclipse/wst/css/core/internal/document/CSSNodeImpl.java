/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.wst.css.core.internal.document;



import java.util.Iterator;

import org.eclipse.wst.css.core.document.ICSSDocument;
import org.eclipse.wst.css.core.document.ICSSModel;
import org.eclipse.wst.css.core.document.ICSSNamedNodeMap;
import org.eclipse.wst.css.core.document.ICSSNode;
import org.eclipse.wst.css.core.document.ICSSNodeList;
import org.eclipse.wst.css.core.internal.formatter.CSSSourceFormatterFactory;
import org.eclipse.wst.css.core.internal.formatter.CSSSourceGenerator;
import org.eclipse.wst.css.core.util.ImportRuleCollector;
import org.eclipse.wst.sse.core.AbstractNotifier;
import org.eclipse.wst.sse.core.FactoryRegistry;
import org.eclipse.wst.sse.core.IndexedRegion;
import org.w3c.dom.DOMException;


/**
 * 
 */
abstract class CSSNodeImpl extends AbstractNotifier implements ICSSNode, IndexedRegion {

	private CSSDocumentImpl fOwnerDocument = null;
	private CSSNodeImpl fParentNode = null;
	private CSSNodeImpl fNextSibling = null;
	private CSSNodeImpl fPreviousSibling = null;
	private CSSNodeImpl fFirstChild = null;
	private CSSNodeImpl fLastChild = null;
	protected CSSNamedNodeMapImpl fAttrs = null;

	/**
	 * CSSNodeImpl constructor comment.
	 */
	CSSNodeImpl() {
		super();
	}

	/**
	 * @param that
	 *            com.ibm.sed.css.treemodel.CSSNodeImpl
	 */
	CSSNodeImpl(CSSNodeImpl that) {
		if (that != null) {
			this.fOwnerDocument = that.fOwnerDocument;
			if (that.fAttrs != null) {
				int nAttrs = that.fAttrs.getLength();
				for (int i = 0; i < nAttrs; i++) {
					CSSAttrImpl attr = (CSSAttrImpl) that.fAttrs.item(i);
					setAttribute(attr.getName(), attr.getValue());
				}
			}
		}
	}

	/**
	 * @return com.ibm.sed.css.treemodel.CSSNodeImpl
	 * @param newChild
	 *            com.ibm.sed.css.treemodel.CSSNodeImpl
	 * @exception org.w3c.dom.DOMException
	 *                The exception description.
	 */
	protected CSSNodeImpl appendChild(CSSNodeImpl newChild) throws org.w3c.dom.DOMException {
		return insertBefore(newChild, null);
	}

	/**
	 * @param newParent
	 *            com.ibm.sed.css.interfaces.ICSSNode
	 * @param deep
	 *            boolean
	 */
	void cloneChildNodes(ICSSNode newParent, boolean deep) {
		if (newParent == null || newParent == this)
			return;

		CSSNodeImpl container = (CSSNodeImpl) newParent;
		container.removeChildNodes();

		for (ICSSNode child = getFirstChild(); child != null; child = child.getNextSibling()) {
			CSSNodeImpl cloned = (CSSNodeImpl) child.cloneNode(deep);
			if (cloned != null)
				container.appendChild(cloned);
		}
	}

	/**
	 * @return boolean
	 * @param offset
	 *            int
	 */
	public boolean contains(int offset) {
		return (getStartOffset() <= offset && offset < getEndOffset());
	}

	/**
	 * @return java.lang.String
	 */
	String generateSource() {
		CSSSourceGenerator formatter = CSSSourceFormatterFactory.getInstance().getSourceFormatter(this);
		return formatter.format(this).toString();
	}

	/**
	 * @return java.lang.String
	 * @param name
	 *            java.lang.String
	 */
	String getAttribute(String name) {
		CSSAttrImpl attr = getAttributeNode(name);
		if (attr != null)
			return attr.getValue();
		return null;
	}

	/**
	 * @return com.ibm.sed.css.treemodel.CSSAttrImpl
	 * @param name
	 *            java.lang.String
	 */
	protected CSSAttrImpl getAttributeNode(String name) {
		if (fAttrs == null)
			return null;

		int nAttrs = fAttrs.getLength();
		for (int i = 0; i < nAttrs; i++) {
			CSSAttrImpl attr = (CSSAttrImpl) fAttrs.item(i);
			if (attr.matchName(name))
				return attr;
		}
		return null;
	}

	/**
	 * @return org.eclipse.wst.css.core.model.interfaces.ICSSNamedNodeMap
	 */
	public ICSSNamedNodeMap getAttributes() {
		if (fAttrs == null)
			fAttrs = new CSSNamedNodeMapImpl();
		return fAttrs;
	}

	/**
	 * @return com.ibm.sed.css.treemodel.CSSNodeList
	 */
	public ICSSNodeList getChildNodes() {
		CSSNodeListImpl list = new CSSNodeListImpl();
		for (ICSSNode node = getFirstChild(); node != null; node = node.getNextSibling()) {
			list.appendNode(node);
		}
		return list;
	}

	/**
	 * @return com.ibm.sed.css.interfaces.ICSSNode
	 * @param node
	 *            com.ibm.sed.css.interfaces.ICSSNode
	 */
	ICSSNode getCommonAncestor(ICSSNode node) {
		if (node == null)
			return null;

		for (ICSSNode na = node; na != null; na = na.getParentNode()) {
			for (ICSSNode ta = this; ta != null; ta = ta.getParentNode()) {
				if (ta == na)
					return ta;
			}
		}

		return null; // not found
	}

	/**
	 * @return com.ibm.sed.css.treemodel.CSSDocumentImpl
	 */
	CSSDocumentImpl getContainerDocument() {
		for (ICSSNode node = this; node != null; node = node.getParentNode()) {
			if (node instanceof CSSDocumentImpl) {
				CSSDocumentImpl doc = (CSSDocumentImpl) node;
				if (doc.isDocument())
					return doc;
			}
		}
		return null;
	}

	/**
	 * @return com.ibm.sed.css.treemodel.CSSNodeImpl
	 * @param offset
	 *            int
	 */
	CSSNodeImpl getContainerNode(int offset) {
		if (!contains(offset))
			return null;

		for (ICSSNode child = getFirstChild(); child != null; child = child.getNextSibling()) {
			ICSSNode found = ((CSSNodeImpl) child).getContainerNode(offset);
			if (found != null)
				return (CSSNodeImpl) found;
		}

		return this;
	}

	/**
	 */
	public FactoryRegistry getFactoryRegistry() {
		ICSSModel model = getOwnerDocument().getModel();
		if (model != null) {
			FactoryRegistry reg = model.getFactoryRegistry();
			if (reg != null)
				return reg;
		}
		return null;
	}

	/**
	 * @return com.ibm.sed.css.treemodel.CSSNode
	 */
	public ICSSNode getFirstChild() {
		return this.fFirstChild;
	}

	/**
	 * @return com.ibm.sed.css.treemodel.CSSNode
	 */
	public ICSSNode getLastChild() {
		return this.fLastChild;
	}

	/**
	 * @return com.ibm.sed.css.treemodel.CSSNode
	 */
	public ICSSNode getNextSibling() {
		return this.fNextSibling;
	}

	/**
	 * @return com.ibm.sed.css.interfaces.ICSSNode
	 * @param offset
	 *            int
	 */
	ICSSNode getNodeAt(int offset) {
		// the same as getContainerNode()
		return getContainerNode(offset);
	}

	/**
	 * @return com.ibm.sed.css.treemodel.CSSDocument
	 */
	public ICSSDocument getOwnerDocument() {
		return this.fOwnerDocument;
	}

	/**
	 * @return com.ibm.sed.css.treemodel.CSSNode
	 */
	public ICSSNode getParentNode() {
		return this.fParentNode;
	}

	/**
	 * @return com.ibm.sed.css.treemodel.CSSNode
	 */
	public ICSSNode getPreviousSibling() {
		return this.fPreviousSibling;
	}

	/**
	 * @return com.ibm.sed.css.interfaces.CSSFactory
	 */
	ICSSNode getRootNode() {
		CSSNodeImpl parent = (CSSNodeImpl) getParentNode();
		if (parent == null)
			return this;
		return parent.getRootNode();
	}

	/**
	 * @return boolean
	 */
	public boolean hasChildNodes() {
		return (this.fFirstChild != null);
	}

	/**
	 * @return boolean
	 */
	public boolean hasProperties() {
		return false;
	}

	/**
	 * @return com.ibm.sed.css.treemodel.CSSNodeImpl
	 * @param newChild
	 *            com.ibm.sed.css.treemodel.CSSNodeImpl
	 * @param refChild
	 *            com.ibm.sed.css.treemodel.CSSNodeImpl
	 * @exception org.w3c.dom.DOMException
	 *                The exception description.
	 */
	protected CSSNodeImpl insertBefore(CSSNodeImpl newChild, CSSNodeImpl refChild) throws org.w3c.dom.DOMException {
		if (newChild == null)
			return null;

		CSSNodeImpl child = newChild;
		CSSNodeImpl next = refChild;
		CSSNodeImpl prev = null;
		if (next == null) {
			prev = this.fLastChild;
			this.fLastChild = child;
		}
		else {
			prev = (CSSNodeImpl) next.getPreviousSibling();
			next.setPreviousSibling(child);
		}

		if (prev == null)
			this.fFirstChild = child;
		else
			prev.setNextSibling(child);
		child.setPreviousSibling(prev);
		child.setNextSibling(next);
		child.setParentNode(this);

		notifyChildReplaced(child, null);

		return newChild;
	}

	/**
	 * @param newAttr
	 *            com.ibm.sed.css.treemodel.CSSNodeImpl
	 * @param oldAttr
	 *            com.ibm.sed.css.treemodel.CSSNodeImpl
	 */
	protected void notifyAttrReplaced(CSSNodeImpl newAttr, CSSNodeImpl oldAttr) {
		// for model
		ICSSDocument doc = getContainerDocument();
		if (doc == null)
			return;
		CSSModelImpl model = (CSSModelImpl) doc.getModel();
		if (model == null)
			return;
		model.attrReplaced(this, newAttr, oldAttr);

		// for adapters
		int type = CHANGE;
		if (newAttr == null)
			type = REMOVE;
		else if (oldAttr == null)
			type = ADD;
		notify(type, oldAttr, oldAttr, newAttr, getStartOffset());
	}

	/**
	 * @param newChild
	 *            com.ibm.sed.css.treemodel.CSSNode
	 * @param oldChild
	 *            com.ibm.sed.css.treemodel.CSSNode
	 */
	protected void notifyChildReplaced(CSSNodeImpl newChild, CSSNodeImpl oldChild) {
		// for model
		ICSSDocument doc = getContainerDocument();
		if (doc == null)
			return;
		CSSModelImpl model = (CSSModelImpl) doc.getModel();
		if (model == null)
			return;
		model.childReplaced(this, newChild, oldChild);

		// for adapters
		int type = CHANGE;
		if (newChild == null)
			type = REMOVE;
		else if (oldChild == null)
			type = ADD;
		notify(type, oldChild, oldChild, newChild, getStartOffset());
	}

	/**
	 * @param attr
	 *            com.ibm.sed.css.treemodel.CSSNodeImpl
	 */
	void removeAttributeNode(CSSNodeImpl attr) {
		// find
		int nAttrs = fAttrs.getLength();
		for (int i = 0; i < nAttrs; i++) {
			if (fAttrs.item(i) == attr) {
				fAttrs.removeNode(i);
				notifyAttrReplaced(null, attr);
				return;
			}
		}
	}

	/**
	 * @return com.ibm.sed.css.treemodel.CSSNodeImpl
	 * @param newChild
	 *            com.ibm.sed.css.treemodel.CSSNodeImpl
	 * @exception org.w3c.dom.DOMException
	 *                The exception description.
	 */
	protected CSSNodeImpl removeChild(CSSNodeImpl oldChild) throws org.w3c.dom.DOMException {
		if (oldChild == null)
			return null;
		if (oldChild.getParentNode() != this)
			return null;

		// close import rules
		ImportRuleCollector trav = new ImportRuleCollector();
		trav.apply(oldChild);
		Iterator it = trav.getRules().iterator();
		while (it.hasNext()) {
			((CSSImportRuleImpl) it.next()).closeStyleSheet();
		}

		CSSNodeImpl child = oldChild;
		CSSNodeImpl prev = (CSSNodeImpl) child.getPreviousSibling();
		CSSNodeImpl next = (CSSNodeImpl) child.getNextSibling();

		if (prev == null)
			this.fFirstChild = next;
		else
			prev.setNextSibling(next);

		if (next == null)
			this.fLastChild = prev;
		else
			next.setPreviousSibling(prev);

		child.setPreviousSibling(null);
		child.setNextSibling(null);
		child.setParentNode(null);

		notifyChildReplaced(null, child);

		return child;
	}

	/**
	 * 
	 */
	void removeChildNodes() {
		ICSSNode nextChild = null;
		for (ICSSNode child = getFirstChild(); child != null; child = nextChild) {
			nextChild = child.getNextSibling();
			removeChild((CSSNodeImpl) child);
		}
	}

	/**
	 * @return com.ibm.sed.css.treemodel.CSSNodeImpl
	 * @param newChild
	 *            com.ibm.sed.css.treemodel.CSSNodeImpl
	 * @param oldChild
	 *            com.ibm.sed.css.treemodel.CSSNodeImpl
	 * @exception org.w3c.dom.DOMException
	 *                The exception description.
	 */
	protected CSSNodeImpl replaceChild(CSSNodeImpl newChild, CSSNodeImpl oldChild) throws org.w3c.dom.DOMException {
		if (oldChild == null)
			return newChild;
		if (newChild != null)
			insertBefore(newChild, oldChild);
		return removeChild(oldChild);
	}

	/**
	 * @param name
	 *            java.lang.String
	 * @param value
	 *            java.lang.String
	 */
	void setAttribute(String name, String value) {
		if (name == null)
			return;

		CSSAttrImpl attr = getAttributeNode(name);
		if (attr != null) {
			String oldValue = attr.getValue();
			if (value != null && value.equals(oldValue))
				return;
			if (value == null) {
				if (oldValue != null) {
					removeAttributeNode(attr);
				}
				return;
			}
		}
		else {
			if (value == null)
				return;
			if (fAttrs == null)
				fAttrs = new CSSNamedNodeMapImpl();
			CSSDocumentImpl doc = (CSSDocumentImpl) getOwnerDocument();
			if (doc == null)
				return;
			attr = (CSSAttrImpl) doc.createCSSAttr(name);
			attr.setOwnerCSSNode(this);
			fAttrs.appendNode(attr);
			notifyAttrReplaced(attr, null);
		}
		attr.setValue(value);
	}

	/**
	 * @param cssText
	 *            java.lang.String
	 */
	public void setCssText(String cssText) {
		// TODO : call flat model parser and replace myself with new three!!
		throw new DOMException(DOMException.INVALID_MODIFICATION_ERR, "");//$NON-NLS-1$
	}

	/**
	 * @param nextSibling
	 *            com.ibm.sed.css.interfaces.ICSSNode
	 */
	private void setNextSibling(ICSSNode nextSibling) {
		this.fNextSibling = (CSSNodeImpl) nextSibling;
	}

	/**
	 * @param ownerDocument
	 *            com.ibm.sed.css.interfaces.CSSDocument
	 */
	void setOwnerDocument(ICSSDocument ownerDocument) {
		this.fOwnerDocument = (CSSDocumentImpl) ownerDocument;
	}

	/**
	 * @param parentNode
	 *            com.ibm.sed.css.interfaces.ICSSNode
	 */
	private void setParentNode(ICSSNode parentNode) {
		this.fParentNode = (CSSNodeImpl) parentNode;
	}

	/**
	 * @param previousSibling
	 *            com.ibm.sed.css.interfaces.ICSSNode
	 */
	private void setPreviousSibling(ICSSNode previousSibling) {
		this.fPreviousSibling = (CSSNodeImpl) previousSibling;
	}

	public int getLength() {
		int result = -1;
		int start = getStartOffset();
		if (start >= 0) {
			int end = getEndOffset();
			if (end >= 0) {
				result = end - start;
				if (result < -1) {
					result = -1;
				}
			}
		}
		return result;
	}
}