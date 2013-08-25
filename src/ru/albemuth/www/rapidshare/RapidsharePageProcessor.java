package ru.albemuth.www.rapidshare;

import ru.albemuth.www.WWWPageProcessor;
import ru.albemuth.www.WWWPage;
import ru.albemuth.www.WWWException;
import ru.albemuth.util.http.HTTPClient;
import ru.albemuth.util.http.HTTPException;
import ru.albemuth.util.Configuration;
import ru.albemuth.util.ConfigurationException;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: vovan
 * Date: 18.01.2008
 * Time: 16:40:31
 */
public class RapidsharePageProcessor extends WWWPageProcessor {

    private String dataPath;

    public void configure(Configuration cfg) throws ConfigurationException {
        super.configure(cfg);
        dataPath = cfg.getStringValue(this, "data-path");
    }

    public WWWPage getFirstPage(HTTPClient.Request request) throws WWWException {
        return getSelectDownloadTypePage(request);
    }

    protected SelectDownloadTypePage getSelectDownloadTypePage(HTTPClient.Request request) throws WWWException {
        try {
            HTTPClient.Response response = httpClient.get(request);
            return new SelectDownloadTypePage(this, response);
        } catch (HTTPException e) {
            throw new WWWException("Can't get select download type page: can't process request " + request, e, null);
        }
    }

    protected DownloadLinkPage getDownloadLinkPage(HTTPClient.Request request) throws WWWException {
        try {
            HTTPClient.Response response = httpClient.post(request);
            return new DownloadLinkPage(this, response);
        } catch (HTTPException e) {
            throw new WWWException("Can't get download link page: can't process request " + request, e, null);
        }
    }

    protected String loadCaptchaImage(String imageURL) throws WWWException {
        try {
            String imageFilePath = dataPath + imageURL.substring(imageURL.lastIndexOf('/') + 1);
            FileOutputStream out = new FileOutputStream(imageFilePath);
            httpClient.post(new HTTPClient.Request(imageURL, getEncoding()), out);
            out.close();
            return imageFilePath;
        } catch (HTTPException e) {
            throw new WWWException("Can't load captcha image", e, null);
        } catch (IOException e) {
            throw new WWWException("Can't store loaded captcha image " + imageURL, e, null);
        }
    }

    protected String loadRapidshareContent(HTTPClient.Request request) throws WWWException {
        try {
            String contentFilePath = dataPath + request.getUrl().substring(request.getUrl().lastIndexOf('/') + 1);
            FileOutputStream out = new FileOutputStream(contentFilePath);
            httpClient.post(request, out);
            out.close();
            return contentFilePath;
        } catch (HTTPException e) {
            throw new WWWException("Can't load captcha image", e, null);
        } catch (IOException e) {
            throw new WWWException("Can't store loaded captcha image " + request.getUrl(), e, null);
        }
    }

}
