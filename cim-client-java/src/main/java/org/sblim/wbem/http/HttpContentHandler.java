/**
 * HttpContentHandler.java
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
 * 
 * Change History
 * Flag       Date        Prog         Description
 *------------------------------------------------------------------------------- 
 *   17931    2005-07-28  thschaef     Add InetAddress to CIM Event
 * 2807325    2009-06-22  blaschke-oss Change licensing from CPL to EPL
 *
 */

package org.sblim.wbem.http;

import java.io.IOException;
import java.net.InetAddress;

public abstract class HttpContentHandler {
	
	public abstract void handleContent(MessageReader pMr, 
			MessageWriter pMw,
			InetAddress pInetAdress
			) 
				throws HttpException, IOException;
	public abstract void close();
}
