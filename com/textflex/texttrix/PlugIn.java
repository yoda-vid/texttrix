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
//import java.net.*;
import java.io.*;
import java.util.jar.*;
import javax.swing.event.*;
import java.awt.*;
import java.util.*;

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
    private String rollIconPath = null; // path to rollover icon
    private String detailedDescriptionPath = null; // path to formatted desc.
    private String category = null; // plugin category, eg "tools"
    private String path = null; // plugin JAR's path
    private JarFile jar = null; // plugin's JAR
    //    private Action action = null;
    //    private JButton actionButton = null;
    private EventListenerList listenerList = null;
    private boolean alwaysEntireText = false;

    /** Constructs a plugin.
	@param aName plugin name
	@param aCategory plugin category, eg "tools"
	@param aDescription short description
	@param aDetailedDescriptionPath path to the HTML-formatted description
	@param aIconPath path to the main icon
	@param aRollIconPath path to the rollover icon
    */
    public PlugIn(String aName, String aCategory, 
		  String aDescription,
		  String aDetailedDescriptionPath, String aIconPath,
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

    /** Runs the plugin on a given section of the text.
	@param s text to manipulate
	@param x index at which to start
	@param y first index at which to no longer work
    */
    public abstract PlugInOutcome run(String s, int selectionStart, 
				      int selectionEnd) ;

    /** Runs the plugin over all the given text.
	@param s text to manipulate
	@param x caret position
    */
    public abstract PlugInOutcome run(String s, int caretPosition);



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
	if (event instanceof PlugInEvent) {
	    EventListener[] listeners 
		= listenerList.getListeners(PlugInListener.class);
	    for (int i = 0; i < listeners.length; i++)
		((PlugInListener)listeners[i]).runPlugIn((PlugInEvent)event);
	} else {
	    super.processEvent(event);
	}
    }

    /** Starts the plug in.
	Override this method to customize what happens when the user
	presses the plug in button within the Text Trix frame.
	By default, the method simply fires <code>runPlugIn</code>
	to start the plug in's primary action, defined in <code>run</code>.
	@see #runPlugIn()
	@see #run(String, int, int)
    */
    public void startPlugIn() {
	runPlugIn();
    }












    /** Sets the plugin's path.
	@param aPath path
     */
    public void setPath(String aPath) { path = aPath; }
    /** Sets the plugin's selection status.
	@param aIgnoreSelection
     */
    public void setAlwaysEntireText(boolean aAlwaysEntireText) { 
	alwaysEntireText = aAlwaysEntireText; 
    }

    /*
    public void setAction(Action aAction) { 
	action = aAction; 
	actionButton = new JButton(action) {
		protected void fireActionPerformed(ActionEvent evt) {
		    try {
			super.fireActionPerformed(evt);
		    } catch (Exception e) {
		    }
		}
	    }
    }
    */














    /** Gets the plugin name.
	@return name
    */
    public String getName() { return name; }
    /** Gets the plugin path to the main icon.
	@return path to the main icon
    */
    public String getIconPath() { return iconPath; }
    /** Gets the plugin path to the rollover icon.
	@return path to the rollover icon
    */
    public String getRollIconPath() { return rollIconPath; }
    /** Gets the plugin short description.
	@return short description
    */
    public String getDescription() { return description; }
    /** Gets the plugin category.
	@return category
    */
    public String getCategory() { return category; }
    /** Gets the plugin selection status.
	<code>true</code> indicates that all of the next, not merely
	the selected text, should be retrieved.
	@return category
    */
    public boolean getAlwaysEntireText() { return alwaysEntireText; }

    /** Gets an icon.
	@param iconPath icon's path
    */
    public ImageIcon getIcon(String iconPath) {
	/*
	URL url = cl.getResource(path);
	//	System.out.println(path);
	//	System.out.println(url.toString());
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
	    iconPath = "com/textflex/texttrix/" + iconPath;
	    //	    System.out.println((new File(path)).exists());
	    if (jar == null) 
		jar = new JarFile(new File(path));
	    JarEntry entry = jar.getJarEntry(iconPath);
	    //	    System.out.println(entry.getName());
	    if (entry == null) return null;
	    InputStream in = jar.getInputStream(entry);
	    bytes = new byte[in.available()];
	    in.read(bytes);
	} catch (IOException e) {
	    e.printStackTrace();
	}
	return new ImageIcon(bytes);
    }

    public abstract ImageIcon getIcon();

    public abstract ImageIcon getRollIcon();

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
	try {
	    descPath = "com/textflex/texttrix/" + descPath;
	    //	    System.out.println((new File(path)).exists());
	    if (jar == null) 
		jar = new JarFile(new File(path));
	    JarEntry entry = jar.getJarEntry(descPath);
	    if (entry == null) return null;
	    //	    System.out.println(entry.getName());
	    InputStreamReader in 
		= new InputStreamReader(jar.getInputStream(entry));
	    reader = new BufferedReader(in);
	} catch (IOException e) {
	    e.printStackTrace();
	}
	return reader;
    }

    /** Gets a detailed description to display as a tool tip from 
	the plug-in's icon.
	@return reader for the path
    */
    public abstract BufferedReader getDetailedDescription();

    /** Gets the file path to the detailed description.
	@return path
	@see #getDetailedDescription()
    */
    public String getDetailedDescriptionPath() { 
	return detailedDescriptionPath; 
    }


}
