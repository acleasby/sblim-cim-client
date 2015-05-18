/**
 * ResponseCache.java
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


package org.sblim.slp.internal.ua;

import java.net.DatagramPacket;
import java.util.Arrays;
import java.util.HashSet;

/**
 * ResponseCache intends to eliminate the processing of SLP responses with the same content.
 *
 */
public class ResponseCache {
	
	private static class Entry {
		
		private byte[] iData;
		
		private int iHashCode;
		
		/**
		 * Ctor.
		 * @param pData
		 * @param pOffset
		 * @param pLength
		 */
		public Entry(byte[] pData, int pOffset, int pLength) {
			iData = new byte[pLength];
			System.arraycopy(pData, pOffset, iData, 0, pLength);
			for(int pos = 0; pos < iData.length; ++pos) {
				iHashCode <<= 4;
				iHashCode += (iData[pos] & 0xff);
			}
		}
		
		/**
		 * Ctor.
		 * @param pPacket
		 */
		public Entry(DatagramPacket pPacket) {
			this(pPacket.getData(), pPacket.getOffset(), pPacket.getLength());
		}
		
		public int hashCode() { return iHashCode; }
		
		public boolean equals(Object pObj) {
			if (this == pObj) return true;
			Entry that = (Entry)pObj;
			return iHashCode == that.iHashCode && Arrays.equals(iData, that.iData);
		}
		
	}
	
	private HashSet iResponseSet = new HashSet();
	
	/**
	 * add
	 * @param pPacket
	 */
	public void add(DatagramPacket pPacket) {
		iResponseSet.add(new Entry(pPacket));
	}
	
	
	/**
	 * contains
	 * @param pPacket
	 * @return boolean
	 */
	public boolean contains(DatagramPacket pPacket) {
		return iResponseSet.contains(new Entry(pPacket));
	}
	
}