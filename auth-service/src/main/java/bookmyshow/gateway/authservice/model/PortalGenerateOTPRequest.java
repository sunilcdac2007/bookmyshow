package bookmyshow.gateway.authservice.model;

public class PortalGenerateOTPRequest {

    private String uid;
    private String captcha;
    private String captchaTxnId;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getCaptcha() {
        return captcha;
    }

    public void setCaptcha(String captcha) {
        this.captcha = captcha;
    }

    public String getCaptchaTxnId() {
        return captchaTxnId;
    }

    public void setCaptchaTxnId(String captchaTxnId) {
        this.captchaTxnId = captchaTxnId;
    }
}
