/**
 * DatagramRequester.java
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
 * 1804402    2007-11-10  ebak         IPv6 ready SLP - revision 4
 * 1892103    2008-02-12  ebak         SLP improvements
 * 1913348    2008-04-08  raman_arora  Malformed service URL crashes SLP discovery
 * 1901290    2008-04-24  rgummada 	   SLP error: "java.io.IOException" on Linux and IPv6
 * 2807325    2009-06-22  blaschke-oss Change licensing from CPL to EPL
 */


package org.sblim.slp.internal.ua;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.sblim.slp.ServiceLocationException;
import org.sblim.slp.internal.IPv6MulticastAddressFactory;
import org.sblim.slp.internal.Net;
import org.sblim.slp.internal.SLPConfig;
import org.sblim.slp.internal.SLPDefaults;
import org.sblim.slp.internal.TRC;
import org.sblim.slp.internal.msg.MsgFactory;
import org.sblim.slp.internal.msg.ReplyMessage;
import org.sblim.slp.internal.msg.RequestMessage;


/**
 * DatagramRequester
 *
 */
public class DatagramRequester implements Runnable {

	RequestMessage iReqMsg;
	
	private Thread iThread;
	
	ResultTable iResTable;
	
	private InetAddress iDst0, iDst1;
	
	private DatagramSocket iDGramSocket;
	
	private int iPort = SLPConfig.getGlobalCfg().getPort();
	
	private boolean
		iUseV4 = Net.hasIPv4() && SLPConfig.getGlobalCfg().useIPv4(),
		iUseV6 = Net.hasIPv6() && SLPConfig.getGlobalCfg().useIPv6();
	
	private List iTCPRequesters;
	
	int iTotalTimeOut;
	int[] iTimeOuts;
	int iMaxResults = SLPConfig.getGlobalCfg().getMaximumResults();
	
	/*
	 * this size is used for the receiver instead of the configurable MTU size
	 */
	private static final int MAX_DATAGRAM_SIZE = 65536;
	private final byte[] iInBuf = new byte[MAX_DATAGRAM_SIZE];
	
	
	/**
	 * Constructor used for unicast requestes
	 * @param pRqstMsg
	 * @param pResTable
	 * @param pDst
	 * @throws IOException 
	 */
	public DatagramRequester(
		RequestMessage pRqstMsg, ResultTable pResTable, InetAddress pDst
	) throws IOException {
		iReqMsg = pRqstMsg; iResTable = pResTable; iDst0 = pDst;
		iTimeOuts = SLPConfig.getGlobalCfg().getDatagramTimeouts();
		iDGramSocket = new DatagramSocket();
	}
	
	/**
	 * Constructor used for multicast requestes
	 * @param pRqstMsg
	 * @param pResTable
	 * @throws IOException 
	 */
	public DatagramRequester(
		RequestMessage pRqstMsg, ResultTable pResTable
	) throws IOException {
		iReqMsg = pRqstMsg; iResTable = pResTable;
		iTimeOuts =  SLPConfig.getGlobalCfg().getMulticastTimeouts();
		iTotalTimeOut = SLPConfig.getGlobalCfg().getMulticastMaximumWait();
		MulticastSocket mcastSocket = new MulticastSocket();
		iDGramSocket = mcastSocket;
		if (iUseV6) {
			iDst0 = IPv6MulticastAddressFactory.get(
				SLPDefaults.IPV6_MULTICAST_SCOPE, pRqstMsg
			);
			mcastSocket.joinGroup(iDst0);
		}
		if (iUseV4) {
			iDst1 = SLPConfig.getMulticastAddress();
			mcastSocket.joinGroup(iDst1);
		}
	}
	
	/**
	 * start
	 * @param pAsThread
	 */
	public void start(boolean pAsThread) {
		iResTable.registerRequester(this);
		if (pAsThread) {
			iThread = new Thread(this); iThread.start();
		} else {
			iThread = null;
			run();
		}
	}
	
	/**
	 * For diagnostic only.
	 * @return int
	 */
	public int getPort() {
		return iDGramSocket == null ? -1 : iDGramSocket.getLocalPort();
	}
	
	/**
	 * waitFor
	 */
	public void waitFor() {
		if (iThread == null) return;
		try {
			iThread.join();
		} catch (InterruptedException e) {
			TRC.error(e);
		}
	}
	
	public void run() {
		try {
			if (iDGramSocket instanceof MulticastSocket) {
				mcastNegotiate();
			} else {
				ucastNegotiate();
			}
		} catch (Exception e) {
			iResTable.addException(e);
			TRC.error(e.getMessage(), e);
		} finally {
			iDGramSocket.close();
			iResTable.unregisterRequester(this);
		}
	}
	
	class MCastLoopController {
		
		private long iStartTime = getMillis();
		private int iTimeOutIdx = 0;
		
		/**
		 * getTimeOut
		 * @return int
		 */
		public int getTimeOut() {
			return iTimeOuts[iTimeOutIdx];
		}
		
		private boolean hasNextTimeOut() {
			return iTimeOutIdx < iTimeOuts.length;
		}
		
		/**
		 * nextTimeOut
		 */
		public void nextTimeOut() {
			if (hasNextTimeOut()) ++iTimeOutIdx;
		}
		
		/**
		 * hasNext
		 * @return boolean
		 */
		public boolean hasNext() {
			return
				iResTable.getTotalResponses() < iMaxResults && 
				getMillis()-iStartTime < iTotalTimeOut &&
				hasNextTimeOut();
		}
		
	}
	
	private void mcastNegotiate() throws Exception {
		byte[] reqBytes = iReqMsg.serialize(true, true, false);
		DatagramPacket outPacket0 =
			iDst0 == null ? null : 
			new DatagramPacket(reqBytes, reqBytes.length, iDst0, iPort);
		DatagramPacket outPacket1 =
			iDst1 == null ? null :
			new DatagramPacket(reqBytes, reqBytes.length, iDst1, iPort);
		DatagramPacket inPacket = new DatagramPacket(iInBuf, iInBuf.length);
		MCastLoopController ctrl = new MCastLoopController();
		boolean respondersUpdated = false;
		ResponseCache rspCache = new ResponseCache();
		sendLoop: while (ctrl.hasNext()) {
			if (respondersUpdated) {
				byte[] msg = iReqMsg.serialize(true, true, true);
				if (outPacket0 != null) outPacket0.setData(msg);
				if (outPacket1 != null) outPacket1.setData(msg);
				respondersUpdated = false;
			}
			// send it relative frequently in order to avoid lost packets
			TRC.debug("sending: "+iReqMsg);
			
			try {
				if (outPacket0 != null) iDGramSocket.send(outPacket0);
			} catch (IOException ioe) {
				//some back level kernels can't handle sending IPv6 mcast
			   TRC.warning("IOException caught during send, disabling IPv6: "+ ioe.getMessage(), ioe);
				outPacket0 = null; iDst0 = null;
			}
		 
			if (outPacket1 != null) iDGramSocket.send(outPacket1);
			
			while (ctrl.hasNext()) {
				iDGramSocket.setSoTimeout(ctrl.getTimeOut());
				try {
					iDGramSocket.receive(inPacket);
				} catch (SocketTimeoutException e) {
					// set new timeout value
					TRC.debug("receive timed out");
					ctrl.nextTimeOut();
					continue sendLoop;
				}
				InetAddress responderAddress = inPacket.getAddress();
				
				respondersUpdated = iReqMsg.updatePrevResponders(
					responderAddress.toString()
				);
				
				if (rspCache.contains(inPacket)) {
					TRC.debug("received packet is found in rspCache");
					continue; // packet data is already processed
				}
				ReplyMessage replyMsg = handleResponse(inPacket);
				
				if (replyMsg != null) {
					if (replyMsg.overflows()) {
						if (!isLinkLocal(responderAddress)) {
							// TCP doesn't seem to be working on linkLocal IPv6 connection
							addTCPRequester(responderAddress);
							rspCache.add(inPacket);
						}
					} else {
						rspCache.add(inPacket);
					}
				}
			}
			break;
		}
		waitForTCPRequesters();
	}
	
	private void ucastNegotiate() throws Exception {
		byte[] reqBytes = iReqMsg.serialize(false, true, false);
		DatagramPacket outPacket =
			new DatagramPacket(reqBytes, reqBytes.length, iDst0, iPort);
		DatagramPacket inPacket = new DatagramPacket(iInBuf, iInBuf.length);
		int timeOutIdx = 0;
		int timeOut = iTimeOuts[timeOutIdx];
		/*
		 * ad-hoc solution to avoid endless loop in case of broken answers
		 */
		int tries = 10;
		while (timeOutIdx < iTimeOuts.length && tries > 0) {
			// send it relative frequently in order to avoid lost packets
			TRC.debug("sending: "+iReqMsg);
			iDGramSocket.send(outPacket);
			iDGramSocket.setSoTimeout(timeOut);
			try {
				iDGramSocket.receive(inPacket);
			} catch (SocketTimeoutException e) {
				// set new timeout value
				TRC.debug("receive timed out");
				timeOut = iTimeOuts[timeOutIdx++];
				continue;
			}
			InetAddress responderAddress = inPacket.getAddress();
			ReplyMessage replyMsg = handleResponse(inPacket);
			if (replyMsg == null) { --tries; continue; }
			if (replyMsg.overflows()) {
				TCPRequester tcpRequester = new TCPRequester(
					iResTable, responderAddress, iReqMsg, true
				);
				tcpRequester.waitFor();
			}
			/*
			 * one answer is wanted from one host
			 */
			break;
		}
	}
	
	/**
	 * Tries to parse the content of the packet as a ReplyMessage.
	 * If parsing is successful ReplyMessage is placed into the ResultTable,
	 * otherwise the Exception is placed there.
	 * @param pPacket
	 * @return the ReplyMessage or null in case of 
	 * 
	 * Add all invalid URL exceptions thrown by parser into exception table
	 */
	private ReplyMessage handleResponse(DatagramPacket pPacket) {
		ReplyMessage replyMsg;
		try {
			replyMsg = (ReplyMessage)MsgFactory.parse(pPacket);
			TRC.debug("expected: "+iReqMsg.getXID()+", received: "+replyMsg);
			if (
				iReqMsg.getXID() != replyMsg.getXID() ||
				!iReqMsg.isAllowedResponseType(replyMsg)
			) {
				TRC.debug("expected: "+iReqMsg.getXID()+", ignoring: "+replyMsg);
				return null;
			}
		} catch (Exception e) {
			iResTable.addException(e);
			return null;
		}
		TRC.debug("resTable <- "+replyMsg);
		iResTable.addResults(replyMsg);
		iResTable.addExceptions(replyMsg);		
		return replyMsg;
	}
	
	private static boolean isLinkLocal(InetAddress pAddr) {
		if (pAddr instanceof Inet6Address) {
			Inet6Address dest6 = (Inet6Address)pAddr;
			// TCP on linkLocal is evil
			return dest6.isLinkLocalAddress();
		}
		return false;
	}
	
	static long getMillis() {
		return new Date().getTime();
	}
	
	private void addTCPRequester(InetAddress pDest) throws ServiceLocationException {
		if (iTCPRequesters == null) iTCPRequesters = new ArrayList();
		iTCPRequesters.add(
			new TCPRequester(
				iResTable, pDest, iReqMsg, true
			)
		);
	}
	
	private void waitForTCPRequesters() {
		if (iTCPRequesters == null) return;
		Iterator itr = iTCPRequesters.iterator();
		while (itr.hasNext()) ((TCPRequester)itr.next()).waitFor();
		iTCPRequesters.clear();
	}
	
}