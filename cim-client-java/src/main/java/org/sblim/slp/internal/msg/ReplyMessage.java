/**
 * ReplyMessage.java
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
 * 1913348    2008-04-08  raman_arora  Malformed service URL crashes SLP discovery
 * 2807325    2009-06-22  blaschke-oss Change licensing from CPL to EPL
 */


package org.sblim.slp.internal.msg;

import java.util.Iterator;

/**
 * ReplyMessage
 *
 */
public abstract class ReplyMessage extends SLPMessage {

	private int iErrorCode;
	
	/**
	 * Ctor.
	 * @param pFunctionID
	 * @param pErrorCode
	 */
	public ReplyMessage(int pFunctionID, int pErrorCode) {
		super(pFunctionID); iErrorCode = pErrorCode;
	}
	
	/**
	 * Ctor.
	 * @param pFunctionID
	 * @param pLangTag
	 * @param pErrorCode
	 */
	public ReplyMessage(int pFunctionID, String pLangTag, int pErrorCode) {
		super(pFunctionID, pLangTag); iErrorCode = pErrorCode;
	}
	
	/**
	 * Ctor.
	 * @param pHeader
	 * @param pErrorCode
	 */
	public ReplyMessage(MsgHeader pHeader, int pErrorCode) {
		super(pHeader); iErrorCode =  pErrorCode;
	}
	
	/**
	 * getErrorCode
	 * @return int
	 */
	public int getErrorCode() { return iErrorCode; }
	
	/**
	 * getResultIterator
	 * @return Iterator
	 */
	public abstract Iterator getResultIterator();
	
	/**
	 * getExceptionIterator
	 * @return Iterator
	 */
	public abstract Iterator getExceptionIterator();
	
}