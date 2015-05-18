/**
 * MessageTable.java
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
 * 1892103    2008-02-12  ebak         SLP improvements
 * 2807325    2009-06-22  blaschke-oss Change licensing from CPL to EPL
 */


package org.sblim.slp.internal.sa;

import java.net.InetAddress;
import java.util.Date;
import java.util.SortedMap;
import java.util.TreeMap;

import org.sblim.slp.ServiceLocationException;
import org.sblim.slp.internal.msg.SLPMessage;
import org.sblim.slp.internal.msg.RequestMessage;;


/**
 * Keeps track of datagram messages. For requestes with the same XID
 * the same responses should be returned.
 *
 */
public class MessageTable {
	
	private static class RequestDescriptor implements Comparable {
		
		private byte[] iSrcAddress;
		
		private byte[] iRequest;
		
		/**
		 * Ctor.
		 * @param pSource
		 * @param pRequest
		 * @throws ServiceLocationException
		 */
		public RequestDescriptor(InetAddress pSource, SLPMessage pRequest)
		throws ServiceLocationException {
			iSrcAddress = pSource.getAddress();
			iRequest =
				(pRequest instanceof RequestMessage) ?
				((RequestMessage)pRequest).serializeWithoutResponders(
					false, true, true
				) :
				pRequest.serialize(false, true, true);
		}

		public int compareTo(Object pObj) {
			RequestDescriptor that = (RequestDescriptor)pObj;
			int cmp = compare(iSrcAddress, that.iSrcAddress);
			if (cmp != 0) return cmp;
			return compare(iRequest, that.iRequest);
		}
		
		private static int compare(byte[] pBytes0, byte[] pBytes1) {
			int len = Math.min(pBytes0.length, pBytes1.length);
			for(int i=0; i<len; i++) {
				int cmp = pBytes0[i] & 0xff - pBytes1[i] & 0xff;
				if (cmp != 0) return cmp;
			}
			return pBytes0.length - pBytes1.length;
		}
		
	}
	
	private static class TableEntry {
		
		private long iTime;
		private RequestDescriptor iReqDesc;
		private byte[] iResponse;
		
		/**
		 * Ctor.
		 * @param pTime
		 * @param pReqKey
		 * @param pResponse
		 */
		public TableEntry(long pTime, RequestDescriptor pReqKey, byte[] pResponse) {
			iTime = pTime; iReqDesc = pReqKey; iResponse =  pResponse;
		}
		
		/**
		 * getTime
		 * @return long
		 */
		public long getTime() { return iTime; }
		
		/**
		 * setTime
		 * @param pTime
		 */
		public void setTime(long pTime) { iTime = pTime; }
		
		/**
		 * getRequestDescriptor
		 * @return RequestDescriptor
		 */
		public RequestDescriptor getRequestDescriptor() { return iReqDesc; }
		
		/**
		 * getResponse
		 * @return byte[]
		 */
		public byte[] getResponse() { return iResponse; }
		
	}
	
	/**
	 * Remember messages for 30 seconds.
	 */
	private static final long KEEPIN = 30;
	
	/**
	 * Time -> TableEntry
	 */
	private SortedMap iTimeMap = new TreeMap();
	
	/**
	 * RequestKey -> TableEntry
	 */
	private SortedMap iReqMap = new TreeMap();
	
	/**
	 * getResponse
	 * @param pSource
	 * @param pRequest
	 * @return byte[]
	 * @throws ServiceLocationException
	 */
	public synchronized byte[] getResponse(InetAddress pSource, SLPMessage pRequest)
	throws ServiceLocationException {
		long now = getSecs();
		RequestDescriptor reqDesc = new RequestDescriptor(pSource, pRequest);
		TableEntry entry = (TableEntry)iReqMap.get(reqDesc);
		if (entry == null) return null;
		clean();
		updateTime(entry, now);
		return entry.getResponse();
	}
	
	/**
	 * addResponse
	 * @param pSource
	 * @param pRequest
	 * @param pRespond
	 * @throws ServiceLocationException
	 */
	public synchronized void addResponse(
		InetAddress pSource, SLPMessage pRequest, byte[] pRespond
	) throws ServiceLocationException {
		insert(
			new TableEntry(
				getSecs(), new RequestDescriptor(pSource, pRequest),
				pRespond
			)
		);
		clean();
	}
	
	private void clean() {
		long now = getSecs();
		Long timeStamp;
		while ((timeStamp = (Long)iTimeMap.firstKey()) != null) {
			if (now - timeStamp.longValue() < KEEPIN) break;
			TableEntry entry = (TableEntry)iTimeMap.get(timeStamp);
			remove(entry);
		}
	}
	
	private void insert(TableEntry pEntry) {
		iTimeMap.put(new Long(pEntry.getTime()), pEntry);
		iReqMap.put(pEntry.getRequestDescriptor(), pEntry);
	}
	
	private void remove(TableEntry pEntry) {
		iTimeMap.remove(new Long(pEntry.getTime()));
		iReqMap.remove(pEntry.getRequestDescriptor());
	}
	
	private void updateTime(TableEntry pEntry, long pTime) {
		remove(pEntry);
		pEntry.setTime(pTime);
		insert(pEntry);
	}
	
	private static long getSecs() {
		return new Date().getTime()/1000;
	}
	
}