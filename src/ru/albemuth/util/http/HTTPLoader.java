package ru.albemuth.util.http;

import ru.albemuth.util.Accessor;
import ru.albemuth.util.Configuration;
import ru.albemuth.util.ConfigurationException;

import java.util.Properties;

public class HTTPLoader {

    public static String load(String url) throws HTTPException {
        HTTPClient httpClient = Accessor.getAccessor(HTTPClient.class).getDefaultInstance();
        if (httpClient == null) {
            Configuration cfg = Accessor.getAccessor(Configuration.class).getDefaultInstance();
            if (cfg == null) {
                cfg = new Configuration(new Properties());
            }
            try {
                httpClient = Configuration.createInstance(HTTPClient.class, cfg.getStringValue(null, "httpClient.class", HTTPClientDefaultImpl.class.getName()));
                httpClient.configure(cfg);
            } catch (ConfigurationException  e) {
                throw new HTTPException("Can't configure http client", e);
            }
        }
        HTTPClient.Response response = httpClient.get(new HTTPClient.Request(url, "utf-8"));
        return response.getContent();
    }


}
