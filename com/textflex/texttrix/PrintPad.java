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

import java.awt.*;
//import java.awt.print.PageFormat;
//import java.awt.print.Printable;
//import java.awt.print.PrinterException;
import java.awt.print.*;
import java.awt.font.*;
import java.awt.geom.*;

/**The printing counterpart of <code>TextPad</code>.
 * <code>TextPad</code> creates <code>PrintPad</code> objects to convert
 * the visual layout of text within the text window to a printed hard
 * copy or a panel image.  As a <code>Printable</code> object, 
 * <code>PrintPad</code> can be called directly from printing methods,
 * such as those in <code>PrinterJob</code>.
 */
public class PrintPad implements Printable {
	
	private PrintPadText[] printText = null; // line-by-line array of text to print
	private int linesPerPage = 0; // max number of lines that fit on current page size
	private Font font = null; // font to print in
	
	/**Constructs the printer counterpart to the current <code>TextPad</code>.
	 * The <code>TextPad</code> often creates the <code>PrintPad</code>,
	 * supplying it with the current text and font.  The text should
	 * be in an array, where each element specifies a given line.
	 * The <code>PrintPad</code> automatically detects how many lines
	 * fit into the current page size by finding an available printer, 
	 * determining its size, and calculating the size of a line of text in
	 * the given font.
	 * @param aPrintText array of text to print, where each element of
	 * the array refers to a separate line
	 * @param aFont the font in which to print
	 */
	public PrintPad(PrintPadText[] aPrintText, Font aFont) {
		printText = aPrintText;
		font = aFont;
	}
	
	/**Prints the text onto a graphics display.
	 * The graphics display could be anything from a printer output
	 * to a <code>JPanel</code>.  The text to print is specified in
	 * either the constructor or <code>setPrintText(String[])</code>
	 * @param g graphics display on which to print; usually provided
	 * by <code>PrinterJob.print(Printable)</code> method call
	 * @param pf page format specification; also usually provided by 
	 * the <code>PrinterJob</code> method
	 * @param page the current page number; the <code>PrinterJob</code>
	 * method may call this <code>PrintPad</code> method multiple
	 * times, which should create complete output for the current page
	 * @throws indicates that an error has occured with the printer
	 * @return an integer representing the status of the print job
	 */
	public int print(Graphics g, PageFormat pf, int page) 
		throws PrinterException {
		Graphics2D g2D = (Graphics2D)g;
		// sets (0,0) as the corner of the printable area
		g2D.translate(pf.getImageableX(), pf.getImageableY());
		// counts the number of pages and stops the printing process once
		// they have been exceeded;
		// TODO: try storing page count in class variable to avoid repetitive calls
		if (page > getPageCount(g2D, pf)) { 
			return Printable.NO_SUCH_PAGE;
		}
		// draws the page on the current graphics display, which can in 
		// turn be displayed on a printer or even a <code>JComponent</code>
		drawPage(g2D, pf, page);
		return Printable.PAGE_EXISTS;
	}
	
	/**Draws the page.
	 * The page will be drawn directly to a <code>Graphics2D</code> object,
	 * which can in turn be drawn onto a printer, a <code>PrintPadPreview</code>
	 * object, or other graphical devices.  The font is specified during
	 * <code>PrintPad</code> construction or via <code>setFont(Font)</code>.
	 * Text is read from <code>PrintPadText</code> array, which contains the text
	 * along with formatting information, such as indentation.  This array can be
	 * set during class creation or through constructor methods.
	 * @param g2D the graphics component on which to draw
	 * @param pf the page format specification
	 * @param page the current page number; only the text for the current
	 * page is drawn
	 */
	public void drawPage(Graphics2D g2D, PageFormat pf, int page) {
		// sets the font specified in the class variable
		g2D.setFont(font);
		
		// creates a clip around the printable area to ensure that printer
		// output doesn't exceed it
		g2D.clip(new Rectangle2D.Double(0, 0, pf.getImageableWidth(), pf.getImageableHeight()));
		
		// the font context for TextLayout
		FontRenderContext fontContext = g2D.getFontRenderContext();
		// the layout to write text, line-by-line
		TextLayout txtLayout = null;
		// writes text line-by-line, advancing the pen position between
		// each line
		float penX = 0; // indent position
		float penY = 0; // line position
		for (int i = page * linesPerPage; i < page * linesPerPage + linesPerPage 
				&& i < printText.length; i++) {
			// creates a new TextLayout object for each line
			txtLayout = new TextLayout(printText[i].getText(), font, fontContext);
			
			// advance the pen;
			// move the pen before even the first writing to ensure that the first
			// line doesn't get cut off
			
			// horizontal adjustment for indentation
			penX = printText[i].getIndent();
			//System.out.println("penX: " + penX);
			
			// vertical advancement for line position
			penY += txtLayout.getAscent() + txtLayout.getDescent() + txtLayout.getLeading();
			txtLayout.draw(g2D, penX, penY);
		}
		
	}
	
	/**Gets the number of pages to write.
	 * Also specifies the maximum number of lines that can fit on a page
	 * of the current printer's paper size.
	 * @param g2D the graphics context, usually specified by 
	 * <code>PrinterJob.print(Printable)</code>
	 * @param pf the page format, usually also specified by the 
	 * <code>PrinterJob</code> method
	 * @return the number of pages to print
	 */
	public int getPageCount(Graphics2D g2D, PageFormat pf) {
		FontRenderContext context = g2D.getFontRenderContext();
		// the line height for the current font and text;
		// reads from the entire body of text, not just for the current
		// page, to ensure uniform line height measurements across pages
		// TODO: check whether to advance pen based on this line height
		// rather than ascent + descent + leading from TextLayout for
		// current line
		String[] lines = new String[printText.length];
		for (int i = 0; i < printText.length; i++) lines[i] = printText[i].getText();
		double lineHeight 
			= font.getLineMetrics(LibTTx.createStringFromArray(lines, 
			false), context).getHeight();
		// determines the maximum number of lines that will fit onto
		// the current paper size 
		linesPerPage = (int)(pf.getImageableHeight() / lineHeight);
		// determines the maximum number of pages for the given 
		// number of lines
		int pp_max = (int)Math.ceil(printText.length * lineHeight
			/ pf.getImageableHeight());
		return pp_max;
	}
	
	/**Gets the current font.
	 * @return the font
	 */
	public Font getFont() {
		return font;
	}
	
	/**Gets the current text to print.
	 * @return the text as a line-by-line array
	 */
	public PrintPadText[] getPrintText() {
		return printText;
	}
	
	/**The font in which to print the text. 
	 * @param aFont the font
	 */
	public void setFont(Font aFont) {
		font = aFont;
	}
	
	/**Sets the line-by-line text to print
	 * @param s an array of the text to print, where each element is
	 * a line of the text
	 */
	public void setPrintText(PrintPadText[] s) {
		printText = s;
	}

}
