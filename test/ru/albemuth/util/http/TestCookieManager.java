package ru.albemuth.util.http;

import org.junit.Test;
import ru.albemuth.util.Configuration;

import java.net.HttpCookie;
import java.net.URL;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author VKornyshev                                        s
 */
public class TestCookieManager {

    @Test
    public void testIsExpired() {
        try {
            /*CookieManagerImpl cm = new CookieManagerImpl();
            cm.configure(new Configuration(new Properties()));
            Calendar c = Calendar.getInstance();

            c.add(Calendar.MONTH, -1);
            assertTrue(cm.isExpired(new HttpCookie("name", "value", "domain", "path", c.getTime(), true, 1)));

            c.add(Calendar.MONTH, 2);
            assertFalse(cm.isExpired(new HttpCookie("name", "value", "domain", "path", c.getTime(), true, 1)));

            assertFalse(cm.isExpired(new HttpCookie("name", "value", "domain", "path", null, true, 1)));*/
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testAccept() {
        try {
            /*CookieManagerImpl cm = new CookieManagerImpl();
            cm.configure(new Configuration(new Properties()));

            assertTrue(cm.accept(new HttpCookie("name", "value", "www.biblion.ru", "/", new Date(), true, 1), new URL("http://www.biblion.ru")));
            assertTrue(cm.accept(new HttpCookie("name", "value", "www.biblion.ru", "/", new Date(), true, 1), new URL("http://www.biblion.ru/")));
            assertTrue(cm.accept(new HttpCookie("name", "value", ".biblion.ru", "/", new Date(), true, 1), new URL("http://www.biblion.ru/")));
            assertTrue(cm.accept(new HttpCookie("name", "value", ".biblion.ru", "/", new Date(), true, 1), new URL("http://biblion.ru/")));
            assertFalse(cm.accept(new HttpCookie("name", "value", "biblion.ru", "/", new Date(), true, 1), new URL("http://www.biblion.ru/")));
            assertFalse(cm.accept(new HttpCookie("name", "value", "test.biblion.ru", "/", new Date(), true, 1), new URL("http://www.biblion.ru/")));
            assertFalse(cm.accept(new HttpCookie("name", "value", "test.biblion.ru", "/", new Date(), true, 1), new URL("http://biblion.ru/")));
            assertTrue(cm.accept(new HttpCookie("name", "value", "www.biblion.ru", "/", new Date(), true, 1), new URL("http://www.biblion.ru/a")));
            assertTrue(cm.accept(new HttpCookie("name", "value", "www.biblion.ru", "/a", new Date(), true, 1), new URL("http://www.biblion.ru/a")));
            assertTrue(cm.accept(new HttpCookie("name", "value", "www.biblion.ru", "/a", new Date(), true, 1), new URL("http://www.biblion.ru/a/b")));
            assertFalse(cm.accept(new HttpCookie("name", "value", "www.biblion.ru", "/a", new Date(), true, 1), new URL("http://www.biblion.ru/b")));
            assertFalse(cm.accept(new HttpCookie("name", "value", "www.biblion.ru", "/a/b", new Date(), true, 1), new URL("http://www.biblion.ru/b/c")));*/
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testGetCookies() {
        try {
            /*CookieManagerImpl cm = new CookieManagerImpl();
            cm.configure(new Configuration(new Properties()));

            cm.addCookies(Arrays.asList(
                    new HttpCookie("cookie", "www.biblion.ru/", "www.biblion.ru", "/", null, true, 1),
                    new HttpCookie("cookie", "www.biblion.ru/aaa/", "www.biblion.ru", "/aaa/", null, true, 1),
                    new HttpCookie("cookie", ".biblion.ru/bbb/", ".biblion.ru", "/bbb/", null, true, 1),
                    new HttpCookie("cookie", "www.biblion.ru/aaa/bbb/", "www.biblion.ru", "/aaa/bbb/", null, true, 1)
            ));

            assertEquals("www.biblion.ru/", cm.getCookies(new URL("http://www.biblion.ru")).toArray(new HttpCookie[1])[0].getValue());
            assertEquals("www.biblion.ru/aaa/", cm.getCookies(new URL("http://www.biblion.ru/aaa/")).toArray(new HttpCookie[1])[0].getValue());
            assertEquals("www.biblion.ru/aaa/", cm.getCookies(new URL("http://www.biblion.ru/aaa/ccc/")).toArray(new HttpCookie[1])[0].getValue());
            assertEquals(".biblion.ru/bbb/", cm.getCookies(new URL("http://www.biblion.ru/bbb/")).toArray(new HttpCookie[1])[0].getValue());
            assertEquals(".biblion.ru/bbb/", cm.getCookies(new URL("http://biblion.ru/bbb/")).toArray(new HttpCookie[1])[0].getValue());
            assertEquals("www.biblion.ru/aaa/bbb/", cm.getCookies(new URL("http://www.biblion.ru/aaa/bbb/")).toArray(new HttpCookie[1])[0].getValue());
            assertEquals(0, cm.getCookies(new URL("http://biblion.ru/ccc/")).size());
                */
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }
}
