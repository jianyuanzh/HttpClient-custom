package com.santaba.sitemonitor.util.httpclient;

import com.santaba.common.logger.LogMsg;
import org.apache.http.*;
import org.apache.http.client.RedirectStrategy;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.protocol.HttpContext;

import java.net.URI;
import java.net.URISyntaxException;


/**
 * Created by vincent on 9/16/15.
 */
public class SMRedirectStrategy implements RedirectStrategy {

    private final static String REDIRECT_LOCATIONS = "SMRedirectionLocations";

    private final boolean _allowRedirect;

    public SMRedirectStrategy(boolean allowRedirect) {
        this._allowRedirect = allowRedirect;
    }

    public boolean isRedirected(HttpRequest request, HttpResponse response, HttpContext context) throws ProtocolException {
        if (response == null) {
            throw new IllegalArgumentException("HTTP response may not be null");
        }

        if (!_allowRedirect ) {
            return false;
        }

        int statusCode = response.getStatusLine().getStatusCode();
        switch (statusCode) {
            case HttpStatus.SC_MOVED_PERMANENTLY:
            case HttpStatus.SC_MOVED_TEMPORARILY:
            case HttpStatus.SC_SEE_OTHER:
            case HttpStatus.SC_TEMPORARY_REDIRECT:
                return true;
            default:
                return false;
        }
    }

    public HttpUriRequest getRedirect(HttpRequest request, HttpResponse response, HttpContext context) throws ProtocolException {

        if (response == null) {
            LogMsg.error("NULL HTTP Response.");
            throw new IllegalArgumentException("HTTP response should not be null");
        }

        if (!_allowRedirect) {
            LogMsg.warn("HTTP redirect disabled. ");
            return null;
        }

        // get the location header to find out where to redirect to
        Header locationHeader = response.getFirstHeader("location");
        if (locationHeader == null) {
            LogMsg.error("NULL localtion header in HTTP response");
            // got a redirect response, but no location header
            throw new ProtocolException(
                    "Received redirect response " + response.getStatusLine()
                         + " but no location header"
            );
        }

        // in case there is space in it (TODO: replace all space with %20 ??)
        String location = locationHeader.getValue().replaceAll(" ", "%20");

        LogMsg.debug("Got redirect localtion - " + location);

        URI uri;
        try {
            uri = new URI(location);
        }
        catch (URISyntaxException e) {
            throw new ProtocolException("Invalid redirect URI: " + location, e);
        }
        
//        ()

        return null;
    }
}
