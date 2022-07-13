package bookmyshow.gateway.authservice.model;


public class PortalGenerateOTPResponse {

    private boolean isSessionActive;
    private boolean status;
    private String txnId;
    private String message;

    public boolean isSessionActive() {
        return isSessionActive;
    }

    public void setSessionActive(boolean sessionActive) {
        isSessionActive = sessionActive;
    }

    public boolean isStatus() {
        return status;
    }

    public String getTxnId() {
        return txnId;
    }

    public String getMessage() {
        return message;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public void setTxnId(String txnId) {
        this.txnId = txnId;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
