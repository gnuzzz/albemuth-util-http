package ru.albemuth.util.http;

import junit.framework.TestCase;
import org.junit.Test;
import ru.albemuth.util.Configuration;

import java.net.CookieHandler;
import java.net.HttpCookie;
import java.net.URI;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TestHTTPClientPureJavaImpl {

    @Test
    public void testGet() {
        try {
            HTTPClient httpClient = new HTTPClientPureJavaImpl();
            httpClient.configure(new Configuration(new Properties()));

            HTTPClient.Request request = new HTTPClient.Request("http://www.biblion.ru/", null, new String[][] {
                    {"aaa", "bbb"}
            }, null, "Windows-1251");
            HTTPClient.Response response = httpClient.get(request);
            assertEquals(200, response.getStatus());
        } catch (Exception e) {
            e.printStackTrace();
            TestCase.fail();
        }
    }

    @Test
    public void testPost() {
        try {
            HTTPClient httpClient = new HTTPClientPureJavaImpl();
            httpClient.configure(new Configuration(new Properties()));

            HTTPClient.Request request = new HTTPClient.Request("http://www.biblion.ru/search/", null, null, new String[][] {
                    {"query", "Глоток свободы"}
            }, "Windows-1251");
            HTTPClient.Response response = httpClient.post(request);
            assertEquals(200, response.getStatus());
        } catch (Exception e) {
            e.printStackTrace();
            TestCase.fail();
        }
    }

    @Test
    public void testSecureGet() {
        try {
            HTTPClient httpClient = new HTTPClientPureJavaImpl();
            httpClient.configure(new Configuration(new Properties()));

            HTTPClient.Request request = new HTTPClient.Request("https://www.google.com/", null, null, null, "Windows-1251");
            HTTPClient.Response response = httpClient.get(request);
            assertEquals(200, response.getStatus());
        } catch (HTTPException e) {
            System.out.println(e.getResponse() != null ? e.getResponse().getContent(): "no response");
            e.printStackTrace();
            fail();
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testRedirectGet() {
        try {
            HTTPClient httpClient = new HTTPClientPureJavaImpl();
            Properties props = new Properties();
            props.put("follow-redirects", "false");
            httpClient.configure(new Configuration(props));

            HTTPClient.Request request = new HTTPClient.Request("http://biblion.ru/", null, null, null, "Windows-1251");
            HTTPClient.Response response = httpClient.get(request);
            System.out.println(response.getContent());
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testSystemCookies() {
        try {
            HTTPClient httpClient = new HTTPClientPureJavaImpl();
            Properties props = new Properties();
            props.put("follow-redirects", "false");
            httpClient.configure(new Configuration(props));

            HTTPClient.Request request = new HTTPClient.Request("http://my.beeline.ru/", "utf-8");
            HTTPClient.Response response = httpClient.get(request);
            System.out.println(response.getStatus());

            java.net.CookieManager cm = (java.net.CookieManager) CookieHandler.getDefault();
            java.net.CookieStore cookieJar = cm.getCookieStore();
            List<HttpCookie> cookies = cookieJar.getCookies();
            HttpCookie jsessionid = null;
            for (HttpCookie cookie: cookies) {
                if ("JSESSIONID".equals(cookie.getName())) {
                    jsessionid = cookie;
                    break;
                }
            }
            assertNotNull(jsessionid);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    //@Test
    public void testCustomCookies() {
        try {
            CookieHandler.setDefault(null);
            HTTPClient httpClient = new HTTPClientPureJavaImpl();
            Properties props = new Properties();
            props.put("follow-redirects", "false");
            props.put("use-system-cookie-manager", "false");
            httpClient.configure(new Configuration(props));

            HTTPClient.Request request = new HTTPClient.Request("https://my.beeline.ru/", "utf-8");
            HTTPClient.Response response = httpClient.get(request);

            CookieManager cm = ((HTTPClientPureJavaImpl)httpClient).getCookieManager();
            CookieStore cookieJar = cm.getCookieStore();
            List<HttpCookie> cookies = cookieJar.getCookies();
            HttpCookie jsessionid = null;
            for (HttpCookie cookie: cookies) {
                if ("JSESSIONID".equals(cookie.getName())) {
                    jsessionid = cookie;
                    break;
                }
            }
            assertNotNull(jsessionid);

            jsessionid.setValue("12345");
            cookieJar.add(new URI("https://my.beeline.ru/"), jsessionid);
            request = new HTTPClient.Request("https://my.beeline.ru/", "utf-8");
            response = httpClient.get(request);
            assertTrue(response.getUrl().getFile().endsWith("expired=true"));
            /*HttpCookie cookie = new HttpCookie("name", "value");
            cookie.setDomain("localhost");
            cookie.setPath("/");
            cookieJar.add(new URI("http://localhost:10000/"), cookie);
            request = new HTTPClient.Request("http://localhost:10000/", "utf-8");
            response = httpClient.get(request);
            System.out.println(response.getStatus());*/
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

}
