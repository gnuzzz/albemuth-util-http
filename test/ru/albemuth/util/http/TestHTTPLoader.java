package ru.albemuth.util.http;

import org.junit.Test;
import ru.albemuth.util.Accessor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class TestHTTPLoader {

    @Test
    public void testLoad() {
        try {
            HTTPClientMockImpl httpClient = new HTTPClientMockImpl();
            Accessor.getAccessor(HTTPClient.class).setDefaultInstance(httpClient);
            httpClient.mockSetResponseContent("aaa aaa");

            String content = HTTPLoader.load("http://aaa.com");
            assertEquals("aaa aaa", content);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

}
