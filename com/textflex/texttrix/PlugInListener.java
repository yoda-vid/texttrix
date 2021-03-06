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
 * Portions created by the Initial Developer are Copyright (C) 2003
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

import java.util.*;

/** Listener to <code>PlugIn</code> events.
    Plug-ins need listeners to communicate to the object that uses
    the plug-in.  For example, <code>TextTrix</code> loads plug-ins,
    but, the user may interact directly with the plug-in rather than with 
    <code>TextTrix</code>.  The plug-in needs a way for to notify
    <code>TextTrix</code> that the user wishes to apply the plug-in
    on <code>TextTrix</code>'s text.

    <p>This listener responds to these events.  <code>TextTrix</code>
    needs to create a listener for each plug-in and add it to the plug-in's
    list of listeners to notify.  When the user chooses a "Run" button
    on a plug-in options panel, for example, the plug-in will notify
    <code>TextTrix</code> to use the plug-in.
*/
public interface PlugInListener extends EventListener {

    /** Invokes the plug-in's primary action.
	@param evt the event that tipped off the listener
     */
    public void runPlugIn(PlugInEvent evt);
}








