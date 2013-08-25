package ru.albemuth.util.http;

import java.net.HttpCookie;
import java.net.URI;

/**
 * @author VKornyshev
 */
public interface CookiePolicy {

    CookiePolicy ACCEPT_ALL = new CookiePolicy() {
        @Override
        public boolean shouldAccept(URI uri, HttpCookie httpCookie) {
            return true;
        }
    };
    CookiePolicy ACCEPT_NONE = new CookiePolicy() {
        @Override
        public boolean shouldAccept(URI uri, HttpCookie httpCookie) {
            return false;
        }
    };
    CookiePolicy ACCEPT_ORIGINAL_SERVER = new CookiePolicy() {
        @Override
        public boolean shouldAccept(URI uri, HttpCookie cookie) {
            return HttpCookie.domainMatches(cookie.getDomain(), uri.getHost());
        }
    };

    boolean shouldAccept(java.net.URI uri, java.net.HttpCookie httpCookie);

}
