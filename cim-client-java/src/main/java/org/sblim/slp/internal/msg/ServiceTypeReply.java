/**
 * ServiceTypeReply.java
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
import java.util.Iterator;
import java.util.List;

import org.sblim.slp.ServiceLocationException;

/*
 *  0                   1                   2                   3
 *  0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
 *  +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *  |      Service Location header (function = SrvTypeRply = 10)    |
 *  +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *  |           Error Code          |    length of <srvType-list>   |
 *  +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *  |                       <srvtype--list>                         \
 *  +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 */
/**
 * ServiceTypeReply message
 *
 */
public class ServiceTypeReply extends ReplyMessage {
	
	private List iServTypeList;

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
		return new ServiceTypeReply(
			pHdr, pInStr.read16(), pInStr.readServTypeList()
		);
	}
	
	/**
	 * Ctor.
	 * @param pErrorCode
	 * @param pServTypeList - list of ServiceTypes
	 */
	public ServiceTypeReply(int pErrorCode, List pServTypeList) {
		super(SRV_TYPE_RPLY, pErrorCode); iServTypeList = pServTypeList;
	}
	
	/**
	 * Ctor.
	 * @param pLangTag
	 * @param pErrorCode
	 * @param pServTypeList - list of ServiceTypes
	 */
	public ServiceTypeReply(String pLangTag, int pErrorCode, List pServTypeList) {
		super(SRV_TYPE_RPLY, pLangTag, pErrorCode);
		iServTypeList = pServTypeList;
	}
	
	/**
	 * Ctor.
	 * @param pHeader
	 * @param pErrorCode
	 * @param pServTypeList - list of ServiceTypes
	 */
	public ServiceTypeReply(MsgHeader pHeader, int pErrorCode, List pServTypeList) {
		super(pHeader, pErrorCode); iServTypeList = pServTypeList;
	}

	public Iterator getResultIterator() {
		return iServTypeList==null ? null : iServTypeList.iterator();
	}

	protected boolean serializeBody(SLPOutputStream pOutStr, SerializeOption pOption) {
		return
			pOutStr.write16(getErrorCode()) &&
			pOutStr.writeServTypeList(getResultIterator());
	}

	public Iterator getExceptionIterator() {
		// this message doesn't have exception table
		return null;
	}
	
}