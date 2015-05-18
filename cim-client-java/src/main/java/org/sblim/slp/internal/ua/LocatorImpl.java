/**
 * LocatorImpl.java
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

import java.util.Locale;
import java.util.SortedSet;
import java.util.Vector;

import org.sblim.slp.Locator;
import org.sblim.slp.ServiceLocationEnumeration;
import org.sblim.slp.ServiceType;
import org.sblim.slp.ServiceURL;
import org.sblim.slp.internal.SLPDefaults;
import org.sblim.slp.internal.TRC;
import org.sblim.slp.internal.msg.AttributeRequest;
import org.sblim.slp.internal.msg.ServiceRequest;
import org.sblim.slp.internal.msg.ServiceTypeRequest;
import org.sblim.slp.internal.msg.Util;

/**
 * LocatorImpl 
 *
 */
public class LocatorImpl implements Locator {
	
	private Locale iLocale;
	private String iLangTag;
	
	/**
	 * Ctor.
	 * @param pLocale
	 */
	public LocatorImpl(Locale pLocale) {
		iLocale = pLocale; iLangTag = Util.getLangTag(iLocale);
		TRC.debug("created, langTag="+iLangTag);
	}

	public ServiceLocationEnumeration findAttributes(
		ServiceURL pURL, Vector pScopes, Vector pAttributeIds
	) {
		return findAttributes(pURL, pScopes, pAttributeIds, null);
	}

	public ServiceLocationEnumeration findAttributes(
		ServiceURL pURL, Vector pScopes, Vector pAttributeIds, Vector pDirectoryAgents
	) {
		return new SLEnumerationImpl(
			new AttributeRequest(
				iLangTag, (SortedSet)null, pURL.toString(), getScopes(pScopes), pAttributeIds,
				null
			), pDirectoryAgents
		);
	}

	public ServiceLocationEnumeration findAttributes(
		ServiceType pType, Vector pScopes, Vector pAttributeIds
	) {
		return findAttributes(pType, pScopes, pAttributeIds, null);
	}

	public ServiceLocationEnumeration findAttributes(
		ServiceType pType, Vector pScopes, Vector pAttributeIds, Vector pDirectoryAgents
	) {
		return new SLEnumerationImpl(
			new AttributeRequest(
				iLangTag, (SortedSet)null, pType.toString(), getScopes(pScopes), pAttributeIds,
				null
			), pDirectoryAgents
		);
	}

	public ServiceLocationEnumeration findServiceTypes(
		String pNamingAuthority, Vector pScopes
	) {
		return findServiceTypes(pNamingAuthority, pScopes, null);
	}

	public ServiceLocationEnumeration findServiceTypes(
		String pNamingAuthority, Vector pScopes, Vector pDirectoryAgent
	) {
		return new SLEnumerationImpl(
			new ServiceTypeRequest(
				iLangTag, null, pNamingAuthority, getScopes(pScopes)
			),
			pDirectoryAgent
		);
	}

	public ServiceLocationEnumeration findServices(
		ServiceType pType, Vector pScopes, String pSearchFilter
	) {
		return findServices(pType, pScopes, pSearchFilter, null);
	}

	public ServiceLocationEnumeration findServices(
		ServiceType pType, Vector pScopes, String pSearchFilter, Vector pDirectoryAgents
	) {
		return new SLEnumerationImpl(
			new ServiceRequest(iLangTag, null, pType, getScopes(pScopes), pSearchFilter, null),
			pDirectoryAgents
		);
	}

	public Locale getLocale() {
		return iLocale;
	}
	
	/**
	 * @param pScopes
	 * @return
	 *   pScopes if that is not empty or a Vector with "default" entry if the pScopes is
	 *   null or empty
	 */
	private static Vector getScopes(Vector pScopes) {
		if (pScopes == null) pScopes = new Vector();
		if (pScopes.isEmpty()) pScopes.add(SLPDefaults.DEFAULT_SCOPE);
		return pScopes;
	}
	
}