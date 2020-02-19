package javax.microedition.io;

import javax.microedition.pki.Certificate;

public interface SecurityInfo {

	Certificate getServerCertificate();

	String getProtocolVersion();

	String getProtocolName();

	String getCipherSuite();

}
