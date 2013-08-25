package ru.albemuth.util.http;

import org.junit.Test;
import ru.albemuth.util.Configuration;

import java.net.HttpCookie;
import java.util.Properties;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class TestHTTPClientCommonsHttpClientImpl {

    @Test
    public void test() {
        try {
            HTTPClient httpClient = new HTTPClientCommonsHttpClientImpl();
            httpClient.configure(new Configuration(new Properties()));

            HTTPClient.Request request = new HTTPClient.Request("https://ishop.qiwi.ru/xml", "utf-8");
            /*HTTPClient.Response response = httpClient.post(request, "<?xml version=\"1.0\" encoding=\"utf-8\"?> \n" +
                    "<request> \n" +
                    "    <protocol-version>4.00</protocol-version> \n" +
                    "    <request-type>30</request-type> \n" +
                    "        <terminal-id>6620</terminal-id> \n" +
                    "        <extra name=\"password\">hff6yas</extra> \n" +
                    "        <extra name=\"txn-id\">10</extra> \n" +
                    "        <extra name=\"to-account\">9165741282</extra> \n" +
                    "        <extra name=\"amount\">98413</extra> \n" +
                    "        <extra name=\"comment\">test</extra> \n" +
                    "        <extra name=\"create-agt\">1</extra> \n" +
                    "        <extra name=\"ltime\">48.5</extra> \n" +
                    "        <extra name=\"ALARM_SMS\">1</extra> \n" +
                    "        <extra name=\"ACCEPT_CALL\">0</extra> \n" +
                    "</request>");*/
            HTTPClient.Response response = httpClient.post(request,"aaaaa");
            System.out.println(response.getStatus() + "\n" + response.getContent());
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testCookieSupport() {
        try {
            HTTPClient httpClient = new HTTPClientCommonsHttpClientImpl();
            httpClient.configure(new Configuration(new Properties()));
            HTTPClient.Request request = new HTTPClient.Request("https://my.beeline.ru", "utf-8");
            HTTPClient.Response response = httpClient.get(request);
            HttpCookie jsessionidCookie = null;
            for (HttpCookie cookie: response.getCookies()) {
                if ("JSESSIONID".equals(cookie.getName())) {
                    jsessionidCookie = cookie;
                }
            }
            assertNotNull(jsessionidCookie);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

}
