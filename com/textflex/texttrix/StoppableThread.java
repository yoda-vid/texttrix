/*
 * Created on Mar 10, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.textflex.texttrix;

/**
 * @author davit
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public abstract class StoppableThread extends Thread {
	
	private boolean stopped = false;
	
	public boolean isStopped() {
		return stopped;
	}
	
	public void setStopped(boolean aStopped) {
		stopped = aStopped;
	}
	
	public abstract void requestStop();

}
