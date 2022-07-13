package bookmyshow.gateway.authservice.model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "PortalOTPReq")
public class AuthwrapperGenerateOTPRequest {
    private String uid;
    private String txnID;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getTxnID() {
        return txnID;
    }

    public void setTxnID(String txnID) {
        this.txnID = txnID;
    }
}
