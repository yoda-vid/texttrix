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
 * Portions created by the Initial Developer are Copyright (C) 2006-7
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s): David Young <david@textflex.com>
 * --> YOUR NAME HERE <--
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

import javax.swing.*;
import javax.swing.event.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;

/** Provides a PlugInWindow template for creating plug-ins with 
 * dialog windows.
 * Edit the sections seen below: variables, the run method, and if
 * necessary, the window code.  For the run method, the basic idea
 * is Text Trix gives you text from the current text area as well as
 * information about where the caret is or what portion of text
 * is highlighted.  You can modify that text however you like and return 
 * that text to Text Trix.
*/
public class PlugInWindowTemplate extends PlugInWindow {

	/* ----------------------------------------------------------------------*/
	/* --> EDIT THESE VARIABLES <-- 
	 * Then edit the run method.  If you want, you can also
	 * modify the window code, at the bottom of this file.
	 */
	private static String title = ""; // title of the plug-in
	private static String grouping = "tools"; // change to "trix" if a Trix instead
	private static String shortDesc = "Short Description"; // one-line summary
	private static boolean retrieveAllText = true; // if true, gets entire body of text
	private TemplateDialog diag = null; // the dialog window for user options


	/** Runs the plugin.
	@param s text to search
	@param x start position
	@param y end position, non-inclusive
	@return the modified text and positions to highlight
	*/
	public PlugInOutcome run(String s, int x, int y) {
		String newstr = s;
		int selectionStart = x;
		int selectionEnd = y;
		boolean noTextChange = false;
		return new PlugInOutcome(
			newstr,
			selectionStart,
			selectionEnd,
			noTextChange);
	}




	/* ------------------------------------------------------------------------*/
	/* NO NEED TO EDIT BELOW THIS LINE,
	 * unless you want to modify the window, at the very
	 * bottom of this file.
	 */

	/** Creates the template plug-in based on predefined variables editable in
	 * the top section of this source code file.
	 * Sets <code>setAlwaysEntireText</code> to <code>retrieveAllText</code>
	 * so that <code>TextTrix</code> sends the appropriate body of text.  If set to
	 * <code>true</code>, the plug-in always receives the entire body of text and
	 * chooses which sections on which to work according to user options.  This 
	 * constructor should also create the <code>Action</code>s
	 * and <code>Listener</code>s	to place in the dialog window so that the 
	 * plug-in can directly listen for user commands before running.
	 * @see #setAlwaysEntireText
	*/
	public PlugInWindowTemplate() {
		super(
			title,
			grouping,
			shortDesc,
			"desc.html",
			"icon.png",
			"icon-roll.png");
		setAlwaysEntireText(retrieveAllText); // retrieve the entire body of text

		// Example action to respond to user events in the dialog window:
		// Runs the plug-in when the user presses the button
		Action templateAction = new AbstractAction("Go!", null) {
			public void actionPerformed(ActionEvent e) {
				runPlugIn();
			}
		};

		// Creates the options dialog window
		diag =
			new TemplateDialog(templateAction);
		setWindow(diag);
	}

	/** Gets the normal icon.
	@return normal icon
	*/
	public ImageIcon getIcon() {
		ImageIcon pic = getIcon(getIconPath());
		return pic;
	}

	/** Gets the rollover icon.
	@return rollover icon
	*/
	public ImageIcon getRollIcon() {
		return getRollIcon(getRollIconPath());
	}

	/** Gets the detailed, HTML-formatted description.
	For display as a tool tip.
	@return a buffered reader for the description file
	*/
	public BufferedReader getDetailedDescription() {
		return super.getDetailedDescription(getDetailedDescriptionPath());
	}

	/** Runs the plugin with the text selection set to (0,0).
	 * @param s text to search
	 * @return the plugin output
	 * @see #run(String, int, int)
	*/
	public PlugInOutcome run(String s) {
		return run(s, 0, 0);
	}
}





/** Dialog window for accepting user input before running the plug-in.
*/
class TemplateDialog extends JPanel {
		
	/**Construct a find/replace dialog box
	 * @param owner frame to which the dialog box will be attached; 
	 * can be null
	 */
	public TemplateDialog(
		Action templateAction) {
		
		super(new GridBagLayout());
		setSize(100, 50);
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.anchor = GridBagConstraints.CENTER;
		
		JButton templateGoBtn = new JButton(templateAction);
		templateGoBtn.setText("Go!");
		LibTTx.addGridBagComponent(
			templateGoBtn,
			constraints,
			0,
			0,
			1,
			1,
			100,
			100,
			this);

	}

}
