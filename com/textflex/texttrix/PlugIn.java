/* PlugIn.java    
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

import javax.swing.*;
import java.net.*;
import java.io.*;
import java.util.jar.*;

/** The abstract superclass for all plugins.
    Specifies the files and locations from which to load plugin resources
    as well as the code to both load them and manipulate text.
    All plugins must extend this abstract class and be packaged in 
    uncompressed JAR files.
*/
public abstract class PlugIn {
    private String description = null; // short description
    private String detailedDescription = null; // long, HTML-formatted desc.
    private String name = null; // plugin name
    private String iconPath = null; // path to main icon
    private String rollIconPath = null; // path to rollover icon
    private String detailedDescriptionPath = null; // path to formatted desc.
    private String category = null; // plugin category, eg "tools"
    private String path = null; // plugin JAR's path
    private JarFile jar = null; // plugin's JAR

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
    }

    /** Sets the plugin's path.
	@param aPath path
     */
    public void setPath(String aPath) { path = aPath; }

    /** Runs the plugin on a given section of the text.
	@param s text to manipulate
	@param x index at which to start
	@param y first index at which to no longer work
    */
    public abstract String run(String s, int x, int y);

    /** Runs the plugin over all the given text.
	@param s text to manipulate
    */
    public abstract String run(String s);
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
	/* Workaround for a bug in JRE v.1.4, where PNG/GIF images
	   don't load from compressed JAR files.  The workaround
	   first loads the date into a byte array before loading
	   the array into a PNG constructor.  Evidently the two-step
	   inflation process avoids creating a chain of streams
	   with more than one zip inflator
	   (see http://developer.java.sun.com/developer/bugParade/bugs/ //
	   4764639.html)
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

    public abstract BufferedReader getDetailedDescription();

    public String getDetailedDescriptionPath() { 
	return detailedDescriptionPath; 
    }


}
