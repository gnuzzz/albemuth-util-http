package ru.albemuth.www.rapidshare;

import ru.albemuth.www.WWWPage;
import ru.albemuth.www.WWWException;
import ru.albemuth.util.http.HTTPClient;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Created by IntelliJ IDEA.
 * User: vovan
 * Date: 18.01.2008
 * Time: 16:38:45
 */
public class SelectDownloadTypePage extends RapidsharePage {

    public SelectDownloadTypePage(RapidsharePageProcessor processor, HTTPClient.Response rapidshareResponse) {
        super(processor, rapidshareResponse);
    }

    protected HTTPClient.Request getNextRequest() throws WWWException {
        return getFreeDownloadTypeRequest();
    }

    public WWWPage getNextPage() throws WWWException {
        return getProcessor().getDownloadLinkPage(getNextRequest());
    }

    public void process() {}

    protected HTTPClient.Request getFreeDownloadTypeRequest() throws WWWException {
        Pattern p = Pattern.compile("<form action=\"(http://[^\"]+)\" method=\"post\">");
        Matcher m = p.matcher(rapidshareResponse.getContent());
        if (m.find()) {
            return new HTTPClient.Request(m.group(1), null, new String[][]{{"Referer", rapidshareResponse.getUrl().toString()}}, new String[][]{{"dl.start", "Free"}}, "utf-8");
        } else {
            throw new WWWException("Can't find free download url for file " + rapidshareResponse.getUrl(), getRapidshareResponse().getContent());
        }
    }

}
