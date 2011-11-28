/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is the Text Trix code.
 *
 * The Initial Developer of the Original Code is
 * Text Flex.
 * Portions created by the Initial Developer are Copyright (C) 2011
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s): David Young <david@textflex.com>
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */

package com.textflex.texttrix;

import javax.swing.JPopupMenu;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Listener to pop up a context menu when right-clicking
 * on the status bar.
 * 
 * @author davit
 */
class StatusBarPopupListener extends MouseAdapter {
	
	private JPopupMenu statusBarPopup;
	
	public StatusBarPopupListener(JPopupMenu aStatusBarPopup) {
		super();
		statusBarPopup = aStatusBarPopup;
	}

	/**
	 * Press right mouse button.
	 * Responds to requests from Macs.
	 *  
	 */
	public void mousePressed(MouseEvent e) {
		if (e.isPopupTrigger()) {
			statusBarPopup.show(e.getComponent(), e.getX(), e.getY());
		}
	}

	/**
	 * Release right mouse button.
	 * Responds to requests from Windows/Linux.
	 *  
	 */
	public void mouseReleased(MouseEvent e) {
		if (e.isPopupTrigger()) {
			statusBarPopup.show(e.getComponent(), e.getX(), e.getY());
		}
	}
}
