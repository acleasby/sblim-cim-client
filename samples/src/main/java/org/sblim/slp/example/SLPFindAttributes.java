/**
 * SLPFindAttributes.java
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
import org.sblim.slp.ServiceURL;

/**
 * List the attribute of a given service. In order to get results, you have to
 * change the service url a really exisiting one.
 */
public class SLPFindAttributes {

	public static void main(String[] args) {

		try {
			Locator locator = ServiceLocationManager.getLocator(Locale.US);

			Vector scopes = new Vector();
			scopes.add("default");

			ServiceURL servicetype = new ServiceURL("service:wbem:https://1.1.1.1:5989", -1);

			Vector attributeIds = new Vector();
			Enumeration enumeration = locator.findAttributes(servicetype, scopes, attributeIds);

			while (enumeration.hasMoreElements()) {
				System.out.println(enumeration.nextElement());
			}

		} catch (ServiceLocationException e) {
			e.printStackTrace();
		}
	}

}
