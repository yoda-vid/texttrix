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

/**
 * @author davit
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class PrintPad implements Printable {
	private String[] printText = null;
	private int linesPerPage = 0;
	private Font font = null;
		
	public PrintPad(String[] aPrintText, Font aFont) {
		printText = aPrintText;
		font = aFont;
	}
	
	public int print(Graphics g, PageFormat pf, int page) 
		throws PrinterException {
		Graphics2D g2D = (Graphics2D)g;
		g2D.translate(pf.getImageableX(), pf.getImageableY());
//		int pp = getPageCount(g2D, pf);
		if (page > getPageCount(g2D, pf)) { 
		
			return Printable.NO_SUCH_PAGE;
		}
//		if (true) return Printable.NO_SUCH_PAGE;
		drawPage(g2D, pf, page);
//		System.out.println(getAllText());
		return Printable.PAGE_EXISTS;
	}
	
	public void drawPage(Graphics2D g2D, PageFormat pf, int page) {
//		if (getAllText().equals("")) return;
//		Font f = new Font()
		g2D.setFont(font);
		String text 
			= LibTTx.createStringFromArray(printText, 
			page * linesPerPage, linesPerPage, true);

		g2D.clip(new Rectangle2D.Double(0, 0, pf.getImageableWidth(), pf.getImageableHeight()));
		
		FontRenderContext fontContext = g2D.getFontRenderContext();
//		Font f = new Font("Serif", Font.PLAIN, 72);
		TextLayout txtLayout = null;//new TextLayout(text, font, fontContext);
//		AffineTransform affTransform = AffineTransform.getTranslateInstance(0, txtLayout.getAscent());
//		Shape txtOutline = txtLayout.getOutline(affTransform);
//		txtLayout.draw(g2D, 0, 0);
//		g2D.draw(txtOutline);
//		float ascent = txtLayout.getAscent();
//		g2D.drawString(text, 0, ascent);
		float penY = 0;
		for (int i = page * linesPerPage; i < page * linesPerPage + linesPerPage 
				&& i < printText.length; i++) {
			txtLayout = new TextLayout(printText[i], font, fontContext);
			txtLayout.draw(g2D, 0, penY);
			penY += txtLayout.getAscent() + txtLayout.getDescent() + txtLayout.getLeading();
		}

//		g2D.scale(scale, scale);
		/*
		g2D.drawString(text, 
			(float)pf.getImageableWidth(), 
			(float)(pf.getImageableHeight()));
		*/
		System.out.println("page: " + page + ", text: " + text);
//		System.out.println("pf width: " + pf.getImageableWidth() + ", pf height: " + pf.getImageableHeight());
//		System.out.println("font height: ")
//		paintComponent(g2D);
		
	}
	
	public int getPageCount(Graphics2D g2D, PageFormat pf) {
//		int lines = LibTTx.getVisibleLineCount(this);
		FontRenderContext context = g2D.getFontRenderContext();
		double lineHeight = font.getLineMetrics(LibTTx.createStringFromArray(printText, false), context).getHeight()
			* 1.15;
		linesPerPage = (int)(pf.getImageableHeight() / lineHeight);
//		if (printText == null) {
//			printText = LibTTx.getVisibleLines(this);
//		}
//		int pp_max = (int)(lines * g2D.getFontMetrics().getHeight() 
//			/ pf.getImageableHeight());
		int pp_max = (int)Math.ceil(printText.length * lineHeight
			/ pf.getImageableHeight());
		System.out.println(
//			"lines: " + lines 
			"font height: "	+ font.getLineMetrics(LibTTx.createStringFromArray(printText, false), context).getHeight()
			+ ", linesPerPage: " + linesPerPage
			+ ", pp_max: " + pp_max
//			+ ", fontMetrics height: " + g2D.getFontMetrics().getHeight()
//			+ ", font size: " + getFont().getSize2D()
//			+ ", pf.imageableHeight: " + pf.getImageableHeight()
//			+ ", rec Height: " + getFont().getStringBounds(getAllText(), context).getHeight()
			);
		return pp_max;
		/*
		Rectangle rec = g2D.getClipBounds(new Rectangle());
		int pp = (int)Math.ceil(rec.getHeight() / pf.getImageableHeight());
		System.out.println("width: " + rec.getWidth() + ", height: " + rec.getHeight() + ", pp: " + pp);
		return pp;
		/*
		if (getAllText().equals("")) return 0;
		FontRenderContext context = g2D.getFontRenderContext();
		Rectangle2D bounds = getFont().getStringBounds(getAllText(), context);
		if (bounds.getWidth() > pf.getImageableWidth()) {
			scale = pf.getImageableWidth() / bounds.getWidth();
		}
		double width = scale * bounds.getWidth();
		int pp = (int)Math.ceil(scale * bounds.getHeight() / pf.getImageableHeight());
		return pp;
		*/
	}
	
	public void setPrintText(String[] s) {
		printText = s;
	}

}
