/*Text Trix
 *a goofy gui editor
 *v.0.1.1
 *Copyright (C) 2002 David Young

 *This program is free software; you can redistribute it and/or
 *modify it under the terms of the GNU General Public License
 *as published by the Free Software Foundation; either version 2
 *of the License, or (at your option) any later version.

 *This program is distributed in the hope that it will be useful,
 *but WITHOUT ANY WARRANTY; without even the implied warranty of
 *MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *GNU General Public License for more details.

 *You should have received a copy of the GNU General Public License
 *along with this program; if not, write to the Free Software
 *Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

 */

package net.sourceforge.texttrix;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.awt.event.*;

/** The main <code>Edword</code> class.  Sets up the window
    and performs the text manipulation.
*/
public class TextTrix extends JFrame {
    JTextArea textArea = new JTextArea(30, 20);
    JScrollPane scrollPane = new JScrollPane(textArea);
    JButton stripReturns = new JButton("Remove Extra Hard Returns");
    JMenuBar menuBar = new JMenuBar();
    JMenu fileMenu = new JMenu("File");
    JMenu editMenu = new JMenu("Edit");
    JMenu helpMenu = new JMenu("Help");

    public TextTrix() {
	setTitle("Text Trix");
	setSize(500, 600);
	setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	Action exitAction = new
	    AbstractAction("Exit") {
		public void actionPerformed(ActionEvent evt) {
		    System.exit(0);
		}
	    };
	JMenuItem exitItem = fileMenu.add(exitAction);

	textArea.setLineWrap(true);
	textArea.setWrapStyleWord(true);

	stripReturns.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent evt) {
		    textArea.setText(stripExtraHardReturns(textArea.getText()));
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
	add(stripReturns, constraints, 0, 0, 1, 1, 100, 100);

	constraints.fill = GridBagConstraints.BOTH;
	constraints.anchor = GridBagConstraints.CENTER;
	add(scrollPane, constraints, 0, 1, 1, 1, 100, 100);

    }

    public static void main(String[] args) {
	TextTrix textTrix = new TextTrix();
	textTrix.show();
    }

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

    /** Strips inserted, extra hard returns.  For example,
	unformatted email arrives with hard returns
	inserted after every line; this method
	strips all but the paragraph, double-spaced
	hard returns.  Text within <code>&#60pre&#62</code>
	and <code>&#60/pre&#62</code> tags are
	left untouched.
	@param s the full text from which to strip
	extra hard returns
	@return stripped text
    */
    public String stripExtraHardReturns(String s) {
	String stripped = "";

	while (!s.equals("")) {
	    int singleReturn = s.indexOf("\n");
	    int doubleReturn = s.indexOf("\n\n");
	    int dash = s.indexOf("-");
	    int asterisk = s.indexOf("*");
	    int startPre = s.indexOf("<pre>");
	    int endPre = s.indexOf("</pre>");

	    // don't catch internal dashes
	    while (dash != -1 && dash < singleReturn) {
		dash = s.indexOf("-", dash + 1);
	    }
	    while (asterisk != -1 && asterisk < singleReturn) {
		asterisk = s.indexOf("*", asterisk + 1);
	    }

	    if (startPre != -1 && startPre < singleReturn) {
		if (endPre != -1) {
		    stripped = stripped 
			+ s.substring(0, endPre + 6);
		    s = s.substring(endPre + 6);
		} else {
		    // if user forgets closing pre tag, goes to end
		    stripped = stripped + s;
		    s = "";
		}
	    } else if (singleReturn == -1) {
		stripped = stripped + s;
		s = "";
	    } else if (singleReturn == doubleReturn) {
		stripped = stripped 
		    + s.substring(0, doubleReturn + 2);
		s = s.substring(doubleReturn +2);
	    } else if (dash == singleReturn + 1
		       || asterisk == singleReturn + 1) {
		stripped = stripped 
		    + s.substring(0, singleReturn + 2);
		s = s.substring(singleReturn + 2);
	    } else {
		stripped = stripped 
		    + s.substring(0, singleReturn) + " ";
		s = s.substring(singleReturn + 1);
	    }
	}
	return stripped;
    }
}
