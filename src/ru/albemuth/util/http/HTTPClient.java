package ru.albemuth.util.http;

import ru.albemuth.util.Closed;
import ru.albemuth.util.Configured;

import java.io.Serializable;
import java.net.HttpCookie;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.net.URL;
import java.io.OutputStream;
import java.io.Reader;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: vovan
 * Date: 05.03.2008
 * Time: 21:22:17
 */
public abstract class HTTPClient implements Configured, Closed {

    private static final String[][] EMPTY_VALUES                    = new String[0][0];
    
    public abstract Response post(Request request) throws HTTPException;

    public abstract Response post(Request request, OutputStream out) throws HTTPException;

    public abstract Response post(Request request, String content) throws HTTPException;

    public abstract Response get(Request request) throws HTTPException;

    public abstract Response get(Request request, OutputStream out) throws HTTPException;

    public abstract void setProxy(Proxy proxy);

    protected String getContent(Reader in) throws IOException {
        StringBuilder ret = new StringBuilder();
        char[] buf = new char[1024];
        for (int read = in.read(buf); read != -1; read = in.read(buf)) {
            ret.append(buf, 0, read);
        }
        return ret.toString();
    }

    private static final Pattern CHARSET_PATTERN                        = Pattern.compile("charset=([\\w\\-]+)");

    public static String contentTypeEncoding(String contentType, String defaultEncoding) {
        String encoding = defaultEncoding;
        if (contentType != null) {
            Matcher m = CHARSET_PATTERN.matcher(contentType);
            if (m.find()) {
                encoding = m.group(1);
            }
        }
        return encoding;
    }

    public static class Request implements Serializable {

        private String url;
        private Date lastModified;
        private String[][] headers;
        private String[][] formValues;
        private String encoding;

        public Request(String url, Date lastModified, String[][] headers, String[][] formValues, String encoding) {
            this.url = url;
            this.lastModified = lastModified;
            this.headers = headers != null ? headers : EMPTY_VALUES;
            this.formValues = formValues != null ? formValues : EMPTY_VALUES;
            this.encoding = encoding;
        }

        public Request(String url, String[][] headers, String encoding) {
            this(url, null, headers, EMPTY_VALUES, encoding);
        }

        public Request(String url, Date lastModified, String encoding) {
            this(url, lastModified, EMPTY_VALUES, EMPTY_VALUES, encoding);
        }

        public Request(String url, String encoding) {
            this(url, null, EMPTY_VALUES, EMPTY_VALUES, encoding);
        }

        public String getUrl() {
            return url;
        }

        public Date getLastModified() {
            return lastModified;
        }

        public String[][] getHeaders() {
            return headers;
        }

        public String[][] getFormValues() {
            return formValues;
        }

        public String getEncoding() {
            return encoding;
        }

    }

    public static class Response implements Serializable {

        private URL url;
        private int status;
        private String message;
        private String content;
        private String contentType;
        private List<HttpCookie> cookies;

        public Response(URL url, int code, String message, String content, String contentType, List<HttpCookie> cookies) {
            this.url = url;
            this.status = code;
            this.message = message;
            this.content = content;
            this.contentType = contentType;
            this.cookies = cookies;
        }

        public URL getUrl() {
            return url;
        }

        public int getStatus() {
            return status;
        }

        public String getMessage() {
            return message;
        }

        public String getContent() {
            return content;
        }

        public String getContentType() {
            return contentType;
        }

        public List<HttpCookie> getCookies() {
            return cookies;
        }

    }

    public static class Proxy {

        public static final Pattern HTTP_PROXY_PATTERN          = Pattern.compile("(.+):(\\d+)");
        public static final Proxy[] EMPTY_ARRAY                 = {};

        private String host;
        private int port;

        public Proxy(String host, int port) {
            this.host = host;
            this.port = port;
        }

        public String getHost() {
            return host;
        }

        public int getPort() {
            return port;
        }

        public String toString() {
            return getHost() + ":" + getPort();
        }

        public int hashCode() {
            return getPort();
        }

        public boolean equals(Object obj) {
            return obj instanceof Proxy && ((Proxy)obj).getHost().equals(getHost()) && ((Proxy)obj).getPort() == getPort();
        }

        public static Proxy createProxy(String proxyString) throws HTTPException {
            Matcher m = HTTP_PROXY_PATTERN.matcher(proxyString);
            if (m.find()) {
                return new Proxy(m.group(1), Integer.parseInt(m.group(2)));
            } else {
                throw new HTTPException("Can't create http proxy from string '" + proxyString + "'");
            }
        }

    }

}
