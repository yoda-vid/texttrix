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

import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JDialog;
import java.awt.Container;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ComponentListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ActionEvent;
import javax.swing.Action;
import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

/** The dialog window that holds the Line Dance components.
 * The dialog is modeled after PlugInWindow dialogs.
 * One dialog is created for all Text Pads, but each pad has
 * its own Line Dance panel and associated table.
 */
class LineDanceDialog extends JDialog {

	private static final String LINE_DANCE = "LineDance";
	
	JPanel padPanel = new JPanel(); // Line Dance panel
	Container contentPane = null; // content pane for the dialog
	TextTrix ttx;
	
	/** Constructs a Line Dance dialog, including its main panel
	 * and table.
	 */
	public LineDanceDialog(TextTrix aTtx) {
		// Setup the owner and title
		super(aTtx, "Line Dance (Bookmarks)");
		ttx = aTtx;
		
		// Setup the content pane and its layout
		contentPane = getContentPane();
		contentPane.setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.anchor = GridBagConstraints.SOUTH;
		
		// Get the size from the saved preferences
		ttx.getPrefs().applyPlugInSizeLoc(this, LINE_DANCE, 350, 300);

		// store window size and location with each movement
		ComponentListener compListener = new ComponentListener() {
			public void componentMoved(ComponentEvent evt) {
				ttx.getPrefs().storePlugInLocation(LINE_DANCE,
						getLocation());
			}

			public void componentResized(ComponentEvent evt) {
				ttx.getPrefs().storePlugInSize(LINE_DANCE, getWidth(),
						getHeight());
			}

			public void componentShown(ComponentEvent evt) {
			}

			public void componentHidden(ComponentEvent evt) {
			}
		};
		addComponentListener(compListener);
		
		
		// Runs the plug-in if the user hits the "Remember Line"
		// button;
		// creates a shortcut key (alt-L) as an alternative way to invoke
		// the button
		Action remCurrLineAction = 
			new AbstractAction("Remember line", null) {
			public void actionPerformed(ActionEvent e) {
				TextPad p = ttx.getSelectedTextPad();
				p.remLineNum(p.getSelectedText());
			}
		};
		LibTTx.setAcceleratedAction(
			remCurrLineAction,
			"Remember line",
			'R',
			KeyStroke.getKeyStroke("alt R"));
		JButton remCurrLineBtn = new JButton(remCurrLineAction);
		
		
		
		// Runs the plug-in if the user hits the "Forget Line"
		// button;
		// creates a shortcut key (alt-L) as an alternative way to invoke
		// the button
		Action forgetSelLineAction = 
			new AbstractAction("Forget line", null) {
			public void actionPerformed(ActionEvent e) {
				ttx.getSelectedTextPad().forgetSelectedLines();
			}
		};
		LibTTx.setAcceleratedAction(
			forgetSelLineAction,
			"Forget line",
			'F',
			KeyStroke.getKeyStroke("alt F"));
		JButton forgetSelLineBtn = new JButton(forgetSelLineAction);
		
		
		// Runs the plug-in if the user hits the "Line Dance"
		// button;
		// creates a shortcut key (alt-L) as an alternative way to invoke
		// the button
		Action lineDanceAction = 
			new AbstractAction("Line Dance (jump to line)", null) {
			public void actionPerformed(ActionEvent e) {
				lineDance();
			}
		};
		LibTTx.setAcceleratedAction(
			lineDanceAction,
			"Line Dance",
			'L',
			KeyStroke.getKeyStroke("alt L"));
		JButton lineDanceBtn = new JButton(lineDanceAction);
		
		
		
		// Runs the plug-in if the user hits the "Name Line"
		// button;
		// creates a shortcut key (alt-L) as an alternative way to invoke
		// the button
		Action nameLineAction = 
			new AbstractAction("Rename Line", null) {
			public void actionPerformed(ActionEvent e) {
				ttx.getSelectedTextPad().editLineName();
			}
		};
		LibTTx.setAcceleratedAction(
			nameLineAction,
			"Rename Line",
			'N',
			KeyStroke.getKeyStroke("alt N"));
		JButton nameLineBtn = new JButton(nameLineAction);
		
		
		
		
		// Add the components
		LibTTx.addGridBagComponent(
			remCurrLineBtn,
			constraints,
			0,
			1,
			1,
			1,
			100,
			0,
			contentPane);
		
		LibTTx.addGridBagComponent(
			nameLineBtn,
			constraints,
			1,
			1,
			1,
			1,
			100,
			0,
			contentPane);
		
		LibTTx.addGridBagComponent(
			forgetSelLineBtn,
			constraints,
			2,
			1,
			1,
			1,
			100,
			0,
			contentPane);
		
		LibTTx.addGridBagComponent(
			lineDanceBtn,
			constraints,
			0,
			2,
			3,
			1,
			100,
			0,
			contentPane);
	}
	
	/** Moves the cursor to the position remembered in 
	 * the selected table line entry.
	 */
	public void lineDance() {
		TextPad pad = ttx.getSelectedTextPad();
		pad.lineDance();
	}
	
	/** Updates the panel dialog with the current
	 * Text Pad's panel.
	 */
	public void updatePadPanel() {
		// Remove the old panel
		contentPane.remove(padPanel);
		contentPane.validate();
		
		// Retrieve and add the current pad's panel
		TextPad pad = ttx.getSelectedTextPad();
		// gets panel only if pad exists and is selected
		if (pad != null) {
			// gets the panel
			padPanel = pad.getLineDancePanel();
			// sets up the layout
			GridBagConstraints constraints = new GridBagConstraints();
			constraints.fill = GridBagConstraints.HORIZONTAL;
			constraints.anchor = GridBagConstraints.NORTH;
			
			// adds the panel
			LibTTx.addGridBagComponent(
				padPanel,
				constraints,
				0,
				0,
				3,
				1,
				100,
				0,
				contentPane);
			contentPane.validate();
		}
	}
	
}
