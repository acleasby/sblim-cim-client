/**
 * ServiceRegistration.java
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
 * 2807325    2009-06-22  blaschke-oss Change licensing from CPL to EPL
 */


package org.sblim.slp.internal.msg;

import java.io.IOException;
import java.util.List;

import org.sblim.slp.ServiceLocationException;
import org.sblim.slp.ServiceURL;

/*
 * 0                   1                   2                   3
 * 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |         Service Location header (function = SrvReg = 3)       |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |                          <URL-Entry>                          \
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * | length of service type string |        <service-type>         \
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |     length of <scope-list>    |         <scope-list>          \
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |  length of attr-list string   |          <attr-list>          \
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |# of AttrAuths |(if present) Attribute Authentication Blocks...\
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 */
/**
 * ServiceRegistration message 
 *
 */
public class ServiceRegistration extends SLPMessage {
	
	private ServiceURL iServURL;
	private List iScopeList;
	private List iAttrList;
	private List iAuthBlockList;
	
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
		ServiceURL url = pInStr.readURL();
		pInStr.readServiceType(); // FIXME reading dummy SrvType. Correct?
		return new ServiceRegistration(
			pHdr,
			url,
			pInStr.readStringList(),
			pInStr.readAttributeList(),
			pInStr.readAuthBlockList()
		);
	}

	/**
	 * Ctor.
	 * @param pServURL
	 * @param pScopeList - list of scope strings
	 * @param pAttrList - list of ServiceLocationAttributes
	 * @param pAuthBlockList
	 */
	public ServiceRegistration(
		ServiceURL pServURL, List pScopeList, List pAttrList,
		List pAuthBlockList
	) {
		super(SRV_REG);
		init(pServURL, pScopeList, pAttrList, pAuthBlockList);
	}
	
	/**
	 * Ctor.
	 * @param pLangTag
	 * @param pServURL
	 * @param pScopeList - list of scope strings
	 * @param pAttrList - list of ServiceLocationAttributes
	 * @param pAuthBlockList
	 */
	public ServiceRegistration(
		String pLangTag, ServiceURL pServURL, List pScopeList,
		List pAttrList, List pAuthBlockList
	) {
		super(SRV_REG, pLangTag);
		init(pServURL, pScopeList, pAttrList, pAuthBlockList);
	}
	
	/**
	 * Ctor.
	 * @param pHeader
	 * @param pServURL
	 * @param pScopeList - list of scope strings
	 * @param pAttrList - list of ServiceLocationAttributes
	 * @param pAuthBlockList
	 */
	public ServiceRegistration(
		MsgHeader pHeader, ServiceURL pServURL,
		List pScopeList, List pAttrList, List pAuthBlockList
	) {
		super(pHeader);
		init(pServURL, pScopeList, pAttrList, pAuthBlockList);
	}
	
	/**
	 * getServiceURL
	 * @return ServiceURL
	 */
	public ServiceURL getServiceURL() { return iServURL; }
	
	/**
	 * getScopeList
	 * @return List
	 */
	public List getScopeList() { return iScopeList; }
	
	/**
	 * getAttributeList
	 * @return List
	 */
	public List getAttributeList() { return iAttrList; }

	protected boolean serializeBody(SLPOutputStream pOutStr, SerializeOption pOption) {
		return
			pOutStr.write(iServURL) &&
			pOutStr.write(iServURL.getServiceType()) &&
			pOutStr.writeStringList(iScopeList) &&
			pOutStr.writeAttributeList(iAttrList) &&
			pOutStr.writeAuthBlockList(iAuthBlockList);
	}
	
	private void init(
		ServiceURL pServURL, List pScopeList, List pAttrList,
		List pAuthBlockList
	) {
		iServURL = pServURL; iScopeList = pScopeList;
		iAttrList =  pAttrList; iAuthBlockList = pAuthBlockList;
	}
	
}