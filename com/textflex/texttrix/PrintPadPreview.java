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
 * Portions created by the Initial Developer are Copyright (C) 2004
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s): David Young <dvd@textflex.com>
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

import javax.swing.*;
import java.awt.*;
import java.awt.print.*;
import java.awt.geom.*;
import java.awt.event.*;

/**Preview dialog window for the <code>PrintPad</code> hard copy printer
 * output.
 * Prior to printing, the preview can be accessed to see if the visual
 * display on the current <code>TextPad</code> is appropriate for printing
 * on the given printer's paper size.
 */
public class PrintPadPreview extends JDialog {
	
	private Book book = null; // the book of multiple pages to preview
	
	/**Constructs a preview dialog to display the given book of 
	 * multiple pages.
	 * 
	 * @param frame the owner, which remains disabled until the preview 
	 * screen is closed to prevent the addition of changes that will print
	 * but not show up on the preview
	 * @param aBook the multiple pages to print
	 * @param printAction the action to print directly from the preview screen
	 */
	public PrintPadPreview(Frame frame, Book aBook, Action printAction) {
		// disables the main Text Trix window, lest the user make changes
		// that will show up in print but not on the preview dialog
		super(frame, "Print Preview", true);
		book = aBook;
		setSize(300, 400);
		//setTitle();
		
		// lays out the dialog contents
		Container contentPane = getContentPane();
		contentPane.setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;
		constraints.anchor = GridBagConstraints.CENTER;
		final PreviewPanel previewer = new PreviewPanel();
		
		// switches to the previous page in the book
		Action prevPageAction = new AbstractAction("Previous page", null) {
			public void actionPerformed(ActionEvent e) {
				previewer.flipPage(-1);
			}
		};
		LibTTx.setAcceleratedAction(
			prevPageAction,
			"Previous Page",
			'P',
			KeyStroke.getKeyStroke("alt P"));
		
		// switches to the next page in the book
		Action nextPageAction = new AbstractAction("Next page", null) {
			public void actionPerformed(ActionEvent e) {
				previewer.flipPage(1);
			}
		};
		LibTTx.setAcceleratedAction(
			nextPageAction,
			"Next Page",
			'N',
			KeyStroke.getKeyStroke("alt N"));
		
		// adds the dialog components
		JButton prevPageButton = new JButton(prevPageAction);
		LibTTx.addGridBagComponent(
			prevPageButton,
			constraints,
			0,
			0,
			1,
			1,
			100,
			0,
			contentPane);

		JButton nextPageButton = new JButton(nextPageAction);
		LibTTx.addGridBagComponent(
			nextPageButton,
			constraints,
			1,
			0,
			1,
			1,
			100,
			0,
			contentPane);
			
		JButton printButton = new JButton(printAction);
		LibTTx.addGridBagComponent(
			printButton,
			constraints,
			2,
			0,
			1,
			1,
			100,
			0,
			contentPane);

		LibTTx.addGridBagComponent(
			previewer,
			constraints,
			0,
			1,
			3,
			1,
			100,
			100,
			contentPane);
	}
	
	/**Creates the panel that previews the potential print output.
	 * 
	 * @author David Young
	 */
	private class PreviewPanel extends JPanel {
		
		private int currPage = 0; // current page, starting at 0
		
		/**Constructs the panel.
		 * 
		 *
		 */
		public PreviewPanel() {
			
		}
		
		/**Paints the preview onto the panel.
		 * 
		 */
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2D = (Graphics2D)g;
			PageFormat pageFormat = book.getPageFormat(currPage);
			
			// offsets
			double xOff = 0;
			double yOff = 0;
			double scale = 1;
			// printer dimensions
			double px = pageFormat.getWidth();
			double py = pageFormat.getHeight();
			// panel dimensions
			double sx = getWidth() - 1; // -1 to give border
			double sy = getHeight() - 1; // -1 to give border
			
			// centering the image on the panel;
			// if true, then printer dimensions less wide relative to height;
			// need to scale to fit height since width already fits
			if (px / py < sx / sy) {
				scale = sy / py;
				xOff = .5 * (sx - scale * px);
				yOff = 0; 
			} else {
				scale = sx / px;
				xOff = 0;
				yOff = .5 * (sy - scale * py);
			}
			
			// shifts the image to fit in the panel's dimensions
			g2D.translate((float)xOff, (float)yOff);
			g2D.scale((float)scale, (float)scale);
			
			// draws the image
			Rectangle2D previewPage = new Rectangle2D.Double(0, 0, px, py);
			g2D.setPaint(Color.white);
			g2D.fill(previewPage);
			g2D.setPaint(Color.black);
			g2D.draw(previewPage);
			Printable printable = book.getPrintable(currPage);
			try {
				printable.print(g2D, pageFormat, currPage);
			} catch(PrinterException e) {
				g2D.draw(new Line2D.Double(0, 0, px, py));
				g2D.draw(new Line2D.Double(0, px, 0, py));
			}
		}
		
		/**Advances or decrements the preview document by the given number of pages.
		 * 
		 * @param by the number of pages to skip
		 */
		public void flipPage(int by) {
			int newPage = currPage + by;
			if (newPage >= 0 && newPage < book.getNumberOfPages()) {
				currPage = newPage;
				repaint();
			}
		}
		
	}

}
