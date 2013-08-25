package ru.albemuth.util.http;

import junit.framework.TestCase;

import java.util.Properties;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import ru.albemuth.util.Configuration;

/**
 * Created by IntelliJ IDEA.
 * User: vovan
 * Date: 07.03.2008
 * Time: 16:35:45
 */
public class TestHTTPProxyPool extends TestCase {

    public void testNothing() {/* do nothing*/}

    public void _testLoadProxies() {
        try {
            Properties props = new Properties();
            props.put("cache-root-path", "C:/Vovan/Lang/albemuth/HTTPClientCache/");
            Configuration cfg = new Configuration(props);
            HTTPClient http = new HTTPClientFileCachedImpl();
            http.configure(cfg);

            Pattern p = Pattern.compile("<tr><td><span class=\"proxy29641\">(\\d+)</span>.<span class=\"proxy29641\">(\\d+)</span>.(\\d+).(\\d+):\\s*(\\d+)</td><td>(?:high-)?anonymous proxy server</td><td>", Pattern.DOTALL);
            for (int i = 1; i <= 41; i++) {
                //System.out.println(i);
                HTTPClient.Request request = new HTTPClient.Request("http://www.samair.ru/proxy/proxy-" + (i < 10 ? "0" + i : "" + i) + ".htm", "Windows-1251");
                HTTPClient.Response response = http.get(request);
                Matcher m = p.matcher(response.getContent());
                for (; m.find(); ) {
                    System.out.println(m.group(1) + "." + m.group(2) + "." + m.group(3) + "." + m.group(4) + ":" + m.group(5));
                }
                //Thread.sleep(3000);
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
        
    }

    public void _test() {
        try {
            Properties props = new Properties();
            props.put("ru.albemuth.util.http.HTTPProxyPool.proxy-check-pattern", "This site is under construction");
            props.put("ru.albemuth.util.http.HTTPProxyPool.proxy-check-timeout", "-1");
            props.put("ru.albemuth.util.http.HTTPProxyPool.proxies-filepath", "C:/Vovan/Lang/albemuth/AccountRegistrator/temp/p.txt");
            props.put("ru.albemuth.util.http.HTTPProxyPool.proxy-check-url", "http://albemuth.ru");
            Configuration cfg = new Configuration(props);

            HTTPProxyPool pool = new HTTPProxyPool();
            pool.configure(cfg);

/*            HTTPClient http = new HTTPClientDefaultImpl();
            http.configure(cfg);*/
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

}
