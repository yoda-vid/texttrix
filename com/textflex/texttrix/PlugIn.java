/* TextTrix.java    
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

public abstract class PlugIn {
    private String description = null;
    private String detailedDescription = null;
    private String name = null;
    private String iconPath = null;
    private String rollIconPath = null;
    private String detailedDescriptionPath = null;
    private String category = null;
    private String path = null;
    private JarFile jar = null;

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

    public void setPath(String aPath) { path = aPath; }

    public abstract String run(String s, int x, int y);

    public abstract String run(String s);

    public String getName() { return name; }

    public String getIconPath() { return iconPath; }

    public String getRollIconPath() { return rollIconPath; }

    public String getDescription() { return description; }

    public String getCategory() { return category; }

    public ImageIcon getIcon(String descPath, Class cl) {
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
	    descPath = "com/textflex/texttrix/" + descPath;
	    //	    System.out.println((new File(path)).exists());
	    if (jar == null) 
		jar = new JarFile(new File(path));
	    JarEntry entry = jar.getJarEntry(descPath);
	    //	    System.out.println(entry.getName());
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

    public BufferedReader getDetailedDescription(String descPath, Class cl) {
	/*
	URL url = cl.getResource(path);
	//	URLConnection uc = url.openConnection();
	System.out.println(url.toString());
	//	InputStream in = cl.getResourceAsStream(path);
	InputStreamReader in = null;
	try {
	    in = new InputStreamReader(url.openStream());
	} catch (IOException e) {
	    e.printStackTrace();
	}
	return new BufferedReader(in);//new InputStreamReader(in));
	*/
	BufferedReader reader = null;
	try {
	    descPath = "com/textflex/texttrix/" + descPath;
	    //	    System.out.println((new File(path)).exists());
	    if (jar == null) 
		jar = new JarFile(new File(path));
	    JarEntry entry = jar.getJarEntry(descPath);
	    //	    System.out.println(entry.getName());
	    InputStreamReader in = new InputStreamReader(jar.getInputStream(entry));
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
