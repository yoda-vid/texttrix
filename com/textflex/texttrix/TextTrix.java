/*Text Trix
 *a goofy gui editor

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
    static ArrayList textAreas = new ArrayList();
    static JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
    static JFileChooser chooser = new JFileChooser();
	static String openPath = "";
	static String savePath = "";
	int fileIndex = 0;

    public TextTrix() {
	setTitle("Text Trix");
	setSize(500, 600);
//	setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	addTextArea(textAreas, tabbedPane, makeNewFile());

	JMenuBar menuBar = new JMenuBar();
	JMenu fileMenu = new JMenu("File");
	JMenu editMenu = new JMenu("Edit");
	JMenu helpMenu = new JMenu("Help");

	/* File menu items */
	JMenuItem newItem = fileMenu.add(new AbstractAction("New") {
		public void actionPerformed(ActionEvent evt) {
		    addTextArea(textAreas, tabbedPane, makeNewFile());
		}
	    });
	// Mozilla tabbed-pane key-binding
	newItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T,
						      InputEvent.CTRL_MASK));
	/* move to tabs using default Java key-bindings:
	 * Ctrl-up to tabs
	 * Lt, rt to switch tabs
	 * Tab back down to TextPad */

	JMenuItem openItem = new JMenuItem("Open...");
	fileMenu.add(openItem);
	openItem.addActionListener(new FileOpenListener());
	openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
						       InputEvent.CTRL_MASK));

	JMenuItem closeItem = fileMenu.add(new AbstractAction("Close") {
		public void actionPerformed(ActionEvent evt) {
			int i = tabbedPane.getSelectedIndex();
			closeTextArea(i, textAreas, tabbedPane);
				//JOptionPane op = new JOptionPane();
				//op.showMessageDialog(op, s);
		}
	});

	JMenuItem saveItem = fileMenu.add(new AbstractAction("Save") {
		public void actionPerformed(ActionEvent evt) {
			TextPad t = (TextPad)textAreas
				.get(tabbedPane.getSelectedIndex());
			if (t.fileExists())
				saveFile(t.getPath());
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
			exitTextTrix();
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

	JMenuItem shortcutsItem = helpMenu.add(new AbstractAction("Shortcuts") {
		public void actionPerformed(ActionEvent evt) {
			String path = "shortcuts.txt";
			displayFile(path);
		}
	});

	JMenuItem licenseItem = helpMenu.add(new AbstractAction("License") {
		public void actionPerformed(ActionEvent evt) {
			String path = "license.txt";
			displayFile(path);
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
		textTrix.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				exitTextTrix();
			}
		});
		textTrix.show();
    }

	public static String getOpenPath() {
		return openPath;
	}

	public static String getSavePath() {
		return savePath;
	}

	public static void setOpenPath(String anOpenPath) {
		openPath = anOpenPath;
	}

	public static void setSavePath(String aSavePath) {
		savePath = aSavePath;
	}

	// okay to return mutable object b/c create it anew here?
	public File makeNewFile() {
		File file;
		do {
			fileIndex++;
		} while ((file = new File("NewFile" + fileIndex + ".txt")).exists());
		return file;
	}
	
	public void displayFile(String path) {
		try {
			BufferedReader reader = new BufferedReader(new
				InputStreamReader(TextTrix.class.
					getResourceAsStream(path)));
			String license = readText(reader);
			reader.close();
			// TODO: need to find true path
			addTextArea(textAreas, tabbedPane, new File(path));
			TextPad t = (TextPad)textAreas.get(tabbedPane.getSelectedIndex());
			t.setEditable(false);
			t.setText(license);
			t.setCaretPosition(0);
			t.setChanged(false);
		} catch(IOException exception) {
			exception.printStackTrace();
		}
	}
	
	public static void exitTextTrix() {
		boolean b = true;
		int i = tabbedPane.getTabCount();
		while (i > 0 && b) {
			b = closeTextArea(i - 1, textAreas, tabbedPane);
			i = tabbedPane.getTabCount();
		}
		if (b == true)
			System.exit(0);
	}
	
	public static boolean closeTextArea(int tabIndex, ArrayList textAreas,
			JTabbedPane tabbedPane) {
		boolean successfulClose = false;
		
		TextPad t = (TextPad)textAreas.get(tabIndex);
		if (t.getChanged()) {
			String s = "Please save first.";
			tabbedPane.setSelectedIndex(tabIndex);
			int choice = JOptionPane.showOptionDialog(					
				null,
				"This file has not yet been saved.\nWhat would you like me to do with it?",
				"Save before close",
				JOptionPane.WARNING_MESSAGE,
				JOptionPane.DEFAULT_OPTION,
				null,				
				new String[] { "Save", "Toss away", "Cancel" },
				"Save");
			switch (choice) {
				case 0:
					if (t.fileExists()) {
						saveFile(t.getPath());
					} else {
						fileSaveDialog();
					}
				case 1:
					removeTextArea(tabIndex, textAreas, tabbedPane);
					successfulClose = true;
					break;
				case 2:
					successfulClose = false;
					break;
				default:
					successfulClose = false;
					break;
			}
		} else {
			removeTextArea(tabIndex, textAreas, tabbedPane);
			successfulClose = true;
		}
		return successfulClose;
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
			final JTabbedPane tabbedPane, File file) {
//		try {
			final TextPad textPad = new TextPad(30, 20, file);
			JScrollPane scrollPane = new JScrollPane(textPad);

			final int i = tabbedPane.getTabCount(); // don't subtract 1 b/c addTab next
			tabbedPane.addTab(file.getName(), scrollPane);
			textPad.setLineWrap(true);
			textPad.setWrapStyleWord(true);
			textPad.addKeyListener(new KeyAdapter() {
				public void keyTyped(KeyEvent e) {
					if (textPad.getChanged()) {
						String title = tabbedPane.getTitleAt(i);
						if (!title.endsWith(" *"))
							tabbedPane.setTitleAt(i, title + " *");
					}
				}
			});
			arrayList.add(textPad);
			tabbedPane.setSelectedIndex(i);
/*		} catch(IOException e) {
			e.printStackTrace();
		}
		*/
	}
	
	public static void removeTextArea(int i, ArrayList l, JTabbedPane tp) {
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

	public static void saveFile(String path) {
		try {
			TextPad t = (TextPad)textAreas
				.get(tabbedPane.getSelectedIndex());
			PrintWriter out = new 
			PrintWriter(new FileWriter(path), true);
			out.print(t.getText());
		    out.close();
			t.setChanged(false);
			t.setFile(path);
			tabbedPane.setTitleAt(tabbedPane.getSelectedIndex(), 
					chooser.getSelectedFile().getName());
		} catch(IOException exception) {
			exception.printStackTrace();
		}
	}

	public static void fileSaveDialog() {
		chooser.setCurrentDirectory(new File(savePath));
	   	final ExtensionFileFilter filter = new ExtensionFileFilter();
	   	filter.addExtension("txt");
	   	filter.setDescription("Text files (*.txt)");
	   	chooser.setFileFilter(filter);

    	int result = chooser.showSaveDialog(null);
    	if (result == JFileChooser.APPROVE_OPTION) {
			String path = chooser.getSelectedFile().getPath();
			saveFile(path);
			setSavePath(path);
			tabbedPane.setTitleAt(tabbedPane.getSelectedIndex(), 
					chooser.getSelectedFile().getName());
	    }
	}

    private class FileOpenListener implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
	    	chooser.setCurrentDirectory(new File(openPath));
		    final ExtensionFileFilter filter = new ExtensionFileFilter();
		    filter.addExtension("txt");
	    	filter.setDescription("Text files (*.txt)");
	    	chooser.setFileFilter(filter);

	    	int result = chooser.showOpenDialog(null);

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
						addTextArea(textAreas, tabbedPane, new File(path));
						t = (TextPad)textAreas
							.get(tabbedPane.getSelectedIndex());
						t.setText(text);
		    		} else {
						t.setText(text);
		    		}
					t.setCaretPosition(0);
					t.setChanged(false);
					t.setFile(path);
					tabbedPane.setTitleAt(tabbedPane.getSelectedIndex(), 
							chooser.getSelectedFile().getName());
	
			    	reader.close();
					setOpenPath(path);
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
