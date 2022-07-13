package bookmyshow.gateway.authservice.model;

public class CaptchaResponse {
    private String responseStatus;
    private int statusCode;
    private String message;
    private String captchaBase64String;
    private String captchaTrnasactionId;

    public String getResponseStatus() {
        return responseStatus;
    }

    public void setResponseStatus(String responseStatus) {
        this.responseStatus = responseStatus;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCaptchaBase64String() {
        return captchaBase64String;
    }

    public void setCaptchaBase64String(String captchaBase64String) {
        this.captchaBase64String = captchaBase64String;
    }

    public String getCaptchaTrnasactionId() {
        return captchaTrnasactionId;
    }

    public void setCaptchaTrnasactionId(String captchaTrnasactionId) {
        this.captchaTrnasactionId = captchaTrnasactionId;
    }
}
