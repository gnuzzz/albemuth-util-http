package ru.albemuth.util.http;

import ru.albemuth.util.CloseException;
import ru.albemuth.util.Configuration;
import ru.albemuth.util.ConfigurationException;

import java.io.OutputStream;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;

public class HTTPClientMockImpl extends HTTPClient {

    private boolean clearAfterRequest;
    private Response response;
    private String responseContent;
    private HTTPException responseException;

    public void mockSetResponse(Response response) {
        this.response = response;
    }

    public void mockSetResponseContent(String responseContent) {
        this.responseContent = responseContent;
    }

    public void mockSetResponseException(HTTPException responseException) {
        this.responseException = responseException;
    }

    protected Response returnResponse(Request request) throws HTTPException {
        try {
            if (responseException != null) {
                if (responseException.getRequest() == null) {
                    responseException.setRequest(request);
                }
                throw responseException;
            }
            Response ret = response;
            if (ret == null) {
                try {
                    ret = new Response(new URL(request.getUrl()), HttpURLConnection.HTTP_OK, "Ok", responseContent, "text/html", Collections.<HttpCookie>emptyList());
                } catch (MalformedURLException e) {
                    throw new HTTPException("Invalid url " + request.getUrl(), e);
                }
            }
            return ret;
        } finally {
            if (clearAfterRequest) {
                response = null;
                responseContent = null;
                responseException = null;
            }
        }
    }

    @Override
    public void configure(Configuration cfg) throws ConfigurationException {
        this.clearAfterRequest = cfg.getBooleanValue(this, "clearAfterRequest", true);
    }

    @Override
    public void close() throws CloseException {
        //do nothing
    }

    @Override
    public Response post(Request request) throws HTTPException {
        return returnResponse(request);
    }

    @Override
    public Response post(Request request, OutputStream out) throws HTTPException {
        return returnResponse(request);
    }

    @Override
    public Response post(Request request, String content) throws HTTPException {
        return returnResponse(request);
    }

    @Override
    public Response get(Request request) throws HTTPException {
        return returnResponse(request);
    }

    @Override
    public Response get(Request request, OutputStream out) throws HTTPException {
        return returnResponse(request);
    }

    @Override
    public void setProxy(Proxy proxy) {
        //do nothing
    }

}
