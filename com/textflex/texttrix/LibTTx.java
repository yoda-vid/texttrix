/* LibTTx.java    
   Text Trix
   the text tinker
   http://textflex.com/texttrix
   
   Copyright (c) 2002-3, Text Flex
   All rights reserved.
   
   Redistribution and use in source and binary forms, with or without
   modification, are permitted provided that the following conditions 
   are met:
   
   * Redistributions of source code must retain the above copyright
   notice, this list of conditions and the following disclaimer.
   * Redistributions in binary form must reproduce the above copyright
   notice, this list of conditions and the following disclaimer in the
   documentation and/or other materials provided with the distribution.
   * Neither the name of the Text Trix nor the names of its
   contributors may be used to endorse or promote products derived
   from this software without specific prior written permission.
   
   THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
   IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
   TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
   PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
   OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
   EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
   PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
   PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
   LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
   NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
   SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.  
*/

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

    public static PlugIn[] loadPlugIns(File plugInDir) {
	//	String plugInDirName = path;//"plugins";
	//	File plugInDir = new File(plugInDirName);
	String endsWith = ".jar"; // only list files ending w/ team.txt
	EndsWithFileFilter filter = new EndsWithFileFilter();
	filter.add(endsWith);
	String[] plugInList = plugInDir.list(filter);
	//       	System.out.println(LibTTx.class.getClassLoader().getResource("com").toString());
	//	System.out.println((new File(LibTTx.class.getResource("com").getFile())).getParentFile().getAbsoluteFile().toString());
	//	System.out.println(LibTTx.class.getResource("LibTTx.class").getFile());
	//	System.out.println(ClassLoader.getSystemResource("texttrix.jar").toString());
	//	String dr = System.getProperty("user.dir");
	//	String sp = System.getProperty("file.separator");
	//	try { System.out.println(new URL("file://" + dr + sp 
	//					 + "plugins"));
	//	} catch (MalformedURLException e) {}
	PlugIn[] plugIns = null;
	// traverse /teams dir, listing names w/o the ending
	if (plugInList != null) {
	    plugIns = new PlugIn[plugInList.length];
	    for (int i = 0; i < plugInList.length; i++) {
		//		URL = 
		ClassLoader loader = null;
		String path = null;
		try {
		    /*
		    String dir = System.getProperty("user.dir");
		    String sep = System.getProperty("file.separator");
		    URL url = new URL("file://" + dir + sep 
				      + "plugins" + sep + plugInList[i]);
		    */
		    //		    System.out.println(plugInDir.toString());
		    String sep = System.getProperty("file.separator");
		    path = plugInDir.toString() 
			+ sep + plugInList[i];
		    String urlPath = "file://" + path;
		    //		    URL url = new URL(urlPath);
		    URL url = new File(path).toURL();
		    System.out.println(url.toString());
		    loader = new URLClassLoader(new URL[] { url } );
		    //		    System.out.println(url.toString());
		} catch (MalformedURLException e) {}
		String name = "com.textflex.texttrix." 
		    + plugInList[i]
		    .substring(0, plugInList[i].indexOf(".jar"));
		plugIns[i] = (PlugIn) createObject(name, loader);
		plugIns[i].setPath(path);
	    }
	}
	return plugIns;
    }

    static Object createObject(String name, ClassLoader loader) {
	Object obj = null;
	try {
	    //	    Class cl = Class.forName(name);
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
