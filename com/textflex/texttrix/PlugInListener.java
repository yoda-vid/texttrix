/* PlugInListener.java    
   Text Trix
   the text tinker
   http://textflex.com/texttrix
   
   Copyright (c) 2002-3, Text Flex
   All rights reserved.
   
   Redistribution and use in source and binary forms, with or without
   modification, are permitted provided that the following conditions 
   are met:
   
   * Redistributions of source code must retain the above copyright
   notice, this list of conditions and the following disclaimer.
   * Redistributions in binary form must reproduce the above copyright
   notice, this list of conditions and the following disclaimer in the
   documentation and/or other materials provided with the distribution.
   * Neither the name of the Text Trix nor the names of its
   contributors may be used to endorse or promote products derived
   from this software without specific prior written permission.
   
   THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
   IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
   TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
   PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
   OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
   EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
   PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
   PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
   LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
   NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
   SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.  
*/

package com.textflex.texttrix;

import java.awt.*;
import javax.swing.event.*;
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








/** The event that <code>PlugIn</code>s create to notify their calling
    class that they await invocation.
*/
class PlugInEvent extends AWTEvent {
    /** An event ID number greater than any of AWT's own events */
    public static final int PLUG_IN_EVENT = AWTEvent.RESERVED_ID_MAX + 5000;

    /** Creates the event and gives it an ID number.
	@param p the <code>PlugIn</code> object creating the event
     */
    public PlugInEvent(PlugIn p) {
	super(p, PLUG_IN_EVENT);
    }
}
