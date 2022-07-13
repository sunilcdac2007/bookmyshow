package bookmyshow.gateway.authservice.model;

public class CaptchaResponseToFrontent {

    private boolean isSessionActive;
    private String message;

    public boolean isSessionActive() {
        return isSessionActive;
    }

    public void setSessionActive(boolean sessionActive) {
        isSessionActive = sessionActive;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
