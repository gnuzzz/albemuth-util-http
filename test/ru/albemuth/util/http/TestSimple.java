package ru.albemuth.util.http;

import junit.framework.TestCase;
import ru.albemuth.util.Configuration;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Properties;

public class TestSimple extends TestCase {

    public void test() {
        try {
            /*HTTPClient httpClient = new HTTPClientCommonsHttpClientImpl();
            Properties p = new Properties();
            p.put("untrusted-ssl", "false");
            httpClient.configure(new Configuration(p));

            //HTTPClient.Request request = new HTTPClient.Request("https://www.biblion.ru/", new String[][] {}, "utf-8");
            //HTTPClient.Request request = new HTTPClient.Request("https://www.platron.ru/", new String[][] {}, "utf-8");
            HTTPClient.Request request = new HTTPClient.Request("https://www.google.ru/", new String[][] {}, "utf-8");
            HTTPClient.Response response = httpClient.get(request);
            System.out.println(response.getContent());*/

            //URL url = new URL("https://www.google.com/");
            URL url = new URL("https://www.platron.ru/");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            HttpURLConnection.setFollowRedirects(true);
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();

            System.out.println(responseCode);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }


}