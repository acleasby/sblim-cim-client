/**
 * TCPThread.java
 * 
 * (C) Copyright IBM Corp. 2007, 2009
 *
 * THIS FILE IS PROVIDED UNDER THE TERMS OF THE ECLIPSE PUBLIC LICENSE 
 * ("AGREEMENT"). ANY USE, REPRODUCTION OR DISTRIBUTION OF THIS FILE 
 * CONSTITUTES RECIPIENTS ACCEPTANCE OF THE AGREEMENT.
 *
 * You can obtain a current copy of the Eclipse Public License from
 * http://www.opensource.org/licenses/eclipse-1.0.php
 *
 * @author: Endre Bak, IBM, ebak@de.ibm.com
 * 
 * Change History
 * Flag       Date        Prog         Description
 *------------------------------------------------------------------------------- 
 * 1804402    2007-09-28  ebak         IPv6 ready SLP
 * 2807325    2009-06-22  blaschke-oss Change licensing from CPL to EPL
 */


package org.sblim.slp.internal.sa;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

import org.sblim.slp.internal.SLPConfig;
import org.sblim.slp.internal.TRC;

/**
 * TCPThread 
 *
 */
public class TCPThread extends RecieverThread {

	private ServerSocket iListenerSocket;
	
	/**
	 * Ctor.
	 * @param pSrvAgent
	 */
	public TCPThread(ServiceAgent pSrvAgent) {
		super("TCP reciever", pSrvAgent);
	}

	protected void init() throws IOException {
		iListenerSocket = new ServerSocket(SLPConfig.getGlobalCfg().getPort());
		iListenerSocket.setReuseAddress(true);
		iListenerSocket.setSoTimeout(100);
	}

	protected void mainLoop() throws IOException {
		try {
			new ConnectionThread(iListenerSocket.accept());
		} catch (SocketTimeoutException e) {
			// superclass will execute the mainLoop again
		}
	}
	
	private class ConnectionThread implements Runnable {

		private Socket iSock;
		
		/**
		 * Ctor.
		 * @param pSock
		 */
		public ConnectionThread(Socket pSock) {
			iSock = pSock;
			new Thread(this).start();
		}
		
		public void run() {
			iSrvAgent.processMessage(iSock);
		}
		
		
	}

	protected void close() {
		if (iListenerSocket == null) return;
		try {
			iListenerSocket.close();
		} catch (IOException e) {
			TRC.error(e);
		}
	}
	
}