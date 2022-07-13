package bookmyshow.gateway.authservice.model;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "PortalTOTPResp")
@Getter
@Setter
public class AuthwrapperOTPResponse {
    String result;
    String responseCode;
    String message;
    String txnID;
}
