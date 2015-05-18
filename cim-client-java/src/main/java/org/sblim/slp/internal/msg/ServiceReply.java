/**
 * ServiceReply.java
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
 * 1892103    2008-02-12  ebak         SLP improvements
 * 1913348    2008-04-08  raman_arora  Malformed service URL crashes SLP discovery
 * 2807325    2009-06-22  blaschke-oss Change licensing from CPL to EPL
 */


package org.sblim.slp.internal.msg;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.sblim.slp.ServiceLocationException;

/*
 *  0                   1                   2                   3
    0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
   |        Service Location header (function = SrvRply = 2)       |
   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
   |        Error Code             |        URL Entry count        |
   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
   |       <URL Entry 1>          ...       <URL Entry N>          \
   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 */
/**
 * ServiceReply message 
 *
 */
public class ServiceReply extends ReplyMessage {
	
	private List iURLEntries;
	private List iURLExceptions;

	/**
	 * parse
	 * @param pHdr
	 * @param pInStr
	 * @return SLPMessage
	 * @throws ServiceLocationException
	 * @throws IOException
	 */
	public static SLPMessage parse(
		MsgHeader pHdr, SLPInputStream pInStr
	) throws ServiceLocationException, IOException {
		int errorCode = pInStr.read16();
		ArrayList urlExceptions = new ArrayList();
		List urlEntries = pInStr.readUrlList(urlExceptions);
		return new ServiceReply(pHdr, errorCode, urlEntries, urlExceptions);
	}
	
	/**
	 * Ctor.
	 * @param pErrorCode
	 * @param pURLEntries - list of ServiceURLs
	 */
	public ServiceReply(int pErrorCode, List pURLEntries) {
		super(SRV_RPLY, pErrorCode);
		iURLEntries = pURLEntries;
	}
	
	/**
	 * Ctor.
	 * @param pLangTag
	 * @param pErrorCode
	 * @param pURLEntries - list of ServiceURLs
	 * @param pURLExceptions - list of URL Exceptions
	 */
	public ServiceReply(String pLangTag, int pErrorCode, List pURLEntries, List pURLExceptions) {
		super(SRV_RPLY, pLangTag, pErrorCode); 
		iURLEntries    = pURLEntries;
		iURLExceptions = pURLExceptions;
	}
	
	/**
	 * Ctor.
	 * @param pHeader
	 * @param pErrorCode
	 * @param pURLEntries - list of ServiceURLs
	 * @param pURLExceptions - list of URL Exceptions
	 */
	public ServiceReply(
		MsgHeader pHeader, int pErrorCode, List pURLEntries, List pURLExceptions
	) {
		super(pHeader, pErrorCode); 
		iURLEntries    = pURLEntries;
		iURLExceptions = pURLExceptions;
	}
	

	/**
	 * getResultIterator
	 * @return iterator of URL Exception list
	 */
	public Iterator getResultIterator() {
		return iURLEntries==null ? null : iURLEntries.iterator();
	}

	/**
	 * getExceptionIterator
	 * @return iterator of URL Exception list
	 */
	public Iterator getExceptionIterator() {
		return iURLExceptions==null ? null : iURLExceptions.iterator();
	}
	
	/**
	 * getURLEntries
	 * @return list of ServiceURLs
	 */
	public List getURLEntries() { return iURLEntries; }
	
	/**
	 * getURLExceptions
	 * @return list of URL Exceptions
	 */
	public List getURLExceptions() { return iURLExceptions; }
	

	protected boolean serializeBody(SLPOutputStream pOutStr, SerializeOption pOption) {
		return pOutStr.write16(getErrorCode()) && pOutStr.writeURLList(iURLEntries);
	}
	
}