/**
 * SLPTool.java
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
 * 2807325    2009-06-22  blaschke-oss Change licensing from CPL to EPL
 */



package org.sblim.slp.example;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.logging.Level;

import org.sblim.slp.Advertiser;
import org.sblim.slp.Locator;
import org.sblim.slp.ServiceLocationAttribute;
import org.sblim.slp.ServiceLocationException;
import org.sblim.slp.ServiceLocationManager;
import org.sblim.slp.ServiceType;
import org.sblim.slp.ServiceURL;
import org.sblim.slp.internal.SLPConfig;
import org.sblim.slp.internal.TRC;

/**
 * SLPTool 
 *
 */
public class SLPTool {
	
	private static void usage() {
		System.err.println(
			"usage:\n" +
			"  SLPTool [opts] SrvRegister serviceURL [attr=val,...]\n"+
			"  SLPTool [opts] SrvDeregister serviceURL\n"+
			"  SLPTool [opts] SrvRequest serviceType [attr=val]\n"+
			"  SLPTool [opts] AttrRequest serviceURL|serviceType [attr]\n\n"+
			"options:\n"+
			"  --port slpPort"+
			"  --DAList daURL0,daURL1...\n"+
			"  --ScopeList scope0,scope1...\n"+
			"  --noV6...\n"+
			"  --noV4...\n"
		);
		System.exit(-1);
	}
	
	private static Vector cDAVec;
	
	private static Vector cScopeVec;
	
	/**
	 * main
	 * @param pArgs
	 * @throws ServiceLocationException
	 */
	public static void main(String[] pArgs) throws ServiceLocationException {
		if (pArgs == null || pArgs.length == 0) usage();
		TRC.setLevel(Level.INFO);
		TRC.setOutput(System.out);
		Iterator argItr = Arrays.asList(pArgs).iterator();
		while (argItr.hasNext()) {
			String param = (String)argItr.next();
			if (param.equals("--port")) {
				parsePort(argItr);
		    } else if (param.equals("--DAList")) {
				parseDAList(argItr);
			} else if (param.equals("--ScopeList")) {
				parseScopeList(argItr); 
			} else if (param.equals("--noV6")) {
				SLPConfig.getGlobalCfg().setUseIPv6(false);
			} else if (param.equals("--noV4")) {
				SLPConfig.getGlobalCfg().setUseIPv4(false);
			} else if (param.equals("SrvRegister")) {
				serviceRegistration(argItr); break;
			} else if (param.equals("SrvDeregister")) {
				serviceDeregistration(argItr); break;
			} else if (param.equals("SrvRequest")) {
				serviceRequest(argItr); break;
			} else if (param.equals("AttrRequest")) {
				attributeRequest(argItr); break;
			} else {
				usage();
			}
		}
		System.out.println("done...");
	}
	
	private static void parsePort(Iterator pArgItr) {
		SLPConfig.getGlobalCfg().setPort(
			Integer.parseInt(readParam(pArgItr))
		);
	}
	
	private static void parseDAList(Iterator pArgItr) {
		cDAVec = parseListString(readParam(pArgItr));
	}
	
	private static void parseScopeList(Iterator pArgItr) {
		cScopeVec = parseListString(readParam(pArgItr));
	}
	
	/**
	 * SrvRegister serviceURL [attr=val,...]
	 * @param pArgItr
	 */
	private static void serviceRegistration(Iterator pArgItr) throws ServiceLocationException {
		String serviceURL = readParam(pArgItr);
		Vector attribs = pArgItr.hasNext() ? new Vector() : null;
		// read attributes
		while (pArgItr.hasNext())
			attribs.add(new ServiceLocationAttribute((String)pArgItr.next()));
		Advertiser advertiser = ServiceLocationManager.getAdvertiser(Locale.US);
		advertiser.register(
			new ServiceURL(serviceURL, ServiceURL.LIFETIME_DEFAULT), attribs
		);
	}
	
	/**
	 * SrvDeregister serviceURL
	 * @param pArgItr
	 */
	private static void serviceDeregistration(Iterator pArgItr) throws ServiceLocationException {
		String serviceURL = readParam(pArgItr);
		Advertiser advertiser = ServiceLocationManager.getAdvertiser(Locale.US);
		advertiser.deregister(
			new ServiceURL(serviceURL, ServiceURL.LIFETIME_DEFAULT)
		);
	}
	
	/**
	 * SrvRequest serviceType [attr=val]
	 * @param pArgItr
	 */
	private static void serviceRequest(Iterator pArgItr) throws ServiceLocationException {
		String serviceType = readParam(pArgItr);
		System.out.println("serviceType:"+serviceType);
		String filter = pArgItr.hasNext() ? (String)pArgItr.next() : null;
		Locator locator = ServiceLocationManager.getLocator(Locale.US);
		dumpEnum(
			locator.findServices(
				new ServiceType(serviceType), cScopeVec, filter, cDAVec
			)
		);
	}
	
	/**
	 * AttrRequest serviceURL
	 * @param pArgItr
	 */
	private static void attributeRequest(Iterator pArgItr) throws ServiceLocationException {
		String serviceURL = readParam(pArgItr);
		Locator locator = ServiceLocationManager.getLocator(Locale.US);
		dumpEnum(
			locator.findAttributes(
				new ServiceURL(serviceURL, 1000), cScopeVec, cDAVec
			)
		);
	}
	
	private static String readParam(Iterator pArgItr) {
		if (!pArgItr.hasNext()) usage();
		return (String)pArgItr.next();
	}
	
	private static Vector parseListString(String pStr) {
		StringTokenizer tokenizer = new StringTokenizer(pStr, ",");
		if (tokenizer.countTokens() == 0) return null;
		Vector vec = new Vector(tokenizer.countTokens());
		while (tokenizer.hasMoreTokens())
			vec.add(tokenizer.nextToken());
		return vec;
	}
	
	private static void dumpEnum(Enumeration pEnum) {
		while (pEnum.hasMoreElements()) {
			System.out.println(pEnum.nextElement());
		}
	}
	
}