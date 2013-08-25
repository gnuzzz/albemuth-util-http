package ru.albemuth.util.http;

/**
 * @author VKornyshev
 */
public interface CookieStore  {

    void add(java.net.URI uri, java.net.HttpCookie httpCookie);

    java.util.List<java.net.HttpCookie> get(java.net.URI uri);

    java.util.List<java.net.HttpCookie> getCookies();

    java.util.List<java.net.URI> getURIs();

    boolean remove(java.net.URI uri, java.net.HttpCookie httpCookie);

    boolean removeAll();
}
