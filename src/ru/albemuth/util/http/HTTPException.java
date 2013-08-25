package ru.albemuth.util.http;

public class HTTPException extends Exception {

    private HTTPClient.Request request;
    private HTTPClient.Response response;

    public HTTPException(String message) {
        super(message);
    }

    public HTTPException(String message, HTTPClient.Request request) {
        super(message);
        this.request = request;
    }

    public HTTPException(String message, HTTPClient.Request request, HTTPClient.Response response) {
        super(message);
        this.request = request;
        this.response = response;
    }

    public HTTPException(String message, Throwable cause) {
        super(message, cause);
    }

    public HTTPException(Throwable cause) {
        super(cause);
    }

    public HTTPClient.Request getRequest() {
        return request;
    }

    public void setRequest(HTTPClient.Request request) {
        this.request = request;
    }

    public HTTPClient.Response getResponse() {
        return response;
    }

    public void setResponse(HTTPClient.Response response) {
        this.response = response;
    }

}
