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
 * Portions created by the Initial Developer are Copyright (C) 2004
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s): David Young <dvd@textflex.com>
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
import java.io.*;
import java.awt.event.*;
import java.awt.*;

/**<code>PlugInWindow</code> simplifies the task of making a window for
 * a <code>PlugIn</code>.  To integrate these windows with the main application,
 * windows listener code in <code>PlugInWindow</code> allows the 
 * the main application to respond to window events.  By overriding the 
 * <code>startPlugIn()</code> method, the plug-in starts by opening the 
 * dialog rather than calling <code>run(String)</code>.
 * 
 * <p>The <code>TextTrix</code> class takes care to encapsulate itself rom 
 * this <code>PlugInWindow</code> class, lest a plug-in could gain access over
 * the main application and damage it.  This plug-in class uses 
 * <code>JFrame</code>s as its windowing component, for example.
 * While the main class could create a <code>JDialog</code> with the main class
 * as the owner to simplify integration with the main class, the plug-in could
 * call <code>getOwner()</code> to gain access to the entire main class.  To
 * still integrate the main class with the plug-in, the main class
 * passes listeners that listen to plug-in windowing and action events and relays
 * the information to the main class.  This class can respond by altering
 * <code>TextPad</code>s, recording plug-in window positions and sizes, or
 * making other changes.
 * 
 * @author David Young
 *
 * 
 */
public abstract class PlugInWindow extends PlugIn {

	private WindowAdapter winAdapter = null; // window listener
	//private JFrame window = null; // window
	private ComponentListener winCompListener = null; // window's component listener
	private JPanel window = null;
	
	/**Constructs a plug-in adapted for window control.
	 * After creating the window, <code>setWindow(JFrame)</code> must be called
	 * on the window object to ensure that it will receive the controls, such 
	 * as the <code>WindowAdapter</code>.
	 * @param aName name of the plug-in
	 * @param aCategory type of plug-in, either <code>tool</code> for serious
	 * functions, or <code>tric</code> for goofy ones
	 * @param aDescription brief description
	 * @param aDetailedDescriptionPath path to an html file for the plug-in's
	 * tool tip
	 * @param aIconPath path to the main icon
	 * @param aRollIconPath path to the icon that the main icon turns into
	 */
	public PlugInWindow(
		String aName,
		String aCategory,
		String aDescription,
		String aDetailedDescriptionPath,
		String aIconPath,
		String aRollIconPath) {
		super(aName, aCategory, aDescription, aDetailedDescriptionPath, aIconPath, aRollIconPath);
		
	}
	
	/** Starts the plug-in by calling the options window, which contains settings
	 * as well as a button to run the plug-in.
	 */
	public void startPlugIn() {
		/*
		setTmpActivated(true);
		*
		if (window != null) {
			window.setVisible(true);
			System.out.println("right here");
		}/* else if (panel != null){
			System.out.println("showing dialog");
			//panel.setVisible(true);
		}
		*/
	}

	/**Attaches a window listener to the window.
	 * The listener must have been previously set with 
	 * <code>setWindowAdapter(WindowAdapter)</code>.
	 *
	public void addWindowAdapter() {
		//window.addWindowListener(winAdapter);//getWindowAdapter());
	}
	
	/**Attaches a component listener to the window for external responses
	 * to windowing events, such as resizing and repositioning.
	 * For example, the main program could now store plug-in window size
	 * and position information in the program's preferences file.
	 * 
	 *
	public void addWindowComponentListener() {
		window.addComponentListener(winCompListener);
		//panel.addComponentListener(winCompListener);
	}
	
	/**Makes the window visible.
	 * 
	 *
	public void activateWindow() {
		//window.toFront();
	}
	
	/**Checks whether the window is currently visible.
	 * WARNING: Delays in windowing events might cause this function to
	 * return false when the window is visible.
	 *
	public boolean isWindowVisible() {
		return window.isVisible();
	}
	
	/**Makes the window invisible.
	 * 
	 *
	public void closeWindow(){
		window.setVisible(false);
	}
	
	/**Removes the window listener from the window.
	 * The listener must have been previously set with 
	 * <code>setWindowAdapter(WindowAdapter)</code>.
	 *
	public void removeWindowAdapter() {
		//window.removeWindowListener(winAdapter);//getWindowAdapter());
	}

	/**Sets the window adapter.
	 * This function must be called before adding or removing window adapters.
	 *
	public void setWindowAdapter(WindowAdapter adapter) {
		winAdapter = adapter;
	}
	
	/**Sets the window's component listener.
	 * 
	 * @see #addWindowComponentListener()
	 *
	public void setWindowComponentListener(ComponentListener aWinCompListener) {
		winCompListener = aWinCompListener;
	}
	*/


	/**Runs the plugin on all the given text.
	 * Perpetuates the abstract class from the superclass.
	 * @param s text to manipulate
	 * @return object containing the updated text; selection start position, or <code>-1</code>
	 * if the text should not be highlighted; and selection end position
	 */
	public abstract PlugInOutcome run(String s);
	
	/**Sets the window.
	 * This function must be called before adding or removing window adapters,
	 * preferably within the constructor.
	 * @param aWindow
	 */
	public void setWindow(JPanel aWindow) {//JFrame aWindow) {
		window = aWindow;
	}
	
	public void setWindowLocation(Point p) {
		//window.setLocation(p);
		window.setLocation(p);
	}
	
	/**Sets the size of the window.
	 * Does not change the size if either the given width or height parameters
	 * are 0, which would otherwise produce an unusable window.  The values
	 * are usually 0 only because the size has never been stored in the 
	 * preferences.
	 */
	public void setWindowSize(int width, int height) {
		if (width != 0 && height != 0) {
			window.setSize(width, height);
			//panel.setSize(width, height);
		} 
	}
	
	/*
	public void setPanel(JPanel p) {
		panel = p;
	}
	*/
	
	
	
	
	
	
	
	/**Gets the window.
	 * 
	 * @return the window
	 */
	public JPanel getWindow() {//JFrame getWindow() {
		return window;
	}
	
	/**Gets the window listener.
	 * @return the window listener
	 * @see TextTrix#focusAllWindows(PlugIn)
	 *
	public WindowAdapter getWindowAdapter() {
		return winAdapter;
	}
	*/
	
	public Point getWindowLocation() {
		return window.getLocation();
		//return panel.getLocation();
	}
	
	/**Gets the width of the plug-in's window.
	 * 
	 * @return the width in pixels
	 */
	public int getWindowWidth() {
		return window.getWidth();
		//return panel.getWidth();
	}
	
	/**Getst the height of the plug-in's window.
	 * 
	 * @return the height in pixels
	 */
	public int getWindowHeight() {
		return window.getHeight();
		//return panel.getHeight();
	}
	
	/*
	public JPanel getPanel() {
		return panel;
	}
	*/
	
	/** Mandates that the subclass calls another function to retrieve the default icon,
	 * ensuring that the path is relative to the plug-in rather than to the class 
	 * <code>PlugIn</code>.
	 * @return the icon
	 */
	public abstract ImageIcon getIcon();
	
	/** Mandates that the subclass calls another function to retrieve the roll-over icon,
	 * ensuring that the path is relative to the plug-in rather than to the class 
	 * <code>PlugIn</code>.
	 * @return the icon
	 */
	public abstract ImageIcon getRollIcon();
	
	/** Gets a detailed description to display as a tool tip from 
	the plug-in's icon.
	Abstract to ensure that the subclass passes its path into 
	<code>getDetailedDescription(String)</code>.
	@return reader for the path
	@see #getDetailedDescription(String)
	*/
	public abstract BufferedReader getDetailedDescription();
}
