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

import java.lang.reflect.*;
import java.io.*;
import java.net.*;
import javax.swing.*;
import java.awt.*;
import java.util.*;

/** Library class to act as a tool chest for Text Trix functions.
 * Most <code>LibTTx</code> functions are potentially relevant to multiple
 * source files.  For greater accessibility, the functions are generic, often
 * requiring Swing components or other objects to manipulate; developers
 * should be aware that objects they pass may return altered.
 * 
 * <p>Since <code>LibTTx</code> consists almost entirely of 
 * <code>static</code> methods, there is almost no need to create an
 * object of it.
 * 
 * @author davit
 */
public class LibTTx {
	
	/** Constructs the library object, almost never necessary since
	 * virtually all of its methods are static.
	 *
	 */
	public LibTTx() {
	}

	/** Loads a single plug-in from a given path.
	 * 
	 * @param path path to the plug-in, including its filename with the 
	 * <code>.jar</code> extension
	 * @return the plug-in
	 */
	public static PlugIn loadPlugIn(String path) {
		// loader corresponding to the given plugin's location
		ClassLoader loader = null;
		int nameStart = path.lastIndexOf(File.separator) + 1;
		String plugInName = path.substring(nameStart);
		/* Beginning with JRE v.1.4.2, the URL must be created
		   by first making a File object from the path and then
		   converting it to a URL.  Manually parsing "file://"
		   to the head of the path and converting the resulting
		   String into a URL no longer works.
		*/
		try {
			URL url = new File(path).toURL();
			//		    System.out.println(url.toString());
			loader = new URLClassLoader(new URL[] { url });
			//		    System.out.println(url.toString());
		} catch (MalformedURLException e) {
		}
		// all plugins are in the package, com.textflex.texttrix
		String name =
			"com.textflex.texttrix."
				+ plugInName.substring(
					0,
					plugInName.indexOf(".jar"));
		PlugIn plugIn = (PlugIn) createObject(name, loader);
		plugIn.setPath(path);
		return plugIn;
	}
	
	/** Loads the specified plug-in from a given directory.
	 * 
	 * @param plugInDir the directory that houses the plug-in JAR files
	 * @param plugInName case-sensitive name of the plug-in, 
	 * including the <code>.jar</code> extension
	 * @return the plug-in
	 */
	public static PlugIn loadPlugIn(File plugInDir, String plugInName) {
		// loader corresponding to the given plugin's location
		ClassLoader loader = null;
		String path = null;
		/* Beginning with JRE v.1.4.2, the URL must be created
		   by first making a File object from the path and then
		   converting it to a URL.  Manually parsing "file://"
		   to the head of the path and converting the resulting
		   String into a URL no longer works.
		*/
		try {
			path =
				plugInDir.toString() + File.separator + plugInName;
			URL url = new File(path).toURL();
			//		    System.out.println(url.toString());
			loader = new URLClassLoader(new URL[] { url });
			//		    System.out.println(url.toString());
		} catch (MalformedURLException e) {
		}
		// all plugins are in the package, com.textflex.texttrix
		String name =
			"com.textflex.texttrix."
				+ plugInName.substring(
					0,
					plugInName.indexOf(".jar"));
		PlugIn plugIn = (PlugIn) createObject(name, loader);
		plugIn.setPath(path);
		return plugIn;
	}
	
	/** Gets a list of all the plug-ins in a given directory.
	 * 
	 * @param plugInDir the plug-in directory
	 * @see #loadPlugIn(File, String)
	 * @return array of plug-in names
	 */
	public static String[] getPlugInList(File plugInDir) {
		// get a list of files ending with ".jar"
		String endsWith = ".jar"; // only list files ending w/ team.txt
		EndsWithFileFilter filter = new EndsWithFileFilter();
		filter.add(endsWith);
		String[] plugInList = plugInDir.list(filter);
		return plugInList;
	}
	
	/** Gets a list of the paths to each plug-in JAR file.
	 * 
	 * @param plugInDir plug-in directory
	 * @return array of plug-in paths
	 */
	public static String[] getPlugInPaths(File plugInDir) {
		String[] list = getPlugInList(plugInDir);
		for (int i = 0; i < list.length; i++) {
			list[i] = plugInDir.toString() + File.separator + list[i];
		}
		return list;
	}
		
		

	/** Loads all the plugins from a given directory.
	Assumes that all files ending in "<code>.jar</code>" are plugins.
	Creates an array of plugin objects from the files.
	@param plugInDir directory containing the plugin JAR files
	@return array of plugins
	*/
	public static PlugIn[] loadPlugIns(File plugInDir) {
		// get a list of files ending with ".jar"
		String[] plugInList = getPlugInList(plugInDir);

		// create objects from each plugin in the list
		PlugIn[] plugIns = null; // array of PlugIn's to return
		// traverse /teams dir, listing names w/o the ending
		if (plugInList != null) {
			plugIns = new PlugIn[plugInList.length];
			for (int i = 0; i < plugInList.length; i++) {
				plugIns[i] = loadPlugIn(plugInDir, plugInList[i]);
			}
		}
		return plugIns;
	}

	/** Create the object of the given name in the class loader's location.
	@param name name of object to load
	@param loader class loader referencing a location containing
	the named class
	*/
	public static Object createObject(String name, ClassLoader loader) {
		Object obj = null; // the object to return
		// load the class
		try {
			//	    System.out.println("name: " + name);
			Class cl = loader.loadClass(name);
			obj = cl.newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return obj;
	}
	
	/** Creates an array from a comma-delimited string.
	 * Each portion of the string is placed in a separate element in the array.
	 * @param s string to convert
	 * @return array of each portion of the string
	 */
	public static String[] createArrayFromString(String s) {
		String[] array = new String[10];
		int arrayInd = 0;
		StringTokenizer tokenizer = new StringTokenizer(s, ",");
		while (tokenizer.hasMoreTokens()) {
			if (arrayInd >= array.length) {
				array = (String[])growArray(array);
			}
			array[arrayInd++] = tokenizer.nextToken();
		}
		return (String[])truncateArray(array, arrayInd);
	}
	
	public static boolean inUnsortedList(String find, String[] list) {
		String check = null;
		for (int i = 0; i < list.length && (check = list[i]) != null; i++) {
			if (check.equals(find)) return true;
		}
		return false;
	}

	/** Increases arrays to accomodate 10% + 10 more elements.
	Allows an unlimited number of elements in a given array.
	@param array to increase in size
	@return larger array
	*/
	public static Object growArray(Object array) {
		Class arrayClass = array.getClass();
		if (!arrayClass.isArray())
			return null; // exit immediately
		Class componentType = arrayClass.getComponentType();
		int len = Array.getLength(array);
		// increase array length by 10% + 10 elements
		Object newArray = Array.newInstance(componentType, len * 11 / 10 + 10);
		System.arraycopy(array, 0, newArray, 0, len);
		return newArray;
	}

	/** Shortens arrays to the minimum number of elements.
	Allows checking for number of filled elements by simply 
	calling <code><i>array</i>.length.
	@param array array to truncate
	@param length number of elements to truncate to
	@return array as a single object, not an array of objects
	*/
	public static Object truncateArray(Object array, int length) {
		Class arrayClass = array.getClass();
		if (!arrayClass.isArray())
			return null; // exit immediately
		Class componentType = arrayClass.getComponentType();
		Object newArray = Array.newInstance(componentType, length);
		System.arraycopy(array, 0, newArray, 0, length);
		return newArray;
	}
	
	/** Searches backward in a text to find a given normal-oriented string.
	 * For example, in the text, text = "Mr. Smith went to the door went out,"
	 * reverseIndexOf(text, "went", 14) would return 10. 
	 * @param str text to search
	 * @param searchStr string to find
	 * @param offset index of first character not included in the search
	 * @return index of found string; -1 if not found
	 */
	public static int reverseIndexOf(String str, String searchStr, int offset) {
		int i = offset - 1;
		//	System.out.println("len: " + searchStr.length());
		while (i >= 0
			&& !str.substring(i, i + searchStr.length()).equals(searchStr)) {
			i--;
		}
		return i;
	}

	/**Read in text from a file and return the text as a string.
	 * Differs from <code>displayFile(String path)</code> because
	 * allows editing.
	 * @param path text file stream
	 * @return text from file
	 */
	public static String readText(String path) {
		String text = "";
		InputStream in = null;
		BufferedReader reader = null;
		// read in lines until none remain
		try {
			in = TextTrix.class.getResourceAsStream(path);
			reader = new BufferedReader(new InputStreamReader(in));
			String line;
			while ((line = reader.readLine()) != null)
				text = text + line + "\n";
		} catch (IOException exception) {
			exception.printStackTrace();
			return "";
		} finally { // clean-up code
			try {
				if (reader != null)
					reader.close();
				if (in != null)
					in.close();
			} catch (IOException exception) {
				//	    exception.printStackTrace();
				return "";
			}
		}
		return text;
	}

	/**Read in text from a file and return the text as a string.
	 * Differs from <code>displayFile(String path)</code> because
	 * allows editing.
	 * @param reader text file stream
	 * @return text from file
	 */
	public static String readText(BufferedReader reader) {
		String text = "";
		String line;
		// read lines until none remain;
		// the calling function should handle clean-up code for the reader stream
		try {
			while ((line = reader.readLine()) != null)
				text = text + line + "\n";
		} catch (IOException exception) {
			exception.printStackTrace();
		}
		return text;
	}

	/**Enable button rollover icon change.
	 * @param button <code>JButton</code> to display icon rollover change
	 * @param iconPath location of icon to change to
	 */
	public static void setRollover(JButton button, String iconPath) {
		button.setRolloverIcon(makeIcon(iconPath));
		button.setRolloverEnabled(true);
	}

	/**Enable button rollover icon change.
	 * @param button <code>JButton</code> to display icon rollover change
	 * @param icon location of icon to change to
	 */
	public static void setRollover(JButton button, ImageIcon icon) {
		button.setRolloverIcon(icon);
		button.setRolloverEnabled(true);
	}

	/**Set an action's properties.
	 * @param action action to set
	 * @param description tool tip
	 * @param mnemonic menu shortcut
	 * @param keyStroke accelerator key shortcut
	 */
	public static void setAcceleratedAction(
		Action action,
		String description,
		char mnemonic,
		KeyStroke keyStroke) {
		action.putValue(Action.SHORT_DESCRIPTION, description);
		action.putValue(Action.MNEMONIC_KEY, new Integer(mnemonic));
		action.putValue(Action.ACCELERATOR_KEY, keyStroke);
	}

	/**Sets an action's properties.
	 * @param action action to set
	 * @param description tool tip
	 * @param mnemonic menu shortcut
	 */
	public static void setAction(
		Action action,
		String description,
		char mnemonic) {
		action.putValue(Action.SHORT_DESCRIPTION, description);
		action.putValue(Action.MNEMONIC_KEY, new Integer(mnemonic));
	}

	/** Sets an action's properties.
	Assumes that the action has a name, and that the name is used
	in the menu that will house the action.
	@param action action to set
	@param name the action's name, from which to get the mnemonic
	@param description tool tip
	@param charsUnavailable a string of characters unavailable to use
	as mnemonics, as in those already in the menu where the action
	will be placed
	 */
	public static String setAction(
		Action action,
		String name,
		String description,
		String charsUnavailable) {
		char mnemonic = 0;
		int i = 0;
		// tries to get a mnemonic that has not been taken
		for (i = 0;
			i < name.length()
				&& charsUnavailable.indexOf((mnemonic = name.charAt(i))) != -1;
			i++);
		// otherwise haven't found a suitable char
		if (i < name.length()) {
			action.putValue(Action.MNEMONIC_KEY, new Integer(mnemonic));
			charsUnavailable += mnemonic;
		}
		// adds the description
		action.putValue(Action.SHORT_DESCRIPTION, description);
		return charsUnavailable;
	}

	/** Creates an image icon.
	@param path image file location relative to TextTrix.class
	@return icon from archive; null if the file cannot be retrieved
	 */
	public static ImageIcon makeIcon(String path) {
		URL iconURL = LibTTx.class.getResource(path);
		return (iconURL != null) ? new ImageIcon(iconURL) : null;
	}

	/**Adds a new component to the <code>GridBagLayout</code> manager.
	   @param c component to add
	   @param constraints layout constraints object
	   @param x column number
	   @param y row number
	   @param w number of columns to span
	   @param h number of rows to span
	   @param wx column weight
	   @param wy row weight
	*/
	public static void addGridBagComponent(
		Component c,
		GridBagConstraints constraints,
		int x,
		int y,
		int w,
		int h,
		int wx,
		int wy,
		Container pane) {
		constraints.gridx = x;
		constraints.gridy = y;
		constraints.gridwidth = w;
		constraints.gridheight = h;
		constraints.weightx = wx;
		constraints.weighty = wy;
		pane.add(c, constraints);
	}
	
}

/** Filters filenames to select only files with particular endings.
 */
class EndsWithFileFilter implements FilenameFilter {
	private String endsWith = ""; // required ending

	/** Sets the required ending.
	@param String string that filename must end with to pass the filter
	*/
	public void add(String aEndsWith) {
		endsWith = aEndsWith;
	}

	/** Aceepts the file if its name ends withe specified string, 
	<code>endsWith</code>.
	@param file file to check
	@param name name to check
	*/
	public boolean accept(File file, String name) {
		return (name.endsWith(endsWith)) ? true : false;
	}
}
