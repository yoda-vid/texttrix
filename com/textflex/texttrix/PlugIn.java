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
 * Portions created by the Initial Developer are Copyright (C) 2003
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
import java.util.jar.*;
import javax.swing.event.*;
import java.awt.*;
import java.util.*;
import java.awt.event.*;

/** The abstract superclass for all plugins.
    Specifies the files and locations from which to load plugin resources
    as well as the code to both load them and manipulate text.
    All plugins must extend this abstract class and be packaged in 
    uncompressed JAR files.
*/
public abstract class PlugIn extends JComponent {
	private String description = null; // short description
	private String detailedDescription = null; // long, HTML-formatted desc.
	private String name = null; // plugin name
	private String iconPath = null; // path to main icon
	private ImageIcon icon = null; // the default icon
	private String rollIconPath = null; // path to rollover icon
	private ImageIcon rollIcon = null; // the roll-over icon
	private String detailedDescriptionPath = null; // path to formatted desc.
	private String category = null; // plugin category, eg "tools"
	private String path = null; // plugin JAR's path
	private JarFile jar = null; // plugin's JAR
	private EventListenerList listenerList = null;
	// list of listeners to notify
	// flag to retrieve the entire text, not just the selected region
	private boolean alwaysEntireText = false;
	private WindowAdapter winAdapter = null;
	private boolean tmpActivated = false;

	/** Constructs a plugin.
	@param aName plugin name
	@param aCategory plugin category, eg "tools"
	@param aDescription short description
	@param aDetailedDescriptionPath path to the HTML-formatted description
	@param aIconPath path to the main icon
	@param aRollIconPath path to the rollover icon
	*/
	public PlugIn(
		String aName,
		String aCategory,
		String aDescription,
		String aDetailedDescriptionPath,
		String aIconPath,
		String aRollIconPath) {

		name = aName;
		category = aCategory;
		description = aDescription;
		detailedDescriptionPath = aDetailedDescriptionPath;
		iconPath = aIconPath;
		rollIconPath = aRollIconPath;
		listenerList = new EventListenerList();
	}

	/** Registers a new plug-in listener to respond to notify
	when the plug-in runs.
	@param listener listener to register and notify
	@see #runPlugIn()
	*/
	public void addPlugInListener(PlugInListener listener) {
		listenerList.add(PlugInListener.class, listener);
	}

	/** Removes a registered listener.
	@param listener listener to register and notify
	@see #runPlugIn()
	*/
	public void removePlugInListener(PlugInListener listener) {
		listenerList.remove(PlugInListener.class, listener);
	}
	
	public void addWindowAdapter() {
	}
	
	public void activateWindow() {
	}
	/*
	public boolean isWindowVisible() {
		return false;
	}
	
	public void reloadWindow(){
	}
	public void removeWindowAdapter() {
	}
	*/
	
	public boolean isTmpActivated() {
		return tmpActivated;
	}
	
	public void setTmpActivated(boolean b) {
		tmpActivated = b;
	}
	
	public void setWindowAdapter(WindowAdapter adapter) {
		winAdapter = adapter;
	}

	/** Runs the plugin on a given section of the text.
	 * To use, must override or else the function will simply call <code>run(s)</code>.
	 * @param s text to manipulate
	 * @param selectionStart
	 * @param selectionEnd
	 * @return object containing the updated text; selection start position, or <code>-1</code>
	 * if the text should not be highlighted; and selection end position
	 */
	public PlugInOutcome run(String s, int selectionStart, int selectionEnd) {
		return run(s);
	}

	/** Runs the plugin on all the given text.
	 * 
	 * @param s text to manipulate
	 * @return object containing the updated text; selection start position, or <code>-1</code>
	 * if the text should not be highlighted; and selection end position
	 */
	public abstract PlugInOutcome run(String s);

	/** Runs the plugin over all the given text.
	 * To use, must override or else the function will simply call <code>run(s)</code>.
	 * @param s text to manipulate
	 * @param caretPosition caret position
	 * @return object containing the updated text; selection start position, or <code>-1</code>
	 * if the text should not be highlighted; and selection end position
	*/
	public PlugInOutcome run(String s, int caretPosition) {
		return run(s);
	}

	/** Invokes the primary action of the plug in.
	Creates an event to notify listeners that the plug has now
	begun to perform its main function.
	*/
	public void runPlugIn() {
		PlugInEvent event = new PlugInEvent(this);
		EventQueue queue = Toolkit.getDefaultToolkit().getSystemEventQueue();
		queue.postEvent(event);
	}

	/** Processes the event that the plug in invokes by notifying all 
	listeners so that they can act on the plug in.
	@param event plug in event
	@see #runPlugIn()
	*/
	public void processEvent(AWTEvent event) {
//		System.out.println("processing event");
		if (event instanceof PlugInEvent) {
			EventListener[] listeners =
				listenerList.getListeners(PlugInListener.class);
			for (int i = 0; i < listeners.length; i++)
				((PlugInListener) listeners[i]).runPlugIn((PlugInEvent) event);
		} else {
			super.processEvent(event);
		}
	}

	/** Starts the plug in.
	 * Override this method to customize what happens when the user
	 * presses the plug in button within the Text Trix frame.
	 * By default, the method simply fires <code>runPlugIn</code>
	 * to start the plug in's primary action, defined in <code>run</code>.
	 * Before making visible a window, such as a plug-in dialog box, 
	 * <code>setTmpActivated(true)</code> should usually be called to
	 * prevent <code>TextTrix</code> from redundantly activating all its windows.
	@see #runPlugIn()
	@see #run(String, int, int)
	*/
	public void startPlugIn() {
		runPlugIn();
	}
	

	/** Sets the plugin's path.
	@param aPath path
	 */
	public void setPath(String aPath) {
		path = aPath;
	}
	/** Sets the plugin's selection status.
	 * @param aAlwaysEntireText if <code>true</code>, the plug-in requests
	 * the entire body of text from the <code>TextPad</code>, even if a section
	 * is highlighted. 
	 */
	public void setAlwaysEntireText(boolean aAlwaysEntireText) {
		alwaysEntireText = aAlwaysEntireText;
	}

	/** Gets the plugin name.
	@return name
	*/
	public String getName() {
		return name;
	}
	/** Gets the plugin path to the main icon.
	@return path to the main icon
	*/
	public String getIconPath() {
		return iconPath;
	}
	/** Gets the plugin path to the rollover icon.
	 * The roll-over icon displays when the mouse rolls over the plug-in's button.
	 * This icon can also serve simply as an alternate icon, independent of any
	 * roll-over mechanism.
	@return path to the rollover icon
	*/
	public String getRollIconPath() {
		return rollIconPath;
	}
	/** Gets the plugin short description.
	@return short description
	*/
	public String getDescription() {
		return description;
	}
	/** Gets the plugin category.
	@return category
	*/
	public String getCategory() {
		return category;
	}
	/** Gets the plugin selection status.
	<code>true</code> indicates that all of the next, not merely
	the selected text, should be retrieved.
	@return category
	*/
	public boolean getAlwaysEntireText() {
		return alwaysEntireText;
	}

	/** Gets an icon.
	@param iconPath icon's path
	*/
	public ImageIcon getImageIcon(String iconPath) {
		/* NON-WORKAROUND:
		URL url = cl.getResource(path);
		return new ImageIcon(url);
		*/
		/* A bug in JRE v.1.4 prevents loading of PNG/GIF images
		   from compressed JAR files.  The workaround is to create
		   uncompressed JAR plug-in files by using the "0" (zero)
		   command-line option while creating the JAR
		   (see http://developer.java.sun.com/developer/bugParade/bugs/ \
		   4764639.html).
		*/

		/* Workaround for an apparent bug preventing the getResourceAsStream's
		   URL via getResource from accessing an external JAR, despite
		   the successful loading from the class loader.  Evidently
		   some systems try to interpret "C:" in "file:/C:/..."
		   as a network host.  Somehow loading a normal path representation
		   into a JarFile properly loads the JAR.
		*/
		byte[] bytes = null;
		try {
			// assume that the resource is located within the given file structure
			iconPath = "com/textflex/texttrix/" + iconPath;
			// access a JAR file
			if (jar == null)
				jar = new JarFile(new File(path));
			JarEntry entry = jar.getJarEntry(iconPath);
			if (entry == null)
				return null;
			// read the file
			InputStream in = jar.getInputStream(entry);
			bytes = new byte[in.available()];
			in.read(bytes);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new ImageIcon(bytes);
	}

	/** Gets the default icon and stores it in <code>icon</code>
	 * 
	 * @param path path to the icon, relative to the plug-in's class file
	 * @return the icon
	 */
	public ImageIcon getIcon(String path) {
		if (icon == null)
			icon = getImageIcon(path);
		return icon;
	}
	/** Gets the roll-over icon and stores it in <code>icon</code>
	 * 
	 * @param path path to the icon, relative to the plug-in's class file
	 * @return the icon
	 */
	public ImageIcon getRollIcon(String path) {
		if (rollIcon == null)
			rollIcon = getImageIcon(path);
		return rollIcon;
	}

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

	/** Gets the detailed description file from the given path.
	 * Each plug-in needs to call this function from <code>getDetailedDescription()</code>
	 * so that the plug-in can supply its own <code>descPath</code>, which this
	 * superclass cannot access by itself.
	 * @param descPath the path to the plug-in's detailed description documentation
	 * @return reader stream to the documentation file
	 * @see #getDetailedDescription()
	 */
	public BufferedReader getDetailedDescription(String descPath) {
		/*
		  // cl is the plugin's class loader
		  InputStream in = cl.getResourceAsStream(path);
		  return new BufferedReader(new InputStreamReader(in));
		*/
		/* Workaround for an apparent bug preventing the getResourceAsStream's
		   URL via getResource from accessing an external JAR, despite
		   the successful loading from the class loader.  Evidently
		   some systems try to interpret "C:" in "file:/C:/..."
		   as a network host.  Somehow loading a normal path representation
		   into a JarFile properly loads the JAR.
		*/
		BufferedReader reader = null;
		InputStreamReader in = null;
		try {
			// assume that the resource is located within the given file structure
			descPath = "com/textflex/texttrix/" + descPath;
			// access a JAR file
			if (jar == null)
				jar = new JarFile(new File(path));
			JarEntry entry = jar.getJarEntry(descPath);
			if (entry == null)
				return null;
			// read the file
			in = new InputStreamReader(jar.getInputStream(entry));
			reader = new BufferedReader(in);
		} catch (IOException e) {
			e.printStackTrace();
		}
		/* Closing the stream within the finally block somehow still closes the stream
		 * when it still needs to be accessed.
		 */
		/* finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch(IOException e) {
			}
		}
		*/
		return reader;
	}

	/** Gets a detailed description to display as a tool tip from 
	the plug-in's icon.
	Abstract to ensure that the subclass passes its path into 
	<code>getDetailedDescription(String)</code>.
	@return reader for the path
	@see #getDetailedDescription(String)
	*/
	public abstract BufferedReader getDetailedDescription();

	/** Gets the file path to the detailed description.
	@return path
	@see #getDetailedDescription()
	*/
	public String getDetailedDescriptionPath() {
		return detailedDescriptionPath;
	}
	
	/** Gets the path to the plug-in.
	 * 
	 * @return the path
	 */
	public String getPath() {
		return path;
	}
	
	public WindowAdapter getWindowAdapter() {
//		System.out.println("making winAdapter");
		return winAdapter;
	}

}
