/**
 * SLPFindServices.java
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

import java.util.Enumeration;
import java.util.Locale;
import java.util.Vector;

import org.sblim.slp.Locator;
import org.sblim.slp.ServiceLocationException;
import org.sblim.slp.ServiceLocationManager;
import org.sblim.slp.ServiceType;

/**
 * Locates WBEM services (HTTPS only) that are registered on a SA/DA
 */
public class SLPFindServices {

	public static void main(String[] args) {
		final Locator locator;
		try {
			locator = ServiceLocationManager.getLocator(Locale.US);

			Vector scopes = new Vector();
			scopes.add("default");
			String query = "";
			ServiceType servicetype = new ServiceType("service:wbem:https");
			Enumeration enumeration = locator.findServices(servicetype, scopes, query);

			while (enumeration.hasMoreElements()) {
				System.out.println(enumeration.nextElement());
			}

		} catch (ServiceLocationException e) {
			e.printStackTrace();
		}
	}
}
