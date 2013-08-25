package ru.albemuth.www.rapidshare;

import ru.albemuth.www.WWWPage;
import ru.albemuth.www.WWWException;
import ru.albemuth.util.http.HTTPClient;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.net.URLDecoder;
import java.io.UnsupportedEncodingException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

import org.apache.log4j.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: vovan
 * Date: 18.01.2008
 * Time: 16:39:50
 */
public class DownloadLinkPage extends RapidsharePage {

    private static final Logger LOG                     = Logger.getLogger(DownloadLinkPage.class);

    protected String captchaImageFilePath;
    protected String captchaCode;
    private String dlFormHTML;
    private String timeoutForDownloadAvailable;
    protected String rapidshareContentFilePath;

    public DownloadLinkPage(RapidsharePageProcessor processor, HTTPClient.Response rapidshareResponse) {
        super(processor, rapidshareResponse);
    }

    protected HTTPClient.Request getNextRequest() {
        return null;//this is the last page in rapidshare download
    }

    public WWWPage getNextPage() throws WWWException {
        return null;
    }

    public void process() throws WWWException {
        LOG.debug("Processing DownloadLinkPage " + getRapidshareResponse().getUrl());
        if (isDownloadAvailable()) {
            waitForDownloadTicket();
            loadCaptchaImage();
            resolveCaptchaCode();
            downloadRapidshareContent();
            notifyDownloadComplete();
        } else {
            notifyNextAvailableDownloadTime();
        }
    }

    protected boolean isDownloadAvailable() {
        Pattern p = Pattern.compile("Instant download-access! \\(Or wait (\\d+) minute(?:s)?\\)");
        Matcher m = p.matcher(rapidshareResponse.getContent());
        if (m.find()) {
            timeoutForDownloadAvailable = m.group(1);
            return false;
        } else {
            timeoutForDownloadAvailable = null;
            return true;
        }
    }

    protected void notifyNextAvailableDownloadTime() {
System.out.println("Next available download time: " + timeoutForDownloadAvailable + " minutes");//todo: implement this
    }

    protected void waitForDownloadTicket() throws WWWException {
        Pattern p = Pattern.compile("<script>var c=(\\d+); var d=c/\\d+; fc\\(\\);");
        Matcher m = p.matcher(rapidshareResponse.getContent());
        if (m.find()) {
            long timeout = Integer.parseInt(m.group(1)) * 1000;
            notifyWaitForDownloadTicketTimeout(timeout);
            try {
                synchronized(this) {
                    wait(timeout != 0 ? timeout : 1);
                }
            } catch (InterruptedException e) {
                throw new WWWException("InterruptedException while waiting for download timeout", e, getRapidshareResponse().getContent());
            }
        }
    }

    protected void notifyWaitForDownloadTicketTimeout(long timeout) {
System.out.println("Waiting " + (timeout / 1000) + " seconds");//todo: implement this
    }

    protected void loadCaptchaImage() throws WWWException {
        try {
            Pattern p = Pattern.compile("unescape\\('([^']+)'\\)");
            Matcher m = p.matcher(rapidshareResponse.getContent());
            if (m.find()) {
                dlFormHTML = URLDecoder.decode(m.group(1), processor.getEncoding());
            } else {
                dlFormHTML = rapidshareResponse.getContent();
            }
            p = Pattern.compile("No premium user. Please enter<br>(?:'\\s*\\+\\s*')?<img src=\"([^\"]+)\"><br>here:");
            m = p.matcher(dlFormHTML);
            if (m.find()) {
                captchaImageFilePath = processor.loadCaptchaImage(m.group(1));
            } else {
                throw new WWWException("Can't load captcha image: can't find captcha image src url", getRapidshareResponse().getContent());
            }
        } catch (UnsupportedEncodingException e) {
            throw new WWWException("Can't load captcha image: can't decode download link form HTML - encoding " + processor.getEncoding() + " isn't supported", e, getRapidshareResponse().getContent());
        }
    }

    protected void resolveCaptchaCode() throws WWWException {
        System.out.println("Captcha file loaded to " + captchaImageFilePath);
        System.out.print("Please, enter captcha code: ");
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        try {
            captchaCode = in.readLine();
        } catch (IOException e) {
            throw new WWWException("Can't resolve captcha code: can't read user's input", e, getRapidshareResponse().getContent());
        }
    }

    protected void downloadRapidshareContent() throws WWWException {
        Pattern p = Pattern.compile("<input checked type=\"radio\" name=\"mirror\" onclick=\"document.dl.action=\\\\'([^\\\\']+)\\\\';document.dl.actionstring.value=\\\\'([^\\\\']+)\\\\'\">");
        Matcher m = p.matcher(dlFormHTML);
        if (m.find()) {
            rapidshareContentFilePath = processor.loadRapidshareContent(new HTTPClient.Request(m.group(1), null, new String[][]{{"Referer", rapidshareResponse.getUrl().toString()}}, new String[][]{{"mirror", "on"}, {"accesscode", captchaCode}, {"actionstring", m.group(2)}}, processor.getEncoding()));
        } else {
            throw new WWWException("Can't download rapidshare content: can't find mirror pattern", getRapidshareResponse().getContent());
        }
    }

    protected void notifyDownloadComplete() {
System.out.println("Download complete, content loaded to " + rapidshareContentFilePath);//todo: implement this
    }

}
