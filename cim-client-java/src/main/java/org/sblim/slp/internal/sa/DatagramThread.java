/**
 * DatagramThread.java
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
 * 1892103    2008-02-13  ebak         SLP improvements
 * 2807325    2009-06-22  blaschke-oss Change licensing from CPL to EPL
 */


package org.sblim.slp.internal.sa;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;

import org.sblim.slp.internal.SLPConfig;
import org.sblim.slp.internal.SLPDefaults;
import org.sblim.slp.internal.TRC;

/**
 * DatagramThread
 *
 */
public class DatagramThread extends RecieverThread {

	private static MulticastSocket cMCastSocket;
	
	private DatagramPacket iPacket = new DatagramPacket(
		new byte[SLPDefaults.MTU], SLPDefaults.MTU
	);

	/**
	 * Ctor.
	 * @param pSrvAgent
	 */
	public DatagramThread(ServiceAgent pSrvAgent) {
		super("DatagramThread", pSrvAgent);
	}
	
	/**
	 * joinGroup
	 * @param pGroup
	 * @throws IOException
	 */
	public synchronized void joinGroup(InetAddress pGroup) throws IOException {
		TRC.debug("join:"+pGroup);
		cMCastSocket.joinGroup(pGroup);
	}
	
	/**
	 * leaveGroup
	 * @param pGroup
	 * @throws IOException
	 */
	public synchronized void leaveGroup(InetAddress pGroup) throws IOException {
		TRC.debug("leave:"+pGroup);
		cMCastSocket.leaveGroup(pGroup);
	}

	protected void init() throws IOException {
		if (cMCastSocket == null) {
			cMCastSocket =  new MulticastSocket(SLPConfig.getGlobalCfg().getPort());
			cMCastSocket.setReuseAddress(true);
			cMCastSocket.setSoTimeout(100);
		}
	}
	
	protected void mainLoop() throws IOException {
		try {
			cMCastSocket.receive(iPacket);
			TRC.debug("Packet received");
			iSrvAgent.processMessage(cMCastSocket, iPacket);
		} catch (SocketTimeoutException e) {
			// superclass will restart this function
		}
	}

	protected void close() {
		if (cMCastSocket == null) return;
		cMCastSocket.close();
		cMCastSocket =  null;
	}
	
}