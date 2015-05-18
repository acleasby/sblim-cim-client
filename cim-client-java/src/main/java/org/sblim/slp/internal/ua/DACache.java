/**
 * DACache.java
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


package org.sblim.slp.internal.ua;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

import org.sblim.slp.internal.SLPDefaults;
import org.sblim.slp.internal.msg.DADescriptor;

/**
 * DACache caches the discovered DA list in order to eliminate frequent DA discovery
 * network traffic.
 * 
 */
public class DACache {
	
	private static class ScopeEntry {
		
		private long iTimeOfDiscovery;
		
		private TreeSet iDADescriptors;
		
		/**
		 * Ctor.
		 * @param pDADescriptors
		 */
		public ScopeEntry(TreeSet pDADescriptors) {
			iDADescriptors = pDADescriptors;
			iTimeOfDiscovery = getSecs();
		}
		
		/**
		 * valid
		 * @return boolean
		 */
		public boolean valid() {
			return getSecs() - iTimeOfDiscovery <= SLPDefaults.DACACHE_TIMEOUT;
		}
		
		/**
		 * getDADescriptorItr
		 * @return Iterator
		 */
		public Iterator getDADescriptorItr() {
			return iDADescriptors==null ? null : iDADescriptors.iterator();
		}
		
	}
	
	/**
	 * key: scope
	 * value: ScopeEntry
	 */
	private static TreeMap cScopeMap = new TreeMap();
	
	// TODO: handle scopes
	
	/**
	 * @param pScopes 
	 * @return List of discoverable scope strings
	 */
	public static synchronized List getDiscoverableScopeList(List pScopes) {
		if (pScopes == null || pScopes.size() == 0) return null;
		List scopeList = null;
		Iterator itr = pScopes.iterator();
		while (itr.hasNext()) {
			String scope = (String)itr.next();
			ScopeEntry scopeEntry = (ScopeEntry)cScopeMap.get(scope);
			if (scopeEntry==null || !scopeEntry.valid()) {
				if (scopeList == null) scopeList = new ArrayList();
				scopeList.add(scope);
			}
		}
		return scopeList;
	}
	
	/**
	 * @param pScopes
	 * @return List of DA URLs
	 */
	public static synchronized List getDAList(List pScopes) {
		if (cScopeMap == null) return null;
		TreeSet daSet = new TreeSet();
		Iterator scopeItr = pScopes.iterator();
		while (scopeItr.hasNext()) {
			String scope = (String)scopeItr.next();
			ScopeEntry scopeEntry =  (ScopeEntry)cScopeMap.get(scope);
			if (scopeEntry == null) continue;
			Iterator descItr = scopeEntry.getDADescriptorItr();
			if (descItr == null) continue;
			while (descItr.hasNext()) {
				daSet.add(((DADescriptor)descItr.next()).getURL());
			}
		}
		return new ArrayList(daSet);
	}
	
	/**
	 * @param pScopes - list of discovered hosts
	 * @param pDADescriptors - DADescriptors of the discovered DAs
	 */
	public static synchronized void setDAList(List pScopes, List pDADescriptors) {
		if (pScopes == null || pDADescriptors == null) return;
		Iterator scopeItr = pScopes.iterator();
		while (scopeItr.hasNext()) {
			String scope = (String)scopeItr.next();
			TreeSet daDescsForScope = null;
			Iterator descItr = pDADescriptors.iterator();
			while (descItr.hasNext()) {
				DADescriptor daDesc = (DADescriptor)descItr.next();
				if (daDesc.hasScope(scope)) {
					if (daDescsForScope == null) daDescsForScope = new TreeSet();
					daDescsForScope.add(daDesc);
				}
			}
			cScopeMap.put(scope, new ScopeEntry(daDescsForScope));
		}
	}
	
	
	static long getSecs() {
		return new Date().getTime()/1000;
	}
	
}