package ru.albemuth.www;

/**
 * Created by IntelliJ IDEA.
 * User: vovan
 * Date: 18.01.2008
 * Time: 15:57:07
 */
public class WWWException extends Exception {

    private String pageContent;

    public WWWException(String message, String pageContent) {
        super(message);
        this.pageContent = pageContent;
    }

    public WWWException(String message, Throwable cause, String pageContent) {
        super(message, cause);
        this.pageContent = pageContent;
    }

    public WWWException(Throwable cause, String pageContent) {
        super(cause);
        this.pageContent = pageContent;
    }

    public String getPageContent() {
        return pageContent;
    }
    
}
