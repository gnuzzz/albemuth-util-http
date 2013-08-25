package ru.albemuth.www.rapidshare;

import ru.albemuth.www.WWWPageProcessor;
import ru.albemuth.www.WWWException;
import ru.albemuth.util.Configuration;
import ru.albemuth.util.http.HTTPClient;

import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: vovan
 * Date: 21.01.2008
 * Time: 16:08:03
 */
public class Download {

    private static final Logger LOG                         = Logger.getLogger(Download.class);

    /**
     *
     * @param args
     *
     * usage: java ru.albemuth.www.rapidshare.Download <rapidshare file link>  
     * use http://rapidshare.com/files/85400370/log.zip.html for tests
     *
     */
    public static void main(String[] args) {
        try {
            WWWPageProcessor processor = new RapidsharePageProcessor();
            processor.configure(new Configuration("/rapidshare.properties"));

            processor.process(new HTTPClient.Request(args[0], "utf-8"));
        } catch (WWWException e) {
            LOG.error("Can't process rapidshare download from " + args[0], e);
            LOG.error(e.getPageContent());
        } catch (Exception e) {
            LOG.error("Can't process rapidshare download from " + args[0], e);
        }
    }

}
