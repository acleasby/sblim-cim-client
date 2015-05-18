/**
 * ServiceDeregistration.java
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
 *    0                   1                   2                   3
 *    0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
 *   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *   |         Service Location header (function = SrvDeReg = 4)     |
 *   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *   |    Length of <scope-list>     |         <scope-list>          \
 *   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *   |                           URL Entry                           \
 *   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *   |      Length of <tag-list>     |            <tag-list>         \
 *   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * The <tag-list> is a <string-list> of attribute tags to deregister as
 * defined in Section 9.4.  If no <tag-list> is present, the SrvDeReg
 * deregisters the service in all languages it has been registered in.
 * If the <tag-list> is present, the SrvDeReg deregisters the attributes
 * whose tags are listed in the tag spec.  Services registered with
 * Authentication Blocks MUST NOT include a <tag-list> in a SrvDeReg
 * message:  A DA will respond with an AUTHENTICATION_FAILED error in
 * this case.
 */

/**
 * ServiceDeregistration message
 *
 */
public class ServiceDeregistration extends SLPMessage {
	
	private List iScopeList;
	private ServiceURL iURL;
	private List iTagList;
	
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
		return new ServiceDeregistration(
			pHdr, pInStr.readStringList(), pInStr.readURL(),
			pInStr.readStringList()
		);
	}
	
	/**
	 * Ctor.
	 * @param pScopeList - list of scope strings
	 * @param pURL
	 * @param pTagList
	 */
	public ServiceDeregistration(List pScopeList, ServiceURL pURL, List pTagList) {
		super(SRV_DEREG);
		init(pScopeList, pURL, pTagList);
	}
	
	/**
	 * Ctor.
	 * @param pLangTag
	 * @param pScopeList - list of scope strings
	 * @param pURL
	 * @param pTagList
	 */
	public ServiceDeregistration(
		String pLangTag, List pScopeList, ServiceURL pURL, List pTagList
	) {
		super(SRV_DEREG, pLangTag);
		init(pScopeList, pURL, pTagList);
	}
	
	/**
	 * Ctor. used by message parser.
	 * @param pHeader
	 * @param pScopeList - list of scope strings
	 * @param pURL
	 * @param pTagList
	 */
	public ServiceDeregistration(
		MsgHeader pHeader, List pScopeList, ServiceURL pURL, List pTagList
	) {
		super(pHeader); init(pScopeList, pURL, pTagList);
	}
	
	/**
	 * getServiceURL
	 * @return ServiceURL
	 */
	public ServiceURL getServiceURL() { return iURL; }
	
	protected boolean serializeBody(SLPOutputStream pOutStr, SerializeOption pOption) {
		return
			pOutStr.writeStringList(iScopeList) &&
			pOutStr.write(iURL) &&
			pOutStr.writeStringList(iTagList);
	}
	
	private void init(List pScopeList, ServiceURL pURL, List pTagList) {
		iScopeList = pScopeList; iURL = pURL; iTagList = pTagList;
	}

}
