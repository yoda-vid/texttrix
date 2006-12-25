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
 * Portions created by the Initial Developer are Copyright (C) 2006-7
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

import javax.swing.filechooser.FileFilter;
import java.util.*;
import java.io.*;

/**
 * Filters for files with specific extensions.
 */
public class ExtensionFileFilter extends FileFilter {
	private String description = ""; // description of the file type grouping
	private ArrayList extensions = new ArrayList(); // the extensions

	/**
	 * Add extension to include for file display. May need to modify to check
	 * whether extension has already been added.
	 * 
	 * @param extension file extension, such as <code>.txt</code>, 
	 * though the period is optional
	 */
	public void addExtension(String extension) {
		if (!extension.startsWith("."))
			extension = "." + extension;
		extensions.add(extension.toLowerCase());
	}

	/**
	 * Sets the file type description.
	 * 
	 * @param aDescription
	 *            file description, such as <code>text
	 * files</code> for
	 *            <code>.txt</code> files
	 */
	public void setDescription(String aDescription) {
		description = aDescription;
	}

	/**
	 * Gets file type's description.
	 * 
	 * @return file type description.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Accept a given file to display if it has the extension currently being
	 * filtered for.
	 * 
	 * @param f
	 *            file whose extension need to check
	 * @return <code>true</code> if accepts file, <code>false</code> if
	 *         don't
	 */
	public boolean accept(File f) {
		if (f.isDirectory())
			return true;
		String name = f.getName().toLowerCase();

		for (int i = 0; i < extensions.size(); i++)
			if (name.endsWith((String) extensions.get(i)))
				return true;
		return false;
	}
}
