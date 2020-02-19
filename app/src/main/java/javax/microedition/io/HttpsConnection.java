package javax.microedition.io;

import java.io.IOException;

public interface HttpsConnection extends HttpConnection {

	SecurityInfo getSecurityInfo() throws IOException;

	@Override
	int getPort();

}
