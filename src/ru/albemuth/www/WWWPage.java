package ru.albemuth.www;

import ru.albemuth.util.http.HTTPClient;

/**
 * Created by IntelliJ IDEA.
 * User: vovan
 * Date: 18.01.2008
 * Time: 15:55:31
 */
public abstract class WWWPage {

    protected abstract HTTPClient.Request getNextRequest() throws WWWException;

    public abstract WWWPage getNextPage() throws WWWException;

    public abstract void process() throws WWWException;


}
