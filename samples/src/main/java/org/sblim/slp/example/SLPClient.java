/**
 * SLPClient.java
 *
 * (C) Copyright IBM Corp. 2005, 2009
 *
 * THIS FILE IS PROVIDED UNDER THE TERMS OF THE ECLIPSE PUBLIC LICENSE 
 * ("AGREEMENT"). ANY USE, REPRODUCTION OR DISTRIBUTION OF THIS FILE 
 * CONSTITUTES RECIPIENTS ACCEPTANCE OF THE AGREEMENT.
 *
 * You can obtain a current copy of the Eclipse Public License from
 * http://www.opensource.org/licenses/eclipse-1.0.php
 *
 * @author: Roberto Pineiro, IBM, roberto.pineiro@us.ibm.com  
 * @author: Chung-hao Tan, IBM ,chungtan@us.ibm.com
 * 
 * Change History
 * Flag       Date        Prog         Description
 *------------------------------------------------------------------------------- 
 * 1516246    2006-07-22  lupusalex    Integrate SLP client code
 * 2807325    2009-06-22  blaschke-oss Change licensing from CPL to EPL
 */

package org.sblim.slp.example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import org.sblim.slp.Locator;
import org.sblim.slp.ServiceLocationException;
import org.sblim.slp.ServiceLocationManager;
import org.sblim.slp.ServiceType;
import org.sblim.slp.ServiceURL;

/**
 * Sample simplified SLP client that provides methods to find wbem services and
 * others.
 */
public class SLPClient {

	public static final String DEFAULT_SCOPE = "default";

	public static final Vector SCOPE = new Vector(Arrays.asList(new String[] { DEFAULT_SCOPE }));

	public static final String SERVICE_WBEM = "service:wbem";

	public static final String SERVICE_WBEM_HTTP = "service:wbem:http";

	public static final String SERVICE_WBEM_HTTPS = "service:wbem:https";

	/**
	 * Ctor.
	 */
	public SLPClient() {}

	/**
	 * Finds all WBEM services
	 * 
	 * @return List of WBEM services
	 */
	public List findWbemServices() {
		return findService(SERVICE_WBEM);
	}

	/**
	 * Finds all services of a given type
	 * 
	 * @param pServiceType
	 *            The service type
	 * @return List of services
	 */
	public List findService(String pServiceType) {
		return findService(pServiceType, SCOPE);
	}

	/**
	 * Finds all services of a given type
	 * 
	 * @param pServiceType
	 *            The service type
	 * @param pScopeList
	 *            The scope
	 * @return List of services
	 */
	public List findService(String pServiceType, Vector pScopeList) {

		List result = new ArrayList();
		final Locator locator;

		try {
			locator = ServiceLocationManager.getLocator(Locale.US);
			ServiceType servicetype = new ServiceType(pServiceType);
			Enumeration enumeration = locator.findServices(servicetype, pScopeList, "");

			while (enumeration.hasMoreElements()) {
				String s = enumeration.nextElement().toString();
				result.add(s);
			}
		} catch (ServiceLocationException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * Find the specified attributes of a given service url
	 * 
	 * @param pUrl
	 *            The service url
	 * @param pScopes
	 *            The scope
	 * @param pAttributeIds
	 *            List of attribute to look for, <code>null</code> represents
	 *            all attributes
	 * @return List of attributes
	 */
	public List findAttributes(String pUrl, Vector pScopes, Vector pAttributeIds) {

		List result = new ArrayList();
		final Locator locator;

		try {
			locator = ServiceLocationManager.getLocator(Locale.US);

			ServiceURL servicetype = new ServiceURL(pUrl, -1);
			Enumeration enumeration = locator.findAttributes(servicetype, pScopes, pAttributeIds);

			while (enumeration.hasMoreElements()) {
				String s = enumeration.nextElement().toString();
				if (s.length() > 2 && s.charAt(0) == '(' && s.charAt(s.length() - 1) == ')') result
						.add(s.substring(1, s.length() - 1));
				else result.add(s);
			}
		} catch (ServiceLocationException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * Unescapes a attribute string
	 * 
	 * @param pString
	 *            The escaped string
	 * @return The unescaped string
	 */
	public static String unescape(String pString) {
		StringBuffer buf = new StringBuffer("");
		int i = pString.indexOf('\\');
		int prev = 0;
		int len = pString.length();
		while (i > -1) {
			buf.append(pString.substring(prev, i));
			if (i + 3 < len) {
				try {
					char ch = (char) (Integer.parseInt(pString.substring(i + 1, i + 3), 16) & 0xFF);
					buf.append(ch);
				} catch (Exception e) {
					// TODO to handle this in a better way
					buf.append(pString.substring(i, i + 3));
				}
				prev = i + 3;
			}
			i = pString.indexOf('\\', prev);
		}
		buf.append(pString.substring(prev));
		return buf.toString();
	}

	/**
	 * Test method.
	 * 
	 * @param args
	 *            Ignored
	 */
	public static void main(String[] args) {

		SLPClient client = new SLPClient();
		List wbemservices = client.findWbemServices();

		Iterator serviceIterator = wbemservices.iterator();
		while (serviceIterator.hasNext()) {
			String url = serviceIterator.next().toString();
			System.out.println(url);
			List attributes = client.findAttributes(url, SCOPE, new Vector());

			Iterator attributeIterator = attributes.iterator();
			while (attributeIterator.hasNext()) {
				System.out.println(attributeIterator.next());
			}
		}

	}
}
