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
 * Portions created by the Initial Developer are Copyright (C) 2011
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

import java.lang.InterruptedException;
import java.lang.Runnable;
import java.lang.Thread;
import javax.swing.JProgressBar;
import javax.swing.text.StyledDocument;
import jsyntaxpane.SyntaxDocument;

public class CheckDocLoad implements Runnable {
	private TextPad pad;
	private JProgressBar statusProgress;
	private String status;
	
	public CheckDocLoad(TextPad aPad, JProgressBar aStatusProgress) {
		pad = aPad;
		statusProgress = aStatusProgress;
	}
	
	public void run() {
		try {
			int doneCount = 0;
			StyledDocument styledDoc = pad.getStyledDocument();
			if (!(styledDoc instanceof SyntaxDocument)) return;
			SyntaxDocument doc = (SyntaxDocument)styledDoc;
			int totLines = pad.getTotalLineNumber();
			status = (totLines > 1000)
					? "Loading document..."
					: "Loading large document (this may take awhile)...";
			//statusMsg.setText(status);
			statusProgress.setIndeterminate(true);
			statusProgress.setString(status);
			while (doneCount < 2) {
				if (doc.isParseDone()) {
					doneCount++;
				} else {
					doneCount = 0;
				}
				Thread.sleep(500);
			}
			//statusMsg.setText("");
			statusProgress.setIndeterminate(false);
			statusProgress.setString("");
		} catch(InterruptedException e) {
		}
	}
}
			