/**
 * PersistentOutputStream.java
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
 * 1535756    2006-08-07  lupusalex    Make code warning free
 * 2807325    2009-06-22  blaschke-oss Change licensing from CPL to EPL
 *
 */
package org.sblim.wbem.http.io;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class PersistentOutputStream extends FilterOutputStream {

	boolean iClosable = false;

	boolean iClosed = false;

	public PersistentOutputStream(OutputStream os) {
		this(os, false);
	}

	public PersistentOutputStream(OutputStream os, boolean closable) {
		super(os);
		this.iClosable = closable;
	}

	public synchronized void close() throws IOException {
		if (!iClosed) {
			iClosed = true;
			if (iClosable) out.close();
		} else throw new IOException("Error while closing the Output stream. It was already closed");
	}
}
