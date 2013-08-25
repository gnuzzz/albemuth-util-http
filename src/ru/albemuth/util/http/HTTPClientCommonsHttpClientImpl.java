package ru.albemuth.util.http;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import ru.albemuth.util.CloseException;
import ru.albemuth.util.Configuration;
import ru.albemuth.util.ConfigurationException;

import java.io.OutputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpCookie;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.log4j.Logger;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.params.*;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: vovan
 * Date: 05.03.2008
 * Time: 21:50:12
 */
public class HTTPClientCommonsHttpClientImpl extends HTTPClient {

    private static final Logger LOG                                 = Logger.getLogger(HTTPClientCommonsHttpClientImpl.class);

    public  static final String HEADER_IF_MODIFIED_SINCE            = "If-Modified-Since";
    public  static final String HEADER_LAST_MODIFIED                = "Last-Modified";

    private HttpClient client;
    public DateFormat httpDateFormat;

    public void configure(Configuration cfg) throws ConfigurationException {
        client = new HttpClient();
        HttpParams params = DefaultHttpParams.getDefaultParams();
        params.setBooleanParameter(HttpMethodParams.SINGLE_COOKIE_HEADER, true);
        ((HttpClientParams)params).setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
        long connectionTimeout = cfg.getLongValue(this, "connection-timeout", 10000);
        client.getHttpConnectionManager().getParams().setIntParameter(HttpConnectionManagerParams.CONNECTION_TIMEOUT, (int)connectionTimeout);
        client.getHttpConnectionManager().getParams().setIntParameter(HttpConnectionManagerParams.SO_TIMEOUT, (int)connectionTimeout);

        if (cfg.getBooleanValue(this, "untrusted-ssl", true)) {
            try {
                SSLContext ctx = SSLContext.getInstance("TLS");
                ctx.init(new KeyManager[0], new TrustManager[] {new DefaultTrustManager()}, new SecureRandom());
                SSLContext.setDefault(ctx);
            } catch (NoSuchAlgorithmException e) {
                throw new ConfigurationException("Can't create untrusted ssl manager", e);
            } catch (KeyManagementException e) {
                throw new ConfigurationException("Can't create untrusted ssl manager", e);
            }
        }

        httpDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
        httpDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    @Override
    public void close() throws CloseException {
        //do nothing
    }

    public Response post(Request request) throws HTTPException {
        return post(request, (String)null);
    }

    public Response post(Request request, OutputStream out) throws HTTPException {
        throw new HTTPException("Not implement yet!");
    }

    @Override
    public Response post(Request request, String content) throws HTTPException {
        LOG.debug("HTTP post to " + request.getUrl());
        PostMethod postMethod = new PostMethod(request.getUrl());
        try {
            boolean contentTypeExists = false;
            boolean userAgentExists = false;
            for (String[] header: request.getHeaders()) {
                postMethod.addRequestHeader(header[0], header[1]);
                if ("Content-Type".equalsIgnoreCase(header[0])) {
                    contentTypeExists = true;
                } if ("User-Agent".equalsIgnoreCase(header[0])) {
                    userAgentExists = true;
                }
            }
            if (!contentTypeExists) {
                postMethod.addRequestHeader("Content-Type","application/x-www-form-urlencoded; charset=" + request.getEncoding());
            }
            if (!userAgentExists) {
                postMethod.addRequestHeader("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)");
            }
            for (String[] value: request.getFormValues()) {
                postMethod.addParameter(value[0], value[1]);
            }
            if (content != null) {
                postMethod.setRequestEntity(new StringRequestEntity(content, "application/x-www-form-urlencoded", request.getEncoding()));
            }
            int responseCode = client.executeMethod(postMethod);
            String contentType = getContentType(postMethod);
            String responseContent = getContent(new InputStreamReader(postMethod.getResponseBodyAsStream(), contentTypeEncoding(contentType, request.getEncoding())));
            if (responseCode == HttpURLConnection.HTTP_OK) {
                return new Response(new URL(postMethod.getURI().getURI()), postMethod.getStatusCode(), postMethod.getStatusText(), responseContent, contentType, cookies(client.getState().getCookies()));
            } else if (responseCode >= HttpURLConnection.HTTP_MULT_CHOICE && responseCode < HttpURLConnection.HTTP_BAD_REQUEST) {
                String redirectUrl =  postMethod.getResponseHeader("location") != null ? postMethod.getResponseHeader("location").getValue() : null;
                if (redirectUrl != null) {
                    return get(new Request(redirectUrl, request.getEncoding()));
                } else {
                    throw new HTTPException("Can't post form values to url " + request.getUrl() + ": no redirect location", request, new Response(new URL(postMethod.getURI().getURI()), postMethod.getStatusCode(), postMethod.getStatusText(), responseContent, contentType, cookies(client.getState().getCookies())));
                }
            } else {
                throw new HTTPException("Can't post form values to url " + request.getUrl() + ": wrong response status " + responseCode, request, new Response(new URL(postMethod.getURI().getURI()), postMethod.getStatusCode(), postMethod.getStatusText(), responseContent, contentType, cookies(client.getState().getCookies())));
            }
        } catch (HttpException e) {
            throw new HTTPException("HttpException while post form values to url " + request.getUrl(), e);
        } catch (IOException e) {
            throw new HTTPException("IOException while post form values to url " + request.getUrl(), e);
        } catch (RuntimeException e) {
            throw new HTTPException("Unexpected exception", e);
        } finally {
            postMethod.releaseConnection();
        }
    }

    public Response get(Request request) throws HTTPException {
        LOG.debug("HTTP get from " + request.getUrl());
        HttpMethod getMethod = null;
        try {
            getMethod = new GetMethod(request.getUrl());
            boolean userAgentExists = false;
            for (String[] header: request.getHeaders()) {
                getMethod.addRequestHeader(header[0], header[1]);
                if ("Header".equalsIgnoreCase(header[0])) {
                    userAgentExists = true;
                }
            }
            if (!userAgentExists) {
                getMethod.addRequestHeader("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)");
            }
            if (request.getLastModified() != null) {
                getMethod.addRequestHeader(HEADER_IF_MODIFIED_SINCE, httpDateFormat.format(request.getLastModified()));
            }
            int responseCode = client.executeMethod(getMethod);
            String contentType = getContentType(getMethod);
            String responseContent = getContent(new InputStreamReader(getMethod.getResponseBodyAsStream(), contentTypeEncoding(contentType, request.getEncoding())));
            if (responseCode == HttpURLConnection.HTTP_OK) {
            } else if (responseCode == HttpURLConnection.HTTP_NOT_MODIFIED) {
                responseContent = null;
            } else if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                throw new HTTPException("Document with url " + request.getUrl() + " not found", request, new Response(new URL(getMethod.getURI().getURI()), getMethod.getStatusCode(), getMethod.getStatusText(), responseContent, contentType, cookies(client.getState().getCookies())));
            } else {
                throw new HTTPException("Unexpected response for GET request to url " + request.getUrl() + ": " + responseCode, request, new Response(new URL(getMethod.getURI().getURI()), getMethod.getStatusCode(), getMethod.getStatusText(), responseContent, contentType, cookies(client.getState().getCookies())));
            }
            return new Response(new URL(getMethod.getURI().getURI()), getMethod.getStatusCode(), getMethod.getStatusText(), responseContent, contentType, cookies(client.getState().getCookies()));
        } catch (HttpException e) {
            throw new HTTPException("HttpException while getting document with url " + request.getUrl(), e);
        } catch (IOException e) {
            throw new HTTPException("IOException while getting document with url " + request.getUrl(), e);
        } catch (RuntimeException e) {
            throw new HTTPException("Can't read file from url " + request.getUrl() + ": " + e.getMessage(), e);
        } finally {
            if (getMethod != null) {
                getMethod.releaseConnection();
            }
        }
    }

    public Response get(Request request, OutputStream out) throws HTTPException {
        LOG.debug("HTTP load content from " + request.getUrl());
        HttpMethod getMethod = null;
        try {
            getMethod = new GetMethod(request.getUrl());
            getMethod.addRequestHeader("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)");
            for (String[] header: request.getHeaders()) {
                getMethod.addRequestHeader(header[0], header[1]);
            }
            if (request.getLastModified() != null) {
                getMethod.addRequestHeader(HEADER_IF_MODIFIED_SINCE, httpDateFormat.format(request.getLastModified()));
            }
            int responseCode = client.executeMethod(getMethod);
            String contentType = getContentType(getMethod);
            if (responseCode == HttpURLConnection.HTTP_OK) {
                InputStream in = getMethod.getResponseBodyAsStream();
                byte[] buff = new byte[1024];
                for (int read = in.read(buff); read != -1; read = in.read(buff)) {
                    out.write(buff, 0, read);
                }
            } else if (responseCode == HttpURLConnection.HTTP_NOT_MODIFIED) {
                String responseContent = getContent(new InputStreamReader(getMethod.getResponseBodyAsStream(), contentTypeEncoding(contentType, request.getEncoding())));
                throw new HTTPException("httpLoadContent not supported HTTP_NOT_MODIFIED header", request, new Response(new URL(getMethod.getURI().getURI()), getMethod.getStatusCode(), getMethod.getStatusText(), responseContent, contentType, cookies(client.getState().getCookies())));
            } else if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                String responseContent = getContent(new InputStreamReader(getMethod.getResponseBodyAsStream(), contentTypeEncoding(contentType, request.getEncoding())));
                throw new HTTPException("Document with url " + request.getUrl() + " not found", request, new Response(new URL(getMethod.getURI().getURI()), getMethod.getStatusCode(), getMethod.getStatusText(), responseContent, contentType, cookies(client.getState().getCookies())));
            } else {
                String responseContent = getContent(new InputStreamReader(getMethod.getResponseBodyAsStream(), contentTypeEncoding(contentType, request.getEncoding())));
                throw new HTTPException("Unexpected response for GET request to url " + request.getUrl() + ": " + responseCode, request, new Response(new URL(getMethod.getURI().getURI()), getMethod.getStatusCode(), getMethod.getStatusText(), responseContent, contentType, cookies(client.getState().getCookies())));
            }
            return new Response(new URL(getMethod.getURI().getURI()), getMethod.getStatusCode(), getMethod.getStatusText(), null, contentType, cookies(client.getState().getCookies()));
        } catch (HttpException e) {
            throw new HTTPException("HttpException while getting document with url " + request.getUrl(), e);
        } catch (IOException e) {
            throw new HTTPException("IOException while getting document with url " + request.getUrl(), e);
        } catch (RuntimeException e) {
            throw new HTTPException("Can't read file from url " + request.getUrl() + ": " + e.getMessage(), e);
        } finally {
            if (getMethod != null) {
                getMethod.releaseConnection();
            }
        }
    }

    public void setProxy(Proxy proxy) {
        client.getHostConfiguration().setProxy(proxy.getHost(), proxy.getPort());
    }

    private String getContentType(HttpMethod method) {
        Header contentTypeHeader = getResponseHeader(method, "Content-type");
        if (contentTypeHeader == null) {contentTypeHeader = getResponseHeader(method, "Content-Type");}
        if (contentTypeHeader == null) {contentTypeHeader = getResponseHeader(method, "content-type");}
        return contentTypeHeader != null ? contentTypeHeader.getValue() : null;
    }

    private Header getResponseHeader(HttpMethod method, String headerName) {
        Header[] headers = method.getResponseHeaders(headerName);
        if (headers != null && headers.length > 0) {
            return headers[0];
        } else {
            return null;
        }
    }

    private List<HttpCookie> cookies(org.apache.commons.httpclient.Cookie[] cookies) {
        List<HttpCookie> ret = new ArrayList<HttpCookie>(client.getState().getCookies().length);
        for (org.apache.commons.httpclient.Cookie c: client.getState().getCookies()) {
            HttpCookie cookie = new HttpCookie(c.getName(), c.getValue());
            cookie.setDomain(c.getDomain());
            cookie.setPath(c.getPath());
            cookie.setMaxAge(c.getExpiryDate() != null ? (c.getExpiryDate().getTime() - System.currentTimeMillis())/1000 : -1);
            cookie.setSecure(c.getSecure());
            cookie.setVersion(c.getVersion());
            ret.add(cookie);
        }
        return ret;
    }

    private static class DefaultTrustManager implements X509TrustManager {

        public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
        }

        public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
        }

        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }

    }
    
}
