package ru.albemuth.www.rapidshare;

import ru.albemuth.www.WWWPage;
import ru.albemuth.util.http.HTTPClient;

/**
 * Created by IntelliJ IDEA.
 * User: vovan
 * Date: 18.01.2008
 * Time: 16:54:57
 */
public abstract class RapidsharePage extends WWWPage {

    protected RapidsharePageProcessor processor;
    protected HTTPClient.Response rapidshareResponse;

    public RapidsharePage(RapidsharePageProcessor processor, HTTPClient.Response rapidshareResponse) {
        this.processor = processor;
        this.rapidshareResponse = rapidshareResponse;
    }

    public RapidsharePageProcessor getProcessor() {
        return processor;
    }

    public HTTPClient.Response getRapidshareResponse() {
        return rapidshareResponse;
    }

}
