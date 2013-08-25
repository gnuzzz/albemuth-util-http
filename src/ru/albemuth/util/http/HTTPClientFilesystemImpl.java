package ru.albemuth.util.http;

import ru.albemuth.util.CloseException;
import ru.albemuth.util.Configuration;

import java.io.*;
import java.net.HttpCookie;
import java.net.URL;
import java.net.HttpURLConnection;
import java.util.Collections;

import org.apache.log4j.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: vovan
 * Date: 05.03.2008
 * Time: 21:26:29
 */
public class HTTPClientFilesystemImpl extends HTTPClient {

    private static final Logger LOG                         = Logger.getLogger(HTTPClientFilesystemImpl.class);

    public void configure(Configuration cfg) {
        //do nothing
    }

    @Override
    public void close() throws CloseException {
        //do nothing
    }

    public Response post(Request request) throws HTTPException {
        return get(request);
    }

    public Response post(Request request, OutputStream out) throws HTTPException {
        return get(request, out);
    }

    @Override
    public Response post(Request request, String content) throws HTTPException {
        return get(request);
    }

    public Response get(Request request) throws HTTPException {
        LOG.debug("Local get from " + request.getUrl());
        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(new FileInputStream(request.getUrl().substring("file://".length())), request.getEncoding()));
            StringBuffer content = new StringBuffer();
            for (String s = in.readLine(); s != null; s = in.readLine()) {
                if (content.length() > 0) {
                    content.append('\n');
                }
                content.append(s);
            }
            return new Response(new URL(request.getUrl()), HttpURLConnection.HTTP_OK, "OK", content.toString(), null, Collections.<HttpCookie>emptyList());
        } catch (FileNotFoundException e) {
            throw new HTTPException("Can't read file from url " + request.getUrl() + ": file not found", e);
        } catch (UnsupportedEncodingException e) {
            throw new HTTPException("Can't read file from url " + request.getUrl() + ": unsupported encoding " + request.getEncoding(), e);
        } catch (IOException e) {
            throw new HTTPException("Can't read file from url " + request.getUrl(), e);
        } catch (Exception e) {
            throw new HTTPException("Can't read file from url " + request.getUrl() + ": unexpected exception", e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    LOG.error("Can't close input reader", e);
                }
            }
        }
    }

    public Response get(Request request, OutputStream out) throws HTTPException {
        LOG.debug("Local get from " + request.getUrl());
        InputStream in = null;
        try {
            in = new FileInputStream(request.getUrl().substring("file://".length()));
            byte[] buf = new byte[1024];
            for (int read = in.read(buf); read != -1; read = in.read(buf)) {
                out.write(buf, 0, read);
            }
            return new Response(new URL(request.getUrl()), HttpURLConnection.HTTP_OK, "OK", null, null, Collections.<HttpCookie>emptyList());
        } catch (FileNotFoundException e) {
            throw new HTTPException("Can't read file from url " + request.getUrl() + ": file not found", e);
        } catch (IOException e) {
            throw new HTTPException("Can't read file from url " + request.getUrl(), e);
        } catch (Exception e) {
            throw new HTTPException("Can't read file from url " + request.getUrl() + ": unexpected exception", e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    LOG.error("Can't close input stream", e);
                }
            }
        }
    }

    public void setProxy(Proxy proxy) {}

}
