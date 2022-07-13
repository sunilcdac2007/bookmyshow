package bookmyshow.gateway.authservice.model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "PortalOTPResp")
public class AuthwrapperGenerateOTPResponse {

    private String responseCode;
    private String message;
    private String result;

    public String getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
