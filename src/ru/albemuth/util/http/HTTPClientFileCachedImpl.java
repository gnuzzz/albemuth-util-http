package ru.albemuth.util.http;

import ru.albemuth.util.CloseException;
import ru.albemuth.util.Configuration;
import ru.albemuth.util.ConfigurationException;

import java.io.*;
import java.net.URLEncoder;
import java.net.URL;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;

import org.apache.log4j.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: vovan
 * Date: 05.03.2008
 * Time: 22:18:46
 */
public class HTTPClientFileCachedImpl extends HTTPClient {

    private static final Logger LOG                         = Logger.getLogger(HTTPClientFileCachedImpl.class);

    private HTTPClient client;
    private HTTPClient fileCacheClient;

    private String cacheRootPath;
    private String cacheEncoding;

    public void configure(Configuration cfg) throws ConfigurationException {
        client = Configuration.createInstance(HTTPClient.class, cfg.getStringValue(this, "httpclient", HTTPClientDefaultImpl.class.getName()));
        client.configure(cfg);
        fileCacheClient = Configuration.createInstance(HTTPClient.class, cfg.getStringValue(this, "cache-httpclient", HTTPClientFilesystemImpl.class.getName()));
        fileCacheClient.configure(cfg);
        cacheRootPath = cfg.getStringValue(this, "cache-root-path");
        cacheEncoding = cfg.getStringValue(this, "cache-encoding", "utf-8");
    }

    @Override
    public void close() throws CloseException {
        //do nothing
    }

    public Response post(Request request) throws HTTPException {
        Response ret;
        try {
            String cachePath = cacheRootPath + URLEncoder.encode(request.getUrl(), cacheEncoding);
            String formValuesPath = "";
            for (String[] field: request.getFormValues()) {
                formValuesPath += "&" + field[0] + "=" + URLEncoder.encode(field[1], cacheEncoding).replaceAll("\\+", "%20");
            }
            cachePath += formValuesPath.hashCode();
            if (new File(cachePath).exists()) {
                ret = fileCacheClient.post(new Request("file://" + cachePath, request.getLastModified(), request.getHeaders(), request.getFormValues(), cacheEncoding));
                ret = new Response(new URL(request.getUrl()), ret.getStatus(), ret.getMessage(), ret.getContent(), null, ret.getCookies());
            } else {
                ret = client.post(request);
                ret = new Response(new URL(request.getUrl()), ret.getStatus(), ret.getMessage(), ret.getContent(), null, ret.getCookies());
                if (ret.getStatus() == HttpURLConnection.HTTP_OK) {
                    cache(ret.getContent(), cachePath);
                }
            }
            return ret;
        } catch (UnsupportedEncodingException e) {
            throw new HTTPException("Cache encoding " + cacheEncoding + " isn't supported", e);
        } catch (MalformedURLException e) {
            throw new HTTPException("Can't create url for cached response", e);
        } catch (IOException e) {
            throw new HTTPException("Can't cache content while loading url " + request.getUrl(), e);
        }
    }

    public Response post(Request request, OutputStream out) throws HTTPException {
        Response ret;
        FileOutputStream cacheOut = null;
        try {
            String cachePath = cacheRootPath + URLEncoder.encode(request.getUrl(), cacheEncoding);
            String formValuesPath = "";
            for (String[] field: request.getFormValues()) {
                formValuesPath += "&" + field[0] + "=" + URLEncoder.encode(field[1], cacheEncoding).replaceAll("\\+", "%20");
            }
            cachePath += formValuesPath.hashCode();
            if (new File(cachePath).exists()) {
                ret = fileCacheClient.post(new Request("file://" + cachePath, request.getLastModified(), request.getHeaders(), request.getFormValues(), cacheEncoding), out);
                ret = new Response(new URL(request.getUrl()), ret.getStatus(), ret.getMessage(), ret.getContent(), null, ret.getCookies());
            } else {
                cacheOut = new FileOutputStream(cachePath);
                ret = client.post(request, cacheOut);
                if (ret.getStatus() == HttpURLConnection.HTTP_OK) {
                    ret = post(request, out);
                }
            }
            return ret;
        } catch (UnsupportedEncodingException e) {
            throw new HTTPException("Cache encoding " + cacheEncoding + " isn't supported", e);
        } catch (MalformedURLException e) {
            throw new HTTPException("Can't create url for cached response", e);
        } catch (IOException e) {
            throw new HTTPException("Can't cache content while post to url " + request.getUrl(), e);
        } finally {
            if (cacheOut != null) {
                try {
                    cacheOut.close();
                } catch (IOException e) {
                    LOG.error("Can't close cache output stream", e);
                }
            }
        }
    }

    @Override
    public Response post(Request request, String content) throws HTTPException {
        Response ret;
        try {
            String cachePath = cacheRootPath + URLEncoder.encode(request.getUrl(), cacheEncoding);
            String formValuesPath = "";
            for (String[] field: request.getFormValues()) {
                formValuesPath += "&" + field[0] + "=" + URLEncoder.encode(field[1], cacheEncoding).replaceAll("\\+", "%20");
            }
            cachePath += formValuesPath.hashCode();
            cachePath += content.hashCode();
            if (new File(cachePath).exists()) {
                ret = fileCacheClient.post(new Request("file://" + cachePath, request.getLastModified(), request.getHeaders(), request.getFormValues(), cacheEncoding));
                ret = new Response(new URL(request.getUrl()), ret.getStatus(), ret.getMessage(), ret.getContent(), null, ret.getCookies());
            } else {
                ret = client.post(request, content);
                ret = new Response(new URL(request.getUrl()), ret.getStatus(), ret.getMessage(), ret.getContent(), null, ret.getCookies());
                if (ret.getStatus() == HttpURLConnection.HTTP_OK) {
                    cache(ret.getContent(), cachePath);
                }
            }
            return ret;
        } catch (UnsupportedEncodingException e) {
            throw new HTTPException("Cache encoding " + cacheEncoding + " isn't supported", e);
        } catch (MalformedURLException e) {
            throw new HTTPException("Can't create url for cached response", e);
        } catch (IOException e) {
            throw new HTTPException("Can't cache content while loading url " + request.getUrl(), e);
        }
    }

    public Response get(Request request) throws HTTPException {
        Response ret;
        try {
            String cachePath = cacheRootPath + URLEncoder.encode(request.getUrl(), cacheEncoding);
            if (new File(cachePath).exists()) {
                ret = fileCacheClient.get(new Request("file://" + cachePath, request.getLastModified(), request.getHeaders(), request.getFormValues(), cacheEncoding));
                ret = new Response(new URL(request.getUrl()), ret.getStatus(), ret.getMessage(), ret.getContent(), null, ret.getCookies());
            } else {
                ret = client.get(request);
                //ret = new Response(new URL(request.getUrl()), ret.getStatus(), ret.getMessage(), ret.getContent());
                if (ret.getStatus() == HttpURLConnection.HTTP_OK) {
                    cache(ret.getContent(), cachePath);
                }
            }
            return ret;
        } catch (UnsupportedEncodingException e) {
            throw new HTTPException("Cache encoding " + cacheEncoding + " isn't supported", e);
        } catch (MalformedURLException e) {
            throw new HTTPException("Can't create url for cached response", e);
        } catch (IOException e) {
            throw new HTTPException("Can't cache content while get from url " + request.getUrl(), e);
        }
    }

    public Response get(Request request, OutputStream out) throws HTTPException {
        Response ret;
        FileOutputStream cacheOut = null;
        try {
            String cachePath = cacheRootPath + URLEncoder.encode(request.getUrl(), cacheEncoding);
            if (new File(cachePath).exists()) {
                ret = fileCacheClient.get(new Request("file://" + cachePath, request.getLastModified(), request.getHeaders(), request.getFormValues(), cacheEncoding), out);
                ret = new Response(new URL(request.getUrl()), ret.getStatus(), ret.getMessage(), ret.getContent(), null, ret.getCookies());
            } else {
                cacheOut = new FileOutputStream(cachePath);
                ret = client.get(request, cacheOut);
                if (ret.getStatus() == HttpURLConnection.HTTP_OK) {
                    ret = get(request, out);
                }
            }
            return ret;
        } catch (UnsupportedEncodingException e) {
            throw new HTTPException("Cache encoding " + cacheEncoding + " isn't supported", e);
        } catch (MalformedURLException e) {
            throw new HTTPException("Can't create url for cached response", e);
        } catch (IOException e) {
            throw new HTTPException("Can't cache content while get from url " + request.getUrl(), e);
        } finally {
            if (cacheOut != null) {
                try {
                    cacheOut.close();
                } catch (IOException e) {
                    LOG.error("Can't close cache output stream", e);
                }
            }
        }
    }

    public void setProxy(Proxy proxy) {
        client.setProxy(proxy);
        fileCacheClient.setProxy(proxy);
    }

    protected void cache(String content, String cachePath) throws IOException {
        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(cachePath), cacheEncoding));
            out.write(content);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    LOG.error("Can't close output writer while caching response content to " + cachePath, e);
                }
            }
        }
    }

    protected void cache(InputStream in, String cachePath) throws IOException {
        OutputStream out = null;
        try {
            out = new FileOutputStream(cachePath);
            byte[] buf = new byte[1024];
            for (int read = in.read(buf); read != -1; read = in.read(buf)) {
                out.write(buf, 0, read);
            }
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    LOG.error("Can't close output writer while caching response content to " + cachePath, e);
                }
            }
        }
    }

}
