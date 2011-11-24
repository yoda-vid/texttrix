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
 * Portions created by the Initial Developer are Copyright (C) 2003-7
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

import java.lang.reflect.*;
import java.io.*;
import java.net.*;
import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.jar.*;
import javax.swing.text.*;
import javax.imageio.*;

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
	
	/** System-indenpendent newline.
	 */
	public static final String NEWLINE = System.getProperty("line.separator");

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
		PlugIn plugIn = null;
		int nameStart = path.lastIndexOf(File.separator) + 1;
		String plugInName = path.substring(nameStart);
		
		/* Beginning with JRE v.1.4.2, the URL must be created
		   by first making a File object from the path and then
		   converting it to a URL.  Manually parsing "file://"
		   to the head of the path and converting the resulting
		   String into a URL no longer works.
		*/
		try {
//			URL url = new File(path).toURL();
			URL url = new File(path).toURI().toURL();
			loader = new URLClassLoader(new URL[] { url });
		} catch (MalformedURLException e) {
		}
		return loadPlugIn(path, plugInName, loader);
		
	}
	
	public static URLClassLoader loadJar(String path) {
		URLClassLoader loader = null;
					System.out.println("loading " + path);
		try {
			URL url = new URL("file", null, path);
			loader = new URLClassLoader(new URL[] { url });
			JarInputStream jis = new JarInputStream(new FileInputStream(path));
			JarEntry entry = jis.getNextJarEntry();
			while (entry != null) {
				String name = entry.getName();
					System.out.println("loading " + name);
				if (name.endsWith(".class")) {
					name = name.substring(0, name.length() - 6);
					name = name.replace('/', '.');
					loader.loadClass(name);
					System.out.println(name + " loaded");
				}
				entry = jis.getNextJarEntry();
			}
		} catch (MalformedURLException e) {
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return loader;
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
		PlugIn plugIn = null;
		
		/* Beginning with JRE v.1.4.2, the URL must be created
		   by first making a File object from the path and then
		   converting it to a URL.  Manually parsing "file://"
		   to the head of the path and converting the resulting
		   String into a URL no longer works.
		*/
		try {
			path = plugInDir.toString() + File.separator + plugInName;
//			URL url = new File(path).toURL();
			URL url = new File(path).toURI().toURL();
			loader = new URLClassLoader(new URL[] { url });
		} catch (MalformedURLException e) {
		}
		return loadPlugIn(path, plugInName, loader);
		
	}
	
	private static PlugIn loadPlugIn(String path, String plugInName, ClassLoader loader) {
		PlugIn plugIn = null;
		// all plugins are in the package, com.textflex.texttrix
		String name =
			"com.textflex.texttrix."
				+ plugInName.substring(0, plugInName.indexOf(".jar"));
		try {
			plugIn = (PlugIn) createObject(name, loader);
		} catch (Exception e) {
			System.out.println("The plug-in, " + name + ", could not be"
				+ NEWLINE + "loaded.  If you'd like to use it, please visit"
				+ NEWLINE + "http://textflex.com/texttrix/plugins.html to"
				+ NEWLINE + "contact the plugin maker.");
		}
		if (plugIn == null) return null;
		plugIn.setPath(path);
		return plugIn;
	}
	
	/*
	 * TODO: Create plugin loader based on URLs and perhaps a file listing page.
	public static PlugIn loadPlugIn(URL url, String plugInName) {
		// loader corresponding to the given plugin's location
		ClassLoader loader = null;
		String path = null;
		PlugIn plugIn = null;
		
		try {
			path = plugInDir.toString() + File.separator + plugInName;
//			URL url = new File(path).toURL();
			URL url = new File(path).toURI().toURL();
			loader = new URLClassLoader(new URL[] { url });
		} catch (MalformedURLException e) {
		}
		return loadPlugIn(path, plugInName, loader);
		
	}
	*/

	/** Gets the base directory, the root directory of the program.
	 *
	 * <p>The code has a relatively elaborate mechanism to locate the plugins
	 * folder and its JAR files. Why not use the URL that the Text Trix
	 * class supplies? Text Trix needs to locate each JAR plugin's absolute
	 * path and name. Text Trix's URL must be truncated to its root
	 * directory's location and built back up through the plugins directory.
	 * Using getParentFile() to the program's root and appending the rest of
	 * the path to the plugins allows one to use URLClassLoader directly
	 * with the resulting URL.
	 * 
	 * <p>Unfortunately, some systems do not locate local files with this
	 * method. The following elaborate system works around this apparent JRE
	 * bug by further breaking the URL into a normal path and loading a file
	 * from it.
	 * 
	 * <p>Unfortunately again, a new feature from JRE v.1.4 causes spaces in
	 * URL strings to be converted to "%20" turning URL's into strings. The
	 * JRE cannot load files with "%20" in them, however; for example,
	 * "c:\Program Files\texttrix-x.y.z\plugins" never gets loaded. The
	 * workaround is to replace all "%20"'s in the string with " ". Along
	 * with v.1.4 comes new String regex tools to make the operation simple,
	 * but prior versions crash after a NoSuchMethodError. The replacement
	 * must be done manually.
	 *
	 * @return <code>plugins</code> folder
	 */
	public static File getBaseFile() {

		// this class's location
		String relClassLoc = "com/textflex/texttrix/TextTrix.class";
		URL urlClassDir = ClassLoader.getSystemResource(relClassLoc);
		// WORKAROUND: jnlp-launched files return null for the urlClassDir, 
		// so returns a stub jar root file for now
		// TODO: identify the jar base and plugin directories
		// for jnlp-launched files and more generally
		if (urlClassDir == null) return new File("/");
		String strClassDir = urlClassDir.getPath();
		// to check whether JAR
		File fileClassDir = new File(urlClassDir.getPath());
		File baseDir = null;
		// move into JAR's parent directory only if launched from a JAR
		if (strClassDir.indexOf(".jar!/" + relClassLoc) != -1) {
			baseDir =
				fileClassDir
					.getParentFile()
					.getParentFile()
					.getParentFile()
					.getParentFile()
					.getParentFile();
		} else { // not from JAR; one less parent directory
			baseDir =
				fileClassDir
					.getParentFile()
					.getParentFile()
					.getParentFile()
					.getParentFile();
		}
		/* convert "%20", the escape character for a space, into " ";
		   conversion necessary starting with JRE v.1.4.0
		   (see http://developer.java.sun.com/developer/ //
		   bugParade/bugs/4466485.html)
		*/
		String strBaseDir = baseDir.toString();
		int space = 0;
		// continue while still have "%20", the spaces symbol
		while ((space = strBaseDir.indexOf("%20")) != -1) {
			if (strBaseDir.length() > space + 3) {
				strBaseDir =
					strBaseDir.substring(0, space)
						+ " "
						+ strBaseDir.substring(space + 3);
			} else {
				strBaseDir = strBaseDir.substring(0, space) + " ";
			}
		}
		/* Though simpler, this alternative solution crashes 
		   after a NoSuchMethodError under JRE <= 1.3.
		*/
		/*
		baseDir = new File(baseDir.toString().replaceAll("%20", " "));
		File plugInsFile = new File(baseDir, "plugins");
		*/

		// base directory;
		// considered nonexistent since baseDir's path in URL syntax
		baseDir = new File(strBaseDir);
		String basePath = baseDir.getPath();

		// directory path given as URL; need to parse into normal syntax
		String protocol = "file:";
		int pathStart = basePath.indexOf(protocol);
		// check if indeed given as URL;
		// if so, delete protocol and any preceding info
		if (pathStart != -1)
			basePath = basePath.substring(pathStart + protocol.length());
		// plugInsPath now in normal syntax
		baseDir = new File(basePath); // the actual file

		// If necessary, adjust path to properly navigate across the network
		if (!baseDir.exists()) {
			// According to testing on Windows XP, an extra backslash needs
			// to be added to the start of the path to create the format: 
			// "\\COMPUTER_NAME\ShareName"
			baseDir = new File("\\" + basePath);
		}
		
		return baseDir;
	}
	
	public static URI getBaseURI(String folders) {
		try {
		URI baseURI = new URI(com.textflex.texttrix.LibTTx.class.
				getProtectionDomain().getCodeSource().getLocation().toString());
		String path = baseURI.toString();
		if (path.endsWith(".jar")) {
			path = path.substring(0, path.lastIndexOf("/")) + "/";
		}
		path = path.endsWith("/") ? path + folders : path + "/" + folders;
		path = path.endsWith("/") ? path : path + "/";
		baseURI = new URI(path);
		//System.out.println("URI: " + baseURI + ", path: " + baseURI.getPath());
		return baseURI;
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static URI getBaseURI() {
		return getBaseURI("");
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
		if (list == null) return null;
		int listLen = list.length;
		for (int i = 0; i < listLen; i++) {
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
			int j = 0;
			PlugIn pl = null;
			for (int i = 0; i < plugInList.length; i++) {
				pl = loadPlugIn(plugInDir, plugInList[i]);
				if (pl != null) plugIns[j++] = pl;
			}
			plugIns = (PlugIn[]) truncateArray(plugIns, j);
//			System.out.println("plugIns length: " + j);
		}
		return plugIns;
	}

	/** Create the object of the given name in the class loader's location.
	@param name name of object to load
	@param loader class loader referencing a location containing
	the named class
	*/
	public static Object createObject(String name, ClassLoader loader) 
		throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		Object obj = null; // the object to return
		// load the class
		try {
			Class cl = loader.loadClass(name);
			obj = cl.newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
			throw e;
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			throw e;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			throw e;
		} catch (RuntimeException e) {
			e.printStackTrace();
			throw e;
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
				array = (String[]) growArray(array);
			}
			array[arrayInd++] = tokenizer.nextToken();
		}
		return (String[]) truncateArray(array, arrayInd);
	}
	
	/**Creates a string from a one-dimensional array of strings.
	 * 
	 * @param array the array of strings
	 * @param offset the element at which to start
	 * @param len the number of elements to append to the output string
	 * @param newlines <code>true</code> if newlines should be appended
	 * the end of each element's string that doesn't alreay have a newline there
	 * @return the final, strung string
	 */
	public static String createStringFromArray(
		String[] array,
		int offset,
		int len,
		boolean newlines) {
		StringBuffer buf = new StringBuffer(1000);
		int finalLen = offset + len;
		for (int i = offset; i < finalLen && i < array.length; i++) {
			buf.append(array[i]);
			// appends newlines at the end of each element if it doesn't
			// already have one
			if (newlines && !array[i].endsWith("\n")) {
				buf.append("\n");
			}
		}
		return buf.toString();
	}
	
	/**Creates a string from an array of strings.
	 * All elements from the array are included.
	 * 
	 * @param array the array of strings
	 * @param newlines <code>true</code> if newlines should be appended
	 * the end of each element's string that doesn't alreay have a newline there
	 * @return the final, strung string
	 * @return the final, strung string
	 */
	public static String createStringFromArray(
		String[] array,
		boolean newlines) {
		return createStringFromArray(array, 0, array.length, newlines);
	}

	/**Checks whether a string is the same as an element in an array of strings.
	 * 
	 * @param find the string to find
	 * @param list the array of strings in which to try tot find it
	 * @return <code>true</code> if an equivalent string is found
	 */
	public static boolean inUnsortedList(String find, String[] list) {
		String check = null;
		for (int i = 0; i < list.length && (check = list[i]) != null; i++) {
			if (check.equals(find))
				return true;
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
	public static int reverseIndexOf(
		String str,
		String searchStr,
		int offset) {
		int searchLen = searchStr.length();
		int strLen = str.length();
		if (offset > strLen || offset < 0) return -1;
		// start position
		int i = offset - 1;
		while (i >= 0
//			&& i + searchLen <= strLen
			&& !str.substring(i).startsWith(searchStr)) {
			i--;
		}
		return i;
	}
	
	/** Searches backward in a text to find a given normal-oriented string.
	 * For example, in the text, text = "Mr. Smith went to the door went out,"
	 * reverseIndexOf(text, "went", 14) would return 10. 
	 * @param doc Document in which to search
	 * @param searchStr string to find
	 * @param offset index of first character not included in the search
	 * @return index of found string; -1 if not found
	 */
	public static int reverseIndexOf(
		Document doc,
		String searchStr,
		int offset) {
		// TODO: needs to be tested
		int docLen = doc.getLength();
		int len = searchStr.length();
		if (offset > docLen || offset < 0) return -1;
		// start position
		int i = offset - 1;
		// reposition start if quarry would extend beyond len of doc
		if (i + len > docLen) i = docLen - len;
		try {
			// compares section of document of the same len
			// as the search string with the search string
			// because document must be given a length for getText
			while (i >= 0
//				&& i + len <= docLen
				&& !doc.getText(i, len).equals(searchStr)) {
				i--;
			}
		} catch (BadLocationException e) {
			e.printStackTrace();
			i = -1;
		}
		// ensures that any negative values are returned as -1
		if (i < 0) i = -1;
		return i;
	}
	
	/** Gets the file extension from a given path.
	 * Assumes that the file extension is the substring
	 * after the final period in the filename.
	 * @param path path to file, or even just a filename
	 * @return the substring following the final period, or
	 * an empty string if no period is found
	 */
	public static String getFileExtension(String path) {
		int exti = path.lastIndexOf(".") + 1;
		String ext = "";
		if (exti > 0) {
			ext = path.substring(exti);
		}
		return ext;
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
		in = TextTrix.class.getResourceAsStream(path);
		if (in != null) {
			try {
				reader = new BufferedReader(new InputStreamReader(in));
				text = readText(reader);
			} finally { // clean-up code
				try {
					if (reader != null) reader.close();
					in.close();
				} catch (IOException exception) {
				}
			}
		}
		return text;
	}

	/**Read in text from a file and return the text as a string.
	 * Differs from <code>displayFile(String path)</code> because
	 * allows editing. Note that the reader is not closed after reading.
	 * @param reader text file stream
	 * @return text from file
	 */
	public static String readText(BufferedReader reader) {
		String text = "";
		StringBuffer builder = new StringBuffer();
		String line;
		// read lines until none remain;
		// the calling function should handle clean-up code for the reader stream.
		// StringBuilder greatly improves performance over simple String concatenation.
		try {
			int c;
			while ((c = reader.read()) != -1)
				//builder.append(line + "\n");
				builder.append((char)c);
/*
			while ((line = reader.readLine()) != null)
				builder.append(line + "\n");
//				text = text + line + "\n";
*/
		} catch (IOException exception) {
			exception.printStackTrace();
		}
		text = builder.toString();
		return text;
	}
	
	public static String getEOL(String text) {
		int r = text.indexOf('\r');
		String eol = "\n"; // defaults to LF (Unix)
		String eolName = "LF";
		if (r != -1) {
			int n = text.indexOf('\n');
			if (n == r + 1) {
				eol = "\r\n"; // CRLF (Windows)
				eolName = "CRLF";
			} else {
				eol = "\r"; // CR (old-style Mac)
				eolName = "CR";
			}
		}
		System.out.println("eolName: " + eolName);
		return eol;
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
	 * @param keyStroke accelerator key shortcut; when using KeyStroke
	 * .getKeyStroke(String), note that the string can be a space-delimited
	 * list of modifiers plus typed keys, such as "ctrl S" or even something
	 * as simple as "F5"
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

	/**Sets an action's properties.
	 * @param action action to set
	 * @param description tool tip
	 */
	public static void setAction(Action action, String description) {
		action.putValue(Action.SHORT_DESCRIPTION, description);
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

	/**Sets an action's properties.
	 * @param action action to set
	 * @param path path to icon file
	 * @see #makeIcon
	 */
	public static void setActionIcon(Action action, String path) {
		action.putValue(Action.SMALL_ICON, makeIcon(path));
	}

	/** Creates an image icon.
	@param path image file location relative to TextTrix.class
	@return icon from archive; null if the file cannot be retrieved
	 */
	public static ImageIcon makeIcon(String path) {
		URL iconURL = LibTTx.class.getResource(path);
		
		/* Workaround for possibly applicable Java bug #6231864
		 * (http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6231864)
		 *
		 * Image loading is slower when creating directly with ImageIcon.
		 * The ImageIO method appears to be faster, reducing icon creation
		 * times during start-up by approx. 50%.
		 */
		try {
			if (iconURL != null) {
				ImageIcon icon = new ImageIcon(ImageIO.read(iconURL));
				return icon;
			}
		} catch(IllegalArgumentException e) {
		} catch(IOException e1) {
		}
		return null;
		
//		return (iconURL != null) ? new ImageIcon(iconURL) : null;
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

	/**Counts the number of lines visible in the given text component.
	 * For example, the text in a JTextArea may have newlines only at
	 * paragraph divisions or blank lines, but the graphical text
	 * component wraps the text so that it appears to have many lines.
	 * This method counts those lines.  The final line is ignored if it
	 * is empty.
	 * @param c the graphical text component, such as <code>JTextArea</code>
	 * or its subclasses
	 * @return the number of visible lines, ignoring the final line if
	 * it is blank
	 */
	public static int getVisibleLineCount(JTextComponent c) {
		// ignores final line if empty
		int len = c.getDocument().getLength();
		int offset = 0;
		int count = 0;
		try {
			while (offset < len) {
				offset = Utilities.getRowEnd(c, offset) + 1;
				count++;
			}
		} catch (BadLocationException e) {
		}
		return count;
	}
	
	/**Gets the visible lines in a text component, each line filling an
	 * element of an array of strings.
	 * 
	 * @param c the text diaplay, almost always with multiple lines, 
	 * such as <code>JTextArea</code> of <code>TextPad</code>
	 * @return an array of the visible lines, with each element consisting
	 * of one line from the display, whether the line terminates in a hard
	 * or a soft return
	 * @deprecated replaced by <code>getPrintableLines(JTextPane)</code>,
	 * which constructs an array of <code>PrintPadText</code> to store
	 * both text and its formatting
	 */
	public static String[] getVisibleLines(JTextComponent c) {
		String[] lines = new String[100]; // defaults to 100 lines
		int linesIdx = 0;
		int len = c.getDocument().getLength();
		int offset = 0;
		int end = 0;
		try {
			while (offset < len) {
				end = Utilities.getRowEnd(c, offset) + 1;
				// grows the array if full
				if (linesIdx >= lines.length) {
					lines = (String[]) growArray(lines);
				}
				// adds the line
				lines[linesIdx++] =
					c.getDocument().getText(offset, end - offset);
				offset = end;
			}
		} catch (BadLocationException e) {
		}
		return (String[]) truncateArray(lines, linesIdx);
	}
	
	/**Gets the text displayed in a <code>JTextPane</code> as 
	 * displayed on-screen, including formatting for the text.
	 * Useful for printing text directly from the screen.
	 * Lines are demarcated not by hard returns, but the soft return
	 * that the <code>JTextPane</code> inserts.  Expanding or 
	 * widening the screen can alter the line divisions and yields
	 * a new array of printable lines.
	 * 
	 * @param c the text display, almost always with multiple lines
	 * @return an array of the displayed lines and their formatting, with each 
	 * element consisting
	 * of one line from the display, whether the line terminates in a hard
	 * or a soft return
	 */
	public static PrintPadText[] getPrintableLines(JTextPane c) {
		
		PrintPadText[] lines = new PrintPadText[100]; // defaults to 100 lines
		int linesIdx = 0;
		StyledDocument doc = c.getStyledDocument();
		int len = doc.getLength();
		int offset = 0;
		int end = 0;
		PrintPadText line = null;
		Element paragraph = null;
		try {
		
			// cylce through the paragraphs
			while (offset < len) {
				paragraph = doc.getParagraphElement(offset);
				int paragraphEnd = paragraph.getEndOffset();
				AttributeSet paragraphAttr = paragraph.getAttributes();
				boolean firstLine = true;
				//System.out.println("offset: " + offset + ", paraEnd: " + paragraphEnd);
				//System.out.println("left indent: " + StyleConstants.getLeftIndent(paragraphAttr));
				
				// cycle through the lines within each paragraph
				while (offset < paragraphEnd) {
					// find the end of the row, or line
					end = Utilities.getRowEnd(c, offset) + 1;
					// find the indentation of the line
					float indent = StyleConstants.getLeftIndent(paragraphAttr);
					// store the line and its formatting
					line = new PrintPadText(
						doc.getText(offset, end - offset),
						indent);
				
					// grow the array if full and add the line
					if (linesIdx >= lines.length) {
						lines = (PrintPadText[]) growArray(lines);
					}
					lines[linesIdx++] = line;
					offset = end;
				}
			}
		} catch (BadLocationException e) {
		}
		return (PrintPadText[]) truncateArray(lines, linesIdx);
	}
	

	/** Gets the next whole word from a given position, assuming that the position
	 * is not in the middle of a word.
	 * @param text text to search
	 * @param start index at which to start
	 * @param finish first index at which to stop searching, though the word may extend 
	 * to or past this index
	 * @return the whole word
	 */
	public static String getWord(String text, int start, int finish) {
		int n = start; // becomes position of start of word
		int end = start + 1; // becomes first character after word
		//int len = end;
		String specialChars = "_\'";
		String word = "";
		// skip over non-letters/non-digits
		char c = 0;
		while (n < finish
			&& !Character.isLetterOrDigit(c = text.charAt(n))
			&& specialChars.indexOf(c) < 0) {
			//		System.out.println("skipped char: " + c);
			n++;
		}
		if (n >= finish)
			return "";
		// progress to the end of a word
		end = n + 1;
		while (end < finish
			&& (Character.isLetterOrDigit(c = text.charAt(end))
				|| specialChars.indexOf(c) >= 0)) {
			//		System.out.println("included char: " + c);
			end++;
		}
		//		}
		return text.substring(n, end);
	}
	
	/** Converts stub escape characters to real ones.
	 * <br> "^t" and "^T" become "\t"
	 * <br> "^n" and "^N" become "\n"
	 * @param escapedStr the string in which to convert to true escape chars
	 * @return the converted string
	*/
	public static String convertEscapeChars(String escapedStr) {
		String convertedStr = escapedStr
			.replaceAll("\\^t", "\t")
			.replaceAll("\\^T", "\t")
			.replaceAll("\\^n", "\n")
			.replaceAll("\\^N", "\n");
//		System.out.println("escapedStr: " + escapedStr + ", convertedStr: " + convertedStr);
		return convertedStr;
	}
	
	/** Randomly picks a string from an array, with increasing weight given to 
	 * earlier elements in the array as the weight factor increases.
	 * @param strings the array of strings from which to randomly choose one
	 * @param weightFront the weighting factor; larger numbers weight more
	 * toward earlier elements in the array; should be > 0
	 * @return the chosen string
	*/
	public static String pickWeightedStr(String[] strings, int weightFront) {
		int n = (int) (strings.length * Math.pow(Math.random(), weightFront));
		return strings[n];
	}
	
	
	/**
	 * Front-end, helper function to ask yes/no questions.
	 * 
	 * @param owner
	 *            parent frame; can be null
	 * @param msg
	 *            message to display in the main window section
	 * @param title
	 *            title to display in the title bar
	 * @return true for "Yes", false for "No"
	 */
	public static boolean yesNoDialog(Component owner, String msg, String title) {
		int choice = JOptionPane.showConfirmDialog(owner, msg, title,
				JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
		// true for Yes, false for No
		if (choice == JOptionPane.YES_OPTION) {
			return true;
		} else {
			return false;
		}
	}
	
	public static boolean checkJavaVersion(String version) {
		String installedVersionStr = System.getProperty("java.version");
//		float installedVersionNum = Float.parseFloat(installedVersionStr);
//		System.out.println("installedVersionStr: " + installedVersionStr);
		return installedVersionStr.indexOf(version) != -1;
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
