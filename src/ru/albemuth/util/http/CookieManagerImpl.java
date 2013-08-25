package ru.albemuth.util.http;

import ru.albemuth.util.CloseException;
import ru.albemuth.util.Closed;
import ru.albemuth.util.Configuration;
import ru.albemuth.util.ConfigurationException;
import ru.albemuth.util.Configured;

import java.net.HttpCookie;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author VKornyshev
 */
public class CookieManagerImpl implements Configured, Closed {

    private List<HttpCookie> cookies;

    @Override
    public void configure(Configuration cfg) throws ConfigurationException {
        this.cookies = new ArrayList<HttpCookie>();
    }

    @Override
    public void close() throws CloseException {
        //do nothing
    }

    public synchronized Collection<HttpCookie> getCookies(URL url) {
        Map<String, HttpCookie> result = new HashMap<String, HttpCookie>();
        for (Iterator<HttpCookie> it = cookies.iterator(); it.hasNext(); ) {
            HttpCookie cookie = it.next();
            if (cookie.hasExpired()) {
                it.remove();
            } else if (accept(cookie, url)) {
                HttpCookie c = result.get(cookie.getName());
                if (c == null || cookie.getDomain().length() > c.getDomain().length() || cookie.getPath().length() > c.getPath().length()) {
                    result.put(cookie.getName(), cookie);
                }
            }
        }
        return result.values();
    }

    public void addCookies(List<HttpCookie> newCookies) {
        for (HttpCookie cookie: newCookies) {
            for (Iterator<HttpCookie> it = cookies.iterator(); it.hasNext(); ) {
                HttpCookie c = it.next();
                if (c.hasExpired() || c.getDomain().equals(cookie.getDomain()) && c.getPath().equals(cookie.getPath()) && c.getName().equals(cookie.getName())) {
                    it.remove();
                }
            }
            cookies.add(cookie);
        }
    }

    public boolean accept(HttpCookie cookie, URL url) {
        return acceptDomain(cookie.getDomain(), url.getHost()) && acceptPath(cookie.getPath(), url.getPath());
    }

    protected boolean acceptDomain(String domain, String host) {
        return domain.equals(host) || domain.startsWith(".") && (host.equals(domain.substring(1, domain.length())) || host.endsWith(domain));
    }

    protected boolean acceptPath(String cookiePath, String urlPath) {
        urlPath = "".equals(urlPath) ? "/" : urlPath;
        return cookiePath.equals(urlPath) || urlPath.startsWith(cookiePath) && (cookiePath.endsWith("/") ? urlPath.charAt(cookiePath.length() - 1) == '/' : urlPath.charAt(cookiePath.length()) == '/');
    }

}
