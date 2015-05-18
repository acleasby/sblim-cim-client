/**
 * DADescriptor.java
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


package org.sblim.slp.internal.msg;

import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

/**
 * <pre>
 * This class contains the DA related information from a DAAdvert message.
 * URL
 * Scope list
 * Attribute list
 * </pre>
 */
public class DADescriptor implements Comparable {
	
	private String iURL;
	private TreeSet iScopeSet;
	private List iAttributes;
	
	/**
	 * Ctor.
	 * @param pURL
	 * @param pScopeSet - set of scope Strings
	 * @param pAttributes - set of ServiceLocationAttributes
	 */
	public DADescriptor(String pURL, TreeSet pScopeSet, List pAttributes) {
		iURL = pURL; iScopeSet = pScopeSet; iAttributes = pAttributes;
	}

	/**
	 * getURL
	 * @return String
	 */
	public String getURL() { return iURL; }
	
	/**
	 * hasScope
	 * @param pScope
	 * @return boolean
	 */
	public boolean hasScope(String pScope) {
		if (iScopeSet==null) return false;
		return iScopeSet.contains(pScope);
	}
	
	public int compareTo(Object o) {
		DADescriptor that = (DADescriptor)o;
		return iURL.compareTo(that.iURL);
	}
	
	
	private int iHashCode = 0;
	
	private void incHashCode(int pHashCode) {
		iHashCode *= 31; iHashCode += pHashCode;
	}
	
	/*
	 * hashCode has to be independent of the order of scopes and attributes
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		if (iHashCode == 0) {
			iHashCode = iURL.hashCode();
			if (iScopeSet != null) {
				Iterator itr = iScopeSet.iterator();
				while (itr.hasNext()) incHashCode(itr.next().hashCode());
			}
			if (iAttributes != null) {
				Iterator itr = iAttributes.iterator();
				/*
				 * iHasCode is simply incremented, because attribute order mustn't be
				 * considered.
				 */
				while (itr.hasNext()) iHashCode += itr.next().hashCode();
			}
		}
		return iHashCode;
	}
	
	public String toString() {
		StringBuffer strBuf = new StringBuffer("URL : "+iURL+"\nScopes : ");
		if (iScopeSet != null) {
			Iterator itr = iScopeSet.iterator();
			boolean more = false;
			while (itr.hasNext()) {
				if (more) strBuf.append(", "); else more = true; 
				strBuf.append(itr.next());
			}
		}
		
		return strBuf.toString();
	}
	
}