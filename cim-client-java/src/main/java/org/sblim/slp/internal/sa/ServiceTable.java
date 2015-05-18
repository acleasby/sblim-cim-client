/**
 * ServiceTable.java
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
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.sblim.slp.ServiceType;
import org.sblim.slp.ServiceURL;
import org.sblim.slp.internal.IPv6MulticastAddressFactory;
import org.sblim.slp.internal.Net;
import org.sblim.slp.internal.SLPConfig;
import org.sblim.slp.internal.SLPDefaults;
import org.sblim.slp.internal.TRC;

/**
 * ServiceTable
 *
 */
public class ServiceTable {
	
	DatagramThread iDgramThread;
	
	private boolean
		iUseV6 = Net.hasIPv6() && SLPConfig.getGlobalCfg().useIPv6();
	
	class AddressHashTable {
		
		class Counter {
			/**
			 * iValue
			 */
			public int iValue = 1;
		}
		
		/**
		 * AddressHash -> Counter
		 */
		private HashMap iMap = new HashMap();
		
		/**
		 * register
		 * @param pType
		 * @throws UnknownHostException
		 * @throws IOException
		 */
		public void register(ServiceType pType) throws UnknownHostException, IOException {
			Integer hash = new Integer( 
				IPv6MulticastAddressFactory.getSrvTypeHash(pType)
			);
			TRC.debug("srvType:"+pType+", hash:"+hash);
			Counter cntr = (Counter)iMap.get(hash);
			if (cntr == null) {
				cntr = new Counter();
				iMap.put(hash, cntr);
				iDgramThread.joinGroup(
					IPv6MulticastAddressFactory.get(SLPDefaults.IPV6_MULTICAST_SCOPE, hash.intValue())
				);
			} else {
				++cntr.iValue;
			}
			
		}
		
		/**
		 * unregister
		 * @param pType
		 * @throws UnknownHostException
		 * @throws IOException
		 */
		public void unregister(ServiceType pType) throws UnknownHostException, IOException {
			Integer hash = new Integer( 
				IPv6MulticastAddressFactory.getSrvTypeHash(pType)
			);
			Counter cntr = (Counter)iMap.get(hash);
			if (cntr == null) return;
			if (cntr.iValue <= 1) {
				iMap.remove(hash);
				iDgramThread.leaveGroup(
					IPv6MulticastAddressFactory.get(SLPDefaults.IPV6_MULTICAST_SCOPE, hash.intValue())
				);
			} else {
				--cntr.iValue;
			}
		}
		
	}
	
	private static class ServiceEntry {
		
		private ServiceURL iSrvURL;
		private List iAttribs;
		private List iScopes;
		
		/**
		 * Ctor.
		 * @param pSrvURL
		 * @param pAttribs
		 * @param pScopes
		 */
		public ServiceEntry(ServiceURL pSrvURL, List pAttribs, List pScopes) {
			set(pSrvURL, pAttribs, pScopes);
		}
		
		/**
		 * set
		 * @param pSrvURL
		 * @param pAttribs
		 * @param pScopes
		 */
		public void set(ServiceURL pSrvURL, List pAttribs, List pScopes) {
			iSrvURL = pSrvURL; iAttribs = pAttribs;
			iScopes = pScopes;
		}
		
		/**
		 * getServiceURL
		 * @return ServiceURL
		 */
		public ServiceURL getServiceURL() { return iSrvURL; }
		
		/**
		 * getServiceType
		 * @return ServiceType
		 */
		public ServiceType getServiceType() {
			return iSrvURL.getServiceType();
		}
		
		/**
		 * getAttributes
		 * @return List
		 */
		public List getAttributes() { return iAttribs; }
		
		/**
		 * getScopes
		 * @return List
		 */
		public List getScopes() { return iScopes; }
		
		/**
		 * hasMatchingScope
		 * @param pScopes
		 * @return boolean
		 */
		public boolean hasMatchingScope(List pScopes) {
			if (pScopes == null) return false;
			Iterator itr = pScopes.iterator();
			while (itr.hasNext())
				if (hasScope((String)itr.next())) return true;
			return false;
		}
		
		public String toString() {
			return
				"url:"+iSrvURL+", attribs:"+dumpList(iAttribs)+", scopes:"+
				dumpList(iScopes);
		}
		
		private boolean hasScope(String pScope) {
			return iScopes == null ? false : iScopes.contains(pScope);
		}
		
	}
	
	static class ServiceEntryList extends ArrayList {
	
		private static final long serialVersionUID = 1L;

		/**
		 * get
		 * @param pSrvURL
		 * @return ServiceEntry
		 */
		public ServiceEntry get(ServiceURL pSrvURL) {
			for(int i=0; i<size(); i++) {
				ServiceEntry entry = (ServiceEntry)get(i);
				if (pSrvURL.equals(entry.getServiceURL())) return entry;
			}
			return null;
		}
		
		/**
		 * remove
		 * @param pSrvURL
		 */
		public void remove(ServiceURL pSrvURL) {
			for(int i=0; i<size(); i++) {
				ServiceEntry entry = (ServiceEntry)get(i);
				if (pSrvURL.equals(entry.getServiceURL())) {
					remove(i); break;
				}
			}
		}
		
		/**
		 * getServiceURLs
		 * @param pSrvType
		 * @param pScopes
		 * @return List
		 */
		public List getServiceURLs(ServiceType pSrvType, List pScopes) {
			if (pSrvType == null) return null;
			List srvURLs = null;
			for(int i=0; i<size(); i++) {
				ServiceEntry entry = (ServiceEntry)get(i);
				if (!entry.hasMatchingScope(pScopes)) continue;
				if (
					pSrvType.getPrincipleTypeName().equals(
						entry.getServiceType().getPrincipleTypeName()
					)
				) {
					if (srvURLs == null) srvURLs = new ArrayList();
					srvURLs.add(entry.getServiceURL());
				}
			}
			return srvURLs;
		}
	
	}
	
	
	private ServiceEntryList iSrvEntryTable = new ServiceEntryList();
	
	
	private AddressHashTable iAddressHashTable = new AddressHashTable();
		

	/**
	 * Ctor.
	 * @param pDgramThread
	 */
	public ServiceTable(DatagramThread pDgramThread) {
		iDgramThread = pDgramThread;
	}
	
	/**
	 * add
	 * @param pSrvURL
	 * @param pAttrList
	 * @param pScopes
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	public synchronized void add(
		ServiceURL pSrvURL, List pAttrList, List pScopes
	) throws UnknownHostException, IOException  {
		if (pSrvURL == null) return;
		TRC.debug("add URL:"+pSrvURL+", scopes:"+dumpList(pScopes));
		ServiceEntry srvEntry = iSrvEntryTable.get(pSrvURL);
		if (srvEntry == null) {
			iSrvEntryTable.add(new ServiceEntry(pSrvURL, pAttrList, pScopes));
		} else {
			srvEntry.set(pSrvURL, pAttrList, pScopes);
		}
		
		if (!iUseV6) return;
		iAddressHashTable.register(pSrvURL.getServiceType());
	}
	
	/**
	 * remove
	 * @param pSrvURL
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	public synchronized void remove(ServiceURL pSrvURL) throws UnknownHostException, IOException {
		iSrvEntryTable.remove(pSrvURL);
		
		if (!iUseV6) return;
		iAddressHashTable.unregister(pSrvURL.getServiceType());
	}
	
	/**
	 * getServiceURLs
	 * @param pSrvType
	 * @param pScopes
	 * @return List
	 */
	public synchronized List getServiceURLs(ServiceType pSrvType, List pScopes) {
		TRC.debug("getServiceURLs srvType:"+pSrvType+", scopes:"+dumpList(pScopes));
		List list = iSrvEntryTable.getServiceURLs(pSrvType, pScopes);
		return list;
	}
	
	/**
	 * getAttributes
	 * @param pSrvURL
	 * @param pScopes
	 * @return List
	 */
	public synchronized List getAttributes(ServiceURL pSrvURL, List pScopes) {
		if (pSrvURL == null) return null;
		if (pSrvURL.getURLPath() == null) return getAttributes(pSrvURL.getServiceType(), pScopes);
		ServiceEntry entry =  iSrvEntryTable.get(pSrvURL);
		return entry == null ? null : entry.getAttributes();
	}
	
	/**
	 * getAttributes
	 * @param pSrvType
	 * @param pScopes
	 * @return List
	 */
	public synchronized List getAttributes(ServiceType pSrvType, List pScopes) {
		if (pSrvType == null) return null;
		HashSet attribs = new HashSet();
		for(int i=0; i<iSrvEntryTable.size(); i++) {
			ServiceEntry entry = (ServiceEntry)iSrvEntryTable.get(i);
			ServiceType srvType = entry.getServiceType();
			if (pSrvType.equals(srvType))
				attribs.addAll(entry.getAttributes());
		}
		return new ArrayList(attribs);
	}
	
	/**
	 * getServiceTypes
	 * @param pScopes
	 * @return List
	 */
	public synchronized List getServiceTypes(List pScopes) {
		List srvTypes = null;
		for(int i=0; i<iSrvEntryTable.size(); i++) {
			ServiceEntry entry = (ServiceEntry)iSrvEntryTable.get(i);
			if (entry.hasMatchingScope(pScopes)) {
				ServiceType srvType = entry.getServiceType();
				if (srvType == null) continue;
				if (srvTypes == null) srvTypes = new ArrayList();
				srvTypes.add(srvType);
			}
		}
		return srvTypes;
	}
	
	static String dumpList(List pList) {
		return dumpList(pList, ",");
	}
	
	private static String dumpList(List pList, String pSep) {
		if (pList == null) return "null";
		StringBuffer buf = new StringBuffer();
		Iterator itr = pList.iterator();
		boolean first = true;
		while (itr.hasNext()) {
			if (!first) buf.append(pSep);
			buf.append(itr.next().toString());
			first = false;
		}
		return buf.toString();
	}
	
}