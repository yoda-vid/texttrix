package net.sourceforge.texttrix;

import java.lang.reflect.*;
import java.io.*;
import java.net.*;

public class LibTTx {

    public LibTTx() {
    }

    public static void main(String[] args) {
	PlugIn[] plugIns = loadPlugIns("net/sourceforge/texttrix");
	System.out.println(NonPrintingChars.print("I do not\nknow", 0, 13));
	for (int i = 0; i < plugIns.length; i++) {
	    System.out.println("plug-in " + i + ": " + plugIns[i].getName());
	}
    }

    public static PlugIn[] loadPlugIns(String path) {
	String plugInDirName = path;//"plugins";
	File plugInDir = new File(plugInDirName);
	String endsWith = "Chars.class"; // only list files ending w/ team.txt
	EndsWithFileFilter filter = new EndsWithFileFilter();
	filter.add(endsWith);
	String[] plugInList = plugInDir.list(filter);
	PlugIn[] plugIns = new PlugIn[plugInList.length];
	// traverse /teams dir, listing names w/o the ending
	if (plugInList != null) {
	    for (int i = 0; i < plugInList.length; i++) {
		String name = "net.sourceforge.texttrix." + plugInList[i].substring(0, plugInList[i].indexOf(".class"));
		plugIns[i] = (PlugIn) createObject(name);
	    }
	}
	return plugIns;
    }

    static Object createObject(String name) {
	Object obj = null;
	try {
	    Class cl = Class.forName(name);
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
    public Object truncateArray(Object array, int length) {
	Class arrayClass = array.getClass();
       	if (!arrayClass.isArray()) return null; // exit immediately
	Class componentType = arrayClass.getComponentType();
	Object newArray = Array.newInstance(componentType, length);
	System.arraycopy(array, 0, newArray, 0, length);
	return newArray;
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
