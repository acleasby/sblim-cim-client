/**
 * SAAdvert.java
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
 *    0                   1                   2                   3
 *    0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
 *   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *   |        Service Location header (function = SAAdvert = 11)     |
 *   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *   |         Length of URL         |              URL              \
 *   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *   |     Length of <scope-list>    |         <scope-list>          \
 *   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *   |     Length of <attr-list>     |          <attr-list>          \
 *   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *   | # auth blocks |        authentication block (if any)          \
 *   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *
 */

/**
 * SAAdvert message 
 *
 */
public class SAAdvert extends ReplyMessage {
	
	private String iURLStr;
	
	private List iScopeList, iAttrList;
	
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
		return new SAAdvert(
			pHdr, pInStr.readString(), pInStr.readStringList(),
			pInStr.readAttributeList()
		);
	}
	
	/**
	 * Ctor.
	 * @param pURLStr
	 * @param pScopeList - list of scope strings
	 * @param pAttrList - list of ServiceLocationAttributes
	 */
	public SAAdvert(
		String pURLStr, List pScopeList, List pAttrList
	) {
		super(SA_ADVERT, 0);
		init(pURLStr, pScopeList, pAttrList);
	}
	
	/**
	 * Ctor.
	 * @param pLangTag
	 * @param pURLStr
	 * @param pScopeList - list of scope strings
	 * @param pAttrList - list of ServiceLocationAttributes
	 */
	public SAAdvert(
		String pLangTag, String pURLStr, List pScopeList, List pAttrList
	) {
		super(SA_ADVERT, pLangTag, 0);
		init(pURLStr, pScopeList, pAttrList);
	}
	
	/**
	 * Ctor.
	 * @param pHeader
	 * @param pURLStr
	 * @param pScopeList - list of scope strings
	 * @param pAttrList - list of ServiceLocationAttributes
	 */
	public SAAdvert(
		MsgHeader pHeader, String pURLStr, List pScopeList, List pAttrList
	) {
		super(pHeader, 0);
		init(pURLStr, pScopeList, pAttrList);
	}
	
	public Iterator getResultIterator() {
		ArrayList list = new ArrayList();
		list.add(iURLStr);
		return list.iterator();
	}

	protected boolean serializeBody(SLPOutputStream pOutStr, SerializeOption pOption) {
		return
			pOutStr.write(iURLStr) &&
			pOutStr.writeStringList(iScopeList) &&
			pOutStr.writeAttributeList(iAttrList);
	}
	
	private void init(
		String pURLStr, List pScopeList, List pAttrList
	) {
		iURLStr = pURLStr; iScopeList =  pScopeList; iAttrList = pAttrList;
	}

	public Iterator getExceptionIterator() {
		// this message doesn't have exception table
		return null;
	}
	
}