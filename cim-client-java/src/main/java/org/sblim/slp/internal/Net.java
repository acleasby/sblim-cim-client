/**
 * Net.java
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


package org.sblim.slp.internal;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * Net 
 *
 */
public class Net {
	
	private static boolean cHasV6, cHasV4;
	
	/**
	 * hasIPv6
	 * @return boolean
	 */
	public static boolean hasIPv6() {
		scan();
		return cHasV6;
	}
	
	/**
	 * hasIPv4
	 * @return boolean
	 */
	public static boolean hasIPv4() {
		scan();
		return cHasV4;
	}
	
	private static boolean cScanned;
	
	private static void scan() {
		if (cScanned) return;
		try {
			cScanned = true;
			Enumeration ifaceEnum = NetworkInterface.getNetworkInterfaces();
			ifLoop: while (ifaceEnum.hasMoreElements()) {
				NetworkInterface iface = (NetworkInterface)ifaceEnum.nextElement();
				Enumeration addrEnum = iface.getInetAddresses();
				while (addrEnum.hasMoreElements()) {
					InetAddress addr = (InetAddress)addrEnum.nextElement();
					if (addr instanceof Inet4Address) {
						cHasV4 = true;
						if (cHasV6) break ifLoop;
					} else if (addr instanceof Inet6Address) {
						cHasV6 = true;
						if (cHasV4) break ifLoop;
					}
				}
			}
			TRC.info("available IP versions : IPv4:"+cHasV4+", IPv6:"+cHasV6);
		} catch (SocketException e) {
			TRC.error(e);
		}
	}
	
	
}