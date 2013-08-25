package ru.albemuth.util.http;

import org.junit.Test;
import ru.albemuth.util.Configuration;

import java.net.HttpCookie;
import java.net.URL;
import java.util.Collections;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

public class TestHTTPClientMockImpl {

    @Test
    public void testMockResponse() {
        try {
            Properties props = new Properties();
            Configuration cfg = new Configuration(props);
            HTTPClientMockImpl httpClient = new HTTPClientMockImpl();
            httpClient.configure(cfg);
            HTTPClient.Response mockResponse, response;

            mockResponse = new HTTPClient.Response(new URL("http://aaa.com"), 200, "Ok", "aaa aaa", "text/html", Collections.<HttpCookie>emptyList());
            httpClient.mockSetResponse(mockResponse);
            response = httpClient.get(new HTTPClient.Request("http://aaa.com/1", "utf-8"));
            assertEquals(new URL("http://aaa.com"), response.getUrl());
            assertEquals(200, response.getStatus());
            assertEquals("Ok", response.getMessage());
            assertEquals("aaa aaa", response.getContent());
            assertEquals("text/html", response.getContentType());

            mockResponse = new HTTPClient.Response(new URL("http://aaa.com"), 404, "Not found", "bbb bbb", "text/plain", Collections.<HttpCookie>emptyList());
            httpClient.mockSetResponse(mockResponse);
            response = httpClient.get(new HTTPClient.Request("http://aaa.com/2", "utf-8"));
            assertEquals(new URL("http://aaa.com"), response.getUrl());
            assertEquals(404, response.getStatus());
            assertEquals("Not found", response.getMessage());
            assertEquals("bbb bbb", response.getContent());
            assertEquals("text/plain", response.getContentType());

        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testMockResponseContent() {
        try {
            Properties props = new Properties();
            Configuration cfg = new Configuration(props);
            HTTPClientMockImpl httpClient = new HTTPClientMockImpl();
            httpClient.configure(cfg);
            HTTPClient.Response response;

            httpClient.mockSetResponseContent("ccc ccc");
            response = httpClient.get(new HTTPClient.Request("http://aaa.com/1", "utf-8"));
            assertEquals(new URL("http://aaa.com/1"), response.getUrl());
            assertEquals(200, response.getStatus());
            assertEquals("Ok", response.getMessage());
            assertEquals("ccc ccc", response.getContent());
            assertEquals("text/html", response.getContentType());
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testClearAfterRequest() {
        try {
            Properties props;
            Configuration cfg;
            HTTPClientMockImpl httpClient;
            HTTPClient.Response mockResponse, response;

            props = new Properties();
            props.put("clearAfterRequest", "true");
            cfg = new Configuration(props);
            httpClient = new HTTPClientMockImpl();
            httpClient.configure(cfg);

            mockResponse = new HTTPClient.Response(new URL("http://aaa.com"), 404, "Not found", "bbb bbb", "text/plain", Collections.<HttpCookie>emptyList());
            httpClient.mockSetResponse(mockResponse);
            response = httpClient.get(new HTTPClient.Request("http://aaa.com/2", "utf-8"));
            assertEquals(new URL("http://aaa.com"), response.getUrl());
            assertEquals(404, response.getStatus());
            assertEquals("Not found", response.getMessage());
            assertEquals("bbb bbb", response.getContent());
            assertEquals("text/plain", response.getContentType());
            response = httpClient.get(new HTTPClient.Request("http://aaa.com/1", "utf-8"));
            assertEquals(new URL("http://aaa.com/1"), response.getUrl());
            assertEquals(200, response.getStatus());
            assertEquals("Ok", response.getMessage());
            assertNull(response.getContent());
            assertEquals("text/html", response.getContentType());

            props = new Properties();
            props.put("clearAfterRequest", "false");
            cfg = new Configuration(props);
            httpClient = new HTTPClientMockImpl();
            httpClient.configure(cfg);

            mockResponse = new HTTPClient.Response(new URL("http://aaa.com"), 404, "Not found", "bbb bbb", "text/plain", Collections.<HttpCookie>emptyList());
            httpClient.mockSetResponse(mockResponse);
            response = httpClient.get(new HTTPClient.Request("http://aaa.com/2", "utf-8"));
            assertEquals(new URL("http://aaa.com"), response.getUrl());
            assertEquals(404, response.getStatus());
            assertEquals("Not found", response.getMessage());
            assertEquals("bbb bbb", response.getContent());
            assertEquals("text/plain", response.getContentType());
            response = httpClient.get(new HTTPClient.Request("http://aaa.com/2", "utf-8"));
            assertEquals(new URL("http://aaa.com"), response.getUrl());
            assertEquals(404, response.getStatus());
            assertEquals("Not found", response.getMessage());
            assertEquals("bbb bbb", response.getContent());
            assertEquals("text/plain", response.getContentType());

            props = new Properties();
            props.put("clearAfterRequest", "false");
            cfg = new Configuration(props);
            httpClient = new HTTPClientMockImpl();
            httpClient.configure(cfg);

            httpClient.mockSetResponseContent("ddd ddd");
            response = httpClient.get(new HTTPClient.Request("http://aaa.com/1", "utf-8"));
            assertEquals(new URL("http://aaa.com/1"), response.getUrl());
            assertEquals(200, response.getStatus());
            assertEquals("Ok", response.getMessage());
            assertEquals("ddd ddd", response.getContent());
            assertEquals("text/html", response.getContentType());
            response = httpClient.get(new HTTPClient.Request("http://aaa.com/1", "utf-8"));
            assertEquals(new URL("http://aaa.com/1"), response.getUrl());
            assertEquals(200, response.getStatus());
            assertEquals("Ok", response.getMessage());
            assertEquals("ddd ddd", response.getContent());
            assertEquals("text/html", response.getContentType());

        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

}
