package ru.albemuth.util.http;

import ru.albemuth.util.CloseException;
import ru.albemuth.util.Configuration;
import ru.albemuth.util.ConfigurationException;

import java.io.OutputStream;

/**
 * Created by IntelliJ IDEA.
 * User: vovan
 * Date: 05.03.2008
 * Time: 22:11:52
 */
public class HTTPClientDefaultImpl extends HTTPClient {

    private HTTPClient localClient;
    private HTTPClient remoteClient;

    public void configure(Configuration cfg) throws ConfigurationException {
        localClient = Configuration.createInstance(HTTPClient.class, cfg.getStringValue(this, "local-httpclient", HTTPClientFilesystemImpl.class.getName()));
        localClient.configure(cfg);
        remoteClient = Configuration.createInstance(HTTPClient.class, cfg.getStringValue(this, "remote-httpclient", HTTPClientCommonsHttpClientImpl.class.getName()));
        remoteClient.configure(cfg);
    }

    @Override
    public void close() throws CloseException {
        remoteClient.close();
        localClient.close();
    }

    public Response post(Request request) throws HTTPException {
        if (request.getUrl().startsWith("file://")) {
            return localClient.post(request);
        } else {
            return remoteClient.post(request);
        }
    }

    public Response post(Request request, OutputStream out) throws HTTPException {
        if (request.getUrl().startsWith("file://")) {
            return localClient.post(request, out);
        } else {
            return remoteClient.post(request, out);
        }
    }

    @Override
    public Response post(Request request, String content) throws HTTPException {
        if (request.getUrl().startsWith("file://")) {
            return localClient.post(request, content);
        } else {
            return remoteClient.post(request, content);
        }
    }

    public Response get(Request request) throws HTTPException {
        if (request.getUrl().startsWith("file://")) {
            return localClient.get(request);
        } else {
            return remoteClient.get(request);
        }
    }

    public Response get(Request request, OutputStream out) throws HTTPException {
        if (request.getUrl().startsWith("file://")) {
            return localClient.get(request, out);
        } else {
            return remoteClient.get(request, out);
        }
    }

    public void setProxy(Proxy proxy) {
        localClient.setProxy(proxy);
        remoteClient.setProxy(proxy);
    }

}
