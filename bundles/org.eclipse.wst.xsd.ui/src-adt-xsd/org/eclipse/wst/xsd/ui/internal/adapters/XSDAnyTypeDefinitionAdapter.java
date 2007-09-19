/*******************************************************************************
 * Copyright (c) 2001, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.wst.xsd.ui.internal.adapters;

import org.eclipse.wst.xsd.ui.internal.adt.facade.IADTObject;

public class XSDAnyTypeDefinitionAdapter extends XSDTypeDefinitionAdapter
{
  public boolean isComplexType()
  {
    return false;
  }

  public boolean isFocusAllowed()
  {
    return false;
  }

  public String[] getActions(Object object)
  {
	return null;
  }

  public boolean isAnonymous()
  {
    return false;
  }

  public IADTObject getTopContainer()
  {
    return null;
  } 
}
