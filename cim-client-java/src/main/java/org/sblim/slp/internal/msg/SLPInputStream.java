/**
 * SLPInputStream.java
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;

import org.sblim.slp.ServiceLocationAttribute;
import org.sblim.slp.ServiceLocationException;
import org.sblim.slp.ServiceType;
import org.sblim.slp.ServiceURL;
import org.sblim.slp.internal.Convert;
import org.sblim.slp.internal.SLPDefaults;
import org.sblim.slp.internal.TRC;

/**
 * Helps the parsing of the bytes of SLP messages. 
 *
 */
public class SLPInputStream {
	
	private InputStream iInStr;
	
	private final byte[] iBBuf = new byte[4];
	
	/**
	 * Ctor.
	 * @param pBytes
	 */
	public SLPInputStream(byte[] pBytes) {
		this(pBytes, 0, pBytes.length);
	}
	
	/**
	 * Ctor.
	 * @param pSock
	 * @throws IOException
	 */
	public SLPInputStream(Socket pSock) throws IOException {
		this(pSock.getInputStream());
	}
	
	/**
	 * Ctor.
	 * @param pInStr
	 */
	public SLPInputStream(InputStream pInStr) {
		iInStr = pInStr;
	}
	
	/**
	 * Ctor.
	 * @param pPacket
	 */
	public SLPInputStream(DatagramPacket pPacket) {
		this(pPacket.getData(),pPacket.getOffset(), pPacket.getLength());
	}
	
	/**
	 * Ctor.
	 * @param pBytes
	 * @param pOffset
	 * @param pLength
	 */
	public SLPInputStream(byte[] pBytes, int pOffset, int pLength) {
		iInStr = new ByteArrayInputStream(pBytes, pOffset, pLength);
	}
	
	/**
	 * readString
	 * @return String
	 * @throws ServiceLocationException
	 * @throws IOException
	 */
	public String readString() throws ServiceLocationException, IOException {
		return Convert.unescape(readRawString());
	}
	
	/**
	 * readStringSet
	 * @return SortedSet of Strings
	 * @throws ServiceLocationException
	 * @throws IOException
	 */
	public SortedSet readStringSet() throws ServiceLocationException, IOException {
		SortedSet set = new TreeSet();
		readStringCollection(set);
		return set;
	}
	
	/**
	 * readStringList
	 * @return List of Strings
	 * @throws ServiceLocationException
	 * @throws IOException
	 */
	public List readStringList() throws ServiceLocationException, IOException {
		ArrayList strList = new ArrayList();
		readStringCollection(strList);
		return strList;
	}
	
	/**
	 * readAttribute
	 * @return ServiceLocationAttribute
	 * @throws ServiceLocationException
	 * @throws IOException
	 */
	public ServiceLocationAttribute readAttribute() throws ServiceLocationException, IOException {
		String str = readRawString();
		return str == null ? null: new ServiceLocationAttribute(str);
	}
	
	/**
	 * readAttributeList
	 * @return List of ServiceLocationAttributes
	 * @throws ServiceLocationException
	 * @throws IOException
	 */
	public List readAttributeList() throws ServiceLocationException, IOException {
		String str = readRawString();
		return str == null ? null: new AttrListParser(str).getList();
	}
	
	/**
	 * # of AttrAuths |(if present) Attribute Authentication Blocks...
	 * @return null
	 * @throws ServiceLocationException
	 * @throws IOException 
	 */
	public List readAuthBlockList() throws ServiceLocationException, IOException {
		Integer blockCntInt = doRead8();
		if (blockCntInt == null) return null;
		int blockCnt = blockCntInt.intValue();
		if (blockCnt != 0) throw new ServiceLocationException(
			ServiceLocationException.NOT_IMPLEMENTED,
			"Handling of authentication blocks is not implemented! blockCount = "+blockCnt
		);
		return null;
	}
	
	/*
	 *  0                   1                   2                   3
     *  0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
     *  +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     *  |   Reserved    |          Lifetime             |   URL Length  |
     *  +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     *  |URL len, contd.|            URL (variable length)              \
     *  +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     *  |# of URL auths |            Auth. blocks (if any)              \
     *  +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     */
    /**
	 * @return ServiceURL
	 * @throws ServiceLocationException
     * @throws IOException 
	 */
	public ServiceURL readURL() throws ServiceLocationException, IOException {
		if (doRead8() == null) return null; // skip reserved
		Integer lifeTimeInt = doRead16();
		if (lifeTimeInt == null) return null;
		int lifeTime = lifeTimeInt.intValue();
		String urlStr = readString();
		if (urlStr == null) return null;
		Integer numOfAuthsInt = doRead8();
		if (numOfAuthsInt == null) return null;
		int numOfAuths = numOfAuthsInt.intValue();
		while (numOfAuths-- > 0) { TRC.warning("readAuth"); readString(); }
		return new ServiceURL(urlStr, lifeTime);
	}
	
	/**
	 * readUrlList
	 * @return List of valid ServiceURLs
	 * @throws ServiceLocationException
	 * @throws IOException
	 * 
	 * Add URL to list only if it is valid URL i.e. no exception is thrown by parser
	 * 
	 */
	public List readUrlList(List pURLExceptions) throws ServiceLocationException, IOException {
		Integer cntInt = doRead16();
		if (cntInt == null) return null;
		int cnt = cntInt.intValue();
		ArrayList urlList = new ArrayList(cnt);
		ServiceURL url ;
		while (cnt-- > 0) {
			try {
				url = readURL();
				if (url == null) break; 
				urlList.add(url);
			}
			catch (IllegalArgumentException e) {
				pURLExceptions.add(e);
				TRC.warning("Ignoring Invalid URL : "+ e.getMessage());
			}			
		}
		return urlList;
	}
	
	/**
	 * readServiceType
	 * @return ServiceType
	 * @throws ServiceLocationException
	 * @throws IOException
	 */
	public ServiceType readServiceType() throws ServiceLocationException, IOException {
		String str = readString();
		return str == null ? null : new ServiceType(str);
	}
	
	/**
	 * readServTypeList
	 * @return List of ServiceTypes
	 * @throws ServiceLocationException
	 * @throws IOException
	 */
	public List readServTypeList() throws ServiceLocationException, IOException {
		Iterator strItr = readStringList().iterator();
		ArrayList srvTypeList = new ArrayList();
		while (strItr.hasNext()) {
			srvTypeList.add(new ServiceType((String)strItr.next()));
		}
		return srvTypeList;
	}
	
	/**
	 * read8
	 * @return int
	 * @throws ServiceLocationException
	 * @throws IOException
	 */
	public int read8() throws ServiceLocationException, IOException {
		Integer res = doRead8();
		if (res == null) throw new ServiceLocationException(
			ServiceLocationException.PARSE_ERROR, "Failed to read byte field!"
		);
		return res.intValue();
	}
	
	/**
	 * read16
	 * @return int
	 * @throws ServiceLocationException
	 * @throws IOException
	 */
	public int read16() throws ServiceLocationException, IOException {
		Integer res = doRead16();
		if (res == null) throw new ServiceLocationException(
			ServiceLocationException.PARSE_ERROR, "Failed to read 2-byte-long field!"
		);
		return res.intValue();
	}
	
	/**
	 * read24
	 * @return int
	 * @throws ServiceLocationException
	 * @throws IOException
	 */
	public int read24() throws ServiceLocationException, IOException {
		Integer res = doRead24();
		if (res == null) throw new ServiceLocationException(
			ServiceLocationException.PARSE_ERROR, "Failed to read 3-byte-long field!"
		);
		return res.intValue();
	}
	
	/**
	 * read32
	 * @return long
	 * @throws ServiceLocationException
	 * @throws IOException
	 */
	public long read32() throws ServiceLocationException, IOException {
		Long res = doRead32();
		if (res == null) throw new ServiceLocationException(
			ServiceLocationException.PARSE_ERROR, "Failed to read 4-byte-long field!"
		);
		return res.longValue();
	}
	
	private Integer doRead8() throws IOException {
		int res = iInStr.read();
		return res < 0 ? null : new Integer(res);
	}
	
	private Integer doRead16() throws IOException {
		int cnt = iInStr.read(iBBuf, 0, 2);
		if (cnt != 2) return null;
		return new Integer((iBBuf[0]&0xff)<<8 | iBBuf[1]&0xff);
	}
	
	private Integer doRead24() throws IOException {
		int cnt = iInStr.read(iBBuf, 0, 3);
		if (cnt != 3) return null;
		return new Integer((iBBuf[0]&0xff)<<16 | (iBBuf[1]&0xff)<<8 | iBBuf[2]&0xff);
	}
	
	private Long doRead32() throws IOException {
		int cnt = iInStr.read(iBBuf, 0, 4);
		if (cnt != 4) return null;
		long res = (iBBuf[0] & 0xff)<<8;
		res |= (iBBuf[1] & 0xff)<<8;
		res |= (iBBuf[2] & 0xff)<<8;
		res |= iBBuf[3] & 0xff;
		return new Long(res);
	}

	private String readRawString() throws ServiceLocationException, IOException {
		Integer lenInt = doRead16();
		if (lenInt == null) return null;
		int len = lenInt.intValue();
		if (len <= 0) return null;
		byte[] bytes = new byte[len];
		int read = iInStr.read(bytes, 0, len);
		if (read != len) return null;
		try {
			return new String(bytes, SLPDefaults.ENCODING);
		} catch (UnsupportedEncodingException e) {
			throw new ServiceLocationException(
				ServiceLocationException.INTERNAL_SYSTEM_ERROR, e
			);
		}
	}
	
	private void readStringCollection(
		Collection pCol
	) throws ServiceLocationException, IOException {
		String rawListStr = readRawString();
		if (rawListStr == null) return;
		StringTokenizer tokenizer = new StringTokenizer(rawListStr, ",");
		while (tokenizer.hasMoreElements())
			pCol.add(Convert.unescape(tokenizer.nextToken()));
	}
	
	private static class AttrListParser {
		
		private int iPos = 0;
		private String iAttrListStr;
		private ArrayList iList = new ArrayList();
		
		/**
		 * Ctor.
		 * @param pAttrListStr
		 * @throws ServiceLocationException
		 */
		public AttrListParser(String pAttrListStr) throws ServiceLocationException {
			debug("attrListStr="+pAttrListStr);
			iAttrListStr = pAttrListStr;
			String attrStr;
			while ((attrStr=readEntry())!=null) {
				debug("attrStr="+attrStr);
				iList.add(new ServiceLocationAttribute(attrStr));
			}
		}
		
		/**
		 * getList
		 * @return List of ServiceLocationAttributes
		 */
		public List getList() { return iList; }
		
		/*
		 *  ( "(" attrID "=" ( value "," )* value ")" ("," / EndOfString ) ) /
		 *  attrID ("," / EndOfString )
		 */
		private String readEntry() throws ServiceLocationException {
			if (iAttrListStr == null) return null;
			int lastIdx = iAttrListStr.length() - 1;
			if (iPos == lastIdx) return null;
			boolean inBlock = false;
			int startPos = iPos;
			while (true) {
				char ch = iAttrListStr.charAt(iPos);
				if (ch == '(') {
					if (inBlock || iPos != startPos) throw new ServiceLocationException(
						ServiceLocationException.PARSE_ERROR, invalidChar('(')
					);
					inBlock = true;
				} else if (ch == ')') {
					if (!inBlock) throw new ServiceLocationException(
						ServiceLocationException.PARSE_ERROR, invalidChar(')')
					);
					if (iPos == lastIdx) return iAttrListStr.substring(startPos);
					inBlock = false;
				} else {
					if (inBlock) {
						if (iPos == lastIdx)
							/*throw new ServiceLocationException(
								ServiceLocationException.PARSE_ERROR,
								"There is no ')' for '(' !"
							);*/
							return iAttrListStr.substring(startPos);
					} else {
						if (ch == ',') {
							++iPos; return iAttrListStr.substring(startPos, iPos-1);
						}
						if (iPos == lastIdx) {
							return iAttrListStr.substring(startPos);
						}
					}
				}
				if (iPos == lastIdx) throw new ServiceLocationException(
					ServiceLocationException.PARSE_ERROR,
					"Unexpected end of Attribute list:\n"+iAttrListStr
				);
				++iPos;
			}
		}
		
		private String invalidChar(char ch) {
			return
				"Invalid '(' character in Attribute list:\n"+iAttrListStr+
				"\nat position: "+iPos;
		}
		
	}
	
	static void debug(String pMsg) {
		//System.out.println(pMsg);
	}
	
}