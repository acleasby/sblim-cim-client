/**
 * AttributeRequest.java
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
 * 1892103    2008-02-15  ebak         SLP improvements
 * 2807325    2009-06-22  blaschke-oss Change licensing from CPL to EPL
 */

package org.sblim.slp.internal.msg;

import java.io.IOException;
import java.util.List;
import java.util.SortedSet;

import org.sblim.slp.ServiceLocationException;
import org.sblim.slp.ServiceURL;

/*
 *    0                   1                   2                   3
 *    0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
 *   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *   |       Service Location header (function = AttrRqst = 6)       |
 *   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *   |       length of PRList        |        <PRList> String        \
 *   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *   |         length of URL         |              URL              \
 *   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *   |    length of <scope-list>     |      <scope-list> string      \
 *   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *   |  length of <tag-list> string  |       <tag-list> string       \
 *   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *   |   length of <SLP SPI> string  |        <SLP SPI> string       \
 *   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 */

/**
 * AttributeRequest message
 *
 */
public class AttributeRequest extends RequestMessage {
	
	private String iURLStr;
	
	private List iTagList, iSPIList;
	
	private static final int[] ALLOWED_RSPS = {
		ATTR_RPLY
	};

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
		return new AttributeRequest(
			pHdr, pInStr.readStringSet(), pInStr.readString(),
			pInStr.readStringList(),
			pInStr.readStringList(), pInStr.readStringList()
		);
	}
	
	/**
	 * Ctor.
	 * @param pPrevResponderSet - set of address strings
	 * @param pURLStr
	 * @param pScopeList - list of scope strings
	 * @param pTagList
	 * @param pSPIList
	 */
	public AttributeRequest(
		SortedSet pPrevResponderSet, 
		String pURLStr, List pScopeList, List pTagList, List pSPIList
	) {
		super(ATTR_RQST, pPrevResponderSet, pScopeList);
		init(pURLStr, pTagList, pSPIList);
	}
	
	/**
	 * Ctor.
	 * @param pLangTag
	 * @param pPrevResponderSet - set of address strings
	 * @param pURLStr
	 * @param pScopeList - list of scope strings
	 * @param pTagList
	 * @param pSPIList
	 */
	public AttributeRequest(
		String pLangTag, SortedSet pPrevResponderSet,
		String pURLStr, List pScopeList, List pTagList, List pSPIList
	) {
		super(ATTR_RQST, pLangTag, pPrevResponderSet, pScopeList);
		init(pURLStr, pTagList, pSPIList);
	}
	
	/**
	 * Ctor.
	 * @param pHeader
	 * @param pPrevResponderSet - set of address strings
	 * @param pURLStr
	 * @param pScopeList - list of scope strings
	 * @param pTagList
	 * @param pSPIList
	 */
	public AttributeRequest(
		MsgHeader pHeader, SortedSet pPrevResponderSet,
		String pURLStr, List pScopeList, List pTagList, List pSPIList
	) {
		super(pHeader, pPrevResponderSet, pScopeList);
		init(pURLStr, pTagList, pSPIList);
	}
	
	/**
	 * getServiceURL
	 * @return ServiceURL
	 */
	public ServiceURL getServiceURL() {
		return new ServiceURL(iURLStr, 65535);
	}
	
	protected boolean serializeRequestBody(SLPOutputStream pOutStr) {
		return
			pOutStr.write(iURLStr) &&
			pOutStr.writeStringList(getScopeList()) &&
			pOutStr.writeStringList(iTagList) &&
			pOutStr.writeStringList(iSPIList);
	}
	
	protected int[] getAllowedResponseIDs() {
		return ALLOWED_RSPS;
	}

	private void init(String pURLStr, List pTagList, List pSPIList) {
		iURLStr = pURLStr; iTagList = pTagList;
		iSPIList = pSPIList;
	}
	
}