package bookmyshow.gateway.authservice.model;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "PortalTOTPReq")
@Getter
@Setter
public class AuthwrapperOTPRequest {
    private String uid;
    private String totp;
    private String txnID;
}
