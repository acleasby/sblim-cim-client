/**
 * ServiceType.java
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
 * Change History
 * Flag       Date        Prog         Description
 *------------------------------------------------------------------------------- 
 * 1516246    2006-07-22  lupusalex    Integrate SLP client code
 * 1804402    2007-09-28  ebak         IPv6 ready SLP
 * 2807325    2009-06-22  blaschke-oss Change licensing from CPL to EPL
 */


package org.sblim.slp;


import java.io.Serializable;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

/**
 * The ServiceType object models the SLP service type. It parses a string based
 * service type specifier into its various components, and contains property
 * accessors to return the components. URL schemes, protocol service types, and
 * abstract service types are all handled.
 */
public class ServiceType implements Serializable {

	private static final long serialVersionUID = -4850546870881037017L;

	private boolean iIsServiceURL = true;

	private String iPrincipleType = "";

	private String iAbstractType = "";

	private String iNamingAuthority = "";

	/**
	 * 
	 * Constructs a service type object from the service type specifier. Throws
	 * IllegalArgumentException if the type name is syntactically incorrect.
	 * 
	 * @param pType
	 *            The service type name as a String. If the service type is from
	 *            a service: URL, the "service:" prefix must be intact.
	 */
	public ServiceType(String pType) {
		parse(pType);
	}

	/**
	 * Returns true if the type name contains the "service:" prefix.
	 * 
	 * @return <code>true</code> if the type name contains the "service:"
	 *         prefix
	 */
	public boolean isServiceURL() {
		return iIsServiceURL;
	}

	/**
	 * Returns true if the type name is for an abstract type.
	 * 
	 * @return <code>true</code> if the type name is for an abstract type
	 */
	public boolean isAbstractType() {
		return iAbstractType.length() > 0;
	}

	/**
	 * Returns true if the naming authority is the default, i.e. is the empty
	 * string.
	 * 
	 * @return <code>true</code> if the naming authority is the default, i.e.
	 *         is the empty string
	 */
	public boolean isNADefault() {
		return iNamingAuthority.length() <= 0;
	}

	/**
	 * Returns the concrete type name in an abstract type, or the empty string
	 * if the service type is not abstract. For example, if the type name is
	 * "service:printing:ipp", the method returns "ipp". If the type name is
	 * "service:ftp", the method returns "".
	 * 
	 * @return <code>true</code> if the service type is not abstract
	 */
	public String getConcreteTypeName() {
		return iAbstractType;
	}

	/**
	 * Returns the abstract type name for an abstract type, the protocol name in
	 * a protocol type, or the URL scheme for a generic URL. For example, in the
	 * abstract type name "service:printing:ipp", the method returns "printing".
	 * In the protocol type name "service:ftp", the method returns "ftp".
	 * 
	 * @return The principle type name
	 */
	public String getPrincipleTypeName() {
		return iPrincipleType;
	}

	/**
	 * If the type is an abstract type, returns the fully formatted abstract
	 * type name including the "service:" and naming authority but without the
	 * concrete type name or intervening colon. If not an abstract type, returns
	 * the empty string. For example, in the abstract type name
	 * "service:printing:ipp", the method returns "service:printing".
	 * 
	 * @return The abstract type name
	 */
	public String getAbstractTypeName() {
		if (isAbstractType()) return "service:" + iPrincipleType
				+ (iNamingAuthority.length() <= 0 ? "" : "." + iNamingAuthority);
		return "";
	}

	/**
	 * Return the naming authority name, or the empty string if the naming
	 * authority is the default.
	 * 
	 * @return The naming authority
	 */
	public String getNamingAuthority() {
		return iNamingAuthority;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 * 
	 * Overrides Object.equals(). The two objects are equal if they are both
	 * ServiceType objects and the components of both are equal.
	 */
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (!(obj instanceof ServiceType)) return false;

		ServiceType servicetype = (ServiceType) obj;
		return iIsServiceURL == servicetype.iIsServiceURL
				&& iPrincipleType.equals(servicetype.iPrincipleType)
				&& iAbstractType.equals(servicetype.iAbstractType)
				&& iNamingAuthority.equals(servicetype.iNamingAuthority);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 * 
	 * Returns the fully formatted type name, including the "service:" if the
	 * type was originally from a service: URL.
	 */
	public String toString() {
		// TODO: clean up this
		return (iIsServiceURL ? "service:" : "") + iPrincipleType
				+ (iNamingAuthority.length() <= 0 ? "" : "." + iNamingAuthority)
				+ (iAbstractType.length() <= 0 ? "" : ":" + iAbstractType);
	}

	
	private int iHashCode = 0;
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 * 
	 * Overrides Object.hashCode(). Hashes on the string value of the "service"
	 * prefix, naming authority, if any, abstract and concrete type names for
	 * abstract types, protocol type name for protocol types, and URL scheme for
	 * generic URLs.
	 */
	public int hashCode() {
		if (iHashCode == 0)
			iHashCode = toString().hashCode();
		return iHashCode;
	}

	private void parse(String pString) {
		StringTokenizer st = new StringTokenizer(pString, ":.", true);
		while (true) {
			try {

				String token = st.nextToken();
				if (token.equals(":") || token.equals(".")) continue;

				if (!token.equalsIgnoreCase("service")) {
					iIsServiceURL = false;
					do {
						iPrincipleType = iPrincipleType + token.toLowerCase();
						if (!st.hasMoreTokens()) break;
						token = st.nextToken();
					} while (true);

					validateTypeComponent(iPrincipleType);
					if (!st.hasMoreTokens()) return;
					continue;
				}
				token = st.nextToken();
				if (!token.equals(":")) continue;
				iPrincipleType = st.nextToken().toLowerCase();
				validateTypeComponent(iPrincipleType);

				if (!st.hasMoreTokens()) return;
				token = st.nextToken();
				if (token.equals(".")) {
					token = st.nextToken();
					validateTypeComponent(token);
					if (token.equalsIgnoreCase("iana")) continue;
					iNamingAuthority = token.toLowerCase();
					if (!st.hasMoreTokens()) return;
					token = st.nextToken();
				}
				if (token.equals(":")) {
					String abstractTypeToken = st.nextToken();
					validateTypeComponent(abstractTypeToken);
					iAbstractType = abstractTypeToken.toLowerCase();
					if (!st.hasMoreTokens()) return;
				}
			} catch (NoSuchElementException e) {
				throw new IllegalArgumentException(e.getMessage());
			}
		}
	}

	private static void validateTypeComponent(String str) {
		int length = str.length();

		for (int pos = 0; pos < length; pos++) {
			char ch = str.charAt(pos);
			if (!Character.isLetterOrDigit(ch) && ch != '+' && ch != '-') { throw new IllegalArgumentException(); }
		}
	}
}
