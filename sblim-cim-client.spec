%define name                    sblim-cim-client
%define archive_folder_name     cim-client
%define version                 1.3.9.3
%define release                 1jpp
%define section                 free
%define cim_client_jar_file     sblimCIMClient
%define slp_name              sblim-slp-client
%define slp_client_jar_file   sblimSLPClient

# -----------------------------------------------------------------------------

Name:           %{name}
Version:        %{version}
Release:        %{release}
Epoch:          0
License:        Eclipse Public License
Url:            http://sblim.sourceforge.net/
Group:          Development/Libraries/Java
Vendor:         JPackage Project
Distribution:   JPackage
Summary:        Java CIM Client library

BuildRoot:      %{_tmppath}/%{name}-%{version}-buildroot
SOURCE:         %{name}-%{version}-src.tar.bz2
SOURCE1:        %{name}-samples-%{version}-src.tar.bz2

BuildArch:      noarch

BuildRequires:  jpackage-utils >= 0:1.5.32
BuildRequires:  ant >= 0:1.6
BuildRequires:  dos2unix

Requires:       jpackage-utils >= 0:1.5.32

%description
The purpose of this package is to provide a CIM Client Class Library for Java
applications. It complies to the DMTF standard CIM Operations over HTTP and 
intends to be compatible with JCP JSR48 once it becomes available. To learn
more about DMTF visit http://www.dmtf.org.
More infos about the Java Community Process and JSR48 can be found at
http://www.jcp.org and http://www.jcp.org/en/jsr/detail?id=48.

# -----------------------------------------------------------------------------

%package javadoc
Summary:        Javadoc for %{name}
Group:          Development/Documentation

%description javadoc
Javadoc for %{name}.

# -----------------------------------------------------------------------------

%package manual
Summary:        Manual and sample code for %{name}
Group:          Development/Documentation

%description manual
Manual and sample code for %{name}.

# -----------------------------------------------------------------------------

%prep

%setup -q -n %{archive_folder_name}
%setup -q -T -D -b 1 -n %{archive_folder_name}

# -----------------------------------------------------------------------------

%build
export ANT_OPTS="-Xmx256m"
ant \
        -Dbuild.compiler=modern \
        -DManifest.version=%{version}\
        build-release

# -----------------------------------------------------------------------------

%install
rm -rf $RPM_BUILD_ROOT

# documentation 
dos2unix COPYING README ChangeLog NEWS
mkdir -p $RPM_BUILD_ROOT%{_docdir}/%{name}-%{version}
install COPYING $RPM_BUILD_ROOT%{_docdir}/%{name}-%{version}
install README $RPM_BUILD_ROOT%{_docdir}/%{name}-%{version}
install ChangeLog $RPM_BUILD_ROOT%{_docdir}/%{name}-%{version}
install NEWS $RPM_BUILD_ROOT%{_docdir}/%{name}-%{version}

# samples (also into _docdir)
pushd samples
  dos2unix README.samples
popd
install samples/README.samples $RPM_BUILD_ROOT%{_docdir}/%{name}-%{version}
cp -pr  samples/org $RPM_BUILD_ROOT%{_docdir}/%{name}-%{version}

# default cim.defaults
dos2unix cim.defaults slp.conf
mkdir -p $RPM_BUILD_ROOT%{_sysconfdir}/java
install cim.defaults $RPM_BUILD_ROOT%{_sysconfdir}/java/%{name}.properties
install slp.conf $RPM_BUILD_ROOT%{_sysconfdir}/java/%{slp_name}.properties

# jar
mkdir -p $RPM_BUILD_ROOT%{_javadir}
install %{archive_folder_name}/%{cim_client_jar_file}.jar $RPM_BUILD_ROOT%{_javadir}/%{name}-%{version}.jar
(
  cd $RPM_BUILD_ROOT%{_javadir} && 
    ln -sf %{name}-%{version}.jar %{cim_client_jar_file}.jar;
    ln -sf %{name}-%{version}.jar %{name}.jar;
)
install %{archive_folder_name}/%{slp_client_jar_file}.jar $RPM_BUILD_ROOT%{_javadir}/%{slp_name}-%{version}.jar
(
  cd $RPM_BUILD_ROOT%{_javadir} && 
    ln -sf %{slp_name}-%{version}.jar %{slp_client_jar_file}.jar;
    ln -sf %{slp_name}-%{version}.jar %{slp_name}.jar;
)

# javadoc
mkdir -p $RPM_BUILD_ROOT%{_javadocdir}/%{name}-%{version}
cp -pr %{archive_folder_name}/doc/* $RPM_BUILD_ROOT%{_javadocdir}/%{name}-%{version}



# -----------------------------------------------------------------------------

%files
%defattr(0644,root,root,0755)
%config %{_sysconfdir}/java/%{name}.properties
%config %{_sysconfdir}/java/%{slp_name}.properties
%doc %{_docdir}/%{name}-%{version}/COPYING
%doc %{_docdir}/%{name}-%{version}/README
%doc %{_docdir}/%{name}-%{version}/ChangeLog
%doc %{_docdir}/%{name}-%{version}/NEWS
%{_javadir}/%{name}.jar
%{_javadir}/%{name}-%{version}.jar
%{_javadir}/%{cim_client_jar_file}.jar
%{_javadir}/%{slp_name}.jar
%{_javadir}/%{slp_name}-%{version}.jar
%{_javadir}/%{slp_client_jar_file}.jar


%files javadoc
%defattr(0644,root,root,0755)
%{_javadocdir}/%{name}-%{version}


%files manual
%defattr(0644,root,root,0755)
%doc %{_docdir}/%{name}-%{version}/README.samples
%doc %{_docdir}/%{name}-%{version}/COPYING
%doc %{_docdir}/%{name}-%{version}/org


# -----------------------------------------------------------------------------

%changelog
* Mon Oct 31 2011 Dave Blaschke <blaschke@us.ibm.com> 
- Maintenance release 1.3.9.3
  o 3160431 Need to support property file on AIX.
  
* Thu Sep 30 2010 Dave Blaschke <blaschke@us.ibm.com> 
- Maintenance release 1.3.9.2
  o 3078216 Fix for a null pointer exception in 1.3.9.1

* Wed Aug 05 2009 Dave Blaschke <blaschke@us.ibm.com> 
- Maintenance release 1.3.9.1
  o 2832736 CIM Client does not recognize HTTP extension headers
  
* Mon Jun 22 2009 Dave Blaschke <blaschke@us.ibm.com> 
- New release 1.3.9
  o 2807325 Change licensing from CPL to EPL

* Fri Dec 12 2008 Dave Blaschke <blaschke@us.ibm.com> 
- New release 1.3.8
  o 2414525 SLPConfig : parseList not returning populated list
  o 2382765 HTTP header field Accept-Language does not include *
  o 2372679 Add property to control synchronized SSL handshaking
  o 2219646 Fix / clean code to remove compiler warnings

* Fri Sep 12 2008 Dave Blaschke <blaschke@us.ibm.com> 
- New release 1.3.7
  o 1984588 HttpClient not closed on cimclient close
  o 2023050 DateTime object not accounting for microseconds correctly
  o 1931266 M-POST not supported in java-client

* Thu Jun 12 2008 Dave Blaschke <blaschke@us.ibm.com> 
- New release 1.3.6
  o 1992321 1.3.6 packaging issues
  o 1954059 wrong path in CIMNameSpace URI
  o 1954069 connection leak and exception in CIMClientXML
  o 1960994 CIMSimpleDateTime.setDay() TODO and bad exception info
  o 1960934 CIMDateTime(Calendar) does not respect DST
  o 1901290 SLP error: "java.io.IOException" on Linux and IPv6
  o 1913348 malformed service URL crashes SLP discovery
  o 1931096 remove dependency of slpclient on cimclient classes
  o 1931332 In HTTPClient need to get status before closing connection
  o 1893499 no CIMExeption thrown for wrong namespace using SAXParser

* Tue Feb 26 2008 Alexander Wolf-Reber <a.wolf-reber@de.ibm.com>
- New release 1.3.5
  o 1892041 Basic/digest authentication problem for Japanese users
  o 1874440 sblimSLPclient PARSE ERROR
  o 1835847 property for configuration file location
  o 1832439 less strict parsing for IPv6 hostnames
  o 1824094 CIMNameSpace(String pURI) doesn't handle namespace properly
  o 1815752 TLS support
  o 1816503 IPv6 support 
  o 1804532 trace for both req/res should be traced in the same file
  o 1804402 IPv6 ready SLP

* Fri Aug 17 2007 Alexander Wolf-Reber <a.wolf-reber@de.ibm.com>
- New release 1.3.4
  o 1732645 Wrong reference building in METHODCALL request

* Fri May 25 2007 Alexander Wolf-Reber <a.wolf-reber@de.ibm.com> 
- New release 1.3.3
  o 1732645 Wrong reference building in METHODCALL request
  o 1705776 Regression: parseBigInteger() doesn't like zero
  o 1688270 Disable chunking because of trailer issues
  o 1676343 Remove dependency from Xerces
  o 1657901 Performance issues
  o 1660568 Chunking broken on SUN JRE
  o 1656285 IndicationHandler does not accept non-Integer message ID
  o 1649779 Indication listener threads freeze

* Thu Feb 22 2007 Alexander Wolf-Reber <a.wolf-reber@de.ibm.com>
- New release 1.3.2
  o 1649611 Interop issue: Quotes not escaped by client
  o 1649595 No chunking requested
  o 1647159 HttpClientPool runs out of HttpClients
  o 1647148 HttpClient.resetSocket() doesn't set socket timeout
  o 1646434 CIMClient close() invalidates all it's enumerations
  o 1637546 CIMEnumerationImpl has faulty close function
  o 1631407 VALUE.REFERENCE doesn't handle references without namespace
  o 1627832 Incorrect retry behaviour on HTTP 401
  o 1620526 Socket Leak in HTTPClient.getResponseCode()
  o 1610046 Does not escape trailing spaces <KEYVALUE>
  o 1604329 Fix OpenPegasus auth module
  o 1516242 Support of OpenPegasus local authentication
  o 1365086 Possible bug in createQualifier

* Mon Nov 13 2006 Alexander Wolf-Reber <a.wolf-reber@de.ibm.com>
- New release 1.3.1
  o 1565091 ssl handshake exception
  o 1574345 Client fails w/ NPE when processing chunked response
  o 1573723 Selection of JSSE provider via properties file not feasible
  o 1535793 Fix&Integrate CIM&SLP configuration classes
  o 1558663 Support custom socket factories in client connections
  o 1552457 NullPointer Exception while authenticating without PW
  o 1547910 parseIMETHODCALL() CIMObjectPath parsing problem
  o 1547908 parseKEYBINDING() creates incorrect reference type
  o 1545915 Wrong parsing of IMETHODCALL request
  o 1365082 Possible bugs in namespace creation
  o 1535756 Make code warning free
  o 1536711 NullPointerException causes client call to never return

* Thu Aug 3 2006 Alexander Wolf-Reber <a.wolf-reber@de.ibm.com> 
- New release 1.3
  o 1528233 Interoperability with Engenio CIMOM broken
  o 1523854 deep always false with ecn
  o 1516246 Integrate SLP client code
  o 1516244 GCJ support
  o 1514405 getInstance() returns a keyless CIMInstance
  o 1498938 Multiple events in single cim-xml request are not handled
  o 1498927 Fill gaps in logging coverage
  o 1498130 Selection of xml parser on a per connection basis
  o 1493639 XML Parsing of PARAMETER.ARRAY wrong
  o 1488846 Bad format Locale information send to CIM Server
  o 1487705 sblimCIMClient throws NumberFormatException in UnsignedInt64
  o 1486379 CIM client retries twice when HTTP/1.1 401 is returned
  o 1381768 CIMClient.close() faulty on HTTPClientPool
 
* Tue Jun 13 2006 Wolfgang Taphorn <taphorn@de.ibm.com> 
- New release 1.2.7-2
  o 1483394 Indication listener threads don't close (fix integration)

* Tue May 16 2006 Alexander Wolf-Reber <a.wolf-reber@de.ibm.com>
- New release 1.2.7
  o 1488924 Intermittent connection loss
  o 1483394 Indication listener threads don't close (broken fix)
  o 1483270 Using Several Cim Clients cause an indication problem
  o 1464860 No default value for VALUETYPE assumed
  o 1455939 CIMDataTime does not handle microsecond
  o 1438152 Wrong message ID in ExportResponseMessage
  o 1422316 Disable delayed acknowledgement

* Tue Dec 20 2005 Wolfgang Taphorn <taphorn@de.ibm.com>
- New release 1.2.6
  o 1377143 Mechanism to enable retries during transmission of request
  o 1381764 SBLIM Client getKey case sensitive
  o 1369307 CIM Client doesn't support embedded objects
  o 1375459 Upgrade for JPackage
  o 1365404 Log file gets created with logging disabled
  o 1362792 setUserPassword(char[]) does not clear old password
  o 1362783 PasswordCredential.getUserPassword expose char[] reference
  o 1362773 Possible NullPointerException in PassworCredential(char[])
  o 1353168 Possible NullPointerExcection in HttpClient.streamFinished()
  o 1353138 CIMObject element in HTTP header wrong formated
  o 1359805 UnsignedInt32.intValue() - Either Exception or method wrong
  o 1359684 Build: Separate SampleCode into own package

* Thu Nov 10 2005 Wolfgang Taphorn <taphorn@de.ibm.com>
- New release 1.2.5
  o 1339658 CIMEnumuration.finalize() closes used sockets
  o 1338684 Unsigned Data types cleanup + documentation
  o 1334388 Need tracing on CIMClient.close()
  o 1326969 Build improvements (was: ZIP archives do not contain... )

* Fri Oct 14 2005 Wolfgang Taphorn <taphorn@de.ibm.com>
- New release 1.2.4
  o 1325933 loadProperties(Properties) missing
  o 1312387 Retry if with POST request if 510 is received   
  o 1309551 Accept empty strings as credentials
 
* Wed Sep 28 2005 Wolfgang Taphorn <taphorn@de.ibm.com>
- New release 1.2.3
  o 1306839 Key properties of type 'numeric' are not supported
  o 1306710 CIM Client does not compile with JDK1.5
 
* Fri Sep 23 2005 Wolfgang Taphorn <taphorn@de.ibm.com>
- New release 1.2.2
  o 1298953 Remove dependency to JLog
  o 1275813 References are not processed properly
  o 1292671 enumerateClassNames is not working properly

* Tue Sep 13 2005 Wolfgang Taphorn <taphorn@de.ibm.com>
- New release 1.2.1
  o 18274 String values get corrupted by CIM client
    
* Thu Aug 18 2005 Wolfgang Taphorn <taphorn@de.ibm.com>
- Initial Version
