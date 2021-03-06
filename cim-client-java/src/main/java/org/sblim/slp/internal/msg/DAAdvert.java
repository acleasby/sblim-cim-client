/**
 * DAAdvert.java
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
import java.util.TreeSet;

import org.sblim.slp.ServiceLocationException;

/*
 * 0                   1                   2                   3
 * 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |        Service Location header (function = DAAdvert = 8)      |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |          Error Code           |  DA Stateless Boot Timestamp  |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |DA Stateless Boot Time,, contd.|         Length of URL         |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * \                              URL                              \
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |     Length of <scope-list>    |         <scope-list>          \
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |     Length of <attr-list>     |          <attr-list>          \
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |    Length of <SLP SPI List>   |     <SLP SPI List> String     \
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * | # Auth Blocks |         Authentication block (if any)         \
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * FIXME: is this URL an URL-entry or an URL String.
 * Assuming URL String.
 */


/**
 * DAAdvert message 
 *
 */
public class DAAdvert extends ReplyMessage {
	
	private long iStatelessBootTime;
	
	private String iURLStr;
	
	private List iScopeList, iAttrList, iSPIList;
	
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
		return new DAAdvert(
			pHdr, pInStr.read16(), pInStr.read32(), pInStr.readString(),
			pInStr.readStringList(), pInStr.readAttributeList(),
			pInStr.readStringList()
		);
	}
	
	/**
	 * Ctor.
	 * @param pErrorCode
	 * @param pStatelessBootTime
	 * @param pURLStr
	 * @param pScopeList - list of scope strings
	 * @param pAttrList  - list of ServiceLocationAttributes
	 * @param pSPIList
	 */
	public DAAdvert(
		int pErrorCode, long pStatelessBootTime, String pURLStr, List pScopeList,
		List pAttrList, List pSPIList
	) {
		super(DA_ADVERT, pErrorCode);
		init(pStatelessBootTime, pURLStr, pScopeList, pAttrList, pSPIList);
	}
	
	/**
	 * Ctor.
	 * @param pLangTag
	 * @param pErrorCode
	 * @param pStatelessBootTime
	 * @param pURLStr
	 * @param pScopeList - list of scope strings
	 * @param pAttrList  - list of ServiceLocationAttributes
	 * @param pSPIList
	 */
	public DAAdvert(
		String pLangTag, int pErrorCode,
		long pStatelessBootTime, String pURLStr, List pScopeList,
		List pAttrList, List pSPIList
	) {
		super(DA_ADVERT, pLangTag, pErrorCode);
		init(pStatelessBootTime, pURLStr, pScopeList, pAttrList, pSPIList);
	}
	
	/**
	 * Ctor.
	 * @param pHeader
	 * @param pErrorCode
	 * @param pStatelessBootTime
	 * @param pURLStr
	 * @param pScopeList - list of scope strings
	 * @param pAttrList  - list of ServiceLocationAttributes
	 * @param pSPIList
	 */
	public DAAdvert(
		MsgHeader pHeader, int pErrorCode,
		long pStatelessBootTime, String pURLStr, List pScopeList,
		List pAttrList, List pSPIList
	) {
		super(pHeader, pErrorCode);
		init(pStatelessBootTime, pURLStr, pScopeList, pAttrList, pSPIList);
	}

	/**
	 * @return Iterator of DADescriptors
	 * @see org.sblim.slp.internal.msg.ReplyMessage#getResultIterator()
	 */
	public Iterator getResultIterator() {
		ArrayList list = new ArrayList(1);
		list.add(new DADescriptor(iURLStr, new TreeSet(iScopeList), iAttrList));
		return list.iterator();
	}

	protected boolean serializeBody(SLPOutputStream pOutStr, SerializeOption pOption) {
		return 
			pOutStr.write16(getErrorCode()) &&
			pOutStr.write32(iStatelessBootTime) &&
			pOutStr.write(iURLStr) &&
			pOutStr.writeStringList(iScopeList) &&
			pOutStr.writeAttributeList(iAttrList) &&
			pOutStr.writeStringList(iSPIList);
	}
	
	private void init(
		long pStatelessBootTime, String pURLStr, List pScopeList, List pAttrList,
		List pSPIList
	) {
		iStatelessBootTime = pStatelessBootTime; iURLStr = pURLStr;
		iScopeList =  pScopeList;
		iAttrList = pAttrList; iSPIList = pSPIList;
	}

	public Iterator getExceptionIterator() {
		// this message doesn't have exception table
		return null;
	}
	
}