/*Text Trix
 *a goofy gui editor
 *v.0.1.3pre0

 *Copyright (c) 2002, David Young
 *All rights reserved.

 *Redistribution and use in source and binary forms, with or without
 *modification, are permitted provided that the following conditions 
 *are met:

    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the name of the Text Trix nor the names of its
      contributors may be used to endorse or promote products derived
      from this software without specific prior written permission.

 *THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 *IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 *TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 *PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
 *OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 *EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 *PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 *PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 *LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 *NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 */

package net.sourceforge.texttrix;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.filechooser.FileFilter;

/** The main <code>TextTrix</code> class.  Sets up the window
    and performs the text manipulation.
*/
public class TextTrix extends JFrame {
    ArrayList textAreas = new ArrayList();
    JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
    JFileChooser chooser = new JFileChooser();

    public TextTrix() {
	setTitle("Text Trix");
	setSize(500, 600);
	setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	addTextArea(textAreas, tabbedPane, "");

	JMenuBar menuBar = new JMenuBar();
	JMenu fileMenu = new JMenu("File");
	JMenu editMenu = new JMenu("Edit");
	JMenu helpMenu = new JMenu("Help");

	/* File menu items */
	JMenuItem newItem = fileMenu.add(new AbstractAction("New") {
		public void actionPerformed(ActionEvent evt) {
		    addTextArea(textAreas, tabbedPane, "");
		}
	    });
	/* Comment-out until key bindings pref
	newItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,
						      InputEvent.CTRL_MASK));
	*/

	JMenuItem openItem = new JMenuItem("Open...");
	fileMenu.add(openItem);
	openItem.addActionListener(new FileOpenListener());
	openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
						       InputEvent.CTRL_MASK));

	JMenuItem closeItem = fileMenu.add(new AbstractAction("Close") {
		public void actionPerformed(ActionEvent evt) {
			int i = tabbedPane.getSelectedIndex();
			TextPad t = (TextPad)textAreas.get(i);
			if (t.getChanged()) {
				String s = "Please save first.";
				JOptionPane op = new JOptionPane();
				op.showMessageDialog(op, s);
			} else
				removeTextArea(tabbedPane.getSelectedIndex(), textAreas, tabbedPane);
		}
	});

	JMenuItem saveItem = fileMenu.add(new AbstractAction("Save") {
		public void actionPerformed(ActionEvent evt) {
			String path = ((TextPad)textAreas
				.get(tabbedPane.getSelectedIndex())).getPath();
			if (path != "")
				saveFile(path);
			else
				fileSaveDialog();
		}
	});
	saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
				InputEvent.CTRL_MASK));

	JMenuItem saveAsItem = new JMenuItem("Save as...");
	fileMenu.add(saveAsItem);
	saveAsItem.addActionListener(new FileSaveListener());
/*	saveAsItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
						      InputEvent.CTRL_MASK));
*/

	fileMenu.addSeparator();
	
	JMenuItem exitItem = fileMenu.add(new AbstractAction("Exit") {
		public void actionPerformed(ActionEvent evt) {
		    System.exit(0);
		}
	    });
	exitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q,
						       InputEvent.CTRL_MASK));

	/* Edit menu items */
	JMenuItem undoItem = editMenu.add(new AbstractAction("Undo") {
		public void actionPerformed(ActionEvent evt) {
			((TextPad)textAreas.get(tabbedPane.getSelectedIndex())).undo();
		}
	});
	undoItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z,
				InputEvent.CTRL_MASK));

	
	JMenuItem redoItem = editMenu.add(new AbstractAction("Redo") {
		public void actionPerformed(ActionEvent evt) {
			((TextPad)textAreas.get(tabbedPane.getSelectedIndex())).redo();
		}
	});
	redoItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y,
				InputEvent.CTRL_MASK));

	editMenu.addSeparator();
	
	JMenuItem cutItem = editMenu.add(new AbstractAction("Cut") {
		public void actionPerformed(ActionEvent evt) {
		    ((TextPad)textAreas.get(tabbedPane.getSelectedIndex())).cut();
		}
	    });
	cutItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X,
						      InputEvent.CTRL_MASK));

	JMenuItem copyItem = editMenu.add(new AbstractAction("Copy") {
		public void actionPerformed(ActionEvent evt) {
		    ((TextPad)textAreas.get(tabbedPane.getSelectedIndex())).copy();
		}
	    });
	copyItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C,
						       InputEvent.CTRL_MASK));

	JMenuItem pasteItem = editMenu.add(new AbstractAction("Paste") {
		public void actionPerformed(ActionEvent evt) {
		    ((TextPad)textAreas.get(tabbedPane.getSelectedIndex())).paste();
		}
	    });
	pasteItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V,
							InputEvent.CTRL_MASK));

	editMenu.addSeparator();

	JMenuItem selectAllItem = editMenu.add(new AbstractAction("Select All") {
		public void actionPerformed(ActionEvent evt) {
		    ((TextPad)textAreas.get(tabbedPane.getSelectedIndex())).selectAll();
		}
	    });
/* Comment-out until bindings pref
	selectAllItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A,
							    InputEvent.CTRL_MASK));
*/
	/* Help menu items */
	JMenuItem aboutItem = helpMenu.add(new AbstractAction("About...") {
		public void actionPerformed(ActionEvent evt) {
			try {
				BufferedReader reader = new BufferedReader(new
					InputStreamReader(TextTrix.class.
						getResourceAsStream("about.txt")));
				String about = readText(reader);
				reader.close();
				JOptionPane op = new JOptionPane();
				op.showMessageDialog(op, about);
			} catch(IOException exception) {
				exception.printStackTrace();
			}
		}
	});

	JMenuItem licenseItem = helpMenu.add(new AbstractAction("License") {
		public void actionPerformed(ActionEvent evt) {
			try {
				BufferedReader reader = new BufferedReader(new
					InputStreamReader(TextTrix.class.
						getResourceAsStream("license.txt")));
				String license = readText(reader);
				reader.close();
				// TODO: need to find true path
				addTextArea(textAreas, tabbedPane, "license.txt");
				TextPad t = (TextPad)textAreas.get(tabbedPane.getSelectedIndex());
				t.setEditable(false);
				t.setText(license);
			} catch(IOException exception) {
				exception.printStackTrace();
			}
		}
	});

	JButton stripReturns = new JButton("Remove Extra Hard Returns");
	stripReturns.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent evt) {
		    TextPad t = (TextPad)textAreas.get(tabbedPane.getSelectedIndex());
		    t.setText(t.stripExtraHardReturns(t.getText()));
		}
	    });

	setJMenuBar(menuBar);
	menuBar.add(fileMenu);
	menuBar.add(editMenu);
	menuBar.add(helpMenu);

	Container contentPane = getContentPane();
	GridBagLayout layout = new GridBagLayout();
	contentPane.setLayout(layout);

	GridBagConstraints constraints = new GridBagConstraints();

	constraints.fill = GridBagConstraints.BOTH;
	constraints.anchor = GridBagConstraints.CENTER;
	add(stripReturns, constraints, 0, 0, 1, 1, 0, 0);

	constraints.fill = GridBagConstraints.BOTH;
	constraints.anchor = GridBagConstraints.CENTER;
	add(tabbedPane, constraints, 0, 1, 1, 1, 100, 100);

    }

    /**Publically executable starter method.
       Creates the <code>TextTrix</code> object and displays it.
     */
    public static void main(String[] args) {
	TextTrix textTrix = new TextTrix();
	textTrix.show();
    }

	public String readText(BufferedReader reader) {
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

    /**Creates a new <code>TextPad</code> object, a text area
       for writing, and gives it a new tab.  Can call for
       each new file; names the tab, <code>Filen.txt</code>,
       where <code>n</code> is the tab number.
    */
    public void addTextArea(ArrayList arrayList, 
			JTabbedPane tabbedPane, String path) {
		TextPad textPad = new TextPad(30, 20, path);
		JScrollPane scrollPane = new JScrollPane(textPad);

		textPad.setLineWrap(true);
		textPad.setWrapStyleWord(true);
		tabbedPane.addTab("NewFile.txt", scrollPane);
		arrayList.add(textPad);
		tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1);
	}
	
	public void removeTextArea(int i, ArrayList l, JTabbedPane tp) {
		l.remove(i);
		tp.remove(i);
	}

    /**Adds a new component to the <code>GridBagLayout</code>
       manager.
       @param c component to add
       @param constraints layout constraints object
       @param x column number
       @param y row number
       @param w number of columns to span
       @param h number of rows to span
       @param wx column weight
       @param wy row weight
    */
    public void add(Component c, GridBagConstraints constraints,
			     int x, int y, int w, int h, 
			     int wx, int wy) {
	constraints.gridx = x;
	constraints.gridy = y;
	constraints.gridwidth = w;
	constraints.gridheight = h;
	constraints.weightx = wx;
	constraints.weighty = wy;
	getContentPane().add(c, constraints);
    }

	public void saveFile(String path) {
		try {
			TextPad t = (TextPad)textAreas
				.get(tabbedPane.getSelectedIndex());
			PrintWriter out = new 
			PrintWriter(new FileWriter(path), true);
			out.print(t.getText());
		    out.close();
			t.setChanged(false);
			t.setPath(path);
		} catch(IOException exception) {
			exception.printStackTrace();
		}
	}

	public void fileSaveDialog() {
		chooser.setCurrentDirectory(new File("."));
	   	final ExtensionFileFilter filter = new ExtensionFileFilter();
	   	filter.addExtension("txt");
	   	filter.setDescription("Text files (*.txt)");
	   	chooser.setFileFilter(filter);

    	int result = chooser.showSaveDialog(TextTrix.this);
    	if (result == JFileChooser.APPROVE_OPTION) {
			String path = chooser.getSelectedFile().getPath();
			saveFile(path);
	    }
	}

    private class FileOpenListener implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
	    	chooser.setCurrentDirectory(new File("."));
		    final ExtensionFileFilter filter = new ExtensionFileFilter();
		    filter.addExtension("txt");
	    	filter.setDescription("Text files (*.txt)");
	    	chooser.setFileFilter(filter);

	    	int result = chooser.showOpenDialog(TextTrix.this);

	    	if (result == JFileChooser.APPROVE_OPTION) {
				String path = chooser.getSelectedFile().getPath();

				try {
		    		BufferedReader reader = 
						new BufferedReader(new FileReader(path));
				    String text = readText(reader);
				
		    		// check if tabs exist; get TextPad if true
		    		int tabIndex = tabbedPane.getSelectedIndex();
					TextPad t = null;
		    		if (tabIndex != -1)
						t = (TextPad)textAreas.get(tabIndex);
		    		/* t.getText() != null, even if have typed nothing in it.
					 * Add tab and set its text if no tabs exist or if current
					 * tab has tokens; set current tab's text otherwise */
		    		if (tabIndex == -1 
							|| (new StringTokenizer(t.getText()))
							.hasMoreTokens()) { 
						addTextArea(textAreas, tabbedPane, path);
						t = (TextPad)textAreas
							.get(tabbedPane.getSelectedIndex());
						t.setText(text);
		    		} else {
						t.setText(text);
		    		}
					t.setCaretPosition(0);
					t.setChanged(false);
					t.setPath(path);
	
			    	reader.close();
				} catch(IOException exception) {
				    exception.printStackTrace();
				}
		    }
		}
	}
	
    private class FileSaveListener implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			fileSaveDialog();
		}
    }
}

class ExtensionFileFilter extends FileFilter {
    private String description = "";
    private ArrayList extensions = new ArrayList();

    public void addExtension(String extension) {
	if (!extension.startsWith("."))
	    extension = "." + extension;
	extensions.add(extension.toLowerCase());
    }

    public void setDescription(String aDescription) {
	description = aDescription;
    }

    public String getDescription() {
	return description;
    }

    public boolean accept(File f) {
	if (f.isDirectory()) return true;
	String name = f.getName().toLowerCase();

	for (int i = 0; i < extensions.size(); i++)
	    if (name.endsWith((String)extensions.get(i)))
		return true;
	return false;
    }
}
