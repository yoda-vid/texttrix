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
