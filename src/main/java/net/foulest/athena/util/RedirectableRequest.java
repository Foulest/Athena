package net.foulest.athena.util;

import com.diogonunes.jcolor.Ansi;
import com.diogonunes.jcolor.Attribute;
import lombok.Getter;
import lombok.Setter;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Getter
@Setter
public class RedirectableRequest {

    static {
        CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));
    }

    private URL request;
    private int protocolRedirectLimit;
    private int connectTimeout = 10000;
    private int readTimeout = 10000;

    public RedirectableRequest(URL request, int protocolRedirectLimit) {
        this.request = Objects.requireNonNull(request, "Request URL cannot be null");
        this.protocolRedirectLimit = protocolRedirectLimit;
    }

    public URLConnection openConnection() throws IOException {
        return openConnection(new HashMap<>());
    }

    public URLConnection openConnection(Map<String, String> requestProperties) throws IOException {
        int redirectCount = 0;
        boolean hasResponse = false;
        HttpURLConnection connection = null;
        URL currentRequest = request;

        while (!hasResponse && (redirectCount <= protocolRedirectLimit)) {
            try {
                connection = (HttpURLConnection) currentRequest.openConnection();
                connection.setConnectTimeout(connectTimeout);
                connection.setReadTimeout(readTimeout);

                HttpURLConnection finalConnection = connection;
                requestProperties.forEach(finalConnection::addRequestProperty);

                connection.setInstanceFollowRedirects(true);

                switch (connection.getResponseCode()) {
                    case HttpURLConnection.HTTP_MOVED_PERM:
                    case HttpURLConnection.HTTP_MOVED_TEMP:
                        redirectCount++;
                        String location = connection.getHeaderField("Location");
                        currentRequest = new URL(request, location);
                        break;

                    default:
                        hasResponse = true;
                        break;
                }
            } catch (FileNotFoundException ex) {
                System.out.println(Ansi.colorize("Stock name is invalid.", Attribute.RED_TEXT()));
            }
        }

        if (redirectCount > protocolRedirectLimit) {
            throw new IOException("Protocol redirect count exceeded for url: " + request.toExternalForm());
        } else {
            return connection;
        }
    }
}
