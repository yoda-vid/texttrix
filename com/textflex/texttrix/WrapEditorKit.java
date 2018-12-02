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
 * Portions created by the Initial Developer are Copyright (C) 2018
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

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;


/**
 * Editor kit providing wrapped text for long words.
 *
 * Java 7 introduced new behavior for JTextPane where word wrap continued 
 * to work at word boundaries, but words that spanned beyond a single 
 * line increased the width of the view rather than wrapping at the 
 * view's width.
 *
 * This fix was originally derived from a post at:
 * http://java-sl.com/tip_letter_wrap_java7.html
 */
public class WrapEditorKit extends StyledEditorKit {
	ViewFactory defaultFactory = new WrapColumnFactory();
	public ViewFactory getViewFactory() {
		return defaultFactory;
	}
	
	/**
	 * View factory with support for wrapping long words.
	 */
	private class WrapColumnFactory implements ViewFactory {
		public View create(Element elem) {
			String kind = elem.getName();
			if (kind != null) {
				if (kind.equals(AbstractDocument.ContentElementName)) {
					// wrapped text
					return new WrapLabelView(elem);
				} else if (kind.equals(AbstractDocument.ParagraphElementName)) {
					return new ParagraphView(elem);
				} else if (kind.equals(AbstractDocument.SectionElementName)) {
					return new BoxView(elem, View.Y_AXIS);
				} else if (kind.equals(StyleConstants.ComponentElementName)) {
					return new ComponentView(elem);
				} else if (kind.equals(StyleConstants.IconElementName)) {
					return new IconView(elem);
				}
			}
	 		
			// default to regular text
			return new LabelView(elem);
		}
	}
	 
	/**
	 * Label with support for wrapping long words.
	 */
	private class WrapLabelView extends LabelView {
		public WrapLabelView(Element elem) {
			super(elem);
		}
	 
		public float getMinimumSpan(int axis) {
			switch (axis) {
				case View.X_AXIS:
					return 0;
				case View.Y_AXIS:
					return super.getMinimumSpan(axis);
				default:
					throw new IllegalArgumentException(
						"Incorrect axis: " + axis);
			}
		}
	 
	}
}
 