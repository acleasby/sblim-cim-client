/**
 * AdvertiserImpl.java
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
 * 1892103    2008-02-15  ebak         SLP improvements
 * 2807325    2009-06-22  blaschke-oss Change licensing from CPL to EPL
 */


package org.sblim.slp.internal;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import org.sblim.slp.Advertiser;
import org.sblim.slp.ServiceLocationException;
import org.sblim.slp.ServiceURL;
import org.sblim.slp.internal.msg.MsgFactory;
import org.sblim.slp.internal.msg.SLPMessage;
import org.sblim.slp.internal.msg.ServiceAcknowledgment;
import org.sblim.slp.internal.msg.ServiceDeregistration;
import org.sblim.slp.internal.msg.ServiceRegistration;
import org.sblim.slp.internal.msg.Util;




/**
 * AdvertiserImpl
 *
 */
public class AdvertiserImpl implements Advertiser {

	private Locale iLocale;
	private String iLangTag;
	private List iDefScopeList;
	
	private final int iMaxDatagramSize = SLPConfig.getGlobalCfg().getMTU();
	private final byte[] iInBuf = new byte[iMaxDatagramSize];
	
	private final int[] iTimeOuts = SLPConfig.getGlobalCfg().getDatagramTimeouts();
	
	private boolean
		iUseV6 = Net.hasIPv6() && SLPConfig.getGlobalCfg().useIPv6();
	
	/**
	 * Ctor.
	 * @param pLocale
	 */
	public AdvertiserImpl(Locale pLocale) {
		iLocale = pLocale;
		iLangTag = Util.getLangTag(iLocale);
		iDefScopeList = new ArrayList();
		iDefScopeList.add(SLPDefaults.DEFAULT_SCOPE);
	}
	
	public void addAttributes(ServiceURL pURL, Vector pAttributes) throws ServiceLocationException {
		throw new ServiceLocationException(ServiceLocationException.NOT_IMPLEMENTED);
	}

	public void deleteAttributes(ServiceURL pURL, Vector pAttributeIds) throws ServiceLocationException {
		throw new ServiceLocationException(ServiceLocationException.NOT_IMPLEMENTED);
	}

	public void deregister(ServiceURL pURL) throws ServiceLocationException {
		sendMessage(
			new ServiceDeregistration(
				iLangTag, iDefScopeList, pURL, null
			)
		);
	}

	public Locale getLocale() {
		return iLocale;
	}

	public void register(ServiceURL pURL, Vector pAttributes) throws ServiceLocationException {
		sendMessage(
			new ServiceRegistration(
				iLangTag, pURL, iDefScopeList, pAttributes, null
			)
		);
		
	}
	
	private void sendMessage(SLPMessage pMsg) throws ServiceLocationException {
		try {
			int res = unicast(pMsg);
			if (res != ServiceLocationException.OK)
				throw new ServiceLocationException((short)res);
			TRC.debug("service registered");
		} catch (IOException e) {
			throw new ServiceLocationException(ServiceLocationException.NETWORK_ERROR, e);
		}
	}
	
	private int unicast(SLPMessage pMsg) throws ServiceLocationException, IOException {
		DatagramSocket dgSocket = new DatagramSocket();
		try {
			byte[] reqBytes = pMsg.serialize(false, true, false);
			InetAddress loopback = iUseV6 ?
					SLPConfig.getLoopbackV6() : SLPConfig.getLoopbackV4();
			TRC.debug("loopback:"+loopback);
			
			DatagramPacket outPacket = new DatagramPacket(
				reqBytes, reqBytes.length, loopback, SLPConfig.getGlobalCfg().getPort()
			);
			DatagramPacket inPacket = new DatagramPacket(iInBuf, iInBuf.length);
			
			int timeOutIdx = 0;
			while (timeOutIdx < iTimeOuts.length) {
				TRC.debug("sending : "+pMsg);
				dgSocket.send(outPacket);
				dgSocket.setSoTimeout(iTimeOuts[timeOutIdx++]);
				try {
					dgSocket.receive(inPacket);
				} catch (SocketTimeoutException e) {
					TRC.debug("receive time out");
					continue;
				}
				SLPMessage msg = MsgFactory.parse(inPacket);
				TRC.debug("expected: "+pMsg.getXID()+", received : "+msg);
				if (msg instanceof ServiceAcknowledgment) {
					ServiceAcknowledgment ack = (ServiceAcknowledgment)msg;
					if (ack.getXID() == pMsg.getXID()) return ack.getErrorCode();
				}
				TRC.debug("ignoring :" + msg);
			}
		} finally {
			dgSocket.close();
		}
		TRC.warning("registration failed");
		return ServiceLocationException.INVALID_REGISTRATION;
	}

	
	
	
}