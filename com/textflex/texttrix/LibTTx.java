package com.textflex.texttrix;

import java.lang.reflect.*;
import java.io.*;
import java.net.*;

public class LibTTx {

    public LibTTx() {
    }

    /*
    public static void main(String[] args) {
	
	PlugIn[] plugIns = loadPlugIns("plugins");
	//      	System.out.println(NonPrintingChars.print("I do not\nknow", 0, 13));
	for (int i = 0; i < plugIns.length; i++) {
	    System.out.println("plug-in " + i + ": " + plugIns[i].getName());
	    plugIns[i].getIcon();
	    plugIns[i].getRollIcon();
	    System.out.println(readText(plugIns[i].getDetailedDescription()));
	}
	
	//      	System.out.println(NonPrintingChars.print("I do not\nknow", 0, 13));
    }
    */

    public static PlugIn[] loadPlugIns(String path) {
	String plugInDirName = path;//"plugins";
	File plugInDir = new File(plugInDirName);
	String endsWith = ".jar"; // only list files ending w/ team.txt
	EndsWithFileFilter filter = new EndsWithFileFilter();
	filter.add(endsWith);
	String[] plugInList = plugInDir.list(filter);
	PlugIn[] plugIns = new PlugIn[plugInList.length];
	// traverse /teams dir, listing names w/o the ending
	if (plugInList != null) {
	    for (int i = 0; i < plugInList.length; i++) {
		//		URL = 
		ClassLoader loader = null;
		try {
		    String dir = System.getProperty("user.dir");
		    String sep = System.getProperty("file.separator");
		    URL url = new URL("file://" + dir + sep 
				      + "plugins" + sep + plugInList[i]);
		    loader = new URLClassLoader(new URL[] { url } );
		    //		    System.out.println(url.toString());
		} catch (MalformedURLException e) {}
		String name = "com.textflex.texttrix." 
		    + plugInList[i]
		    .substring(0, plugInList[i].indexOf(".jar"));
		plugIns[i] = (PlugIn) createObject(name, loader);
	    }
	}
	return plugIns;
    }

    static Object createObject(String name, ClassLoader loader) {
	Object obj = null;
	try {
	    //	    Class cl = Class.forName(name);
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


    /** Increases arrays to accomodate 10% + 10 more elements.
	Allows an unlimited number of elements in a given array.
	@param array to increase in size
	@return larger array
    */
    public static Object growArray(Object array) {
	Class arrayClass = array.getClass();
	if (!arrayClass.isArray()) return null; // exit immediately
	Class componentType = arrayClass.getComponentType();
	int len = Array.getLength(array);
	// increase array length by 10% + 10 elements
	Object newArray =  Array.newInstance(componentType, 
					     len * 11 / 10 + 10);
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
       	if (!arrayClass.isArray()) return null; // exit immediately
	Class componentType = arrayClass.getComponentType();
	Object newArray = Array.newInstance(componentType, length);
	System.arraycopy(array, 0, newArray, 0, length);
	return newArray;
    }


    /**Read in text from a file and return the text as a string.
     * Differs from <code>displayFile(String path)</code> because
     * allows editing.
     * @param reader text file stream
     * @return text from file
     */
    public static String readText(String path) {
	String text = "";
	try {
	    InputStream in = TextTrix.class.getResourceAsStream(path);
	    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
	    String line;
	    while ((line = reader.readLine()) != null)
		text = text + line + "\n";
	} catch(IOException exception) {
	    exception.printStackTrace();
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
	try {
	    while ((line = reader.readLine()) != null)
		text = text + line + "\n";
	} catch(IOException exception) {
	    exception.printStackTrace();
	}
	return text;
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
