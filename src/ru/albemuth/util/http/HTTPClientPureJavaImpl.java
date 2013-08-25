package ru.albemuth.util.http;

import org.apache.log4j.Logger;
import ru.albemuth.util.CloseException;
import ru.albemuth.util.Configuration;
import ru.albemuth.util.ConfigurationException;

import java.io.*;
import java.net.*;
import java.util.Collections;
import java.util.List;

public class HTTPClientPureJavaImpl extends HTTPClient {

    private static final Logger LOG                     = Logger.getLogger(HTTPClientPureJavaImpl.class);

    private int connectTimeout;
    private int readTimeout;
    private boolean followRedirects;
    private CookieManager cookieManager;

    public CookieManager getCookieManager() {
        return cookieManager;
    }

    public void configure(Configuration cfg) throws ConfigurationException {
        this.connectTimeout = cfg.getIntValue(this, "connect-timeout", 300000);
        this.readTimeout = cfg.getIntValue(this, "read-timeout", 300000);
        this.followRedirects = cfg.getBooleanValue(this, "follow-redirects", true);
        if (cfg.getBooleanValue(this, "use-system-cookie-manager", true)) {
            java.net.CookieManager manager = new java.net.CookieManager();
            manager.setCookiePolicy(java.net.CookiePolicy.ACCEPT_ALL);
            CookieHandler.setDefault(manager);
        } else {
            CookieManager manager = new CookieManager();
            manager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
            this.cookieManager = manager;
        }
    }

    @Override
    public void close() throws CloseException {
        //do nothing
    }

    @Override
    public Response post(Request request) throws HTTPException {
        return post(request, (String)null);
    }

    @Override
    public Response post(Request request, OutputStream out) throws HTTPException {
        throw new AbstractMethodError("not implemented yet");
    }

    @Override
    public Response post(Request request, String content) throws HTTPException {
        LOG.debug("HTTP post to " + request.getUrl());
        InputStream responseStream = null;
        try {
            URL url = new URL(request.getUrl());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(connectTimeout);
            connection.setReadTimeout(readTimeout);
            connection.setInstanceFollowRedirects(followRedirects);
            boolean contentTypeExists = false;
            for (String[] header: request.getHeaders()) {
                if ("Content-Type".equalsIgnoreCase(header[0])) {
                    contentTypeExists = true;
                }
                connection.setRequestProperty(header[0], header[1]);
            }
            if (!contentTypeExists) {
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=" + request.getEncoding());
            }
            processRequestCookies(connection);
            OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());

            if (content != null) {
                writer.write(content);
            }

            if (request.getFormValues() != null) {
                String[][] formValues = request.getFormValues();
                for (int i = 0; i < formValues.length; i++) {
                    if (i > 0) {
                        writer.write("&");
                    }
                    writer.write(formValues[i][0] + "=" + URLEncoder.encode(formValues[i][1], request.getEncoding()));
                }
            }
            writer.close();

            int responseCode = connection.getResponseCode();
            String contentType =  connection.getContentType();
            List<HttpCookie> cookies = processResponseCookies(connection);
            responseStream = connection.getErrorStream() != null ? connection.getErrorStream() : connection.getInputStream();
            String responseContent = getContent(new InputStreamReader(responseStream, contentTypeEncoding(contentType, request.getEncoding())));
            if (responseCode == HttpURLConnection.HTTP_OK) {
                return new Response(connection.getURL(), responseCode, connection.getResponseMessage(), responseContent, contentType, cookies);
            } else if (responseCode >= HttpURLConnection.HTTP_MULT_CHOICE && responseCode < HttpURLConnection.HTTP_BAD_REQUEST) {
                String redirectUrl = connection.getHeaderField("Location") != null ? connection.getHeaderField("Location") : null;
                if (redirectUrl != null) {
                    return get(new Request(redirectUrl, request.getEncoding()));
                } else {
                    throw new HTTPException("Can't post form values to url " + request.getUrl() + ": no redirect location", request, new Response(connection.getURL(), responseCode, connection.getResponseMessage(), responseContent, contentType, cookies));
                }
            } else {
                throw new HTTPException("Can't post form values to url " + request.getUrl() + ": wrong response status " + responseCode, request, new Response(connection.getURL(), responseCode, connection.getResponseMessage(), responseContent, contentType, cookies));
            }

        } catch (MalformedURLException e) {
            throw new HTTPException("Invalid url " + request.getUrl());
        } catch (ProtocolException e) {
            throw new HTTPException("Can't do get request to url " + request.getUrl(), e);
        } catch (IOException e) {
            throw new HTTPException("IOException while processing get request to url " + request.getUrl(), e);
        } finally {
            if (responseStream != null) {
                try {
                    responseStream.close();
                } catch (IOException e) {
                    LOG.error("Can't close response stream while processing get request to url " + request.getUrl(), e);
                }
            }
        }
    }

    @Override
    public Response get(Request request) throws HTTPException {
        LOG.debug("HTTP get from " + request.getUrl());
        InputStream responseStream = null;
        try {
            URL url = new URL(request.getUrl());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(connectTimeout);
            connection.setReadTimeout(readTimeout);
            connection.setInstanceFollowRedirects(followRedirects);
            for (String[] header: request.getHeaders()) {
                connection.setRequestProperty(header[0], header[1]);
            }
            processRequestCookies(connection);
            
            int responseCode = connection.getResponseCode();
            String contentType =  connection.getContentType();
            List<HttpCookie> cookies = processResponseCookies(connection);
            responseStream = connection.getErrorStream() != null ? connection.getErrorStream() : connection.getInputStream();
            String responseContent = getContent(new InputStreamReader(responseStream, contentTypeEncoding(contentType, request.getEncoding())));
            if (responseCode == HttpURLConnection.HTTP_OK) {
            } else if (responseCode == HttpURLConnection.HTTP_NOT_MODIFIED) {
                responseContent = null;
            } else if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                throw new HTTPException("Document with url " + request.getUrl() + " not found", request, new Response(connection.getURL(), responseCode, connection.getResponseMessage(), responseContent, contentType, cookies));
            } else if (responseCode >= HttpURLConnection.HTTP_MULT_CHOICE && responseCode < HttpURLConnection.HTTP_BAD_REQUEST) {
                String redirectUrl =  connection.getHeaderField("Location");
                if (redirectUrl != null) {
                    return get(new Request(redirectUrl, request.getEncoding()));
                } else {
                    throw new HTTPException("Can't get document from url " + request.getUrl() + ": no redirect location", request, new Response(connection.getURL(), responseCode, connection.getResponseMessage(), responseContent, contentType, cookies));
                }
            } else {
                throw new HTTPException("Unknown response for GET request to url " + request.getUrl() + ": " + responseCode, request, new Response(connection.getURL(), responseCode, connection.getResponseMessage(), responseContent, contentType, cookies));
            }
            return new Response(connection.getURL(), responseCode, connection.getResponseMessage(), responseContent, contentType, cookies);

        } catch (MalformedURLException e) {
            throw new HTTPException("Invalid url " + request.getUrl());
        } catch (ProtocolException e) {
            throw new HTTPException("Can't do get request to url " + request.getUrl(), e);
        } catch (IOException e) {
            throw new HTTPException("IOException while processing get request to url " + request.getUrl(), e);
        } finally {
            if (responseStream != null) {
                try {
                    responseStream.close();
                } catch (IOException e) {
                    LOG.error("Can't close response stream while processing get request to url " + request.getUrl(), e);
                }
            }
        }
    }

    @Override
    public Response get(Request request, OutputStream out) throws HTTPException {
        throw new AbstractMethodError("not implemented yet");
    }

    @Override
    public void setProxy(Proxy proxy) {
        throw new AbstractMethodError("not implemented yet");
    }

    protected void processRequestCookies(HttpURLConnection connection) {
        if (cookieManager != null) {
            try {
                List<HttpCookie> cookies = cookieManager.getCookieStore().get(connection.getURL().toURI());
                if (cookies.size() > 0) {
                    StringBuilder cookiesHeader = new StringBuilder();
                    for (HttpCookie cookie: cookies) {
                        if (cookiesHeader.length() > 0) {
                            cookiesHeader.append("; ");
                        }
                        cookiesHeader.append(cookie.getName()).append("=").append(cookie.getValue());
                    }
                    connection.setRequestProperty("Cookie", cookiesHeader.toString());
                }
            } catch (URISyntaxException e) {
                LOG.error("Can't obtain cookies from cookie store for url " + connection.getURL(), e);
            }
        }
    }

    protected List<HttpCookie> processResponseCookies(HttpURLConnection connection) {
        List<HttpCookie> cookies;
        String cookiesHeader = connection.getHeaderField("Set-Cookie");
        if (cookiesHeader != null) {
            cookies = HttpCookie.parse(connection.getHeaderField("Set-Cookie"));
            if (cookieManager != null) {
                for (HttpCookie cookie: cookies) {
                    try {
                        cookieManager.getCookieStore().add(connection.getURL().toURI(), cookie);
                    } catch (URISyntaxException e) {
                        LOG.error("Can't store cooke for url " + connection.getURL(), e);
                    }
                }
            }
        } else {
            cookies = Collections.emptyList();
        }
        return cookies;
    }

}
