package com.textflex.texttrix;

import javax.swing.*;
import java.net.*;
import java.io.*;

public abstract class PlugIn {
    private String description = null;
    private String detailedDescription = null;
    private String name = null;
    private String iconPath = null;
    private String rollIconPath = null;
    private String detailedDescriptionPath = null;
    private String category = null;

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

    public abstract String run(String s, int x, int y);

    public String getName() { return name; }

    public String getIconPath() { return iconPath; }

    public String getRollIconPath() { return rollIconPath; }

    public String getDescription() { return description; }

    public String getCategory() { return category; }

    public ImageIcon getIcon(String path, Class cl) {
	URL url = cl.getResource(path);
	//	System.out.println(path);
	//	System.out.println(url.toString());
	return new ImageIcon(url);
    }

    public abstract ImageIcon getIcon();

    public abstract ImageIcon getRollIcon();

    public BufferedReader getDetailedDescription(String path, Class cl) {
	InputStream in = cl.getResourceAsStream(path);
	return new BufferedReader(new InputStreamReader(in));
    }

    public abstract BufferedReader getDetailedDescription();

    public String getDetailedDescriptionPath() { 
	return detailedDescriptionPath; 
    }


}
