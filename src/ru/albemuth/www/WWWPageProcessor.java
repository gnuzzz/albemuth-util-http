package ru.albemuth.www;

import ru.albemuth.util.http.HTTPClient;
import ru.albemuth.util.http.HTTPClientDefaultImpl;
import ru.albemuth.util.Configuration;
import ru.albemuth.util.ConfigurationException;
import ru.albemuth.util.Configured;

/**
 * Created by IntelliJ IDEA.
 * User: vovan
 * Date: 18.01.2008
 * Time: 15:55:40
 */
public abstract class WWWPageProcessor implements Configured {

    protected HTTPClient httpClient;
    protected String encoding;

    public void configure(Configuration cfg) throws ConfigurationException {
        httpClient = Configuration.createInstance(HTTPClient.class, cfg.getStringValue(this, "http-client-classname", HTTPClientDefaultImpl.class.getName()));
        httpClient.configure(cfg);
        encoding = cfg.getStringValue(this, "encoding", "utf-8");
    }

    public String getEncoding() {
        return encoding;
    }

    public void process(HTTPClient.Request request) throws WWWException {
        for (WWWPage page = getFirstPage(request); page != null; page = page.getNextPage()) {
            page.process();
        }
    }

    public abstract WWWPage getFirstPage(HTTPClient.Request request) throws WWWException;

}
