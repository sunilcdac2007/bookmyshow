package bookmyshow.gateway.authservice.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CaptchaValidateResponse {
    private int statusCode;
    private String responseStatus;
    @JsonProperty(value="isValidCaptcha")
    private boolean isValidCaptcha;

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getResponseStatus() {
        return responseStatus;
    }

    public void setResponseStatus(String responseStatus) {
        this.responseStatus = responseStatus;
    }

    public boolean isValidCaptcha() {
        return isValidCaptcha;
    }

    public void setValidCaptcha(boolean validCaptcha) {
        isValidCaptcha = validCaptcha;
    }
}
