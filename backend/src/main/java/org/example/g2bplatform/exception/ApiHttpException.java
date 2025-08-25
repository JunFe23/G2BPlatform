package org.example.g2bplatform.exception;

public class ApiHttpException extends RuntimeException {
    private final int status;
    private final String body;

    public ApiHttpException(int status, String body, String message) {
        super(message);
        this.status = status;
        this.body = body;
    }

    public int getStatus() { return status; }
    public String getBody() { return body; }
}
