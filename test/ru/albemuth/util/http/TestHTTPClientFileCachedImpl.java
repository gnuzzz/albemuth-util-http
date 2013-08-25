package ru.albemuth.util.http;

import junit.framework.TestCase;

import java.util.Properties;
import java.io.FileOutputStream;

import ru.albemuth.util.Configuration;

/**
 * Created by IntelliJ IDEA.
 * User: vovan
 * Date: 06.03.2008
 * Time: 21:06:27
 */
public class TestHTTPClientFileCachedImpl extends TestCase {

    public void test() {
        try {
            Properties props = new Properties();
            props.put("ru.albemuth.util.http.HTTPClientFileCachedImpl.cache-root-path", System.getenv("TEMP") + "/HTTPClientCache/");
            HTTPClient http = new HTTPClientFileCachedImpl();
            http.configure(new Configuration(props));
            http.get(new HTTPClient.Request("http://www.biblion.ru", "Cp1251"));
            http.get(new HTTPClient.Request("http://www.biblion.ru", "Cp1251"));
            FileOutputStream out;
            out = new FileOutputStream(System.getenv("TEMP") + "/a.gif");
            http.get(new HTTPClient.Request("http://www.biblion.ru/i/logo.gif", "Cp1251"), out);
            out.close();
            out = new FileOutputStream(System.getenv("TEMP") + "/b.gif");
            http.get(new HTTPClient.Request("http://www.biblion.ru/i/logo.gif", "Cp1251"), out);
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

}
