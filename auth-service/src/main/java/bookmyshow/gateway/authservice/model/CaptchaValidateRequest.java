package bookmyshow.gateway.authservice.model;

public class CaptchaValidateRequest {
    private String osName;
    private String deviceId;
    private String apkVersionName;
    private String apkVersionId;
    private String apiKey;
    private String timestamp;
    private String servicePoint;
    private String deviceHashKey;
    private String mobile;
    private String tx;
    private String langCode;
    private String captchaTxnId;
    private String captcha;

    public String getOsName() {
        return osName;
    }

    public void setOsName(String osName) {
        this.osName = osName;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getApkVersionName() {
        return apkVersionName;
    }

    public void setApkVersionName(String apkVersionName) {
        this.apkVersionName = apkVersionName;
    }

    public String getApkVersionId() {
        return apkVersionId;
    }

    public void setApkVersionId(String apkVersionId) {
        this.apkVersionId = apkVersionId;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getServicePoint() {
        return servicePoint;
    }

    public void setServicePoint(String servicePoint) {
        this.servicePoint = servicePoint;
    }

    public String getDeviceHashKey() {
        return deviceHashKey;
    }

    public void setDeviceHashKey(String deviceHashKey) {
        this.deviceHashKey = deviceHashKey;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getTx() {
        return tx;
    }

    public void setTx(String tx) {
        this.tx = tx;
    }

    public String getLangCode() {
        return langCode;
    }

    public void setLangCode(String langCode) {
        this.langCode = langCode;
    }

    public String getCaptchaTxnId() {
        return captchaTxnId;
    }

    public void setCaptchaTxnId(String captchaTxnId) {
        this.captchaTxnId = captchaTxnId;
    }

    public String getCaptcha() {
        return captcha;
    }

    public void setCaptcha(String captcha) {
        this.captcha = captcha;
    }
}
