package ru.albemuth.util.http;

import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import ru.albemuth.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: vovan
 * Date: 07.03.2008
 * Time: 15:29:29
 */
public class HTTPProxyPool implements Configured {

    private static final Logger LOG                 = Logger.getLogger(HTTPProxyPool.class);

    List<HTTPClient.Proxy> proxies;
    private Pattern proxyCheckPattern;
    private long proxyPoolTimeout;
    private long proxyCheckTimeout;
    private List<ProxyChecker> checkers;
    private int checkersNumber;

    public void configure(Configuration cfg) throws ConfigurationException {
        proxyCheckPattern = Pattern.compile(cfg.getStringValue(this, "proxy-check-pattern"));
        proxyPoolTimeout = cfg.getLongValue(this, "proxy-pool-timeout", -1);
        proxyCheckTimeout = cfg.getLongValue(this, "proxy-check-timeout", -1);
        checkersNumber = cfg.getIntValue(this, "checkers-number", 10);
        String proxiesFilepath = cfg.getStringValue(this, "proxies-filepath");
        try {
            check(cfg, new HTTPClient.Request(cfg.getStringValue(this, "proxy-check-url"), cfg.getStringValue(this, "encoding", "utf-8")), loadProxiesFromFile(proxiesFilepath));
            //proxiesPool = new ProxyPool(proxies, cfg.getIntValue(this, "proxy-pool-action", Pool.WAIT_WHEN_EXHAUSTED), cfg.getLongValue(this, "proxy-pool-timeout", 4000));
System.out.println(proxies.size());
        } catch (LoadException e) {
            throw new ConfigurationException("Can't load proxies from file " + proxiesFilepath, e);
        }
    }

    //public synchronized getAva

    public synchronized HTTPClient.Proxy getProxy() throws HTTPException {
        if (proxies.size() == 0) {
            try {
                wait(proxyPoolTimeout);
            } catch (InterruptedException e) {
                throw new HTTPException("InterruptedException while waiting for proxy instance", e);
            }
            if (proxies.size() == 0) {
                throw new HTTPException("Can't obtain proxy instance from pool: timeout");
            }
        }
        return proxies.remove(RandomGenerator.randomInt(0, proxies.size()));
    }

    public synchronized void returnProxy(HTTPClient.Proxy proxy) throws HTTPException {
        proxies.add(proxy);
    }

    protected List<HTTPClient.Proxy> loadProxiesFromFile(String proxiesFilePath) throws LoadException {
        Loader.SimpleLoader loader = new Loader.SimpleLoader();
        loader.load(proxiesFilePath);
        List<HTTPClient.Proxy> proxies = new ArrayList<HTTPClient.Proxy>();
        for (String proxyString: loader.getData()) {
            if (proxyString.trim().length() > 0) {
                try {
                    proxies.add(HTTPClient.Proxy.createProxy(proxyString));
                } catch (HTTPException e) {
                    throw new LoadException("Can't load proxies from file " + proxiesFilePath, e);
                }
            }
        }
        return proxies;
    }

    protected synchronized void check(Configuration cfg, HTTPClient.Request request, List<HTTPClient.Proxy> proxies) throws ConfigurationException {
        this.proxies = new LinkedList<HTTPClient.Proxy>();
        checkers = new LinkedList<ProxyChecker>();
        List<HTTPClient.Proxy> chekersProxies = null;
        int proxiesPerChecker = proxies.size() / checkersNumber + (proxies.size() % checkersNumber == 0 ? 0 : 1);
        for (HTTPClient.Proxy proxy: proxies) {
            if (chekersProxies == null) {
                chekersProxies = new LinkedList<HTTPClient.Proxy>();
            }
            chekersProxies.add(proxy);
            if (chekersProxies.size() >= proxiesPerChecker) {
                HTTPClient httpClient = Configuration.createInstance(HTTPClient.class, cfg.getStringValue(this, "httpclient", HTTPClientDefaultImpl.class.getName()));
                httpClient.configure(cfg);
                ProxyChecker checker = new ProxyChecker(httpClient, request, chekersProxies);
                checkers.add(checker);
                new Thread(checker).start();
                chekersProxies = null;
            }
        }
        if (chekersProxies != null) {
            HTTPClient httpClient = Configuration.createInstance(HTTPClient.class, cfg.getStringValue(this, "httpclient", HTTPClientDefaultImpl.class.getName()));
            httpClient.configure(cfg);
            ProxyChecker checker = new ProxyChecker(httpClient, request, chekersProxies);
            checkers.add(checker);
            new Thread(checker).start();
        }
        if (checkers.size() > 0) {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new ConfigurationException("InterruptedException while waiting for proxy checkers", e);
            }
        }
    }

    protected boolean check(HTTPClient httpClient, HTTPClient.Request request, HTTPClient.Proxy proxy) {
        try {
            httpClient.setProxy(proxy);
            long t1 = System.currentTimeMillis();
            HTTPClient.Response response = httpClient.get(request);
            long t2 = System.currentTimeMillis();
            return proxyCheckPattern.matcher(response.getContent()).find() && (proxyCheckTimeout <= 0 || t2 - t1 <= proxyCheckTimeout);
        } catch (HTTPException e) {
            LOG.warn("HTTP exception while checking proxy " + proxy + ": " + e.getMessage());
            return false;
        }
    }

    protected synchronized void addAliveProxies(ProxyChecker checker, List<HTTPClient.Proxy> aliveProxies) {
        proxies.addAll(aliveProxies);
        checkers.remove(checker);
        if (checkers.size() == 0) {
            notify();
        }
    }

    class ProxyChecker implements Runnable {

        private HTTPClient httpClient;
        private HTTPClient.Request request;
        private List<HTTPClient.Proxy> proxies;

        public ProxyChecker(HTTPClient httpClient, HTTPClient.Request request, List<HTTPClient.Proxy> proxies) {
            this.httpClient = httpClient;
            this.request = request;
            this.proxies = proxies;
        }

        public void run() {
            List<HTTPClient.Proxy> aliveProxies = new LinkedList<HTTPClient.Proxy>();
            try {
                for (HTTPClient.Proxy proxy: proxies) {
                    if (check(httpClient, request, proxy)) {
                        aliveProxies.add(proxy);
                    }
                }
            } catch (Exception e) {
                LOG.error("Unexpected exception", e);
            }
            addAliveProxies(this, aliveProxies);
        }

    }

}
